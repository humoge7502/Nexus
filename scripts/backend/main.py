"""
NEXUS data collection API — scrapes internship and hackathon sources.

Deploy on Google Cloud Run. Scrape endpoints are protected by a shared secret
(`NEXUS_API_KEY`); when the variable is absent the endpoints fail closed.
"""

from datetime import datetime
import hashlib
import hmac
import html
import logging
import os
import re
import time
from typing import List, Optional
from urllib.parse import urlparse

from fastapi import Depends, FastAPI, Header, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware

logger = logging.getLogger("nexus.scraper")
logging.basicConfig(level=logging.INFO)

API_KEY = os.getenv("NEXUS_API_KEY", "").strip()
ALLOWED_ORIGINS = [
    origin.strip()
    for origin in os.getenv("ALLOWED_ORIGINS", "").split(",")
    if origin.strip()
]
MAX_RESULTS = 100
MAX_KEYWORD_LENGTH = 100
MAX_LOCATION_LENGTH = 80
MAX_TEXT_LENGTH = 4_000
MAX_DESCRIPTION_LENGTH = 12_000
MAX_URL_LENGTH = 2_048

app = FastAPI(
    title="NEXUS Data API",
    docs_url=None,
    redoc_url=None,
    openapi_url=None,
)

if ALLOWED_ORIGINS:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=ALLOWED_ORIGINS,
        allow_methods=["GET"],
        allow_headers=["X-API-Key"],
        allow_credentials=False,
    )

FIREBASE_ENABLED = os.getenv("FIREBASE_ENABLED", "false").lower() == "true"

if FIREBASE_ENABLED:
    import firebase_admin
    from firebase_admin import credentials, firestore

    cred = credentials.ApplicationDefault()
    firebase_admin.initialize_app(cred)
    db = firestore.client()

TECH_SKILLS = [
    "python",
    "java",
    "kotlin",
    "javascript",
    "typescript",
    "react",
    "node.js",
    "django",
    "spring",
    "flutter",
    "sql",
    "mongodb",
    "postgresql",
    "aws",
    "gcp",
    "azure",
    "docker",
    "kubernetes",
    "machine learning",
    "tensorflow",
    "pytorch",
    "data structures",
    "algorithms",
    "system design",
    "rest api",
    "git",
    "ci/cd",
    "agile",
    "microservices",
    "html",
    "css",
    "graphql",
    "redis",
    "kafka",
]

_KEYWORD_PATTERN = re.compile(r"^[\w\s.,'&+()/-]+$", re.UNICODE)
_TAG_PATTERN = re.compile(r"<[^>]+>")


def require_api_key(x_api_key: Optional[str] = Header(default=None)) -> None:
    if not API_KEY:
        raise HTTPException(
            status_code=503, detail="Scraper authentication is not configured."
        )
    if not x_api_key or not hmac.compare_digest(x_api_key, API_KEY):
        raise HTTPException(status_code=401, detail="Invalid API key.")


def clean_text(value: object, limit: int = MAX_TEXT_LENGTH) -> str:
    if value is None:
        return ""
    text = html.unescape(_TAG_PATTERN.sub(" ", str(value)))
    text = re.sub(r"\s+", " ", text).strip()
    return text[:limit]


def clean_url(value: object) -> str:
    if value is None:
        return ""
    candidate = str(value).strip()
    if not candidate:
        return ""
    if candidate.startswith("/"):
        candidate = f"https://devpost.com{candidate}"
    try:
        parsed = urlparse(candidate)
    except ValueError:
        return ""
    if parsed.scheme not in ("http", "https") or not parsed.netloc:
        return ""
    # Truncating a URL silently redirects users somewhere else; reject instead.
    if len(candidate) > MAX_URL_LENGTH:
        return ""
    return candidate


def extract_skills(description: str) -> List[str]:
    desc_lower = description.lower()
    return [s.title() for s in TECH_SKILLS if s in desc_lower]


def stable_id(prefix: str, *parts: str) -> str:
    digest = hashlib.sha1("\u0001".join(parts).encode("utf-8")).hexdigest()[:24]
    return f"{prefix}_{digest}"


def validate_keywords(keywords: str) -> str:
    cleaned = keywords.strip()
    if not cleaned or len(cleaned) > MAX_KEYWORD_LENGTH:
        raise HTTPException(status_code=422, detail="Invalid search keywords.")
    if not _KEYWORD_PATTERN.match(cleaned):
        raise HTTPException(status_code=422, detail="Invalid search keywords.")
    return cleaned


def validate_location(location: str) -> str:
    cleaned = location.strip()
    if not cleaned or len(cleaned) > MAX_LOCATION_LENGTH:
        raise HTTPException(status_code=422, detail="Invalid location.")
    if not _KEYWORD_PATTERN.match(cleaned):
        raise HTTPException(status_code=422, detail="Invalid location.")
    return cleaned


def write_opportunities(documents: List[dict]) -> None:
    if not FIREBASE_ENABLED or not documents:
        return
    batch = db.batch()
    for document in documents:
        doc_ref = db.collection("opportunities").document(document["id"])
        batch.set(
            doc_ref,
            {**document, "createdAt": firestore.SERVER_TIMESTAMP},
            merge=True,
        )
    batch.commit()


@app.get("/health")
def health_check():
    return {
        "status": "ok",
        "timestamp": datetime.now().isoformat(),
        "firestore": FIREBASE_ENABLED,
    }


@app.get("/scrape/internships", dependencies=[Depends(require_api_key)])
def scrape_internships(
    keywords: str = Query("software engineering internship"),
    location: str = Query("India"),
    count: int = Query(50, ge=1, le=MAX_RESULTS),
):
    keywords = validate_keywords(keywords)
    location = validate_location(location)

    try:
        from jobspy import scrape_jobs

        jobs = scrape_jobs(
            site_name=["linkedin", "indeed", "glassdoor"],
            search_term=keywords,
            location=location,
            results_wanted=count,
            hours_old=48,
            country_indeed="India",
            job_type="internship" if "intern" in keywords.lower() else None,
        )
    except ImportError:
        logger.exception("jobspy is not installed")
        raise HTTPException(status_code=503, detail="Scraper dependency unavailable.")
    except Exception:
        logger.exception("Internship scrape failed")
        raise HTTPException(status_code=502, detail="Upstream scrape failed.")

    results: List[dict] = []
    documents: List[dict] = []
    seen_urls = set()
    now = datetime.now()

    for _, job in jobs.iterrows():
        apply_url = clean_url(job.get("job_url"))
        if not apply_url or apply_url in seen_urls:
            continue
        seen_urls.add(apply_url)

        description = clean_text(job.get("description"), MAX_DESCRIPTION_LENGTH)
        title = clean_text(job.get("title"), 300)
        company = clean_text(job.get("company"), 200)
        job_location = clean_text(job.get("location"), 200)
        skills = extract_skills(description)
        remote = "remote" in job_location.lower()

        results.append(
            {
                "title": title,
                "company": company,
                "company_logo": clean_url(job.get("company_logo_url")),
                "location": job_location,
                "description": description,
                "apply_url": apply_url,
                "is_remote": remote,
                "stipend": clean_text(job.get("salary_source"), 120),
                "skills": skills,
                "source": clean_text(job.get("site"), 40),
                "posted_at": now.isoformat(),
            }
        )

        documents.append(
            {
                "id": stable_id("internship", apply_url),
                "type": "INTERNSHIP",
                "title": title,
                "role": title,
                "company": company,
                "companyLogo": clean_url(job.get("company_logo_url")),
                "description": description,
                "applyUrl": apply_url,
                "applicationUrl": apply_url,
                "location": job_location,
                "locationType": "REMOTE" if remote else "ONSITE",
                "stipend": clean_text(job.get("salary_source"), 120),
                "duration": "",
                "requiredSkills": skills,
                "preferredSkills": [],
                "collegeTiersEligible": [],
                "domain": "Software Engineering",
                "tags": [clean_text(job.get("site"), 40)],
                "isActive": True,
                "postedAt": int(now.timestamp() * 1000),
                "matchScore": 0.0,
            }
        )

    try:
        write_opportunities(documents)
    except Exception:
        logger.exception("Failed to persist scraped internships")

    return {"success": True, "count": len(results), "data": results}


@app.get("/scrape/hackathons", dependencies=[Depends(require_api_key)])
def scrape_hackathons(count: int = Query(20, ge=1, le=MAX_RESULTS)):
    try:
        import requests
        from bs4 import BeautifulSoup

        url = "https://devpost.com/hackathons?status=open&page=1"
        response = requests.get(
            url,
            headers={"User-Agent": "NexusDataBot/1.0 (+https://nexusapp.in)"},
            timeout=15,
        )
        response.raise_for_status()
        soup = BeautifulSoup(response.text, "html.parser")
    except ImportError:
        logger.exception("Hackathon scraper dependency missing")
        raise HTTPException(status_code=503, detail="Scraper dependency unavailable.")
    except Exception:
        logger.exception("Hackathon scrape failed")
        raise HTTPException(status_code=502, detail="Upstream scrape failed.")

    results: List[dict] = []
    documents: List[dict] = []
    seen_urls = set()
    now = datetime.now()

    for card in soup.select(".challenge-listing")[:count]:
        title_el = card.select_one(".challenge-title")
        if not title_el:
            continue
        title = clean_text(title_el.get_text(strip=True), 300)
        link = clean_url(title_el.get("href"))
        if not title or not link or link in seen_urls:
            continue
        seen_urls.add(link)

        date_el = card.select_one(".challenge-date")
        desc_el = card.select_one(".challenge-description")
        deadline = clean_text(date_el.get_text(strip=True), 120) if date_el else ""
        description = (
            clean_text(desc_el.get_text(strip=True), MAX_DESCRIPTION_LENGTH)
            if desc_el
            else ""
        )

        results.append(
            {
                "title": title,
                "url": link,
                "date": deadline,
                "description": description,
                "source": "devpost",
            }
        )

        documents.append(
            {
                "id": stable_id("hackathon", link),
                "type": "HACKATHON",
                "title": title,
                "role": title,
                "company": "Devpost",
                "description": description,
                "applyUrl": link,
                "applicationUrl": link,
                "location": "Online",
                "locationType": "REMOTE",
                "stipend": "",
                "duration": deadline,
                "requiredSkills": [],
                "preferredSkills": [],
                "collegeTiersEligible": [],
                "domain": "Hackathon",
                "tags": ["Devpost"],
                "isActive": True,
                "postedAt": int(now.timestamp() * 1000),
                "matchScore": 0.0,
            }
        )

    try:
        write_opportunities(documents)
    except Exception:
        logger.exception("Failed to persist scraped hackathons")

    return {"success": True, "count": len(results), "data": results}

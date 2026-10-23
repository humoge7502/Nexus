"""
NEXUS Backend API — Job scraping, hackathon data, certification data
Deploy on Google Cloud Run
"""

from fastapi import FastAPI, Query
from fastapi.middleware.cors import CORSMiddleware
from typing import Optional
from datetime import datetime
import os
import json

app = FastAPI(title="NEXUS Backend API")
app.add_middleware(
    CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"]
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


def extract_skills(description: str) -> list:
    desc_lower = description.lower()
    return [s for s in TECH_SKILLS if s in desc_lower]


@app.get("/health")
def health_check():
    return {"status": "ok", "timestamp": datetime.now().isoformat()}


@app.get("/scrape/internships")
async def scrape_internships(
    keywords: str = Query(
        "software engineering internship", description="Search keywords"
    ),
    location: str = Query("India", description="Location"),
    count: int = Query(50, description="Number of results", le=100),
):
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
        results = []
        for _, job in jobs.iterrows():
            entry = {
                "title": job.get("title", ""),
                "company": job.get("company", ""),
                "company_logo": job.get("company_logo_url", ""),
                "location": job.get("location", ""),
                "description": job.get("description", ""),
                "apply_url": job.get("job_url", ""),
                "is_remote": "remote" in str(job.get("location", "")).lower(),
                "stipend": str(job.get("salary_source", "") or ""),
                "skills": extract_skills(job.get("description", "")),
                "source": job.get("site", ""),
                "posted_at": datetime.now().isoformat(),
            }
            results.append(entry)
        if FIREBASE_ENABLED:
            batch = db.batch()
            for entry in results:
                doc_ref = db.collection("opportunities").document()
                batch.set(doc_ref, {**entry, "createdAt": firestore.SERVER_TIMESTAMP})
            batch.commit()
        return {"success": True, "count": len(results), "data": results}
    except ImportError:
        return {"success": False, "error": "jobspy not installed", "data": []}
    except Exception as e:
        return {"success": False, "error": str(e), "data": []}


@app.get("/scrape/hackathons")
async def scrape_hackathons():
    try:
        import requests
        from bs4 import BeautifulSoup

        url = "https://devpost.com/hackathons?status=open&page=1"
        headers = {"User-Agent": "Mozilla/5.0"}
        resp = requests.get(url, headers=headers, timeout=15)
        soup = BeautifulSoup(resp.text, "html.parser")
        results = []
        for card in soup.select(".challenge-listing")[:20]:
            title_el = card.select_one(".challenge-title")
            date_el = card.select_one(".challenge-date")
            desc_el = card.select_one(".challenge-description")
            if title_el:
                results.append(
                    {
                        "title": title_el.get_text(strip=True),
                        "url": title_el.get("href", ""),
                        "date": date_el.get_text(strip=True) if date_el else "",
                        "description": desc_el.get_text(strip=True) if desc_el else "",
                        "source": "devpost",
                    }
                )
        return {"success": True, "count": len(results), "data": results}
    except Exception as e:
        return {"success": False, "error": str(e), "data": []}

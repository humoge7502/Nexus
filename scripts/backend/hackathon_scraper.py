import os
import re
import requests
from bs4 import BeautifulSoup
from datetime import datetime
from typing import Optional

DEVPOST_BASE = "https://devpost.com/hackathons"
HEADERS = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"}

FIREBASE_ENABLED = os.getenv("FIREBASE_ENABLED", "false").lower() == "true"
if FIREBASE_ENABLED:
    import firebase_admin
    from firebase_admin import credentials, firestore

    cred = credentials.ApplicationDefault()
    firebase_admin.initialize_app(cred)
    db = firestore.client()


def parse_deadline(text: str) -> Optional[str]:
    if not text:
        return None
    patterns = [
        r"(\w+ \d{1,2},?\s*\d{4})",
        r"(\d{1,2}/\d{1,2}/\d{4})",
        r"(\d{4}-\d{2}-\d{2})",
    ]
    for p in patterns:
        m = re.search(p, text)
        if m:
            return m.group(1)
    return text.strip()


def parse_prizes(text: str) -> list:
    amounts = re.findall(r"\$[\d,]+(?:\.\d{2})?", text)
    return [a.strip() for a in amounts] if amounts else []


def scrape_devpost(page: int = 1, status: str = "open") -> list:
    url = f"{DEVPOST_BASE}?status={status}&page={page}"
    resp = requests.get(url, headers=HEADERS, timeout=15)
    resp.raise_for_status()
    soup = BeautifulSoup(resp.text, "html.parser")
    results = []

    challenge_cards = soup.select("article.challenge-listing, .challenge-listing")
    if not challenge_cards:
        challenge_cards = soup.select("section#challenge-list li, div.challenge-entry")

    for card in challenge_cards:
        title_el = card.select_one(
            ".challenge-title a, h2 a, a[href*='devpost.com/software']"
        )
        if not title_el:
            continue

        title = title_el.get_text(strip=True)
        href = title_el.get("href", "")

        date_el = card.select_one(".challenge-date, .date, time, .entry-date")
        deadline = parse_deadline(date_el.get_text(strip=True)) if date_el else None

        prize_el = card.select_one(".challenge-prize, .prize-amount, .prizes")
        prizes = parse_prizes(prize_el.get_text(strip=True)) if prize_el else []

        themes_el = card.select_one(".challenge-themes, .themes, .tags")
        themes = []
        if themes_el:
            themes = [t.get_text(strip=True) for t in themes_el.select("a, span, .tag")]

        participants_el = card.select_one(
            ".challenge-participants, .participants, .entry-count"
        )
        participants = 0
        if participants_el:
            nums = re.findall(r"\d+", participants_el.get_text(strip=True))
            if nums:
                participants = int(nums[0])

        entry = {
            "title": title,
            "url": href,
            "deadline": deadline,
            "prizes": prizes,
            "themes": themes,
            "participants": participants,
            "source": "devpost",
            "status": status,
            "scraped_at": datetime.now().isoformat(),
        }
        results.append(entry)

    return results


def scrape_multiple_pages(max_pages: int = 3, status: str = "open") -> list:
    all_hackathons = []
    for page in range(1, max_pages + 1):
        try:
            data = scrape_devpost(page=page, status=status)
            if not data:
                break
            all_hackathons.extend(data)
        except Exception:
            break
    return all_hackathons


def store_to_firestore(hackathons: list) -> int:
    if not FIREBASE_ENABLED:
        return 0
    batch = db.batch()
    count = 0
    for entry in hackathons:
        doc_ref = db.collection("hackathons").document()
        batch.set(doc_ref, {**entry, "createdAt": firestore.SERVER_TIMESTAMP})
        count += 1
        if count % 500 == 0:
            batch.commit()
            batch = db.batch()
    if count % 500 != 0:
        batch.commit()
    return count


if __name__ == "__main__":
    import json

    print(f"Scraping Devpost... ({datetime.now().isoformat()})")
    hackathons = scrape_multiple_pages(max_pages=3)
    print(f"Found {len(hackathons)} hackathons")
    if FIREBASE_ENABLED:
        stored = store_to_firestore(hackathons)
        print(f"Stored {stored} to Firestore")
    else:
        print(json.dumps(hackathons, indent=2))

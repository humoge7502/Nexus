"""
NEXUS LeetCode Sync - Fetches problem data via alfa-leetcode-api
Outputs JSON ready for Firestore ingestion
"""

import json
from datetime import datetime
from typing import Optional
import requests

ALFA_LEETCODE_API = "https://alfa-leetcode-api.onrender.com"


def fetch_problems(limit: int = 50, skip: int = 0) -> list[dict]:
    """Fetch problems from alfa-leetcode-api."""
    url = f"{ALFA_LEETCODE_API}/problems"
    params = {"limit": limit, "skip": skip}
    try:
        resp = requests.get(url, params=params, timeout=15)
        resp.raise_for_status()
        data = resp.json()
        return data.get("problemsetQuestionList", [])
    except Exception as e:
        print(f"Failed to fetch problems: {e}")
        return []


def normalize_problem(raw: dict) -> dict:
    """Normalize a LeetCode problem into NEXUS Firestore format."""
    return {
        "id": raw.get("questionFrontendId", ""),
        "title": raw.get("title", ""),
        "titleSlug": raw.get("titleSlug", ""),
        "difficulty": raw.get("difficulty", "MEDIUM"),
        "platform": "LeetCode",
        "url": f"https://leetcode.com/problems/{raw.get('titleSlug', '')}/",
        "topics": [
            tag.strip() for tag in raw.get("topicTags", "").split(",") if tag.strip()
        ]
        if isinstance(raw.get("topicTags"), str)
        else [
            t.get("name", "") for t in raw.get("topicTags", []) if isinstance(t, dict)
        ],
        "is_premium": raw.get("isPaidOnly", False),
        "ac_rate": raw.get("acRate", 0),
        "total_accepted": raw.get("totalAccepted", 0),
        "total_submissions": raw.get("totalSubmissions", 0),
        "sync_timestamp": datetime.now().isoformat(),
    }


def sync_all(max_problems: int = 200) -> list[dict]:
    """Fetch and normalize all problems, return as Firestore-ready list."""
    all_normalized = []
    skip = 0
    batch_size = 50
    while len(all_normalized) < max_problems:
        raw = fetch_problems(limit=batch_size, skip=skip)
        if not raw:
            break
        normalized = [normalize_problem(p) for p in raw]
        all_normalized.extend(normalized)
        skip += batch_size
        print(f"Fetched {len(normalized)} problems (total: {len(all_normalized)})")
    return all_normalized[:max_problems]


if __name__ == "__main__":
    problems = sync_all(200)
    output = {
        "problems": problems,
        "count": len(problems),
        "synced_at": datetime.now().isoformat(),
    }
    with open("leetcode_problems.json", "w") as f:
        json.dump(output, f, indent=2)
    print(f"Synced {len(problems)} problems to leetcode_problems.json")

"""
NEXUS LeetCode Sync - fetches problem metadata from LeetCode's public GraphQL API.
Outputs JSON ready for Firestore ingestion.

Run manually when refreshing the problem catalog:
    python scripts/leetcode_sync.py
"""

import json
from datetime import datetime

import requests

LEETCODE_GRAPHQL = "https://leetcode.com/graphql"

PROBLEMS_QUERY = """
query problemsetQuestionList($categorySlug: String, $limit: Int, $skip: Int) {
  questionList(
    categorySlug: $categorySlug
    limit: $limit
    skip: $skip
    filters: {}
  ) {
    totalNum
    data {
      questionFrontendId
      title
      titleSlug
      difficulty
      isPaidOnly
      acRate
      topicTags { name }
    }
  }
}
"""


def fetch_problems(limit: int = 50, skip: int = 0) -> list[dict]:
    """Fetch one page of problems from LeetCode GraphQL."""
    try:
        resp = requests.post(
            LEETCODE_GRAPHQL,
            json={
                "query": PROBLEMS_QUERY,
                "variables": {"categorySlug": "", "limit": limit, "skip": skip},
                "operationName": "problemsetQuestionList",
            },
            headers={
                "Content-Type": "application/json",
                "Referer": "https://leetcode.com",
            },
            timeout=15,
        )
        resp.raise_for_status()
        payload = resp.json()
        if payload.get("errors"):
            print(f"GraphQL error: {payload['errors']}")
            return []
        return payload.get("data", {}).get("questionList", {}).get("data", []) or []
    except Exception as e:
        print(f"Failed to fetch problems: {e}")
        return []


def normalize_problem(raw: dict) -> dict:
    """Normalize a LeetCode problem into NEXUS Firestore format."""
    tags = raw.get("topicTags") or []
    return {
        "id": raw.get("questionFrontendId", ""),
        "title": raw.get("title", ""),
        "titleSlug": raw.get("titleSlug", ""),
        "difficulty": (raw.get("difficulty") or "MEDIUM").upper(),
        "platform": "LeetCode",
        "url": f"https://leetcode.com/problems/{raw.get('titleSlug', '')}/",
        "topics": [t.get("name", "") for t in tags if isinstance(t, dict)],
        "is_premium": bool(raw.get("isPaidOnly", False)),
        "ac_rate": raw.get("acRate", 0),
        "sync_timestamp": datetime.now().isoformat(),
    }


def sync_all(max_problems: int = 200) -> list[dict]:
    """Fetch and normalize problems, returning a Firestore-ready list."""
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

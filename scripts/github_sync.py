"""
NEXUS GitHub Sync - Fetches contribution data from GitHub GraphQL API
Formats data for the Nexus Android app's profile and stats screens
"""

import json
import os
from datetime import datetime, timedelta
from typing import Optional
import requests

GITHUB_API = "https://api.github.com/graphql"
CONTRIBUTIONS_QUERY = """
query($username: String!, $from: DateTime!, $to: DateTime!) {
  user(login: $username) {
    contributionsCollection(from: $from, to: $to) {
      contributionCalendar {
        totalContributions
        weeks {
          contributionDays {
            contributionCount
            date
            weekday
          }
        }
      }
    }
    repositories(first: 50, orderBy: {field: UPDATED_AT, direction: DESC}) {
      nodes {
        name
        description
        url
        stargazerCount
        forkCount
        primaryLanguage { name }
        updatedAt
      }
    }
    pullRequests(first: 20, orderBy: {field: CREATED_AT, direction: DESC}) {
      totalCount
      nodes { title url state createdAt }
    }
    issues(first: 20, orderBy: {field: CREATED_AT, direction: DESC}) {
      totalCount
      nodes { title url state createdAt }
    }
  }
}
"""


def fetch_contributions(username: str, token: str, days: int = 365) -> Optional[dict]:
    """Fetch GitHub contribution data for the given user."""
    headers = {"Authorization": f"Bearer {token}"}
    to_date = datetime.now()
    from_date = to_date - timedelta(days=days)
    variables = {
        "username": username,
        "from": from_date.isoformat() + "Z",
        "to": to_date.isoformat() + "Z",
    }
    try:
        resp = requests.post(
            GITHUB_API,
            json={"query": CONTRIBUTIONS_QUERY, "variables": variables},
            headers=headers,
            timeout=15,
        )
        resp.raise_for_status()
        return resp.json()
    except Exception as e:
        print(f"GitHub API request failed: {e}")
        return None


def format_for_firestore(raw: dict) -> dict:
    """Transform GitHub API response into NEXUS Firestore document."""
    user_data = raw.get("data", {}).get("user", {})
    calendar = user_data.get("contributionsCollection", {}).get(
        "contributionCalendar", {}
    )
    weeks = calendar.get("weeks", [])

    daily_contributions = []
    for week in weeks:
        for day in week.get("contributionDays", []):
            daily_contributions.append(
                {
                    "date": day["date"],
                    "count": day["contributionCount"],
                }
            )

    repos = [
        {
            "name": r["name"],
            "description": r.get("description"),
            "url": r["url"],
            "stars": r["stargazerCount"],
            "forks": r["forkCount"],
            "language": r.get("primaryLanguage", {}).get("name")
            if r.get("primaryLanguage")
            else None,
            "updated_at": r.get("updatedAt"),
        }
        for r in user_data.get("repositories", {}).get("nodes", [])
    ]

    prs = user_data.get("pullRequests", {})
    issues = user_data.get("issues", {})

    return {
        "total_contributions": calendar.get("totalContributions", 0),
        "daily_contributions": daily_contributions,
        "repositories": repos,
        "pull_requests": {
            "total_count": prs.get("totalCount", 0),
            "recent": [
                {
                    "title": n["title"],
                    "url": n["url"],
                    "state": n["state"],
                    "created_at": n["createdAt"],
                }
                for n in prs.get("nodes", [])
            ],
        },
        "issues": {
            "total_count": issues.get("totalCount", 0),
            "recent": [
                {
                    "title": n["title"],
                    "url": n["url"],
                    "state": n["state"],
                    "created_at": n["createdAt"],
                }
                for n in issues.get("nodes", [])
            ],
        },
        "synced_at": datetime.now().isoformat(),
    }


if __name__ == "__main__":
    token = os.environ.get("GITHUB_TOKEN", "")
    username = os.environ.get("GITHUB_USERNAME", "")

    if not token or not username:
        print("Set GITHUB_TOKEN and GITHUB_USERNAME environment variables")
        exit(1)

    raw = fetch_contributions(username, token)
    if raw:
        formatted = format_for_firestore(raw)
        with open("github_contributions.json", "w") as f:
            json.dump(formatted, f, indent=2)
        print(f"Synced GitHub data for {username}")
    else:
        print("Failed to fetch GitHub data")

"""
NEXUS Job Scraper - Backend data collector
Deploy as Cloud Function or Cloud Run service
"""

import json
from datetime import datetime, timedelta
from typing import Optional
import requests
from dataclasses import dataclass, asdict


@dataclass
class Job:
    id: str
    title: str
    company: str
    company_logo: Optional[str]
    location: str
    is_remote: bool
    stipend: Optional[str]
    duration: str
    skills: list
    deadline: str
    apply_url: str
    description: str
    posted_at: str
    source: str


class JobScraper:
    """Scrapes job listings from multiple sources"""

    def __init__(self):
        self.headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        }

    def scrape_internshala(
        self, query: str = "internship", max_results: int = 50
    ) -> list[Job]:
        """Scrape Internshala for internships"""
        jobs = []
        try:
            url = f"https://internshala.com/internships/{query}-internship/"
            response = requests.get(url, headers=self.headers, timeout=10)
            print(f"Scraped {len(jobs)} jobs from Internshala")
        except Exception as e:
            print(f"Internshala scrape failed: {e}")
        return jobs

    def scrape_linkedin(self, query: str, max_results: int = 50) -> list[Job]:
        """Scrape LinkedIn Jobs"""
        return []

    def to_firestore_format(self, jobs: list[Job]) -> list[dict]:
        return [asdict(job) for job in jobs]


def lambda_handler(event, context):
    """Cloud Function entry point"""
    scraper = JobScraper()
    all_jobs = []
    all_jobs.extend(scraper.scrape_internshala())
    all_jobs.extend(scraper.scrape_linkedin("software engineering internship"))

    formatted = scraper.to_firestore_format(all_jobs)
    return {
        "statusCode": 200,
        "body": json.dumps(
            {"jobs_found": len(formatted), "timestamp": datetime.now().isoformat()}
        ),
    }


if __name__ == "__main__":
    result = lambda_handler(None, None)
    print(result)

"""Security and sanitization tests for the NEXUS scraper API.

Run:
    pip install -r requirements-dev.txt
    pytest -q
"""

from __future__ import annotations

import os
import sys
from pathlib import Path

import pytest
from fastapi.testclient import TestClient

sys.path.insert(0, str(Path(__file__).resolve().parent))
import main  # noqa: E402


@pytest.fixture()
def client(monkeypatch):
    monkeypatch.setattr(main, "API_KEY", "test-key")
    monkeypatch.setattr(main, "FIREBASE_ENABLED", False)
    return TestClient(main.app)


def test_health_is_public_and_does_not_leak_config(client):
    response = client.get("/health")
    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "ok"
    assert body["firestore"] is False
    assert "key" not in str(body).lower()


def test_scrape_requires_api_key(client):
    assert client.get("/scrape/internships").status_code == 401
    assert client.get("/scrape/internships", headers={"X-API-Key": "wrong"}).status_code == 401


def test_scrape_fails_closed_when_key_unconfigured(monkeypatch):
    monkeypatch.setattr(main, "API_KEY", "")
    unconfigured = TestClient(main.app)
    response = unconfigured.get("/scrape/internships", headers={"X-API-Key": "anything"})
    assert response.status_code == 503


def test_scrape_rejects_injection_shaped_keywords(client):
    headers = {"X-API-Key": "test-key"}
    response = client.get(
        "/scrape/internships",
        params={"keywords": "'; DROP TABLE jobs; --"},
        headers=headers,
    )
    assert response.status_code == 422
    assert client.get("/scrape/internships", params={"keywords": "x" * 200}, headers=headers).status_code == 422
    assert client.get("/scrape/hackathons", params={"count": 500}, headers=headers).status_code == 422


def test_stable_id_is_deterministic_and_bounded():
    first = main.stable_id("internship", "https://example.com/job/1")
    second = main.stable_id("internship", "https://example.com/job/1")
    other = main.stable_id("internship", "https://example.com/job/2")
    assert first == second
    assert first != other
    assert first.startswith("internship_")
    assert len(first) <= 40


def test_clean_url_blocks_active_content_and_keeps_https():
    assert main.clean_url("https://example.com/apply") == "https://example.com/apply"
    assert main.clean_url("javascript:alert(1)") == ""
    assert main.clean_url("data:text/html;base64,PHNjcmlwdD4=") == ""
    assert main.clean_url("file:///etc/passwd") == ""
    assert main.clean_url("not a url") == ""
    assert main.clean_url("https://example.com/" + "a" * 4000) == ""


def test_clean_text_strips_markup_and_truncates():
    assert main.clean_text("<b>Hello</b>  world") == "Hello world"
    assert main.clean_text(None) == ""
    assert len(main.clean_text("x" * 50, limit=10)) == 10


def test_extract_skills_is_deduplicated():
    skills = main.extract_skills("We use Python, python, Kotlin and AWS.")
    lowered = [skill.lower() for skill in skills]
    assert "python" in lowered
    assert "kotlin" in lowered
    assert len(lowered) == len(set(lowered))


def test_validation_rejects_blank_and_overlong_inputs():
    with pytest.raises(main.HTTPException):
        main.validate_keywords("   ")
    with pytest.raises(main.HTTPException):
        main.validate_location("x" * 200)
    assert main.validate_keywords("software intern") == "software intern"

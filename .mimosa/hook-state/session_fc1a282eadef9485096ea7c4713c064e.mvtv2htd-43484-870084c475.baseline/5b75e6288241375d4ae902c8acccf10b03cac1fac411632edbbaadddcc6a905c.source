import re

TECH_SKILLS = [
    "python",
    "java",
    "kotlin",
    "javascript",
    "typescript",
    "go",
    "rust",
    "c++",
    "c#",
    "swift",
    "ruby",
    "php",
    "scala",
    "r",
    "matlab",
    "perl",
    "react",
    "angular",
    "vue.js",
    "svelte",
    "next.js",
    "nuxt.js",
    "node.js",
    "express.js",
    "django",
    "flask",
    "spring",
    "spring boot",
    "fastapi",
    "asp.net",
    "rails",
    "laravel",
    "flutter",
    "react native",
    "jetpack compose",
    "swiftui",
    "sql",
    "mysql",
    "postgresql",
    "mongodb",
    "sqlite",
    "mariadb",
    "redis",
    "elasticsearch",
    "cassandra",
    "dynamodb",
    "firebase",
    "aws",
    "gcp",
    "azure",
    "heroku",
    "digitalocean",
    "vercel",
    "netlify",
    "docker",
    "kubernetes",
    "jenkins",
    "github actions",
    "gitlab ci",
    "terraform",
    "ansible",
    "prometheus",
    "grafana",
    "machine learning",
    "deep learning",
    "tensorflow",
    "pytorch",
    "scikit-learn",
    "keras",
    "nlp",
    "computer vision",
    "llm",
    "data science",
    "data analysis",
    "data engineering",
    "pandas",
    "numpy",
    "matplotlib",
    "tableau",
    "power bi",
    "html",
    "css",
    "sass",
    "tailwind css",
    "bootstrap",
    "graphql",
    "rest api",
    "grpc",
    "websocket",
    "git",
    "linux",
    "bash",
    "agile",
    "scrum",
    "ci/cd",
    "microservices",
    "event-driven",
    "kafka",
    "rabbitmq",
    "blockchain",
    "solidity",
    "web3",
    "smart contracts",
    "cybersecurity",
    "penetration testing",
    "ethical hacking",
]

SOFT_SKILLS = [
    "communication",
    "teamwork",
    "leadership",
    "problem solving",
    "critical thinking",
    "time management",
    "adaptability",
    "collaboration",
    "public speaking",
    "mentoring",
]

TOOLS = [
    "jira",
    "confluence",
    "notion",
    "figma",
    "sketch",
    "adobe xd",
    "photoshop",
    "illustrator",
    "blender",
    "unity",
    "unreal engine",
    "postman",
    "insomnia",
    "swagger",
    "datadog",
    "sentry",
    "vscode",
    "intellij",
    "android studio",
    "xcode",
]

FRAMEWORKS_BY_NAME = {
    "pytorch": "PyTorch",
    "tensorflow": "TensorFlow",
    "django": "Django",
    "flask": "Flask",
    "spring": "Spring",
    "spring boot": "Spring Boot",
    "react": "React",
    "angular": "Angular",
    "vue.js": "Vue.js",
    "svelte": "Svelte",
    "next.js": "Next.js",
    "node.js": "Node.js",
    "express.js": "Express.js",
    "fastapi": "FastAPI",
    "flutter": "Flutter",
    "rails": "Ruby on Rails",
    "laravel": "Laravel",
}

CATEGORY_MAP = {}
for s in TECH_SKILLS:
    CATEGORY_MAP[s] = "technical"
for s in SOFT_SKILLS:
    CATEGORY_MAP[s] = "soft"
for s in TOOLS:
    CATEGORY_MAP[s] = "tool"


def extract_skills(description: str) -> list:
    desc_lower = description.lower()
    matched = []
    seen = set()

    for skill_list, category in [
        (TECH_SKILLS, "technical"),
        (SOFT_SKILLS, "soft"),
        (TOOLS, "tool"),
    ]:
        for skill in skill_list:
            if skill in seen:
                continue
            pattern = (
                r"\b" + re.escape(skill) + r"\b"
                if " " not in skill
                else re.escape(skill)
            )
            if re.search(pattern, desc_lower):
                matched.append(
                    {
                        "name": FRAMEWORKS_BY_NAME.get(skill, skill.title()),
                        "category": category,
                        "raw": skill,
                    }
                )
                seen.add(skill)

    matched.sort(key=lambda x: x["category"])
    return matched


def extract_frameworks(description: str) -> list:
    desc_lower = description.lower()
    return [
        FRAMEWORKS_BY_NAME[name] for name in FRAMEWORKS_BY_NAME if name in desc_lower
    ]

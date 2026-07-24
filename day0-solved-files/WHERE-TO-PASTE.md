# Day 0 — nothing to paste

Day 0 is **preflight only**. See `StudentGuides/Day0-README.md` for:

- what SmartBudget is and why we build it,
- the full 10-day journey table,
- tech stack + architecture diagram,
- prerequisites (Java 25+, Maven, Node/npm, PostgreSQL, Docker),
- what already ships in the starter code,
- how the `// TODO TICKET-Fxxx` comment convention works.

No files change on Day 0, so this folder intentionally contains no code — only this
note. The first real code overlay lives in [`../day1-solved-files/`](../day1-solved-files/)
(SQL foundations).

**Before you start Day 1, verify:**

```bash
java -version        # 25.x  (or the version pom.xml pins)
mvn -v               # 3.8+
node -v && npm -v    # 22.x / 10.x
psql --version       # 15+
docker --version     # Docker Desktop running (only needed on Day 10)
```

If everything reports a version, you're ready for Day 1. If not, install what's missing
before touching any code — Day 1's SQL work assumes a running PostgreSQL on
`localhost:5432`.

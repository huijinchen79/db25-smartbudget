#!/bin/sh
# Phase 5 verification for the SmartBudget training project.
# Idempotent: resets backend/, frontend/, db/, docker-compose.yml,
# and the repo-root README.md to their first-commit state, then
# overlays each day's solved folder cumulatively and records:
#   - `mvn compile`  exit code
#   - `mvn test`     exit code + tests-run summary
#   - `npm run build` exit code (from Day 8 onwards, when frontend is dirty)
#
# The results table is written to phase5-results.md next to this script.
# Redirect long output to files — do not pipe to head/tail (SIGPIPE).

set -u  # unset variable is a fatal error
# NOTE: do NOT set -e; we want to keep going after per-day failures.

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$REPO_ROOT" || exit 1

LOG_DIR="$REPO_ROOT/phase5-logs"
RESULTS="$REPO_ROOT/phase5-results.md"
rm -rf "$LOG_DIR"
mkdir -p "$LOG_DIR"

# Force Java 25 (matches pom.xml <java.version>25</java.version>).
JAVA25="$(/usr/libexec/java_home -v 25 2>/dev/null || true)"
if [ -z "$JAVA25" ]; then
    echo "ERROR: JDK 25 not found via /usr/libexec/java_home -v 25" >&2
    exit 1
fi
export JAVA_HOME="$JAVA25"
export PATH="$JAVA_HOME/bin:$PATH"
echo "Using JAVA_HOME=$JAVA_HOME"

MVN="mvn -q -o=false"   # -q quiet; leave default settings

# --- Reset starter tree to first-commit state -------------------
first_commit="$(git rev-list --max-parents=0 HEAD)"
echo "First commit: $first_commit"

reset_starter() {
    git checkout "$first_commit" -- backend/ frontend/ db/ docker-compose.yml README.md 2>/dev/null
    # remove any tracked-but-not-in-first-commit paths from these trees
    for extra in .github frontend-static; do
        [ -d "$extra" ] && rm -rf "$extra"
    done
    # nuke maven and vite build outputs
    rm -rf backend/target frontend/dist frontend/node_modules/.vite 2>/dev/null
}

# --- Overlay a solved folder onto the working tree --------------
overlay_day() {
    day="$1"
    src="$REPO_ROOT/day${day}-solved-files"
    [ -d "$src" ] || { echo "  (no folder for day$day, skipping overlay)"; return; }
    for sub in backend frontend db .github frontend-static; do
        if [ -d "$src/$sub" ]; then
            cp -R "$src/$sub/" "$REPO_ROOT/$sub/" 2>&1 | tee -a "$LOG_DIR/day${day}-overlay.log"
        fi
    done
    if [ -f "$src/docker-compose.yml" ]; then
        cp "$src/docker-compose.yml" "$REPO_ROOT/docker-compose.yml"
    fi
    if [ -f "$src/README.md" ]; then
        cp "$src/README.md" "$REPO_ROOT/README.md"
    fi
}

# --- Build/test one snapshot ------------------------------------
# Writes to $LOG_DIR/dayN-{compile,test,frontend}.{log,exit}
run_backend_compile() {
    tag="$1"
    log="$LOG_DIR/${tag}-compile.log"
    ( cd "$REPO_ROOT/backend" && $MVN clean compile ) >"$log" 2>&1
    ec=$?
    echo "$ec" > "$LOG_DIR/${tag}-compile.exit"
    echo "  compile exit=$ec"
}

run_backend_test() {
    tag="$1"
    log="$LOG_DIR/${tag}-test.log"
    ( cd "$REPO_ROOT/backend" && $MVN test ) >"$log" 2>&1
    ec=$?
    echo "$ec" > "$LOG_DIR/${tag}-test.exit"
    # Extract "Tests run: X, Failures: Y, Errors: Z, Skipped: W" – last occurrence.
    summary="$(grep -E 'Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+' "$log" | tail -1)"
    [ -z "$summary" ] && summary="(no test summary — build failed before tests ran or no tests)"
    echo "$summary" > "$LOG_DIR/${tag}-test.summary"
    echo "  test exit=$ec  $summary"
}

run_frontend_build() {
    tag="$1"
    log="$LOG_DIR/${tag}-frontend.log"
    if [ ! -d "$REPO_ROOT/frontend" ]; then
        echo "SKIPPED (no frontend/)" > "$LOG_DIR/${tag}-frontend.exit"
        return
    fi
    ( cd "$REPO_ROOT/frontend" && npm install --silent && npm run build ) >"$log" 2>&1
    ec=$?
    echo "$ec" > "$LOG_DIR/${tag}-frontend.exit"
    echo "  frontend build exit=$ec"
}

record_row() {
    day="$1"
    ce=$(cat "$LOG_DIR/day${day}-compile.exit" 2>/dev/null || echo "-")
    te=$(cat "$LOG_DIR/day${day}-test.exit" 2>/dev/null || echo "-")
    ts=$(cat "$LOG_DIR/day${day}-test.summary" 2>/dev/null || echo "-")
    fe=$(cat "$LOG_DIR/day${day}-frontend.exit" 2>/dev/null || echo "-")
    printf "| Day %s | %s | %s | %s | %s |\n" \
        "$day" "$ce" "$te" "$ts" "$fe" >> "$RESULTS"
}

# --- Run --------------------------------------------------------
{
    echo "# Phase 5 — Cumulative build/test results"
    echo
    echo "First commit: \`$first_commit\`"
    echo "Java: \`$JAVA_HOME\`"
    echo
    echo "| Snapshot | mvn compile exit | mvn test exit | Test summary | frontend build exit |"
    echo "|----------|------------------|---------------|--------------|---------------------|"
} > "$RESULTS"

echo "== Baseline (starter, no overlays) =="
reset_starter
run_backend_compile baseline
run_backend_test baseline
# don't build frontend at baseline — nothing has changed yet
record_row baseline

for D in 1 2 3 4 5 6 7 8 9 10; do
    echo "== Overlay day${D} =="
    overlay_day "$D"
    run_backend_compile "day${D}"
    run_backend_test    "day${D}"
    if [ "$D" -ge 8 ]; then
        run_frontend_build "day${D}"
    fi
    record_row "day${D}"
done

echo
echo "Done. Summary in $RESULTS. Per-run logs in $LOG_DIR/."

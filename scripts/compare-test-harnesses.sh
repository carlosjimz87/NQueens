#!/usr/bin/env bash
set -euo pipefail

# ============================================================================
# compare-test-harnesses.sh
#
# Runs unit, integration, and instrumented tests on two branches,
# collects JUnit XML results, and generates a markdown comparison report.
#
# Usage:
#   ./scripts/compare-test-harnesses.sh [branch_a] [branch_b]
#   Default: feature/solo_testing vs feature/agent_testing
#
# Requirements:
#   - Emulator or device connected (for instrumented tests)
#   - Clean working tree (uncommitted changes will be stashed)
#   - xmllint (optional, for prettier parsing)
# ============================================================================

BRANCH_A="${1:-feature/solo_testing}"
BRANCH_B="${2:-feature/agent_testing}"
REPORT_DIR="$(pwd)/test-reports"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="${REPORT_DIR}/comparison_${TIMESTAMP}.md"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; }

mkdir -p "$REPORT_DIR"

# ---------------------------------------------------------------------------
# Parse JUnit XML results from a directory
# Outputs: total tests, passed, failed, errors, skipped, total_time_sec
# ---------------------------------------------------------------------------
parse_junit_xml() {
    local xml_dir="$1"
    local total=0 passed=0 failed=0 errors=0 skipped=0
    local total_time="0"

    if [[ ! -d "$xml_dir" ]]; then
        echo "0|0|0|0|0|0"
        return
    fi

    for xml_file in "$xml_dir"/*.xml; do
        [[ -f "$xml_file" ]] || continue

        # Extract attributes from <testsuite> root element
        local t f e s time_val
        t=$(grep -oP 'tests="[^"]*"' "$xml_file" | head -1 | grep -oP '"[^"]*"' | tr -d '"')
        f=$(grep -oP 'failures="[^"]*"' "$xml_file" | head -1 | grep -oP '"[^"]*"' | tr -d '"')
        e=$(grep -oP 'errors="[^"]*"' "$xml_file" | head -1 | grep -oP '"[^"]*"' | tr -d '"')
        s=$(grep -oP 'skipped="[^"]*"' "$xml_file" | head -1 | grep -oP '"[^"]*"' | tr -d '"')
        time_val=$(grep -oP 'time="[^"]*"' "$xml_file" | head -1 | grep -oP '"[^"]*"' | tr -d '"')

        total=$((total + ${t:-0}))
        failed=$((failed + ${f:-0}))
        errors=$((errors + ${e:-0}))
        skipped=$((skipped + ${s:-0}))
        total_time=$(echo "$total_time + ${time_val:-0}" | bc 2>/dev/null || echo "$total_time")
    done

    passed=$((total - failed - errors - skipped))
    echo "${total}|${passed}|${failed}|${errors}|${skipped}|${total_time}"
}

# ---------------------------------------------------------------------------
# Extract per-test timing details from JUnit XML
# Outputs lines: "classname.testname|time_sec"
# ---------------------------------------------------------------------------
extract_test_timings() {
    local xml_dir="$1"
    [[ -d "$xml_dir" ]] || return

    for xml_file in "$xml_dir"/*.xml; do
        [[ -f "$xml_file" ]] || continue
        # Match <testcase classname="..." name="..." time="...">
        grep -oP '<testcase\s+[^>]*classname="([^"]*)"[^>]*name="([^"]*)"[^>]*time="([^"]*)"' "$xml_file" \
            | sed -E 's/.*classname="([^"]*)".* name="([^"]*)".* time="([^"]*)".*/\1.\2|\3/' \
            2>/dev/null || true
    done
}

# ---------------------------------------------------------------------------
# Count test files and @Test methods from source tree
# ---------------------------------------------------------------------------
count_tests_from_source() {
    local branch="$1"
    local pattern="$2"  # e.g., "src/test" or "src/androidTest"

    local files
    files=$(git ls-tree -r "$branch" --name-only | grep -E "${pattern}/.*Test\.kt$" || true)

    local file_count=0
    local test_count=0

    while IFS= read -r f; do
        [[ -z "$f" ]] && continue
        file_count=$((file_count + 1))
        local c
        c=$(git show "${branch}:${f}" 2>/dev/null | grep -c "@Test" || echo 0)
        test_count=$((test_count + c))
    done <<< "$files"

    echo "${file_count}|${test_count}"
}

# ---------------------------------------------------------------------------
# Classify tests by layer from source
# ---------------------------------------------------------------------------
classify_tests() {
    local branch="$1"

    local unit_files=0 unit_tests=0
    local integration_files=0 integration_tests=0
    local ui_files=0 ui_tests=0
    local e2e_files=0 e2e_tests=0

    # All test .kt files
    local all_files
    all_files=$(git ls-tree -r "$branch" --name-only | grep -E "Test\.kt$" || true)

    while IFS= read -r f; do
        [[ -z "$f" ]] && continue
        local c
        c=$(git show "${branch}:${f}" 2>/dev/null | grep -c "@Test" || echo 0)
        [[ "$c" -eq 0 ]] && continue

        if echo "$f" | grep -q "src/androidTest"; then
            # Check if it's E2E (Activity-level) or component UI test
            local content
            content=$(git show "${branch}:${f}" 2>/dev/null)
            if echo "$content" | grep -q "createAndroidComposeRule<MainActivity>"; then
                e2e_files=$((e2e_files + 1))
                e2e_tests=$((e2e_tests + c))
            elif echo "$content" | grep -q "InstrumentedTest\|BoardScreenInstrumentedTest"; then
                e2e_files=$((e2e_files + 1))
                e2e_tests=$((e2e_tests + c))
            else
                ui_files=$((ui_files + 1))
                ui_tests=$((ui_tests + c))
            fi
        elif echo "$f" | grep -qiE "integration|IntegrationTest|WiringTest"; then
            integration_files=$((integration_files + 1))
            integration_tests=$((integration_tests + c))
        else
            unit_files=$((unit_files + 1))
            unit_tests=$((unit_tests + c))
        fi
    done <<< "$all_files"

    echo "${unit_files}|${unit_tests}|${integration_files}|${integration_tests}|${ui_files}|${ui_tests}|${e2e_files}|${e2e_tests}"
}

# ---------------------------------------------------------------------------
# Run tests on a branch and collect results
# ---------------------------------------------------------------------------
run_branch_tests() {
    local branch="$1"
    local label="$2"
    local result_dir="${REPORT_DIR}/${label}"

    mkdir -p "$result_dir"

    info "Checking out branch: $branch"
    git checkout "$branch" --quiet 2>/dev/null

    # --- Source-level metrics (before running) ---
    info "Classifying tests from source..."
    classify_tests "$branch" > "${result_dir}/classification.txt"

    # --- Unit tests ---
    info "Running unit tests on $branch..."
    local unit_exit=0
    ./gradlew testDebugUnitTest --no-daemon --quiet 2>&1 | tee "${result_dir}/unit_output.log" || unit_exit=$?

    # Copy JUnit XML results
    local unit_xml_src="app/build/test-results/testDebugUnitTest"
    if [[ -d "$unit_xml_src" ]]; then
        cp -r "$unit_xml_src" "${result_dir}/unit_xml"
    fi
    echo "$unit_exit" > "${result_dir}/unit_exit_code.txt"

    # --- Instrumented tests ---
    info "Running instrumented tests on $branch..."
    local instr_exit=0
    ./gradlew connectedDebugAndroidTest --no-daemon --quiet 2>&1 | tee "${result_dir}/instrumented_output.log" || instr_exit=$?

    # Copy instrumented JUnit XML results
    local instr_xml_src="app/build/outputs/androidTest-results/connected"
    if [[ -d "$instr_xml_src" ]]; then
        # Flatten: may have device subfolders
        mkdir -p "${result_dir}/instrumented_xml"
        find "$instr_xml_src" -name "*.xml" -exec cp {} "${result_dir}/instrumented_xml/" \;
    fi
    echo "$instr_exit" > "${result_dir}/instrumented_exit_code.txt"

    # --- Clean build outputs so they don't leak into next branch ---
    rm -rf app/build/test-results app/build/outputs/androidTest-results

    info "Finished tests on $branch"
}

# ---------------------------------------------------------------------------
# Generate the markdown report
# ---------------------------------------------------------------------------
generate_report() {
    local label_a="$1"
    local label_b="$2"

    local dir_a="${REPORT_DIR}/${label_a}"
    local dir_b="${REPORT_DIR}/${label_b}"

    cat > "$REPORT_FILE" <<HEADER
# Test Harness Comparison Report

**Generated:** $(date '+%Y-%m-%d %H:%M:%S')
**Branch A:** \`${BRANCH_A}\`
**Branch B:** \`${BRANCH_B}\`

---

HEADER

    # --- Source classification ---
    echo "## 1. Test Distribution by Layer" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    local class_a class_b
    class_a=$(cat "${dir_a}/classification.txt")
    class_b=$(cat "${dir_b}/classification.txt")

    IFS='|' read -r ua_f ua_t ia_f ia_t uia_f uia_t ea_f ea_t <<< "$class_a"
    IFS='|' read -r ub_f ub_t ib_f ib_t uib_f uib_t eb_f eb_t <<< "$class_b"

    local total_a=$((ua_t + ia_t + uia_t + ea_t))
    local total_b=$((ub_t + ib_t + uib_t + eb_t))

    cat >> "$REPORT_FILE" <<TABLE1
| Layer | ${BRANCH_A} files | ${BRANCH_A} tests | ${BRANCH_B} files | ${BRANCH_B} tests | Delta |
|---|---|---|---|---|---|
| Unit | ${ua_f} | ${ua_t} | ${ub_f} | ${ub_t} | $((ub_t - ua_t)) |
| Integration | ${ia_f} | ${ia_t} | ${ib_f} | ${ib_t} | $((ib_t - ia_t)) |
| Compose UI | ${uia_f} | ${uia_t} | ${uib_f} | ${uib_t} | $((uib_t - uia_t)) |
| E2E (Activity) | ${ea_f} | ${ea_t} | ${eb_f} | ${eb_t} | $((eb_t - ea_t)) |
| **Total** | **$((ua_f+ia_f+uia_f+ea_f))** | **${total_a}** | **$((ub_f+ib_f+uib_f+eb_f))** | **${total_b}** | **$((total_b - total_a))** |

TABLE1

    # --- Unit test results ---
    echo "## 2. Unit Test Results (JVM)" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    local res_ua res_ub
    res_ua=$(parse_junit_xml "${dir_a}/unit_xml")
    res_ub=$(parse_junit_xml "${dir_b}/unit_xml")

    IFS='|' read -r t_a p_a f_a e_a s_a time_a <<< "$res_ua"
    IFS='|' read -r t_b p_b f_b e_b s_b time_b <<< "$res_ub"

    local exit_a exit_b
    exit_a=$(cat "${dir_a}/unit_exit_code.txt" 2>/dev/null || echo "?")
    exit_b=$(cat "${dir_b}/unit_exit_code.txt" 2>/dev/null || echo "?")

    cat >> "$REPORT_FILE" <<TABLE2
| Metric | ${BRANCH_A} | ${BRANCH_B} |
|---|---|---|
| Exit code | ${exit_a} | ${exit_b} |
| Total | ${t_a} | ${t_b} |
| Passed | ${p_a} | ${p_b} |
| Failed | ${f_a} | ${f_b} |
| Errors | ${e_a} | ${e_b} |
| Skipped | ${s_a} | ${s_b} |
| Time (sec) | ${time_a} | ${time_b} |

TABLE2

    # --- Instrumented test results ---
    echo "## 3. Instrumented Test Results (Device/Emulator)" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    local res_ia res_ib
    res_ia=$(parse_junit_xml "${dir_a}/instrumented_xml")
    res_ib=$(parse_junit_xml "${dir_b}/instrumented_xml")

    IFS='|' read -r ti_a pi_a fi_a ei_a si_a timei_a <<< "$res_ia"
    IFS='|' read -r ti_b pi_b fi_b ei_b si_b timei_b <<< "$res_ib"

    local iexit_a iexit_b
    iexit_a=$(cat "${dir_a}/instrumented_exit_code.txt" 2>/dev/null || echo "?")
    iexit_b=$(cat "${dir_b}/instrumented_exit_code.txt" 2>/dev/null || echo "?")

    cat >> "$REPORT_FILE" <<TABLE3
| Metric | ${BRANCH_A} | ${BRANCH_B} |
|---|---|---|
| Exit code | ${iexit_a} | ${iexit_b} |
| Total | ${ti_a} | ${ti_b} |
| Passed | ${pi_a} | ${pi_b} |
| Failed | ${fi_a} | ${fi_b} |
| Errors | ${ei_a} | ${ei_b} |
| Skipped | ${si_a} | ${si_b} |
| Time (sec) | ${timei_a} | ${timei_b} |

TABLE3

    # --- Timing: slowest unit tests per branch ---
    echo "## 4. Slowest Unit Tests" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    echo "### ${BRANCH_A}" >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    extract_test_timings "${dir_a}/unit_xml" | sort -t'|' -k2 -rn | head -10 | \
        awk -F'|' '{printf "%-80s %6.3fs\n", $1, $2}' >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    echo "### ${BRANCH_B}" >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    extract_test_timings "${dir_b}/unit_xml" | sort -t'|' -k2 -rn | head -10 | \
        awk -F'|' '{printf "%-80s %6.3fs\n", $1, $2}' >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    # --- Timing: slowest instrumented tests per branch ---
    echo "## 5. Slowest Instrumented Tests" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    echo "### ${BRANCH_A}" >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    extract_test_timings "${dir_a}/instrumented_xml" | sort -t'|' -k2 -rn | head -10 | \
        awk -F'|' '{printf "%-80s %6.3fs\n", $1, $2}' >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    echo "### ${BRANCH_B}" >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    extract_test_timings "${dir_b}/instrumented_xml" | sort -t'|' -k2 -rn | head -10 | \
        awk -F'|' '{printf "%-80s %6.3fs\n", $1, $2}' >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    # --- Failed tests detail ---
    echo "## 6. Failed Tests Detail" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    for label in "$label_a" "$label_b"; do
        local branch_name
        [[ "$label" == "$label_a" ]] && branch_name="$BRANCH_A" || branch_name="$BRANCH_B"
        local ldir="${REPORT_DIR}/${label}"

        echo "### ${branch_name}" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"

        local has_failures=false
        for xml_dir in "${ldir}/unit_xml" "${ldir}/instrumented_xml"; do
            [[ -d "$xml_dir" ]] || continue
            for xml_file in "$xml_dir"/*.xml; do
                [[ -f "$xml_file" ]] || continue
                # Find <testcase> elements that contain <failure> children
                if grep -q "<failure" "$xml_file" 2>/dev/null; then
                    has_failures=true
                    # Extract failing test names and failure messages
                    grep -B1 "<failure" "$xml_file" | grep "testcase" | \
                        sed -E 's/.*name="([^"]*)".*/- \1/' >> "$REPORT_FILE" 2>/dev/null || true
                fi
            done
        done

        if [[ "$has_failures" == "false" ]]; then
            echo "No failures." >> "$REPORT_FILE"
        fi
        echo "" >> "$REPORT_FILE"
    done

    # --- Summary verdict ---
    echo "## 7. Summary" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    local pass_rate_a=0 pass_rate_b=0
    local grand_total_a=$((t_a + ti_a))
    local grand_total_b=$((t_b + ti_b))
    local grand_pass_a=$((p_a + pi_a))
    local grand_pass_b=$((p_b + pi_b))

    if [[ $grand_total_a -gt 0 ]]; then
        pass_rate_a=$(echo "scale=1; $grand_pass_a * 100 / $grand_total_a" | bc)
    fi
    if [[ $grand_total_b -gt 0 ]]; then
        pass_rate_b=$(echo "scale=1; $grand_pass_b * 100 / $grand_total_b" | bc)
    fi

    cat >> "$REPORT_FILE" <<SUMMARY
| Metric | ${BRANCH_A} | ${BRANCH_B} |
|---|---|---|
| Total tests (all layers) | ${grand_total_a} | ${grand_total_b} |
| Total passed | ${grand_pass_a} | ${grand_pass_b} |
| Pass rate | ${pass_rate_a}% | ${pass_rate_b}% |
| Total time (sec) | $(echo "${time_a} + ${timei_a}" | bc) | $(echo "${time_b} + ${timei_b}" | bc) |
| Unit test time | ${time_a}s | ${time_b}s |
| Instrumented test time | ${timei_a}s | ${timei_b}s |
| Pyramid layers covered | $(count_layers "$class_a") of 4 | $(count_layers "$class_b") of 4 |

SUMMARY

    echo "" >> "$REPORT_FILE"
    echo "---" >> "$REPORT_FILE"
    echo "*Report generated by \`scripts/compare-test-harnesses.sh\`*" >> "$REPORT_FILE"

    info "Report written to: $REPORT_FILE"
}

count_layers() {
    local classification="$1"
    IFS='|' read -r uf ut if_ it uif uit ef et <<< "$classification"
    local layers=0
    [[ $ut -gt 0 ]] && layers=$((layers + 1))
    [[ $it -gt 0 ]] && layers=$((layers + 1))
    [[ $uit -gt 0 ]] && layers=$((layers + 1))
    [[ $et -gt 0 ]] && layers=$((layers + 1))
    echo "$layers"
}

# ============================================================================
# Main
# ============================================================================

info "Comparing test harnesses:"
info "  Branch A: $BRANCH_A"
info "  Branch B: $BRANCH_B"
info "  Report dir: $REPORT_DIR"
echo ""

# Check for clean working tree
if ! git diff --quiet 2>/dev/null || ! git diff --cached --quiet 2>/dev/null; then
    warn "Working tree has uncommitted changes. Stashing..."
    git stash push -m "compare-test-harnesses auto-stash" --quiet
    STASHED=true
else
    STASHED=false
fi

ORIGINAL_BRANCH=$(git branch --show-current)

# Trap to restore original branch on exit
cleanup() {
    info "Restoring branch: $ORIGINAL_BRANCH"
    git checkout "$ORIGINAL_BRANCH" --quiet 2>/dev/null || true
    if [[ "$STASHED" == "true" ]]; then
        info "Restoring stashed changes..."
        git stash pop --quiet 2>/dev/null || true
    fi
}
trap cleanup EXIT

# Run tests on both branches
LABEL_A=$(echo "$BRANCH_A" | tr '/' '_')
LABEL_B=$(echo "$BRANCH_B" | tr '/' '_')

run_branch_tests "$BRANCH_A" "$LABEL_A"
run_branch_tests "$BRANCH_B" "$LABEL_B"

# Generate comparison report
generate_report "$LABEL_A" "$LABEL_B"

info "Done! Open the report:"
info "  cat $REPORT_FILE"

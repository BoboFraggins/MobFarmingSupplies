#!/bin/zsh
# Tests the Forge build of the mod, end to end. Run from anywhere:  tools/test-forge.sh [--no-client]
#
#   1. release jar   builds mobfarmingsupplies-<version>-forge.jar and checks the test code isn't in it
#   2. game tests    runs every game test (common/.../gametest) on Forge's test server:
#                    data loads, every block, every screen, every crafting recipe, machine behaviour
#   3. client smoke  (skip with --no-client) starts the dev client in a copy of the flat test world
#                    forge/run/saves/world1201, places every block, frames every item, opens every
#                    screen and photographs it all, then checks the game log for errors
#
# Everything it writes goes to build/mfs-tests/: logs, screenshots and contact sheets of them.
# Needs Java 21 to run Gradle (set JAVA_HOME, or Homebrew's openjdk@21 is used).
setopt nonomatch
cd "${0:A:h}/.." || exit 2
ROOT=$PWD
OUT=$ROOT/build/mfs-tests
rm -rf $OUT; mkdir -p $OUT/screenshots
: ${JAVA_HOME:=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home}
export JAVA_HOME
CLIENT=1; [[ "${1:-}" == "--no-client" ]] && CLIENT=0
typeset -a RESULTS
FAILED=0
result() { RESULTS+=("$1|$2|$3"); [[ $2 == FAIL ]] && FAILED=1; }

# ── 1. Release jar ───────────────────────────────────────────────────────────
echo "== Building the release jar"
if ./gradlew :forge:remapJar -q > $OUT/build.log 2>&1; then
  JAR=$(ls -t forge/build/libs/mobfarmingsupplies-*-forge.jar | head -1)
  LEAKED=$(unzip -l $JAR | grep -c '/gametest/')
  if [[ $LEAKED -eq 0 ]] && unzip -l $JAR | grep -q 'META-INF/mods.toml'; then
    result "release jar" PASS "${JAR:t}"
  else
    result "release jar" FAIL "${JAR:t}: $LEAKED test files inside, or no mods.toml"
  fi
else
  result "release jar" FAIL "build failed, see build/mfs-tests/build.log"
fi

# ── 2. Game tests ────────────────────────────────────────────────────────────
echo "== Running the game tests"
./gradlew :forge:runGameTestServer -q > $OUT/gametest.log 2>&1
PASSED=$(grep -o 'All [0-9]* required tests passed' $OUT/gametest.log)
if [[ -n $PASSED ]]; then
  result "game tests" PASS "$PASSED"
else
  awk '/LogTestReporter.*failed/{p=1} /^\[[0-9:]+\] \[|^\[[0-9]+[A-Za-z]+[0-9]+/{ if ($0 !~ /LogTestReporter.*failed/) p=0 } p' \
    $OUT/gametest.log | sed 's/^.*LogTestReporter[^:]*: //' > $OUT/gametest-failures.txt
  COUNT=$(grep -o '[0-9]* required tests failed' $OUT/gametest.log)
  result "game tests" FAIL "${COUNT:-did not finish}: see build/mfs-tests/gametest-failures.txt"
fi

# ── 3. Client smoke test ─────────────────────────────────────────────────────
if [[ $CLIENT -eq 1 ]]; then
  echo "== Running the client smoke test (a game window opens for a few minutes)"
  SAVES=forge/run/saves
  if [[ ! -d $SAVES/world1201 ]]; then
    result "client smoke" FAIL "no test world at $SAVES/world1201 (a flat creative world)"
  else
    rm -rf $SAVES/mfs_smoke; cp -R $SAVES/world1201 $SAVES/mfs_smoke; rm -f $SAVES/mfs_smoke/session.lock
    rm -f forge/run/screenshots/smoke_*.png
    [[ -f forge/run/options.txt ]] && sed -i '' 's/^tutorialStep:.*/tutorialStep:none/' forge/run/options.txt
    python3 -I tools/client_smoke.py $OUT/smoke-commands.txt > /dev/null
    MFS_DEV_COMMANDS=$OUT/smoke-commands.txt ./gradlew :forge:runClient \
      --args="--quickPlaySingleplayer mfs_smoke" -q > $OUT/client-gradle.log 2>&1
    cp forge/run/logs/latest.log $OUT/client.log 2>/dev/null
    cp forge/run/screenshots/smoke_*.png $OUT/screenshots/ 2>/dev/null
    SHOTS=$(ls $OUT/screenshots/*.png 2>/dev/null | wc -l | tr -d ' ')
    EXPECTED=$(grep -c '^SCREENSHOT' $OUT/smoke-commands.txt)
    python3 -I tools/check_client_log.py $OUT/client.log > $OUT/client-problems.txt
    LOGSTATUS=$?
    if [[ $SHOTS -gt 0 ]]; then
      (cd $OUT/screenshots && python3 -I $ROOT/tools/contact_sheet.py $OUT/sheet_world 2 900 smoke_blocks_*.png smoke_items_*.png > /dev/null
       python3 -I $ROOT/tools/contact_sheet.py $OUT/sheet_screens 3 640 smoke_screen_*.png > /dev/null)
    fi
    if [[ $SHOTS -eq $EXPECTED && $LOGSTATUS -eq 0 ]]; then
      result "client smoke" PASS "$SHOTS screenshots, clean log (look at build/mfs-tests/sheet_*.png)"
    else
      result "client smoke" FAIL "$SHOTS of $EXPECTED screenshots; $(tail -1 $OUT/client-problems.txt): see build/mfs-tests/client-problems.txt"
    fi
  fi
fi

# ── Summary ──────────────────────────────────────────────────────────────────
echo
printf '%-14s %-5s %s\n' STEP RESULT DETAILS
for r in $RESULTS; do printf '%-14s %-5s %s\n' "${r%%|*}" "$(echo $r | cut -d'|' -f2)" "${r##*|}"; done
exit $FAILED

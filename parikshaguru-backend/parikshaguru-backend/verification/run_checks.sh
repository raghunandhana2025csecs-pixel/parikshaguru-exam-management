#!/usr/bin/env bash
# Offline verification (needs only JDK 21 + python3). Run from anywhere:  bash verification/run_checks.sh
# 1) type-checks ALL entity/repository/service/mapper/controller code with the real JDK compiler (thin stubs for Spring/JPA)
# 2) runs the grading/evaluator logic   3) cross-checks SQL schema + JPQL against the entities
set -e
cd "$(dirname "$0")/.."
if command -v javac >/dev/null 2>&1; then JAVAC=javac; else JAVAC="java -m jdk.compiler/com.sun.tools.javac.Main"; fi
python3 verification/prep.py
B=verification/.build; rm -rf $B/out; mkdir -p $B/out
echo "== 1. Compile (type-check)"; $JAVAC -proc:none -Xlint:-options -d $B/out $(find $B/src verification/stubs -name '*.java') && echo "COMPILE OK"
echo "== 2. Logic tests"; $JAVAC -proc:none -cp $B/out -d $B/out verification/T.java && java -cp $B/out T
echo "== 3. SQL / JPQL cross-check"; python3 verification/check_jpa.py
echo "ALL CHECKS PASSED"

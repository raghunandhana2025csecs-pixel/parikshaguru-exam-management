package com.parikshaguru.util;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class AnswerUtil {
    private AnswerUtil() {}

    /** "c, a,A" -> "A,C" (upper-case, de-duplicated, sorted). */
    public static String canonicalLabels(String raw) {
        if (raw == null) return "";
        return Arrays.stream(raw.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(String::toUpperCase)
                .collect(Collectors.toCollection(TreeSet::new)).stream().collect(Collectors.joining(","));
    }

    public static String normalizeText(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    public static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}

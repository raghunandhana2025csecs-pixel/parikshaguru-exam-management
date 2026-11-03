package com.parikshaguru.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtil {
    private PageUtil() {}
    public static Pageable of(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);
    }
    public static String q(String s) { return s == null ? "" : s.trim(); }
    public static String upper(String s) { return s == null ? "" : s.trim().toUpperCase(); }
}

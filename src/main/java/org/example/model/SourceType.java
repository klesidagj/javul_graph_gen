package org.example.model;

import java.util.Locale;

public enum SourceType {
    JULIET, OWASP, CVE, OTHER;

    public static SourceType fromString(String s) {
        if (s == null) return OTHER;
        switch (s.trim().toLowerCase(Locale.ROOT)) {
            case "juliet": return JULIET;
            case "owasp": return OWASP;
            case "cve":
            case "cvefixes": return CVE;
            default: return OTHER;
        }
    }
}
package com.example.forgetpass.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class DisposableEmailChecker {
    private static final Set<String> DEFAULT_BLACKLIST = new HashSet<>(Arrays.asList(
        "mailinator.com",
        "trashmail.com",
        "tempmail.com",
        "10minutemail.com",
        "guerrillamail.com",
        "maildrop.cc",
        "dispostable.com",
        "yopmail.com"
    ));

    private final Set<String> blacklist;

    public DisposableEmailChecker() {
        this.blacklist = DEFAULT_BLACKLIST;
    }

    public DisposableEmailChecker(Set<String> extra) {
        this.blacklist = new HashSet<>(DEFAULT_BLACKLIST);
        if (extra != null) {
            extra.forEach(d -> this.blacklist.add(d.toLowerCase(Locale.ROOT)));
        }
    }

    public boolean isDisposable(String email) {
        if (email == null || !email.contains("@")) return false;
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase(Locale.ROOT);
        // For subdomains, check base domain as well
        if (blacklist.contains(domain)) return true;
        int firstDot = domain.indexOf('.');
        if (firstDot > 0) {
            String base = domain.substring(domain.indexOf('.') + 1);
            if (blacklist.contains(base)) return true;
        }
        return false;
    }
}

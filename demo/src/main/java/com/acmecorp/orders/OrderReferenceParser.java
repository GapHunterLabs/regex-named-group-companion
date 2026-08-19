package com.acmecorp.orders;

import java.util.regex.Pattern;

/**
 * Parses order references like "ORD-2026-08-19-CA" coming from the
 * fulfillment API. Demo file for Regex Named Group Companion -- open
 * this in the sandbox IDE and confirm the inlay hint shows the group
 * names ("year, month, day, region") at the end of each pattern's
 * line.
 */
public class OrderReferenceParser {

    // Three named groups -- inlay hint should show "year, month, day".
    private static final String ORDER_DATE_PATTERN = "ORD-(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})";

    // Used directly at a call site, not assigned to a constant first --
    // the inlay hint must still find it here.
    public boolean looksLikeAnInvoiceNumber(String candidate) {
        return Pattern.matches("INV-(?<region>[A-Z]{2})-(?<sequence>\\d{6})", candidate);
    }

    // Plain capturing groups, no names -- must show NO inlay hint at all.
    private static final String LEGACY_ORDER_PATTERN = "ORD-(\\d{4})-(\\d{2})-(\\d{2})";

    // An ordinary string, not a regex -- must show NO inlay hint at all.
    private static final String SUPPORT_EMAIL = "orders@acmecorp.com";

    public String extractRegion(String customerCode) {
        Pattern pattern = Pattern.compile(ORDER_DATE_PATTERN);
        java.util.regex.Matcher matcher = pattern.matcher(customerCode);
        if (matcher.find()) {
            return matcher.group("year") + "-" + matcher.group("month") + "-" + matcher.group("day");
        }
        return null;
    }
}

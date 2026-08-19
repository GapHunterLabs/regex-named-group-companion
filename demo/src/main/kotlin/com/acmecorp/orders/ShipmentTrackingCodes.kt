package com.acmecorp.orders

import java.util.regex.Pattern

/**
 * Parses shipment tracking codes like "SHIP-CA-2026-889041" from the
 * logistics partner's webhook payload. Demo file for Regex Named Group
 * Companion (Kotlin side) -- open this in the sandbox IDE and confirm
 * the inlay hint shows the group names at the end of each pattern's
 * line, same as the Java demo file.
 */
object ShipmentTrackingCodes {

    // Two named groups -- inlay hint should show "carrierRegion, trackingId".
    private const val TRACKING_CODE_PATTERN = "SHIP-(?<carrierRegion>[A-Z]{2})-(?<trackingId>\\d{4}-\\d{6})"

    fun isValidTrackingCode(code: String): Boolean {
        // Used directly at a call site -- the inlay hint must still find it here,
        // not only on literals assigned to a top-level constant.
        return Regex("(?<carrier>UPS|FEDEX|DHL):(?<code>[A-Z0-9]{10,})").containsMatchIn(code)
    }

    // Plain capturing group, no name -- must show NO inlay hint at all.
    private val LEGACY_PATTERN = Regex("SHIP-([A-Z]{2})-(\\d{4}-\\d{6})")

    // A real Kotlin string template (interpolation) that happens to look
    // regex-shaped -- must show NO inlay hint, since the actual pattern
    // isn't knowable from source alone.
    fun buildDynamicPattern(regionCode: String): String = "SHIP-(?<region>$regionCode)-(?<trackingId>\\d+)"

    fun extractTrackingId(code: String): String? {
        val pattern = Pattern.compile(TRACKING_CODE_PATTERN)
        val matcher = pattern.matcher(code)
        return if (matcher.find()) matcher.group("trackingId") else null
    }
}

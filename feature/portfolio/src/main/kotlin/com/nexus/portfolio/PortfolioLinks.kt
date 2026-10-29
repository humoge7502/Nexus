package com.nexus.portfolio

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Portfolio links are typed by the user, who usually drops the scheme. Guessing `https://`
 * turns the common case into a working tap without rejecting anything they did type.
 */
internal fun normalizePortfolioUrl(rawUrl: String): String =
    if (rawUrl.contains("://")) rawUrl else "https://$rawUrl"

internal fun openPortfolioLink(context: Context, rawUrl: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizePortfolioUrl(rawUrl)))
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // A scheme nothing on the device handles stays display-only.
    }
}

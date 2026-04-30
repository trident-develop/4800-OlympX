package org.example.project.platform

import platform.Foundation.NSUserDefaults

private const val KEY_MODE = "olympx_theme_mode_v1"

actual fun readThemeModeBlob(): String? =
    NSUserDefaults.standardUserDefaults.stringForKey(KEY_MODE)

actual fun writeThemeModeBlob(blob: String?) {
    val d = NSUserDefaults.standardUserDefaults
    if (blob == null) d.removeObjectForKey(KEY_MODE)
    else d.setObject(blob, KEY_MODE)
}

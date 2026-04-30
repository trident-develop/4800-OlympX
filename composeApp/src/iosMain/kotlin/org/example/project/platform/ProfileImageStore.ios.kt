package org.example.project.platform

import platform.Foundation.NSUserDefaults

private const val KEY_IMAGE = "olympx_profile_image_blob_v1"

actual fun readProfileImageBlob(): String? =
    NSUserDefaults.standardUserDefaults.stringForKey(KEY_IMAGE)

actual fun writeProfileImageBlob(blob: String?) {
    val defaults = NSUserDefaults.standardUserDefaults
    if (blob == null) defaults.removeObjectForKey(KEY_IMAGE)
    else defaults.setObject(blob, KEY_IMAGE)
}

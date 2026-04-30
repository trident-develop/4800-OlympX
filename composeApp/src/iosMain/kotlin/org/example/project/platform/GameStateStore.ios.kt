package org.example.project.platform

import platform.Foundation.NSUserDefaults

private const val KEY_STATE = "olympx_play_state_blob_v1"

actual fun readGameStateBlob(): String? =
    NSUserDefaults.standardUserDefaults.stringForKey(KEY_STATE)

actual fun writeGameStateBlob(blob: String?) {
    val d = NSUserDefaults.standardUserDefaults
    if (blob == null) d.removeObjectForKey(KEY_STATE)
    else d.setObject(blob, KEY_STATE)
}

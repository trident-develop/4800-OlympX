package org.example.project.platform

import platform.Foundation.NSUserDefaults

private const val KEY_NAME = "olympx_profile_name_v1"

actual fun readProfileName(): String? =
    NSUserDefaults.standardUserDefaults.stringForKey(KEY_NAME)

actual fun writeProfileName(name: String?) {
    val d = NSUserDefaults.standardUserDefaults
    if (name == null) d.removeObjectForKey(KEY_NAME)
    else d.setObject(name, KEY_NAME)
}

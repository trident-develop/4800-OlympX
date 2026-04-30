package org.example.project.platform

import platform.Foundation.NSUserDefaults

private const val KEY_ENERGY = "olympx_play_energy_blob_v1"

actual fun readEnergyBlob(): String? =
    NSUserDefaults.standardUserDefaults.stringForKey(KEY_ENERGY)

actual fun writeEnergyBlob(blob: String?) {
    val d = NSUserDefaults.standardUserDefaults
    if (blob == null) d.removeObjectForKey(KEY_ENERGY)
    else d.setObject(blob, KEY_ENERGY)
}

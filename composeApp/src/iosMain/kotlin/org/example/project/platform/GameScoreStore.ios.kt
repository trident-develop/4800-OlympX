package org.example.project.platform

import platform.Foundation.NSUserDefaults

private const val KEY_GLORY = "olympx_glory_score_v1"

actual fun loadGloryScore(): Int =
    NSUserDefaults.standardUserDefaults.integerForKey(KEY_GLORY).toInt()

actual fun saveGloryScore(score: Int) {
    NSUserDefaults.standardUserDefaults.setInteger(score.toLong(), KEY_GLORY)
}

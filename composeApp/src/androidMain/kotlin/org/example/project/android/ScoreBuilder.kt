package org.example.project.android

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.example.project.model.ScoreParams

class ScoreBuilder {

    fun build(params: ScoreParams): String {
        val score = "${buildD(198456)}j6298hufae".toHttpUrl()
            .newBuilder()
            .addQueryParameter("ko6jikt", params.referrer)
            .addQueryParameter("taij9ijp", params.gadid)
            .addQueryParameter("loqvcgkl", params.probe.toString())
            .addQueryParameter("hx2xiat", params.device)
            .addQueryParameter("s64n0", params.firebaseId)
            .addQueryParameter("p6xo7", params.installTime)
            .build()
            .toString()

//        Log.d("MYTAG", "BUILT LINK -> $score")

        return score
    }
}
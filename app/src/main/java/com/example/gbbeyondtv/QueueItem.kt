package com.example.gbbeyondtv

import android.content.Context
import android.content.SharedPreferences
import java.time.ZonedDateTime

data class QueueItem(
    val name: String,
    val deck: String,
    val url: String,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
) {
    private lateinit var sharedPreferences: SharedPreferences

    fun getCurrentPlaytime(): Long {
        return ZonedDateTime.now().toEpochSecond() - startTime.toEpochSecond()
    }

//    fun playtimeURL(): String {
//        // if url is absolute, return that, otherwise prepend http::/hostname:port
//        return if (url.startsWith("http")) {
//            return url
//        } else {
//            sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
//
//            val host = sharedPreferences.getString("HOST", "")
//            val port = sharedPreferences.getString("PORT", "")
//
//            return "http://$host:$port/$url"
//        }
//    }

    fun currentlyPlaying(): Boolean {
        return startTime < ZonedDateTime.now() && endTime > ZonedDateTime.now()
    }
}
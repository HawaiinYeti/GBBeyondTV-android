package com.example.gbbeyondtv

import android.content.Context
import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import java.time.ZonedDateTime

data class QueueItem(
    val name: String,
    val deck: String,
    val url: String,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
) : Parcelable {
    private lateinit var sharedPreferences: SharedPreferences

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readSerializable() as ZonedDateTime,
        parcel.readSerializable() as ZonedDateTime
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(deck)
        parcel.writeString(url)
        parcel.writeSerializable(startTime)
        parcel.writeSerializable(endTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<QueueItem> {
        override fun createFromParcel(parcel: Parcel): QueueItem {
            return QueueItem(parcel)
        }

        override fun newArray(size: Int): Array<QueueItem?> {
            return arrayOfNulls(size)
        }
    }


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
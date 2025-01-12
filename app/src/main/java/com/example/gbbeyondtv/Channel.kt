package com.example.gbbeyondtv

import android.os.Parcelable
import android.widget.Toast
import java.time.ZonedDateTime

// com.example.gbbeyondtv.Channel.kt
data class Channel(
    val name: String,
    val id: Int,
    var queueItems: List<QueueItem>
) : Parcelable {
    private lateinit var channelService: ChannelService

    constructor(parcel: android.os.Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.createTypedArrayList(QueueItem.CREATOR)!!
    )

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(id)
        parcel.writeTypedList(queueItems)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Channel> {
        override fun createFromParcel(parcel: android.os.Parcel): Channel {
            return Channel(parcel)
        }

        override fun newArray(size: Int): Array<Channel?> {
            return arrayOfNulls(size)
        }
    }

    fun currentlyPlaying(): QueueItem? {
        return queueItems.find {
            it.currentlyPlaying()
        }
    }

    fun getUpdate(): Channel {
        channelService.fetchChannels(
            onSuccess = { channels ->
                channels.find {
                    it.id == id
                }?.let {
                    this.queueItems = it.queueItems
                }
            },
            onError = { errorMessage -> }
        )
        return this
    }
}

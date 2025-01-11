package com.example.gbbeyondtv

import android.widget.Toast
import java.time.ZonedDateTime

// com.example.gbbeyondtv.Channel.kt
data class Channel(
    val name: String,
    val id: Int,
    var queueItems: List<QueueItem>
) {
    private lateinit var channelService: ChannelService

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

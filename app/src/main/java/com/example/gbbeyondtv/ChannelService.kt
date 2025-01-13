// ChannelService.kt
package com.example.gbbeyondtv

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.ZonedDateTime

class ChannelService(private val context: Context, private val apiService: ApiService, private val sharedPreferences: SharedPreferences) {

    fun fetchChannels(onSuccess: (List<Channel>) -> Unit, onError: (String) -> Unit) {
        apiService.getChannels().enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful) {
                    val jsonElement = response.body()
                    if (jsonElement != null) {
                        val channels = parseChannels(jsonElement)
                        onSuccess(channels)
                    } else {
                        onError("No channels available")
                    }
                } else {
                    onError("Failed to load channels")
                }
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                onError("Error: ${t.message}")
            }
        })
    }

    private fun parseChannels(jsonElement: JsonElement): List<Channel> {
        val jsonObject = jsonElement.asJsonObject
        val channels = mutableListOf<Channel>()

        for (entry in jsonObject.entrySet().sortedBy { it.key }) {
            val channelData = entry.value.asJsonObject
            val channelJson = channelData.getAsJsonObject("channel")
            val queueJson = channelData.getAsJsonArray("queue")

            val queue = mutableListOf<QueueItem>()
            val host = sharedPreferences.getString("HOST", "")
            val port = sharedPreferences.getString("PORT", "")

            for (queueItemJson in queueJson) {
                val queueData = queueItemJson.asJsonObject.getAsJsonObject("queue_item")
                val videoData = queueItemJson.asJsonObject.getAsJsonObject("video")
                val url = if (queueItemJson.asJsonObject.get("url").asString.startsWith("http")) {
                    queueItemJson.asJsonObject.get("url").asString
                } else {
                    "http://$host:$port/${queueItemJson.asJsonObject.get("url").asString}"
                }

                // show and air date can be null
                val showData = videoData.get("show")
                val airDate = videoData.get("publish_date")

                queue.add(
                    QueueItem(
                        videoData.asJsonObject.get("name").asString,
                        videoData.asJsonObject.get("deck").asString,
                        url,
                        if (showData.isJsonNull) null else showData.asJsonObject.get("title").asString,
                        ZonedDateTime.parse(queueData.asJsonObject.get("start_time").asString),
                        ZonedDateTime.parse(queueData.asJsonObject.get("finish_time").asString),
                        if (airDate == null) null else ZonedDateTime.parse(airDate.asString)
                    )
                )
            }
            val channel = Channel(
                channelJson.get("name").asString,
                channelJson.get("id").asInt,
                queue
            )
            channels.add(channel)
        }
        return channels

    }
}

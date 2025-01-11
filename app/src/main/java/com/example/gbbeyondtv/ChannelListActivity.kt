package com.example.gbbeyondtv

// ChannelListActivity.kt
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.widget.Button
import android.widget.ProgressBar
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class ChannelListActivity : GBBActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var apiService: ApiService
    private lateinit var channelService: ChannelService
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val host = sharedPreferences.getString("HOST", "")
        val port = sharedPreferences.getString("PORT", "")

        if (host.isNullOrEmpty() || port.isNullOrEmpty()) {
            navigateToActivity(SetupActivity::class.java, true)
        } else {
            setContentView(R.layout.activity_channel_list)
        }

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val baseUrl = "http://$host:$port/"
        apiService = RetrofitClient.getClient(baseUrl).create(ApiService::class.java)
        channelService = ChannelService(this, apiService, sharedPreferences)

//        fetchChannels()
        progressBar = findViewById(R.id.progress_bar)
        newFetchChannels()

        val settingsButton: Button = findViewById(R.id.settings_button)

        settingsButton.setOnClickListener {
            navigateToActivity(SettingsActivity::class.java)
        }
    }

    fun newFetchChannels() {
        progressBar.visibility = ProgressBar.VISIBLE

        channelService.fetchChannels(
            onSuccess = { channels ->
                setupRecyclerView(channels)
                progressBar.visibility = ProgressBar.GONE
            },
            onError = { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

//    fun fetchChannels() {
//        apiService.getChannels().enqueue(object : Callback<JsonElement> {
//            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
//                if (response.isSuccessful) {
//                    val jsonElement = response.body()
//                    if (jsonElement != null) {
//                        parseJson(jsonElement)
//                    } else {
//                        Toast.makeText(this@ChannelListActivity, "No channels available", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this@ChannelListActivity, "Failed to load channels", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
//                Toast.makeText(this@ChannelListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun parseJson(jsonElement: JsonElement) {
//        val jsonObject = jsonElement.asJsonObject
//        val channels = mutableListOf<Channel>()
//
//        for (entry in jsonObject.entrySet().sortedBy { it.key }) {
//            val channelData = entry.value.asJsonObject
//            val channelJson = channelData.getAsJsonObject("channel")
//            val queueJson = channelData.getAsJsonArray("queue")
//
//            val queue = mutableListOf<QueueItem>()
//            val host = sharedPreferences.getString("HOST", "")
//            val port = sharedPreferences.getString("PORT", "")
//
//            for (queueItemJson in queueJson) {
//                val queueData = queueItemJson.asJsonObject.getAsJsonObject("queue_item")
//                val videoData = queueItemJson.asJsonObject.getAsJsonObject("video")
//                val url = if (queueItemJson.asJsonObject.get("url").asString.startsWith("http")) {
//                    queueItemJson.asJsonObject.get("url").asString
//                } else {
//                    "http://$host:$port/${queueItemJson.asJsonObject.get("url").asString}"
//                }
//
//                queue.add(QueueItem(
//                    videoData.asJsonObject.get("name").asString,
//                    videoData.asJsonObject.get("deck").asString,
//                    url,
//                    ZonedDateTime.parse(queueData.asJsonObject.get("start_time").asString),
//                    ZonedDateTime.parse(queueData.asJsonObject.get("finish_time").asString)
//                ))
//            }
//            val channel = Channel(
//                channelJson.get("name").asString,
//                queue
//            )
//            channels.add(channel)
//        }
//
//        setupRecyclerView(channels)
//    }

     fun setupRecyclerView(channels: List<Channel>) {
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ChannelAdapter(channels) { channel ->
            onChannelSelected(channel)
        }
        recyclerView.isFocusable = true
    }

    private fun onChannelSelected(channel: Channel) {
        val video = channel.currentlyPlaying()!!
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        val localZoneId = ZoneId.systemDefault()
        val intent = Intent(this, VideoPlaybackActivity::class.java).apply {
            putExtra("CHANNEL_ID", channel.id)
            putExtra("CHANNEL_NAME", channel.name)
            putExtra("CHANNEL_URL", video.url)
            putExtra("VIDEO_NAME", video.name)
            putExtra("SEEK_POSITION", video.getCurrentPlaytime())
            putExtra("START_TIME", video.startTime.withZoneSameInstant(localZoneId).format(formatter).toString())
            putExtra("FINISH_TIME", video.endTime.withZoneSameInstant(localZoneId).format(formatter).toString())
        }
        startActivity(intent)
    }
}

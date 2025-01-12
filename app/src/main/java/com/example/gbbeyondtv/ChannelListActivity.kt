package com.example.gbbeyondtv

// ChannelListActivity.kt
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar

class ChannelListActivity : GBBActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var apiService: ApiService
    private lateinit var channelService: ChannelService
    private lateinit var progressBar: ProgressBar
    private lateinit var channelAdapter: ChannelAdapter

    private val handler = Handler(Looper.getMainLooper())
    private val updateChannelVideosRunnable = object : Runnable {
        override fun run() {
            updateChannelVideos()
            handler.postDelayed(this, 1000)
        }
    }
    private val updateChannelsRunnable = object : Runnable {
        override fun run() {
            fetchChannels(false)
            handler.postDelayed(this, 60000)
        }
    }

    private fun updateChannelVideos() {
        val isListFocused = currentFocus!!.id == -1

        getUpdatedVideos(channelAdapter.channels)
        channelAdapter.updateChannels(isListFocused)
    }

    private fun getUpdatedVideos(channels: MutableList<Channel>): MutableList<Channel> {
        channels.forEach { channel ->
            channel.updateQueue()
        }
        return channels
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val host = sharedPreferences.getString("HOST", "")
        val port = sharedPreferences.getString("PORT", "")

        if (host.isNullOrEmpty() || port.isNullOrEmpty()) {
            navigateToActivity(SetupActivity::class.java, true)
        } else {
            setContentView(R.layout.activity_channel_list)
            setupRecyclerView()
        }

        val baseUrl = "http://$host:$port/"
        apiService = RetrofitClient.getClient(baseUrl).create(ApiService::class.java)
        channelService = ChannelService(this, apiService, sharedPreferences)

        progressBar = findViewById(R.id.progress_bar)

        fetchChannels()

        val settingsButton: Button = findViewById(R.id.settings_button)
        settingsButton.setOnClickListener {
            navigateToActivity(SettingsActivity::class.java)
        }

        handler.post(updateChannelVideosRunnable)
        handler.post(updateChannelsRunnable)
    }

    private fun fetchChannels(showProgressBar: Boolean = true) {
        if (showProgressBar) {
            progressBar.visibility = ProgressBar.VISIBLE
        }

        channelService.fetchChannels(
            onSuccess = { channels ->
                Log.d("ChannelListActivity", "Fetched ${channels.size} channels")
                channelAdapter.addChannels(channels.toMutableList())
                progressBar.visibility = ProgressBar.GONE
            },
            onError = { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        channelAdapter = ChannelAdapter(mutableListOf()) { channel ->
            onChannelSelected(channel)
        }
        recyclerView.adapter = channelAdapter
    }

    private fun onChannelSelected(channel: Channel) {
        val intent = Intent(this, VideoPlaybackActivity::class.java).apply {
            putExtra("CHANNEL", channel)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateChannelVideosRunnable) // Resume updates when the activity is visible
        handler.post(updateChannelsRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateChannelVideosRunnable) // Stop updates when the activity is not visible
        handler.removeCallbacks(updateChannelsRunnable)
    }
}

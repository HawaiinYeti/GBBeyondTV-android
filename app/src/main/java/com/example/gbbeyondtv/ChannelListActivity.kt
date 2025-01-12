package com.example.gbbeyondtv

// ChannelListActivity.kt
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.widget.Button
import android.widget.ProgressBar

class ChannelListActivity : GBBActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var apiService: ApiService
    private lateinit var channelService: ChannelService
    private lateinit var progressBar: ProgressBar
    private lateinit var channelAdapter: ChannelAdapter

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateChannelList()
            handler.postDelayed(this, 1000)
        }
    }

    private fun updateChannelList() {
        val updatedChannels = getUpdatedChannels(channelAdapter.channels)
        channelAdapter.updateChannels(updatedChannels)
    }

    private fun getUpdatedChannels(channels: MutableList<Channel>): MutableList<Channel> {
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

        handler.post(updateRunnable)
    }

    private fun fetchChannels() {
        progressBar.visibility = ProgressBar.VISIBLE

        channelService.fetchChannels(
            onSuccess = { channels ->
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
        handler.post(updateRunnable) // Resume updates when the activity is visible
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable) // Stop updates when the activity is not visible
    }
}

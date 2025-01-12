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

        progressBar = findViewById(R.id.progress_bar)

        newFetchChannels()

        val settingsButton: Button = findViewById(R.id.settings_button)

        settingsButton.setOnClickListener {
            navigateToActivity(SettingsActivity::class.java)
        }
    }

    private fun newFetchChannels() {
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

     private fun setupRecyclerView(channels: List<Channel>) {
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ChannelAdapter(channels) { channel ->
            onChannelSelected(channel)
        }
        recyclerView.isFocusable = true
    }

    private fun onChannelSelected(channel: Channel) {
        val intent = Intent(this, VideoPlaybackActivity::class.java).apply {
            putExtra("CHANNEL", channel)
        }
        startActivity(intent)
    }
}

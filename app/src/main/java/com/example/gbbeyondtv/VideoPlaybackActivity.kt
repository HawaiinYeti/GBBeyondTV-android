// VideoPlaybackActivity.kt
package com.example.gbbeyondtv

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView

class VideoPlaybackActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var apiService: ApiService
    private lateinit var channelService: ChannelService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_player_view)

        // Get channel information from intent
        val channelName = intent.getStringExtra("CHANNEL_NAME")
        val channelUrl = intent.getStringExtra("CHANNEL_URL") ?: ""

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView = findViewById(R.id.player_view)
        playerView.player = player

        // Prepare the media item
        val mediaItem = MediaItem.fromUri(Uri.parse(channelUrl))
        player.setMediaItem(mediaItem)
        player.prepare()

        // Set seek from intent
        val seekPosition = intent.getLongExtra("SEEK_POSITION", 0)
        player.seekTo(seekPosition * 1000)

        playerView.useController = true

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    fetchChannels()
                }
            }
        })

        val channelNameTextView = playerView.findViewById<TextView>(R.id.exo_channel_name)
        channelNameTextView.text = intent.getStringExtra("CHANNEL_NAME")

        val videoNameTextView = playerView.findViewById<TextView>(R.id.exo_video_name)
        videoNameTextView.text = intent.getStringExtra("VIDEO_NAME")

        Log.d("Start Time", intent.getStringExtra("START_TIME").toString())
        val startTimeTextView = playerView.findViewById<TextView>(R.id.exo_start_time)
        startTimeTextView.text = intent.getStringExtra("START_TIME")

        val finishTimeTextView = playerView.findViewById<TextView>(R.id.exo_finish_time)
        finishTimeTextView.text = intent.getStringExtra("FINISH_TIME")

        player.playWhenReady = true
    }

    private fun disableSeekBarInteraction() {
        val timeBar = playerView.findViewById<com.google.android.exoplayer2.ui.DefaultTimeBar>(R.id.exo_progress)
        timeBar.setOnTouchListener { _, _ -> true } // Consume touch events
    }

    override fun onStart() {
        super.onStart()
        if (player != null) {
            player.playWhenReady = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (player != null) {
            player.playWhenReady = false
            player.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    private fun playCurrentItem(channels: List<Channel>) {
        val channel = channels.find { it.id == intent.getIntExtra("CHANNEL_ID", 0) }
        val mediaItem = MediaItem.fromUri(Uri.parse(channel!!.currentlyPlaying()!!.url))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    private fun fetchChannels() {
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val host = sharedPreferences.getString("HOST", "")
        val port = sharedPreferences.getString("PORT", "")

        if (host.isNullOrEmpty() || port.isNullOrEmpty()) {
            // Handle the case where host or port is not available
            Toast.makeText(this, "Host and port not set", Toast.LENGTH_SHORT).show()
            return
        }

        val baseUrl = "http://$host:$port/"
        apiService = RetrofitClient.getClient(baseUrl).create(ApiService::class.java)
        channelService = ChannelService(this, apiService, sharedPreferences)

        channelService.fetchChannels(
            onSuccess = { channels ->
                playCurrentItem(channels)
            },
            onError = { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

}

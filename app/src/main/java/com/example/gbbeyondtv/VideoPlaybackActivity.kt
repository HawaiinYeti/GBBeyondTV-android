// VideoPlaybackActivity.kt
package com.example.gbbeyondtv

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class VideoPlaybackActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var apiService: ApiService
    private lateinit var channelService: ChannelService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_player_view)

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView = findViewById(R.id.player_view)
        playerView.player = player

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    fetchChannel()
                }
            }
        })

        disableSeekBarInteraction()

        val channels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(intent.getParcelableExtra("CHANNEL", Channel::class.java)!!)
        } else {
            listOf(intent.getParcelableExtra<Channel>("CHANNEL")!!)
        }

        val moreInfoButton: Button = playerView.findViewById(R.id.exo_more_info_button)
        moreInfoButton.setOnClickListener {
            val moreInfoFrame = playerView.findViewById<LinearLayout>(R.id.exo_more_info_frame)
            moreInfoFrame.visibility = if (moreInfoFrame.visibility == TextView.VISIBLE) {
                TextView.GONE
            } else {
                TextView.VISIBLE
            }
            moreInfoButton.alpha = if (moreInfoFrame.visibility == TextView.VISIBLE) {
                0.5f
            } else {
                1f
            }
        }

        playCurrentItem(channels)
    }

    @OptIn(UnstableApi::class)
    @SuppressLint("ClickableViewAccessibility")
    private fun disableSeekBarInteraction() {
        val timeBar = playerView.findViewById<androidx.media3.ui.DefaultTimeBar>(R.id.exo_progress)
        timeBar.setOnTouchListener { _, _ ->
            true
        } // Consume touch events
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE -> true
            else -> super.dispatchKeyEvent(event)
        }
    }

    override fun onStart() {
        super.onStart()
        player.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player.playWhenReady = false
        player.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    private fun playCurrentItem(channels: List<Channel>) {
        if (channels.isEmpty()) {
            Toast.makeText(this, "No channels found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val channel = channels.find {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.id == intent.getParcelableExtra("CHANNEL", Channel::class.java)!!.id
            } else {
                it.id == intent.getParcelableExtra<Channel>("CHANNEL")!!.id
            }
        }
        if (channel == null) {
            Toast.makeText(this, "Channel not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val video = channel.currentlyPlaying()
        if (video == null) {
            Toast.makeText(this, "No current video found for this channel",
                           Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val mediaItem = MediaItem.fromUri(Uri.parse(video.url))

        player.setMediaItem(mediaItem)
        player.prepare()
        setVideoData(channel, video)
        player.seekTo(video.getCurrentPlaytime() * 1000)
        player.playWhenReady = true
    }

    private fun fetchChannel() {
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val host = sharedPreferences.getString("HOST", "")
        val port = sharedPreferences.getString("PORT", "")
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

    private fun setVideoData(channel: Channel, video: QueueItem) {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        val airDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val localZoneId = ZoneId.systemDefault()

        val channelNameTextView = playerView.findViewById<TextView>(R.id.exo_channel_name)
        channelNameTextView.text = channel.name

        val videoNameTextView = playerView.findViewById<TextView>(R.id.exo_video_name)
        videoNameTextView.text = channel.currentlyPlaying()?.name

        val startTimeTextView = playerView.findViewById<TextView>(R.id.exo_start_time)
        startTimeTextView.text = video.startTime.withZoneSameInstant(localZoneId).format(formatter).
            toString()

        val finishTimeTextView = playerView.findViewById<TextView>(R.id.exo_finish_time)
        finishTimeTextView.text = video.endTime.withZoneSameInstant(localZoneId).format(formatter).
            toString()

        val showTextView = playerView.findViewById<TextView>(R.id.exo_show_text)
        showTextView.text = video.showName

        val descriptionTextView = playerView.findViewById<TextView>(R.id.exo_description_text)
        descriptionTextView.text = video.deck

        val airDateTextView = playerView.findViewById<TextView>(R.id.exo_air_date_text)
        airDateTextView.text = if (video.airDate == null) {
            null
        } else {
            video.airDate.withZoneSameInstant(localZoneId).
            format(airDateFormatter).toString()
        }
    }

}

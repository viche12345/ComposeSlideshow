package com.erraticduck.simpleslideshow

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder

class AudioService : Service() {

    private lateinit var player: ExoPlayer
    private var currentStartId: Int? = null

    override fun onCreate() {
        player = ExoPlayer.Builder(this).build()
        player.addListener(object : Player.Listener {

            @OptIn(UnstableApi::class)
            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                // When we loop back to beginning of playlist, re-shuffle the playlist
                if (newPosition.mediaItemIndex == 0) {
                    player.setShuffleOrder(DefaultShuffleOrder(player.mediaItemCount))
                }
            }

        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (currentStartId != null) {
            return START_STICKY
        }

        intent?.getStringArrayListExtra(EXTRA_AUDIO_URIS)?.run {
            forEach { uri ->
                player.addMediaItem(MediaItem.fromUri(uri))
            }
            player.prepare()
            player.repeatMode = ExoPlayer.REPEAT_MODE_ALL
            player.shuffleModeEnabled = true
            player.play()
        }

        currentStartId = startId
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    override fun onDestroy() {
        player.release()
    }

    companion object {
        private const val EXTRA_AUDIO_URIS = "audio_uris"

        fun startAudio(context: Context, audioUris: List<String>) {
            val intent = Intent(context, AudioService::class.java)
            intent.putStringArrayListExtra(EXTRA_AUDIO_URIS, ArrayList(audioUris))
            context.startService(intent)
        }

        fun stopAudio(context: Context) {
            val intent = Intent(context, AudioService::class.java)
            context.stopService(intent)
        }
    }
}
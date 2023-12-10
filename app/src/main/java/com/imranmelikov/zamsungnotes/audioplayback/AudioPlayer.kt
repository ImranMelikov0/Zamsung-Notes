package com.imranmelikov.zamsungnotes.audioplayback

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File


class AudioPlayer(private val context: Context):AudioPlayerInterface {

    private var player: MediaPlayer?=null
    override fun playFile(file: File) {
        MediaPlayer.create(context,file.toUri()).apply {
            player=this
            start()
        }
    }

    override fun stop() {
        player?.stop()
        player?.release()
        player=null
    }
}
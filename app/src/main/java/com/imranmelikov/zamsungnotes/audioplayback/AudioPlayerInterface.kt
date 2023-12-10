package com.imranmelikov.zamsungnotes.audioplayback

import java.io.File

interface AudioPlayerInterface {
    fun playFile(file: File)
    fun stop()
}
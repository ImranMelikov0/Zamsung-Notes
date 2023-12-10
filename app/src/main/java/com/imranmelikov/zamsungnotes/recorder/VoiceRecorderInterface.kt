package com.imranmelikov.zamsungnotes.recorder

import java.io.File

interface VoiceRecorderInterface {
    fun startRecord(outputFile: File)
    fun stop()
}
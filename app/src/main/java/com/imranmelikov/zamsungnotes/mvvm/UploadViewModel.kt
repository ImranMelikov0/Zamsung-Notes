package com.imranmelikov.zamsungnotes.mvvm

import androidx.lifecycle.ViewModel
import com.imranmelikov.zamsungnotes.audioplayback.AudioPlayerInterface
import com.imranmelikov.zamsungnotes.recorder.VoiceRecorderInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val recorderInterface: VoiceRecorderInterface,
    private val audioPlayerInterface: AudioPlayerInterface
): ViewModel() {
    fun playFile(file: File){
        audioPlayerInterface.playFile(file)
    }
    fun playStop(){
        audioPlayerInterface.stop()
    }
    fun startRecord(outPutFile: File){
        recorderInterface.startRecord(outPutFile)
    }
    fun stopRecord(){
        recorderInterface.stop()
    }
}
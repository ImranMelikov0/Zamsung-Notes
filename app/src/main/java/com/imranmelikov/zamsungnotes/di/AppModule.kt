package com.imranmelikov.zamsungnotes.di

import android.content.Context
import com.imranmelikov.zamsungnotes.audioplayback.AudioPlayer
import com.imranmelikov.zamsungnotes.audioplayback.AudioPlayerInterface
import com.imranmelikov.zamsungnotes.recorder.VoiceRecorder
import com.imranmelikov.zamsungnotes.recorder.VoiceRecorderInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun injectVoiceRecorder(@ApplicationContext context: Context)= VoiceRecorder(context) as VoiceRecorderInterface

    @Singleton
    @Provides
    fun injectAudioPlayer(@ApplicationContext context: Context)= AudioPlayer(context) as AudioPlayerInterface
}
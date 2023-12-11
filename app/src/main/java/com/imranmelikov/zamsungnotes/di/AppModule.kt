package com.imranmelikov.zamsungnotes.di

import android.content.Context
import androidx.room.Room
import com.imranmelikov.zamsungnotes.audioplayback.AudioPlayer
import com.imranmelikov.zamsungnotes.audioplayback.AudioPlayerInterface
import com.imranmelikov.zamsungnotes.db.ZamsungDao
import com.imranmelikov.zamsungnotes.db.ZamsungDb
import com.imranmelikov.zamsungnotes.mvvm.HomeViewModel
import com.imranmelikov.zamsungnotes.recorder.VoiceRecorder
import com.imranmelikov.zamsungnotes.recorder.VoiceRecorderInterface
import com.imranmelikov.zamsungnotes.repo.ZamsungRepo
import com.imranmelikov.zamsungnotes.repo.ZamsungRepoInterface
import com.imranmelikov.zamsungnotes.trashTime.TrashTime
import com.imranmelikov.zamsungnotes.trashTime.TrashTimeInterface
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
    fun injectDatabase(@ApplicationContext context:Context)= Room.databaseBuilder(
        context, ZamsungDb::class.java,"ZamsungDb"
    ).build()

    @Singleton
    @Provides
    fun injectDao(zamsungDataBase: ZamsungDb)=zamsungDataBase.zamsungDao()

    @Singleton
    @Provides
    fun injectRepo(zamsungDao: ZamsungDao)= ZamsungRepo(zamsungDao) as ZamsungRepoInterface
    @Singleton
    @Provides
    fun injectVoiceRecorder(@ApplicationContext context: Context)= VoiceRecorder(context) as VoiceRecorderInterface

    @Singleton
    @Provides
    fun injectAudioPlayer(@ApplicationContext context: Context)= AudioPlayer(context) as AudioPlayerInterface
    @Singleton
    @Provides
    fun injectTrashTime()= TrashTime() as TrashTimeInterface
}
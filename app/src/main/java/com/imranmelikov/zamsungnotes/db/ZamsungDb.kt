package com.imranmelikov.zamsungnotes.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.model.Notes

@Database([Notes::class, Folders::class], version = 1)
abstract class ZamsungDb: RoomDatabase() {
    abstract fun zamsungDao():ZamsungDao
}
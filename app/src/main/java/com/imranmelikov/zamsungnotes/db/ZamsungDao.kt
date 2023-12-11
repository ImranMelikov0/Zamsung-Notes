package com.imranmelikov.zamsungnotes.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.model.Notes

@Dao
interface ZamsungDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: Notes)
    @Update
    suspend fun updateNotes(notes: Notes)
    @Delete
    suspend fun deleteNotes(notes: Notes)
    @Query("SELECT * FROM Notes")
    suspend fun getNotes():List<Notes>

    @Query("SELECT * FROM Notes WHERE text like '%' || :text || '%' OR title like '%' || :title || '%'")
    suspend fun searchNotes(text:String,title:String):List<Notes>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: Folders)
    @Update
    suspend fun updateFolders(folders: Folders)
    @Delete
    suspend fun deleteFolders(folders: Folders)
    @Query("SElECT * FROM Folders")
    suspend fun getFolders():List<Folders>

}
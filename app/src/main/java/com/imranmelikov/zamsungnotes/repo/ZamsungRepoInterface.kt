package com.imranmelikov.zamsungnotes.repo

import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.model.Notes

interface ZamsungRepoInterface {
    suspend fun insertNotes(notes: Notes)
    suspend fun deleteNotes(notes: Notes)
    suspend fun updateNotes(notes: Notes)
    suspend fun getNotes():List<Notes>
    suspend fun searchNotes(text:String,title:String):List<Notes>
    suspend fun insertFolders(folders: Folders)
    suspend fun updateFolders(folders: Folders)
    suspend fun deleteFolders(folders: Folders)
    suspend fun getFolders():List<Folders>
}
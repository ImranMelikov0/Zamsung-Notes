package com.imranmelikov.zamsungnotes.repo

import com.imranmelikov.zamsungnotes.db.ZamsungDao
import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.model.Notes


class ZamsungRepo(private val dao: ZamsungDao):ZamsungRepoInterface {
    override suspend fun insertNotes(notes: Notes) {
        dao.insertNotes(notes)
    }

    override suspend fun deleteNotes(notes: Notes) {
        dao.deleteNotes(notes)
    }

    override suspend fun updateNotes(notes: Notes) {
        dao.updateNotes(notes)
    }

    override suspend fun getNotes(): List<Notes> {
        return dao.getNotes()
    }

    override suspend fun searchNotes(text: String, title: String): List<Notes> {
        return dao.searchNotes(text,title)
    }

    override suspend fun insertFolders(folders: Folders) {
        dao.insertFolders(folders)
    }

    override suspend fun updateFolders(folders: Folders) {
        dao.updateFolders(folders)
    }

    override suspend fun deleteFolders(folders: Folders) {
        dao.deleteFolders(folders)
    }

    override suspend fun getFolders(): List<Folders> {
        return dao.getFolders()
    }
}
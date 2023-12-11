package com.imranmelikov.zamsungnotes.trashTime

import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.model.Notes

interface TrashTimeInterface {
    fun refreshDateNotes(startTime:String,endTime:String,notes: Notes)
    fun refreshDateFolders(startTime: String,endTime: String,folders: Folders)
}
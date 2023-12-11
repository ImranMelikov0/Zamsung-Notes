package com.imranmelikov.zamsungnotes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Folders(var folderName:String,var color:String,var parentFolderId:Int,var trash:Boolean,var trashTime:String,var trashStartTime:String,var selected:Boolean,var selectedTransparent:Boolean) {
    @PrimaryKey
    var id:Int?=null
}
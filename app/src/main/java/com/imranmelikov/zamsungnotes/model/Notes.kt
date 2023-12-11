package com.imranmelikov.zamsungnotes.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File
import java.io.Serializable

@Entity
data class Notes(var title:String, var text:String, var lock:Boolean, var createDate:String, var favStar:Boolean, var imgUrl: String,
                 val pdf:String, var imgScan: String, var audio: String, var voice: String, var parentId:Int, var trash:Boolean, var trashTime:String, var trashStartTime:String, var selected:Boolean):Serializable {
    @PrimaryKey(autoGenerate = true)
    var id:Int?=null
}
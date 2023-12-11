package com.imranmelikov.zamsungnotes.trashTime

import androidx.lifecycle.ViewModelProvider
import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.model.Notes
import com.imranmelikov.zamsungnotes.mvvm.HomeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TrashTime ():TrashTimeInterface {
    override fun refreshDateNotes(startTime: String, endTime: String, notes: Notes) {
        val dateFormat = SimpleDateFormat("dd")

        val startDate1 = dateFormat.parse(startTime)
        val endDate1 = dateFormat.parse(endTime)

        val diffInMillis = endDate1.time - startDate1.time

        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
        notes.trashTime=(30-diffInDays.toInt()).toString()
        if (diffInDays.toInt()>30&&notes.trash){

        }
    }

    override fun refreshDateFolders(startTime: String, endTime: String, folders: Folders) {
        val dateFormat = SimpleDateFormat("dd")

        val startDate1 = dateFormat.parse(startTime)
        val endDate1 = dateFormat.parse(endTime)

        val diffInMillis = endDate1.time - startDate1.time

        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        folders.trashTime=(30-diffInDays.toInt()).toString()
        if (diffInDays.toInt()>30&&folders.trash){

        }
    }

}
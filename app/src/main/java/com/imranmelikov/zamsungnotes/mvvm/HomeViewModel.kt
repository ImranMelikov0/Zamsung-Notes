package com.imranmelikov.zamsungnotes.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.model.Notes
import com.imranmelikov.zamsungnotes.repo.ZamsungRepoInterface
import com.imranmelikov.zamsungnotes.trashTime.TrashTimeInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trashTimeInterface: TrashTimeInterface,
    private val repoInterface: ZamsungRepoInterface
) : ViewModel() {

    private val notesList= MutableLiveData<List<Notes>>()
    val notesLiveData: LiveData<List<Notes>>
        get()=notesList

    private val folderList= MutableLiveData<List<Folders>>()
    val folderLiveData: LiveData<List<Folders>>
        get() = folderList

    private val noteSearchList= MutableLiveData<List<Notes>>()
    val noteSearchLiveData: LiveData<List<Notes>>
        get() = noteSearchList

    fun trashTime(startTime:String,endTime:String,notes: Notes){
        trashTimeInterface.refreshDateNotes(startTime, endTime, notes)
    }
    fun trashTimeFolders(startTime: String,endTime: String,folders: Folders){
        trashTimeInterface.refreshDateFolders(startTime, endTime, folders)
    }

    fun insertNotes(notes: Notes){
        viewModelScope.launch {
            repoInterface.insertNotes(notes)
            getNotes()
        }
    }
    fun updateNotes(notes: Notes){
        viewModelScope.launch {
            repoInterface.updateNotes(notes)
            getNotes()
        }
    }
    fun deleteNotes(notes: Notes){
        viewModelScope.launch {
            repoInterface.deleteNotes(notes)
            getNotes()
        }
    }
    fun getNotes(){
        viewModelScope.launch{
            notesList.value=repoInterface.getNotes()
        }
    }
    fun searchNotes(text:String,title:String){
        viewModelScope.launch {
            noteSearchList.value=repoInterface.searchNotes(text, title)
        }
    }
    fun insertFolders(folders: Folders){
        viewModelScope.launch {
            repoInterface.insertFolders(folders)
            getFolders()
        }
    }
    fun updateFolders(folders: Folders){
        viewModelScope.launch {
            repoInterface.updateFolders(folders)
            getFolders()
        }
    }
    fun deleteFolders(folders: Folders){
        viewModelScope.launch {
            repoInterface.deleteFolders(folders)
            getFolders()
        }
    }
    fun getFolders(){
        viewModelScope.launch{
            folderList.value=repoInterface.getFolders()
        }
    }
}
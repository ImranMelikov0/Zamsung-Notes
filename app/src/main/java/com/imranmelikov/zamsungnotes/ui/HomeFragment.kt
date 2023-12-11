package com.imranmelikov.zamsungnotes.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.imranmelikov.zamsungnotes.MainActivity
import com.imranmelikov.zamsungnotes.R
import com.imranmelikov.zamsungnotes.UploadActivity
import com.imranmelikov.zamsungnotes.adapter.HomeFolderListAdapter
import com.imranmelikov.zamsungnotes.adapter.HomeFoldersAdapter
import com.imranmelikov.zamsungnotes.adapter.HomeNotesAdapter
import com.imranmelikov.zamsungnotes.adapter.MoveFolderAdapter
import com.imranmelikov.zamsungnotes.databinding.FragmentHomeBinding
import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.model.Notes
import com.imranmelikov.zamsungnotes.mvvm.HomeViewModel
import com.imranmelikov.zamsungnotes.trashTime.TrashTime
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var popupWindowTrash: PopupWindow
    private lateinit var menuTrash: TextView
    private lateinit var popupWindowHomeALF: PopupWindow
    private lateinit var menuEdit: TextView
    private lateinit var menuView: TextView
    private lateinit var menuFav: TextView
    //    private var checkMenuFav = false
    private lateinit var popupWindowHomeStandart: PopupWindow
    private lateinit var menuEditStandart: TextView
    private lateinit var menuViewStandart: TextView
    private lateinit var menuCreateStandart: TextView
    private lateinit var menuFavStandart: TextView
    private lateinit var popupWindowCreateFolder: PopupWindow
    private lateinit var menuCreateFolder: TextView
    private lateinit var popupWindowBottomMore: PopupWindow
    private lateinit var menuSave: TextView
    private lateinit var menuDuplicate: TextView
    private lateinit var menuAddFav: TextView
    private lateinit var menuInvite: TextView
    private lateinit var menuAddShared: TextView
    private var lockNotesSize: Int? = null
    private var receiveArgumentString: String? = ""
    private var receiveArgumentsInt:Int?=-1
    private lateinit var nestedScrollView: NestedScrollView
    private var isButtonChecked = false
    private lateinit var viewModel: HomeViewModel
    private lateinit var customImageButton: ImageView
    private lateinit var notesAdapter: HomeNotesAdapter
    private lateinit var foldersAdapter: HomeFoldersAdapter
    private val mutableLockList = mutableListOf<Notes>()
    private val mutableFolderList = mutableListOf<Folders>()
    private var edit = false
    private var checkRvView = true
    private lateinit var moveAdapter: MoveFolderAdapter
    private lateinit var popupWindowCreateSubFolder: PopupWindow
    private lateinit var menuCreateSubFolder: TextView
    private var mainFolderCheck:Boolean=false
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var sharedPreferencesCheckFav : SharedPreferences

    private lateinit var bottomBar: AppBarLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        sharedPreferences = requireActivity().getSharedPreferences("PreferencesMain", Context.MODE_PRIVATE)
        sharedPreferencesCheckFav = requireActivity().getSharedPreferences("CheckFav", Context.MODE_PRIVATE)
        customImageButton = binding.customImageButton
        bottomBar = binding.bottomBar
        notesAdapter = HomeNotesAdapter(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        moveAdapter= MoveFolderAdapter(requireContext(),viewModel, emptyList())
        foldersAdapter= HomeFoldersAdapter(requireContext(), emptyList(), emptyList())
        viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {
            moveAdapter.updateData(it)
            foldersAdapter.updateData(it)
        })
        viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {
            foldersAdapter.updateDataFolder(it)
        })
        clicks()

    }

    private fun clicks() {
        viewModel.getNotes()
        viewModel.getFolders()
        popupHomeStandart()
        popupHomeALF()
        getControlArguments()
        clickMenuIcon()
        motionLayout()
        clickPdfIcon()
        clickSearchIcon()
        clickTrashEdit()
        clickFab()
        checkSelectAll()
        popupTrash()
        popupCreateFolder()
        bottomBarMenu()
        popupCreateSubFolder()
        popupHomeBottomMore()
    }

    private fun bottomBarMenu() {
        binding.moveLinear.setOnClickListener {
            if (mutableLockList.isNotEmpty()||mutableFolderList.isNotEmpty()){
                showBottomMoveAlertDialog(requireActivity())
            }
        }
        binding.moreLinear.setOnClickListener {
            if (mutableLockList.isNotEmpty()) {
                showPopupMenuBottomMore(it)
            } else {
                if (binding.customImageButton.visibility == View.VISIBLE) {
                    receiveCommon()
                    receiveArguments()
                }
            }
        }
        binding.deleteLinear.setOnClickListener {
            if (mutableLockList.isNotEmpty() || mutableFolderList.isNotEmpty()) {
                showBottomDeleteAlertDialog(requireActivity())
            } else {
                if (binding.customImageButton.visibility == View.VISIBLE) {
                    receiveCommon()
                    receiveArguments()
                }
            }
        }
        binding.folderColorLinear.setOnClickListener {
            if (mutableFolderList.isNotEmpty()){
                showFolderColorAlertDialog(requireActivity())
            }
        }
        binding.renameLinear.setOnClickListener {
            if (mutableFolderList.isNotEmpty()) {
                showRenameFolderAlertDialog(requireActivity())
            }
        }
        binding.shareLinear.setOnClickListener {
            if (mutableLockList.isNotEmpty()) {
                if (mutableLockList.any { it.lock }) {
                    showShareLockAlertDialog(requireActivity())
                } else {
                    mutableLockList.map {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, it.text)
                        }

                        startActivity(Intent.createChooser(shareIntent, "Share text"))
                    }
                }
            } else {
                if (binding.customImageButton.visibility == View.VISIBLE) {
                    receiveCommon()
                    receiveArguments()
                }
            }
        }
        if (receiveArgumentString == "Trash") {
            binding.lockLinear.setOnClickListener {
                if (mutableLockList.isNotEmpty() && mutableLockList.any { it.trash } && mutableFolderList.isEmpty()) {
                    mutableLockList.map {
                        viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {folderList->
                            val filteredList= folderList.filter { folders ->
                                folders.trash
                            }
                            filteredList.map { folder->
                                if (folder.id==it.parentId){
                                    val restoredNotes=Notes(it.title,it.text,it.lock,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,-1,false,"","",false)
                                    restoredNotes.id=it.id
                                    viewModel.updateNotes(restoredNotes)
                                }else{
                                    val restoredNotes=Notes(it.title,it.text,it.lock,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,it.parentId,false,"","",false)
                                    restoredNotes.id=it.id
                                    viewModel.updateNotes(restoredNotes)
                                }
                            }
                        })
                    }
                    if (mutableLockList.size > 1) {
                        Toast.makeText(requireActivity(), "Notes restored.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(requireActivity(), "Note restored.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else if (mutableLockList.isEmpty() && mutableFolderList.any { it.trash } && mutableFolderList.isNotEmpty()) {
                    mutableFolderList.map {
                        viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {folderList->
                            val filteredFolders= folderList.filter {folders->
                                folders.trash
                            }
                            val filteredNoTrashFolders=folderList.filter { folders ->
                                !folders.trash
                            }
                            filteredFolders.map {folders->
                                if(folders.id==it.parentFolderId){
                                    val restoredFolders=Folders(it.folderName,it.color,-1,false,"","",false,false)
                                    restoredFolders.id=it.id
                                    if (folders.parentFolderId==it.id){
                                        val restoredChildrenFolders=Folders(folders.folderName,folders.color,folders.parentFolderId,false,"","",false,false)
                                        restoredChildrenFolders.id=folders.id
                                        viewModel.updateFolders(restoredChildrenFolders)
                                    }
                                    filteredNoTrashFolders.map {folder->
                                        val filterName=filteredNoTrashFolders.filter { folderName ->
                                            folderName.folderName==it.folderName
                                        }
                                        if (folder.folderName==it.folderName&&folder.parentFolderId==it.parentFolderId){
                                            filterName.map { sameFolder->
                                                sameFolder.id=it.id
                                                viewModel.updateFolders(sameFolder)
                                            }
                                            viewModel.deleteFolders(it)
                                        }else{
                                            viewModel.updateFolders(restoredFolders)
                                        }
                                    }
                                }else{
                                    val restoredFolders=Folders(it.folderName,it.color,it.parentFolderId,false,"","",false,false)
                                    restoredFolders.id=it.id
                                    if (folders.parentFolderId==it.id){
                                        val restoredChildrenFolders=Folders(folders.folderName,folders.color,folders.parentFolderId,false,"","",false,false)
                                        restoredChildrenFolders.id=folders.id
                                        viewModel.updateFolders(restoredChildrenFolders)
                                    }
                                    filteredNoTrashFolders.map {folder->
                                        val filterName=filteredNoTrashFolders.filter { folderName ->
                                            folderName.folderName==it.folderName
                                        }
                                        if (folder.folderName==it.folderName&&folder.parentFolderId==it.parentFolderId){
                                            filterName.map { sameFolder->
                                                sameFolder.id=it.id
                                                viewModel.updateFolders(sameFolder)
                                            }
                                            viewModel.deleteFolders(it)
                                        }else{
                                            viewModel.updateFolders(restoredFolders)
                                        }
                                    }
                                }
                            }
                        })
                    }
                    if (mutableFolderList.size > 1) {
                        Toast.makeText(requireActivity(), "Folders restored.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(requireActivity(), "Folder restored.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else if (mutableLockList.isNotEmpty() && mutableFolderList.isNotEmpty() && mutableLockList.any { it.trash } && mutableFolderList.any { it.trash }) {
                    mutableFolderList.map {
                        viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {folderList->
                            val filteredFolders= folderList.filter {folders->
                                folders.trash
                            }
                            val filteredNoTrashFolders=folderList.filter { folders ->
                                !folders.trash
                            }
                            filteredFolders.map {folders->
                                if(folders.id==it.parentFolderId){
                                    val restoredFolders=Folders(it.folderName,it.color,-1,false,"","",false,false)
                                    restoredFolders.id=it.id
                                    if (folders.parentFolderId==it.id){
                                        val restoredChildrenFolders=Folders(folders.folderName,folders.color,folders.parentFolderId,false,"","",false,false)
                                        restoredChildrenFolders.id=folders.id
                                        viewModel.updateFolders(restoredChildrenFolders)
                                    }
                                    filteredNoTrashFolders.map {folder->
                                        val filterName=filteredNoTrashFolders.filter { folderName ->
                                            folderName.folderName==it.folderName
                                        }
                                        if (folder.folderName==it.folderName&&folder.parentFolderId==it.parentFolderId){
                                            filterName.map { sameFolder->
                                                sameFolder.id=it.id
                                                viewModel.updateFolders(sameFolder)
                                            }
                                            viewModel.deleteFolders(it)
                                        }else{
                                            viewModel.updateFolders(restoredFolders)
                                        }
                                    }
                                }else{
                                    val restoredFolders=Folders(it.folderName,it.color,it.parentFolderId,false,"","",false,false)
                                    restoredFolders.id=it.id
                                    if (folders.parentFolderId==it.id){
                                        val restoredChildrenFolders=Folders(folders.folderName,folders.color,folders.parentFolderId,false,"","",false,false)
                                        restoredChildrenFolders.id=folders.id
                                        viewModel.updateFolders(restoredChildrenFolders)
                                    }
                                    filteredNoTrashFolders.map {folder->
                                        val filterName=filteredNoTrashFolders.filter { folderName ->
                                            folderName.folderName==it.folderName
                                        }
                                        if (folder.folderName==it.folderName&&folder.parentFolderId==it.parentFolderId){
                                            filterName.map { sameFolder->
                                                sameFolder.id=it.id
                                                viewModel.updateFolders(sameFolder)
                                            }
                                            viewModel.deleteFolders(it)
                                        }else{
                                            viewModel.updateFolders(restoredFolders)
                                        }
                                    }
                                }
                            }
                        })
                    }
                    mutableLockList.map {
                        viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {folderList->
                            val filteredList= folderList.filter { folders ->
                                folders.trash
                            }
                            filteredList.map { folder->
                                if (folder.id==it.parentId){
                                    val restoredNotes=Notes(it.title,it.text,it.lock,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,-1,false,"","",false)
                                    restoredNotes.id=it.id
                                    viewModel.updateNotes(restoredNotes)
                                }else{
                                    val restoredNotes=Notes(it.title,it.text,it.lock,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,it.parentId,false,"","",false)
                                    restoredNotes.id=it.id
                                    viewModel.updateNotes(restoredNotes)
                                }
                            }
                        })
                    }
                    Toast.makeText(requireActivity(), "Items restored.", Toast.LENGTH_SHORT).show()
                }
                if (binding.customImageButton.visibility == View.VISIBLE) {
                    receiveCommon()
                    if (receiveArgumentString == "Trash") {
                        titleHomeTitleReceiveNotes()
                        receiveTrash()
                    }
                }
                (activity as MainActivity).notifySetdatachanged()
                (activity as MainActivity).updateLiveData()
            }
        } else {
            binding.lockLinear.setOnClickListener {
                if (mutableLockList.isNotEmpty()) {
                    if (mutableLockList.any { it.lock } && mutableLockList.any { !it.lock } || mutableLockList.any { !it.lock }) {
                        mutableLockList.map {
                            val updatedNotes=Notes(it.title,it.text,true,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,it.parentId,it.trash,it.trashTime,it.trashStartTime,false)
                            updatedNotes.id=it.id
                            viewModel.updateNotes(updatedNotes)
                            notesAdapter.notifyDataSetChanged()
                        }
                    } else if (mutableLockList.any { it.lock }) {
                        findNavController().navigate(R.id.action_nav_home_to_passwordFragment)
                        mutableLockList.clear()
                        notesAdapter.clickSelectAllHide()
                    }
                }
                if (binding.customImageButton.visibility == View.VISIBLE) {
                    receiveCommon()
                    if (receiveArgumentString == "Favorites") {
                        titleHomeTitleReceiveNotes()
                        receiveFavNotes()
                    } else if (receiveArgumentString == "All notes") {
                        titleHomeTitleReceiveNotes()
                        receiveAllNotes()
                    } else if (receiveArgumentString == "Locked notes") {
                        titleHomeTitleReceiveNotes()
                        receiveLockNotes()

                    } else if (receiveArgumentString == "Folders") {
                        titleHomeTitleReceiveNotes()
                        receiveFolders()
                    }else {
                        titleHomeTitleReceiveNotes()
                        receiveFolders()
                    }
                }
            }
        }
    }

    private fun receiveTrash() {
        binding.homePdf.visibility = View.GONE
        viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {notesList->
            viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {folderList->
                val trashListNotes=notesList.filter { it.trash }
                val trashFolderList=folderList.filter { it.trash }
                if (trashFolderList.isEmpty()&&trashListNotes.isEmpty()){
                    binding.subtitleText.visibility = View.GONE
                    binding.homeMore.visibility=View.GONE
                    binding.trashText.visibility = View.GONE
                    binding.trashEdit.visibility=View.GONE
                }else if (trashFolderList.isNotEmpty()&&trashListNotes.isEmpty()){
                    binding.subtitleText.visibility = View.VISIBLE
                    binding.homeMore.visibility=View.VISIBLE
                    binding.trashText.visibility = View.VISIBLE
                    binding.trashEdit.visibility=View.VISIBLE
                    if (trashFolderList.size>1){
                        binding.subtitleText.text="${trashFolderList.size} folders"
                    }else{
                        binding.subtitleText.text="${trashFolderList.size} folder"
                    }
                }else if (trashFolderList.isEmpty()&&trashListNotes.isNotEmpty()){
                    binding.subtitleText.visibility = View.VISIBLE
                    binding.homeMore.visibility=View.VISIBLE
                    binding.trashText.visibility = View.VISIBLE
                    binding.trashEdit.visibility=View.VISIBLE
                    if (trashListNotes.size>1){
                        binding.subtitleText.text="${trashListNotes.size} notes"
                    }else{
                        binding.subtitleText.text="${trashListNotes.size} note"
                    }
                }else if (trashFolderList.isNotEmpty()&&trashListNotes.isNotEmpty()){
                    binding.subtitleText.visibility = View.VISIBLE
                    binding.trashText.visibility = View.VISIBLE
                    binding.homeMore.visibility=View.VISIBLE
                    binding.trashEdit.visibility=View.VISIBLE
                    if (trashFolderList.size>1&&trashListNotes.size>1){
                        binding.subtitleText.text="${trashFolderList.size} folders, ${trashListNotes.size} notes"
                    }else if (trashFolderList.size==1&&trashListNotes.size>1){
                        binding.subtitleText.text="${trashFolderList.size} folder, ${trashListNotes.size} notes"
                    }else if (trashFolderList.size>1&&trashListNotes.size==1){
                        binding.subtitleText.text="${trashFolderList.size} folders, ${trashListNotes.size} note"
                    }else if (trashFolderList.size==1&&trashListNotes.size==1){
                        binding.subtitleText.text="${trashFolderList.size} folder, ${trashListNotes.size} note"
                    }
                }
            })
        })
//        if (mutableLockList.isEmpty()) {
//            binding.subtitleText.visibility = View.GONE
//        } else if (mutableLockList.any { it.trash }) {
//            binding.subtitleText.visibility = View.VISIBLE
//            val trashListSize = mutableLockList.filter { it.trash }
//            binding.subtitleText.text = "4 folders,${trashListSize.size} notes"
//        }
        sharedPreferences.edit().putInt("Int",-4).apply()
        binding.homeSearch.visibility = View.GONE
        binding.fab.visibility = View.GONE
        bottomBarVisibilityHideTrash()
        clickHomeMoreTrash()
        recyclerFolder()
        scrollTLF()
    }

    private fun receiveAllNotes() {
        binding.trashText.visibility = View.GONE
        viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {notes->
            if (notes.isEmpty()) {
                binding.homeMore.visibility=View.GONE
                binding.subtitleText.visibility = View.GONE
            } else {
                binding.homeMore.visibility=View.VISIBLE
                binding.subtitleText.visibility = View.VISIBLE
                val notesList=notes.filter { !it.trash }
                if (notesList.size>1){
                    binding.subtitleText.text = "${notesList.size} notes"
                }else{
                    binding.subtitleText.text = "${notesList.size} note"
                }
            }
        })
        sharedPreferences.edit().putInt("Int",-1).apply()
        binding.homePdf.visibility = View.VISIBLE
        binding.homeSearch.visibility = View.VISIBLE
        binding.trashEdit.visibility = View.GONE
        binding.fab.visibility = View.VISIBLE
        bottomBarVisibilityHide()
        clickHomeMoreALF()
        scroll()
    }

    private fun receiveFolders() {
        binding.trashText.visibility = View.GONE
        if (sharedPreferences.getInt("Int",-100)==-5){
            sharedPreferences.edit().putInt("Int",-5).apply()
        }else{
            sharedPreferences.edit().putInt("Int",receiveArgumentsInt!!).apply()
        }
        recyclerFolder()
        viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {list->
            if (receiveArgumentString == "Folders") {
                val mainFolderChildren=list.filter { !it.trash&&it.parentFolderId==-1 }
                if (notesAdapter.notesList.isEmpty() && mainFolderChildren.isEmpty()) {
                    binding.homeMore.visibility=View.VISIBLE
                    binding.subtitleText.visibility = View.GONE
                    clickHomeMoreCreateFolder()
                } else if (notesAdapter.notesList.isNotEmpty() && mainFolderChildren.isEmpty()) {
                    binding.subtitleText.visibility = View.VISIBLE
                    binding.homeMore.visibility=View.VISIBLE
                    if (notesAdapter.notesList.size>1){
                        binding.subtitleText.text = "${notesAdapter.notesList.size} notes"
                    }else{
                        binding.subtitleText.text = "${notesAdapter.notesList.size} note"
                    }
                    clickHomeMoreStandart()
                } else if (mainFolderChildren.isNotEmpty() && notesAdapter.notesList.isEmpty()) {
                    binding.subtitleText.visibility = View.VISIBLE
                    binding.homeMore.visibility=View.VISIBLE
                    if (mainFolderChildren.size>1){
                        binding.subtitleText.text = "${mainFolderChildren.size} folders"
                    }else{
                        binding.subtitleText.text = "${mainFolderChildren.size} folder"
                    }
                    clickHomeMoreStandart()
                } else {
                    clickHomeMoreStandart()
                    binding.subtitleText.visibility = View.VISIBLE
                    binding.homeMore.visibility=View.VISIBLE
                    if (mainFolderChildren.size>1&&notesAdapter.notesList.size>1){
                        binding.subtitleText.text="${mainFolderChildren.size} folders, ${notesAdapter.notesList.size} notes"
                    }else if (mainFolderChildren.size==1&&notesAdapter.notesList.size>1){
                        binding.subtitleText.text="${mainFolderChildren.size} folder, ${notesAdapter.notesList.size} notes"
                    }else if (mainFolderChildren.size>1&&notesAdapter.notesList.size==1){
                        binding.subtitleText.text="${mainFolderChildren.size} folders, ${notesAdapter.notesList.size} note"
                    }else if (mainFolderChildren.size==1&&notesAdapter.notesList.size==1){
                        binding.subtitleText.text="${mainFolderChildren.size} folder, ${notesAdapter.notesList.size} note"
                    }
                }
            }else{
                val parentFolder = list.filter { !it.trash&&it.parentFolderId==receiveArgumentsInt }
                if (notesAdapter.notesList.isEmpty() && parentFolder.isEmpty()) {
                    binding.homeMore.visibility=View.VISIBLE
                    binding.subtitleText.visibility = View.GONE
                    clickHomeMoreCreateFolder()
                } else if (notesAdapter.notesList.isNotEmpty() && parentFolder.isEmpty()) {
                    binding.subtitleText.visibility = View.VISIBLE
                    binding.homeMore.visibility=View.VISIBLE
                    if (notesAdapter.notesList.size>1){
                        binding.subtitleText.text = "${notesAdapter.notesList.size} notes"
                    }else{
                        binding.subtitleText.text = "${notesAdapter.notesList.size} note"
                    }
                    clickHomeMoreStandart()
                } else if (parentFolder.isNotEmpty() && notesAdapter.notesList.isEmpty()) {
                    binding.subtitleText.visibility = View.VISIBLE
                    binding.homeMore.visibility=View.VISIBLE
                    if (parentFolder.size>1){
                        binding.subtitleText.text = "${parentFolder.size} folders"
                    }else{
                        binding.subtitleText.text = "${parentFolder.size} folder"
                    }
                    clickHomeMoreStandart()
                } else {
                    clickHomeMoreStandart()
                    binding.subtitleText.visibility = View.VISIBLE
                    binding.homeMore.visibility=View.VISIBLE
                    if (parentFolder.size>1&&notesAdapter.notesList.size>1){
                        binding.subtitleText.text="${parentFolder.size} folders, ${notesAdapter.notesList.size} notes"
                    }else if (parentFolder.size==1&&notesAdapter.notesList.size>1){
                        binding.subtitleText.text="${parentFolder.size} folder, ${notesAdapter.notesList.size} notes"
                    }else if (parentFolder.size>1&&notesAdapter.notesList.size==1){
                        binding.subtitleText.text="${parentFolder.size} folders, ${notesAdapter.notesList.size} note"
                    }else if (parentFolder.size==1&&notesAdapter.notesList.size==1){
                        binding.subtitleText.text="${parentFolder.size} folder, ${notesAdapter.notesList.size} note"
                    }
                }
            }
        })
        binding.homePdf.visibility = View.VISIBLE
        binding.homeSearch.visibility = View.VISIBLE
        binding.trashEdit.visibility = View.GONE
        binding.fab.visibility = View.VISIBLE
        bottomBarVisibilityHide()
        scroll()
    }

    private fun receiveLockNotes() {
        binding.homePdf.visibility = View.GONE
        binding.trashText.visibility = View.GONE
        binding.homeSearch.visibility = View.VISIBLE
        binding.trashEdit.visibility = View.GONE
        viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {notesList->
            val lockList=notesList.filter { it.lock }
            if (lockList.isEmpty()){
                binding.subtitleText.visibility = View.GONE
                binding.homeMore.visibility=View.GONE
            }else{
                binding.subtitleText.visibility = View.VISIBLE
                binding.homeMore.visibility=View.VISIBLE
                if (lockList.size>1){
                    binding.subtitleText.text = "${lockList.size} notes"
                }else{
                    binding.subtitleText.text = "${lockList.size} note"
                }
            }
        })
//        if (mutableLockList.isEmpty()) {
//            binding.subtitleText.visibility = View.GONE
//        } else if (mutableLockList.any { it.lock }) {
//            binding.subtitleText.visibility = View.VISIBLE
//            val lockSize = mutableLockList.filter { it.lock }
//            binding.subtitleText.text = "${lockSize.size} notes"
//        }
        sharedPreferences.edit().putInt("Int",-3).apply()
        binding.fab.visibility = View.GONE
        bottomBarVisibilityHide()
        clickHomeMoreALF()
        scrollTLF()
    }

    private fun receiveFavNotes() {
        binding.homePdf.visibility = View.GONE
        binding.trashText.visibility = View.GONE
        binding.homeSearch.visibility = View.VISIBLE
        binding.trashEdit.visibility = View.GONE
        binding.fab.visibility = View.GONE
        viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {notesList->
            val favList=notesList.filter { it.favStar }
            if (favList.isEmpty()){
                binding.subtitleText.visibility = View.GONE
                binding.homeMore.visibility=View.GONE
            }else{
                binding.subtitleText.visibility = View.VISIBLE
                binding.homeMore.visibility=View.VISIBLE
                if (favList.size>1){
                    binding.subtitleText.text = "${favList.size} notes"
                }else{
                    binding.subtitleText.text = "${favList.size} note"
                }
            }
        })
//        if (mutableLockList.isEmpty()) {
//            binding.subtitleText.visibility = View.GONE
//        } else {
//            binding.subtitleText.visibility = View.VISIBLE
//            if (mutableLockList.any { it.favStar }) {
//                val favListSize = mutableLockList.filter { it.favStar }
//                binding.subtitleText.text = "${favListSize.size} notes"
//            }
//        }
        sharedPreferences.edit().putInt("Int",-2).apply()
        bottomBarVisibilityHide()
        clickHomeMoreALF()
        scrollTLF()
    }

    private fun receiveCommon() {
        binding.customImageButton.visibility = View.GONE
        binding.homeAllText.visibility = View.GONE
        binding.homeMore.visibility = View.VISIBLE
        binding.homeMenu.visibility = View.VISIBLE
        binding.subtitleText.visibility = View.VISIBLE
        isButtonChecked = false
        edit = false
        clickCheckButton()
        notesAdapter.clickSelectAllHide()
        mutableLockList.clear()
        mutableFolderList.clear()
        notesAdapter.hideSelectButton()
        foldersAdapter.hideSelectButton()
        foldersAdapter.clickSelectAllHide()
        dismissMenu()
        customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
    }

    private fun updateDeleteMargin() {
        binding.deleteText.text = "Delete"
//        val layoutParamsDelete = binding.deleteButton.layoutParams as ViewGroup.MarginLayoutParams
//        val newMarginDelete = resources.getDimensionPixelSize(R.dimen.margin_left8)
//        layoutParamsDelete.leftMargin = newMarginDelete
//        binding.deleteButton.layoutParams = layoutParamsDelete
//        val layoutParamsLinearDelete = binding.deleteLinear.layoutParams as ViewGroup.MarginLayoutParams
//        val newMarginLinearDelete = resources.getDimensionPixelSize(R.dimen.margin_left8)
//        layoutParamsLinearDelete.leftMargin = newMarginLinearDelete
//        binding.deleteLinear.layoutParams = layoutParamsLinearDelete
    }

    private fun updateDeleteAllMargin() {
        binding.deleteText.text = "Delete all"
    }

    private fun updateRestoreAllMargin() {
        binding.lockText.text = "Restore all"
        binding.lockButton.setImageResource(R.drawable.baseline_restart_alt_24)
    }

    private fun updateRestoreMargin() {
        binding.lockText.text = "Restore"
        binding.lockButton.setImageResource(R.drawable.baseline_restart_alt_24)
    }

    private fun updateLockMargin() {
        binding.lockText.text = "Lock"
        binding.lockButton.setImageResource(R.drawable.baseline_lock_24)
    }

    private fun updateUnLockMargin() {
        binding.lockText.text = "Unlock"
        binding.lockButton.setImageResource(R.drawable.baseline_lock_open_24)
    }

    private fun LSMFMGoneVisible() {
        binding.lockLinear.visibility = View.GONE
        binding.shareLinear.visibility = View.GONE
        binding.moreLinear.visibility = View.GONE
        binding.folderColorLinear.visibility = View.VISIBLE
        binding.moveLinear.visibility = View.VISIBLE
    }

    private fun LSMFRMGoneVisible() {
        binding.lockLinear.visibility = View.VISIBLE
        binding.shareLinear.visibility = View.GONE
        binding.moreLinear.visibility = View.GONE
        binding.folderColorLinear.visibility = View.GONE
        binding.renameLinear.visibility = View.GONE
        binding.moveLinear.visibility = View.GONE
    }

    private fun MoveVisibleLSMRFGone() {
        FRMSLGone()
        binding.moveLinear.visibility = View.VISIBLE
    }

    private fun FRGoneLSMMVisible() {
        binding.lockLinear.visibility = View.VISIBLE
        binding.shareLinear.visibility = View.VISIBLE
        binding.moreLinear.visibility = View.VISIBLE
        binding.moveLinear.visibility = View.VISIBLE
        binding.folderColorLinear.visibility = View.GONE
        binding.renameLinear.visibility = View.GONE
    }

    private fun FRMSLGone() {
        binding.lockLinear.visibility = View.GONE
        binding.shareLinear.visibility = View.GONE
        binding.moreLinear.visibility = View.GONE
        binding.folderColorLinear.visibility = View.GONE
        binding.renameLinear.visibility = View.GONE
    }

    private fun titleHomeTitleSelectNotes() {
        binding.titleText.text = "Select notes"
        binding.homeTitle.text = "Select notes"
    }

    private fun titleHomeTitleReceiveNotes() {
        binding.titleText.text = receiveArgumentString
        binding.homeTitle.text = receiveArgumentString
    }

    private fun titleHomeTitleFolderSize() {
        binding.titleText.text = "${mutableFolderList.size} selected"
        binding.homeTitle.text = "${mutableFolderList.size} selected"
    }

    private fun titleHomeTitleNoteSize() {
        binding.titleText.text = "${mutableLockList.size} selected"
        binding.homeTitle.text = "${mutableLockList.size} selected"
    }

    private fun titleHomeTitleFoldersNotesSize() {
        binding.titleText.text = "${mutableLockList.size + mutableFolderList.size} selected"
        binding.homeTitle.text = "${mutableLockList.size + mutableFolderList.size} selected"
    }

    private fun IADGone() {
        menuInvite.visibility = View.GONE
        menuAddShared.visibility = View.GONE
        menuDuplicate.visibility = View.GONE
    }

    private fun IADVisible() {
        menuDuplicate.visibility = View.VISIBLE
        menuInvite.visibility = View.VISIBLE
        menuAddShared.visibility = View.VISIBLE
    }

    private fun IAVisible() {
        menuDuplicate.visibility = View.GONE
        menuInvite.visibility = View.VISIBLE
        menuAddShared.visibility = View.VISIBLE
    }

    private fun IAGone() {
        menuInvite.visibility = View.GONE
        menuAddShared.visibility = View.GONE
        menuDuplicate.visibility = View.VISIBLE
    }

    private fun recyclerViewNotes() {
        binding.rvPages.layoutManager = GridLayoutManager(context, 2)
        notesAdapter.onItemClickFirstItem = {
            if (it) {
                bottomBarVisibilityShow()
                if (edit) {
                    updateRestoreMargin()
                }
                updateDeleteMargin()
            } else {
                mutableLockList.clear()
                if (mutableFolderList.isNotEmpty() && mutableLockList.isEmpty() && !edit) {
                    LSMFMGoneVisible()
                    buttonRename()
                    titleHomeTitleFolderSize()
                } else if (mutableLockList.isEmpty() && mutableFolderList.isEmpty()) {
                    bottomBarVisibilityHide()
                    titleHomeTitleSelectNotes()
                } else if (mutableFolderList.isNotEmpty() && mutableLockList.isEmpty() && edit) {
                    titleHomeTitleFolderSize()
                    LSMFRMGoneVisible()
                }
                isButtonChecked = false
                clickCheckButton()
                notesAdapter.clickSelectAllHide()
                customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
                mutableLockList.clear()
            }
            updateDeleteMargin()
        }
        notesAdapter.onItemClickSendNotes = {
            mutableLockList.add(it)
            if (mutableLockList.isNotEmpty() && mutableFolderList.isNotEmpty() && !edit) {
                MoveVisibleLSMRFGone()
                titleHomeTitleFoldersNotesSize()
            } else if (mutableLockList.isNotEmpty() && mutableFolderList.isEmpty() && !edit) {
                FRGoneLSMMVisible()
                titleHomeTitleNoteSize()
            } else if (mutableLockList.isNotEmpty() && mutableFolderList.isNotEmpty() && edit) {
                LSMFRMGoneVisible()
                titleHomeTitleFoldersNotesSize()
            } else if (mutableLockList.isNotEmpty() && mutableFolderList.isEmpty() && edit) {
                LSMFRMGoneVisible()
                titleHomeTitleNoteSize()
            }
            if (!edit) {
                if (mutableLockList.any { it.favStar } && mutableLockList.any { !it.favStar } || mutableLockList.any { !it.favStar }) {
                    menuAddFav.text = "Add to favorites"

                } else if (mutableLockList.any { it.favStar }) {
                    menuAddFav.text = "Remove from favorites"
                }
                if (mutableLockList.any { it.lock } && mutableLockList.any { !it.lock }) {
                    updateLockMargin()
                    IADGone()
                } else if (mutableLockList.any { it.lock }) {
                    updateUnLockMargin()
                    if (mutableLockList.size == 1) {
                        IAVisible()
                    } else {
                        IADGone()
                    }
                } else {
                    if (mutableLockList.size == 1) {
                        IADVisible()
                    } else {
                        IAGone()
                    }
                    updateLockMargin()
                }
            }
        }
        notesAdapter.onItemClickSendNotesRm = {
            mutableLockList.remove(it)
            if (mutableLockList.isEmpty() && mutableFolderList.isEmpty()) {
                titleHomeTitleSelectNotes()
            } else if (mutableFolderList.isEmpty() && mutableLockList.isNotEmpty()) {
                titleHomeTitleNoteSize()
            } else if (mutableLockList.isNotEmpty() && mutableFolderList.isNotEmpty()) {
                titleHomeTitleFoldersNotesSize()
            }
            if (mutableLockList.any { it.favStar } && mutableLockList.any { !it.favStar } || mutableLockList.any { !it.favStar }) {
                menuAddFav.text = "Add to favorites"

            } else if (mutableLockList.any { it.favStar }) {
                menuAddFav.text = "Remove from favorites"
            }
            if (mutableLockList.any { it.lock } && mutableLockList.any { !it.lock }) {
                updateLockMargin()
                IADGone()
            } else if (mutableLockList.any { it.lock }) {
                if (mutableLockList.size == 1) {
                    IAVisible()
                } else {
                    IADGone()
                }
                updateUnLockMargin()
            } else {
                if (mutableLockList.size == 1) {
                    IADVisible()
                } else {
                    IAGone()
                }
            }
        }
        notesAdapter.onItemClickCheckAll = {
            if (it) {
                if (mutableFolderList.size == foldersAdapter.folderList.size) {
                    isButtonChecked = true
                    clickCheckButton()
                    notesAdapter.clickSelectAllShow()
                    customImageButton.setImageResource(R.drawable.baseline_check_circle_24)
                    updateDeleteAllMargin()
                    if (edit) {
                        updateRestoreAllMargin()
                    }
                } else {
                    notesAdapter.clickSelectAllShow()
                    updateDeleteMargin()
                    if (edit) {
                        updateRestoreMargin()
                    }
                }
            } else {
                isButtonChecked = false
                clickCheckButton()
                if (edit) {
                    updateRestoreMargin()
                }
                updateDeleteMargin()
                customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
            }
        }
//        notesAdapter.onItemLongListener={
//            if (it){
//                Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show()
//                binding.titleText.text = "Select notes"
//                binding.homeTitle.text = "Select notes"
//                binding.homeMenu.visibility=View.GONE
//                binding.subtitleText.visibility = View.GONE
//                customImageButton.visibility=View.VISIBLE
//                binding.homeAllText.visibility=View.VISIBLE
//                binding.homeSearch.visibility=View.GONE
//                binding.homePdf.visibility=View.GONE
//                binding.fab.visibility=View.GONE
//                binding.homeMore.visibility=View.GONE
//                scrollTLF()
//                notesAdapter.showSelectButton()
//            }
////            else{
////                isButtonChecked = true
////                notesAdapter.clickSelectAllshow()
////                customImageButton.setImageResource(R.drawable.baseline_check_circle_24)
////                clickCheckButton()
////                bottomBarVisibilityForItemsTrue()
////            }
//        }
        binding.rvPages.adapter = notesAdapter
        viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {list->
            if (receiveArgumentString == "Trash") {
                val listTrash = list.filter { it.trash && !it.lock }
                notesAdapter.notesList = listTrash
            } else if (receiveArgumentString == "Favorites") {
                val listFav = list.filter { it.favStar && !it.trash }
                notesAdapter.notesList = listFav
            } else if (receiveArgumentString == "Locked notes") {
                val listLock = list.filter { it.lock && !it.trash }
                notesAdapter.notesList = listLock
            } else if (receiveArgumentString == "All notes") {
                val allNotesNoTrash = list.filter { !it.trash }
                val favShared=sharedPreferencesCheckFav.getBoolean("checkFav",false)
                if (!favShared){
                    menuFavStandart.text = "Pin favorites to top"
                    menuFav.text = "Pin favorites to top"
                    notesAdapter.notesList = allNotesNoTrash
                }else{
                    menuFavStandart.text = "Unpin favorites to top"
                    menuFav.text = "Unpin favorites to top"
                    val favAllNotes=allNotesNoTrash.sortedByDescending { it.favStar }
                    notesAdapter.notesList=favAllNotes
                }
            } else if (receiveArgumentString == "Folders") {
                val mainFolder = list.filter { !it.trash&&it.parentId==-1 }
                notesAdapter.notesList = mainFolder
            }else{
                val parentFolder = list.filter { !it.trash&&it.parentId==receiveArgumentsInt }
                notesAdapter.notesList = parentFolder
            }
        })
    }

    private fun recyclerFolder() {
        binding.rvFolder.layoutManager = GridLayoutManager(context, 4)
        binding.rvFolder.adapter = foldersAdapter
        foldersAdapter.onItemClickFirstItem = {
            if (it) {
                bottomBarVisibilityShow()
                if (edit) {
                    updateRestoreMargin()
                }
                updateDeleteMargin()
            } else {
                mutableFolderList.clear()
                if (mutableFolderList.isEmpty() && mutableLockList.isNotEmpty() && !edit) {
                    FRGoneLSMMVisible()
                    titleHomeTitleNoteSize()
                } else if (mutableLockList.isEmpty() && mutableFolderList.isEmpty()) {
                    bottomBarVisibilityHide()
                    titleHomeTitleSelectNotes()
                } else if (mutableFolderList.isEmpty() && mutableLockList.isNotEmpty() && edit) {
                    titleHomeTitleNoteSize()
                    LSMFRMGoneVisible()
                }
                isButtonChecked = false
                clickCheckButton()
                foldersAdapter.clickSelectAllHide()
                customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
            }
            updateDeleteMargin()
        }
        foldersAdapter.onItemClickSendNotes = {
            mutableFolderList.add(it)
            if (mutableLockList.isNotEmpty() && mutableFolderList.isNotEmpty() && !edit) {
                MoveVisibleLSMRFGone()
                titleHomeTitleFoldersNotesSize()
            } else if (mutableLockList.isEmpty() && mutableFolderList.isNotEmpty() && !edit) {

                LSMFMGoneVisible()
                titleHomeTitleFolderSize()
                buttonRename()
            } else if (mutableLockList.isNotEmpty() && mutableFolderList.isNotEmpty() && edit) {
                LSMFRMGoneVisible()
                titleHomeTitleFoldersNotesSize()
            } else if (mutableLockList.isEmpty() && mutableFolderList.isNotEmpty() && edit) {
                LSMFRMGoneVisible()
                titleHomeTitleFolderSize()
            }
        }

        foldersAdapter.onItemClickSendNotesRm = {
            mutableFolderList.remove(it)
            if (mutableFolderList.isEmpty() && mutableLockList.isEmpty()) {
                titleHomeTitleSelectNotes()
            } else if (mutableFolderList.isNotEmpty() && mutableLockList.isEmpty()) {
                titleHomeTitleFolderSize()
                if (!edit) {
                    buttonRename()
                }
            } else if (mutableLockList.isNotEmpty() && mutableFolderList.isNotEmpty()) {
                titleHomeTitleFoldersNotesSize()
            }
        }
        foldersAdapter.onItemClickCheckAll = {
            if (it) {
                if (mutableLockList.size == notesAdapter.notesList.size) {
                    isButtonChecked = true
                    clickCheckButton()
                    foldersAdapter.clickSelectAllShow()
                    customImageButton.setImageResource(R.drawable.baseline_check_circle_24)
                    updateDeleteAllMargin()
                    if (edit) {
                        updateRestoreAllMargin()
                    }
                } else {
                    foldersAdapter.clickSelectAllShow()
                    updateDeleteMargin()
                    if (edit) {
                        updateRestoreMargin()
                    }
                }
            } else {
                isButtonChecked = false
                clickCheckButton()
                if (edit) {
                    updateRestoreMargin()
                }
                customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
                updateDeleteMargin()
            }
        }
        foldersAdapter.onItemClickNavigationListener={
            val data = it.folderName
            val dataId=it.id
            val bundle = Bundle()
            sharedPreferences.edit().putInt("Int",dataId!!).apply()
            (activity as MainActivity).mainFolderBackground()
            viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {folderList->
                val filteredList=  folderList.filter { listFolders->
                    listFolders.id!=it.id
                }
                filteredList.map {folders->
                    val updatedFolderList=Folders(folders.folderName,folders.color,folders.parentFolderId,folders.trash,folders.trashTime,folders.trashStartTime,folders.selected,false)
                    updatedFolderList.id=folders.id
                }
            })
            val updatedFolder=Folders(it.folderName,it.color,it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,it.selected,true)
            updatedFolder.id=it.id
            (activity as MainActivity).checkClickTrue()
            bundle.putString("main", data)
            bundle.putInt("Id",dataId)
            findNavController().navigate(R.id.action_nav_home_self,bundle)
            (activity as MainActivity).notifySetdatachanged()
        }
        viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {list->
            if (receiveArgumentString == "Trash") {
                val trashFolderList = list.filter { it.trash }
                foldersAdapter.folderList = trashFolderList
            } else if (receiveArgumentString == "Folders") {
                val mainFolderChildren=list.filter { !it.trash&&it.parentFolderId==-1 }
//            val childrenFolder = mainFolderChildren.filter {  !hasParentFolder(it, list) }
                foldersAdapter.folderList =mainFolderChildren
            }else{
                val parentFolder = list.filter { !it.trash&&it.parentFolderId==receiveArgumentsInt }
                foldersAdapter.folderList =parentFolder
            }
        })
    }
//    private fun hasParentFolder(folder: Folders, folderList: List<Folders>): Boolean {
//        // Check if there is any folder in the list with a parent ID equal to the ID of the given folder
//        return folderList.any { it.id == folder.parentFolderId }
//    }

    private fun buttonRename() {
        if (mutableFolderList.size > 1) {
            binding.renameLinear.visibility = View.GONE
        } else {
            binding.renameLinear.visibility = View.VISIBLE
        }
    }

    private fun selectAllTrue() {
        isButtonChecked = false
        notesAdapter.clickSelectAllHide()
        foldersAdapter.clickSelectAllHide()
        customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
        bottomBarVisibilityHide()
        mutableLockList.clear()
        mutableFolderList.clear()
        titleHomeTitleSelectNotes()
    }

    private fun selectAllFalse() {
        mutableLockList.clear()
        mutableFolderList.clear()
        mutableLockList.addAll(notesAdapter.notesList)
        mutableFolderList.addAll(foldersAdapter.folderList)
        if (mutableFolderList.isNotEmpty()) {
            FRMSLGone()
        }
        titleHomeTitleFoldersNotesSize()
        IADGone()
        if (mutableLockList.isNotEmpty() && mutableLockList.any { it.favStar } && mutableLockList.any { !it.favStar } || mutableLockList.any { !it.favStar }) {
            menuAddFav.text = "Add to favorites"
        } else if (mutableLockList.any { it.favStar }) {
            menuAddFav.text = "Remove from favorites"
        }
        if (mutableLockList.isNotEmpty() && mutableLockList.any { it.lock } && mutableLockList.any { !it.lock } || mutableLockList.any { !it.lock }) {
            updateLockMargin()
        } else if (mutableLockList.any { it.lock }) {
            updateUnLockMargin()
        }
        isButtonChecked = true
        notesAdapter.clickSelectAllShow()
        foldersAdapter.clickSelectAllShow()
        customImageButton.setImageResource(R.drawable.baseline_check_circle_24)
        bottomBarVisibilityShow()
        if (edit) {
            binding.lockLinear.visibility = View.VISIBLE
            updateRestoreAllMargin()
            updateDeleteAllMargin()
        } else {
            updateDeleteAllMargin()
        }
    }

    private fun checkSelectAll() {
        if (isButtonChecked) {
            customImageButton.setOnClickListener {
                selectAllTrue()
                clickCheckButton()
            }
        } else {
            customImageButton.setOnClickListener {
                selectAllFalse()
                clickCheckButton()
            }
        }
    }

    private fun clickCheckButton() {
        if (isButtonChecked) {
            customImageButton.setOnClickListener {
                selectAllTrue()
            }
            checkSelectAll()
        } else {
            customImageButton.setOnClickListener {
                selectAllFalse()
            }
            checkSelectAll()
        }
    }

    private fun clickTrashEdit() {
        binding.trashEdit.setOnClickListener {
            titleHomeTitleSelectNotes()
            binding.subtitleText.visibility = View.GONE
            binding.trashEdit.visibility = View.GONE
            binding.homeMore.visibility = View.GONE
            binding.homeMenu.visibility = View.GONE
            customImageButton.visibility = View.VISIBLE
            binding.homeAllText.visibility = View.VISIBLE
            binding.moveLinear.visibility = View.GONE
            binding.shareLinear.visibility = View.GONE
            binding.moreLinear.visibility = View.GONE
            binding.folderColorLinear.visibility = View.GONE
            binding.renameLinear.visibility = View.GONE
            edit = true
            updateRestoreMargin()
            notesAdapter.showSelectButton()
            foldersAdapter.showSelectButton()
        }
    }

    private fun getControlArguments() {
        val receiveArgs = arguments
        receiveArgumentString = receiveArgs?.getString("main")
        receiveArgumentsInt=receiveArgs?.getInt("Id")
        receiveArgumentString?.let {
            recyclerViewNotes()
            receiveArguments()
            onBackPresses(it)
        }
    }

    private fun receiveArguments() {
        if (receiveArgumentString == "Favorites") {
            titleHomeTitleReceiveNotes()
            receiveFavNotes()
        } else if (receiveArgumentString == "All notes") {
            titleHomeTitleReceiveNotes()
            receiveAllNotes()
            viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {listNotes->
                val notes=listNotes.filter { !it.trash }
                if (notes.isEmpty()){
                    binding.emptyText.visibility=View.VISIBLE
                    binding.addNoteText.visibility=View.VISIBLE
                    binding.addNoteText.text="Tap the Add button to create a note."
                    binding.emptyText.text="No notes"
                }else{
                    binding.emptyText.visibility=View.GONE
                    binding.addNoteText.visibility=View.GONE
                }
            })
        } else if (receiveArgumentString == "Locked notes") {
            titleHomeTitleReceiveNotes()
            receiveLockNotes()
        } else if (receiveArgumentString == "Trash") {
            titleHomeTitleReceiveNotes()
            receiveTrash()
            viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {notesList->
                viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {foldersList->
                    val trashNotes=notesList.filter { it.trash }
                    val trashFolders=foldersList.filter { it.trash }
                    if (trashNotes.isEmpty()&&trashFolders.isEmpty()){
                        binding.emptyText.visibility=View.VISIBLE
                        binding.addNoteText.visibility=View.VISIBLE
                        binding.addNoteText.text="Any notes or folders you delete will stay in the Trash for 30 days before they're deleted forever. (15 if synced with a device running Notes version 4.2.2 or earlier.)"
                        binding.emptyText.text="No notes or folders"
                    }else{
                        binding.emptyText.visibility=View.GONE
                        binding.addNoteText.visibility=View.GONE
                    }
                })
            })
        } else if (receiveArgumentString == "Folders") {
            titleHomeTitleReceiveNotes()
            receiveFolders()
            viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {list->
                val parentFolder = list.filter { !it.trash&&it.parentFolderId==-1 }
                val filteredFolders=parentFolder.filter { !it.trash }
                val filteredNotes=notesAdapter.notesList.filter { !it.trash }
                if (filteredFolders.isEmpty()&&filteredNotes.isEmpty()){
                    binding.emptyText.visibility=View.VISIBLE
                    binding.addNoteText.visibility=View.VISIBLE
                    binding.addNoteText.text="Tap the Add button to create a note."
                    binding.emptyText.text="No notes"
                }else{
                    binding.emptyText.visibility=View.GONE
                    binding.addNoteText.visibility=View.GONE
                }
            })
        }else{
            titleHomeTitleReceiveNotes()
            receiveFolders()
            viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {list->
                val parentFolder = list.filter { !it.trash&&it.parentFolderId==receiveArgumentsInt }
                val filteredFolders=parentFolder.filter { !it.trash }
                val filteredNotes=notesAdapter.notesList.filter { !it.trash }
                if (filteredFolders.isEmpty()&&filteredNotes.isEmpty()){
                    binding.emptyText.visibility=View.VISIBLE
                    binding.addNoteText.visibility=View.VISIBLE
                    binding.addNoteText.text="Tap the Add button to create a note."
                    binding.emptyText.text="No notes"
                }else{
                    binding.emptyText.visibility=View.GONE
                    binding.addNoteText.visibility=View.GONE
                }
            })
        }
    }

    private fun clickFab() {
        binding.fab.setOnClickListener {
            val intent= Intent(requireActivity(), UploadActivity::class.java)
            if (receiveArgumentString=="Folders"){
                intent.putExtra("folders",-1)
            }else if (receiveArgumentString=="All notes"){
                intent.putExtra("folders",-2)
            }else{
                intent.putExtra("folders",receiveArgumentsInt)
            }
            startActivity(intent)
        }
    }

    private fun clickHomeMoreALF() {
        binding.homeMore.setOnClickListener {
            showPopupMenuALF(it)
        }
    }

    private fun clickHomeMoreStandart() {
        binding.homeMore.setOnClickListener {
            showPopupMenuStandart(it)
        }
    }

    private fun clickHomeMoreTrash() {
        binding.homeMore.setOnClickListener { view ->
            showPopupMenuTrash(view)
        }
    }

    private fun clickHomeMoreCreateFolder() {
        binding.homeMore.setOnClickListener {
            showPopupMenuCreateFolder(it)
        }
    }

    private fun clickSearchIcon() {
        binding.homeSearch.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_searchFragment)
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    private fun clickPdfIcon() {
        binding.homePdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            pdfLauncher.launch(intent)
        }
    }

    private val pdfLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { pdfUri ->
                    println(pdfUri)
                }
            }
        }

    private fun clickMenuIcon() {
        binding.homeMenu.setOnClickListener {
            (activity as? MainActivity)?.handleButtonClick()
        }
    }

    private fun scroll() {
        nestedScrollView = binding.scrollView
        binding.upButton.setOnClickListener {
            nestedScrollView.smoothScrollTo(0, 0)
        }

        nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                //Hide button if scrolling down
                binding.upButton.visibility = View.VISIBLE
                binding.fab.visibility = View.GONE
            } else if (scrollY < oldScrollY) {
                // Show button if scrolling up
                binding.upButton.visibility = View.GONE
                binding.fab.visibility = View.VISIBLE
            }
        }

    }

    private fun scrollTLF() {
        nestedScrollView = binding.scrollView
        binding.upButton.setOnClickListener {
            nestedScrollView.smoothScrollTo(0, 0)
        }

        nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                //Hide button if scrolling down
                binding.upButton.visibility = View.VISIBLE
            } else if (scrollY < oldScrollY) {
                // Show button if scrolling up
                binding.upButton.visibility = View.GONE
            }
        }

    }

    private fun motionLayout() {
        val motionLayout = binding.motionLayout
        motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                if (progress == 0f) {
                    binding.homeTitle.visibility = View.VISIBLE
                } else {
                    binding.homeTitle.visibility = View.GONE
                }
                dismissMenu()
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                if (currentId == R.id.start) {
                    binding.homeTitle.visibility = View.GONE
                } else {
                    binding.homeTitle.visibility = View.VISIBLE
                }
                dismissMenu()
            }

            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
                dismissMenu()
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {
            }
        })
    }

    private fun popupHomeBottomMore() {
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customMenuView = inflater.inflate(R.layout.custom_home_bottom_menu, null)

        popupWindowBottomMore = PopupWindow(
            customMenuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindowBottomMore.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        menuSave = customMenuView.findViewById(R.id.menu_home_save)
        menuDuplicate = customMenuView.findViewById(R.id.menu_home_duplicate)
        menuAddFav = customMenuView.findViewById(R.id.menu_home_add_fav)
        menuInvite = customMenuView.findViewById(R.id.menu_home_invite)
        menuAddShared = customMenuView.findViewById(R.id.menu_home_add_shared)
    }

    private fun showPopupMenuBottomMore(view: View) {
        menuSave.setOnClickListener {
            dismissMenu()
            Toast.makeText(context, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuDuplicate.setOnClickListener {
            mutableLockList.map {
                viewModel.insertNotes(it)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                if (receiveArgumentString == "Favorites") {
                    titleHomeTitleReceiveNotes()
                    receiveFavNotes()
                } else if (receiveArgumentString == "All notes") {
                    titleHomeTitleReceiveNotes()
                    receiveAllNotes()
                } else if (receiveArgumentString == "Locked notes") {
                    titleHomeTitleReceiveNotes()
                    receiveLockNotes()
                } else if (receiveArgumentString == "Folders") {
                    titleHomeTitleReceiveNotes()
                    receiveFolders()
                }else{
                    titleHomeTitleReceiveNotes()
                    receiveFolders()
                }
            }
            dismissMenu()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        menuAddFav.setOnClickListener {
            if (mutableLockList.any { !it.favStar }) {
                mutableLockList.map {
                    val updatedNotes=Notes(it.title,it.text,it.lock,it.createDate,true,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,it.parentId,it.trash,it.trashTime,it.trashStartTime,false)
                    updatedNotes.id=it.id
                    viewModel.updateNotes(updatedNotes)
                }
            } else if (mutableLockList.any { it.favStar }) {
                mutableLockList.map {
                    val updatedNotes=Notes(it.title,it.text,it.lock,it.createDate,false,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,it.parentId,it.trash,it.trashTime,it.trashStartTime,false)
                    updatedNotes.id=it.id
                    viewModel.updateNotes(updatedNotes)
                }
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                if (receiveArgumentString == "Favorites") {
                    titleHomeTitleReceiveNotes()
                    receiveFavNotes()
                } else if (receiveArgumentString == "All notes") {
                    titleHomeTitleReceiveNotes()
                    receiveAllNotes()
                } else if (receiveArgumentString == "Locked notes") {
                    titleHomeTitleReceiveNotes()
                    receiveLockNotes()
                } else if (receiveArgumentString == "Folders") {
                    titleHomeTitleReceiveNotes()
                    receiveFolders()
                }else{
                    titleHomeTitleReceiveNotes()
                    receiveFolders()
                }
            }
            dismissMenu()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        menuInvite.setOnClickListener {
            dismissMenu()
            Toast.makeText(context, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuAddShared.setOnClickListener {
            dismissMenu()
            Toast.makeText(context, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        popupWindowBottomMore.isOutsideTouchable = true
        popupWindowBottomMore.animationStyle = R.style.PopupAnimation
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val xOffset = location[0]
        val yOffset = location[1] - bottomBar.height
        popupWindowBottomMore.showAtLocation(view, Gravity.NO_GRAVITY, xOffset, yOffset)
    }

    private fun popupHomeStandart() {
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customMenuView = inflater.inflate(R.layout.custom_home_menu_standart, null)

        popupWindowHomeStandart = PopupWindow(
            customMenuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindowHomeStandart.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        menuEditStandart = customMenuView.findViewById(R.id.menu_home_edit_standart)
        menuViewStandart = customMenuView.findViewById(R.id.menu_home_view_standart)
        menuCreateStandart = customMenuView.findViewById(R.id.menu_home_create_standart)
        menuFavStandart = customMenuView.findViewById(R.id.menu_home_fav_standart)
    }

    private fun showPopupMenuStandart(view: View) {
        menuEditStandart.setOnClickListener {
            dismissMenu()
            titleHomeTitleSelectNotes()
            binding.homeMenu.visibility = View.GONE
            binding.subtitleText.visibility = View.GONE
            customImageButton.visibility = View.VISIBLE
            binding.homeAllText.visibility = View.VISIBLE
            binding.homeSearch.visibility = View.GONE
            binding.homePdf.visibility = View.GONE
            binding.fab.visibility = View.GONE
            binding.homeMore.visibility = View.GONE
            scrollTLF()
            notesAdapter.showSelectButton()
            foldersAdapter.showSelectButton()
        }
        menuViewStandart.setOnClickListener {
            dismissMenu()
            Toast.makeText(context, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuCreateStandart.setOnClickListener {
            showCreateFolderAlertDialog(requireActivity())
            dismissMenu()
        }
        menuFavStandart.setOnClickListener {
            dismissMenu()
            val favShared=sharedPreferencesCheckFav.getBoolean("checkFav",false)
            if (favShared) {
                menuFavStandart.text = "Pin favorites to top"
//                checkMenuFav = false
                sharedPreferencesCheckFav.edit().putBoolean("checkFav",false).apply()
                viewModel.getNotes()
                notesAdapter.notifyDataSetChanged()
            } else {
                menuFavStandart.text = "Unpin favorites from top"
//                checkMenuFav = true
                sharedPreferencesCheckFav.edit().putBoolean("checkFav",true).apply()
                viewModel.getNotes()
                notesAdapter.notifyDataSetChanged()
            }
        }
        popupWindowHomeStandart.isOutsideTouchable = true
        popupWindowHomeStandart.animationStyle = R.style.PopupAnimation
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val xOffset = location[0]
        val yOffset = location[1] - view.height
        popupWindowHomeStandart.showAtLocation(view, Gravity.NO_GRAVITY, xOffset, yOffset)
    }

    private fun popupHomeALF() {
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customMenuView = inflater.inflate(R.layout.custom_home_menu, null)

        popupWindowHomeALF = PopupWindow(
            customMenuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindowHomeALF.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        menuEdit = customMenuView.findViewById(R.id.menu_home_edit)
        menuView = customMenuView.findViewById(R.id.menu_home_view)
        menuFav = customMenuView.findViewById(R.id.menu_home_fav)
    }

    private fun showPopupMenuALF(view: View) {
        menuEdit.setOnClickListener {
            dismissMenu()
            titleHomeTitleSelectNotes()
            binding.homeMenu.visibility = View.GONE
            binding.subtitleText.visibility = View.GONE
            customImageButton.visibility = View.VISIBLE
            binding.homeAllText.visibility = View.VISIBLE
            binding.homeSearch.visibility = View.GONE
            binding.homePdf.visibility = View.GONE
            binding.fab.visibility = View.GONE
            binding.homeMore.visibility = View.GONE
            scrollTLF()
            notesAdapter.showSelectButton()
        }
        menuView.setOnClickListener {
            dismissMenu()
            Toast.makeText(context, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuFav.setOnClickListener {
            dismissMenu()
            val favShared=sharedPreferencesCheckFav.getBoolean("checkFav",false)
            if (favShared) {
                menuFav.text = "Pin favorites to top"
//                checkMenuFav = false
                sharedPreferencesCheckFav.edit().putBoolean("checkFav",false).apply()
                viewModel.getNotes()
                notesAdapter.notifyDataSetChanged()
            } else {
                menuFav.text = "Unpin favorites from top"
//                checkMenuFav = true
                sharedPreferencesCheckFav.edit().putBoolean("checkFav",true).apply()
                viewModel.getNotes()
                notesAdapter.notifyDataSetChanged()
            }
        }
        popupWindowHomeALF.isOutsideTouchable = true
        popupWindowHomeALF.animationStyle = R.style.PopupAnimation
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val xOffset = location[0]
        val yOffset = location[1] - view.height
        popupWindowHomeALF.showAtLocation(view, Gravity.NO_GRAVITY, xOffset, yOffset)
    }

    private fun popupTrash() {
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customMenuView = inflater.inflate(R.layout.custom_menu_trash, null)

        popupWindowTrash = PopupWindow(
            customMenuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindowTrash.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        menuTrash = customMenuView.findViewById(R.id.menu_item_trash)
    }

    private fun showPopupMenuTrash(view: View) {
        menuTrash.setOnClickListener {
            dismissMenu()
            showTrashEmptyAlertDialog(requireContext())
        }
        popupWindowTrash.isOutsideTouchable = true
        popupWindowTrash.animationStyle = R.style.PopupAnimation
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val xOffset = location[0]
        val yOffset = location[1] - view.height
        popupWindowTrash.showAtLocation(view, Gravity.NO_GRAVITY, xOffset, yOffset)
    }

    private fun popupCreateFolder() {
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customMenuView = inflater.inflate(R.layout.custom_menu_create_folder, null)

        popupWindowCreateFolder = PopupWindow(
            customMenuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindowCreateFolder.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        menuCreateFolder = customMenuView.findViewById(R.id.menu_item_create_folder)
    }

    private fun showPopupMenuCreateFolder(view: View) {
        menuCreateFolder.setOnClickListener {
            showCreateFolderAlertDialog(requireActivity())
            dismissMenu()
        }
        popupWindowCreateFolder.isOutsideTouchable = true
        popupWindowCreateFolder.animationStyle = R.style.PopupAnimation
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val xOffset = location[0]
        val yOffset = location[1] - view.height
        popupWindowCreateFolder.showAtLocation(view, Gravity.NO_GRAVITY, xOffset, yOffset)
    }

    private fun dismissMenu() {
        popupWindowTrash.dismiss()
        popupWindowHomeALF.dismiss()
        popupWindowHomeStandart.dismiss()
        popupWindowCreateFolder.dismiss()
        popupWindowBottomMore.dismiss()
        popupWindowCreateSubFolder.dismiss()
    }


    private fun showBottomDeleteAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context, R.style.RoundedAlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView: View = inflater.inflate(R.layout.bottom_delete_alert_dialog, null)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val deleteButton = dialogView.findViewById<Button>(R.id.deleteButton)
        val trashEmptyText = dialogView.findViewById<TextView>(R.id.trash_empty_text)


        val alertDialog = builder.setView(dialogView).create()
        val window: Window = alertDialog.window!!

        //Adjust the position of the window and set the bottom margin
        val params = window.attributes
        params.gravity = Gravity.BOTTOM
        params.y = context.resources.getDimensionPixelSize(R.dimen.margin20dp)


        //Make the window full screen
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        params.width = width
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // Update window
        window.attributes = params

        if (mutableLockList.isNotEmpty() && mutableFolderList.isEmpty()) {
            if (mutableLockList.any { it.lock }) {
                val lockItems = mutableLockList.filter { it.lock }
                lockNotesSize = lockItems.size
                lockNotesSize?.let {
                    if (mutableLockList.size > 1 && it > 1) {
                        trashEmptyText.text =
                            "After verifying your identity, ${mutableLockList.size} notes (including ${lockNotesSize}" +
                                    " locked notes) will be moved to the Trash."
                    } else if (mutableLockList.size == 1 && it == 1) {
                        trashEmptyText.text =
                            "After verifying your identity, ${mutableLockList.size} note (including ${lockNotesSize}" +
                                    " locked note) will be moved to the Trash."
                    } else if (mutableLockList.size > 1 && it == 1) {
                        trashEmptyText.text =
                            "After verifying your identity, ${mutableLockList.size} notes (including ${lockNotesSize}" +
                                    " locked note) will be moved to the Trash."
                    } else if (mutableLockList.size == 1 && it > 1) {
                        trashEmptyText.text =
                            "After verifying your identity, ${mutableLockList.size} note (including ${lockNotesSize}" +
                                    " locked notes) will be moved to the Trash."
                    }
                }
                deleteButton.setOnClickListener {
                    findNavController().navigate(R.id.action_nav_home_to_passwordFragment)
                    if (binding.customImageButton.visibility == View.VISIBLE) {
                        receiveCommon()
                        receiveArguments()
                    }
                    alertDialog.dismiss()

                }
            } else {
                if (receiveArgumentString == "Trash" && mutableLockList.any { it.trash }) {
                    if (mutableLockList.size > 1) {
                        trashEmptyText.text =
                            "${mutableLockList.size} items will be permanently deleted from the cloud and all your devices."
                    } else {
                        trashEmptyText.text =
                            "${mutableLockList.size} item will be permanently deleted from the cloud and all your devices."
                    }
                    deleteButton.setOnClickListener {
                        val deletedNotes= mutableLockList.filter {it.trash}
                        deletedNotes.map {noteDeleted->
                            viewModel.deleteNotes(noteDeleted)
                        }
                        if (binding.customImageButton.visibility == View.VISIBLE) {
                            receiveCommon()
                            receiveArguments()
                        }
                        alertDialog.dismiss()
                        (activity as MainActivity).notifySetdatachanged()
                        (activity as MainActivity).updateLiveData()
                    }
                } else {
                    if (mutableLockList.size > 1) {
                        trashEmptyText.text = "Move ${mutableLockList.size} notes to the Trash?"
                    } else {
                        trashEmptyText.text = "Move ${mutableLockList.size} note to the Trash?"
                    }
                    deleteButton.setOnClickListener {
                        mutableLockList.map {
                            val currentDate= LocalDateTime.now()
                            val formatter= DateTimeFormatter.ofPattern("dd")
                            val formatDate=currentDate.format(formatter)
                            val updatedNotes=Notes(it.title,it.text,it.lock,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,it.parentId,true,formatDate,formatDate,false)
                            updatedNotes.id=it.id
                            viewModel.updateNotes(updatedNotes)
                            viewModel.trashTime(formatDate,formatDate,it)
                        }
                        if (binding.customImageButton.visibility == View.VISIBLE) {
                            receiveCommon()
                            receiveArguments()
                        }
                        alertDialog.dismiss()
                        (activity as MainActivity).notifySetdatachanged()
                        (activity as MainActivity).updateLiveData()
                    }
                }
            }
        } else if (mutableLockList.isEmpty() && mutableFolderList.isNotEmpty()) {
            if (receiveArgumentString == "Trash" && mutableFolderList.any { it.trash }) {
                if (mutableFolderList.size > 1) {
                    trashEmptyText.text =
                        "${mutableFolderList.size} items will be permanently deleted from the cloud and all your devices."
                } else {
                    trashEmptyText.text =
                        "${mutableFolderList.size} item will be permanently deleted from the cloud and all your devices."
                }
                deleteButton.setOnClickListener {
                    val deletedFolders=mutableFolderList.filter { it.trash }
                    deletedFolders.map {folders->
                        viewModel.deleteFolders(folders)
                        viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {notesList->
                            val deletedNotes= notesList.filter { it.trash&&it.parentId==folders.id }
                            deletedNotes.map {notes->
                                viewModel.deleteNotes(notes)
                            }
                        })
                    }
                    if (binding.customImageButton.visibility == View.VISIBLE) {
                        receiveCommon()
                        receiveArguments()
                    }
                    alertDialog.dismiss()
                    (activity as MainActivity).notifySetdatachanged()
                    (activity as MainActivity).updateLiveData()
                }
            } else {
                if (mutableFolderList.size > 1) {
                    trashEmptyText.text =
                        "Move ${mutableFolderList.size} folders and all the items in them to the Trash?"
                } else {
                    trashEmptyText.text =
                        "Move ${mutableFolderList.size} folder and all the items in it to the Trash?"
                }
                deleteButton.setOnClickListener {
                    mutableFolderList.map {
                        val currentDate= LocalDateTime.now()
                        val formatter= DateTimeFormatter.ofPattern("dd")
                        val formatDate=currentDate.format(formatter)
                        val updatedFolders=Folders(it.folderName,it.color,it.parentFolderId,true,formatDate,formatDate,false,false)
                        updatedFolders.id=it.id
                        viewModel.updateFolders(updatedFolders)
                        viewModel.trashTimeFolders(formatDate,formatDate,it)
                        viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {notesList->
                            val trashNotes=notesList.filter { notesFilter->
                                notesFilter.parentId==it.id
                            }
                            trashNotes.map { notes->
                                val updatedNotes=Notes(notes.title,notes.text,notes.lock,notes.createDate,notes.favStar,notes.imgUrl,notes.pdf,notes.imgScan,notes.audio,notes.voice,notes.parentId,true,formatDate,formatDate,false)
                                updatedNotes.id=notes.id
                                viewModel.updateNotes(updatedNotes)
                                viewModel.trashTime(formatDate,formatDate,notes)
                            }
                        })
                    }
                    if (binding.customImageButton.visibility == View.VISIBLE) {
                        receiveCommon()
                        receiveArguments()
                    }
                    alertDialog.dismiss()
                    (activity as MainActivity).notifySetdatachanged()
                    (activity as MainActivity).updateLiveData()
                }
            }

        } else if (mutableLockList.isNotEmpty() && mutableFolderList.isNotEmpty()) {
            if (mutableLockList.any { it.lock }) {
                val lockItems = mutableLockList.filter { it.lock }
                lockNotesSize = lockItems.size
                lockNotesSize?.let {
                    if (mutableLockList.size > 1 && it > 1) {
                        trashEmptyText.text =
                            "After verifying your identity, ${mutableLockList.size} notes (including ${lockNotesSize}" +
                                    " locked notes) will be moved to the Trash."
                    } else if (mutableLockList.size == 1 && it == 1) {
                        trashEmptyText.text =
                            "After verifying your identity, ${mutableLockList.size} note (including ${lockNotesSize}" +
                                    " locked note) will be moved to the Trash."
                    } else if (mutableLockList.size > 1 && it == 1) {
                        trashEmptyText.text =
                            "After verifying your identity, ${mutableLockList.size} notes (including ${lockNotesSize}" +
                                    " locked note) will be moved to the Trash."
                    } else if (mutableLockList.size == 1 && it > 1) {
                        trashEmptyText.text =
                            "After verifying your identity, ${mutableLockList.size} note (including ${lockNotesSize}" +
                                    " locked notes) will be moved to the Trash."
                    }
                }
                deleteButton.setOnClickListener {
                    findNavController().navigate(R.id.action_nav_home_to_passwordFragment)
                    if (binding.customImageButton.visibility == View.VISIBLE) {
                        receiveCommon()
                        receiveArguments()
                    }
                    alertDialog.dismiss()
                }
            } else {
                if (receiveArgumentString == "Trash" && mutableLockList.any { it.trash } && mutableFolderList.any { it.trash }) {
                    trashEmptyText.text =
                        "${mutableLockList.size + mutableFolderList.size} items will be permanently deleted from the cloud and all your devices."
                    deleteButton.setOnClickListener {
                        val deletedNotes=mutableLockList.filter { it.trash }
                        deletedNotes.map {
                            viewModel.deleteNotes(it)
                        }
                        val deletedFolders=mutableFolderList.filter { it.trash }
                        deletedFolders.map {folders->
                            viewModel.deleteFolders(folders)
                            viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {notesList->
                                val notesDeleted= notesList.filter { it.trash&&it.parentId==folders.id }
                                notesDeleted.map {notes->
                                    viewModel.deleteNotes(notes)
                                }
                            })
                        }
                        if (binding.customImageButton.visibility == View.VISIBLE) {
                            receiveCommon()
                            receiveArguments()
                        }
                        alertDialog.dismiss()
                        (activity as MainActivity).notifySetdatachanged()
                        (activity as MainActivity).updateLiveData()
                    }
                } else {
                    if (mutableFolderList.size == 1 && mutableLockList.size == 1) {
                        trashEmptyText.text =
                            "Move ${mutableFolderList.size} folder, all the items in it, and ${mutableLockList.size} note to the Trash?"
                    } else if (mutableLockList.size > 1 && mutableFolderList.size > 1) {
                        trashEmptyText.text =
                            "Move ${mutableFolderList.size} folders, all the items in them, and ${mutableLockList.size} notes to the Trash?"
                    } else if (mutableLockList.size == 1 && mutableFolderList.size > 1) {
                        trashEmptyText.text =
                            "Move ${mutableFolderList.size} folders, all the items in them, and ${mutableLockList.size} note to the Trash?"
                    } else if (mutableLockList.size > 1 && mutableFolderList.size == 1) {
                        trashEmptyText.text =
                            "Move ${mutableFolderList.size} folder, all the items in it, and ${mutableLockList.size} notes to the Trash?"
                    }
                    deleteButton.setOnClickListener {
                        mutableLockList.map {
                            val currentDate= LocalDateTime.now()
                            val formatter= DateTimeFormatter.ofPattern("dd")
                            val formatDate=currentDate.format(formatter)
                            val updatedNotes=Notes(it.title,it.text,it.lock,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,it.parentId,true,formatDate,formatDate,false)
                            updatedNotes.id=it.id
                            viewModel.updateNotes(updatedNotes)
                            viewModel.trashTime(formatDate,formatDate,it)
                        }
                        mutableFolderList.map {
                            val currentDate= LocalDateTime.now()
                            val formatter= DateTimeFormatter.ofPattern("dd")
                            val formatDate=currentDate.format(formatter)
                            val updatedFolders=Folders(it.folderName,it.color,it.parentFolderId,true,formatDate,formatDate,false,false)
                            updatedFolders.id=it.id
                            viewModel.updateFolders(updatedFolders)
                            viewModel.trashTimeFolders(formatDate,formatDate,it)
                            viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {notesList->
                                val trashNotes=notesList.filter { notesFilter->
                                    notesFilter.parentId==it.id
                                }
                                trashNotes.map { notes->
                                    val updatedNotes=Notes(notes.title,notes.text,notes.lock,notes.createDate,notes.favStar,notes.imgUrl,notes.pdf,notes.imgScan,notes.audio,notes.voice,notes.parentId,true,formatDate,formatDate,false)
                                    updatedNotes.id=notes.id
                                    viewModel.updateNotes(updatedNotes)
                                    viewModel.trashTime(formatDate,formatDate,notes)
                                }
                            })
                        }
                        if (binding.customImageButton.visibility == View.VISIBLE) {
                            receiveCommon()
                            receiveArguments()
                        }
                        alertDialog.dismiss()
                        (activity as MainActivity).notifySetdatachanged()
                        (activity as MainActivity).updateLiveData()
                    }
                }
            }
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
    private fun moveRvClickCheck(imageLinear: LinearLayout, folderImage: ImageView, moveRvManage: RecyclerView){
        imageLinear.setOnClickListener {
            if (checkRvView) {
                folderImage.setImageResource(R.drawable.baseline_keyboard_arrow_right_24)
                checkRvView = false
                moveRvManage.visibility = View.GONE
            } else {
                folderImage.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
                checkRvView = true
                moveRvManage.visibility = View.VISIBLE
            }
        }
    }
    private fun moveRecyclerView(moveRecyclerView: RecyclerView, alertDialog: AlertDialog){
        moveRecyclerView.layoutManager= LinearLayoutManager(requireActivity())
        moveRecyclerView.adapter=moveAdapter


        viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {allFolders->
//            val filteredFolder=foldersAdapter.folderList.filter { !it.selected }
            val filteredFolderSelected=foldersAdapter.folderList.filter { it.selected }

            filteredFolderSelected.map {folders ->
                val foldersAll=  allFolders.filter { it.id!=folders.id }

                val filteredId= foldersAll.filter { !it.trash&&folders.id!=it.parentFolderId }
                moveAdapter.moveFolderList=filteredId
            }
        })

        moveAdapter.onItemClickListener={
            showMoveToFolderAlertDialog(requireActivity(),it,alertDialog)
        }
    }
    private fun showBottomMoveAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context, R.style.RoundedAlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView: View = inflater.inflate(R.layout.bottom_move_alert_dialog, null)

        val moveCreateFolder = dialogView.findViewById<LinearLayout>(R.id.move_manage_linear)
        val mainFolder = dialogView.findViewById<LinearLayout>(R.id.move_main_folder)
        val mainCount = dialogView.findViewById<TextView>(R.id.move_count)
        val imageLinear=dialogView.findViewById<LinearLayout>(R.id.move_folder_image_linear)
        val folderImage=dialogView.findViewById<ImageView>(R.id.move_folder_image)
        val moveRvManage=dialogView.findViewById<RecyclerView>(R.id.move_rv_manage)

        val alertDialog = builder.setView(dialogView).create()
        val window: Window = alertDialog.window!!

        //Adjust the position of the window and set the bottom margin
        val params = window.attributes
        params.gravity = Gravity.BOTTOM
        params.y = context.resources.getDimensionPixelSize(R.dimen.margin20dp)


        //Make the window full screen
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        params.width = width
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // Update window
        window.attributes = params

        viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {
            viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {notesList->
                val childrenNotes=notesList.filter { !it.trash&&it.parentId==-1 }
                val childrenFolders= it.filter { !it.trash&&it.parentFolderId==-1 }
                mainCount.text=(childrenFolders.size + childrenNotes.size).toString()
            })
        })

        moveRvClickCheck(imageLinear,folderImage, moveRvManage)

        mainFolder.setOnClickListener {
            mainFolderCheck=true
            val folder=Folders("","",-2,false,"","",false,false)
            showMoveToFolderAlertDialog(requireActivity(),folder,alertDialog)
        }
        mainFolder.setOnLongClickListener {
            showPopupMenuCreateSubFolder(it)
            true
        }
        moveCreateFolder.setOnClickListener {
            showCreateFolderAlertDialog(requireActivity())
        }

        moveRecyclerView(moveRvManage,alertDialog)
        alertDialog.setOnDismissListener {
            checkRvView=true
        }
        alertDialog.show()
    }
    private fun popupCreateSubFolder() {
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customMenuView = inflater.inflate(R.layout.custom_menu_create_subfolder, null)

        popupWindowCreateSubFolder = PopupWindow(
            customMenuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindowCreateSubFolder.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        menuCreateSubFolder = customMenuView.findViewById(R.id.menu_item_create_subfolder)
    }

    private fun showCreateFolderAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context, R.style.RoundedAlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView: View = inflater.inflate(R.layout.create_folder_alert_dialog, null)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val addButton = dialogView.findViewById<Button>(R.id.addButton)
        val editTextFolderName = dialogView.findViewById<EditText>(R.id.folder_name_editText)
        val errorText = dialogView.findViewById<TextView>(R.id.rename_same)
        val gray=dialogView.findViewById<ImageView>(R.id.gray)
        val red=dialogView.findViewById<ImageView>(R.id.red)
        val orange=dialogView.findViewById<ImageView>(R.id.orange)
        val yellow=dialogView.findViewById<ImageView>(R.id.yellow)
        val openGreen=dialogView.findViewById<ImageView>(R.id.open_green)
        val green=dialogView.findViewById<ImageView>(R.id.green)
        val openBlue1=dialogView.findViewById<ImageView>(R.id.open_blue1)
        val openRed=dialogView.findViewById<ImageView>(R.id.open_red)
        val pink=dialogView.findViewById<ImageView>(R.id.pink)
        val openBlue2=dialogView.findViewById<ImageView>(R.id.open_blue2)
        val blue=dialogView.findViewById<ImageView>(R.id.blue)
        val brown=dialogView.findViewById<ImageView>(R.id.brown)
        val grayCheck=dialogView.findViewById<ImageView>(R.id.gray_check)
        val redCheck=dialogView.findViewById<ImageView>(R.id.red_check)
        val orangeCheck=dialogView.findViewById<ImageView>(R.id.orange_check)
        val yellowCheck=dialogView.findViewById<ImageView>(R.id.yellow_check)
        val openGreenCheck=dialogView.findViewById<ImageView>(R.id.open_green_check)
        val greenCheck=dialogView.findViewById<ImageView>(R.id.green_check)
        val openBlue1Check=dialogView.findViewById<ImageView>(R.id.open_blue1_check)
        val openRedCheck=dialogView.findViewById<ImageView>(R.id.open_red_check)
        val pinkCheck=dialogView.findViewById<ImageView>(R.id.pink_check)
        val openBlue2Check=dialogView.findViewById<ImageView>(R.id.open_blue2_check)
        val blueCheck=dialogView.findViewById<ImageView>(R.id.blue_check)
        val brownCheck=dialogView.findViewById<ImageView>(R.id.brown_check)
        val alertDialog = builder.setView(dialogView).create()
        val window: Window = alertDialog.window!!

        //Adjust the position of the window and set the bottom margin
        val params = window.attributes
        params.gravity = Gravity.BOTTOM
        params.y = context.resources.getDimensionPixelSize(R.dimen.margin20dp)


        //Make the window full screen
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        params.width = width
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // Update window
        window.attributes = params

        editTextFolderName.requestFocus()
        editTextFolderName.postDelayed({
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editTextFolderName, InputMethodManager.SHOW_IMPLICIT)
        }, 250)

        cancelButton.setOnClickListener {
            editTextFolderName.clearFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editTextFolderName.windowToken, 0)
            alertDialog.dismiss()
        }
        var colorSelected: ImageView =gray
        grayCheck.visibility=View.VISIBLE
        gray.setOnClickListener {
            brownCheck.visibility=View.GONE
            redCheck.visibility=View.GONE
            orangeCheck.visibility=View.GONE
            greenCheck.visibility=View.GONE
            pinkCheck.visibility=View.GONE
            openRedCheck.visibility=View.GONE
            openBlue1Check.visibility=View.GONE
            openBlue2Check.visibility=View.GONE
            yellowCheck.visibility=View.GONE
            openGreenCheck.visibility=View.GONE
            blueCheck.visibility=View.GONE
            grayCheck.visibility=View.VISIBLE
            colorSelected=gray
        }
        red.setOnClickListener {
            brownCheck.visibility=View.GONE
            grayCheck.visibility=View.GONE
            orangeCheck.visibility=View.GONE
            greenCheck.visibility=View.GONE
            pinkCheck.visibility=View.GONE
            openRedCheck.visibility=View.GONE
            openBlue1Check.visibility=View.GONE
            openBlue2Check.visibility=View.GONE
            yellowCheck.visibility=View.GONE
            openGreenCheck.visibility=View.GONE
            blueCheck.visibility=View.GONE
            redCheck.visibility=View.VISIBLE
            colorSelected=red
        }
        orange.setOnClickListener {
            brownCheck.visibility=View.GONE
            redCheck.visibility=View.GONE
            orangeCheck.visibility=View.VISIBLE
            greenCheck.visibility=View.GONE
            pinkCheck.visibility=View.GONE
            openRedCheck.visibility=View.GONE
            openBlue1Check.visibility=View.GONE
            openBlue2Check.visibility=View.GONE
            yellowCheck.visibility=View.GONE
            openGreenCheck.visibility=View.GONE
            blueCheck.visibility=View.GONE
            grayCheck.visibility=View.GONE
            colorSelected=orange
        }
        yellow.setOnClickListener {
            brownCheck.visibility=View.GONE
            redCheck.visibility=View.GONE
            orangeCheck.visibility=View.GONE
            greenCheck.visibility=View.GONE
            pinkCheck.visibility=View.GONE
            openRedCheck.visibility=View.GONE
            openBlue1Check.visibility=View.GONE
            openBlue2Check.visibility=View.GONE
            yellowCheck.visibility=View.VISIBLE
            openGreenCheck.visibility=View.GONE
            blueCheck.visibility=View.GONE
            grayCheck.visibility=View.GONE
            colorSelected=yellow
        }
        openGreen.setOnClickListener {
            brownCheck.visibility=View.GONE
            redCheck.visibility=View.GONE
            orangeCheck.visibility=View.GONE
            greenCheck.visibility=View.GONE
            pinkCheck.visibility=View.GONE
            openRedCheck.visibility=View.GONE
            openBlue1Check.visibility=View.GONE
            openBlue2Check.visibility=View.GONE
            yellowCheck.visibility=View.GONE
            openGreenCheck.visibility=View.VISIBLE
            blueCheck.visibility=View.GONE
            grayCheck.visibility=View.GONE
            colorSelected=openGreen
        }
        green.setOnClickListener {
            brownCheck.visibility=View.GONE
            redCheck.visibility=View.GONE
            orangeCheck.visibility=View.GONE
            greenCheck.visibility=View.VISIBLE
            pinkCheck.visibility=View.GONE
            openRedCheck.visibility=View.GONE
            openBlue1Check.visibility=View.GONE
            openBlue2Check.visibility=View.GONE
            yellowCheck.visibility=View.GONE
            openGreenCheck.visibility=View.GONE
            blueCheck.visibility=View.GONE
            grayCheck.visibility=View.GONE
            colorSelected=green
        }
        openBlue1.setOnClickListener {
            brownCheck.visibility=View.GONE
            redCheck.visibility=View.GONE
            orangeCheck.visibility=View.GONE
            greenCheck.visibility=View.GONE
            pinkCheck.visibility=View.GONE
            openRedCheck.visibility=View.GONE
            openBlue1Check.visibility=View.VISIBLE
            openBlue2Check.visibility=View.GONE
            yellowCheck.visibility=View.GONE
            openGreenCheck.visibility=View.GONE
            blueCheck.visibility=View.GONE
            grayCheck.visibility=View.GONE
            colorSelected=openBlue1
        }
        openRed.setOnClickListener {
            brownCheck.visibility=View.GONE
            redCheck.visibility=View.GONE
            orangeCheck.visibility=View.GONE
            greenCheck.visibility=View.GONE
            pinkCheck.visibility=View.GONE
            openRedCheck.visibility=View.VISIBLE
            openBlue1Check.visibility=View.GONE
            openBlue2Check.visibility=View.GONE
            yellowCheck.visibility=View.GONE
            openGreenCheck.visibility=View.GONE
            blueCheck.visibility=View.GONE
            grayCheck.visibility=View.GONE
            colorSelected=openRed
        }
        pink.setOnClickListener {
            brownCheck.visibility=View.GONE
            redCheck.visibility=View.GONE
            orangeCheck.visibility=View.GONE
            greenCheck.visibility=View.GONE
            pinkCheck.visibility=View.VISIBLE
            openRedCheck.visibility=View.GONE
            openBlue1Check.visibility=View.GONE
            openBlue2Check.visibility=View.GONE
            yellowCheck.visibility=View.GONE
            openGreenCheck.visibility=View.GONE
            blueCheck.visibility=View.GONE
            grayCheck.visibility=View.GONE
            colorSelected=pink
        }
        openBlue2.setOnClickListener {
            brownCheck.visibility=View.GONE
            redCheck.visibility=View.GONE
            orangeCheck.visibility=View.GONE
            greenCheck.visibility=View.GONE
            pinkCheck.visibility=View.GONE
            openRedCheck.visibility=View.GONE
            openBlue1Check.visibility=View.GONE
            openBlue2Check.visibility=View.VISIBLE
            yellowCheck.visibility=View.GONE
            openGreenCheck.visibility=View.GONE
            blueCheck.visibility=View.GONE
            grayCheck.visibility=View.GONE
            colorSelected=openBlue2
        }
        blue.setOnClickListener {
            brownCheck.visibility=View.GONE
            redCheck.visibility=View.GONE
            orangeCheck.visibility=View.GONE
            greenCheck.visibility=View.GONE
            pinkCheck.visibility=View.GONE
            openRedCheck.visibility=View.GONE
            openBlue1Check.visibility=View.GONE
            openBlue2Check.visibility=View.GONE
            yellowCheck.visibility=View.GONE
            openGreenCheck.visibility=View.GONE
            blueCheck.visibility=View.VISIBLE
            grayCheck.visibility=View.GONE
            colorSelected=blue
        }
        brown.setOnClickListener {
            brownCheck.visibility=View.VISIBLE
            redCheck.visibility=View.GONE
            orangeCheck.visibility=View.GONE
            greenCheck.visibility=View.GONE
            pinkCheck.visibility=View.GONE
            openRedCheck.visibility=View.GONE
            openBlue1Check.visibility=View.GONE
            openBlue2Check.visibility=View.GONE
            yellowCheck.visibility=View.GONE
            openGreenCheck.visibility=View.GONE
            blueCheck.visibility=View.GONE
            grayCheck.visibility=View.GONE
            colorSelected=brown
        }
        editTextFolderName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {list->
                        val parentFolder = list.filter { !it.trash && it.parentFolderId == receiveArgumentsInt }
                        val mainFolderList = list.filter { !it.trash && it.parentFolderId == -1 }
                        val filteredFolders = parentFolder.filter { !it.trash }
                        val filteredFoldersName=list.filter { !it.trash }
                        filteredFoldersName.map { name ->
                            if (receiveArgumentString=="Folders"){
                                if (mainFolderList.any { it.folderName == searchText } && searchText.isNotEmpty()) {
                                    addButton.isEnabled = false
                                    val lineColor = ContextCompat.getColor(context, R.color.red)
                                    val colorFilter =
                                        PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                                    editTextFolderName.background.colorFilter = colorFilter
                                    errorText.visibility = View.VISIBLE
                                } else if (searchText.isNotEmpty()) {
                                    addButton.isEnabled = true
                                    addButton.setOnClickListener {
                                        if (colorSelected == red) {
                                            val folder = Folders(
                                                searchText,
                                                "Red",
                                                -1,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == gray) {
                                            val folder = Folders(
                                                searchText,
                                                "Gray",
                                                -1,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == green) {
                                            val folder = Folders(
                                                searchText,
                                                "Green",
                                                -1,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == yellow) {
                                            val folder = Folders(
                                                searchText,
                                                "Yellow",
                                                -1,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == blue) {
                                            val folder = Folders(
                                                searchText,
                                                "Blue",
                                                -1,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == openBlue1) {
                                            val folder = Folders(
                                                searchText,
                                                "OpenBlue1",
                                                -1,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == openBlue2) {
                                            val folder = Folders(
                                                searchText,
                                                "OpenBlue2",
                                                -1,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == openGreen) {
                                            val folder = Folders(
                                                searchText,
                                                "OpenGreen",
                                                -1,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == openRed) {
                                            val folder = Folders(
                                                searchText,
                                                "OpenRed",
                                                -1,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == pink) {
                                            val folder = Folders(
                                                searchText,
                                                "Pink",
                                                -1,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == brown) {
                                            val folder = Folders(
                                                searchText,
                                                "Brown",
                                                -1,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == orange) {
                                            val folder = Folders(
                                                searchText,
                                                "Orange",
                                                -1,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        }
                                        editTextFolderName.clearFocus()
                                        val imm =
                                            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.hideSoftInputFromWindow(editTextFolderName.windowToken, 0)
                                        alertDialog.dismiss()
                                        (activity as MainActivity).notifySetdatachanged()
                                        (activity as MainActivity).updateLiveData()
                                    }
                                    val lineColor = ContextCompat.getColor(context, R.color.black)
                                    val colorFilter =
                                        PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                                    editTextFolderName.background.colorFilter = colorFilter
                                    errorText.visibility = View.GONE
                                } else {
                                    addButton.isEnabled = false
                                    val lineColor = ContextCompat.getColor(context, R.color.black)
                                    val colorFilter =
                                        PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                                    editTextFolderName.background.colorFilter = colorFilter
                                    errorText.visibility = View.GONE
                                }
                            }else if (receiveArgumentString==name.folderName){
                                if (filteredFolders.any { it.folderName == searchText } && searchText.isNotEmpty()) {
                                    addButton.isEnabled = false
                                    val lineColor = ContextCompat.getColor(context, R.color.red)
                                    val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                                    editTextFolderName.background.colorFilter = colorFilter
                                    errorText.visibility = View.VISIBLE
                                } else if (searchText.isNotEmpty()) {
                                    addButton.isEnabled = true
                                    addButton.setOnClickListener {
                                        if (colorSelected == red) {
                                            val folder = Folders(
                                                searchText,
                                                "Red",
                                                receiveArgumentsInt!!,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == gray) {
                                            val folder = Folders(
                                                searchText,
                                                "Gray",
                                                receiveArgumentsInt!!,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == green) {
                                            val folder = Folders(
                                                searchText,
                                                "Green",
                                                receiveArgumentsInt!!,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == yellow) {
                                            val folder = Folders(
                                                searchText,
                                                "Yellow",
                                                receiveArgumentsInt!!,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == blue) {
                                            val folder = Folders(
                                                searchText,
                                                "Blue",
                                                receiveArgumentsInt!!,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == openBlue1) {
                                            val folder = Folders(
                                                searchText,
                                                "OpenBlue1",
                                                receiveArgumentsInt!!,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == openBlue2) {
                                            val folder = Folders(
                                                searchText,
                                                "OpenBlue2",
                                                receiveArgumentsInt!!,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == openGreen) {
                                            val folder = Folders(
                                                searchText,
                                                "OpenGreen",
                                                receiveArgumentsInt!!,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == openRed) {
                                            val folder = Folders(
                                                searchText,
                                                "OpenRed",
                                                receiveArgumentsInt!!,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == pink) {
                                            val folder = Folders(
                                                searchText,
                                                "Pink",
                                                receiveArgumentsInt!!,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == brown) {
                                            val folder = Folders(
                                                searchText,
                                                "Brown",
                                                receiveArgumentsInt!!,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        } else if (colorSelected == orange) {
                                            val folder = Folders(
                                                searchText,
                                                "Orange",
                                                receiveArgumentsInt!!,
                                                false,
                                                "",
                                                "",
                                                false,
                                                false
                                            )
                                            viewModel.insertFolders(folder)
                                        }
                                        editTextFolderName.clearFocus()
                                        val imm =
                                            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.hideSoftInputFromWindow(editTextFolderName.windowToken, 0)
                                        alertDialog.dismiss()
                                        (activity as MainActivity).notifySetdatachanged()
                                        (activity as MainActivity).updateLiveData()
                                    }
                                    val lineColor = ContextCompat.getColor(context, R.color.black)
                                    val colorFilter =
                                        PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                                    editTextFolderName.background.colorFilter = colorFilter
                                    errorText.visibility = View.GONE
                                } else {
                                    addButton.isEnabled = false
                                    val lineColor = ContextCompat.getColor(context, R.color.black)
                                    val colorFilter =
                                        PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                                    editTextFolderName.background.colorFilter = colorFilter
                                    errorText.visibility = View.GONE
                                }
                            }

                        }
                })
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        alertDialog.show()
    }

    private fun showPopupMenuCreateSubFolder(view: View) {
        menuCreateSubFolder.setOnClickListener {
            showCreateFolderAlertDialog(requireActivity())
            dismissMenu()
        }
        popupWindowCreateSubFolder.isOutsideTouchable = true
        popupWindowCreateSubFolder.animationStyle = R.style.PopupAnimation
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val xOffset = location[0]
        val yOffset = location[1] - view.height
        popupWindowCreateSubFolder.showAtLocation(view, Gravity.NO_GRAVITY, xOffset, yOffset)
    }
    private fun showMoveToFolderAlertDialog(context: Context, folder: Folders, moveAlertDialog: AlertDialog) {
        val builder = AlertDialog.Builder(context, R.style.RoundedAlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView: View = inflater.inflate(R.layout.move_folder_alert_dialog, null)

        val moveCancelButton=dialogView.findViewById<Button>(R.id.cancelButton)
        val moveButton=dialogView.findViewById<Button>(R.id.moveButton)
        val moveText=dialogView.findViewById<TextView>(R.id.move_text)

        val alertDialog = builder.setView(dialogView).create()
        val window: Window = alertDialog.window!!

        //Adjust the position of the window and set the bottom margin
        val params = window.attributes
        params.gravity = Gravity.BOTTOM
        params.y = context.resources.getDimensionPixelSize(R.dimen.margin20dp)


        //Make the window full screen
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        params.width = width
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // Update window
        window.attributes = params


        if (mutableLockList.isNotEmpty() && mutableFolderList.isEmpty()) {
            if (mainFolderCheck){
                moveButton.setOnClickListener {
                    mutableLockList.map {
                        val updatedNote=Notes(it.title,it.text,it.lock,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,-1,it.trash,it.trashTime,it.trashStartTime,false)
                        updatedNote.id=it.id
                        viewModel.updateNotes(updatedNote)
                    }
                    if (binding.customImageButton.visibility == View.VISIBLE) {
                        receiveCommon()
                        receiveArguments()
                    }
                    alertDialog.dismiss()
                    moveAlertDialog.dismiss()
                    (activity as MainActivity).notifySetdatachanged()
                    (activity as MainActivity).updateLiveData()
                }
                if (mutableLockList.size > 1) {
                    moveText.text = "Move ${mutableLockList.size} notes to Folders?"
                } else {
                    moveText.text = "Move ${mutableLockList.size} note to Folders?"
                }
            }else{
                moveButton.setOnClickListener {
                    mutableLockList.map {
                        folder.id?.let { id->
                            val updatedNote=Notes(it.title,it.text,it.lock,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,id,it.trash,it.trashTime,it.trashStartTime,false)
                            updatedNote.id=it.id
                            viewModel.updateNotes(updatedNote)
                        }
                    }
                    if (binding.customImageButton.visibility == View.VISIBLE) {
                        receiveCommon()
                        receiveArguments()
                    }
                    alertDialog.dismiss()
                    moveAlertDialog.dismiss()
                    (activity as MainActivity).notifySetdatachanged()
                    (activity as MainActivity).updateLiveData()
                }
                if (mutableLockList.size > 1) {
                    moveText.text = "Move ${mutableLockList.size} notes to ${folder.folderName}?"
                } else {
                    moveText.text = "Move ${mutableLockList.size} note to ${folder.folderName}?"
                }
            }
        } else if (mutableLockList.isEmpty() && mutableFolderList.isNotEmpty()) {
            if (mainFolderCheck){
                if (mutableFolderList.size > 1) {
                    moveText.text = "Move ${mutableFolderList.size} folders to Folders?"
                } else {
                    moveText.text = "Move ${mutableFolderList.size} folder to Folders?"
                }
                moveButton.setOnClickListener {
                    mutableFolderList.map {
                        val updatedFolders=Folders(it.folderName,it.color,-1,it.trash,it.trashTime,it.trashStartTime,false,false)
                        updatedFolders.id=it.id
                        viewModel.updateFolders(updatedFolders)
                    }
                    if (binding.customImageButton.visibility == View.VISIBLE) {
                        receiveCommon()
                        receiveArguments()
                    }
                    alertDialog.dismiss()
                    moveAlertDialog.dismiss()
                    (activity as MainActivity).notifySetdatachanged()
                    (activity as MainActivity).updateLiveData()
                }
            }else{
                if (mutableFolderList.size > 1) {
                    moveText.text = "Move ${mutableFolderList.size} folders to ${folder.folderName}?"
                } else {
                    moveText.text = "Move ${mutableFolderList.size} folder to ${folder.folderName}?"
                }
                moveButton.setOnClickListener {
                    mutableFolderList.map {
                        folder.id?.let {id->
                            val updatedFolders=Folders(it.folderName,it.color,id,it.trash,it.trashTime,it.trashStartTime,false,false)
                            updatedFolders.id=it.id
                            viewModel.updateFolders(updatedFolders)
                        }
                    }
                    if (binding.customImageButton.visibility == View.VISIBLE) {
                        receiveCommon()
                        receiveArguments()
                    }
                    alertDialog.dismiss()
                    moveAlertDialog.dismiss()
                    (activity as MainActivity).notifySetdatachanged()
                    (activity as MainActivity).updateLiveData()
                }
            }
        } else if (mutableLockList.isNotEmpty() && mutableFolderList.isNotEmpty()) {
            if (mainFolderCheck){
                if (mutableFolderList.size == 1 && mutableLockList.size == 1) {
                    moveText.text = "Move ${mutableFolderList.size} folder and ${mutableLockList.size} note to Folders?"
                } else if (mutableLockList.size > 1 && mutableFolderList.size > 1) {
                    moveText.text = "Move ${mutableFolderList.size} folders and ${mutableLockList.size} notes to Folders?"
                } else if (mutableLockList.size == 1 && mutableFolderList.size > 1) {
                    moveText.text = "Move ${mutableFolderList.size} folders and ${mutableLockList.size} note to Folders?"
                } else if (mutableLockList.size > 1 && mutableFolderList.size == 1) {
                    moveText.text = "Move ${mutableFolderList.size} folder and ${mutableLockList.size} notes to Folders?"
                }
                moveButton.setOnClickListener {
                    mutableFolderList.map {
                        val updatedFolders=Folders(it.folderName,it.color,-1,it.trash,it.trashTime,it.trashStartTime,false,false)
                        updatedFolders.id=it.id
                        viewModel.updateFolders(updatedFolders)
                    }
                    mutableLockList.map {
                        val updatedNotes=Notes(it.title,it.text,it.lock,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,-1,it.trash,it.trashTime,it.trashStartTime,false)
                        updatedNotes.id=it.id
                        viewModel.updateNotes(updatedNotes)
                    }
                    if (binding.customImageButton.visibility == View.VISIBLE) {
                        receiveCommon()
                        receiveArguments()
                    }
                    alertDialog.dismiss()
                    moveAlertDialog.dismiss()
                    (activity as MainActivity).notifySetdatachanged()
                    (activity as MainActivity).updateLiveData()
                }
            }else{
                if (mutableFolderList.size == 1 && mutableLockList.size == 1) {
                    moveText.text = "Move ${mutableFolderList.size} folder and ${mutableLockList.size} note to ${folder.folderName}?"
                } else if (mutableLockList.size > 1 && mutableFolderList.size > 1) {
                    moveText.text = "Move ${mutableFolderList.size} folders and ${mutableLockList.size} notes to ${folder.folderName}?"
                } else if (mutableLockList.size == 1 && mutableFolderList.size > 1) {
                    moveText.text = "Move ${mutableFolderList.size} folders and ${mutableLockList.size} note to ${folder.folderName}?"
                } else if (mutableLockList.size > 1 && mutableFolderList.size == 1) {
                    moveText.text = "Move ${mutableFolderList.size} folder and ${mutableLockList.size} notes to ${folder.folderName}?"
                }
                moveButton.setOnClickListener {
                    mutableFolderList.map {
                        folder.id?.let {id->
                            val updatedFolders=Folders(it.folderName,it.color,id,it.trash,it.trashTime,it.trashStartTime,false,false)
                            updatedFolders.id=it.id
                            viewModel.updateFolders(updatedFolders)
                        }
                    }
                    mutableLockList.map {
                        folder.id?.let {id->
                            val updatedNotes=Notes(it.title,it.text,it.lock,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,id,it.trash,it.trashTime,it.trashStartTime,false)
                            updatedNotes.id=it.id
                            viewModel.updateNotes(updatedNotes)
                        }
                    }
                    if (binding.customImageButton.visibility == View.VISIBLE) {
                        receiveCommon()
                        receiveArguments()
                    }
                    alertDialog.dismiss()
                    moveAlertDialog.dismiss()
                    (activity as MainActivity).notifySetdatachanged()
                    (activity as MainActivity).updateLiveData()
                }
            }
        }

        moveCancelButton.setOnClickListener {
            alertDialog.dismiss()

        }
        alertDialog.setOnDismissListener {
            mainFolderCheck=false
        }
        alertDialog.show()
    }

    private fun showTrashEmptyAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context, R.style.RoundedAlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView: View = inflater.inflate(R.layout.trash_empty_alert_dialog, null)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val deleteButton = dialogView.findViewById<Button>(R.id.deleteButton)
        val trashEmptyText = dialogView.findViewById<TextView>(R.id.trash_empty_text)

        val alertDialog = builder.setView(dialogView).create()
        val window: Window = alertDialog.window!!

        //Adjust the position of the window and set the bottom margin
        val params = window.attributes
        params.gravity = Gravity.BOTTOM
        params.y = context.resources.getDimensionPixelSize(R.dimen.margin20dp)


        //Make the window full screen
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        params.width = width
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // Update window
        window.attributes = params

        viewModel.notesLiveData.observe(viewLifecycleOwner, Observer { notesList->
            viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {foldersList->
                val filteredNotes=notesList.filter { it.trash }
                val filteredFolders=foldersList.filter { it.trash }
                if (filteredNotes.isNotEmpty() && filteredFolders.isEmpty()) {

                    if (filteredNotes.size > 1) {
                        trashEmptyText.text = "${filteredNotes.size} items will be permanently deleted from the cloud and all your devices."
                    } else {
                        trashEmptyText.text = "${filteredNotes.size} item will be permanently deleted from the cloud and all your devices."
                    }
                } else if (filteredNotes.isEmpty() && filteredFolders.isNotEmpty()) {
                    if (filteredFolders.size > 1) {
                        trashEmptyText.text = "${filteredFolders.size} items will be permanently deleted from the cloud and all your devices."
                    } else {
                        trashEmptyText.text = "${filteredFolders.size} item will be permanently deleted from the cloud and all your devices."
                    }
                } else if (filteredNotes.isNotEmpty() && filteredFolders.isNotEmpty()) {
                    trashEmptyText.text = "${filteredNotes.size + filteredFolders.size} items will be permanently deleted from the cloud and all your devices."
                }
            })
        })

//        if (notesAdapter.notesList.isNotEmpty() && foldersAdapter.folderList.isEmpty()) {
//            if (notesAdapter.notesList.any { it.trash }) {
//                val trashList = notesAdapter.notesList.filter { it.trash }
//                if (trashList.size > 1) {
//                    trashEmptyText.text = "${trashList.size} items will be permanently deleted from the cloud and all your devices."
//                } else {
//                    trashEmptyText.text = "${trashList.size} item will be permanently deleted from the cloud and all your devices."
//                }
//            }
//        } else if (notesAdapter.notesList.isEmpty() && foldersAdapter.folderList.isNotEmpty()) {
//            if (foldersAdapter.folderList.any { it.trash }) {
//                val trashList = foldersAdapter.folderList.filter { it.trash }
//                if (trashList.size > 1) {
//                    trashEmptyText.text = "${trashList.size} items will be permanently deleted from the cloud and all your devices."
//                } else {
//                    trashEmptyText.text = "${trashList.size} item will be permanently deleted from the cloud and all your devices."
//                }
//            }
//        } else if (notesAdapter.notesList.isNotEmpty() && foldersAdapter.folderList.isNotEmpty()) {
//            if (foldersAdapter.folderList.any { it.trash } && notesAdapter.notesList.any { it.trash }) {
//                val trashListNote = notesAdapter.notesList.filter { it.trash }
//                val trashListFolder = foldersAdapter.folderList.filter { it.trash }
//                trashEmptyText.text =
//                    "${trashListNote.size + trashListFolder.size} items will be permanently deleted from the cloud and all your devices."
//            }
//        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        deleteButton.setOnClickListener {
            viewModel.notesLiveData.observe(viewLifecycleOwner, Observer { notesList->
                viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {foldersList->
                    val filteredNotes=notesList.filter { it.trash }
                    val filteredFolders=foldersList.filter { it.trash }
                    if (filteredNotes.isNotEmpty() && filteredFolders.isEmpty()) {
                        filteredNotes.map {
                            viewModel.deleteNotes(it)
                        }
                    } else if (filteredNotes.isEmpty() && filteredFolders.isNotEmpty()) {
                        filteredFolders.map {
                            viewModel.deleteFolders(it)
                        }
                    } else if (filteredNotes.isNotEmpty() && filteredFolders.isNotEmpty()) {
                        filteredFolders.map { folders->
                            filteredNotes.map { notes->
                                viewModel.deleteNotes(notes)
                                viewModel.deleteFolders(folders)
                                val notesDeleted= filteredNotes.filter { it.parentId==folders.id }
                                notesDeleted.map {listNotes->
                                    viewModel.deleteNotes(listNotes)
                                }
                            }
                        }
                    }
                })
            })
//            if (mutableLockList.isNotEmpty() && mutableFolderList.isNotEmpty()) {
//
//            } else if (mutableLockList.isEmpty() && mutableFolderList.isNotEmpty()) {
//
//            } else if (mutableFolderList.isEmpty() && mutableLockList.isNotEmpty()) {
//
//            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }

        alertDialog.show()
    }

    private fun showShareLockAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context, R.style.RoundedAlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView: View = inflater.inflate(R.layout.bottom_share_lock_alert_dialog, null)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val removeButton = dialogView.findViewById<Button>(R.id.removeButton)

        val alertDialog = builder.setView(dialogView).create()
        val window: Window = alertDialog.window!!

        //Adjust the position of the window and set the bottom margin
        val params = window.attributes
        params.gravity = Gravity.BOTTOM
        params.y = context.resources.getDimensionPixelSize(R.dimen.margin20dp)


        //Make the window full screen
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        params.width = width
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // Update window
        window.attributes = params


        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        removeButton.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_passwordFragment)
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showRenameFolderAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context, R.style.RoundedAlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView: View = inflater.inflate(R.layout.bottom_rename_alert_dialog, null)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val renameButton = dialogView.findViewById<Button>(R.id.renameButton)
        val editTextFolderName = dialogView.findViewById<EditText>(R.id.folder_name_editText)
        val errorText = dialogView.findViewById<TextView>(R.id.rename_same)

        val alertDialog = builder.setView(dialogView).create()
        val window: Window = alertDialog.window!!

        //Adjust the position of the window and set the bottom margin
        val params = window.attributes
        params.gravity = Gravity.BOTTOM
        params.y = context.resources.getDimensionPixelSize(R.dimen.margin20dp)


        //Make the window full screen
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        params.width = width
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // Update window
        window.attributes = params

        editTextFolderName.requestFocus()
        editTextFolderName.postDelayed({
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editTextFolderName, InputMethodManager.SHOW_FORCED)
        }, 250)


        mutableFolderList.map {
            editTextFolderName.setText(it.folderName)
        }
        cancelButton.setOnClickListener {
            editTextFolderName.clearFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editTextFolderName.windowToken, 0)
            alertDialog.dismiss()
        }
        editTextFolderName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                viewModel.folderLiveData.observe(viewLifecycleOwner, Observer { list ->
                    val parentFolder = list.filter { !it.trash && it.parentFolderId == receiveArgumentsInt }
                    val mainFolder = list.filter { !it.trash && it.parentFolderId == -1 }
                    val filteredFolders = parentFolder.filter { !it.trash }
                    val filteredFoldersName=list.filter { !it.trash }
                    filteredFoldersName.map { name->
                        if (receiveArgumentString=="Folders"){
                            if (mainFolder.any { it.folderName == searchText }) {
                                renameButton.isEnabled = false
                                val lineColor = ContextCompat.getColor(requireActivity(), R.color.red)
                                val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                                editTextFolderName.background.colorFilter = colorFilter
                                errorText.visibility = View.VISIBLE
                            } else {
                                renameButton.isEnabled = true
                                renameButton.setOnClickListener {
                                    mutableFolderList.map {
                                        val updatedFolder = Folders(
                                            searchText,
                                            it.color,
                                            it.parentFolderId,
                                            it.trash,
                                            it.trashTime,
                                            it.trashStartTime,
                                            false,
                                            false
                                        )
                                        updatedFolder.id = it.id
                                        viewModel.updateFolders(updatedFolder)
                                    }
                                    if (binding.customImageButton.visibility == View.VISIBLE) {
                                        receiveCommon()
                                        receiveArguments()
                                    }
                                    editTextFolderName.clearFocus()
                                    val imm =
                                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    imm.hideSoftInputFromWindow(editTextFolderName.windowToken, 0)
                                    alertDialog.dismiss()
                                    (activity as MainActivity).notifySetdatachanged()
                                    (activity as MainActivity).updateLiveData()
                                }
                                val lineColor = ContextCompat.getColor(requireActivity(), R.color.black)
                                val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                                editTextFolderName.background.colorFilter = colorFilter
                                errorText.visibility = View.GONE
                            }
                        }else if(receiveArgumentString==name.folderName){
                            if (filteredFolders.any { it.folderName == searchText }) {
                                renameButton.isEnabled = false
                                val lineColor = ContextCompat.getColor(requireActivity(), R.color.red)
                                val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                                editTextFolderName.background.colorFilter = colorFilter
                                errorText.visibility = View.VISIBLE
                            } else {
                                renameButton.isEnabled = true
                                renameButton.setOnClickListener {
                                    mutableFolderList.map {
                                        val updatedFolder = Folders(
                                            searchText,
                                            it.color,
                                            it.parentFolderId,
                                            it.trash,
                                            it.trashTime,
                                            it.trashStartTime,
                                            false,
                                            false
                                        )
                                        updatedFolder.id = it.id
                                        viewModel.updateFolders(updatedFolder)
                                    }
                                    if (binding.customImageButton.visibility == View.VISIBLE) {
                                        receiveCommon()
                                        receiveArguments()
                                    }
                                    editTextFolderName.clearFocus()
                                    val imm =
                                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    imm.hideSoftInputFromWindow(editTextFolderName.windowToken, 0)
                                    alertDialog.dismiss()
                                    (activity as MainActivity).notifySetdatachanged()
                                    (activity as MainActivity).updateLiveData()
                                }
                                val lineColor = ContextCompat.getColor(requireActivity(), R.color.black)
                                val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                                editTextFolderName.background.colorFilter = colorFilter
                                errorText.visibility = View.GONE
                            }
                        }
                    }
            })
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })
        alertDialog.show()
    }
    private fun showFolderColorAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context, R.style.RoundedAlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView: View = inflater.inflate(R.layout.bottom_folder_color_alert_dialog, null)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val gray=dialogView.findViewById<ImageView>(R.id.gray)
        val red=dialogView.findViewById<ImageView>(R.id.red)
        val orange=dialogView.findViewById<ImageView>(R.id.orange)
        val yellow=dialogView.findViewById<ImageView>(R.id.yellow)
        val openGreen=dialogView.findViewById<ImageView>(R.id.open_green)
        val green=dialogView.findViewById<ImageView>(R.id.green)
        val openBlue1=dialogView.findViewById<ImageView>(R.id.open_blue1)
        val openRed=dialogView.findViewById<ImageView>(R.id.open_red)
        val pink=dialogView.findViewById<ImageView>(R.id.pink)
        val openBlue2=dialogView.findViewById<ImageView>(R.id.open_blue2)
        val blue=dialogView.findViewById<ImageView>(R.id.blue)
        val brown=dialogView.findViewById<ImageView>(R.id.brown)
        val alertDialog = builder.setView(dialogView).create()
        val window: Window = alertDialog.window!!

        //Adjust the position of the window and set the bottom margin
        val params = window.attributes
        params.gravity = Gravity.BOTTOM
        params.y = context.resources.getDimensionPixelSize(R.dimen.margin20dp)


        //Make the window full screen
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        params.width = width
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // Update window
        window.attributes = params

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        gray.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Gray",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        red.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Red",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        orange.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Orange",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        yellow.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Yellow",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        openGreen.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"OpenGreen",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        green.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Green",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        openBlue1.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"OpenBlue1",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        openRed.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"OpenRed",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        pink.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Pink",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        openBlue2.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"OpenBlue2",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        blue.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Blue",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        brown.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Brown",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
                receiveArguments()
            }
            alertDialog.dismiss()
            (activity as MainActivity).notifySetdatachanged()
            (activity as MainActivity).updateLiveData()
        }
        alertDialog.show()
    }
    private fun bottomBarVisibilityShow() {
        if (bottomBar.visibility == View.VISIBLE) {
            // if bottomBar is shown,hide it
        } else {
            // if bottomBar is hidden,show it
            bottomBar.visibility = View.VISIBLE
            bottomBar.animate().translationY(0f)
        }
    }
    private fun bottomBarVisibilityHide() {
        if (bottomBar.visibility == View.VISIBLE) {
            // if bottomBar is shown,hide it
            bottomBar.animate().translationY(bottomBar.height.toFloat())
                .withEndAction { bottomBar.visibility = View.GONE }
        } else {
            // if bottomBar is hidden doing nothing
        }
    }
    private fun bottomBarVisibilityHideTrash() {
        if (bottomBar.visibility == View.VISIBLE) {
            // if bottomBar is shown,hide it
            bottomBar.animate().translationY(bottomBar.height.toFloat())
                .withEndAction { bottomBar.visibility = View.GONE
                    binding.moveLinear.visibility=View.VISIBLE
                    binding.shareLinear.visibility=View.VISIBLE
                    binding.moreLinear.visibility=View.VISIBLE
                    updateLockMargin()
                }
        } else {
            // if bottomBar is hidden doing nothing
        }
    }
    //    private fun controlSharedNavigation(){
//        val receiveSharedPreferencesInt=sharedPreferences.getInt("Int",-100)
//        if(receiveSharedPreferencesInt==-1){
//            sharedPreferences.edit().putInt("Int",-1).apply()
//            requireActivity().finish()
//        }else if (receiveSharedPreferencesInt==-5){
//            sharedPreferences.edit().putInt("Int",-5).apply()
//            requireActivity().finish()
//        }
//    }
    private fun onBackPresses(getString:String){
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                if (binding.customImageButton.visibility == View.VISIBLE) {
                    receiveCommon()
                    if (getString == "Favorites") {
                        binding.titleText.text = getString
                        binding.homeTitle.text = getString
                        receiveFavNotes()
                    } else if (getString == "All notes") {
                        binding.titleText.text = getString
                        binding.homeTitle.text = getString
                        receiveAllNotes()
                    } else if (getString == "Locked notes") {
                        binding.titleText.text = getString
                        binding.homeTitle.text = getString
                        receiveLockNotes()
                    } else if (getString == "Trash") {
                        binding.titleText.text = getString
                        binding.homeTitle.text = getString
                        receiveTrash()
                    } else if (getString == "Folder") {
                        binding.titleText.text = getString
                        binding.homeTitle.text = getString
                        receiveFolders()
                    }else{
                        binding.titleText.text = getString
                        binding.homeTitle.text = getString
                        receiveFolders()
                    }
                    true // Key processed
                } else {
                    (activity as MainActivity).finish()
                    dismissMenu()
                    false // Key not processed, default behavior continues
                }
            } else {
//                (activity as MainActivity).finish()
                dismissMenu()
                false // Key not processed, default behavior continues
            }
        }
    }
}
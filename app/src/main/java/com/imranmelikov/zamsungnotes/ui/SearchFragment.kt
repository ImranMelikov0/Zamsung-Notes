package com.imranmelikov.zamsungnotes.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
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
import androidx.appcompat.app.AlertDialog
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
import com.imranmelikov.zamsungnotes.adapter.HomeNotesAdapter
import com.imranmelikov.zamsungnotes.adapter.MoveFolderAdapter
import com.imranmelikov.zamsungnotes.databinding.FragmentSearchBinding
import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.model.Notes
import com.imranmelikov.zamsungnotes.mvvm.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var editText: EditText
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var popupWindowSettings: PopupWindow
    private lateinit var menuSetting: TextView
    private lateinit var notesAdapter: HomeNotesAdapter
    private lateinit var bottomBar: AppBarLayout
    private lateinit var customImageButton: ImageView
    private val mutableLockList= mutableListOf<Notes>()
    private var isButtonChecked = false
    private lateinit var popupWindowBottomMore: PopupWindow
    private lateinit var menuSave: TextView
    private lateinit var menuDuplicate: TextView
    private lateinit var menuAddFav: TextView
    private lateinit var menuInvite: TextView
    private lateinit var menuAddShared: TextView
    private lateinit var viewModel: HomeViewModel
    private lateinit var moveAdapter: MoveFolderAdapter
    private var mainFolderCheck:Boolean=false
    private var checkRvView = true
    private lateinit var popupWindowCreateSubFolder: PopupWindow
    private lateinit var menuCreateSubFolder: TextView
    private lateinit var speechRecognizer: SpeechRecognizer
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentSearchBinding.inflate(inflater,container,false)
        viewModel= ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        customImageButton= binding.customImageButton
        bottomBar=binding.bottomBar
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        moveAdapter= MoveFolderAdapter(requireContext(),viewModel, emptyList())
        viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {
            moveAdapter.updateData(it)
        })
        clicks()
    }
    private fun clicks(){
        binding.homeVoice.setOnClickListener {
            startSpeechToText()
        }
        editTextControl()
        popupSettings()
        bottomBarMenu()
        scrollSearch()
        popupSearchBottomMore()
        checkSelectAll()
        popupCreateSubFolder()

        binding.searchBack.setOnClickListener {
            (activity as MainActivity).updateLiveData()
            (activity as MainActivity).notifySetdatachanged()
            findNavController().popBackStack()
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            dismissMenu()
        }
        binding.homeMore.setOnClickListener {
            showPopupMenu(it)
        }
    }

    private fun popupSearchBottomMore(){
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
            }
            dismissMenu()
        }
        menuAddFav.setOnClickListener {
            if (mutableLockList.isNotEmpty()&&mutableLockList.any { !it.favStar }){
                mutableLockList.map {
                    val updatedNotes=Notes(it.title,it.text,it.lock,it.createDate,true,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,it.parentId,it.trash,it.trashTime,it.trashStartTime,false)
                    updatedNotes.id=it.id
                    viewModel.updateNotes(updatedNotes)
                }
            }else if (mutableLockList.any { it.favStar }){
                mutableLockList.map {
                    val updatedNotes=Notes(it.title,it.text,it.lock,it.createDate,false,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,it.parentId,it.trash,it.trashTime,it.trashStartTime,false)
                    updatedNotes.id=it.id
                    viewModel.updateNotes(updatedNotes)
                }
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
            }
            dismissMenu()
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
    private fun receiveCommon(){
        binding.customImageButton.visibility = View.GONE
        binding.homeAllText.visibility=View.GONE
        binding.homeMore.visibility=View.VISIBLE
        binding.searchBack.visibility=View.VISIBLE
        isButtonChecked = false
        clickCheckButton()
        notesAdapter.clickSelectAllHide()
        mutableLockList.clear()
        notesAdapter.hideSelectButton()
        dismissMenu()
        customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
        binding.homeVoice.visibility = View.VISIBLE
        binding.homeCancel.visibility = View.GONE
        binding.searchEditText.visibility=View.VISIBLE
        binding.titleText.text = "Search"
        binding.searchEditText.hint = "Search"
        bottomBarVisibilityHide()
    }

    private fun bottomBarMenu(){
        binding.moveLinear.setOnClickListener {
            if (mutableLockList.isNotEmpty()){
                showBottomMoveAlertDialog(requireActivity())
            }
        }
        binding.moreLinear.setOnClickListener {
            if (mutableLockList.isNotEmpty()&&mutableLockList.any { !it.lock&&!it.trash }){
                showPopupMenuBottomMore(it)
            }else{
                if (binding.customImageButton.visibility == View.VISIBLE) {
                    receiveCommon()
                }
            }
        }
        binding.deleteLinear.setOnClickListener {
            if (mutableLockList.isNotEmpty()&&mutableLockList.any { !it.lock&&!it.trash }){
                showBottomDeleteAlertDialog(requireActivity())
            }else{
                if (binding.customImageButton.visibility == View.VISIBLE) {
                    receiveCommon()
                }
            }
        }
        binding.shareLinear.setOnClickListener {
            if (mutableLockList.isNotEmpty()&&mutableLockList.any { !it.lock&&!it.trash }){

            }else{
                if (binding.customImageButton.visibility == View.VISIBLE) {
                    receiveCommon()
                }
            }
        }
        binding.lockLinear.setOnClickListener {
            if (mutableLockList.isNotEmpty()&&mutableLockList.any { !it.lock }){
                mutableLockList.map {
                    val updatedNotes=Notes(it.title,it.text,true,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,it.parentId,it.trash,it.trashTime,it.trashStartTime,false)
                    updatedNotes.id=it.id
                    viewModel.updateNotes(updatedNotes)
                    notesAdapter.notifyDataSetChanged()
                }
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
            }
        }
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
            val filteredId= allFolders.filter { !it.trash }
            moveAdapter.moveFolderList=filteredId
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
            val folder= Folders("","",-2,false,"","",false,false)
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
                viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {folders->
                    val mainFolderList=folders.filter { !it.trash&&it.parentFolderId==-1 }
                    if (mainFolderList.any { it.folderName == searchText }&&searchText.isNotEmpty()) {
                        addButton.isEnabled = false
                        val lineColor = ContextCompat.getColor(context, R.color.red)
                        val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                        editTextFolderName.background.colorFilter = colorFilter
                        errorText.visibility = View.VISIBLE
                    } else if (searchText.isNotEmpty()){
                        addButton.isEnabled = true
                        addButton.setOnClickListener {
                            if (colorSelected==red){
                                val folder=Folders(searchText,"Red",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            }else if (colorSelected==gray){
                                val folder= Folders(searchText,"Gray",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            }else if (colorSelected==green){
                                val folder=Folders(searchText,"Green",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            } else if (colorSelected==yellow){
                                val folder=Folders(searchText,"Yellow",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            }else if (colorSelected==blue){
                                val folder=Folders(searchText,"Blue",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            }
                            else if (colorSelected==openBlue1){
                                val folder=Folders(searchText,"OpenBlue1",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            }else if (colorSelected==openBlue2){
                                val folder=Folders(searchText,"OpenBlue2",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            }
                            else if (colorSelected==openGreen){
                                val folder=Folders(searchText,"OpenGreen",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            }else if (colorSelected==openRed){
                                val folder=Folders(searchText,"OpenRed",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            }
                            else if (colorSelected==pink){
                                val folder=Folders(searchText,"Pink",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            }else if (colorSelected==brown){
                                val folder=Folders(searchText,"Brown",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            }else if (colorSelected==orange){
                                val folder=Folders(searchText,"Orange",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            }
                            editTextFolderName.clearFocus()
                            val imm =
                                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(editTextFolderName.windowToken, 0)
                            alertDialog.dismiss()
                        }
                        val lineColor = ContextCompat.getColor(context, R.color.black)
                        val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                        editTextFolderName.background.colorFilter = colorFilter
                        errorText.visibility = View.GONE
                    }else{
                        addButton.isEnabled = false
                        val lineColor = ContextCompat.getColor(context, R.color.black)
                        val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                        editTextFolderName.background.colorFilter = colorFilter
                        errorText.visibility = View.GONE
                    }
                })
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        alertDialog.show()
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


        if (mutableLockList.isNotEmpty()) {
            if (mainFolderCheck){
                moveButton.setOnClickListener {
                    mutableLockList.map {
                        val updatedNote=Notes(it.title,it.text,it.lock,it.createDate,it.favStar,it.imgUrl,it.pdf,it.imgScan,it.audio,it.voice,-1,it.trash,it.trashTime,it.trashStartTime,false)
                        updatedNote.id=it.id
                        viewModel.updateNotes(updatedNote)
                    }
                    if (binding.customImageButton.visibility == View.VISIBLE) {
                        receiveCommon()
                    }
                    alertDialog.dismiss()
                    moveAlertDialog.dismiss()
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
                    }
                    alertDialog.dismiss()
                    moveAlertDialog.dismiss()
                }
                if (mutableLockList.size > 1) {
                    moveText.text = "Move ${mutableLockList.size} notes to ${folder.folderName}?"
                } else {
                    moveText.text = "Move ${mutableLockList.size} note to ${folder.folderName}?"
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
    private fun showBottomDeleteAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context, R.style.RoundedAlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView: View = inflater.inflate(R.layout.bottom_delete_alert_dialog, null)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val deleteButton = dialogView.findViewById<Button>(R.id.deleteButton)
        val trashEmptyText=dialogView.findViewById<TextView>(R.id.trash_empty_text)


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
            }
            alertDialog.dismiss()
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
    private fun searchRecyclerView(){
        notesAdapter= HomeNotesAdapter(requireActivity())
        binding.rvSearch.layoutManager= GridLayoutManager(context,2)
        notesAdapter.onItemClickFirstItem={
            if (it){
                bottomBarVisibilityShow()
            }else{
                mutableLockList.clear()
                bottomBarVisibilityHide()
                isButtonChecked = false
                clickCheckButton()
                notesAdapter.clickSelectAllHide()
                customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
            }
        }
        notesAdapter.onItemClickSendNotes={
            mutableLockList.add(it)
            binding.titleText.text = "${mutableLockList.size} selected"
            binding.searchEditText.hint = "${mutableLockList.size} selected"
            if (mutableLockList.any { it.favStar}&&mutableLockList.any { !it.favStar }||mutableLockList.any { !it.favStar }){
                menuAddFav.text="Add to favorites"

            }else if (mutableLockList.any { it.favStar }){
                menuAddFav.text="Remove from favorites"
            }
            if (mutableLockList.size==1){
                menuInvite.visibility=View.VISIBLE
                menuAddShared.visibility=View.VISIBLE
                menuDuplicate.visibility=View.VISIBLE
            }else{
                menuInvite.visibility=View.GONE
                menuAddShared.visibility=View.GONE
                menuDuplicate.visibility=View.GONE
            }
        }
        notesAdapter.onItemClickSendNotesRm={
            mutableLockList.remove(it)
            if (mutableLockList.isEmpty()){
                binding.titleText.text = "Select notes"
                binding.searchEditText.hint = "Select notes"
            }else{
                binding.titleText.text = "${mutableLockList.size} selected"
                binding.searchEditText.hint = "${mutableLockList.size} selected"
            }
            if (mutableLockList.any { it.favStar}&&mutableLockList.any { !it.favStar}||mutableLockList.any { !it.favStar }){
                menuAddFav.text="Add to favorites"

            }else if (mutableLockList.any { it.favStar }){
                menuAddFav.text="Remove from favorites"
            }
            if (mutableLockList.size==1){
                menuInvite.visibility=View.VISIBLE
                menuAddShared.visibility=View.VISIBLE
                menuDuplicate.visibility=View.VISIBLE
            }else{
                menuInvite.visibility=View.GONE
                menuAddShared.visibility=View.GONE
                menuDuplicate.visibility=View.GONE
            }
        }
        notesAdapter.onItemClickCheckAll={
            if (it){
                isButtonChecked = true
                clickCheckButton()
                notesAdapter.clickSelectAllShow()
                customImageButton.setImageResource(R.drawable.baseline_check_circle_24)
            }else{
                isButtonChecked = false
                clickCheckButton()
                customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
            }
        }
//        notesAdapter.onItemLongListener={
//            Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show()
//            binding.titleText.text = "Select notes"
//            binding.searchEditText.hint = "Select notes"
//            binding.searchBack.visibility=View.GONE
//            binding.homeVoice.visibility = View.GONE
//            binding.homeCancel.visibility = View.GONE
//            binding.searchEditText.visibility=View.GONE
//            customImageButton.visibility=View.VISIBLE
//            binding.homeAllText.visibility=View.VISIBLE
//            binding.homeMore.visibility=View.GONE
//            notesAdapter.showSelectButton()
//        }
        binding.rvSearch.adapter=notesAdapter
        viewModel.noteSearchLiveData.observe(viewLifecycleOwner, Observer {list->
            val listLockTrash=list.filter { !it.lock&&!it.trash }
            notesAdapter.notesList=listLockTrash
            if (list.isEmpty()){
                binding.searchNotFoundText.text="No recent searches"
                binding.searchNotFoundText.visibility=View.VISIBLE
            }else{
                binding.searchNotFoundText.visibility=View.VISIBLE
            }
        })
    }
    private fun selectAllTrue(){
        isButtonChecked = false
        notesAdapter.clickSelectAllHide()
        customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
        bottomBarVisibilityHide()
        mutableLockList.clear()
        binding.titleText.text = "Select notes"
        binding.searchEditText.hint = "Select notes"
    }
    private fun selectAllFalse(){
        mutableLockList.clear()
        mutableLockList.addAll(notesAdapter.notesList)
        binding.titleText.text = "${mutableLockList.size} selected"
        binding.searchEditText.hint = "${mutableLockList.size} selected"
        menuInvite.visibility=View.GONE
        menuAddShared.visibility=View.GONE
        menuDuplicate.visibility=View.GONE
        if (mutableLockList.isNotEmpty()&&mutableLockList.any { it.favStar }&&mutableLockList.any { !it.favStar }||mutableLockList.any { !it.favStar }){
            menuAddFav.text="Add to favorites"
        }else if (mutableLockList.any { it.favStar }){
            menuAddFav.text="Remove from favorites"
        }
        isButtonChecked = true
        notesAdapter.clickSelectAllShow()
        customImageButton.setImageResource(R.drawable.baseline_check_circle_24)
        bottomBarVisibilityShow()
    }
    private fun checkSelectAll(){
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
    private fun clickCheckButton(){
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
    private fun bottomBarVisibilityHide() {
        if (bottomBar.visibility == View.VISIBLE) {
            // if bottomBar is shown,hide it
            bottomBar.animate().translationY(bottomBar.height.toFloat())
                .withEndAction { bottomBar.visibility = View.GONE }
        } else {
            // if bottomBar is hidden doing nothing
        }
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
    private fun scrollSearch() {
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

    private fun editTextControl(){
        editText=binding.searchEditText
        editText.requestFocus()
        editText.postDelayed({
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 250)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()

                if (searchText.isNotEmpty()){
                    viewModel.searchNotes(searchText,searchText)
                    searchRecyclerView()
                    binding.rvSearch.visibility=View.VISIBLE
                    binding.homeCancel.visibility=View.VISIBLE
                    binding.homeVoice.visibility=View.GONE
                }else{
                    binding.homeCancel.visibility=View.GONE
                    binding.homeVoice.visibility=View.VISIBLE
                    binding.searchNotFoundText.visibility=View.VISIBLE
                    binding.rvSearch.visibility=View.GONE
                    binding.searchNotFoundText.text="No recent searches"
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireActivity())

        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
            }

            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(p0: Float) {

            }

            override fun onBufferReceived(p0: ByteArray?) {

            }

            override fun onEndOfSpeech() {

            }

            override fun onError(p0: Int) {

            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val spokenText = matches[0]
                    editText.setText(spokenText)
                    viewModel.searchNotes(spokenText,spokenText)
                }
            }

            override fun onPartialResults(p0: Bundle?) {

            }

            override fun onEvent(p0: Int, p1: Bundle?) {

            }
        }

        speechRecognizer.setRecognitionListener(recognitionListener)

        binding.homeCancel.setOnClickListener {
            editText.text.clear()
        }
    }
    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Konuşun...")
        speechRecognizer.startListening(intent)
    }

    private fun stopSpeechToText() {
        speechRecognizer.stopListening()
    }

    private fun showPopupMenu(view: View) {
        menuSetting.setOnClickListener {
            dismissMenu()
            Toast.makeText(context, "This property does not work yet.", Toast.LENGTH_SHORT).show()
        }
        popupWindowSettings.isOutsideTouchable = true
        popupWindowSettings.showAsDropDown(view)
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
    private fun popupSettings(){
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customMenuView = inflater.inflate(R.layout.custom_menu_settings, null)

        popupWindowSettings = PopupWindow(
            customMenuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindowSettings.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        menuSetting = customMenuView.findViewById(R.id.menu_item_setting)
    }

    private fun dismissMenu() {
        popupWindowSettings.dismiss()
        popupWindowBottomMore.dismiss()
        popupWindowCreateSubFolder.dismiss()
    }

}
package com.imranmelikov.zamsungnotes.ui

import android.content.Context
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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.imranmelikov.zamsungnotes.MainActivity
import com.imranmelikov.zamsungnotes.R
import com.imranmelikov.zamsungnotes.adapter.ManageFolderListAdapter
import com.imranmelikov.zamsungnotes.adapter.MoveFolderAdapter
import com.imranmelikov.zamsungnotes.databinding.FragmentManageFoldersBinding
import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.model.Notes
import com.imranmelikov.zamsungnotes.mvvm.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class ManageFoldersFragment : Fragment() {
    private lateinit var binding: FragmentManageFoldersBinding
    private var isButtonChecked = false
    private lateinit var customImageButton: ImageView
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var bottomBar: AppBarLayout
    private lateinit var adapter: ManageFolderListAdapter
    private val mutableFolderList = mutableListOf<Folders>()
    private var checkRvView = false
    private var  moveCheckRvView = true
    private var checkClick = false
    private lateinit var moveAdapter: MoveFolderAdapter
    private lateinit var popupWindowCreateSubFolder: PopupWindow
    private lateinit var menuCreateSubFolder: TextView
    private var mainFolderCheck:Boolean=false
    private lateinit var viewModel: HomeViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManageFoldersBinding.inflate(inflater, container, false)
        viewModel= ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        customImageButton = binding.customImageButton
        bottomBar = binding.bottomBar
        binding.manageBack.setOnClickListener {
            (activity as MainActivity).updateLiveData()
            (activity as MainActivity).notifySetdatachanged()
            findNavController().popBackStack()
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        }
        recyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        moveAdapter= MoveFolderAdapter(requireActivity(), viewModel, emptyList())
        viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {
            moveAdapter.updateData(it)
        })
        clicks()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun clicks(){
        viewModel.getNotes()
        viewModel.getFolders()
        adapter.popupItemClickFolder()
        popupCreateSubFolder()
        checkSelectAll()
        clickManageEdit()
        clickManageLinear()
        onBackPresses()
        checkClick()
        scrollTLF()
        clickMainFolder()
        clickBottomButtons()
    }

    private fun clickBottomButtons(){
        binding.moveLinear.setOnClickListener {
            showBottomMoveAlertDialog(requireActivity())
        }
        binding.createLinear.setOnClickListener {
            mutableFolderList.map {
                adapter.showCreateFolderAlertDialog(it)
            }
        }
        binding.folderColorLinear.setOnClickListener {
            showFolderColorAlertDialog(requireActivity())
        }
        binding.deleteLinear.setOnClickListener {
            showBottomDeleteAlertDialog(requireActivity())
        }
        binding.renameLinear.setOnClickListener {
            showRenameFolderAlertDialog(requireActivity())
        }
    }
    private fun clickMainFolder(){
        binding.mainFolder.setOnLongClickListener {
            if (customImageButton.visibility==View.GONE){

            }
            true
        }
        binding.mainFolder.setOnClickListener {
            if (customImageButton.visibility==View.GONE){
                showPopupMenuCreateSubFolder(it)
            }
        }

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
                val mainFolderList=adapter.folderList.filter {!it.trash&& it.parentFolderId==-1 }
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
                            val folder=Folders(searchText,"Gray",-1,false,"","",false,false)
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
                        (activity as MainActivity).notifySetdatachanged()
                        (activity as MainActivity).updateLiveData()
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
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        alertDialog.show()
    }

    private fun moveRvClickCheck(imageLinear: LinearLayout, folderImage: ImageView, moveRvManage: RecyclerView){
        imageLinear.setOnClickListener {
            if (moveCheckRvView) {
                folderImage.setImageResource(R.drawable.baseline_keyboard_arrow_right_24)
                moveCheckRvView = false
                moveRvManage.visibility = View.GONE
            } else {
                folderImage.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
                moveCheckRvView = true
                moveRvManage.visibility = View.VISIBLE
            }
        }
    }
    private fun moveRecyclerView(moveRecyclerView: RecyclerView, alertDialog: AlertDialog){
        moveRecyclerView.layoutManager= LinearLayoutManager(requireActivity())
        moveRecyclerView.adapter=moveAdapter

        viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {allFolders->
//            val filteredFolder=foldersAdapter.folderList.filter { !it.selected }
            val filteredFolderSelected=adapter.folderList.filter { it.selected }

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
            moveCheckRvView=true
        }
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

        if (mutableFolderList.isNotEmpty()) {
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
                    }
                    alertDialog.dismiss()
                    moveAlertDialog.dismiss()
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
                    }
                    alertDialog.dismiss()
                    moveAlertDialog.dismiss()
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
            }
            alertDialog.dismiss()
        }
        red.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Red",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
            }
            alertDialog.dismiss()
        }
        orange.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Orange",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
            }
            alertDialog.dismiss()
        }
        yellow.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Yellow",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
            }
            alertDialog.dismiss()
        }
        openGreen.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"OpenGreen",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
            }
            alertDialog.dismiss()
        }
        green.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Green",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
            }
            alertDialog.dismiss()
        }
        openBlue1.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"OpenBlue1",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
            }
            alertDialog.dismiss()
        }
        openRed.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"OpenRed",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
            }
            alertDialog.dismiss()
        }
        pink.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Pink",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
            }
            alertDialog.dismiss()
        }
        openBlue2.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"OpenBlue2",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
            }
            alertDialog.dismiss()
        }
        blue.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Blue",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
            }
            alertDialog.dismiss()
        }
        brown.setOnClickListener {
            mutableFolderList.map {
                val updatedFolders=Folders(it.folderName,"Brown",it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,it.selectedTransparent)
                updatedFolders.id=it.id
                viewModel.updateFolders(updatedFolders)
            }
            if (binding.customImageButton.visibility == View.VISIBLE) {
                receiveCommon()
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
                mutableFolderList.map { selectedFolder ->
                    if (adapter.folderList.any { it.folderName == searchText && it.parentFolderId ==selectedFolder.parentFolderId }) {
                        renameButton.isEnabled = false
                        val lineColor = ContextCompat.getColor(requireActivity(), R.color.red)
                        val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                        editTextFolderName.background.colorFilter = colorFilter
                        errorText.visibility = View.VISIBLE
                    } else {
                        renameButton.isEnabled = true
                        renameButton.setOnClickListener {
                            mutableFolderList.map {
                                val updatedFolder=Folders(searchText,it.color,it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,false,false)
                                updatedFolder.id=it.id
                                viewModel.updateFolders(updatedFolder)
                            }
                            if (binding.customImageButton.visibility == View.VISIBLE) {
                                receiveCommon()
                            }
                            editTextFolderName.clearFocus()
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(editTextFolderName.windowToken, 0)
                            alertDialog.dismiss()
                        }
                        val lineColor = ContextCompat.getColor(requireActivity(), R.color.black)
                        val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                        editTextFolderName.background.colorFilter = colorFilter
                        errorText.visibility = View.GONE
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        alertDialog.show()
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
        if ( mutableFolderList.isNotEmpty()&&mutableFolderList.any { !it.trash }) {
            if (mutableFolderList.size > 1) {
                trashEmptyText.text = "Move ${mutableFolderList.size} folders to the Trash?"
            } else {
                trashEmptyText.text = "Move ${mutableFolderList.size} folder to the Trash?"
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
                            val updatedNotes= Notes(notes.title,notes.text,notes.lock,notes.createDate,notes.favStar,notes.imgUrl,notes.pdf,notes.imgScan,notes.audio,notes.voice,notes.parentId,true,formatDate,formatDate,false)
                            updatedNotes.id=notes.id
                            viewModel.updateNotes(updatedNotes)
                            viewModel.trashTime(formatDate,formatDate,notes)
                        }
                    })
                }
                if (binding.customImageButton.visibility == View.VISIBLE) {
                    receiveCommon()
                }
                alertDialog.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun receiveCommon() {
        dismissMenu()
        adapter.dismissMenu()
        binding.manageEdit.visibility=View.VISIBLE
        binding.manageBack.visibility=View.VISIBLE
        binding.manageCreateText.setTextColor(ContextCompat.getColor(requireContext(),R.color.black))
        binding.manageTitleText.text="Manage folders"
        binding.manageAdd.setImageResource(R.drawable.baseline_add2_24)
        customImageButton.visibility=View.GONE
        binding.manageAllText.visibility=View.GONE
        binding.customImageButton.visibility = View.GONE
        isButtonChecked = false
        checkClick=false
        checkClick()
        clickCheckButton()
        customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
        adapter.clickSelectAllHide()
        mutableFolderList.clear()
        adapter.hideSelectButton()
        bottomBarVisibilityHide()
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
    private fun dismissMenu() {
        popupWindowCreateSubFolder.dismiss()
    }

    private fun checkClick() {
        if (!checkClick) {
            binding.folderImageLinear.setOnClickListener {
                if (checkRvView) {
                    binding.folderImage.setImageResource(R.drawable.baseline_keyboard_arrow_right_24)
                    checkRvView = false
                    binding.rvManage.visibility = View.GONE
                } else {
                    binding.folderImage.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
                    checkRvView = true
                    binding.rvManage.visibility = View.VISIBLE
                }
            }
        } else {
            binding.folderImageLinear.setOnClickListener {

            }
        }
    }
    private fun buttonCreateRename(){
        if (mutableFolderList.size>1){
            binding.createLinear.visibility=View.GONE
            binding.renameLinear.visibility=View.GONE
            binding.folderColorText.text="Folder color"
        }else{
            binding.createLinear.visibility=View.VISIBLE
            binding.renameLinear.visibility=View.VISIBLE
            binding.folderColorText.text="Folder c..."
        }
    }

    private fun recyclerView() {
        adapter= ManageFolderListAdapter(requireActivity(),viewModel, emptyList())
        viewModel.notesLiveData.observe(viewLifecycleOwner, Observer {
            adapter.updateData(it)
        })
        binding.rvManage.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvManage.adapter = adapter
        adapter.onItemClickFirstItem = {
            if (it) {
                bottomBarVisibilityShow()
            } else {
                mutableFolderList.clear()
                bottomBarVisibilityHide()
                isButtonChecked = false
                clickCheckButton()
                adapter.clickSelectAllHide()
                customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
            }
            binding.deleteText.text="Delete"
        }
        adapter.onItemClickSendNotes = {
            mutableFolderList.add(it)
            binding.manageTitleText.text = "${mutableFolderList.size} selected"
            buttonCreateRename()
        }

        adapter.onItemClickSendNotesRm={
            mutableFolderList.remove(it)
            if (mutableFolderList.isEmpty()){
                binding.manageTitleText.text= "Select notes"
            }else{
                buttonCreateRename()
                binding.manageTitleText.text = "${mutableFolderList.size} selected"
            }
        }
        adapter.onItemClickCheckAll={
            if (it){
                isButtonChecked = true
                clickCheckButton()
                adapter.clickSelectAllShow()
                customImageButton.setImageResource(R.drawable.baseline_check_circle_24)
                binding.deleteText.text="Delete all"
            }else{
                isButtonChecked = false
                clickCheckButton()
                customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
                binding.deleteText.text="Delete"
            }
        }
        viewModel.folderLiveData.observe(viewLifecycleOwner, Observer {list->
            val notTrashList=list.filter { !it.trash }
            adapter.folderList=notTrashList
            val mainFolderParentId =notTrashList.filter { it.parentFolderId==-1 }
            binding.mainFolderCount.text=mainFolderParentId.size.toString()
        })
    }

    private fun clickManageLinear(){
        binding.manageLinear.setOnClickListener {
            showCreateFolderAlertDialog(requireActivity())
        }
    }
    private fun clickManageEdit(){
        binding.manageEdit.setOnClickListener {
            binding.manageEdit.visibility=View.GONE
            binding.manageBack.visibility=View.GONE
            binding.manageCreateText.setTextColor(ContextCompat.getColor(requireContext(),R.color.gray))
            binding.manageTitleText.text="Select folder"
            binding.manageAdd.setImageResource(R.drawable.baseline_add_24)
            customImageButton.visibility=View.VISIBLE
            binding.manageAllText.visibility=View.VISIBLE
            binding.rvManage.visibility=View.VISIBLE
            checkClick=true
            checkRvView=true
            binding.folderImage.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
            checkClick()
            adapter.showSelectButton()
        }
    }
    private fun selectAllTrue(){
        isButtonChecked = false
        adapter.clickSelectAllHide()
        customImageButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
        bottomBarVisibilityHide()
        mutableFolderList.clear()
        binding.manageTitleText.text="Select folders"
        buttonCreateRename()
        binding.deleteText.text="Delete"
    }
    private fun selectAllFalse(){
        mutableFolderList.clear()
        mutableFolderList.addAll(adapter.folderList)
        binding.manageTitleText.text = "${mutableFolderList.size} selected"
        isButtonChecked = true
        adapter.clickSelectAllShow()
        customImageButton.setImageResource(R.drawable.baseline_check_circle_24)
        bottomBarVisibilityShow()
        buttonCreateRename()
        binding.deleteText.text="Delete all"
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
    private fun scrollTLF() {
        nestedScrollView = binding.nestedScrollView
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
    private fun onBackPresses(){
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                if (binding.manageAllText.visibility == View.VISIBLE) {
                    receiveCommon()
                    true
                } else {
                    (activity as MainActivity).updateLiveData()
                    (activity as MainActivity).notifySetdatachanged()
                    dismissMenu()
                    adapter.dismissMenu()
                    val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    false
                }
            } else {
                (activity as MainActivity).updateLiveData()
                (activity as MainActivity).notifySetdatachanged()
                dismissMenu()
                adapter.dismissMenu()
                val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                false
            }
        }
    }
}
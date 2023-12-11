package com.imranmelikov.zamsungnotes.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.imranmelikov.zamsungnotes.R
import com.imranmelikov.zamsungnotes.databinding.FolderListManageBinding
import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.model.Notes
import com.imranmelikov.zamsungnotes.mvvm.HomeViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ManageFolderListAdapter(private val context: Context, private val viewModel: HomeViewModel, private var notesList: List<Notes>):
    RecyclerView.Adapter<ManageFolderListAdapter.ManageFolderListViewHolder>() {
    class ManageFolderListViewHolder(var binding: FolderListManageBinding): RecyclerView.ViewHolder(binding.root)

    private var clickEdit=true
    private val itemSelectedList= mutableListOf<Int>()
    private  var clickSelectAll=true
    private var checkPosition=false
    var onItemClickFirstItem:((Boolean)->Unit)?=null
    var onItemClickSendNotes:((Folders)->Unit)?=null
    var onItemClickSendNotesRm:((Folders)->Unit)?=null
    var onItemClickCheckAll:((Boolean)->Unit)?=null
    private lateinit var popupWindowCreateSubFolder: PopupWindow
    private lateinit var menuCreateSubFolder: TextView
    private lateinit var menuRenameFolder: TextView
    private lateinit var menuDeleteFolder: TextView
    private lateinit var menuColorFolder: TextView

    private val diffUtil= object: DiffUtil.ItemCallback<Folders>(){
        override fun areItemsTheSame(oldItem: Folders, newItem: Folders): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: Folders, newItem: Folders): Boolean {
            return oldItem==newItem
        }
    }
    private val recyclerDiffer= AsyncListDiffer(this,diffUtil)
    var folderList:List<Folders>
        get()=recyclerDiffer.currentList
        set(value) = recyclerDiffer.submitList(value)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageFolderListViewHolder {
        val binding=FolderListManageBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ManageFolderListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return folderList.size
    }

    override fun onBindViewHolder(holder: ManageFolderListViewHolder, position: Int) {
        val folderArrayList=folderList.get(position)
        holder.binding.folderName.text=folderArrayList.folderName
        popupItemClickFolder()
        val folderParentId=folderList.filter { it.parentFolderId== folderArrayList.id}
        val notesParentId=notesList.filter { it.parentId==folderArrayList.id&&!it.trash }
        if (folderParentId.size+notesParentId.size==0){
            holder.binding.itemCount.text=""
        }else{
            holder.binding.itemCount.text=(folderParentId.size + notesParentId.size).toString()
        }
        if (clickEdit){
            // If in edit mode, hide select buttons
            holder.binding.selectButton.visibility= View.GONE

            // Reset selected state for all items
            folderList.map { it.selected=false }

            // Block itemView to prevent unintended clicks during edit mode
            holder.itemView.setOnClickListener {
                showPopupMenuItemClickFolder(it,folderArrayList)
            }
        }else{
//            holder.itemView.setOnLongClickListener { true }
            // If not in edit mode, handle item clicks for checking and unchecking
            clickItems(holder, folderArrayList, position)

            // Handle checking and unchecking all items
            checkAllItems(holder)
        }
    }

   fun popupItemClickFolder() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customMenuView = inflater.inflate(R.layout.custom_folder_transactions, null)

        popupWindowCreateSubFolder = PopupWindow(
            customMenuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindowCreateSubFolder.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        menuCreateSubFolder = customMenuView.findViewById(R.id.folder_create_subfolder)
        menuColorFolder=customMenuView.findViewById(R.id.folder_color)
        menuDeleteFolder=customMenuView.findViewById(R.id.folder_delete)
        menuRenameFolder=customMenuView.findViewById(R.id.folder_rename)
    }

    private fun showPopupMenuItemClickFolder(view: View, folderArraylist: Folders) {
        menuCreateSubFolder.setOnClickListener {
            showCreateFolderAlertDialog(folderArraylist)
            dismissMenu()
        }
        menuRenameFolder.setOnClickListener {
            showRenameFolderAlertDialog(folderArraylist)
            dismissMenu()
        }
        menuDeleteFolder.setOnClickListener {
            showBottomDeleteAlertDialog(folderArraylist)
            dismissMenu()
        }
        menuColorFolder.setOnClickListener {
            showFolderColorAlertDialog(folderArraylist)
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

    private fun showBottomDeleteAlertDialog(folderArraylist: Folders) {
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

        trashEmptyText.text = "Move 1 folder to the Trash?"
        deleteButton.setOnClickListener {
            val currentDate= LocalDateTime.now()
            val formatter= DateTimeFormatter.ofPattern("dd")
            val formatDate=currentDate.format(formatter)
            val updatedFolders=Folders(folderArraylist.folderName,folderArraylist.color,folderArraylist.parentFolderId,true,formatDate,formatDate,false,false)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            viewModel.trashTimeFolders(formatDate,formatDate,folderArraylist)
            val trashNotes=notesList.filter { notesFilter->
                notesFilter.parentId==folderArraylist.id
            }
            trashNotes.map { notes->
                val updatedNotes= Notes(notes.title,notes.text,notes.lock,notes.createDate,notes.favStar,notes.imgUrl,notes.pdf,notes.imgScan,notes.audio,notes.voice,notes.parentId,true,formatDate,formatDate,false)
                updatedNotes.id=notes.id
                viewModel.updateNotes(updatedNotes)
                viewModel.trashTime(formatDate,formatDate,notes)
            }
            alertDialog.dismiss()
        }
        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    fun showCreateFolderAlertDialog(folderArraylist: Folders) {
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
                val childFolderList=folderList.filter { folderArraylist.id==it.parentFolderId }
                if (childFolderList.any { it.folderName == searchText}&&searchText.isNotEmpty()) {
                    addButton.isEnabled = false
                    val lineColor = ContextCompat.getColor(context, R.color.red)
                    val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                    editTextFolderName.background.colorFilter = colorFilter
                    errorText.visibility = View.VISIBLE
                } else if (searchText.isNotEmpty()){
                    addButton.isEnabled = true
                    addButton.setOnClickListener {
                        if (colorSelected==red){
                            val folder=Folders(searchText,"Red",folderArraylist.id!!,false,"","",false,false)
                            viewModel.insertFolders(folder)
                        }else if (colorSelected==gray){
                            val folder=Folders(searchText,"Gray",folderArraylist.id!!,false,"","",false,false)
                            viewModel.insertFolders(folder)
                        }else if (colorSelected==green){
                            val folder=Folders(searchText,"Green",folderArraylist.id!!,false,"","",false,false)
                            viewModel.insertFolders(folder)
                        } else if (colorSelected==yellow){
                            val folder=Folders(searchText,"Yellow",folderArraylist.id!!,false,"","",false,false)
                            viewModel.insertFolders(folder)
                        }else if (colorSelected==blue){
                            val folder=Folders(searchText,"Blue",folderArraylist.id!!,false,"","",false,false)
                            viewModel.insertFolders(folder)
                        }
                        else if (colorSelected==openBlue1){
                            val folder=Folders(searchText,"OpenBlue1",folderArraylist.id!!,false,"","",false,false)
                            viewModel.insertFolders(folder)
                        }else if (colorSelected==openBlue2){
                            val folder=Folders(searchText,"OpenBlue2",folderArraylist.id!!,false,"","",false,false)
                            viewModel.insertFolders(folder)
                        }
                        else if (colorSelected==openGreen){
                            val folder=Folders(searchText,"OpenGreen",folderArraylist.id!!,false,"","",false,false)
                            viewModel.insertFolders(folder)
                        }else if (colorSelected==openRed){
                            val folder=Folders(searchText,"OpenRed",folderArraylist.id!!,false,"","",false,false)
                            viewModel.insertFolders(folder)
                        }
                        else if (colorSelected==pink){
                            val folder=Folders(searchText,"Pink",folderArraylist.id!!,false,"","",false,false)
                            viewModel.insertFolders(folder)
                        }else if (colorSelected==brown){
                            val folder=Folders(searchText,"Brown",folderArraylist.id!!,false,"","",false,false)
                            viewModel.insertFolders(folder)
                        }else if (colorSelected==orange){
                            val folder=Folders(searchText,"Orange",folderArraylist.id!!,false,"","",false,false)
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
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        alertDialog.show()
    }
    private fun showRenameFolderAlertDialog(folderArraylist: Folders) {
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
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editTextFolderName, InputMethodManager.SHOW_FORCED)
        }, 250)


        editTextFolderName.setText(folderArraylist.folderName)

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
                if (folderList.any { it.folderName == searchText&&it.parentFolderId==folderArraylist.parentFolderId }) {
                    renameButton.isEnabled = false
                    val lineColor = ContextCompat.getColor(context, R.color.red)
                    val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                    editTextFolderName.background.colorFilter = colorFilter
                    errorText.visibility = View.VISIBLE
                } else {
                    renameButton.isEnabled = true
                    renameButton.setOnClickListener {
                        val updatedFolder=Folders(searchText,folderArraylist.color,folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,false)
                        updatedFolder.id=folderArraylist.id
                        viewModel.updateFolders(updatedFolder)
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
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        alertDialog.show()
    }

    private fun showFolderColorAlertDialog(folderArraylist: Folders) {
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
            val updatedFolders=Folders(folderArraylist.folderName,"Gray",folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,folderArraylist.selectedTransparent)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            alertDialog.dismiss()
        }
        red.setOnClickListener {
            val updatedFolders=Folders(folderArraylist.folderName,"Red",folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,folderArraylist.selectedTransparent)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            alertDialog.dismiss()
        }
        orange.setOnClickListener {
            val updatedFolders=Folders(folderArraylist.folderName,"Orange",folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,folderArraylist.selectedTransparent)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            alertDialog.dismiss()
        }
        yellow.setOnClickListener {
            val updatedFolders=Folders(folderArraylist.folderName,"Yellow",folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,folderArraylist.selectedTransparent)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            alertDialog.dismiss()
        }
        openGreen.setOnClickListener {
            val updatedFolders=Folders(folderArraylist.folderName,"OpenGreen",folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,folderArraylist.selectedTransparent)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            alertDialog.dismiss()
        }
        green.setOnClickListener {
            val updatedFolders=Folders(folderArraylist.folderName,"Green",folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,folderArraylist.selectedTransparent)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            alertDialog.dismiss()
        }
        openBlue1.setOnClickListener {
            val updatedFolders= Folders(folderArraylist.folderName,"OpenBlue1",folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,folderArraylist.selectedTransparent)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            alertDialog.dismiss()
        }
        openRed.setOnClickListener {
            val updatedFolders=Folders(folderArraylist.folderName,"OpenRed",folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,folderArraylist.selectedTransparent)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            alertDialog.dismiss()
        }
        pink.setOnClickListener {
            val updatedFolders=Folders(folderArraylist.folderName,"Pink",folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,folderArraylist.selectedTransparent)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            alertDialog.dismiss()
        }
        openBlue2.setOnClickListener {
            val updatedFolders=Folders(folderArraylist.folderName,"OpenBlue2",folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,folderArraylist.selectedTransparent)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            alertDialog.dismiss()
        }
        blue.setOnClickListener {
            val updatedFolders=Folders(folderArraylist.folderName,"Blue",folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,folderArraylist.selectedTransparent)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            alertDialog.dismiss()
        }
        brown.setOnClickListener {
            val updatedFolders=Folders(folderArraylist.folderName,"Brown",folderArraylist.parentFolderId,folderArraylist.trash,folderArraylist.trashTime,folderArraylist.trashStartTime,false,folderArraylist.selectedTransparent)
            updatedFolders.id=folderArraylist.id
            viewModel.updateFolders(updatedFolders)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
    fun dismissMenu() {
        popupWindowCreateSubFolder.dismiss()
    }

    private fun checkAllItems(holder: ManageFolderListAdapter.ManageFolderListViewHolder){
        // Update the check state based on the clickSelectAll flag
        if (clickSelectAll){
            holder.binding.selectButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
        }else{
            holder.binding.selectButton.setImageResource(R.drawable.baseline_check_circle_24)
            // Ensure not to add extra items to the selected list
            if (!checkPosition){
                itemSelectedList.clear()
                itemSelectedList.addAll(folderList.indices)
                checkPosition=true
            }
        }
    }
    private fun clickItems(holder: ManageFolderListAdapter.ManageFolderListViewHolder, folderArraylist: Folders, position: Int){
        // Show select button and handle item clicks for checking and unchecking
        holder.binding.selectButton.visibility= View.VISIBLE
        fun clickItem(){
            if (!folderArraylist.selected){
                holder.itemView.setOnClickListener {
                    selectItem(holder, folderArraylist, position)
                    clickItem()
                }
            }else{
                holder.itemView.setOnClickListener {
                    unSelectItem(holder, folderArraylist, position)
                    clickItem()
                }
            }
        }
        clickItem()
    }
    private fun selectItem(holder: ManageFolderListAdapter.ManageFolderListViewHolder, folderArraylist: Folders, position: Int) {
        // Add the selected item to the list, update UI, and trigger callbacks
        itemSelectedList.add(position)
        holder.binding.selectButton.setImageResource(R.drawable.baseline_check_circle_24)
        folderArraylist.selected=true
        onItemClickFirstItem?.let {
            it(true)
        }
        onItemClickSendNotes?.let {
            it(folderArraylist)
        }
        if (itemSelectedList.size==folderList.size){
            onItemClickCheckAll?.let {
                it(true)
            }
        }
    }
    private fun unSelectItem(holder: ManageFolderListAdapter.ManageFolderListViewHolder, folderArraylist: Folders, position: Int){
        // Remove the unselected item from the list, update UI, and trigger callbacks
        if (itemSelectedList.contains(position)){
            itemSelectedList.remove(position)
            onItemClickSendNotesRm?.let {
                it(folderArraylist)
            }
            if (itemSelectedList.isEmpty()){
                onItemClickFirstItem?.let {
                    it(false)
                    checkPosition=false
                }
            }else{
                if (itemSelectedList.size!=folderList.size){
                    onItemClickCheckAll?.let {
                        it(false)
                    }
                }
            }
        }else{
        }
        folderArraylist.selected=false
        holder.binding.selectButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
    }

    // Method to show select buttons and enter edit mode
    fun showSelectButton(){
        clickEdit=false
        notifyDataSetChanged()
    }

    // Method to hide select buttons and exit edit mode
    fun hideSelectButton(){
        clickEdit=true
        notifyDataSetChanged()
    }

    // Method to trigger select all items and update UI
    fun clickSelectAllShow(){
        clickSelectAll=false
        folderList.map{it.selected=true}
        checkPosition=false
        itemSelectedList.clear()
        notifyDataSetChanged()
    }

    // Method to trigger unselect all items and update UI
    fun clickSelectAllHide(){
        clickSelectAll=true
        folderList.map {it.selected=false }
        checkPosition=false
        itemSelectedList.clear()
        notifyDataSetChanged()
    }
    fun updateData(newData: List<Notes>) {
        notesList = newData
        notifyDataSetChanged()
    }
}
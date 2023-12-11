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


class MoveFolderAdapter(val context: Context, private val viewModel: HomeViewModel, private var notesList: List<Notes>): RecyclerView.Adapter<MoveFolderAdapter.MoveFolderViewHolder>() {
    class MoveFolderViewHolder(var binding:FolderListManageBinding): RecyclerView.ViewHolder(binding.root)

    var onItemClickListener:((Folders)->Unit)?=null
    private lateinit var popupWindowCreateSubFolder: PopupWindow
    private lateinit var menuCreateSubFolder: TextView

    private val diffUtil= object: DiffUtil.ItemCallback<Folders>(){
        override fun areItemsTheSame(oldItem: Folders, newItem: Folders): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: Folders, newItem: Folders): Boolean {
            return oldItem==newItem
        }
    }
    private val recyclerDiffer= AsyncListDiffer(this,diffUtil)
    var moveFolderList:List<Folders>
        get()=recyclerDiffer.currentList
        set(value) = recyclerDiffer.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoveFolderViewHolder {
        val binding=
            FolderListManageBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MoveFolderViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return moveFolderList.size
    }

    override fun onBindViewHolder(holder: MoveFolderViewHolder, position: Int) {
        val folderList=moveFolderList.get(position)
        holder.binding.folderName.text=folderList.folderName
        val folderParentId=moveFolderList.filter { it.parentFolderId== folderList.id}
        val notesParentId=notesList.filter { it.parentId==folderList.id&&!it.trash }
        if (folderParentId.size+notesParentId.size==0){
            holder.binding.itemCount.text=""
        }else{
            holder.binding.itemCount.text=(folderParentId.size + notesParentId.size).toString()
        }
        popupCreateFolder()
        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(folderList)
            }
        }
        holder.itemView.setOnLongClickListener {
            showPopupMenuCreateFolder(it,folderList)
            true
        }

    }
    private fun popupCreateFolder() {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customMenuView = inflater.inflate(R.layout.custom_menu_create_subfolder, null)

        popupWindowCreateSubFolder = PopupWindow(
            customMenuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindowCreateSubFolder.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        menuCreateSubFolder = customMenuView.findViewById(R.id.menu_item_create_subfolder)
    }

    private fun showPopupMenuCreateFolder(view: View, folderArraylist: Folders) {
        menuCreateSubFolder.setOnClickListener {
            showCreateFolderAlertDialog(folderArraylist)
            popupWindowCreateSubFolder.dismiss()
        }
        popupWindowCreateSubFolder.isOutsideTouchable = true
        popupWindowCreateSubFolder.animationStyle = R.style.PopupAnimation
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val xOffset = location[0]
        val yOffset = location[1] - view.height
        popupWindowCreateSubFolder.showAtLocation(view, Gravity.NO_GRAVITY, xOffset, yOffset)
    }

    private fun showCreateFolderAlertDialog(folderArraylist: Folders) {
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
                val childFolderList=moveFolderList.filter { folderArraylist.id==it.parentFolderId }
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
                            val folder= Folders(searchText,"Orange",folderArraylist.id!!,false,"","",false,false)
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
    fun updateData(newData: List<Notes>) {
        notesList = newData
        notifyDataSetChanged()
    }
}
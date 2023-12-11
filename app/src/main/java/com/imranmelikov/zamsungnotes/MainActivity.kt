package com.imranmelikov.zamsungnotes

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.imranmelikov.zamsungnotes.adapter.HomeFolderListAdapter
import com.imranmelikov.zamsungnotes.databinding.ActivityMainBinding
import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.mvvm.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var  navController: NavController
    private lateinit var pdfUri: Uri
    private lateinit var adapter: HomeFolderListAdapter
    private var checkRvView=false
    private lateinit var popupWindowCreateSubFolder: PopupWindow
    private lateinit var menuCreateSubFolder: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        drawerLayout = binding.drawerLayout

        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        navView.setupWithNavController(navController)

        viewModel.getFolders()
        viewModel.getNotes()

        sharedPreferences = getSharedPreferences("PreferencesMain", Context.MODE_PRIVATE)
        val receiveSharedPreferencesInt = sharedPreferences.getInt("Int", -100)
        viewModel.folderLiveData.observe(this, Observer {folderList->
            folderList.map { folders ->
              if (receiveSharedPreferencesInt ==folders.id) {
                    val data = folders.folderName
                    val dataId=folders.id
                    val bundle = Bundle()
                    sharedPreferences.edit().putInt("Int",dataId!!).apply()
                    bundle.putString("main", data)
                    bundle.putInt("Id",dataId)
                    navController.navigate(R.id.action_nav_home_self,bundle)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
        })
        if (receiveSharedPreferencesInt == -1) {
            binding.itemLinear.setBackgroundResource(R.color.transparent_black)
            val data = "All notes"
            val bundle = Bundle()
            bundle.putString("main", data)
            sharedPreferences.edit().putInt("Int", -1).apply()
            navController.navigate(R.id.action_nav_home_self, bundle)
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (receiveSharedPreferencesInt == -2) {
            binding.itemLinearFav.setBackgroundResource(R.color.transparent_black)
            val data = "Favorites"
            val bundle = Bundle()
            bundle.putString("main", data)
            sharedPreferences.edit().putInt("Int", -2).apply()
            navController.navigate(R.id.action_nav_home_self, bundle)
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (receiveSharedPreferencesInt == -3) {
            binding.itemLinearLock.setBackgroundResource(R.color.transparent_black)
            val data = "Locked notes"
            val bundle = Bundle()
            bundle.putString("main", data)
            sharedPreferences.edit().putInt("Int", -3).apply()
            navController.navigate(R.id.action_nav_home_self, bundle)
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (receiveSharedPreferencesInt == -4) {
            binding.itemLinearTrash.setBackgroundResource(R.color.transparent_black)
            val data = "Trash"
            val bundle = Bundle()
            bundle.putString("main", data)
            sharedPreferences.edit().putInt("Int", -4).apply()
            navController.navigate(R.id.action_nav_home_self, bundle)
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (receiveSharedPreferencesInt == -5) {
            binding.mainFolder.setBackgroundResource(R.color.transparent_black)
            val data = "Folders"
            val bundle = Bundle()
            bundle.putString("main", data)
            sharedPreferences.edit().putInt("Int", -5).apply()
            navController.navigate(R.id.action_nav_home_self, bundle)
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        val getIntent=intent
        if (getIntent!=null&&getIntent.hasExtra("lock")){
            binding.itemLinearLock.setBackgroundResource(R.color.transparent_black)
            val intentString=getIntent.getStringExtra("lock") as String
            val bundle = Bundle()
            bundle.putString("main", intentString)
            sharedPreferences.edit().putInt("Int",-3).apply()
            navController.navigate(R.id.action_nav_home_self,bundle)
            drawerLayout.closeDrawer(GravityCompat.START)
        }else if (getIntent!=null&&getIntent.hasExtra("All notes")){
            binding.itemLinear.setBackgroundResource(R.color.transparent_black)
            val intentString=getIntent.getStringExtra("All notes") as String
            val bundle = Bundle()
            bundle.putString("main", intentString)
            sharedPreferences.edit().putInt("Int",-1).apply()
            navController.navigate(R.id.action_nav_home_self,bundle)
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        clicks()
    }

    private fun clicks(){
        // collect all click functions
        viewModel.getNotes()
        viewModel.getFolders()
        clickFav()
        clickShared()
        clickAll()
        clickLock()
        clickTrash()
        clickManageFoldersButton()
        clickSettingButton()
        recyclerView()
        checkClick()
        clickMainFolder()
        popupCreateSubFolder()
        adapter.popupItemClickFolder()
    }
    fun checkClickTrue(){
        adapter.checkClick=false
        adapter.notifyDataSetChanged()
    }
    fun mainFolderBackground(){
        binding.mainFolder.setBackgroundResource(R.color.white)
    }
    private fun clickMainFolder(){
        binding.mainFolder.setOnLongClickListener {
            showPopupMenuCreateSubFolder(it)
            true
        }
        binding.mainFolder.setOnClickListener {
            binding.mainFolder.setBackgroundResource(R.color.transparent_black)
            binding.itemLinear.setBackgroundResource(R.color.white)
            binding.itemLinearFav.setBackgroundResource(R.color.white)
            binding.itemLinearLock.setBackgroundResource(R.color.white)
            binding.itemLinearTrash.setBackgroundResource(R.color.white)
            val data = "Folders"
            val bundle = Bundle()
            bundle.putString("main", data)
            sharedPreferences.edit().putInt("Int",-5).apply()
            navController.navigate(R.id.action_nav_home_self,bundle)
            adapter.checkClick=true
            adapter.notifyDataSetChanged()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
    private fun popupCreateSubFolder() {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
            showCreateFolderAlertDialog()
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

    private fun showCreateFolderAlertDialog() {
        val builder = AlertDialog.Builder(this, R.style.RoundedAlertDialog)
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
        params.y = this.resources.getDimensionPixelSize(R.dimen.margin20dp)


        //Make the window full screen
        val displayMetrics = this.resources.displayMetrics
        val width = displayMetrics.widthPixels
        params.width = width
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // Update window
        window.attributes = params

        editTextFolderName.requestFocus()
        editTextFolderName.postDelayed({
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editTextFolderName, InputMethodManager.SHOW_IMPLICIT)
        }, 250)

        cancelButton.setOnClickListener {
            editTextFolderName.clearFocus()
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
               val folders= adapter.folderList
                    val mainFolderList=folders.filter { !it.trash&&it.parentFolderId==-1 }
                    if (mainFolderList.any { it.folderName == searchText } && searchText.isNotEmpty()) {
                        addButton.isEnabled = false
                        val lineColor = ContextCompat.getColor(this@MainActivity, R.color.red)
                        val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                        editTextFolderName.background.colorFilter = colorFilter
                        errorText.visibility = View.VISIBLE
                    } else if (searchText.isNotEmpty()) {
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
                                val folder= Folders(searchText,"Orange",-1,false,"","",false,false)
                                viewModel.insertFolders(folder)
                            }
                            editTextFolderName.clearFocus()
                            val imm =
                                this@MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(editTextFolderName.windowToken, 0)
                            alertDialog.dismiss()
                        }
                        val lineColor = ContextCompat.getColor(this@MainActivity, R.color.black)
                        val colorFilter = PorterDuffColorFilter(lineColor, PorterDuff.Mode.SRC_IN)
                        editTextFolderName.background.colorFilter = colorFilter
                        errorText.visibility = View.GONE
                    } else {
                        addButton.isEnabled = false
                        val lineColor = ContextCompat.getColor(this@MainActivity, R.color.black)
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
    fun updateLiveData(){
        viewModel.getNotes()
        viewModel.getFolders()
    }
    fun notifySetdatachanged(){
        adapter.notifyDataSetChanged()
    }
    private fun recyclerView(){
        adapter = HomeFolderListAdapter(this, viewModel, emptyList())
        binding.folderRecyclerview.layoutManager= LinearLayoutManager(this)
        binding.folderRecyclerview.adapter=adapter
        viewModel.notesLiveData.observe(this, Observer {
            binding.allNotesCount.text=it.size.toString()
            val trashNotes=it.filter { trahsnotes-> trahsnotes.trash }
            viewModel.folderLiveData.observe(this, Observer {folder->
                val filteredFolders=folder.filter { it.trash }
                    binding.trashNotesCount.text=(trashNotes.size+filteredFolders.size).toString()
            })
            val favNotes=it.filter { it.favStar }
            val lockNotes=it.filter { it.lock }
            if (favNotes.isEmpty()){
                binding.itemLinearFav.visibility=View.GONE
            }else{
                binding.itemLinearFav.visibility=View.VISIBLE
                binding.favNotesCount.text=favNotes.size.toString()
            }
            if (lockNotes.isEmpty()){
                binding.itemLinearLock.visibility=View.GONE
            }else{
                binding.itemLinearLock.visibility=View.VISIBLE
                binding.lockedNotesCount.text=lockNotes.size.toString()
            }
           adapter.updateData(it)
        })
        viewModel.getNotes()
        viewModel.getFolders()

        adapter.onItemClickListener={
            binding.mainFolder.setBackgroundResource(R.color.white)
            binding.itemLinear.setBackgroundResource(R.color.white)
            binding.itemLinearFav.setBackgroundResource(R.color.white)
            binding.itemLinearLock.setBackgroundResource(R.color.white)
            binding.itemLinearTrash.setBackgroundResource(R.color.white)
            val data = it.folderName
            val dataId=it.id
            val bundle = Bundle()
            sharedPreferences.edit().putInt("Int",dataId!!).apply()
//            viewModel.folderLiveData.observe(this, Observer {folderList->
//                val filteredList=  folderList.filter { listFolders->
//                    listFolders.id!=it.id
//                }
//                filteredList.map {folders->
//                    val updatedFolderList=Folders(folders.folderName,folders.color,folders.parentFolderId,folders.trash,folders.trashTime,folders.trashStartTime,folders.selected,false)
//                    updatedFolderList.id=folders.id
////                    viewModel.updateFolders(updatedFolderList)
//                }
//            })
//            val updatedFolder=Folders(it.folderName,it.color,it.parentFolderId,it.trash,it.trashTime,it.trashStartTime,it.selected,true)
//            updatedFolder.id=it.id
////            viewModel.updateFolders(updatedFolder)
            bundle.putString("main", data)
            bundle.putInt("Id",dataId)
            navController.navigate(R.id.action_nav_home_self,bundle)
            drawerLayout.closeDrawer(GravityCompat.START)
            adapter.notifyDataSetChanged()
        }
        viewModel.folderLiveData.observe(this, Observer {list->
            val notTrashList=list.filter { !it.trash }
            adapter.folderList=notTrashList
            val mainFolderParentId =notTrashList.filter { it.parentFolderId==-1 }
            binding.mainFolderCount.text=mainFolderParentId.size.toString()
        })
    }
    private fun checkClick(){
        binding.folderImageLinear.setOnClickListener {
            if (checkRvView){
                binding.folderImage.setImageResource(R.drawable.baseline_keyboard_arrow_right_24)
                checkRvView=false
                binding.folderRecyclerview.visibility= View.GONE
            }else{
                binding.folderImage.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
                checkRvView=true
                binding.folderRecyclerview.visibility= View.VISIBLE
            }
        }
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // For loading Image
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK && data != null) {
                    val imageSelected = data.extras!!["data"] as Bitmap?
                }
                1 -> if (resultCode == RESULT_OK && data != null) {
                    val imageSelected = data.data
                    val pathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (imageSelected != null) {
                        val myCursor = contentResolver.query(
                            imageSelected,
                            pathColumn, null, null, null
                        )
                        // Setting the image to the ImageView
                        if (myCursor != null) {
                            myCursor.moveToFirst()
                            val columnIndex = myCursor.getColumnIndex(pathColumn[0])
                            val picturePath = myCursor.getString(columnIndex)
                            val bitmapFactory= BitmapFactory.decodeFile(picturePath)
                            myCursor.close()
                        }
                    }
                }
            }
        }
        // For loading PDF
        when (requestCode) {
            12 -> if (resultCode == RESULT_OK) {
                pdfUri = data?.data!!
                val uri: Uri = data.data!!
                val uriString: String = uri.toString()
                var pdfName: String? = null
                if (uriString.startsWith("content://")) {
                    var myCursor: Cursor? = null
                    try {
                        // Setting the PDF to the TextView
                        myCursor = applicationContext!!.contentResolver.query(uri, null, null, null, null)
                        if (myCursor != null && myCursor.moveToFirst()) {
                            pdfName = myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        }
                    } finally {
                        myCursor?.close()
                    }
                }
            }
        }
    }
    private fun clickShared(){
        binding.itemLinearShared.setOnClickListener {
            Toast.makeText(this,"This feature is in beta version.Not ready to use yet", Toast.LENGTH_SHORT).show()
        }
    }
    private fun clickFav(){
        //  send arg to homeFragment for favorites
        binding.itemLinearFav.setOnClickListener {
            binding.mainFolder.setBackgroundResource(R.color.white)
            binding.itemLinear.setBackgroundResource(R.color.white)
            binding.itemLinearFav.setBackgroundResource(R.color.transparent_black)
            binding.itemLinearLock.setBackgroundResource(R.color.white)
            binding.itemLinearTrash.setBackgroundResource(R.color.white)
            val data = "Favorites"
            val bundle = Bundle()
            bundle.putString("main", data)
            sharedPreferences.edit().putInt("Int",-2).apply()
            navController.navigate(R.id.action_nav_home_self,bundle)
            drawerLayout.closeDrawer(GravityCompat.START)
            adapter.checkClick=true
            adapter.notifyDataSetChanged()
        }
    }
    private fun clickAll(){
        //  send arg to homeFragment for allNotes
        binding.itemLinear.setOnClickListener {
            binding.mainFolder.setBackgroundResource(R.color.white)
            binding.itemLinear.setBackgroundResource(R.color.transparent_black)
            binding.itemLinearFav.setBackgroundResource(R.color.white)
            binding.itemLinearLock.setBackgroundResource(R.color.white)
            binding.itemLinearTrash.setBackgroundResource(R.color.white)
            val data = "All notes"
            val bundle = Bundle()
            bundle.putString("main", data)
            sharedPreferences.edit().putInt("Int",-1).apply()
            navController.navigate(R.id.action_nav_home_self,bundle)
            drawerLayout.closeDrawer(GravityCompat.START)
            adapter.checkClick=true
            adapter.notifyDataSetChanged()
        }
    }
    private fun clickLock(){
        //  send arg to homeFragment for lock
        binding.itemLinearLock.setOnClickListener {
            binding.mainFolder.setBackgroundResource(R.color.white)
            binding.itemLinear.setBackgroundResource(R.color.white)
            binding.itemLinearFav.setBackgroundResource(R.color.white)
            binding.itemLinearLock.setBackgroundResource(R.color.transparent_black)
            binding.itemLinearTrash.setBackgroundResource(R.color.white)
            val data = "Locked notes"
            val bundle = Bundle()
            bundle.putString("main", data)
            sharedPreferences.edit().putInt("Int",-3).apply()
            navController.navigate(R.id.action_nav_home_self,bundle)
            drawerLayout.closeDrawer(GravityCompat.START)
            adapter.checkClick=true
            adapter.notifyDataSetChanged()
        }
    }
    private fun clickTrash(){
        // send arg to homeFragment for trash
        binding.itemLinearTrash.setOnClickListener {
            binding.mainFolder.setBackgroundResource(R.color.white)
            binding.itemLinear.setBackgroundResource(R.color.white)
            binding.itemLinearFav.setBackgroundResource(R.color.white)
            binding.itemLinearLock.setBackgroundResource(R.color.white)
            binding.itemLinearTrash.setBackgroundResource(R.color.transparent_black)
            val data = "Trash"
            val bundle = Bundle()
            bundle.putString("main", data)
            sharedPreferences.edit().putInt("Int",-4).apply()
            navController.navigate(R.id.action_nav_home_self,bundle)
            adapter.checkClick=true
            adapter.notifyDataSetChanged()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
    private fun clickSettingButton(){
        // go to settings of android system regarding app
        binding.homeSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = android.net.Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }
    private fun clickManageFoldersButton(){
        // go to manageFolderFragment
        binding.manageButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START,false)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            navController.navigate(R.id.action_nav_home_to_manageFoldersFragment)
        }
    }
    fun handleButtonClick(){
        // Connection of opening a drawer by clicking on the menu icon with the activity
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        // control backPress
        if (popupWindowCreateSubFolder.isShowing){
            dismissMenu()
        }else if (adapter.popupWindowCreateSubFolder.isShowing){
            adapter.dismissMenu()
        }else{
            if (binding.drawerLayout.isOpen){
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }else{
                super.onBackPressed()
            }
        }
    }

}
package com.imranmelikov.zamsungnotes

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.imranmelikov.zamsungnotes.databinding.ActivityUploadBinding
import com.imranmelikov.zamsungnotes.mvvm.UploadViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private lateinit var popupWindowUploadMore: PopupWindow
    private lateinit var menuPageSorter: TextView
    private lateinit var menuPageTemplates: TextView
    private lateinit var menuPageSettings: TextView
    private lateinit var menuFullScreen: TextView
    private lateinit var menuInvite: TextView
    private lateinit var menuAddToShared: TextView
    private lateinit var menuTags: TextView
    private lateinit var menuSaveFile: TextView
    private lateinit var menuFav: ImageView
    private lateinit var menuShare: ImageView
    private lateinit var menuDelete: ImageView
    private lateinit var popupWindowUploadAttach: PopupWindow
    private lateinit var menuImage: CardView
    private lateinit var menuCamera: CardView
    private lateinit var menuScan: CardView
    private lateinit var menuPdf: CardView
    private lateinit var menuVoice: LinearLayout
    private lateinit var menuAudio: LinearLayout
    private lateinit var menuDraw: LinearLayout
    private lateinit var menuTextBox: LinearLayout
    private var checkFav=false
    private var checkBook=false
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var imageUri: Uri
    private var checkCameraScan=false
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionlauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncherAudio: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncherAudio: ActivityResultLauncher<String>
    private  var selectedImage: Uri?=null

    private var selectedFileAudio: Uri? = null
    private var selectedFilePath:String?=null
    private lateinit var mediaPlayer: MediaPlayer
    private var isRecording = false
    private var isRecordingPlay = false
    private var handler: Handler? = null
    private var elapsedTime = 0L
    private lateinit var viewModel: UploadViewModel

    private var audiFile: File?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel= ViewModelProvider(this@UploadActivity).get(UploadViewModel::class.java)

        binding.uploadBack.setOnClickListener {
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
            viewModel.playStop()
            viewModel.stopRecord()
            if (::mediaPlayer.isInitialized){
                mediaPlayer.stop()
                mediaPlayer.release()
            }
            finish()
        }

        binding.editText.requestFocus()
        binding.editText.postDelayed({
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editText, InputMethodManager.SHOW_IMPLICIT)
        }, 250)
        clicks()
    }
    private fun clicks(){
        clickMore()
        clickBook()
        clickAttachFile()
        popupUploadMore()
        popupUploadAttach()
        registerLauncher()
        registerLauncherAudio()
        playAudioButton()
        recordVoice()
        playVoice()
    }

    private fun checkPermissionAudio(view: View){
        val permissionName=android.Manifest.permission.READ_MEDIA_AUDIO
        if (ContextCompat.checkSelfPermission(this,permissionName)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissionName)){
                Snackbar.make(view,"Permission needed.", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                    View.OnClickListener {
                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
                        permissionLauncherAudio.launch(Manifest.permission.READ_MEDIA_AUDIO)
                    }
                }).show()
            }else{
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
                    permissionLauncherAudio.launch(Manifest.permission.READ_MEDIA_AUDIO)
                }
            }
        }else{
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            activityResultLauncherAudio.launch(intent)
        }
    }
    private fun checkPermissionGallery(view: View){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_MEDIA_IMAGES)!= PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)){
                Snackbar.make(view,"Permission needed", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                    View.OnClickListener {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionlauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    }).show()
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionlauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
        }else{
            val intentToGallery= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }
    private fun registerLauncherAudio(){
        activityResultLauncherAudio = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult=result.data
                    if (intentFromResult!=null){
                        val uri = result.data?.data
                        uri?.let {
                            selectedFileAudio=uri
                            binding.audioRelative.visibility= View.VISIBLE
                            selectedFilePath=it.path
                            showSelectedFile(it)
                        }
                    }
                }
            })
        permissionLauncherAudio=registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            ActivityResultCallback {
                if (it){
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "audio/*"
                    activityResultLauncherAudio.launch(intent)
                }else{
                    Toast.makeText(this,"Permission needed", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun registerLauncher(){
        activityResultLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {result->
                if(result.resultCode== RESULT_OK){
                    val intentFromResult=result.data
                    if(intentFromResult!=null){
                        selectedImage=intentFromResult.data
                        selectedImage.let {
                            binding.uploadImage.setImageURI(it)
                            binding.uploadImage.visibility= View.VISIBLE
                            it?.let { uri->
                                selectedFilePath=uri.path
                            }
                        }
                    }
                }
            })
        permissionlauncher=registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            ActivityResultCallback {
                if(it){
                    val intentToGallery= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                }else{
                    Toast.makeText(this,"Permission needed", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun checkPermissions() {
        val permissionName = android.Manifest.permission.CAMERA

        if (ContextCompat.checkSelfPermission(this, permissionName) != PackageManager.PERMISSION_GRANTED) {
            // User has not granted the permission or denied it before
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionName)) {
                // Additional information or rationale can be provided to the user here
                // However, the fundamental request for permission is made
                ActivityCompat.requestPermissions(this, arrayOf(permissionName), REQUEST_IMAGE_CAPTURE)
            } else {
                // First-time permission request
                ActivityCompat.requestPermissions(this, arrayOf(permissionName), REQUEST_IMAGE_CAPTURE)
            }
        } else {
            // Permission is already granted
            dispatchTakePictureIntent()
        }
    }
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            null
        }
        photoFile?.also {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (checkCameraScan){
                binding.uploadImage.setImageURI(imageUri)
                binding.uploadImage.visibility= View.VISIBLE
            }else{
                binding.uploadImageBig.setImageURI(imageUri)
                binding.uploadImageBig.visibility= View.VISIBLE
            }

        }
    }
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, "JPEG_${timeStamp}.jpg")
        imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)

        return imageFile
    }



    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode==REQUEST_IMAGE_CAPTURE){
            if (grantResults.size>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent()
            }else{
                Toast.makeText(this,"Permission denied.", Toast.LENGTH_SHORT).show()
            }
        }else if (requestCode==200){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isRecording=true
                File(cacheDir,"audio.mp3").also {
                    viewModel.startRecord(it)
                    audiFile=it
                }
                handler = Handler(Looper.getMainLooper())
                handler?.postDelayed(object : Runnable {
                    override fun run() {
                        if (isRecording) {
                            elapsedTime += 1000
                            updateElapsedTime()
                            handler?.postDelayed(this, 1000)
                        }
                    }
                }, 1000)
                binding.voiceRelative.visibility= View.VISIBLE
                binding.voiceRecordStopButton.visibility= View.VISIBLE
                binding.voiceRecordButton.visibility= View.GONE
                binding.voicePlayButton.visibility= View.GONE
            } else {
                Toast.makeText(this, "Permission denied for recording.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showSelectedFile(uri: Uri) {
        uri.let {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            try {
                mediaMetadataRetriever.setDataSource(this, it)

                val durationInMillis = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()

                val minutes = (durationInMillis ?: 0) / 1000 / 60
                val seconds = ((durationInMillis ?: 0) / 1000) % 60

                val formattedDuration = String.format("%02d:%02d", minutes, seconds)
                binding.textViewDuration.text = formattedDuration
                binding.textViewFileInfo.text=selectedFilePath


            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("AudioInfo", "Hata: ${e.message}")
            } finally {
                // MediaMetadataRetriever'ı serbest bırak
                mediaMetadataRetriever.release()
            }
        }

    }

//    private fun getFileName(uri: Uri): String {
//        var result: String? = null
//        if (uri.scheme == "content") {
//            val cursor = contentResolver.query(uri, null, null, null, null)
//            try {
//                if (cursor != null && cursor.moveToFirst()) {
//                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
//                }
//            } finally {
//                cursor?.close()
//            }
//        }
//        if (result == null) {
//            result = uri.path
//            val cut = result?.lastIndexOf('/')
//            if (cut != -1) {
//                result = result?.substring(cut!! + 1)
//            }
//        }
//        return result ?: "Bilinmeyen Dosya"
//    }

    override fun onDestroy() {
        if(this::mediaPlayer.isInitialized){
            mediaPlayer.stop()
            mediaPlayer.release()
            viewModel.playStop()
            viewModel.stopRecord()
        }
        super.onDestroy()
    }

    //    private fun playSelectedFile() {
//        if (!this::mediaPlayer.isInitialized){
//            mediaPlayer=MediaPlayer.create(this,selectedFileAudio)
//        }
//        if (mediaPlayer.isPlaying){
//            mediaPlayer.pause()
//            mediaPlayer.seekTo(0)
//            binding.playButton.setImageResource(R.drawable.baseline_play_arrow_24)
//        }else{
//            binding.playButton.setImageResource(R.drawable.baseline_stop_24)
//            mediaPlayer.start()
//        }
//    }
    private fun playSelectedFile() {
        try {
            if (!this::mediaPlayer.isInitialized) {
                mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(this, selectedFileAudio!!)
                mediaPlayer.prepare()
            }

            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                mediaPlayer.seekTo(0)
                binding.playButton.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                binding.playButton.setImageResource(R.drawable.baseline_stop_24)
                mediaPlayer.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MediaPlayer", "Error: ${e.message}")
        }
    }

    private fun updateElapsedTime() {
        val seconds = elapsedTime / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val timeString = String.format("%02d:%02d", minutes, remainingSeconds)
        binding.voiceTextViewDuration.text = timeString
    }
    private fun recordVoice(){
        binding.voiceRecordStopButton.setOnClickListener {
            if (isRecording){
                isRecording=false
                binding.voiceRecordButton.visibility= View.VISIBLE
                binding.voiceRecordStopButton.visibility= View.GONE
                viewModel.stopRecord()
                handler?.removeCallbacksAndMessages(null)
                updateElapsedTime()
                binding.voicePlayButton.visibility= View.VISIBLE
                binding.voicePlayButton.setImageResource(R.drawable.baseline_play_arrow_24)
            }
        }
        binding.voiceRecordButton.setOnClickListener {
            if (!isRecording){
                isRecording=true
                File(cacheDir,"audio.mp3").also {
                    viewModel.startRecord(it)
                    audiFile=it
                }
                handler?.postDelayed(object : Runnable {
                    override fun run() {
                        if (isRecording) {
                            elapsedTime += 1000
                            updateElapsedTime()
                            handler?.postDelayed(this, 1000)
                        }
                    }
                }, 1000)
                binding.voiceRecordStopButton.visibility= View.VISIBLE
                binding.voiceRecordButton.visibility= View.GONE
                binding.voicePlayButton.visibility= View.GONE
                binding.voicePlayButton.setImageResource(R.drawable.baseline_stop_24)
            }
        }
    }

    private fun playVoice(){
        binding.voicePlayButton.setOnClickListener {
            if (isRecordingPlay){
                isRecordingPlay=false
                binding.voicePlayButton.setImageResource(R.drawable.baseline_play_arrow_24)
                viewModel.playStop()
            }else{
                isRecordingPlay=true
                binding.voicePlayButton.setImageResource(R.drawable.baseline_stop_24)
                viewModel.playFile(audiFile?:return@setOnClickListener)
            }
        }
    }
    private fun checkPermissionVoice(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),200)
    }
    private fun playAudioButton(){
        binding.playButton.setOnClickListener {
            playSelectedFile()
        }
    }
    private fun dismissMenu() {
        popupWindowUploadMore.dismiss()
        popupWindowUploadAttach.dismiss()
    }

    private fun clickMore(){
        binding.uploadMore.setOnClickListener {
            showPopupUploadMore(it)
        }
    }
    private fun clickAttachFile(){
        binding.uploadAttach.setOnClickListener {
            showPopupUploadAttach(it)
        }
    }
    private fun clickBook(){
        binding.uploadBook.setOnClickListener {
            if (checkBook){
                checkBookFalse()
            }else{
                checkBookTrue()
            }
        }

    }
    private fun checkBookTrue(){
        checkBook=true
        binding.editText.isActivated=false
        binding.editText.isEnabled=false
        binding.uploadBook.setBackgroundResource(R.color.button_color)
    }
    private fun checkBookFalse(){
        checkBook=false
        binding.editText.isActivated=true
        binding.editText.isEnabled=true
        binding.editText.requestFocus()
        binding.editText.postDelayed({
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editText, InputMethodManager.SHOW_IMPLICIT)
        }, 250)
        binding.uploadBook.setBackgroundResource(R.color.white)
    }

    private fun popupUploadMore() {
        val inflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customMenuView = inflater.inflate(R.layout.custom_upload_more_menu, null)

        popupWindowUploadMore = PopupWindow(
            customMenuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindowUploadMore.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        menuPageSorter = customMenuView.findViewById(R.id.page_sorter)
        menuPageTemplates = customMenuView.findViewById(R.id.page_template)
        menuPageSettings = customMenuView.findViewById(R.id.page_settings)
        menuFullScreen = customMenuView.findViewById(R.id.full_screen)
        menuInvite = customMenuView.findViewById(R.id.invite_collaborators)
        menuAddToShared = customMenuView.findViewById(R.id.add_to_share)
        menuTags = customMenuView.findViewById(R.id.tags)
        menuSaveFile = customMenuView.findViewById(R.id.save_files)
        menuFav = customMenuView.findViewById(R.id.upload_fav)
        menuShare = customMenuView.findViewById(R.id.upload_share)
        menuDelete = customMenuView.findViewById(R.id.upload_delete)
    }

    private fun showPopupUploadMore(view: View) {
        menuPageSorter.setOnClickListener {
            Toast.makeText(this, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuPageTemplates.setOnClickListener {
            Toast.makeText(this, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuPageSettings.setOnClickListener {
            Toast.makeText(this, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuFullScreen.setOnClickListener {
            Toast.makeText(this, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuInvite.setOnClickListener {
            Toast.makeText(this, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuAddToShared.setOnClickListener {
            Toast.makeText(this, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuTags.setOnClickListener {
            Toast.makeText(this, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuSaveFile.setOnClickListener {
            Toast.makeText(this, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuFav.setOnClickListener {
            if (checkFav){
                checkFavFalse()
            }else{
                checkFavTrue()
            }
        }
        menuShare.setOnClickListener {
            dismissMenu()
            Toast.makeText(this, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuDelete.setOnClickListener {
            dismissMenu()
            val intent= Intent(this,MainActivity::class.java)
            viewModel.playStop()
            viewModel.stopRecord()
            if (::mediaPlayer.isInitialized){
                mediaPlayer.stop()
                mediaPlayer.release()
            }
            finish()
            startActivity(intent)
        }
        popupWindowUploadMore.isOutsideTouchable = true
        popupWindowUploadMore.animationStyle = R.style.PopupAnimation
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val xOffset = location[0]
        val yOffset = location[1] - view.height
        popupWindowUploadMore.showAtLocation(view, Gravity.NO_GRAVITY, xOffset, yOffset)
    }
    private fun popupUploadAttach() {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customMenuView = inflater.inflate(R.layout.custom_upload_menu, null)

        popupWindowUploadAttach = PopupWindow(
            customMenuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindowUploadAttach.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        menuImage = customMenuView.findViewById(R.id.upload_image_linear)
        menuCamera = customMenuView.findViewById(R.id.upload_camera_linear)
        menuScan = customMenuView.findViewById(R.id.upload_scan_linear)
        menuPdf = customMenuView.findViewById(R.id.upload_pdf_linear)
        menuVoice = customMenuView.findViewById(R.id.upload_voice_linear)
        menuAudio = customMenuView.findViewById(R.id.upload_audio_linear)
        menuDraw = customMenuView.findViewById(R.id.upload_drawing_linear)
        menuTextBox = customMenuView.findViewById(R.id.upload_textbox_linear)
    }

    private fun showPopupUploadAttach(view: View) {
        menuImage.setOnClickListener {
            checkPermissionGallery(view)
            dismissMenu()
        }
        menuCamera.setOnClickListener {
            checkCameraScan=true
            checkPermissions()
            dismissMenu()
        }
        menuScan.setOnClickListener {
            checkCameraScan=false
            checkPermissions()
            dismissMenu()
        }
        menuPdf.setOnClickListener {
            Toast.makeText(this, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuVoice.setOnClickListener {
            checkPermissionVoice()
            dismissMenu()
        }
        menuAudio.setOnClickListener {
            checkPermissionAudio(view)
            dismissMenu()
        }
        menuDraw.setOnClickListener {
            Toast.makeText(this, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        menuTextBox.setOnClickListener {
            Toast.makeText(this, "These properties do not work yet.", Toast.LENGTH_SHORT).show()
        }
        popupWindowUploadAttach.isOutsideTouchable = true
        popupWindowUploadAttach.animationStyle = R.style.PopupAnimation
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val xOffset = location[0]
        val yOffset = location[1] - view.height
        popupWindowUploadAttach.showAtLocation(view, Gravity.NO_GRAVITY, xOffset, yOffset)
    }
    private fun checkFavTrue(){
        checkFav=true
        menuFav.setImageResource(R.drawable.baseline_star_24)
        dismissMenu()
        Toast.makeText(this, "Note added to favorites.", Toast.LENGTH_SHORT).show()
    }
    private fun checkFavFalse(){
        checkFav=false
        menuFav.setImageResource(R.drawable.baseline_star_border_24)
        dismissMenu()
        Toast.makeText(this, "Note removed from favorites.", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (popupWindowUploadAttach.isShowing||popupWindowUploadMore.isShowing){
            dismissMenu()
        }else{
            viewModel.playStop()
            viewModel.stopRecord()
            if (::mediaPlayer.isInitialized){
                mediaPlayer.stop()
                mediaPlayer.release()
            }
            super.onBackPressed()
            finish()
        }
    }
}
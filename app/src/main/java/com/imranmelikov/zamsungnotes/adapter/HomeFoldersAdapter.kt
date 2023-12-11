package com.imranmelikov.zamsungnotes.adapter

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.imranmelikov.zamsungnotes.R
import com.imranmelikov.zamsungnotes.databinding.RecyclerFolderBinding
import com.imranmelikov.zamsungnotes.model.Folders
import com.imranmelikov.zamsungnotes.model.Notes


class HomeFoldersAdapter(private val context: Context, private var notesList:List<Notes>,private var folders:List<Folders>):
    RecyclerView.Adapter<HomeFoldersAdapter.HomeFoldersViewHolder>() {
    class HomeFoldersViewHolder(var binding:RecyclerFolderBinding): RecyclerView.ViewHolder(binding.root)

    private var clickEdit=true
    private val itemSelectedList= mutableListOf<Int>()
    private  var clickSelectAll=true
    private var checkPosition=false
    var onItemClickFirstItem:((Boolean)->Unit)?=null
    var onItemClickSendNotes:((Folders)->Unit)?=null
    var onItemClickSendNotesRm:((Folders)->Unit)?=null
    var onItemClickCheckAll:((Boolean)->Unit)?=null
    var onItemClickNavigationListener:((Folders)->Unit)?=null
    private lateinit var sharedPreferences: SharedPreferences
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFoldersViewHolder {
        val binding= RecyclerFolderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return HomeFoldersViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return folderList.size
    }

    override fun onBindViewHolder(holder: HomeFoldersViewHolder, position: Int) {
        val folderArrayList=folderList.get(position)
        holder.binding.folderName.text=folderArrayList.folderName

        val folderParentId=folders.filter { it.parentFolderId== folderArrayList.id&&!it.trash}
        val notesParentId=notesList.filter { it.parentId==folderArrayList.id &&!it.trash}
        val folderParentIdTrash=folderList.filter { it.parentFolderId== folderArrayList.id&&it.trash}
        val notesParentIdTrash=notesList.filter { it.parentId==folderArrayList.id &&it.trash}
        sharedPreferences = context.getSharedPreferences("PreferencesMain", Context.MODE_PRIVATE)
        val sharedFoldersId=sharedPreferences.getInt("Int",-100)
         if(sharedFoldersId==-4){
            holder.binding.folderCount.text=(folderParentIdTrash.size + notesParentIdTrash.size).toString()
        }else{
            holder.binding.folderCount.text=(folderParentId.size + notesParentId.size).toString()
        }
        folderUpColor(folderArrayList,holder)
        if (clickEdit){
            // If in edit mode, hide select buttons
            holder.binding.folderSelectButton.visibility= View.GONE
            holder.binding.folderCount.visibility= View.VISIBLE

            // Reset selected state for all items
            folderList.map { it.selected=false }


            // Block itemView to prevent unintended clicks during edit mode
            if (folderArrayList.trash){
                holder.itemView.setOnClickListener {
                    Toast.makeText(context,"Restore this folder to open it.", Toast.LENGTH_SHORT).show()
                }
            }else{
                holder.itemView.setOnClickListener {
                    onItemClickNavigationListener?.let {
                        it(folderArrayList)
                    }
                }
            }
        }else{
//            holder.itemView.setOnLongClickListener { true }
            // If not in edit mode, handle item clicks for checking and unchecking
            clickItems(holder, folderArrayList, position)

            // Handle checking and unchecking all items
            checkAllItems(holder)
        }
    }

    private fun checkAllItems(holder: HomeFoldersAdapter.HomeFoldersViewHolder){
        // Update the check state based on the clickSelectAll flag
        if (clickSelectAll){
            holder.binding.folderSelectButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
        }else{
            holder.binding.folderSelectButton.setImageResource(R.drawable.baseline_check_circle_24)
            // Ensure not to add extra items to the selected list
            if (!checkPosition){
                itemSelectedList.clear()
                itemSelectedList.addAll(folderList.indices)
                checkPosition=true
            }
        }
    }
    private fun clickItems(holder: HomeFoldersAdapter.HomeFoldersViewHolder, folderArraylist: Folders, position: Int){
        // Show select button and handle item clicks for checking and unchecking
        holder.binding.folderSelectButton.visibility= View.VISIBLE
        holder.binding.folderCount.visibility= View.GONE
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
    private fun selectItem(holder:HomeFoldersAdapter.HomeFoldersViewHolder, folderArraylist: Folders, position: Int) {
        // Add the selected item to the list, update UI, and trigger callbacks
        itemSelectedList.add(position)
        holder.binding.folderSelectButton.setImageResource(R.drawable.baseline_check_circle_24)
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
    private fun unSelectItem(holder: HomeFoldersAdapter.HomeFoldersViewHolder, folderArraylist: Folders, position: Int){
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
            println("Error")
        }
        folderArraylist.selected=false
        holder.binding.folderSelectButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
    }
    private fun folderUpColor(folderArrayList: Folders, holder: HomeFoldersAdapter.HomeFoldersViewHolder){
        if (folderArrayList.color=="Gray"){
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.folder_upcolor) as GradientDrawable
            holder.binding.folderUpColor.background=backgroundDrawable
        }else if (folderArrayList.color=="Red"){
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.folder_upcolor_red) as GradientDrawable
            holder.binding.folderUpColor.background=backgroundDrawable
        }else if (folderArrayList.color=="Orange"){
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.folder_upcolor_orange) as GradientDrawable
            holder.binding.folderUpColor.background=backgroundDrawable
        }else if (folderArrayList.color=="Yellow"){
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.folder_upcolor_yellow) as GradientDrawable
            holder.binding.folderUpColor.background=backgroundDrawable
        }else if (folderArrayList.color=="OpenGreen"){
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.folder_upcolor_open_green) as GradientDrawable
            holder.binding.folderUpColor.background=backgroundDrawable
        }else if (folderArrayList.color=="Green"){
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.folder_upcolor_green) as GradientDrawable
            holder.binding.folderUpColor.background=backgroundDrawable
        }else if (folderArrayList.color=="OpenBlue1"){
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.folder_upcolor_open_blue1) as GradientDrawable
            holder.binding.folderUpColor.background=backgroundDrawable
        }else if (folderArrayList.color=="OpenRed"){
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.folder_upcolor_open_red) as GradientDrawable
            holder.binding.folderUpColor.background=backgroundDrawable
        }else if (folderArrayList.color=="Pink"){
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.folder_upcolor_pink) as GradientDrawable
            holder.binding.folderUpColor.background=backgroundDrawable
        }else if (folderArrayList.color=="OpenBlue2"){
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.folder_upcolor_open_blue2) as GradientDrawable
            holder.binding.folderUpColor.background=backgroundDrawable
        }else if (folderArrayList.color=="Blue"){
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.folder_upcolor_blue) as GradientDrawable
            holder.binding.folderUpColor.background=backgroundDrawable
        }else if (folderArrayList.color=="Brown"){
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.folder_upcolor_brown) as GradientDrawable
            holder.binding.folderUpColor.background=backgroundDrawable
        }
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
    fun updateDataFolder(newData: List<Folders>) {
        folders= newData
        notifyDataSetChanged()
    }
}
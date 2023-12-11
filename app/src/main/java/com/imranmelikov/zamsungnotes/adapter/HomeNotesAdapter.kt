package com.imranmelikov.zamsungnotes.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.imranmelikov.zamsungnotes.R
import com.imranmelikov.zamsungnotes.UploadActivity
import com.imranmelikov.zamsungnotes.databinding.RecyclerPageBinding
import com.imranmelikov.zamsungnotes.model.Notes


class HomeNotesAdapter(private val context: Context): RecyclerView.Adapter<HomeNotesAdapter.HomeRvViewHolder>() {
    class HomeRvViewHolder(var binding: RecyclerPageBinding): RecyclerView.ViewHolder(binding.root)

    // Variables to control the state of the adapter
    private var clickEdit=true
    private  var clickSelectAll=true
    private var checkPosition=false
    var onItemClickFirstItem:((Boolean)->Unit)?=null
    var onItemClickSendNotes:((Notes)->Unit)?=null
    var onItemClickSendNotesRm:((Notes)->Unit)?=null
    var onItemClickCheckAll:((Boolean)->Unit)?=null
    //    var onItemLongListener:((Boolean)->Unit)?=null
    private val itemSelectedList= mutableListOf<Int>()

    // DiffUtil for efficient RecyclerView updates
    private val diffUtil=object : DiffUtil.ItemCallback<Notes>(){
        override fun areItemsTheSame(oldItem: Notes, newItem: Notes): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: Notes, newItem: Notes): Boolean {
            return oldItem==newItem
        }
    }
    private val recyclerDiffer= AsyncListDiffer(this,diffUtil)

    // Getter and setter for the list of Notes
    var notesList:List<Notes>
        get() = recyclerDiffer.currentList
        set(value) = recyclerDiffer.submitList(value)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRvViewHolder {
        val binding=RecyclerPageBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return HomeRvViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    override fun onBindViewHolder(holder: HomeRvViewHolder, position: Int) {
        val notesArrayList=notesList.get(position)
        holder.binding.createTime.text=notesArrayList.createDate
        if (!notesArrayList.lock){
            if (notesArrayList.favStar){
                holder.binding.favStar.visibility= View.VISIBLE
            }else{
                holder.binding.favStar.visibility= View.GONE
            }
            holder.binding.pageTitle.text=notesArrayList.title
            holder.binding.pageMainTitle.text=notesArrayList.title
            holder.binding.pageMainTitle.visibility= View.VISIBLE
            holder.binding.pageLock.visibility= View.GONE

            if (notesArrayList.trash){
                holder.binding.deleteTime.visibility= View.VISIBLE
                holder.binding.cardTrash.visibility= View.VISIBLE

                if (notesArrayList.trashTime=="1"){
                    holder.binding.deleteTime.text="Tomorrow"
                }else if (notesArrayList.trashTime=="0"){
                    holder.binding.deleteTime.text="Today"
                }else{
                    holder.binding.deleteTime.text="${notesArrayList.trashTime} days"
                }
            }else{
                holder.binding.deleteTime.visibility= View.GONE
                holder.binding.cardTrash.visibility= View.GONE
            }
        }else{
            if (notesArrayList.favStar){
                holder.binding.favStar.visibility= View.VISIBLE
            }else{
                holder.binding.favStar.visibility= View.GONE
            }
            holder.binding.deleteTime.visibility= View.GONE
            holder.binding.cardTrash.visibility= View.GONE
            holder.binding.pageTitle.text="Locked note"
            holder.binding.pageMainTitle.visibility= View.GONE
            holder.binding.pageLock.visibility= View.VISIBLE
        }

        if (clickEdit){
            // If in edit mode, hide select buttons
            holder.binding.pageSelectButton.visibility= View.GONE

            // Reset selected state for all items
            notesList.map { it.selected=false }

//            holder.itemView.setOnLongClickListener {
//                onItemLongListener?.let {
//                    it(true)
//                }
//                true
//            }
            // Block itemView to prevent unintended clicks during edit mode
            holder.itemView.setOnClickListener {
                if (notesArrayList.lock){
                    Navigation.findNavController(it).navigate(R.id.action_nav_home_to_passwordFragment)
                }else{
                    val intent=Intent(context,UploadActivity::class.java)
                    intent.putExtra("Note",notesArrayList)
                    context.startActivity(intent)
                }
            }
        }else{
//            holder.itemView.setOnLongClickListener { true }
            // If not in edit mode, handle item clicks for checking and unchecking
            clickItems(holder, notesArrayList, position)

            // Handle checking and unchecking all items
            checkAllItems(holder)
        }

    }

    private fun checkAllItems(holder: HomeNotesAdapter.HomeRvViewHolder){
        // Update the check state based on the clickSelectAll flag
        if (clickSelectAll){
            holder.binding.pageSelectButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
        }else{
            holder.binding.pageSelectButton.setImageResource(R.drawable.baseline_check_circle_24)
            // Ensure not to add extra items to the selected list
            if (!checkPosition){
                itemSelectedList.clear()
                itemSelectedList.addAll(notesList.indices)
                checkPosition=true
            }
        }
    }

    private fun clickItems(holder: HomeNotesAdapter.HomeRvViewHolder, notesArrayList: Notes, position: Int){
        // Show select button and handle item clicks for checking and unchecking
        holder.binding.pageSelectButton.visibility= View.VISIBLE
        fun clickItem(){
            if (!notesArrayList.selected){
                holder.itemView.setOnClickListener {
                    selectItem(holder, notesArrayList, position)
                    clickItem()
                }
            }else{
                holder.itemView.setOnClickListener {
                    unSelectItem(holder, notesArrayList, position)
                    clickItem()
                }
            }
        }
        clickItem()
    }
    private fun selectItem(holder: HomeNotesAdapter.HomeRvViewHolder, notesArrayList: Notes, position: Int) {
        // Add the selected item to the list, update UI, and trigger callbacks
        itemSelectedList.add(position)
        holder.binding.pageSelectButton.setImageResource(R.drawable.baseline_check_circle_24)
        notesArrayList.selected=true
        onItemClickFirstItem?.let {
            it(true)
        }
        onItemClickSendNotes?.let {
            it(notesArrayList)
        }
        if (itemSelectedList.size==notesList.size){
            onItemClickCheckAll?.let {
                it(true)
            }
        }
    }
    private fun unSelectItem(holder: HomeNotesAdapter.HomeRvViewHolder, notesArrayList: Notes, position: Int){
        // Remove the unselected item from the list, update UI, and trigger callbacks
        if (itemSelectedList.contains(position)){
            itemSelectedList.remove(position)
            onItemClickSendNotesRm?.let {
                it(notesArrayList)
            }
            if (itemSelectedList.isEmpty()){
                onItemClickFirstItem?.let {
                    it(false)
                    checkPosition=false
                }
            }else{
                if (itemSelectedList.size!=notesList.size){
                    onItemClickCheckAll?.let {
                        it(false)
                    }
                }
            }
        }else{
        }
        notesArrayList.selected=false
        holder.binding.pageSelectButton.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
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
        notesList.map{it.selected=true}
        checkPosition=false
        itemSelectedList.clear()
        notifyDataSetChanged()
    }

    // Method to trigger unselect all items and update UI
    fun clickSelectAllHide(){
        clickSelectAll=true
        notesList.map {it.selected=false }
        checkPosition=false
        itemSelectedList.clear()
        notifyDataSetChanged()
    }
}
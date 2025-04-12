package com.example.notesplay

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import java.io.File

//some comment
class FolderAdapter(
    private val folderList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderNameTextView: TextView = itemView.findViewById(android.R.id.text1)

        init {
            itemView.setOnClickListener {
                val folderName = folderList[adapterPosition]
                val intent = Intent(itemView.context, NoteListActivity::class.java)
                intent.putExtra("FOLDER_NAME", folderName)
                itemView.context.startActivity(intent)
            }

            // Hold to delete
            itemView.setOnLongClickListener {
                val folderToDelete = folderList[adapterPosition]
                (itemView.context as? FolderListActivity)?.showDeleteFolderConfirmationDialog(folderToDelete)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return FolderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val currentFolder = folderList[position]
        holder.folderNameTextView.text = currentFolder
    }

    override fun getItemCount(): Int {
        return folderList.size
    }
}
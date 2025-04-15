package com.example.notesplay

import android.content.Intent
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import java.io.File


class FolderAdapter(
    private val folderList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        val folderNameTextView: TextView = itemView.findViewById(android.R.id.text1)
        var currentFolderName: String? = null

        init {
            itemView.setOnClickListener {
                val folderName = folderList[adapterPosition]
                val intent = Intent(itemView.context, NoteListActivity::class.java)
                intent.putExtra("FOLDER_NAME", folderName)
                itemView.context.startActivity(intent)
            }

            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            val renameItem = menu?.add(0, R.id.context_menu_rename, 0, "Rename")
            val deleteItem = menu?.add(0, R.id.context_menu_delete, 1, "Delete")

            renameItem?.setOnMenuItemClickListener {
                (itemView.context as? FolderListActivity)?.showRenameFolderDialog(currentFolderName)
                true
            }
            deleteItem?.setOnMenuItemClickListener {
                currentFolderName?.let { it1 ->
                    (itemView.context as? FolderListActivity)?.showDeleteFolderConfirmationDialog(
                        it1
                    )
                }
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
        holder.currentFolderName = currentFolder
    }

    override fun getItemCount(): Int {
        return folderList.size
    }

}
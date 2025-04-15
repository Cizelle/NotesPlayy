package com.example.notesplay

import android.content.Intent
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private val noteList: List<String>,
    private val folderName: String?,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {


    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        val noteNameTextView: TextView = itemView.findViewById(android.R.id.text1)
        var currentNoteFileName: String? = null

        init {
            itemView.setOnClickListener {
                currentNoteFileName?.let { fileName ->
                    folderName?.let {
                        if (fileName.endsWith(".txt")) {
                            val intent = Intent(itemView.context, ViewNoteActivity::class.java)
                            intent.putExtra("FOLDER_NAME", it)
                            intent.putExtra("NOTE_FILE_NAME", fileName)
                            itemView.context.startActivity(intent)
                        } else if (fileName.endsWith(".jpg")) {
                            val intent = Intent(itemView.context, ViewImageNoteActivity::class.java)
                            intent.putExtra("FOLDER_NAME", it)
                            intent.putExtra("NOTE_FILE_NAME", fileName)
                            itemView.context.startActivity(intent)
                        }
                    }
                }
            }

            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            val renameItem = menu?.add(0, R.id.context_menu_rename, 0, "Rename")
            val deleteItem = menu?.add(0, R.id.context_menu_delete, 1, "Delete")
            val moveItem = menu?.add(0, R.id.context_menu_move, 2, "Move to Folder")

            renameItem?.setOnMenuItemClickListener {
                (itemView.context as? NoteListActivity)?.showRenameNoteDialog(currentNoteFileName)
                true
            }
            deleteItem?.setOnMenuItemClickListener {
                currentNoteFileName?.let { it1 ->
                    (itemView.context as? NoteListActivity)?.showDeleteNoteConfirmationDialog(
                        it1
                    )
                }
                true
            }
            moveItem?.setOnMenuItemClickListener {
                (itemView.context as? NoteListActivity)?.showFolderSelectionDialogForMove(currentNoteFileName)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = noteList[position]
        holder.noteNameTextView.text = currentNote
        holder.currentNoteFileName = currentNote // Set the filename
    }

    override fun getItemCount(): Int {
        return noteList.size
    }
}
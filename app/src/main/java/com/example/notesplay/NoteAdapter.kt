package com.example.notesplay

import android.content.Intent
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


    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteNameTextView: TextView = itemView.findViewById(android.R.id.text1)

        init {
            itemView.setOnClickListener {
                val noteFileName = noteList[adapterPosition]
                folderName?.let {
                    if (noteFileName.endsWith(".txt")) {
                        val intent = Intent(itemView.context, ViewNoteActivity::class.java)
                        intent.putExtra("FOLDER_NAME", it)
                        intent.putExtra("NOTE_FILE_NAME", noteFileName)
                        itemView.context.startActivity(intent)
                    } else if (noteFileName.endsWith(".jpg")) {
                        val intent = Intent(itemView.context, ViewImageNoteActivity::class.java)
                        intent.putExtra("FOLDER_NAME", it)
                        intent.putExtra("NOTE_FILE_NAME", noteFileName)
                        itemView.context.startActivity(intent)
                    }
                }
            }

            itemView.setOnLongClickListener {
                val noteToDelete = noteList[adapterPosition]
                (itemView.context as? NoteListActivity)?.showDeleteNoteConfirmationDialog(noteToDelete)
                true // Consume the long click
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
        if (currentNote.endsWith(".jpg")) {
            holder.noteNameTextView.text = "[Image] " + currentNote
        } else {
            holder.noteNameTextView.text = currentNote
        }
    }

    override fun getItemCount(): Int {
        return noteList.size
    }
}
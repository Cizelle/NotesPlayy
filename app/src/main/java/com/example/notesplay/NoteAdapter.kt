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
                    folderName?.let { folder ->
                        if (fileName.endsWith(".txt")) {
                            val intent = Intent(itemView.context, ViewNoteActivity::class.java)
                            intent.putExtra("FOLDER_NAME", folder)
                            intent.putExtra("NOTE_FILE_NAME", fileName)
                            (itemView.context as? NoteListActivity)?.startActivityForResult(
                                intent,
                                NoteListActivity.VIEW_NOTE_REQUEST_CODE
                            )
                        } else if (fileName.endsWith(".jpg")) {
                            val intent = Intent(itemView.context, ViewImageNoteActivity::class.java)
                            intent.putExtra("FOLDER_NAME", folder)
                            intent.putExtra("NOTE_FILE_NAME", fileName)
                            (itemView.context as? NoteListActivity)?.startActivityForResult(
                                intent,
                                NoteListActivity.VIEW_NOTE_REQUEST_CODE
                            )
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
            val generateQuizItem = menu?.add(
                0,
                R.id.context_menu_generate_quiz,
                3,
                "Generate Quiz"
            )

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
                (itemView.context as? NoteListActivity)?.showFolderSelectionDialogForMove(
                    currentNoteFileName
                )
                true
            }
            generateQuizItem?.setOnMenuItemClickListener {
                val noteContent =
                    (itemView.context as? NoteListActivity)?.getNoteContent(currentNoteFileName)
                if (noteContent != null) {
                    (itemView.context as? NoteListActivity)?.startQuizGeneration(noteContent)
                } else {
                    Toast.makeText(
                        itemView.context,
                        "Error loading note content for quiz.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
        holder.currentNoteFileName = currentNote
    }

    override fun getItemCount(): Int {
        return noteList.size
    }
}

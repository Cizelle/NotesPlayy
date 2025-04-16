package com.example.notesplay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuizResultsAdapter(private val results: List<QuizResultsActivity.QuizResult>) :
    RecyclerView.Adapter<QuizResultsAdapter.ResultViewHolder>() {

    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionTextView: TextView = itemView.findViewById(R.id.questionTextView)
        val userAnswerTextView: TextView = itemView.findViewById(R.id.userAnswerTextView)
        val correctAnswerTextView: TextView = itemView.findViewById(R.id.correctAnswerTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_result, parent, false)
        return ResultViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val currentResult = results[position]
        holder.questionTextView.text = currentResult.question
        holder.userAnswerTextView.text = "Your Answer: ${currentResult.userAnswer}"
        holder.correctAnswerTextView.text = "Correct Answer: ${currentResult.correctAnswer}"
    }

    override fun getItemCount() = results.size
}
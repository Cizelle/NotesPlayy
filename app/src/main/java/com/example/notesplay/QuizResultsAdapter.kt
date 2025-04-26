package com.example.notesplay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuizResultsAdapter(private val results: List<QuizResultsActivity.QuizResult>) :
    RecyclerView.Adapter<QuizResultsAdapter.ResultViewHolder>() {

    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionTextView: TextView = itemView.findViewById(R.id.questionTextView)
        val userAnswerTextView: TextView = itemView.findViewById(R.id.userAnswerTextView)
        val correctAnswerTextView: TextView = itemView.findViewById(R.id.correctAnswerTextView)
        val answerCorrectnessIndicator: ImageView = itemView.findViewById(R.id.answerCorrectnessIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_result, parent, false)
        return ResultViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]
        holder.questionTextView.text = result.question
        holder.userAnswerTextView.text = "Your Answer: ${result.userAnswer}"
        holder.correctAnswerTextView.text = "Correct Answer: ${result.correctAnswer}"

        if (result.isUserAnswerCorrect) {
            holder.answerCorrectnessIndicator.setImageResource(R.drawable.ic_correct)
            holder.answerCorrectnessIndicator.visibility = View.VISIBLE
        } else if (result.userAnswer != null && result.userAnswer != result.correctAnswer) {
            holder.answerCorrectnessIndicator.setImageResource(R.drawable.ic_incorrect)
            holder.answerCorrectnessIndicator.visibility = View.VISIBLE
        } else {
            holder.answerCorrectnessIndicator.visibility = View.GONE
        }
    }

    override fun getItemCount() = results.size
}
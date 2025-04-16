package com.example.notesplay

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesplay.QuizItem

class QuizResultsActivity : AppCompatActivity() {

    private lateinit var scoreTextView: TextView
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var resultsAdapter: QuizResultsAdapter
    private lateinit var doneButton: Button

    private var quizQuestions: List<QuizItem> = emptyList()
    private var userAnswers: Map<Int, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_results)

        scoreTextView = findViewById(R.id.scoreTextView)
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView)
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        doneButton = findViewById(R.id.doneButton)

        quizQuestions = intent.getSerializableExtra("QUIZ_QUESTIONS") as? List<QuizItem> ?: emptyList()
        userAnswers = intent.getSerializableExtra("USER_ANSWERS") as? Map<Int, String> ?: emptyMap()

        calculateScoreAndDisplayResults()

        doneButton.setOnClickListener {
            finish()
        }
    }

    private fun calculateScoreAndDisplayResults() {
        var score = 0
        val results = mutableListOf<QuizResult>()

        for (i in quizQuestions.indices) {
            val correctAnswer = quizQuestions[i].correctAnswer
            val userAnswer = userAnswers[i]
            val isCorrect = userAnswer == correctAnswer
            if (isCorrect) {
                score++
            }
            results.add(QuizResult(quizQuestions[i].question, userAnswer ?: "", correctAnswer))
        }

        scoreTextView.text = "Your Score: $score / ${quizQuestions.size}"

        resultsAdapter = QuizResultsAdapter(results)
        resultsRecyclerView.adapter = resultsAdapter
    }

    data class QuizResult(val question: String, val userAnswer: String, val correctAnswer: String)
}
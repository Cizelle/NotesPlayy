package com.example.notesplay

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    private lateinit var questionTextView: TextView
    private lateinit var optionsRadioGroup: RadioGroup
    private lateinit var option1RadioButton: RadioButton
    private lateinit var option2RadioButton: RadioButton
    private lateinit var option3RadioButton: RadioButton
    private lateinit var option4RadioButton: RadioButton
    private lateinit var nextButton: Button
    private lateinit var submitButton: Button

    private var quizQuestions: List<QuizItem> = emptyList()
    private var currentQuestionIndex = 0
    private var userAnswers = mutableMapOf<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        questionTextView = findViewById(R.id.questionTextView)
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup)
        option1RadioButton = findViewById(R.id.option1RadioButton)
        option2RadioButton = findViewById(R.id.option2RadioButton)
        option3RadioButton = findViewById(R.id.option3RadioButton)
        option4RadioButton = findViewById(R.id.option4RadioButton)
        nextButton = findViewById(R.id.nextButton)
        submitButton = findViewById(R.id.submitButton)

        quizQuestions = intent.getSerializableExtra("QUIZ_QUESTIONS") as? List<QuizItem> ?: emptyList()

        if (quizQuestions.isNotEmpty()) {
            displayQuestion(currentQuestionIndex)

            nextButton.setOnClickListener {
                val selectedOptionId = optionsRadioGroup.checkedRadioButtonId
                if (selectedOptionId != -1) {
                    val selectedRadioButton = findViewById<RadioButton>(selectedOptionId)
                    userAnswers[currentQuestionIndex] = selectedRadioButton.text.toString()
                    optionsRadioGroup.clearCheck()

                    currentQuestionIndex++
                    if (currentQuestionIndex < quizQuestions.size - 1) {
                        displayQuestion(currentQuestionIndex)
                    } else if (currentQuestionIndex == quizQuestions.size - 1) {
                        displayQuestion(currentQuestionIndex)
                        nextButton.visibility = View.GONE
                        submitButton.visibility = View.VISIBLE
                    }
                } else {
                }
            }

            submitButton.setOnClickListener {
                val selectedOptionId = optionsRadioGroup.checkedRadioButtonId
                if (selectedOptionId != -1) {
                    val selectedRadioButton = findViewById<RadioButton>(selectedOptionId)
                    userAnswers[currentQuestionIndex] = selectedRadioButton.text.toString()
                }
                showQuizResults()
            }
        } else {
            finish()
        }
    }


    private fun displayQuestion(index: Int) {
        val currentQuestionData = quizQuestions[index]
        questionTextView.text = currentQuestionData.question

        val optionsList = currentQuestionData.options.split(", ").shuffled()

        option1RadioButton.text = optionsList.getOrNull(0) ?: ""
        option2RadioButton.text = optionsList.getOrNull(1) ?: ""
        option3RadioButton.text = optionsList.getOrNull(2) ?: ""
        option4RadioButton.text = optionsList.getOrNull(3) ?: ""
    }

    private fun showQuizResults() {
        val intent = Intent(this, QuizResultsActivity::class.java)
        intent.putExtra("QUIZ_QUESTIONS", quizQuestions as java.io.Serializable)
        intent.putExtra("USER_ANSWERS", userAnswers as java.io.Serializable)
        startActivity(intent)
        finish()
    }
}

package com.example.notesplay

data class QuizItem(
    val question: String,
    val correctAnswer: String,
    val options: String
) : java.io.Serializable
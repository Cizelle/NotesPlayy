package com.example.notesplay

data class QuizItem(
    val question: String,
    val correctAnswer: String,
    val options: List<String>
) : java.io.Serializable

package com.example.multiplicationgame

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var questionTextView: TextView
    private lateinit var answerEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var jerboImageView: ImageView
    
    private var score = 0
    private var num1 = 0
    private var num2 = 0
    private var correctAnswer = 0
    private var questionsAnswered = 0
    private val totalQuestions = 10
    private val timePerQuestion = 10000L // 10 seconds
    
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        questionTextView = findViewById(R.id.questionTextView)
        answerEditText = findViewById(R.id.answerEditText)
        submitButton = findViewById(R.id.submitButton)
        scoreTextView = findViewById(R.id.scoreTextView)
        timerTextView = findViewById(R.id.timerTextView)
        jerboImageView = findViewById(R.id.jerboImageView)

        // Set initial jerbo state
        updateJerboState()

        // Set up click listener
        submitButton.setOnClickListener {
            checkAnswer()
        }

        // Start the game
        generateNewQuestion()
        startTimer()
    }

    private fun updateJerboState() {
        val state = when {
            score >= 0 -> 1
            score >= -2 -> 2
            score >= -4 -> 3
            score >= -6 -> 4
            else -> 5
        }
        val imageResource = resources.getIdentifier("state_$state", "drawable", packageName)
        jerboImageView.setImageResource(imageResource)
    }

    private fun generateNewQuestion() {
        num1 = Random.nextInt(1, 10)
        num2 = Random.nextInt(1, 10)
        correctAnswer = num1 * num2
        questionTextView.text = "$num1 × $num2 = ?"
        answerEditText.text.clear()
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(timePerQuestion, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = "Tiempo: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                // Time's up - wrong answer
                score--
                updateScore()
                updateJerboState()
                nextQuestion()
            }
        }.start()
    }

    private fun checkAnswer() {
        timer?.cancel()
        val userAnswer = answerEditText.text.toString().toIntOrNull()
        
        if (userAnswer == correctAnswer) {
            score++
        } else {
            score--
        }
        
        updateScore()
        updateJerboState()
        nextQuestion()
    }

    private fun updateScore() {
        scoreTextView.text = "Puntuación: $score"
    }

    private fun nextQuestion() {
        questionsAnswered++
        if (questionsAnswered >= totalQuestions) {
            endGame()
        } else {
            generateNewQuestion()
        }
    }

    private fun endGame() {
        questionTextView.text = "¡Juego terminado!\nPuntuación final: $score"
        answerEditText.isEnabled = false
        submitButton.isEnabled = false
        timer?.cancel()
    }
} 
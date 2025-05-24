package com.example.multiplicationgame

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
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
    private lateinit var timerTextView: TextView
    private lateinit var jerboImageView: ImageView
    private lateinit var restartButton: Button
    private lateinit var exitButton: Button
    
    private var score = 0
    private var num1 = 0
    private var num2 = 0
    private var correctAnswer = 0
    private var questionsAnswered = 0
    private var consecutiveCorrect = 0
    private var consecutiveIncorrect = 0
    private var jerboState = 1
    private val totalQuestions = 20
    private val timePerQuestion = 15000L // 15 seconds
    
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        questionTextView = findViewById(R.id.questionTextView)
        answerEditText = findViewById(R.id.answerEditText)
        submitButton = findViewById(R.id.submitButton)
        timerTextView = findViewById(R.id.timerTextView)
        jerboImageView = findViewById(R.id.jerboImageView)
        restartButton = findViewById(R.id.restartButton)
        exitButton = findViewById(R.id.exitButton)

        // Set up click listeners
        submitButton.setOnClickListener {
            checkAnswer()
        }

        restartButton.setOnClickListener {
            restartGame()
        }

        exitButton.setOnClickListener {
            finish()
        }

        // Start the game
        startGame()
    }

    private fun startGame() {
        // Reset all game variables
        score = 0
        questionsAnswered = 0
        consecutiveCorrect = 0
        consecutiveIncorrect = 0
        jerboState = 1
        
        // Reset UI elements
        questionTextView.text = ""
        answerEditText.text.clear()
        timerTextView.text = "Tiempo: ${timePerQuestion / 1000}s"
        
        // Reset button states
        answerEditText.isEnabled = true
        submitButton.isEnabled = true
        
        // Show/hide appropriate views
        answerEditText.visibility = View.VISIBLE
        submitButton.visibility = View.VISIBLE
        restartButton.visibility = View.GONE
        exitButton.visibility = View.GONE
        
        // Update jerbo to initial state
        updateJerboState()
        
        // Start new game
        generateNewQuestion()
        startTimer()
    }

    private fun updateJerboState() {
        val imageResource = resources.getIdentifier("state_$jerboState", "drawable", packageName)
        jerboImageView.setImageResource(imageResource)
        
        // Check if jerbo is dead (state 6)
        if (jerboState == 6) {
            endGame()
        }
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
                consecutiveCorrect = 0
                consecutiveIncorrect++
                if (consecutiveIncorrect >= 2) {
                    if (jerboState < 6) {
                        jerboState++
                        consecutiveIncorrect = 0
                    }
                }
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
            consecutiveCorrect++
            consecutiveIncorrect = 0
            // If 3 correct answers in a row, improve jerbo state
            if (consecutiveCorrect >= 3) {
                if (jerboState > 1) {
                    jerboState--
                }
                consecutiveCorrect = 0
            }
        } else {
            score--
            consecutiveCorrect = 0
            consecutiveIncorrect++
            if (consecutiveIncorrect >= 2) {
                if (jerboState < 6) {
                    jerboState++
                    consecutiveIncorrect = 0
                }
            }
        }
        
        updateJerboState()
        nextQuestion()
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
        val message = if (jerboState == 6) {
            "¡El jerbo ha muerto!\nPuntuación final: $score"
        } else {
            "¡Juego terminado!\nPuntuación final: $score"
        }
        questionTextView.text = message
        answerEditText.isEnabled = false
        submitButton.isEnabled = false
        timer?.cancel()
        
        // Hide game elements and show restart/exit buttons
        answerEditText.visibility = View.GONE
        submitButton.visibility = View.GONE
        restartButton.visibility = View.VISIBLE
        exitButton.visibility = View.VISIBLE
    }

    private fun restartGame() {
        startGame()
    }
} 
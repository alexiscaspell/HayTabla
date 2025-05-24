package com.example.multiplicationgame

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var questionTextView: TextView
    private lateinit var answerEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var timerTextView: TextView
    private lateinit var healthTextView: TextView
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
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var vibrator: Vibrator
    private var isGameActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        questionTextView = findViewById(R.id.questionTextView)
        answerEditText = findViewById(R.id.answerEditText)
        submitButton = findViewById(R.id.submitButton)
        timerTextView = findViewById(R.id.timerTextView)
        healthTextView = findViewById(R.id.healthTextView)
        jerboImageView = findViewById(R.id.jerboImageView)
        restartButton = findViewById(R.id.restartButton)
        exitButton = findViewById(R.id.exitButton)

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Set up click listeners
        submitButton.setOnClickListener {
            if (isGameActive) {
                checkAnswer()
            }
        }

        // Set up keyboard action listener
        answerEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && isGameActive) {
                checkAnswer()
                true
            } else {
                false
            }
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

    private fun playWrongAnswerEffect() {
        if (!isGameActive) return
        
        // Vibrate
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }

        // Play sound
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.wrong_answer)
        mediaPlayer?.setOnCompletionListener { mp ->
            mp.release()
            mediaPlayer = null
        }
        mediaPlayer?.start()
    }

    private fun playCorrectAnswerEffect() {
        if (!isGameActive) return
        
        // Play sound
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.correct_answer)
        mediaPlayer?.setOnCompletionListener { mp ->
            mp.release()
            mediaPlayer = null
        }
        mediaPlayer?.start()
    }

    private fun startGame() {
        // Reset all game variables
        score = 0
        questionsAnswered = 0
        consecutiveCorrect = 0
        consecutiveIncorrect = 0
        jerboState = 1
        isGameActive = true
        
        // Reset UI elements
        questionTextView.text = ""
        answerEditText.text.clear()
        timerTextView.text = "Tiempo: ${timePerQuestion / 1000}s"
        updateHealthDisplay()
        
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

    private fun updateHealthDisplay() {
        val healthPercentage = ((7 - jerboState) * 100.0 / 6.0).roundToInt()
        healthTextView.text = "Salud de Roberto: $healthPercentage%"
    }

    private fun updateJerboState() {
        val imageResource = resources.getIdentifier("state_$jerboState", "drawable", packageName)
        jerboImageView.setImageResource(imageResource)
        updateHealthDisplay()
        
        // Check if jerbo is dead (state 6)
        if (jerboState == 6) {
            endGame()
        }
    }

    private fun generateNewQuestion() {
        if (!isGameActive) return
        
        num1 = Random.nextInt(1, 10)
        num2 = Random.nextInt(1, 10)
        correctAnswer = num1 * num2
        questionTextView.text = "$num1 × $num2 = ?"
        answerEditText.text.clear()
        answerEditText.requestFocus()
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(timePerQuestion, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (isGameActive) {
                    timerTextView.text = "Tiempo: ${millisUntilFinished / 1000}s"
                }
            }

            override fun onFinish() {
                if (isGameActive) {
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
                    playWrongAnswerEffect()
                    updateJerboState()
                    nextQuestion()
                }
            }
        }.start()
    }

    private fun checkAnswer() {
        if (!isGameActive) return
        
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
            playCorrectAnswerEffect()
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
            playWrongAnswerEffect()
        }
        
        updateJerboState()
        nextQuestion()
    }

    private fun nextQuestion() {
        if (!isGameActive) return
        
        questionsAnswered++
        if (questionsAnswered >= totalQuestions) {
            endGame()
        } else {
            generateNewQuestion()
        }
    }

    private fun endGame() {
        isGameActive = false
        timer?.cancel()
        timer = null
        
        val message = if (jerboState == 6) {
            "¡Roberto ha muerto!\nPuntuación final: $score"
        } else {
            "¡Juego terminado!\nRoberto sobrevivió con una puntuación de: $score"
        }
        questionTextView.text = message
        answerEditText.isEnabled = false
        submitButton.isEnabled = false
        
        // Hide game elements and show restart/exit buttons
        answerEditText.visibility = View.GONE
        submitButton.visibility = View.GONE
        restartButton.visibility = View.VISIBLE
        exitButton.visibility = View.VISIBLE
    }

    private fun restartGame() {
        startGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        isGameActive = false
        timer?.cancel()
        timer = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
} 
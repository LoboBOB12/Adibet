package app.lobo.adibet
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import android.widget.TextView
import java.util.*
import kotlin.math.abs

class GameActivity : Activity() {
    private lateinit var gameView: GameView
    private lateinit var scoreTextView: TextView
    private lateinit var timeTextView: TextView

    private lateinit var ballBitmap: Bitmap
    private lateinit var towerBitmap: Bitmap

    private var ballX: Float = 0f
    private var ballY: Float = 0f
    private var ballVelocity: Float = 0f
    private val gravity: Float = 0.6f

    private var towerX: Float = 0f
    private var towerY: Float = 0f

    private var score: Int = 0
    private var time: Int = 0
    private lateinit var timer: Timer
    private lateinit var handler: Handler

    private val gameLoopRunnable = object : Runnable {
        override fun run() {
            updateGame()
            renderGame()
            handler.postDelayed(this, 16)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameView = GameView(this)
        val gameContainer = findViewById<FrameLayout>(R.id.gameContainer)
        gameContainer.addView(gameView)

        scoreTextView = findViewById(R.id.scoreTextView)
        timeTextView = findViewById(R.id.timeTextView)

        ballBitmap = BitmapFactory.decodeResource(resources, R.drawable.ball)
        towerBitmap = BitmapFactory.decodeResource(resources, R.drawable.tower)

        handler = Handler()
    }

    override fun onResume() {
        super.onResume()
        startGame()
    }

    override fun onPause() {
        super.onPause()
        stopGame()
    }

    private fun startGame() {
        resetGame()
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                time++
                runOnUiThread { timeTextView.text = "Time: $time" }
            }
        }, 1000, 1000)
        handler.postDelayed(gameLoopRunnable, 16)

        gameView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                ballVelocity = -16f
            }
            true
        }
    }

    private fun stopGame() {
        timer.cancel()
        handler.removeCallbacks(gameLoopRunnable)
    }

    private fun resetGame() {
        ballX = 100f
        ballY = gameView.height / 2f - ballBitmap.height / 2f
        ballVelocity = 0f
        towerX = gameView.width.toFloat()
        towerY = 0f
        score = 0
        time = 0
        scoreTextView.text = "Score: $score"
        timeTextView.text = "Time: $time"
    }

    private fun updateGame() {
        // Update the position of the ball based on the velocity
        ballY += ballVelocity

        // Apply gravity to the ball's velocity
        ballVelocity += gravity

        // Check if the ball reaches the bottom of the screen
        if (ballY + ballBitmap.height >= gameView.height) {
            ballY = (gameView.height - ballBitmap.height).toFloat()
            ballVelocity = 0f // Reset the velocity when the ball hits the ground
        }

        // Update the position of the tower
        towerX -= 10f

        // Check if the tower reaches the left side of the screen
        if (towerX + towerBitmap.width < 0) {
            towerX = gameView.width.toFloat()
            towerY = Random().nextInt(gameView.height - towerBitmap.height).toFloat()
            increaseScore()
        }

        // Check for collision between the ball and the tower
        if (isCollisionDetected()) {
            gameOver()
        }
    }

    private fun renderGame() {
        val canvas = gameView.holder.lockCanvas()
        try {
            // Draw the background image
            val backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.bg1)
            val scaledBackgroundBitmap = Bitmap.createScaledBitmap(
                backgroundBitmap, canvas.width, canvas.height, false
            )
            canvas?.drawBitmap(scaledBackgroundBitmap, 0f, 0f, null)

            // Draw the ball and tower
            canvas?.drawBitmap(ballBitmap, ballX, ballY, null)
            canvas?.drawBitmap(towerBitmap, towerX, towerY, null)
        } finally {
            gameView.holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun increaseScore() {
        score++
        runOnUiThread { scoreTextView.text = "Score: $score" }
    }

    private fun isCollisionDetected(): Boolean {
        val ballRect = RectF(ballX, ballY, ballX + ballBitmap.width, ballY + ballBitmap.height)
        val towerRect = RectF(towerX, towerY, towerX + towerBitmap.width, towerY + towerBitmap.height)
        return RectF.intersects(ballRect, towerRect)
    }

    private fun gameOver() {
        stopGame()
        val intent = Intent(this, finnish::class.java)
        startActivity(intent)
    }

    inner class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
        init {
            holder.addCallback(this)
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            startGame()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            stopGame()
        }
    }
}




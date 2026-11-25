package com.shortstracker.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class BlockActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val d = resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#14161F"))
            setPadding(dp(32), dp(32), dp(32), dp(32))
        }

        root.addView(TextView(this).apply {
            text = "🛑"
            textSize = 64f
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "Daily limit reached"
            textSize = 26f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, dp(8))
        })
        root.addView(TextView(this).apply {
            text = "You have watched ${Store.count(this@BlockActivity)} Reels/Shorts today. Reels are blocked until tomorrow. Time to study!"
            textSize = 16f
            setTextColor(Color.parseColor("#B6BAC9"))
            gravity = Gravity.CENTER
        })
        root.addView(Button(this).apply {
            text = "Go to Home"
            setOnClickListener { goHome() }
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(32) })

        setContentView(root)
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        goHome()
    }
}

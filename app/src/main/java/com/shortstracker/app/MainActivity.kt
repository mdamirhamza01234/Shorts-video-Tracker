package com.shortstracker.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var ring: RingView
    private lateinit var streakText: TextView
    private lateinit var limitText: TextView
    private lateinit var statusCard: LinearLayout
    private lateinit var statusText: TextView
    private val appCountViews = mutableMapOf<String, TextView>()

    private val d get() = resources.displayMetrics.density
    private fun dp(v: Int) = (v * d).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.firstRun(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(40), dp(20), dp(32))
        }

        content.addView(label("Shorts Tracker", 24f, Color.WHITE, bold = true))

        streakText = label("", 18f, Color.parseColor("#FFB547"), bold = true).apply {
            setPadding(0, dp(8), 0, dp(16))
        }
        content.addView(streakText)

        ring = RingView(this)
        content.addView(ring)

        // Limit controls
        val limitRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(20), 0, dp(8))
        }
        limitRow.addView(Button(this).apply {
            text = "-"
            setOnClickListener { changeLimit(-5) }
        })
        limitText = label("", 18f, Color.WHITE).apply {
            setPadding(dp(20), 0, dp(20), 0)
        }
        limitRow.addView(limitText)
        limitRow.addView(Button(this).apply {
            text = "+"
            setOnClickListener { changeLimit(5) }
        })
        content.addView(limitRow)

        // Accessibility status
        statusCard = card()
        statusText = label("", 15f, Color.WHITE)
        statusCard.addView(statusText)
        statusCard.addView(Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        content.addView(statusCard, cardParams())

        // Blocks
        val blocks = card()
        blocks.addView(label("Blocks", 18f, Color.WHITE, bold = true).apply {
            setPadding(0, 0, 0, dp(8))
        })
        for (app in Apps.all) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(6), 0, dp(6))
            }
            val names = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            names.addView(label(app.label, 16f, Color.WHITE))
            val cnt = label("", 13f, Color.parseColor("#B6BAC9"))
            appCountViews[app.pkg] = cnt
            names.addView(cnt)
            row.addView(names, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(Switch(this).apply {
                isChecked = Store.blockEnabled(this@MainActivity, app.pkg)
                setOnCheckedChangeListener { _, on -> Store.setBlockEnabled(this@MainActivity, app.pkg, on) }
            })
            blocks.addView(row)
        }
        content.addView(blocks, cardParams())

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#14161F"))
            addView(content)
        }
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun changeLimit(delta: Int) {
        Store.setLimit(this, Store.limit(this) + delta)
        refresh()
    }

    private fun refresh() {
        val limit = Store.limit(this)
        val count = Store.count(this)
        ring.update(count, limit)
        limitText.text = "Daily limit: $limit"

        val streak = Store.streak(this)
        streakText.text = if (count > limit) "💨 Streak lost - try again tomorrow"
        else "🔥 $streak day streak"

        for (app in Apps.all) {
            appCountViews[app.pkg]?.text = "${Store.appCount(this, app.pkg)} today"
        }

        val on = isServiceEnabled()
        statusCard.visibility = if (on) View.GONE else View.VISIBLE
        statusText.text = "Tracking is OFF. Turn on \"Shorts Tracker\" in Accessibility settings so reels can be counted."
    }

    private fun isServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any {
            it.equals("$packageName/${ReelService::class.java.name}", ignoreCase = true) ||
                it.equals("$packageName/.ReelService", ignoreCase = true)
        }
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = GradientDrawable().apply {
            setColor(Color.parseColor("#1E2130"))
            cornerRadius = 16 * d
        }
    }

    private fun cardParams() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = dp(16) }
}

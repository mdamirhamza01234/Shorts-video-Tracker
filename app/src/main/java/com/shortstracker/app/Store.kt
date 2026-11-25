package com.shortstracker.app

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TrackedApp(val pkg: String, val label: String)

object Apps {
    val all = listOf(
        TrackedApp("com.instagram.android", "Instagram Reels"),
        TrackedApp("com.google.android.youtube", "YouTube Shorts"),
        TrackedApp("com.facebook.katana", "Facebook Reels")
    )

    fun find(pkg: String): TrackedApp? = all.firstOrNull { it.pkg == pkg }
}

object Store {
    private const val DEFAULT_LIMIT = 30
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun prefs(c: Context): SharedPreferences =
        c.applicationContext.getSharedPreferences("tracker", Context.MODE_PRIVATE)

    fun dateKey(d: Date = Date()): String = fmt.format(d)

    fun limit(c: Context): Int = prefs(c).getInt("limit", DEFAULT_LIMIT)

    fun setLimit(c: Context, value: Int) {
        prefs(c).edit().putInt("limit", value.coerceIn(1, 500)).apply()
    }

    /** First day the app was opened; streaks never count days before this. */
    fun firstRun(c: Context): String {
        val p = prefs(c)
        val existing = p.getString("first_run", null)
        if (existing != null) return existing
        val today = dateKey()
        p.edit().putString("first_run", today).apply()
        return today
    }

    fun blockEnabled(c: Context, pkg: String): Boolean =
        prefs(c).getBoolean("block_$pkg", true)

    fun setBlockEnabled(c: Context, pkg: String, on: Boolean) {
        prefs(c).edit().putBoolean("block_$pkg", on).apply()
    }

    fun count(c: Context, date: String = dateKey()): Int =
        prefs(c).getInt("c_$date", 0)

    fun appCount(c: Context, pkg: String, date: String = dateKey()): Int =
        prefs(c).getInt("a_${date}_$pkg", 0)

    fun add(c: Context, pkg: String) {
        val d = dateKey()
        val p = prefs(c)
        p.edit()
            .putInt("c_$d", p.getInt("c_$d", 0) + 1)
            .putInt("a_${d}_$pkg", p.getInt("a_${d}_$pkg", 0) + 1)
            .apply()
    }

    /**
     * Number of consecutive days (ending today) spent at or under the limit.
     * Today only counts while it is still under the limit, so the flame goes out
     * as soon as the limit is crossed.
     */
    fun streak(c: Context): Int {
        val limit = limit(c)
        val first = firstRun(c)
        val cal = Calendar.getInstance()
        var streak = 0
        var guard = 0
        while (guard++ < 3650) {
            val key = dateKey(cal.time)
            if (key < first) break
            if (count(c, key) > limit) break
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }
}

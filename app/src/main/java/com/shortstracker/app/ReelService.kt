package com.shortstracker.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ReelService : AccessibilityService() {

    private var lastCountAt = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) return
        val pkg = event.packageName?.toString() ?: return
        if (Apps.find(pkg) == null) return
        if (!Store.blockEnabled(this, pkg)) return

        val root = rootInActiveWindow ?: return
        if (!isReelScreen(root)) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastCountAt < 900) return
        lastCountAt = now

        if (Store.count(this) >= Store.limit(this)) {
            block()
        } else {
            Store.add(this, pkg)
        }
    }

    override fun onInterrupt() {}

    private fun block() {
        performGlobalAction(GLOBAL_ACTION_HOME)
        val i = Intent(this, BlockActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }

    /** Looks for a Reels/Shorts player view in the current window (capped search). */
    private fun isReelScreen(root: AccessibilityNodeInfo): Boolean {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0
        while (queue.isNotEmpty() && visited < 500) {
            val node = queue.removeFirst()
            visited++
            val id = node.viewIdResourceName?.lowercase()
            if (id != null && MARKERS.any { id.contains(it) }) return true
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return false
    }

    companion object {
        private val MARKERS = listOf("clips_viewer", "reel_recycler", "reel_player", "reels_viewer", "shorts")
    }
}

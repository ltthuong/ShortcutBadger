package me.leolin.shortcutbadger.impl

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import me.leolin.shortcutbadger.Badger
import me.leolin.shortcutbadger.ShortcutBadgeException

class ZTEHomeBadger : Badger {
    @Throws(ShortcutBadgeException::class)
    override fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int) {
        val extra = Bundle()
        extra.putInt("app_badge_count", badgeCount)
        extra.putString("app_badge_component_name", componentName.flattenToString())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            context.contentResolver.call(
                Uri.parse("content://com.android.launcher3.cornermark.unreadbadge"),
                "setAppUnreadCount", null, extra
            )
        }
    }

    override val supportLaunchers: List<String>
        get() = ArrayList(0)
}


package me.leolin.shortcutbadger.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import me.leolin.shortcutbadger.Badger
import me.leolin.shortcutbadger.ShortcutBadgeException

/**
 * @author leolin
 */
class VivoHomeBadger : Badger {
    @Throws(ShortcutBadgeException::class)
    override fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int) {
        val intent = Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM")
        intent.putExtra("packageName", context.packageName)
        intent.putExtra("className", componentName.className)
        intent.putExtra("notificationNum", badgeCount)
        context.sendBroadcast(intent)
    }

    override val supportLaunchers: List<String>
        get() = mutableListOf("com.vivo.launcher")
}

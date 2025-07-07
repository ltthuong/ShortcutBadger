package me.leolin.shortcutbadger.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import me.leolin.shortcutbadger.Badger
import me.leolin.shortcutbadger.ShortcutBadgeException
import me.leolin.shortcutbadger.util.BroadcastHelper.sendIntentExplicitly

/**
 * @author Gernot Pansy
 */
class ApexHomeBadger : Badger {
    @Throws(ShortcutBadgeException::class)
    override fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int) {
        val intent = Intent(INTENT_UPDATE_COUNTER)
        intent.putExtra(PACKAGENAME, componentName.packageName)
        intent.putExtra(COUNT, badgeCount)
        intent.putExtra(CLASS, componentName.className)

        sendIntentExplicitly(context, intent)
    }

    override val supportLaunchers: List<String>
        get() = mutableListOf("com.anddoes.launcher")

    companion object {
        private const val INTENT_UPDATE_COUNTER = "com.anddoes.launcher.COUNTER_CHANGED"
        private const val PACKAGENAME = "package"
        private const val COUNT = "count"
        private const val CLASS = "class"
    }
}

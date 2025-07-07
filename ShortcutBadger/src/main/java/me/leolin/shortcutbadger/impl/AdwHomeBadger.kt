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
class AdwHomeBadger : Badger {
    @Throws(ShortcutBadgeException::class)
    override fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int) {
        val intent = Intent(INTENT_UPDATE_COUNTER)
        intent.putExtra(PACKAGENAME, componentName.packageName)
        intent.putExtra(CLASSNAME, componentName.className)
        intent.putExtra(COUNT, badgeCount)

        sendIntentExplicitly(context, intent)
    }

    override val supportLaunchers: List<String>
        get() = mutableListOf(
            "org.adw.launcher",
            "org.adwfreak.launcher"
        )

    companion object {
        const val INTENT_UPDATE_COUNTER: String = "org.adw.launcher.counter.SEND"
        const val PACKAGENAME: String = "PNAME"
        const val CLASSNAME: String = "CNAME"
        const val COUNT: String = "COUNT"
    }
}

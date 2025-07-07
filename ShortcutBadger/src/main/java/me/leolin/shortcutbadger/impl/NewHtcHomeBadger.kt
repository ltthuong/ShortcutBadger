package me.leolin.shortcutbadger.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import me.leolin.shortcutbadger.Badger
import me.leolin.shortcutbadger.ShortcutBadgeException
import me.leolin.shortcutbadger.util.BroadcastHelper.sendIntentExplicitly

/**
 * @author Leo Lin
 */
class NewHtcHomeBadger : Badger {
    @Throws(ShortcutBadgeException::class)
    override fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int) {
        val intent1 = Intent(INTENT_SET_NOTIFICATION)
        var intent1Success: Boolean

        intent1.putExtra(EXTRA_COMPONENT, componentName.flattenToShortString())
        intent1.putExtra(EXTRA_COUNT, badgeCount)

        val intent = Intent(INTENT_UPDATE_SHORTCUT)
        var intentSuccess: Boolean

        intent.putExtra(PACKAGENAME, componentName.packageName)
        intent.putExtra(COUNT, badgeCount)

        try {
            sendIntentExplicitly(context, intent1)
            intent1Success = true
        } catch (e: ShortcutBadgeException) {
            intent1Success = false
        }

        try {
            sendIntentExplicitly(context, intent)
            intentSuccess = true
        } catch (e: ShortcutBadgeException) {
            intentSuccess = false
        }

        if (!intent1Success && !intentSuccess) {
            throw ShortcutBadgeException("unable to resolve intent: $intent")
        }
    }

    override val supportLaunchers: List<String>
        get() = listOf("com.htc.launcher")

    companion object {
        const val INTENT_UPDATE_SHORTCUT: String = "com.htc.launcher.action.UPDATE_SHORTCUT"
        const val INTENT_SET_NOTIFICATION: String = "com.htc.launcher.action.SET_NOTIFICATION"
        const val PACKAGENAME: String = "packagename"
        const val COUNT: String = "count"
        const val EXTRA_COMPONENT: String = "com.htc.launcher.extra.COMPONENT"
        const val EXTRA_COUNT: String = "com.htc.launcher.extra.COUNT"
    }
}

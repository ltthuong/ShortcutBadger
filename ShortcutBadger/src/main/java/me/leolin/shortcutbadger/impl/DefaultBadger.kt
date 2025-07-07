package me.leolin.shortcutbadger.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import me.leolin.shortcutbadger.Badger
import me.leolin.shortcutbadger.ShortcutBadgeException
import me.leolin.shortcutbadger.util.BroadcastHelper.resolveBroadcast
import me.leolin.shortcutbadger.util.BroadcastHelper.sendDefaultIntentExplicitly

/**
 * @author leolin
 */
class DefaultBadger : Badger {
    @Throws(ShortcutBadgeException::class)
    override fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int) {
        val intent = Intent(INTENT_ACTION)
        intent.putExtra(INTENT_EXTRA_BADGE_COUNT, badgeCount)
        intent.putExtra(INTENT_EXTRA_PACKAGENAME, componentName.packageName)
        intent.putExtra(INTENT_EXTRA_ACTIVITY_NAME, componentName.className)

        sendDefaultIntentExplicitly(context, intent)
    }

    override val supportLaunchers: List<String>
        get() = mutableListOf(
            "fr.neamar.kiss",
            "com.quaap.launchtime",
            "com.quaap.launchtime_official"
        )

    fun isSupported(context: Context): Boolean {
        val intent = Intent(INTENT_ACTION)
        return resolveBroadcast(context, intent).size > 0
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && resolveBroadcast(
            context,
            Intent(IntentConstants.Companion.DEFAULT_OREO_INTENT_ACTION)
        ).size > 0)
    }

    companion object {
        private val INTENT_ACTION: String = IntentConstants.Companion.DEFAULT_INTENT_ACTION
        private const val INTENT_EXTRA_BADGE_COUNT = "badge_count"
        private const val INTENT_EXTRA_PACKAGENAME = "badge_count_package_name"
        private const val INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name"
    }
}

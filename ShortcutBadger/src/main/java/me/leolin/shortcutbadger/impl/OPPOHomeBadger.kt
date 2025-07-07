package me.leolin.shortcutbadger.impl

import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import me.leolin.shortcutbadger.Badger
import me.leolin.shortcutbadger.ShortcutBadgeException
import me.leolin.shortcutbadger.util.BroadcastHelper.sendIntentExplicitly

/**
 * Created by NingSo on 2016/10/14.上午10:09
 *
 * @author: NingSo
 * Email: ningso.ping@gmail.com
 */
class OPPOHomeBadger : Badger {
    private var mCurrentTotalCount = -1

    @Throws(ShortcutBadgeException::class)
    override fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int) {
        if (mCurrentTotalCount == badgeCount) {
            return
        }
        mCurrentTotalCount = badgeCount
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            executeBadgeByContentProvider(context, badgeCount)
        } else {
            executeBadgeByBroadcast(context, componentName, badgeCount)
        }
    }

    override val supportLaunchers: List<String>
        get() = listOf("com.oppo.launcher")

    @Throws(ShortcutBadgeException::class)
    private fun executeBadgeByBroadcast(
        context: Context, componentName: ComponentName,
        badgeCount: Int
    ) {
        var badgeCount = badgeCount
        if (badgeCount == 0) {
            badgeCount = -1
        }
        val intent = Intent(INTENT_ACTION)
        intent.putExtra(INTENT_EXTRA_PACKAGENAME, componentName.packageName)
        intent.putExtra(INTENT_EXTRA_BADGE_COUNT, badgeCount)
        intent.putExtra(INTENT_EXTRA_BADGE_UPGRADENUMBER, badgeCount)

        sendIntentExplicitly(context, intent)
    }

    /**
     * Send request to OPPO badge content provider to set badge in OPPO home launcher.
     *
     * @param context       the context to use
     * @param badgeCount    the badge count
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Throws(ShortcutBadgeException::class)
    private fun executeBadgeByContentProvider(context: Context, badgeCount: Int) {
        try {
            val extras = Bundle()
            extras.putInt(INTENT_EXTRA_BADGEUPGRADE_COUNT, badgeCount)
            context.contentResolver.call(
                Uri.parse(PROVIDER_CONTENT_URI),
                "setAppBadgeCount",
                null,
                extras
            )
        } catch (ignored: Throwable) {
            throw ShortcutBadgeException("Unable to execute Badge By Content Provider")
        }
    }

    companion object {
        private const val PROVIDER_CONTENT_URI = "content://com.android.badge/badge"
        private const val INTENT_ACTION = "com.oppo.unsettledevent"
        private const val INTENT_EXTRA_PACKAGENAME = "pakeageName"
        private const val INTENT_EXTRA_BADGE_COUNT = "number"
        private const val INTENT_EXTRA_BADGE_UPGRADENUMBER = "upgradeNumber"
        private const val INTENT_EXTRA_BADGEUPGRADE_COUNT = "app_badge_count"
    }
}
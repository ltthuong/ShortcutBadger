package me.leolin.shortcutbadger.util

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Build
import me.leolin.shortcutbadger.ShortcutBadgeException
import me.leolin.shortcutbadger.impl.IntentConstants

/**
 * Created by mahijazi on 17/05/16.
 */
object BroadcastHelper {
    fun resolveBroadcast(context: Context, intent: Intent?): List<ResolveInfo> {
        val packageManager = context.packageManager
        val receivers = packageManager.queryBroadcastReceivers(intent!!, 0)

        return receivers ?: emptyList()
    }

    @Throws(ShortcutBadgeException::class)
    fun sendIntentExplicitly(context: Context, intent: Intent) {
        val resolveInfos = resolveBroadcast(context, intent)

        if (resolveInfos.size == 0) {
            throw ShortcutBadgeException("unable to resolve intent: $intent")
        }

        for (info in resolveInfos) {
            val actualIntent = Intent(intent)

            if (info != null) {
                actualIntent.setPackage(info.resolvePackageName)
                context.sendBroadcast(actualIntent)
            }
        }
    }

    @Throws(ShortcutBadgeException::class)
    fun sendDefaultIntentExplicitly(context: Context, intent: Intent) {
        var oreoIntentSuccess = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val oreoIntent = Intent(intent)

            oreoIntent.setAction(IntentConstants.DEFAULT_OREO_INTENT_ACTION)

            try {
                sendIntentExplicitly(context, oreoIntent)
                oreoIntentSuccess = true
            } catch (e: ShortcutBadgeException) {
                oreoIntentSuccess = false
            }
        }

        if (oreoIntentSuccess) {
            return
        }

        // try pre-Oreo default intent
        sendIntentExplicitly(context, intent)
    }
}

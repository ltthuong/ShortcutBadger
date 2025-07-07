package me.leolin.shortcutbadger.impl

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import me.leolin.shortcutbadger.Badger
import me.leolin.shortcutbadger.ShortcutBadgeException
import me.leolin.shortcutbadger.util.BroadcastHelper

@Deprecated("XiaomiHomeBadger is deprecated")
class XiaomiHomeBadger : Badger {

    companion object {
        const val INTENT_ACTION = "android.intent.action.APPLICATION_MESSAGE_UPDATE"
        const val EXTRA_UPDATE_APP_COMPONENT_NAME = "android.intent.extra.update_application_component_name"
        const val EXTRA_UPDATE_APP_MSG_TEXT = "android.intent.extra.update_application_message_text"
    }

    private var resolveInfo: ResolveInfo? = null

    @Throws(ShortcutBadgeException::class)
    override fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int) {
        try {
            val miuiNotificationClass = Class.forName("android.app.MiuiNotification")
            val miuiNotification = miuiNotificationClass.getDeclaredConstructor().newInstance()
            val field = miuiNotification.javaClass.getDeclaredField("messageCount")
            field.isAccessible = true
            try {
                field.set(miuiNotification, if (badgeCount == 0) "" else badgeCount.toString())
            } catch (e: Exception) {
                field.set(miuiNotification, badgeCount)
            }
        } catch (e: Exception) {
            val localIntent = Intent(INTENT_ACTION).apply {
                putExtra(
                    EXTRA_UPDATE_APP_COMPONENT_NAME,
                    "${componentName.packageName}/${componentName.className}"
                )
                putExtra(EXTRA_UPDATE_APP_MSG_TEXT, if (badgeCount == 0) "" else badgeCount.toString())
            }

            try {
                BroadcastHelper.sendIntentExplicitly(context, localIntent)
            } catch (_: ShortcutBadgeException) {
                // Ignored
            }
        }

        if (Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)) {
            tryNewMiuiBadge(context, badgeCount)
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Throws(ShortcutBadgeException::class)
    private fun tryNewMiuiBadge(context: Context, badgeCount: Int) {
        if (resolveInfo == null) {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        resolveInfo?.let { info ->
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = Notification.Builder(context)
                .setContentTitle("")
                .setContentText("")
                .setSmallIcon(info.iconResource)

            val notification = builder.build()
            try {
                val field = notification.javaClass.getDeclaredField("extraNotification")
                val extraNotification = field.get(notification)
                val method = extraNotification.javaClass.getDeclaredMethod("setMessageCount", Int::class.javaPrimitiveType)
                method.invoke(extraNotification, badgeCount)
                notificationManager.notify(0, notification)
            } catch (e: Exception) {
                throw ShortcutBadgeException("not able to set badge", e)
            }
        }
    }

    override val supportLaunchers: List<String>
        get() = listOf(
            "com.miui.miuilite",
            "com.miui.home",
            "com.miui.miuihome",
            "com.miui.miuihome2",
            "com.miui.mihome",
            "com.miui.mihome2",
            "com.i.miui.launcher"
        )
}

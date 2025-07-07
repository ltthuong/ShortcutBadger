package me.leolin.shortcutbadger.impl

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import me.leolin.shortcutbadger.Badger
import me.leolin.shortcutbadger.ShortcutBadgeException

/**
 * @author Nikolay Pakhomov
 * created 16/04/2018
 */
class YandexLauncherBadger : Badger {
    @Throws(ShortcutBadgeException::class)
    override fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int) {
        val extras = Bundle()
        extras.putString(COLUMN_CLASS, componentName.className)
        extras.putString(COLUMN_PACKAGE, componentName.packageName)
        extras.putString(COLUMN_BADGES_COUNT, badgeCount.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            context.contentResolver.call(CONTENT_URI, METHOD_TO_CALL, null, extras)
        }
    }

    override val supportLaunchers: List<String>
        get() = listOf(PACKAGE_NAME)

    companion object {
        const val PACKAGE_NAME: String = "com.yandex.launcher"

        private const val AUTHORITY = "com.yandex.launcher.badges_external"
        private val CONTENT_URI: Uri = Uri.parse("content://" + AUTHORITY)
        private const val METHOD_TO_CALL = "setBadgeNumber"

        private const val COLUMN_CLASS = "class"
        private const val COLUMN_PACKAGE = "package"
        private const val COLUMN_BADGES_COUNT = "badges_count"

        fun isVersionSupported(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                try {
                    context.contentResolver.call(CONTENT_URI, "", null, null)
                    return true
                } catch (e: IllegalArgumentException) {
                    return false
                }
            }
            return false
        }
    }
}

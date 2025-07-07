package me.leolin.shortcutbadger.impl

import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import me.leolin.shortcutbadger.Badger
import me.leolin.shortcutbadger.ShortcutBadgeException

/**
 * @author Radko Roman
 * @since  13.04.17.
 */
class EverythingMeHomeBadger : Badger {
    @Throws(ShortcutBadgeException::class)
    override fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int) {
        val contentValues = ContentValues()
        contentValues.put(COLUMN_PACKAGE_NAME, componentName.packageName)
        contentValues.put(COLUMN_ACTIVITY_NAME, componentName.className)
        contentValues.put(COLUMN_COUNT, badgeCount)
        context.contentResolver.insert(Uri.parse(CONTENT_URI), contentValues)
    }

    override val supportLaunchers: List<String>
        get() = mutableListOf("me.everything.launcher")

    companion object {
        private const val CONTENT_URI = "content://me.everything.badger/apps"
        private const val COLUMN_PACKAGE_NAME = "package_name"
        private const val COLUMN_ACTIVITY_NAME = "activity_name"
        private const val COLUMN_COUNT = "count"
    }
}

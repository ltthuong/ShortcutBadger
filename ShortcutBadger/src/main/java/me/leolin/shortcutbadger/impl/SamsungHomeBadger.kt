package me.leolin.shortcutbadger.impl

import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import me.leolin.shortcutbadger.Badger
import me.leolin.shortcutbadger.ShortcutBadgeException
import me.leolin.shortcutbadger.util.CloseHelper.close

/**
 * @author Leo Lin
 */
class SamsungHomeBadger : Badger {
    private var defaultBadger: DefaultBadger? = null

    init {
        if (Build.VERSION.SDK_INT >= 21) {
            defaultBadger = DefaultBadger()
        }
    }

    @Throws(ShortcutBadgeException::class)
    override fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int) {
        if (defaultBadger != null && defaultBadger!!.isSupported(context)) {
            defaultBadger!!.executeBadge(context, componentName, badgeCount)
        } else {
            val mUri = Uri.parse(CONTENT_URI)
            val contentResolver = context.contentResolver
            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(
                    mUri,
                    CONTENT_PROJECTION,
                    "package=?",
                    arrayOf(componentName.packageName),
                    null
                )
                if (cursor != null) {
                    val entryActivityName = componentName.className
                    var entryActivityExist = false
                    while (cursor.moveToNext()) {
                        val id = cursor.getInt(0)
                        val contentValues = getContentValues(componentName, badgeCount, false)
                        contentResolver.update(mUri, contentValues, "_id=?", arrayOf(id.toString()))
                        if (entryActivityName == cursor.getString(cursor.getColumnIndex("class"))) {
                            entryActivityExist = true
                        }
                    }

                    if (!entryActivityExist) {
                        val contentValues = getContentValues(componentName, badgeCount, true)
                        contentResolver.insert(mUri, contentValues)
                    }
                }
            } finally {
                close(cursor)
            }
        }
    }

    private fun getContentValues(
        componentName: ComponentName,
        badgeCount: Int,
        isInsert: Boolean
    ): ContentValues {
        val contentValues = ContentValues()
        if (isInsert) {
            contentValues.put("package", componentName.packageName)
            contentValues.put("class", componentName.className)
        }

        contentValues.put("badgecount", badgeCount)

        return contentValues
    }

    override val supportLaunchers: List<String>
        get() = mutableListOf(
            "com.sec.android.app.launcher",
            "com.sec.android.app.twlauncher"
        )

    companion object {
        private const val CONTENT_URI = "content://com.sec.badge/apps?notify=true"
        private val CONTENT_PROJECTION = arrayOf("_id", "class")
    }
}

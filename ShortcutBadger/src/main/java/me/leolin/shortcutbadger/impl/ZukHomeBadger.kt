package me.leolin.shortcutbadger.impl

import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import me.leolin.shortcutbadger.Badger
import me.leolin.shortcutbadger.ShortcutBadgeException

/**
 * Created by wuxuejian on 2016/10/9.
 * 需在设置 -- 通知和状态栏 -- 应用角标管理 中开启应用
 */
class ZukHomeBadger : Badger {
    private val CONTENT_URI: Uri = Uri.parse("content://com.android.badge/badge")

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Throws(ShortcutBadgeException::class)
    override fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int) {
        val extra = Bundle()
        extra.putInt("app_badge_count", badgeCount)
        context.contentResolver.call(CONTENT_URI, "setAppBadgeCount", null, extra)
    }

    override val supportLaunchers: List<String>
        get() = listOf("com.zui.launcher")
}

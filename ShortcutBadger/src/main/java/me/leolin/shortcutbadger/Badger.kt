package me.leolin.shortcutbadger

import android.content.ComponentName
import android.content.Context

interface Badger {

    /**
     * Called when user attempts to update notification count
     * @param context Caller context
     * @param componentName Component containing package and class name of calling application's
     * launcher activity
     * @param badgeCount Desired notification count
     * @throws ShortcutBadgeException
     */
    @Throws(ShortcutBadgeException::class)
    fun executeBadge(context: Context, componentName: ComponentName, badgeCount: Int)

    /**
     * Called to let [ShortcutBadger] knows which launchers are supported by this badger.
     * @return List containing supported launchers package names
     */
    val supportLaunchers: List<String>
}

package me.leolin.shortcutbadger

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import me.leolin.shortcutbadger.ShortcutBadgeException
import me.leolin.shortcutbadger.impl.AdwHomeBadger
import me.leolin.shortcutbadger.impl.ApexHomeBadger
import me.leolin.shortcutbadger.impl.AsusHomeBadger
import me.leolin.shortcutbadger.impl.DefaultBadger
import me.leolin.shortcutbadger.impl.EverythingMeHomeBadger
import me.leolin.shortcutbadger.impl.HuaweiHomeBadger
import me.leolin.shortcutbadger.impl.NewHtcHomeBadger
import me.leolin.shortcutbadger.impl.NovaHomeBadger
import me.leolin.shortcutbadger.impl.OPPOHomeBadger
import me.leolin.shortcutbadger.impl.SamsungHomeBadger
import me.leolin.shortcutbadger.impl.SonyHomeBadger
import me.leolin.shortcutbadger.impl.VivoHomeBadger
import me.leolin.shortcutbadger.impl.YandexLauncherBadger
import me.leolin.shortcutbadger.impl.ZTEHomeBadger
import me.leolin.shortcutbadger.impl.ZukHomeBadger
import java.util.Collections
import java.util.LinkedList
import kotlin.concurrent.Volatile

/**
 * @author Leo Lin
 */
object ShortcutBadger {
    private const val LOG_TAG = "ShortcutBadger"
    private const val SUPPORTED_CHECK_ATTEMPTS = 3

    private val BADGERS: MutableList<Class<out Badger?>> = LinkedList()

    @Volatile
    private var sIsBadgeCounterSupported: Boolean? = null
    private val sCounterSupportedLock = Any()

    init {
        BADGERS.addAll(
            listOf(
                AdwHomeBadger::class.java,
                ApexHomeBadger::class.java,
                DefaultBadger::class.java,
                NewHtcHomeBadger::class.java,
                NovaHomeBadger::class.java,
                SonyHomeBadger::class.java,
                AsusHomeBadger::class.java,
                HuaweiHomeBadger::class.java,
                OPPOHomeBadger::class.java,
                SamsungHomeBadger::class.java,
                ZukHomeBadger::class.java,
                VivoHomeBadger::class.java,
                ZTEHomeBadger::class.java,
                EverythingMeHomeBadger::class.java,
                YandexLauncherBadger::class.java,
            )
        )
    }

    private var sShortcutBadger: Badger? = null
    private var sComponentName: ComponentName? = null

    /**
     * Tries to update the notification count
     *
     * @param context    Caller context
     * @param badgeCount Desired badge count
     * @return true in case of success, false otherwise
     */
    fun applyCount(context: Context, badgeCount: Int): Boolean {
        try {
            applyCountOrThrow(context, badgeCount)
            return true
        } catch (e: ShortcutBadgeException) {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                Log.d(LOG_TAG, "Unable to execute badge", e)
            }
            return false
        }
    }

    /**
     * Tries to update the notification count, throw a [ShortcutBadgeException] if it fails
     *
     * @param context    Caller context
     * @param badgeCount Desired badge count
     */
    @Throws(ShortcutBadgeException::class)
    fun applyCountOrThrow(context: Context, badgeCount: Int) {
        if (sShortcutBadger == null) {
            val launcherReady = initBadger(context)

            if (!launcherReady) throw ShortcutBadgeException("No default launcher available")
        }

        try {
            sComponentName?.let { sShortcutBadger!!.executeBadge(context, it, badgeCount) }
        } catch (e: Exception) {
            throw ShortcutBadgeException("Unable to execute badge", e)
        }
    }

    /**
     * Tries to remove the notification count
     *
     * @param context Caller context
     * @return true in case of success, false otherwise
     */
    fun removeCount(context: Context): Boolean {
        return applyCount(context, 0)
    }

    /**
     * Tries to remove the notification count, throw a [ShortcutBadgeException] if it fails
     *
     * @param context Caller context
     */
    @Throws(ShortcutBadgeException::class)
    fun removeCountOrThrow(context: Context) {
        applyCountOrThrow(context, 0)
    }

    /**
     * Whether this platform launcher supports shortcut badges. Doing this check causes the side
     * effect of resetting the counter if it's supported, so this method should be followed by
     * a call that actually sets the counter to the desired value, if the counter is supported.
     */
    fun isBadgeCounterSupported(context: Context): Boolean {
        // Checking outside synchronized block to avoid synchronization in the common case (flag
        // already set), and improve perf.
        if (sIsBadgeCounterSupported == null) {
            synchronized(sCounterSupportedLock) {
                // Checking again inside synch block to avoid setting the flag twice.
                if (sIsBadgeCounterSupported == null) {
                    var lastErrorMessage: String? = null
                    for (i in 0..<SUPPORTED_CHECK_ATTEMPTS) {
                        try {
                            Log.i(
                                LOG_TAG, "Checking if platform supports badge counters, attempt "
                                        + String.format("%d/%d.", i + 1, SUPPORTED_CHECK_ATTEMPTS)
                            )
                            if (initBadger(context)) {
                                sComponentName?.let {
                                    sShortcutBadger!!.executeBadge(context,
                                        it, 0)
                                }
                                sIsBadgeCounterSupported = true
                                Log.i(LOG_TAG, "Badge counter is supported in this platform.")
                                break
                            } else {
                                lastErrorMessage = "Failed to initialize the badge counter."
                            }
                        } catch (e: Exception) {
                            // Keep retrying as long as we can. No need to dump the stack trace here
                            // because this error will be the norm, not exception, for unsupported
                            // platforms. So we just save the last error message to display later.
                            lastErrorMessage = e.message
                        }
                    }

                    if (sIsBadgeCounterSupported == null) {
                        Log.w(
                            LOG_TAG, "Badge counter seems not supported for this platform: "
                                    + lastErrorMessage
                        )
                        sIsBadgeCounterSupported = false
                    }
                }
            }
        }
        return sIsBadgeCounterSupported!!
    }

    /**
     * @param context      Caller context
     * @param notification
     * @param badgeCount
     */
    fun applyNotification(context: Context?, notification: Notification, badgeCount: Int) {
        if (Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)) {
            try {
                val field = notification.javaClass.getDeclaredField("extraNotification")
                val extraNotification = field[notification]
                val method = extraNotification.javaClass.getDeclaredMethod(
                    "setMessageCount",
                    Int::class.javaPrimitiveType
                )
                method.invoke(extraNotification, badgeCount)
            } catch (e: Exception) {
                if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                    Log.d(LOG_TAG, "Unable to execute badge", e)
                }
            }
        }
    }

    // Initialize Badger if a launcher is availalble (eg. set as default on the device)
    // Returns true if a launcher is available, in this case, the Badger will be set and sShortcutBadger will be non null.
    private fun initBadger(context: Context): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (launchIntent == null) {
            Log.e(LOG_TAG, "Unable to find launch intent for package " + context.packageName)
            return false
        }

        sComponentName = launchIntent.component

        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfos =
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        //Turns out framework does not guarantee to put DEFAULT Activity on top of the list.
        val resolveInfoDefault =
            context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        validateInfoList(resolveInfoDefault, resolveInfos)

        for (resolveInfo in resolveInfos) {
            val currentHomePackage = resolveInfo.activityInfo.packageName

            for (badger in BADGERS) {
                var shortcutBadger: Badger? = null
                try {
                    shortcutBadger = badger.newInstance()
                } catch (ignored: Exception) {
                }
                if (shortcutBadger != null && shortcutBadger.supportLaunchers!!.contains(
                        currentHomePackage
                    )
                ) {
                    if (isLauncherVersionSupported(context, currentHomePackage)) {
                        sShortcutBadger = shortcutBadger
                    }
                    break
                }
            }
            if (sShortcutBadger != null) {
                break
            }
        }

        if (sShortcutBadger == null) {
            if (Build.MANUFACTURER.equals("ZUK", ignoreCase = true)) sShortcutBadger =
                ZukHomeBadger()
            else if (Build.MANUFACTURER.equals("OPPO", ignoreCase = true)) sShortcutBadger =
                OPPOHomeBadger()
            else if (Build.MANUFACTURER.equals("VIVO", ignoreCase = true)) sShortcutBadger =
                VivoHomeBadger()
            else if (Build.MANUFACTURER.equals("ZTE", ignoreCase = true)) sShortcutBadger =
                ZTEHomeBadger()
            else sShortcutBadger = DefaultBadger()
        }

        return true
    }

    /**
     * Making sure that launcher version that yet doesn't support badges mechanism
     * is **NOT** used by ***sShortcutBadger***.
     */
    private fun isLauncherVersionSupported(context: Context, currentHomePackage: String): Boolean {
        if (YandexLauncherBadger.PACKAGE_NAME != currentHomePackage) {
            return true
        }
        return YandexLauncherBadger.isVersionSupported(context)
    }

    /**
     * Making sure the default Home activity is on top of the returned list
     * @param defaultActivity       default Home activity
     * @param resolveInfos          list of all Home activities in the system
     */
    private fun validateInfoList(defaultActivity: ResolveInfo, resolveInfos: List<ResolveInfo>) {
        var indexToSwapWith = 0
        var i = 0
        val resolveInfosSize = resolveInfos.size
        while (i < resolveInfosSize) {
            val resolveInfo = resolveInfos[i]
            val currentActivityName = resolveInfo.activityInfo.packageName
            if (currentActivityName == defaultActivity.activityInfo.packageName) {
                indexToSwapWith = i
            }
            i++
        }
        Collections.swap(resolveInfos, 0, indexToSwapWith)
    }
}

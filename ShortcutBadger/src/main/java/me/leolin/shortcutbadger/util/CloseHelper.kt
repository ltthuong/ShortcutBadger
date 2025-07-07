package me.leolin.shortcutbadger.util

import android.database.Cursor
import java.io.Closeable
import java.io.IOException

/**
 * @author leolin
 */
object CloseHelper {
    fun close(cursor: Cursor?) {
        if (cursor != null && !cursor.isClosed) {
            cursor.close()
        }
    }


    fun closeQuietly(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (var2: IOException) {
        }
    }
}

package me.leolin.shortcutbadger

class ShortcutBadgeException : Exception {
    constructor(message: String?) : super(message)

    constructor(message: String?, e: Exception?) : super(message, e)
}

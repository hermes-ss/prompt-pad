package com.hermes.promptpad

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.mutableStateListOf

enum class HubKind { MESSAGE, CALL, EMAIL, APP }

data class HubItem(
    val key: String,
    val pkg: String,
    val title: String,
    val text: String,
    val postedAt: Long,
    val kind: HubKind,
    val reply: Pair<PendingIntent, RemoteInput>?,
    var flagged: Boolean = false
)

/** ponytail: one in-memory list owned by the service; the Hub UI is only alive while the app is. */
class HubListener : NotificationListenerService() {

    override fun onListenerConnected() {
        items.clear()
        activeNotifications?.forEach { add(it) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) = add(sbn)

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        items.removeAll { it.key == sbn.key }
    }

    private fun add(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        val n = sbn.notification
        val x = n.extras
        val title = x.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        val text = x.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        items.removeAll { it.key == sbn.key }
        items.add(0, HubItem(sbn.key, sbn.packageName, title, text, sbn.postTime, kindOf(sbn.packageName, n), replyOf(n)))
    }

    companion object {
        val items = mutableStateListOf<HubItem>()

        fun isEnabled(ctx: Context): Boolean =
            (android.provider.Settings.Secure.getString(ctx.contentResolver, "enabled_notification_listeners") ?: "")
                .contains(ctx.packageName)

        fun openSettings(ctx: Context) {
            ctx.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        fun kindOf(pkg: String, n: Notification): HubKind = when {
            n.category == Notification.CATEGORY_CALL || n.category == Notification.CATEGORY_MISSED_CALL -> HubKind.CALL
            n.category == Notification.CATEGORY_EMAIL || pkg.contains("mail") -> HubKind.EMAIL
            n.category == Notification.CATEGORY_MESSAGE || pkg.contains("mms") || pkg.contains("messag") -> HubKind.MESSAGE
            else -> HubKind.APP
        }

        fun replyOf(n: Notification): Pair<PendingIntent, RemoteInput>? {
            n.actions?.forEach { a ->
                a.remoteInputs?.firstOrNull { it.allowFreeFormInput }?.let { return a.actionIntent to it }
            }
            return null
        }

        /** Sends [text] back through the notification's RemoteInput and drops the row. */
        fun sendReply(ctx: Context, item: HubItem, text: String): Boolean {
            val (pi, ri) = item.reply ?: return false
            val intent = Intent()
            RemoteInput.addResultsToIntent(arrayOf(ri), intent, android.os.Bundle().apply { putCharSequence(ri.resultKey, text) })
            return runCatching { pi.send(ctx, 0, intent); items.removeAll { it.key == item.key }; true }.getOrDefault(false)
        }
    }
}

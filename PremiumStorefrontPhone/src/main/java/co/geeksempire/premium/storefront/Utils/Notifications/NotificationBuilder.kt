/*
 * Copyright © 2021 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 5/30/21, 12:36 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package co.geeksempire.premium.storefront.Utils.Notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import co.geeksempire.premium.storefront.R

class NotificationBuilder (private val context: Context) {

    private val notificationManager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

    fun create(notificationChannelId: String = this@NotificationBuilder.javaClass.simpleName, notificationId: Int = 666,
               notificationTitle: String?, notificationContent: String?, notificationContentDone: String? = null,
               notificationColor: Int = context.getColor(R.color.default_color),
               notificationIntent: Intent? = null,
               notificationSilent: Boolean = false,
               notificationDone: Boolean = false) : Notification {

        val notificationBuilder = NotificationCompat.Builder(context, notificationChannelId)
        notificationBuilder.setTicker(notificationTitle)
        notificationBuilder.setContentTitle(notificationTitle?:context.getString(R.string.applicationName))
        notificationBuilder.setContentText(notificationContent?:context.getString(R.string.settingUpText))
        notificationBuilder.setSmallIcon(R.drawable.notification_icon)
        notificationBuilder.color = notificationColor
        notificationBuilder.setStyle(NotificationCompat.BigTextStyle()
            .bigText(notificationContent))

        if (notificationSilent) {
            notificationBuilder.setNotificationSilent()
        }

        if (notificationDone) {

            notificationBuilder.setContentText(notificationContentDone?:context.getString(R.string.doneText))
            notificationBuilder.setAutoCancel(true)
            notificationBuilder.setOngoing(false)

            notificationManager.notify(notificationId, notificationBuilder.build())

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notificationBuilder.setChannelId(notificationChannelId)

            val notificationChannel = NotificationChannel(
                notificationChannelId,
                context.getString(R.string.applicationName),
                NotificationManager.IMPORTANCE_HIGH)

            notificationManager.createNotificationChannel(notificationChannel)

        }

        notificationBuilder.setContentIntent(
            PendingIntent.getActivity(context, 111, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        )

        notificationManager.notify(notificationId, notificationBuilder.build())

        return notificationBuilder.build()

    }

}
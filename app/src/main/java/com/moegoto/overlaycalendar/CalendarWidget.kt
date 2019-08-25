package com.moegoto.overlaycalendar

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.ContentUris
import android.content.ContentValues.TAG
import android.content.Context
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.RemoteViews
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import java.text.SimpleDateFormat
import java.util.*


/**
 * Implementation of App Widget functionality.
 */
class CalendarWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        const val ACTION_ITEM_CLICK = "com.moegoto.overlaycalendar.ACTION_ITEM_CLICK"

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.calendar_widget)

            val remoteViewsFactoryIntent = Intent(context, CalendarUpdateService::class.java)
            views.setRemoteAdapter(R.id.date_list, remoteViewsFactoryIntent)

            // クリックイベントの登録
            val itemClickIntent = Intent(context, CalendarWidget::class.java)
            itemClickIntent.action = ACTION_ITEM_CLICK
            val itemClickPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                itemClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setPendingIntentTemplate(R.id.date_list, itemClickPendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (ACTION_ITEM_CLICK.equals(intent!!.action)) {

            // 選択した日付のカレンダーを起動
            var beginDate =
                SimpleDateFormat("yyyyMMdd").parse(intent!!.extras?.getString("targetDate"))
            var builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
            ContentUris.appendId(builder, beginDate.time);
            val intent = Intent(Intent.ACTION_VIEW, builder.build());
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
            context!!.startActivity(intent)

            // カレンダーの再読み込み
            val manager = AppWidgetManager.getInstance(context)
            val myWidget = ComponentName(context, CalendarWidget::class.java)
            val appWidgetIds = manager.getAppWidgetIds(myWidget)
            manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.date_list)
        }

    }
}


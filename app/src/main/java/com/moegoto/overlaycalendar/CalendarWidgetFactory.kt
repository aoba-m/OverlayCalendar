package com.moegoto.overlaycalendar

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import java.util.*
import kotlin.collections.ArrayList
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import kotlin.coroutines.coroutineContext


class CalendarWidgetFactory : RemoteViewsService.RemoteViewsFactory {

    var dateList = ArrayList<Calendar>()
    var context: Context
    var eventMap: Map<String, List<CalendarUtils.EventRecord>>? = null

    constructor(ctx: Context) {
        this.context = ctx
    }


    override fun onCreate() {
        Log.v(TAG, "[onCreate]");
    }

    override fun onDestroy() {
        Log.v(TAG, "[onDestroy]");
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun onDataSetChanged() {
        dateList.clear()

        var offset: Long = 0
        for (i in 0..31) {
            var date = Date(Date().time + offset)
            var calendar = Calendar.getInstance()
            calendar.time = date
            dateList.add(calendar)
            offset += 1000 * 60 * 60 * 24
        }

        eventMap = CalendarUtils().getEvent(context)
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getViewAt(position: Int): RemoteViews? {
        Log.v(TAG, "[getViewAt]: $position")

        if (dateList.size <= 0) {
            return null
        }

        var calendar = dateList[position]
        var rv = RemoteViews("com.moegoto.overlaycalendar", R.layout.calendar_widget_row)

        // 日付をセットする
        rv.setTextViewText(R.id.date_text, SimpleDateFormat("d").format(calendar.time))
        if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
            rv.setTextColor(R.id.date_text, Color.parseColor("#FF8888"))
            rv.setTextColor(R.id.weekday_text, Color.parseColor("#FF8888"))
            rv.setTextViewText(R.id.weekday_text, "SUN")
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
            rv.setTextColor(R.id.date_text, Color.parseColor("#8888FF"))
            rv.setTextColor(R.id.weekday_text, Color.parseColor("#8888FF"))
            rv.setTextViewText(R.id.weekday_text, "SAT")
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == 2) {
            rv.setTextColor(R.id.date_text, Color.parseColor("#FFFFFF"))
            rv.setTextColor(R.id.weekday_text, Color.parseColor("#FFFFFF"))
            rv.setTextViewText(R.id.weekday_text, "MON")
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == 3) {
            rv.setTextColor(R.id.date_text, Color.parseColor("#FFFFFF"))
            rv.setTextColor(R.id.weekday_text, Color.parseColor("#FFFFFF"))
            rv.setTextViewText(R.id.weekday_text, "TUE")
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == 4) {
            rv.setTextColor(R.id.date_text, Color.parseColor("#FFFFFF"))
            rv.setTextColor(R.id.weekday_text, Color.parseColor("#FFFFFF"))
            rv.setTextViewText(R.id.weekday_text, "WED")
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == 5) {
            rv.setTextColor(R.id.date_text, Color.parseColor("#FFFFFF"))
            rv.setTextColor(R.id.weekday_text, Color.parseColor("#FFFFFF"))
            rv.setTextViewText(R.id.weekday_text, "THU")
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == 6) {
            rv.setTextColor(R.id.date_text, Color.parseColor("#FFFFFF"))
            rv.setTextColor(R.id.weekday_text, Color.parseColor("#FFFFFF"))
            rv.setTextViewText(R.id.weekday_text, "FRI")
        }
        // 予定をセットする
        var dateKey = SimpleDateFormat("yyyyMMdd").format(calendar.time)
        if (eventMap != null && eventMap!!.containsKey(dateKey)) {
            val events = eventMap!![dateKey]
            var sb = StringBuilder()
            for (event in events!!) {
                if (sb.length > 0) {
                    sb.append("\n")
                }
                sb.append(event.title)
            }
            rv.setTextViewText(R.id.plan_text, sb.toString());
        } else {
            rv.setTextViewText(R.id.plan_text, "");
        }

        val intent = Intent()
        intent.putExtra("targetDate", dateKey);
        rv.setOnClickFillInIntent(R.id.date_row, intent);

        return rv
    }

    override fun getCount(): Int {
        return dateList.size
    }

    override fun getViewTypeCount(): Int {
        return 1
    }
}
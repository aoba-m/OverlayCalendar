package com.moegoto.overlaycalendar

import android.text.format.DateUtils
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.content.ContentResolver
import android.content.ContentValues.TAG

class CalendarUtils {
    private val CALENDAR_URL = "content://com.android.calendar/"

    fun getEvent(context: Context): Map<String, List<EventRecord>>? {
        try {
            val contentResolver = context.contentResolver
            val calendarIdList = ArrayList<CalendarRecord>()
            run {
                val builder = Uri.parse(CALENDAR_URL + "instances/when").buildUpon()
                val now = Date().time
                ContentUris.appendId(builder, now - DateUtils.WEEK_IN_MILLIS)
                ContentUris.appendId(builder, now + DateUtils.WEEK_IN_MILLIS * 4)
                // カレンダーの取得
                val cursor = contentResolver.query(
                    Uri.parse(CALENDAR_URL + "calendars"),
                    arrayOf<String>(
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.NAME,
                        CalendarContract.Calendars.VISIBLE,
                        CalendarContract.Calendars.CALENDAR_TIME_ZONE
                    ),
                    null, null, null
                )
                while (cursor!!.moveToNext()) {
                    val calendar = CalendarRecord()
                    calendar._id = cursor!!.getString(0)
                    calendar.displayName = cursor!!.getString(1)
                    calendar.selected = cursor!!.getInt(2)
                    calendar.timezone = cursor!!.getString(3)
                    calendarIdList.add(calendar)
                }
                cursor!!.close()
            }
            // 予定の取得
            val eventList = ArrayList<EventRecord>()
            for (calendar in calendarIdList) {
                val builder = Uri.parse(CALENDAR_URL + "instances/when").buildUpon()
                val now = Date().time
                ContentUris.appendId(builder, now - DateUtils.WEEK_IN_MILLIS)
                ContentUris.appendId(builder, now + DateUtils.WEEK_IN_MILLIS * 4)
                var eventCursor = contentResolver.query(
                    builder.build(),
                    arrayOf<String>("_id", "event_id", "title", "begin", "end", "allDay"),
                    "calendar_id=" + calendar._id, null,
                    "startDay ASC, startMinute ASC"
                )
                while (eventCursor!!.moveToNext()) {
                    val event = EventRecord()
                    event.calendar = calendar
                    event._id = eventCursor!!.getInt(0)
                    event.event_id = eventCursor!!.getInt(1)
                    event.title = eventCursor!!.getString(2)
                    event.begin = Date(eventCursor!!.getLong(3))
                    event.end = Date(eventCursor!!.getLong(4))
                    event.allDay = !eventCursor!!.getString(5).equals("0")
                    if (event.allDay!!) {
                        event.end = Date(event.end!!.time - DateUtils.DAY_IN_MILLIS)
                    }
                    eventList.add(event)
                }
                eventCursor!!.close()
            }
            val map = HashMap<String, ArrayList<EventRecord>>()
            for (event in eventList) {
                val builder = Uri.parse(CALENDAR_URL + "events").buildUpon()
                val eventCursor = contentResolver.query(
                    builder.build(),
                    arrayOf<String>("title", "dtstart", "eventTimezone"),
                    "view_events._id=" + event.event_id, null,
                    ""
                )
                while (eventCursor!!.moveToNext()) {
                    pushToMap(map, event)
                }
                eventCursor!!.close()
            }
            return map
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return null
        }
    }

    private fun pushToMap(map: MutableMap<String, ArrayList<EventRecord>>, event: EventRecord) {
        val format = SimpleDateFormat("yyyyMMdd")
        for (offset in 0..13) {
            val offsetStartDate =
                format.format(Date(event.begin!!.getTime() + DateUtils.DAY_IN_MILLIS * offset))
            val endDate = format.format(event.end!!.getTime())
            if (offsetStartDate.compareTo(endDate) <= 0) {
                var list = map[offsetStartDate]
                if (list == null) {
                    list = ArrayList()
                    map[offsetStartDate] = list
                }
                list.add(event)
            }
        }
    }

    class CalendarRecord {
        var _id: String? = null
        var displayName: String? = null
        var selected: Int = 0
        var timezone: String? = null
    }

    class EventRecord {
        var calendar: CalendarRecord? = null
        var _id: Int = 0
        var event_id: Int = 0
        var title: String? = null
        var begin: Date? = null
        var end: Date? = null
        var allDay: Boolean? = null
    }
}
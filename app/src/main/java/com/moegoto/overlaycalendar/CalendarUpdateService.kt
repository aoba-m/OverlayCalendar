package com.moegoto.overlaycalendar

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViewsService

class CalendarUpdateService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return CalendarWidgetFactory(applicationContext)
    }

}

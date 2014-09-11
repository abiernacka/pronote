/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pronote.application.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import pronote.application.MainActivity;
import pronote.application.R;
import pronote.application.db.NotesDbAdapter;
import pronote.application.widget.ProNoteWidgetProvider;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.


/**
 * @author Agnieszka Żelezik i Jerzy Biernacki
 * @brief Klasa odpowiedzialna za wyświetlenie powiadomienia.
 * <p/>
 * Reaguje na wywołanie alarmu i tworzy odpowiedni obiekt klasy Notification informujący o nadejściu ustawionego terminu wyświetlenia przypomnienia.<br>
 */
public class OneShotAlarm extends BroadcastReceiver {
    private NotesDbAdapter mDbHelper;

    /**
     * @brief Metoda tworzy odpowiedni obiekt klasy Notification informujący o nadejściu ustawionego terminu wyświetlenia przypomnienia
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        //Update Widgetow
//      Intent widgetUpdateIntent = new Intent(ProNoteWidgetProvider.UPDATE_WIDGETS);
//      context.sendBroadcast(widgetUpdateIntent);

        Intent widgetUpdateIntent = new Intent(ProNoteWidgetProvider.UPDATE_WIDGETS);
        context.sendBroadcast(widgetUpdateIntent);
        String mTitleText = "";
        mDbHelper = new NotesDbAdapter(context);
        mDbHelper.open();
        Long mRowId = intent.getLongExtra(NotesDbAdapter.KEY_ROWID, -1);
        if (mRowId != null && mRowId != 0L) {
            Log.d("AGA", "rowid=" + mRowId);
            Cursor note = mDbHelper.fetchNote(mRowId);
            mTitleText = note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
        }
        ///////////////

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notif = new Notification(R.drawable.ic_launcher, mTitleText, System.currentTimeMillis());
        Intent previewIntent = new Intent(context, MainActivity.class);
        previewIntent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId.longValue());
        PendingIntent appIntent = PendingIntent.getActivity(context, mRowId.intValue(), previewIntent, 0);

        Intent deleteIntent = new Intent(context, ClearNotificationReceiver.class);
        deleteIntent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId.longValue());
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context,
                mRowId.intValue(), deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.deleteIntent = deletePendingIntent;

        notif.defaults |= Notification.DEFAULT_ALL;
        notif.flags |= Notification.FLAG_SHOW_LIGHTS;

        notif.ledARGB = Color.RED;
        notif.ledOffMS = 300;
        notif.ledOnMS = 300;
        long[] vibrate = {0, 100, 200, 300};
        notif.vibrate = vibrate;
        notif.setLatestEventInfo(context, mTitleText, "ProNote", appIntent);
        nm.notify(mRowId.intValue(), notif);
        Toast.makeText(context, mTitleText, Toast.LENGTH_SHORT).show();
    }
}


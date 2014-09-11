package pronote.application.notifications;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import pronote.application.db.NotesDbAdapter;


/**
 * @author Agnieszka Żelezik i Jerzy Biernacki
 * @brief Klasa odpowiedzialna za usuwanie alarmów po wciśnięciu przycisku [Clear notifications].
 * <p/>
 * Reaguje na wciśnięcie przycisku [Clear notifications] umieszczonego w status barze<br>
 */
public class ClearNotificationReceiver extends BroadcastReceiver {

    /**
     * @brief Metoda reaguje na wciśnięcie przycisku [Clear notifications] umieszczonego w status barze
     */
    @Override
    public void onReceive(Context context, Intent receivedIntent) {
        Bundle extras = receivedIntent.getExtras();
        Long mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                : null;

        if (mRowId != null) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(mRowId.intValue());
            Intent intent = new Intent(context, OneShotAlarm.class);
            intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId.longValue());
            PendingIntent sender = PendingIntent.getBroadcast(context,
                    (int) mRowId.intValue(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            am.cancel(sender);
        }
    }

}

package pronote.application.notifications;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import pronote.application.db.NotesDbAdapter;

/**
 * @brief Klasa odpowiedzialna za ponowne ustawienie wszystkich powiadomień po restarcie systemu.<br>
 * 
 * Ustawia na nowo powiadomienia do wszystkich wydarzeń które powinny sie jeszcze odbyć w przyszłości.
 * @author Agnieszka Żelezik i Jerzy Biernacki
 */
public class RebootSetAlarms extends BroadcastReceiver {

	private NotesDbAdapter mDbHelper;
	
	/**
	 * @brief Metoda uruchamiana przez system Android po włączeniu systemu.
	 * 
	 * Ustawia na nowo powiadomienia do wszystkich wydarzeń które powinny sie jeszcze odbyć w przyszłości.
	 * 
	 * @param context Kontekst na którym klasa operuje
	 * @param intent Intencja
	 */
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		mDbHelper = new NotesDbAdapter(context);
        mDbHelper.open();
        Cursor mNotesCursor;
        mNotesCursor = mDbHelper.fetchAllNotes();
        mNotesCursor.moveToFirst();
        do
        {
          Long date = mNotesCursor.getLong(
                  			mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATE_TIME));
        	Long id=mNotesCursor.getLong(
        			mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_ROWID));
            Intent appIntent = new Intent(context, OneShotAlarm.class);
            appIntent.putExtra(NotesDbAdapter.KEY_ROWID, id.longValue());
            PendingIntent sender = PendingIntent.getBroadcast(context,
                    id.intValue(), appIntent, PendingIntent.FLAG_CANCEL_CURRENT );
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(date));
            if(calendar.getTimeInMillis() >= System.currentTimeMillis()) {
                AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
            }
        } while(mNotesCursor.moveToNext());
       // new ProNoteUpdater(context).run();
	}
}

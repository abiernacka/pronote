package pronote.application.widget;


import java.util.Calendar;

import pronote.application.MainActivity;
import pronote.application.R;
import pronote.application.db.NotesDbAdapter;
import pronote.application.utils.Formatter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;


/**
 * @brief Klasa odpowiedzialna za tworzenie i odświeżanie widgeta aplikacji
 * 
 * Zapewnia obsługę widgetu, tworzenie jego layoutu oraz wypełnianie go danymi.<br>
 * Zapewnie również jego odświeżanie po odebraniu broadcasta
 * @author Agnieszka Żelezik i Jerzy Biernacki
 */
public class ProNoteWidgetProvider extends AppWidgetProvider {

	
		/**
		 * @brief String przechowujący nazwe akcji broadcasta na którą klasa powinna reagować
		*/
		public static final String UPDATE_WIDGETS="android.appwidget.action.APPWIDGET_UPDATE";
		
		/**
		 * @brief Metoda odbierająca broadcast i odświeżająca dane widgeta
		*/
		@Override
		public void onReceive(Context context, Intent intent)
		{
			super.onReceive(context, intent);
			if(intent.getAction().equals(ProNoteWidgetProvider.UPDATE_WIDGETS))
			{
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				int [] ids = appWidgetManager.getAppWidgetIds(new ComponentName (context, ProNoteWidgetProvider.class));
				onUpdate(context, appWidgetManager,ids);
				
				if(ids.length>0) Log.println(3, "ProNote", "onReceive. ids[0]: "+ids[0]);
				else Log.println(3, "ProNote", "onReceive. Puste ids");
				
			}
		}
	
		/**
		 * @brief Metoda odświeżająca wszystkei widgety co 2 godziny
		*/
		@Override
	    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	        final int N = appWidgetIds.length;

	        // Perform this loop procedure for each App Widget that belongs to this provider
	        for (int i=0; i<N; i++) {
	            int appWidgetId = appWidgetIds[i];

	            // Create an Intent to launch ExampleActivity
	            Intent intent = new Intent(context, MainActivity.class);
	            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,0);

	            
	            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pronote_appwidget);
	            views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);

	            NotesDbAdapter mDbHelper = new NotesDbAdapter(context);
	            mDbHelper.open();
	            Cursor mNotesCursor;
	            mNotesCursor = mDbHelper.fetchAllNotes();
	            
	            if(mNotesCursor.moveToFirst())
	            {
	            	
	            	boolean end=false;
	            	
	            	while(!end)
	            	{
	            		String title = mNotesCursor.getString(mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
		        		  long time = mNotesCursor.getLong(mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATE_TIME));
		        		  Calendar c = Calendar.getInstance();
                  c.setTimeInMillis(time);
			            if(c.getTimeInMillis() > System.currentTimeMillis())
			            {
			            	views.setTextViewText(R.id.UpcomingEventText, Formatter.date(time) + " " + title);
			            	end=true;
			            }
			            else
			            {
			            	if(!mNotesCursor.moveToNext())
			            		{
			            			end=true;
			            			views.setTextViewText(R.id.UpcomingEventText, context.getString(R.string.no_events));
			            		}
			            }
	            	}
	            	
	            }
	            else
	            {
	            	views.setTextViewText(R.id.UpcomingEventText, context.getString(R.string.no_events));
	            }
	            
	            // Tell the AppWidgetManager to perform an update on the current app widget
	            appWidgetManager.updateAppWidget(appWidgetId, views);
	        }
	    }
	}
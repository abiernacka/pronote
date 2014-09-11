package pronote.application;

import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import pronote.application.db.NotesDbAdapter;
import pronote.application.fragment.ListFragment;
import pronote.application.fragment.PreviewFragment;
import pronote.application.model.Note;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        setProgressBarIndeterminateVisibility(false);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ListFragment())
                    .commit();
        }
        Long mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                    : null;
        }

        if (mRowId != null) {

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(mRowId.intValue());

            NotesDbAdapter mDbHelper = new NotesDbAdapter(this);
            mDbHelper.open();
            Cursor mNotesCursor = mDbHelper.fetchNote(mRowId);
            long rowId = mNotesCursor.getLong(mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_ROWID));
            String title = mNotesCursor.getString(mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
            String body = mNotesCursor.getString(mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
            String call = mNotesCursor.getString(mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_CALL_NUMBER));
            String sms = mNotesCursor.getString(mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_SMS_NUMBER));
            String smsText = mNotesCursor.getString(mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_SMS_TEXT));
            String recordPath = mNotesCursor.getString(mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_RECORD_PATH));
            long dateTime = mNotesCursor.getLong(mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATE_TIME));
            String photo = mNotesCursor.getString(mNotesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_PHOTO));

            Note note = new Note();
            note.setRowId(rowId);
            note.setTitle(title);
            note.setBody(body);
            note.setCallNumber(call);
            note.setSmsNumber(sms);
            note.setSmsText(smsText);
            note.setRecordPath(recordPath);
            note.setDateTime(dateTime);
            note.setPhoto(photo);
            PreviewFragment p = new PreviewFragment();
            p.setNote(note);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, p)
                    .addToBackStack(null)
                    .commit();
        }
        checkGooglePlayServices();

    }
    public void checkGooglePlayServices(){
        // Check status of Google Play Services
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // Check Google Play Service Available
        try{
            if (status != ConnectionResult.SUCCESS) {
                GooglePlayServicesUtil.getErrorDialog(status, this, 10).show();
            } else {


                final AdView mAdView = (AdView) findViewById(R.id.ad);
                mAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        mAdView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        super.onAdFailedToLoad(errorCode);
                        mAdView.setVisibility(View.GONE);
                    }
                });

                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }
        }
        catch (Exception e) {
            //
        }
    }
}

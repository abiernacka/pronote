package pronote.application;

import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import pronote.application.db.NotesDbAdapter;
import pronote.application.fragment.EditFragment;
import pronote.application.fragment.ListFragment;
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
        EditFragment editFragment = new EditFragment();
        editFragment.setNote(note);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, editFragment)
                .addToBackStack(null)
                .commit();
      }
    }
}

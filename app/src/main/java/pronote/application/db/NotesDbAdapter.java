package pronote.application.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import pronote.application.model.Note;

/**
 * Created by agnieszka on 29.08.14.
 */
public class NotesDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_CALL_NUMBER = "call_number";
    public static final String KEY_SMS_NUMBER = "sms_number";
    public static final String KEY_SMS_TEXT = "sms_text";
    public static final String KEY_RECORD_PATH = "record_path";
    public static final String KEY_DATE_TIME = "date_time";
    public static final String KEY_PHOTO = "photo";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_CREATE =
            "create table NotesData (_id integer primary key autoincrement, "
                    + "title text not null, body text not null, call_number text, sms_number text, sms_text text, record_path text, date_str text, time_str text, date_time date, photo text);";

    private static final String DATABASE_NAME = "ProNoteDB";
    private static final String DATABASE_TABLE = "NotesData";
    private static final int DATABASE_VERSION = 22;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    public NotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public NotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createNote(Note note) {
        ContentValues initialValues = new ContentValues();
        if (note.getRowId() != -1) {
          initialValues.put(KEY_ROWID, note.getRowId());
        }
        initialValues.put(KEY_TITLE, note.getTitle());
        initialValues.put(KEY_BODY, note.getBody());
        initialValues.put(KEY_CALL_NUMBER, note.getCallNumber());
        initialValues.put(KEY_SMS_NUMBER, note.getSmsNumber());
        initialValues.put(KEY_SMS_TEXT, note.getSmsText());
        initialValues.put(KEY_RECORD_PATH, note.getRecordPath());
        initialValues.put(KEY_DATE_TIME, note.getDateTime());
        initialValues.put(KEY_PHOTO, note.getPhoto());
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY,KEY_CALL_NUMBER,KEY_SMS_NUMBER,KEY_SMS_TEXT,KEY_RECORD_PATH,KEY_DATE_TIME,KEY_PHOTO}, null, null, null, null, KEY_DATE_TIME + " DESC");
    }

    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                                KEY_TITLE, KEY_BODY, KEY_CALL_NUMBER,KEY_SMS_NUMBER,KEY_SMS_TEXT,KEY_RECORD_PATH,KEY_DATE_TIME,KEY_PHOTO}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public boolean updateNote(Note note) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, note.getTitle());
        args.put(KEY_BODY, note.getBody());
        args.put(KEY_CALL_NUMBER, note.getCallNumber());
        args.put(KEY_SMS_NUMBER, note.getSmsNumber());
        args.put(KEY_SMS_TEXT, note.getSmsText());
        args.put(KEY_RECORD_PATH, note.getRecordPath());
        args.put(KEY_DATE_TIME, note.getDateTime());
        args.put(KEY_PHOTO, note.getPhoto());
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + note.getRowId(), null) > 0;
    }
}

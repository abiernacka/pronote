/**
 * Created by MiQUiDO on 29.08.14.
 *
 * Copyright 2014 MiQUiDO <http://www.miquido.com/>. All rights reserved.
 */
package pronote.application.fragment;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.Inflater;

import pronote.application.R;
import pronote.application.adapter.NotesAdapter;
import pronote.application.db.NotesDbAdapter;
import pronote.application.model.Note;
import pronote.application.view.EnhancedListView;

/**
 * TODO Add class description...
 *
 * @author miquido
 */
public class ListFragment extends Fragment {

  private NotesAdapter adapter;

    public ListFragment() {
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }


  @Override
  public void onViewCreated(View view, Bundle saveInstanceState) {
    super.onViewCreated(view, saveInstanceState);
    EnhancedListView listView = (EnhancedListView) view.findViewById(R.id.list);
    adapter = new NotesAdapter(getActivity().getLayoutInflater());

    listView.setOnItemClickListener(onItemClickListener);
    listView.setAdapter(adapter);
    listView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
                @Override
                public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {

                    final Note item = adapter.getItem(position);
                  new AsyncTask<Note, Void, Void>() {

                                  @Override
                                  protected Void doInBackground(Note... params) {
                                      NotesDbAdapter dbHelper = new NotesDbAdapter(getActivity());
                                      dbHelper.open();
                                      dbHelper.deleteNote(item.getRowId());
                                      return null;
                                  }

                                  @Override
                                  protected void onPostExecute(Void result) {
                                    List<Note> notes = adapter.getNotes();
                                    notes.remove(item);
                                    adapter.setNotes(notes);
                                    adapter.notifyDataSetChanged();
                                  }

                              }.execute(item);
                    return new EnhancedListView.Undoable() {
                        @Override
                        public void undo() {
                          new AsyncTask<Note, Void, Void>() {

                             @Override
                             protected Void doInBackground(Note... params) {
                               NotesDbAdapter dbHelper = new NotesDbAdapter(getActivity());
                               dbHelper.open();
                               dbHelper.createNote(item);
                                 return null;
                             }

                             @Override
                             protected void onPostExecute(Void result) {
                               List<Note> notes = adapter.getNotes();
                               notes.add(item);
                               adapter.setNotes(notes);
                               adapter.notifyDataSetChanged();
                             }

                         }.execute(item);
                        }
                    };
                }
            });
    listView.enableSwipeToDismiss();
    listView.setSwipeDirection(EnhancedListView.SwipeDirection.END);
    listView.setSwipingLayout(R.id.card_view);


    new AsyncTask<Void, Void, List<Note>>() {

      @Override
      protected List<Note> doInBackground(Void... params) {

        NotesDbAdapter dbHelper = new NotesDbAdapter(getActivity());
        dbHelper.open();
        Cursor cursor = dbHelper.fetchAllNotes();
        getActivity().startManagingCursor(cursor);
        List<Note> items = getItems(cursor);
        return items;
      }

      @Override
      protected void onPreExecute() {
          getActivity().setProgressBarIndeterminateVisibility(true);
      }

      @Override
      protected void onPostExecute(List<Note> result) {
        adapter.setNotes(result);
        adapter.notifyDataSetChanged();
        getActivity().setProgressBarIndeterminateVisibility(false);
      }
    }.execute();
  }

  private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      PreviewFragment previewFragment = new PreviewFragment();
      previewFragment.setNote(adapter.getItem(position));
      getActivity().getSupportFragmentManager().beginTransaction()
              .replace(R.id.container, previewFragment)
              .addToBackStack(null)
              .commit();
    }
  };

  private List<Note> getItems(Cursor mNotesCursor)
      {
        List<Note> list = new ArrayList<Note>();

      	mNotesCursor.moveToFirst();
      	for(int i = 0; i < mNotesCursor.getCount(); i++) {
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

          list.add(note);
      		mNotesCursor.moveToNext();
      	}

      	return list;

      }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            EditFragment editFragment = new EditFragment();
          Note newNote = new Note();
          newNote.setDateTime(new Date().getTime());
            editFragment.setNote(newNote);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, editFragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

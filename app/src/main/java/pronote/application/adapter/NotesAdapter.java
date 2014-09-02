/**
 * Created by MiQUiDO on 02.09.14.
 *
 * Copyright 2014 MiQUiDO <http://www.miquido.com/>. All rights reserved.
 */
package pronote.application.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pronote.application.R;
import pronote.application.model.Note;

/**
 * TODO Add class description...
 *
 * @author miquido
 */
public class NotesAdapter extends BaseAdapter {

  public static final String PATTERN_DATE = "dd-MM-yyyy";
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(PATTERN_DATE);

    private List<Note> notes;
    private LayoutInflater inflater;

    public NotesAdapter(LayoutInflater inflater) {
      this.inflater = inflater;
    }

  public List<Note> getNotes() {
    return notes;
  }

  public void setNotes(List<Note> notes) {
    this.notes = notes;
  }

  @Override
    public int getCount() {
        return notes == null ? 0 : notes.size();
    }

    @Override
    public Note getItem(int position) {
        return notes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return notes.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, container, false);
        }
        String title = getItem(position).getTitle();
        if (TextUtils.isEmpty(title)) {
          title = "-";
        }

        ((TextView) convertView.findViewById(R.id.textTitle))
                .setText(title);
        String date = "-";
        if (getItem(position).getDateTime() != 0) {
          date = DATE_FORMAT.format(new Date(getItem(position).getDateTime()));
        }
        ((TextView) convertView.findViewById(R.id.textDate))
                      .setText(date);
        return convertView;
    }
}
/**
 * Created by MiQUiDO on 02.09.14.
 *
 * Copyright 2014 MiQUiDO <http://www.miquido.com/>. All rights reserved.
 */
package pronote.application.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import pronote.application.R;
import pronote.application.model.Note;

/**
 * TODO Add class description...
 *
 * @author miquido
 */
public class NotesAdapter extends BaseAdapter {

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

        ((TextView) convertView.findViewById(R.id.textTitle))
                .setText(getItem(position).getTitle());
        return convertView;
    }
}
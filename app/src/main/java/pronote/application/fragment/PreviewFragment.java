/**
 * Created by MiQUiDO on 29.08.14.
 *
 * Copyright 2014 MiQUiDO <http://www.miquido.com/>. All rights reserved.
 */
package pronote.application.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import pronote.application.R;
import pronote.application.db.NotesDbAdapter;
import pronote.application.model.Note;
import pronote.application.utils.Formatter;
import pronote.application.widget.ProNoteWidgetProvider;

/**
 * TODO Add class description...
 *
 * @author miquido
 */
public class PreviewFragment extends Fragment {

    private static final String SERIALIZABLE_NOTE = "SERIALIZABLE_NOTE";

    private TextView editTextDate;
    private ImageButton buttonCall;
    private ImageButton buttonSMS;
    private ImageButton buttonPlay;
    private ImageView imagePhoto;
    private TextView editTextCall;
    private TextView editTextSMS;
    private TextView title;
    private TextView body;
    private TextView editTextSMSText;
    private MediaPlayer mediaPlayer = null;

    private Note note;

    public PreviewFragment() {
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        if (mediaPlayer != null) {
          mediaPlayer.stop();
        }
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            note = (Note) savedInstanceState.getSerializable(SERIALIZABLE_NOTE);
        }
        return inflater.inflate(R.layout.fragment_preview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle saveInstanceState) {
        super.onViewCreated(view, saveInstanceState);

        editTextDate = (TextView) view.findViewById(R.id.date);
        imagePhoto = (ImageView) view.findViewById(R.id.imagePhoto);
        editTextCall = (TextView) view.findViewById(R.id.editTextCall);
        editTextSMS = (TextView) view.findViewById(R.id.editTextSMS);
        buttonCall = (ImageButton) view.findViewById(R.id.buttonCall);
        buttonSMS = (ImageButton) view.findViewById(R.id.buttonSMS);
        buttonPlay = (ImageButton) view.findViewById(R.id.buttonPlay);
        title = (TextView) view.findViewById(R.id.title);
        body = (TextView) view.findViewById(R.id.body);
        editTextSMSText = (TextView) view.findViewById(R.id.editTextSMSText);

        setListeners();

        populateFields(view);
    }

    private void populateFields(View view ) {
        title.setText(note.getTitle());
        if (TextUtils.isEmpty(note.getTitle())) {
          title.setText(getString(R.string.untitled));
        }
        body.setText(note.getBody() + note.getDateTime());
        editTextCall.setText(String.format(getString(R.string.call_to), note.getCallNumber()));
        editTextSMS.setText(String.format(getString(R.string.send_sms_to), note.getCallNumber()));
        editTextSMSText.setText(String.format(getString(R.string.sms_content_label), note.getSmsText()));
        if (note.getDateTime() != 0) {
          editTextDate.setText(Formatter.date(note.getDateTime()));
        } else {
          editTextDate.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(note.getBody())) {
          body.setVisibility(View.GONE);
        } else {
          body.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(note.getCallNumber())) {
          view.findViewById(R.id.viewCall).setVisibility(View.GONE);
        } else {
          view.findViewById(R.id.viewCall).setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(note.getSmsNumber()) && TextUtils.isEmpty(note.getSmsText())) {
          view.findViewById(R.id.viewSMS).setVisibility(View.GONE);
        } else {
          view.findViewById(R.id.viewSMS).setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(note.getRecordPath())) {
          view.findViewById(R.id.viewRecord).setVisibility(View.GONE);
        } else {
          view.findViewById(R.id.viewRecord).setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(note.getPhoto())) {
          view.findViewById(R.id.imagePhoto).setVisibility(View.GONE);
        } else {
          view.findViewById(R.id.imagePhoto).setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeFile(note.getPhoto());
            imagePhoto.setImageBitmap(bitmap);
        }
    }

    private void setListeners() {
        imagePhoto.setOnClickListener(onPhotoImageClickListener);
        buttonSMS.setOnClickListener(onSMSBtClickListener);
        buttonCall.setOnClickListener(onCallBtClickListener);
        buttonPlay.setOnClickListener(onPlayBtClickListener);
    }

  private View.OnClickListener onPhotoImageClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
     showImage();
    }
  };

  private View.OnClickListener onCallBtClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+note.getCallNumber()));
        startActivity(dial);
    }
  };

  private View.OnClickListener onPlayBtClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
          mediaPlayer.stop();
          buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
        } else {
          mediaPlayer = new MediaPlayer();
          buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop));
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                  buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
                }

            });
              try {
                  mediaPlayer.setDataSource(note.getRecordPath());
                  mediaPlayer.prepare();
                  mediaPlayer.start();
              } catch (Exception e) {
                  mediaPlayer.stop();
                  buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
              }
        }
    }
  };

  private View.OnClickListener onSMSBtClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent sendSms = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + note.getSmsNumber()));
        sendSms.putExtra("sms_body", note.getSmsText());
        startActivity(sendSms);
    }
  };

    public void setNote(Note note) {
        this.note = note;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.preview, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
          EditFragment editFragment = new EditFragment();
          editFragment.setNote(note);
          getActivity().getSupportFragmentManager().beginTransaction()
                  .replace(R.id.container, editFragment)
                  .addToBackStack(null)
                  .commit();
            return true;
        } else if (id == R.id.action_discard) {
          new AsyncTask<Note, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    getActivity().setProgressBarIndeterminateVisibility(true);
                }

                @Override
                protected Void doInBackground(Note... params) {
                    NotesDbAdapter dbHelper = new NotesDbAdapter(getActivity());
                    dbHelper.open();
                    dbHelper.deleteNote(note.getRowId());
                    Intent widgetUpdateIntent = new Intent(ProNoteWidgetProvider.UPDATE_WIDGETS);
                    getActivity().sendBroadcast(widgetUpdateIntent);
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    getActivity().setProgressBarIndeterminateVisibility(false);
                    getActivity().onBackPressed();
                }
            }.execute(note);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        icicle.putSerializable(SERIALIZABLE_NOTE, note);
    }

  public void showImage() {
  		if(!checkSdCard()) {
  			Toast.makeText(getActivity(), getString(R.string.sd_card_error), Toast.LENGTH_LONG).show();
  			return;
  		}
  		if (note.getPhoto() != null && new File(note.getPhoto()).exists()) {
  			Intent intent = new Intent();
  			intent.setAction(android.content.Intent.ACTION_VIEW);
  			intent.setDataAndType(Uri.fromFile(new File(note.getPhoto())), "image/jpg");
        getActivity().startActivity(intent);
  		}
  	}
  	/**
  	 * @brief Sprawdza czy karta SD jest dostepna
  	 *
  	 * @return Zwraca prawdę jeśli karta SD jest dostepna, a false w wypadku przeciwnym
  	 */
  	public static boolean checkSdCard()
  	{
  		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
  	}
}

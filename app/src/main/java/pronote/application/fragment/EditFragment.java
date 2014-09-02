/**
 * Created by MiQUiDO on 29.08.14.
 *
 * Copyright 2014 MiQUiDO <http://www.miquido.com/>. All rights reserved.
 */
package pronote.application.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

import pronote.application.R;
import pronote.application.db.NotesDbAdapter;
import pronote.application.model.Note;

/**
 * TODO Add class description...
 *
 * @author miquido
 */
public class EditFragment extends Fragment {

    private static final int CAMERA_PIC_REQUEST = 1337;
    private static final int CONTACT_SELECTED_CALL = 123;
    private static final int CONTACT_SELECTED_SMS = 124;
    private static final String SERIALIZABLE_NOTE = "SERIALIZABLE_NOTE";

    private EditText editTextDate;
    private EditText editTextTime;
    private Button buttonCall;
    private Button buttonSMS;
    private Button buttonRecord;
    private Button buttonPlay;
    private CheckBox checkboxPhoto, checkboxCall, checkboxSMS, checkboxRecord;
    private ImageView imagePhoto;
    private EditText editTextCall;
    private EditText editTextSMS;
    private EditText title;
    private EditText body;
    private EditText editTextSMSText;

    private Calendar calendar = Calendar.getInstance();

    private boolean isRecording = false;
    private MediaRecorder mediaRecorder = new MediaRecorder();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Chronometer chronometer;

    private Note note;

    public EditFragment() {
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        mediaPlayer.stop();
        if (mediaRecorder != null && isRecording) {
            mediaRecorder.stop();
            isRecording = false;
        }
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            note = (Note) savedInstanceState.getSerializable(SERIALIZABLE_NOTE);
        }
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle saveInstanceState) {
        super.onViewCreated(view, saveInstanceState);

        editTextDate = (EditText) view.findViewById(R.id.editTextDate);
        updateDateDisplay();

        editTextTime = (EditText) view.findViewById(R.id.editTextTime);
        updateTimeDisplay();

        checkboxPhoto = (CheckBox) view.findViewById(R.id.checkboxPhoto);
        checkboxCall = (CheckBox) view.findViewById(R.id.checkboxCall);
        checkboxSMS = (CheckBox) view.findViewById(R.id.checkboxSMS);
        checkboxRecord = (CheckBox) view.findViewById(R.id.checkboxRecord);
        imagePhoto = (ImageView) view.findViewById(R.id.imagePhoto);
        editTextCall = (EditText) view.findViewById(R.id.editTextCall);
        editTextSMS = (EditText) view.findViewById(R.id.editTextSMS);
        buttonCall = (Button) view.findViewById(R.id.buttonCall);
        buttonSMS = (Button) view.findViewById(R.id.buttonSMS);
        buttonRecord = (Button) view.findViewById(R.id.buttonRecord);
        buttonPlay = (Button) view.findViewById(R.id.buttonPlay);
        chronometer = (Chronometer) view.findViewById(R.id.chronometer);
        title = (EditText) view.findViewById(R.id.title);
        body = (EditText) view.findViewById(R.id.body);
        editTextSMSText = (EditText) view.findViewById(R.id.editTextSMSText);

        setListeners(view);

        populateFields();
    }

    private void populateFields() {
        title.setText(note.getTitle());
        body.setText(note.getBody());
        editTextCall.setText(note.getCallNumber());
        editTextSMS.setText(note.getSmsNumber());
        editTextSMSText.setText(note.getSmsText());
        calendar.setTimeInMillis(note.getDateTime());
        updateDateDisplay();
        updateTimeDisplay();
        checkboxCall.setChecked(!TextUtils.isEmpty(note.getCallNumber()));
        checkboxSMS.setChecked(!TextUtils.isEmpty(note.getSmsNumber()) || !TextUtils.isEmpty(note.getSmsText()));
        if (TextUtils.isEmpty(note.getRecordPath())) {
            checkboxRecord.setChecked(false);
        } else {
            checkboxRecord.setChecked(true);
        }
        if (TextUtils.isEmpty(note.getPhoto())) {
            checkboxPhoto.setChecked(false);
        } else {
            checkboxPhoto.setChecked(true);
            Bitmap bitmap = BitmapFactory.decodeFile(note.getPhoto());
            imagePhoto.setImageBitmap(bitmap);
        }
    }

    private void setListeners(View view) {

        editTextDate.setOnClickListener(onButtonDateClickListner);
        editTextTime.setOnClickListener(onButtonTimeClickListner);
        checkboxPhoto.setOnCheckedChangeListener(getOnCheckedChangeListener(view.findViewById(R.id.imagePhoto)));
        checkboxCall.setOnCheckedChangeListener(getOnCheckedChangeListener(view.findViewById(R.id.viewCall)));
        checkboxSMS.setOnCheckedChangeListener(getOnCheckedChangeListener(view.findViewById(R.id.viewSMS)));
        checkboxRecord.setOnCheckedChangeListener(getOnCheckedChangeListener(view.findViewById(R.id.viewRecord)));
        imagePhoto.setOnClickListener(onPhotoImageClickListener);
        buttonSMS.setOnClickListener(onSMSBtClickListener);
        buttonCall.setOnClickListener(onCallBtClickListener);
        buttonRecord.setOnClickListener(onRecordBtClickListener);
        buttonPlay.setOnClickListener(onPlayBtClickListener);
    }

  private View.OnClickListener onPhotoImageClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
      startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }
  };

  private View.OnClickListener onCallBtClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent chooser = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(chooser, CONTACT_SELECTED_CALL);
    }
  };

  private View.OnClickListener onPlayBtClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if(isRecording) {
          return;
        }

        if(mediaPlayer.isPlaying()) {
          mediaPlayer.stop();
        } else {
          buttonRecord.setEnabled(false);
          buttonPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_stop));
          chronometer.setBase(SystemClock.elapsedRealtime());
          chronometer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {
                        buttonPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_play));
                        buttonRecord.setEnabled(true);
                        chronometer.stop();
                    } catch (IllegalStateException e) {
                        //ignore
                    }
                }

            });
              try {
                  mediaPlayer.setDataSource(note.getRecordPath());
                  mediaPlayer.prepare();
                  mediaPlayer.start();
              } catch (Exception e) {
                    mediaPlayer.stop();
                  buttonPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_play));
                  buttonRecord.setEnabled(true);
                  chronometer.stop();
              }
        }
    }
  };

  private View.OnClickListener onSMSBtClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
      Intent chooser = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
      startActivityForResult(chooser, CONTACT_SELECTED_SMS);
    }
  };

  private View.OnClickListener onRecordBtClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if(mediaPlayer.isPlaying()) {
        return;
      }
      if(isRecording) {
        stopRecording();
      } else {
        startRecording();
      }
      isRecording = !isRecording;
    }
  };

  private void stopRecording() {
  	chronometer.stop();
    mediaRecorder.stop();
    buttonRecord.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_record));
    buttonPlay.setEnabled(true);
  }
  private void startRecording() {
  	mediaRecorder = new MediaRecorder();
    prepare();
    mediaRecorder.start();
    chronometer.setBase(SystemClock.elapsedRealtime());
    chronometer.start();
    buttonRecord.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_stop_record));
    buttonPlay.setEnabled(false);
  }

  private void prepare() {
  	try {
      mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
      mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
      note.setRecordPath(getNewPath());
      mediaRecorder.setOutputFile(note.getRecordPath());
      mediaRecorder.prepare();
    } catch (Exception e) {
      //ignore tmp
    }
  }

  private CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener(final View viewToShow) {
    return new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
          viewToShow.setVisibility(View.VISIBLE);
        } else {
          viewToShow.setVisibility(View.GONE);
        }
      }
    };
  }

  private View.OnClickListener onButtonDateClickListner = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      new DatePickerDialog(getActivity(), pDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
              calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
  };

  private View.OnClickListener onButtonTimeClickListner = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new TimePickerDialog(getActivity(), pTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true).show();
      }
    };

  private void updateDateDisplay() {
    editTextDate.setText(
          new StringBuilder()
                  // Month is 0 based so add 1
                  .append(calendar.get(Calendar.MONTH) + 1).append("/")
                  .append(calendar.get(Calendar.DAY_OF_MONTH)).append("/")
                  .append(calendar.get(Calendar.YEAR)).append(" "));
  }

  private void updateTimeDisplay() {
    String hourOfDayText = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
    if (hourOfDayText.length() == 1) {
      hourOfDayText = "0" + hourOfDayText;
    }
    String minuteText = String.valueOf(calendar.get(Calendar.MINUTE));
    if (minuteText.length() == 1) {
      minuteText = "0" + minuteText;
    }
    editTextTime.setText(String.format("%s:%s", hourOfDayText, minuteText));
  }

  private DatePickerDialog.OnDateSetListener pDateSetListener = new DatePickerDialog.OnDateSetListener() {

    public void onDateSet(DatePicker view, int year,
                        int monthOfYear, int dayOfMonth) {
        calendar.set(year, monthOfYear, dayOfMonth);
      updateDateDisplay();
    }
  };

  private TimePickerDialog.OnTimeSetListener pTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
      calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
      calendar.set(Calendar.MINUTE, minute);
      updateTimeDisplay();
    }
  };

  public void onActivityResult(int requestCode, int resultCode, Intent data){
    super.onActivityResult(requestCode, resultCode, data);
    switch(requestCode) {
      case CONTACT_SELECTED_CALL:
      case CONTACT_SELECTED_SMS:
        if (resultCode == Activity.RESULT_OK) {
          Uri contactData = data.getData();
          Cursor c = getActivity().managedQuery(contactData, null, null, null, null);
          if (c.moveToFirst()) {
            int contactID = c.getInt(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
            String[] selectionArgs = new String[]{"" + contactID};
            Cursor context = getActivity().managedQuery(uri, projection, selection, selectionArgs, null);
            if (context.moveToFirst()) {
              if (requestCode == CONTACT_SELECTED_CALL) {
                editTextCall.setText(context.getString(0));
              } else if (requestCode == CONTACT_SELECTED_SMS) {
                editTextSMS.setText(context.getString(0));
              }
            }
          }
      }
      break;
      case CAMERA_PIC_REQUEST:
        if(resultCode == Activity.RESULT_OK)
        {
          Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
          OutputStream writer = null;

          if (note.getPhoto() != null) {
            File oldFile = new File(note.getPhoto());
            if (oldFile.exists()) {
              oldFile.delete();
            }
          }

          note.setPhoto(getNewPhotoPath());
          File file = new File(note.getPhoto());
          try
          {
            writer = new FileOutputStream(file);
            if(!thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, writer))
            {
              break;
            }

            writer.flush();
            writer.close();

            imagePhoto.setImageBitmap(thumbnail);
          }
          catch(Exception e)
          {

              Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
              break;
          }
        }

        break;
      }
    }
  public static String getNewPhotoPath() {
    String path;
    path = Environment.getExternalStorageDirectory().getAbsolutePath();
    path += "/ProNote/NotePhotos/";
    File fPath = new File(path);
    fPath.mkdirs();
    path += String.valueOf(Calendar.getInstance().getTimeInMillis());
    path += ".jpg";

    return path;
  }

    public void setNote(Note note) {
        this.note = note;
    }




  public static String getNewPath() {
  	String path;
  	path = Environment.getExternalStorageDirectory().getAbsolutePath();
    path += "/ProNote/NoteRecords/";
    File fPath = new File(path);
    fPath.mkdirs();
    path += String.valueOf(Calendar.getInstance().getTimeInMillis());
    path += ".amr";

  	return path;
  }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_accept) {
            new AsyncTask<Note, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    getActivity().setProgressBarIndeterminateVisibility(true);
                }

                @Override
                protected Void doInBackground(Note... params) {
                    NotesDbAdapter dbHelper = new NotesDbAdapter(getActivity());
                    dbHelper.open();
                    Note note2Save = params[0];
                    if (note2Save.getRowId() == -1) {
                        dbHelper.createNote(note2Save);
                    } else {
                        dbHelper.updateNote(note2Save);
                    }
                    dbHelper.close();
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    getActivity().setProgressBarIndeterminateVisibility(false);
                    FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
                      int backStackCount = supportFragmentManager.getBackStackEntryCount();
                      for (int i = 0; i < backStackCount; i++) {
                        supportFragmentManager.popBackStack();
                      }
                }
            }.execute(getUpdatedNote());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public Note getUpdatedNote() {
        note.setTitle(title.getText().toString());
        note.setDateTime(calendar.getTimeInMillis());
        note.setBody(body.getText().toString());
        if (!checkboxPhoto.isChecked()) {
            note.setPhoto(null);
        }
        if (checkboxCall.isChecked()) {
            note.setCallNumber(editTextCall.getText().toString());
        } else {
            note.setCallNumber(null);
        }
        if (checkboxSMS.isChecked()) {
            note.setSmsNumber(editTextSMS.getText().toString());
            note.setSmsText(editTextSMSText.getText().toString());
        } else {
            note.setSmsNumber(null);
            note.setSmsText(null);
        }
        if (!checkboxRecord.isChecked()) {
            note.setRecordPath(null);
        }
        return note;
    }


    @Override
    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        icicle.putSerializable(SERIALIZABLE_NOTE, note);
    }
}

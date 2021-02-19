package com.ichi2.apisample;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int AD_PERM_REQUEST = 0;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1;
    private static final int AD_PERM_REQUEST_VALID = 2;

    private static final int ACTION_SELECT_FILE = 10;

    private static final String STATE_REF_DB = "com.ichi2.apisample.uistate";

    private EditText inputFilename;
    private AutoCompleteTextView inputStartNote;
    private RadioGroup radioGroupDirection;
    private RadioGroup radioGroupTiming;
    private Spinner selectInterval;
    private SeekBar seekTempo;
    private AutoCompleteTextView inputInstrument;

    private HashSet<String> savedStartNotes = new HashSet<>();
    private HashSet<String> savedInstruments = new HashSet<>();

    private AnkiDroidHelper mAnkiDroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar main_toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);

        inputFilename = findViewById(R.id.inputFilename);
        inputStartNote = findViewById(R.id.inputStartNote);
        radioGroupDirection = findViewById(R.id.radioGroupDirection);
        radioGroupTiming = findViewById(R.id.radioGroupTiming);
        selectInterval = findViewById(R.id.selectInterval);
        seekTempo = findViewById(R.id.seekTempo);
        inputInstrument = findViewById(R.id.inputInstrument);

        seekTempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView label = findViewById(R.id.labelTempoValue);
                label.setText(Integer.toString(seekBar.getProgress()));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        final String[] items = new String[] { "", "min2", "Maj2", "min3", "Maj3" }; // @todo: Make full list of intervals
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        selectInterval.setAdapter(adapter);

        configureTempoButtons();
        configureClearAllButton();
        configureSelectFileButton();
        configureCheckExistenceButton();
        configureAddToAnkiButton();
        configureSettingsButton();

        restoreUiState();

        mAnkiDroid = new AnkiDroidHelper(this);

        if (mAnkiDroid.shouldRequestPermission()) {
            mAnkiDroid.requestPermission(this, AD_PERM_REQUEST_VALID);
        } else if (!doesModelExist() || !doesModelHaveEnoughFields() || !doesModelHaveStoredFields()) {
            validateModel();
        }
    }

    private void configureTempoButtons() {
        final Button actionTempoMinus = findViewById(R.id.actionTempoMinus);
        actionTempoMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int progress = seekTempo.getProgress();
                if (progress > 0) {
                    seekTempo.setProgress(progress - 1);
                }
            }
        });

        final Button actionTempoPlus = findViewById(R.id.actionTempoPlus);
        actionTempoPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int progress = seekTempo.getProgress();
                if (progress < 200) { // @fixme Use some constant probably
                    seekTempo.setProgress(progress + 1);
                }
            }
        });
    }

    private void configureClearAllButton() {
        final Button actionClearAll = findViewById(R.id.actionClearAll);
        actionClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputFilename.setText("");
                inputStartNote.setText("");
                radioGroupDirection.check(findViewById(R.id.radioDirectionAny).getId());
                radioGroupTiming.check(findViewById(R.id.radioTimingAny).getId());
                selectInterval.setSelection(0);
                seekTempo.setProgress(0);
                inputInstrument.setText("");
            }
        });
    }

    private void configureSelectFileButton() {
        final Button actionSelectFile = findViewById(R.id.actionSelectFile);
        actionSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[] {
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_EXTERNAL_STORAGE
                );

                Intent intent = new Intent()
                        .setAction(Intent.ACTION_GET_CONTENT)
                        .setType("audio/*")
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .putExtra(Intent.EXTRA_LOCAL_ONLY, true);

                startActivityForResult(Intent.createChooser(intent, getResources().getText(R.string.select_filename)),
                        ACTION_SELECT_FILE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_SELECT_FILE && resultCode == RESULT_OK) {
            final Uri selectedFile = data.getData();
            String filePath = getFilePath(this, selectedFile);
            inputFilename.setText(filePath);

            MediaExtractor extractor = new MediaExtractor();
            try {
                extractor.setDataSource(filePath);
            } catch (Exception e) {
                try {
                    extractor.setDataSource(new FileInputStream(filePath).getFD());
                } catch (Exception e1) {
                    e.printStackTrace();
                    return;
                }
            }
            MediaFormat format = extractor.getTrackFormat(0);
            String mime = format.getString(MediaFormat.KEY_MIME);
            extractor.selectTrack(0);
            MediaCodec decoder;
            try {
                decoder = MediaCodec.createDecoderByType(mime);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            decoder.configure(format, null, null, 0);
            decoder.start();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            ArrayList<Byte> audioData = new ArrayList<>();
            boolean isEOS = false;
            while ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) == 0) {
                if (!isEOS) {
                    int inputBufferId = decoder.dequeueInputBuffer(10000);
                    if (inputBufferId >= 0) {
                        ByteBuffer inputBuffer = decoder.getInputBuffer(inputBufferId);
                        int sampleSize = extractor.readSampleData(inputBuffer, 0);
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEOS = true;
                        } else {
                            decoder.queueInputBuffer(inputBufferId, 0, sampleSize, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }

                int outputBufferId = decoder.dequeueOutputBuffer(bufferInfo, 10000);
                if (outputBufferId >= 0) {
                    ByteBuffer outputBuffer = decoder.getOutputBuffer(outputBufferId);
                    int t = outputBuffer.position();
                    byte[] dst = new byte[bufferInfo.size - bufferInfo.offset];
                    outputBuffer.get(dst);
                    for (byte b : dst) {
                        audioData.add(b);
                    }
                    outputBuffer.position(t);
                    decoder.releaseOutputBuffer(outputBufferId, false);
                }
            }
            decoder.stop();
            decoder.release();

            int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

            int signalLength = audioData.size() / channelCount;
            if (signalLength == 0) {
                return;
            }
            double[] signal = new double[signalLength];
            for (int i = 0; i < signalLength; i++) {
                signal[i] = (audioData.get(channelCount * i) & 0xFF) | (audioData.get(channelCount * i + 1) << 8);
            }

            int chunkLength = 2048; // arbitrary
            int nChunks = signalLength / chunkLength;
            if (nChunks == 0) {
                return;
            }
            double[][] chunks = new double[nChunks][];
            double[] chunkAmps = new double[nChunks];
            double maxAmp = Double.MIN_VALUE;
            for (int i = 0; i < nChunks; i++) {
                chunks[i] = new double[chunkLength];
                System.arraycopy(signal, i*chunkLength, chunks[i], 0, chunkLength);
                chunkAmps[i] = rootMeanSquare(chunks[i]);
                if (chunkAmps[i] > maxAmp) {
                    maxAmp = chunkAmps[i];
                }
            }

            double threshold = maxAmp * 0.75; // arbitrary
            boolean isAbove = chunkAmps[0] > threshold;
            LinkedList<int[]> peaksIndices = new LinkedList<>();
            if (isAbove) {
                peaksIndices.add(new int[2]);
                peaksIndices.getLast()[0] = 0;
            }
            for (int i = 1; i < chunkAmps.length; i++) {
                if (chunkAmps[i] < threshold && isAbove) {
                    isAbove = false;
                    peaksIndices.getLast()[1] = i;
                } else if (chunkAmps[i] > threshold && !isAbove) {
                    isAbove = true;
                    peaksIndices.add(new int[2]);
                    peaksIndices.getLast()[0] = i;
                }
            }
            if (isAbove) {
                peaksIndices.getLast()[1] = chunkAmps.length - 1;
            }

            if (peaksIndices.size() != 2) {
                return;
            }

            int firstPeakStartChunkIdx = peaksIndices.getFirst()[0];
            int firstPeakEndChunkIdx = peaksIndices.getFirst()[1];
            int secondPeakStartChunkIdx = peaksIndices.getLast()[0];
            int secondPeakEndChunkIdx = peaksIndices.getLast()[1];

            double silenceThreshold = maxAmp * 0.25; //arbitrary
            int endChunkInx = peaksIndices.getLast()[1];
            while (endChunkInx < nChunks && chunkAmps[endChunkInx] > silenceThreshold) {
                endChunkInx++;
            }

            int firstNoteStartChunkIdx = firstPeakStartChunkIdx + (firstPeakEndChunkIdx - firstPeakStartChunkIdx) / 2;
            int secondNoteStartChunkIdx = secondPeakStartChunkIdx + (secondPeakEndChunkIdx - secondPeakStartChunkIdx) / 2;
            int firstNoteEndChunkIdx = firstNoteStartChunkIdx + (secondNoteStartChunkIdx - firstNoteStartChunkIdx) / 2;
            int secondNoteEndChunkIdx = endChunkInx;

            double[] signal1 = new double[(firstNoteEndChunkIdx - firstNoteStartChunkIdx) * chunkLength];
            for (int i = firstNoteStartChunkIdx; i < firstNoteEndChunkIdx; i++) {
                System.arraycopy(chunks[i], 0, signal1, (i - firstNoteStartChunkIdx) * chunkLength, chunkLength);
            }
            String note1 = getNote(getDominantFrequency(signal1, sampleRate, channelCount));

            double[] signal2 = new double[(secondNoteEndChunkIdx - secondNoteStartChunkIdx) * chunkLength];
            for (int i = secondNoteStartChunkIdx; i < secondNoteEndChunkIdx; i++) {
                System.arraycopy(chunks[i], 0, signal2, (i - secondNoteStartChunkIdx) * chunkLength , chunkLength);
            }
            String note2 = getNote(getDominantFrequency(signal2, sampleRate, channelCount));

            Log.i("start_note", note1);
            Log.i("direction", getDirection(note1, note2));
            Log.i("interval", getInterval(note1, note2));
        }
    }

    private double rootMeanSquare(double[] arr) {
        double sum = 0;
        for (double n : arr) {
            sum += Math.pow(n, 2);
        }
        return Math.sqrt(sum / arr.length);
    }

    private int getDominantFrequency(double[] signal, int sampleRate, int channelCount) {
        int len = signal.length;
        double[] waveTransformReal = new double[len];
        double[] waveTransformImg = new double[len];
        System.arraycopy(signal, 0, waveTransformReal, 0, len);

        Fft.transform(waveTransformReal, waveTransformImg);

        double[] abs = new double[len];
        for (int i = 0; i < len; i++) {
            abs[i] = (Math.sqrt(waveTransformReal[i] * waveTransformReal[i] + waveTransformImg[i] * waveTransformImg[i]));
        }

        int maxIndex = 0;
        for (int i = 0; i < len; i++) {
            if (abs[i] > abs[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex * sampleRate * channelCount / len;
    }

    private final static ArrayList<String> notes = new ArrayList<>(Arrays.asList("C0", "C#0", "D0",
            "D#0", "E0", "F0", "F#0", "G0", "G#0", "A0", "A#0", "B0", "C1", "C#1", "D1", "D#1", "E1", "F1", "F#1", "G1",
            "G#1", "A1", "A#1", "B1", "C2", "C#2", "D2", "D#2", "E2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2", "C3",
            "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3", "C4", "C#4", "D4", "D#4", "E4", "F4",
            "F#4", "G4", "G#4", "A4", "A#4", "B4", "C5", "C#5", "D5", "D#5", "E5", "F5", "F#5", "G5", "G#5", "A5",
            "A#5", "B5", "C6", "C#6", "D6", "D#6", "E6", "F6", "F#6", "G6", "G#6", "A6", "A#6", "B6", "C7", "C#7", "D7",
            "D#7", "E7", "F7", "F#7", "G7", "G#7", "A7", "A#7", "B7", "C8", "C#8", "D8", "D#8", "E8", "F8", "F#8", "G8",
            "G#8", "A8", "A#8", "B8"));

    private static String getNote(double frequency) {
        final double a4Frequency = 440;
        final int a4Index = 57;
        final double rootThingy = Math.pow(2, (double) 1 / (double) 12);
        double tempFrequency = a4Frequency;
        double lastFrequency = tempFrequency;
        int tempIndex = a4Index;
        int lastIndex = tempIndex;
        boolean flag = frequency > a4Frequency;
        boolean lastFlag = flag;
        while (flag == lastFlag) {
            lastIndex = tempIndex;
            tempIndex = flag ? tempIndex + 1 : tempIndex - 1;
            if (tempIndex < 0 || tempIndex >= notes.size()) {
                return "frequency out of range";
            }
            lastFrequency = tempFrequency;
            int diff = tempIndex - a4Index;
            tempFrequency = a4Frequency * Math.pow(rootThingy, diff);
            lastFlag = flag;
            flag = frequency > tempFrequency;
        }
        if (Math.abs(Math.abs(frequency) - Math.abs(tempFrequency)) < Math
                .abs(Math.abs(frequency) - Math.abs(lastFrequency))) {
            return notes.get(tempIndex);
        } else {
            return notes.get(lastIndex);
        }
    }

    private static String getInterval(String note1, String note2) {
        final String[] intervals = new String[] { "zero", "min2", "Maj2", "min3", "Maj3" };
        int distance = Math.abs(notes.indexOf(note1) - notes.indexOf(note2));
        if (distance >= intervals.length) {
            return "too big interval";
        }
        return intervals[distance];
    }

    private static String getDirection(String note1, String note2) {
        int index1 = notes.indexOf(note1);
        int index2 = notes.indexOf(note2);
        if (index2 - index1 > 0) {
            return "ascending";
        } else if (index2 - index1 < 0) {
            return "descending";
        } else {
            return "same note";
        }
    }

    private void configureCheckExistenceButton() {
        final AlertDialog.Builder markNoteDialog = new AlertDialog.Builder(this);
        markNoteDialog
                .setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            final int count = getMusInterval().markExistingNotes();
                            showMsg(getResources().getQuantityString(R.plurals.mi_marked, count, count));
                        } catch (MusInterval.Exception e) {
                            processMusIntervalException(e);
                        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                            processInvalidAnkiDatabase(e);
                        }
                    }
                })
                .setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final Button actionCheckExistence = findViewById(R.id.actionCheckExistence);
        actionCheckExistence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                    return;
                }
                if (!doesModelExist() || !doesModelHaveEnoughFields() || !doesModelHaveStoredFields()) {
                    validateModel();
                    return;
                }
                try {
                    final MusInterval mi = getMusInterval();
                    final int count = mi.getExistingNotesCount();

                    if (count > 0) {
                        final int marked = mi.getExistingMarkedNotesCount();

                        if (count == marked) {
                            showMsg(getResources().getQuantityString(R.plurals.mi_exists_marked, count, count));
                        } else if (marked == 0) {
                            markNoteDialog.setMessage(getResources().getQuantityString(R.plurals.mi_exists_ask_mark, count, count)).show();
                        } else {
                            markNoteDialog.setMessage(getResources().getQuantityString(R.plurals.mi_exists_partially_marked_ask_mark, marked, count, marked)).show();
                        }
                    } else {
                        showMsg(R.string.mi_not_exists);
                    }
                } catch (MusInterval.Exception e) {
                    processMusIntervalException(e);
                } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                    processInvalidAnkiDatabase(e);
                }
            }
        });
    }

    private void configureAddToAnkiButton() {
        final Button actionAddToAnki = findViewById(R.id.actionAddToAnki);
        actionAddToAnki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                    return;
                }
                if (!doesModelExist() || !doesModelHaveEnoughFields() || !doesModelHaveStoredFields()) {
                    validateModel();
                    return;
                }
                try {
                    MusInterval newMi = getMusInterval().addToAnki();
                    inputFilename.setText(newMi.sound);
                    inputStartNote.setText(newMi.startNote);

                    savedStartNotes.add(newMi.startNote);
                    savedInstruments.add(newMi.instrument);

                    showMsg(R.string.item_added);
                } catch (MusInterval.Exception e) {
                    processMusIntervalException(e);
                }
            }
        });
    }

    private void configureSettingsButton() {
        final Button actionOpenSettings = findViewById(R.id.actionOpenSettings);
        actionOpenSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                    return;
                }
                if (!doesModelExist()) {
                    DialogFragment f = new CreateModelDialogFragment();
                    f.show(getFragmentManager(), "createModelDialog");
                    return;
                } else if (!doesModelHaveEnoughFields()) {
                    showMsg(String.format(getResources().getString(R.string.invalid_model), MusInterval.Builder.DEFAULT_MODEL_NAME));
                    return;
                }
                openSettings();
            }
        });
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private Long findModel() {
        return mAnkiDroid.findModelIdByName(MusInterval.Builder.DEFAULT_MODEL_NAME);
    }

    private boolean doesModelExist() {
        return findModel() != null;
    }

    private boolean doesModelHaveEnoughFields() {
        return mAnkiDroid.getFieldList(findModel()).length >= MusInterval.Fields.SIGNATURE.length;
    }

    private boolean doesModelHaveStoredFields() {
        final ArrayList<String> existingModelFields = new ArrayList<>(Arrays.asList(mAnkiDroid.getFieldList(findModel())));
        final String[] storedFields = new String[MusInterval.Fields.SIGNATURE.length];
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0; i < MusInterval.Fields.SIGNATURE.length; i++) {
            storedFields[i] = sharedPreferences.getString(MusInterval.Fields.SIGNATURE[i], MusInterval.Fields.SIGNATURE[i]);
        }
        ArrayList<String> takenFields = new ArrayList<>();
        for (String field : storedFields) {
            if (!existingModelFields.contains(field) || takenFields.contains(field)) {
                return false;
            }
            takenFields.add(field);
        }
        return true;
    }

    private void validateModel() {
        Long modelId = findModel();
        if (modelId == null) {
            DialogFragment f = new CreateModelDialogFragment();
            f.show(getFragmentManager(), "createModelDialog");
        } else if (!doesModelHaveEnoughFields()) {
            showMsg(String.format(getResources().getString(R.string.invalid_model), MusInterval.Builder.DEFAULT_MODEL_NAME));
        } else if (!doesModelHaveStoredFields()) {
            DialogFragment f = new ConfigureModelDialogFragment();
            f.show(getFragmentManager(), "configureModelDialog");
        }
    }

    @Override
    protected void onPause() {
        final SharedPreferences uiDb = getSharedPreferences(STATE_REF_DB, Context.MODE_PRIVATE);
        uiDb.edit()
                .putString("inputFilename", inputFilename.getText().toString())
                .putString("inputStartNote", inputStartNote.getText().toString())
                .putInt("radioGroupDirection", radioGroupDirection.getCheckedRadioButtonId())
                .putInt("radioGroupTiming", radioGroupTiming.getCheckedRadioButtonId())
                .putInt("selectInterval", selectInterval.getSelectedItemPosition())
                .putString("inputTempo", Integer.toString(seekTempo.getProgress()))
                .putString("inputInstrument", inputInstrument.getText().toString())
                .putStringSet("savedStartNotes", savedStartNotes)
                .putStringSet("savedInstruments", savedInstruments)
                .apply();

        super.onPause();
    }

    protected void restoreUiState() {
        final SharedPreferences uiDb = getSharedPreferences(STATE_REF_DB, Context.MODE_PRIVATE);
        inputFilename.setText(uiDb.getString("inputFilename", ""));
        inputStartNote.setText(uiDb.getString("inputStartNote", ""));
        radioGroupDirection.check(uiDb.getInt("radioGroupDirection", findViewById(R.id.radioDirectionAny).getId()));
        radioGroupTiming.check(uiDb.getInt("radioGroupTiming", findViewById(R.id.radioTimingAny).getId()));
        selectInterval.setSelection(uiDb.getInt("selectInterval", 0));
        seekTempo.setProgress(Integer.parseInt(uiDb.getString("inputTempo", "0")));
        inputInstrument.setText(uiDb.getString("inputInstrument", ""));

        savedStartNotes = (HashSet<String>) uiDb.getStringSet("savedStartNotes", new HashSet<String>());
        inputStartNote.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, savedStartNotes.toArray(new String[0])));

        savedInstruments = (HashSet<String>) uiDb.getStringSet("savedInstruments", new HashSet<String>());
        inputInstrument.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, savedInstruments.toArray(new String[0])));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AD_PERM_REQUEST_VALID:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showMsg(R.string.anki_permission_denied);
                } else if (!doesModelExist() || !doesModelHaveEnoughFields() || !doesModelHaveStoredFields()) {
                    validateModel();
                }
                break;
            case AD_PERM_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showMsg(R.string.anki_permission_denied);
                }
            }
            case PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showMsg(R.string.fs_permission_denied);
                }
            }
        }
    }

    private MusInterval getMusInterval() throws MusInterval.ValidationException {
        final String anyStr = getResources().getString(R.string.radio_any);

        final int radioDirectionId = radioGroupDirection.getCheckedRadioButtonId();
        final RadioButton radioDirection = findViewById(radioDirectionId);
        final String directionStr = radioDirectionId != -1  && radioDirection != null ?
                radioDirection.getText().toString() : anyStr;

        final int radioTimingId = radioGroupTiming.getCheckedRadioButtonId();
        final RadioButton radioTiming = findViewById(radioTimingId);
        final String timingStr = radioTimingId != -1 && radioTiming != null ?
                radioTiming.getText().toString() : anyStr;

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final Map<String, String> storedFields = new HashMap<>();
        for (String field : MusInterval.Fields.SIGNATURE) {
            storedFields.put(field, sharedPreferences.getString(field, field));
        }

        return new MusInterval.Builder(mAnkiDroid)
                .model_fields(storedFields)
                .sound(inputFilename.getText().toString())
                .start_note(inputStartNote.getText().toString())
                .direction(!directionStr.equals(anyStr) ? directionStr : "")
                .timing(!timingStr.equals(anyStr) ? timingStr : "")
                .interval(selectInterval.getSelectedItem().toString())
                .tempo(seekTempo.getProgress() > 0 ? Integer.toString(seekTempo.getProgress()) : "")
                .instrument(inputInstrument.getText().toString())
                .build();
    }

    private void processMusIntervalException(MusInterval.Exception miException) {
        try {
            throw miException;
        } catch (MusInterval.NoteNotExistsException e) {
            showMsg(R.string.mi_not_exists);
        } catch (MusInterval.StartNoteSyntaxException e) {
            showMsg(R.string.invalid_start_note);
        } catch (MusInterval.CreateDeckException e) {
            showMsg(R.string.create_deck_error);
        } catch (MusInterval.AddToAnkiException e) {
            showMsg(R.string.add_card_error);
        } catch (MusInterval.MandatoryFieldEmptyException e) {
            showMsg(R.string.mandatory_field_empty);
        } catch (MusInterval.SoundAlreadyAddedException e) {
            showMsg(R.string.already_added);
        } catch (MusInterval.AddSoundFileException e) {
            showMsg(R.string.add_file_error);
        } catch (MusInterval.Exception e) {
            showMsg(R.string.unknown_adding_error);
        }
    }

    private void processInvalidAnkiDatabase(AnkiDroidHelper.InvalidAnkiDatabaseException invalidAnkiDatabaseException) {
        try {
            throw invalidAnkiDatabaseException;
        } catch (AnkiDroidHelper.InvalidAnkiDatabase_fieldAndFieldNameCountMismatchException e) {
            showMsg(R.string.InvalidAnkiDatabase_unknownError);
        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
            showMsg(R.string.InvalidAnkiDatabase_fieldAndFieldNameCountMismatch);
        }
    }

    private void showMsg(int msgResId) {
        Toast.makeText(MainActivity.this, getResources().getString(msgResId), Toast.LENGTH_LONG).show();
    }

    private void showMsg(final String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Transform "content://..." uri to the real file path
     */
    @SuppressLint("NewApi")
    public static String getFilePath(Context context, Uri uri) {
        String selection = null;
        String[] selectionArgs = null;

        if (DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return "/storage/" + split[0] + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[] { split[1] };
            }
        }

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                final int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                //
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static class CreateModelDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final MainActivity mainActivity = (MainActivity) getActivity();
            return new AlertDialog.Builder(mainActivity)
                    .setMessage(String.format(
                            getResources().getString(R.string.create_model),
                            MusInterval.Builder.DEFAULT_MODEL_NAME))
                    .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mainActivity.handleCreateModel();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .create();
        }
    }

    public static class ConfigureModelDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final MainActivity mainActivity = (MainActivity) getActivity();
            return new AlertDialog.Builder(mainActivity)
                    .setMessage(String.format(
                            getResources().getString(R.string.configure_model),
                            MusInterval.Builder.DEFAULT_MODEL_NAME))
                    .setPositiveButton(R.string.configure, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mainActivity.openSettings();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .create();
        }
    }

    private void handleCreateModel() {
        String modelName = MusInterval.Builder.DEFAULT_MODEL_NAME;
        final Long newModelId = mAnkiDroid.addNewCustomModel(
                modelName,
                MusInterval.Fields.SIGNATURE,
                MusInterval.Builder.CARD_NAMES,
                MusInterval.Builder.QFMT,
                MusInterval.Builder.AFMT,
                MusInterval.Builder.CSS);
        if (newModelId != null) {
            showMsg(String.format(
                    getResources().getString(R.string.create_model_success),
                    modelName));
        } else {
            showMsg(R.string.create_model_error);
        }
    }

}

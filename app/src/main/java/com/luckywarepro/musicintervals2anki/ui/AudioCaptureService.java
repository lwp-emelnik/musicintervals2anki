package com.luckywarepro.musicintervals2anki.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.luckywarepro.musicintervals2anki.R;
import com.luckywarepro.musicintervals2anki.helper.AudioUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import linc.com.pcmdecoder.PCMDecoder;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

@SuppressWarnings("FieldCanBeLocal")
public class AudioCaptureService extends Service {
    public final static String EXTRA_RESULT_DATA = "AudioCaptureService:Extra:ResultData";
    public final static String EXTRA_ALLOW_MULTIPLE = "AudioCaptureService:Extra:AllowMultiple";
    public final static String EXTRA_RECORDINGS = "AudioCaptureService:Extra:Recordings";

    public final static String ACTION_FILES_UPDATED = "AudioCaptureService:FilesUpdated";
    public final static String EXTRA_URI_STRING = "AudioCaptureService:Extra:UriString";
    public final static String ACTION_CLOSED = "AudioCaptureService:Closed";

    private final static String CAPTURES_DIRECTORY = "AudioCaptures";

    private final static int SERVICE_ID = 1;
    private final static String NOTIFICATION_CHANNEL_ID = "AudioCapture channel";
    private final static String NOTIFICATION_CHANNEL_NAME = "Audio Capture Service Channel";

    private final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final int WORD_LENGTH = 16;
    private final int SAMPLE_RATE = 44100;
    private final int CHANNEL_MASK = AudioFormat.CHANNEL_IN_STEREO;
    private final int CHANNEL_COUNT = 2;

    private final static int NUM_SAMPLES_PER_READ = 1024;
    private final static int BYTES_PER_SAMPLE = 2;
    private final static int BUFFER_SIZE_IN_BYTES = NUM_SAMPLES_PER_READ * BYTES_PER_SAMPLE;

    private final static String LOG_TAG = "AudioCaptureService";

    private boolean allowMultiple;

    private MediaProjection projection;
    private AudioRecord record;

    private Thread captureThread;
    private Handler handler;

    private File tempPcmFile;

    private boolean isRecording;
    private long recordingStartedAt;

    private WindowManager windowManager;

    private View overlayView;
    private TextView textTop;
    private RecordingButton actionRecord;
    private Button actionClose;
    private TextView textBottom;

    private View countdownView;
    private TextView textCount;
    private ArrayList<Runnable> countdownCallbacks;

    private TextView textLatest;
    private LinearLayout layoutLatestActions;
    private PlaybackButton actionPlayLatest;

    private LinkedList<Recording> recordings;

    private MediaPlayer mediaPlayer;
    private ToneGenerator toneGenerator;

    private Runnable playbackFinishedCallback;

    public static String getCapturesDirectory(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + AudioCaptureService.CAPTURES_DIRECTORY;
    }

    private final BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tearDown();
        }
    };

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    public void onCreate() {
        super.onCreate();
        NotificationChannel notificationChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
        startForeground(
                SERVICE_ID,
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).build()
        );

        handler = new Handler();

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.x = 0;
        layoutParams.y = 0;

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_recording, null, false);
        View.OnTouchListener moveOnTouchListener = new MoveViewOnTouchListener(windowManager, overlayView);
        overlayView.setOnTouchListener(moveOnTouchListener);

        countdownView = LayoutInflater.from(this).inflate(R.layout.overlay_countdown, null, false);
        countdownView.setVisibility(View.GONE);

        textCount = countdownView.findViewById(R.id.textCount);

        actionRecord = overlayView.findViewById(R.id.actionRecord);
        actionRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) {
                    stopPlayback();
                    hideLatestMenu();

                    actionRecord.setEnabled(false);
                    countdownView.setVisibility(View.VISIBLE);

                    final int t = 3;
                    countdownCallbacks = new ArrayList<>(t + 1);
                    for (int i = 0; i < t; i++) {
                        final int count = t - i;
                        Runnable callback = new Runnable() {
                            @Override
                            public void run() {
                                textCount.setText(String.valueOf(count));
                            }
                        };
                        handler.postDelayed(callback, i * 1000);
                        countdownCallbacks.add(callback);
                    }

                    Runnable callback = new Runnable() {
                        @Override
                        public void run() {
                            handleStartCapture();
                            countdownCallbacks = null;
                        }
                    };
                    handler.postDelayed(callback, t * 1000);
                    countdownCallbacks.add(callback);

                } else {
                    stopAudioCapture();
                    isRecording = false;
                    actionRecord.setRecording(false);
                }
            }
        });
        actionRecord.setOnTouchListener(moveOnTouchListener);

        actionClose = overlayView.findViewById(R.id.actionClose);
        actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalBroadcastManager.getInstance(AudioCaptureService.this).sendBroadcast(new Intent(ACTION_CLOSED));

                MainActivity.storeCapturing(AudioCaptureService.this, false);

                Intent intent = new Intent(AudioCaptureService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                tearDown();
            }
        });
        actionClose.setOnTouchListener(moveOnTouchListener);

        textLatest = overlayView.findViewById(R.id.textLatest);

        layoutLatestActions = overlayView.findViewById(R.id.layoutLatestActions);

        recordings = new LinkedList<>();

        toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, 25);

        actionPlayLatest = overlayView.findViewById(R.id.actionPlayLatest);
        actionPlayLatest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (mediaPlayer == null) {
                        actionPlayLatest.setPlaying(true);
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioAttributes(
                                new AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .build()
                        );
                        Uri uri = recordings.getLast().getUri();
                        mediaPlayer.setDataSource(AudioCaptureService.this, uri);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        playbackFinishedCallback = new Runnable() {
                            @Override
                            public void run() {
                                stopPlayback();
                            }
                        };
                        long duration = AudioUtil.getDuration(AudioCaptureService.this, uri);
                        handler.postDelayed(playbackFinishedCallback, duration);
                    } else {
                        stopPlayback();
                    }
                } catch (IOException e) {
                    throw new Error();
                }
            }
        });
        actionPlayLatest.setOnTouchListener(moveOnTouchListener);

        Button actionDiscardLatest = overlayView.findViewById(R.id.actionDiscardLatest);
        actionDiscardLatest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlayback();

                Intent intent = new Intent(ACTION_FILES_UPDATED);
                LocalBroadcastManager.getInstance(AudioCaptureService.this).sendBroadcast(intent);

                String[] filenames = MainActivity.getStoredFilenames(AudioCaptureService.this);
                if (filenames.length > 0) {
                    String[] newFilenames = new String[filenames.length - 1];
                    System.arraycopy(filenames, 0, newFilenames, 0, filenames.length - 1);
                    MainActivity.storeFilenames(AudioCaptureService.this, newFilenames);
                    if (newFilenames.length == 0) {
                        MainActivity.storeAfterCapturing(AudioCaptureService.this, false);
                    }
                }

                // Recording discardedRecording = recordings.getLast();
                //  Uri uri = discardedRecording.getUri();
                //  String path = uri.getPath();
                //  if (!new File(path).delete()) {
                //      Log.e(LOG_TAG, "Could not delete discarded recording file");
                //  }
                // since draft files might be referenced from other tabs we cannot simply delete them anymore

                recordings.removeLast();

                textBottom.setText(getString(R.string.recorded_files, recordings.size()));
                if (recordings.size() == 0) {
                    hideLatestMenu();
                    actionRecord.setEnabled(true);
                } else {
                    Recording recording = recordings.getLast();
                    textLatest.setText(getString(R.string.latest_file, recording.getDurationSeconds()));
                }
            }
        });
        actionDiscardLatest.setOnTouchListener(moveOnTouchListener);

        textTop = overlayView.findViewById(R.id.textTop);
        refreshTime(0);
        textBottom = overlayView.findViewById(R.id.textBottom);
        textBottom.setText(getString(R.string.recorded_files, 0));

        Button actionSkip = countdownView.findViewById(R.id.actionSkip);
        actionSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (countdownCallbacks != null) {
                            for (Runnable callback : countdownCallbacks) {
                                handler.removeCallbacks(callback);
                            }
                            handleStartCapture();
                        }
                    }
                });
            }
        });

        windowManager.addView(overlayView, layoutParams);
        windowManager.addView(countdownView, layoutParams);

        LocalBroadcastManager.getInstance(this).registerReceiver(closeReceiver, new IntentFilter(MainActivity.ACTION_CLOSE_CAPTURING));
    }

    private void stopPlayback() {
        if (playbackFinishedCallback != null) {
            handler.removeCallbacks(playbackFinishedCallback);
            playbackFinishedCallback = null;
        }
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        actionPlayLatest.setPlaying(false);
    }

    private void hideLatestMenu() {
        layoutLatestActions.setVisibility(View.GONE);
        textLatest.setVisibility(View.GONE);
    }

    private void handleStartCapture() {
        countdownCallbacks = null;
        countdownView.setVisibility(View.GONE);
        startAudioCapture();
        isRecording = true;
        actionRecord.setEnabled(true);
        actionRecord.setRecording(true);
        textTop.setTypeface(null, Typeface.BOLD);
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150);
    }

    private void tearDown() {
        actionRecord.setOnClickListener(null);
        actionClose.setOnClickListener(null);
        if (countdownCallbacks != null) {
            for (Runnable callback : countdownCallbacks) {
                handler.removeCallbacks(callback);
            }
        }
        countdownCallbacks = null;
        if (captureThread != null && captureThread.isAlive()) {
            captureThread.interrupt();
            try {
                captureThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new Error();
            }
            record.stop();
            if (!tempPcmFile.delete()) {
                Log.e(LOG_TAG, "Could not delete temp audio file");
            }
        }
        record.release();
        projection.stop();
        stopPlayback();
        toneGenerator.release();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        windowManager.removeView(overlayView);
        windowManager.removeView(countdownView);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(closeReceiver);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.Q)
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return Service.START_NOT_STICKY;
        }
        if (intent.hasExtra(EXTRA_ALLOW_MULTIPLE)) {
            allowMultiple = intent.getBooleanExtra(EXTRA_ALLOW_MULTIPLE, false);
        }
        if (intent.hasExtra(EXTRA_RECORDINGS)) {
            String[] filenames = intent.getStringArrayExtra(EXTRA_RECORDINGS);
            for (String filename : filenames) {
                Uri uri = Uri.parse(filename);
                long duration = AudioUtil.getDuration(this, uri);
                Recording recording = new Recording(uri, duration);
                recordings.add(recording);
            }
            if (recordings.size() > 0) {
                if (!allowMultiple) {
                    actionRecord.setEnabled(false);
                }
                Recording recording = recordings.getLast();
                textBottom.setText(getString(R.string.recorded_files, recordings.size()));
                textLatest.setVisibility(View.VISIBLE);
                textLatest.setText(getString(R.string.latest_file, recording.getDurationSeconds()));
                layoutLatestActions.setVisibility(View.VISIBLE);
            }
        }
        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        projection = projectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) intent.getParcelableExtra(EXTRA_RESULT_DATA));
        AudioPlaybackCaptureConfiguration config = new AudioPlaybackCaptureConfiguration.Builder(projection)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        AudioFormat format = new AudioFormat.Builder()
                .setEncoding(ENCODING)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(CHANNEL_MASK)
                .build();
        record = new AudioRecord.Builder()
                .setAudioFormat(format)
                .setBufferSizeInBytes(BUFFER_SIZE_IN_BYTES)
                .setAudioPlaybackCaptureConfig(config)
                .build();
        return Service.START_STICKY;
    }

    private void startAudioCapture() {
        recordingStartedAt = System.currentTimeMillis();
        record.startRecording();
        captureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                tempPcmFile = createAudioFile();
                writeAudioToFile(tempPcmFile);
            }
        });
        captureThread.start();
    }

    private File createAudioFile() {
        File capturesDir = new File(getCapturesDirectory(this));
        if (!capturesDir.exists() && !capturesDir.mkdirs()) {
            exitWithMsg(R.string.directory_creation_error);
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SSS", Locale.US).format(new Date());
        String filename = String.format("Capture-%s.pcm", timestamp);
        return new File(capturesDir.getAbsolutePath() + "/" + filename);
    }

    private void writeAudioToFile(File file) {
        try {
            if (!file.createNewFile()) {
                exitWithMsg(R.string.file_creation_error);
                return;
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            short[] capturedSamples = new short[NUM_SAMPLES_PER_READ];

            while (!captureThread.isInterrupted()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshTime(System.currentTimeMillis() - recordingStartedAt);

                    }
                });
                record.read(capturedSamples, 0, NUM_SAMPLES_PER_READ);
                fileOutputStream.write(toByteArray(capturedSamples), 0, BUFFER_SIZE_IN_BYTES);
            }

            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error();
        }
    }

    private void refreshTime(long millis) {
        textTop.setText(
                String.format(
                        Locale.US,
                        "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
                        (TimeUnit.MILLISECONDS.toMillis(millis) % TimeUnit.SECONDS.toMillis(1)) / 10
                )
        );
    }

    private void stopAudioCapture() {
        captureThread.interrupt();
        try {
            captureThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new Error();
        }

        try {
            String pathname = tempPcmFile.getAbsolutePath();
            String convertedPathname = pathname.substring(0, pathname.lastIndexOf(".")) + ".mp3";
            File convertedFile = new File(convertedPathname);
            if (!convertedFile.createNewFile()) {
                exitWithMsg(R.string.file_creation_error);
                return;
            }
            PCMDecoder.encodeToMp3(
                    pathname,
                    CHANNEL_COUNT,
                    SAMPLE_RATE * WORD_LENGTH * CHANNEL_COUNT,
                    SAMPLE_RATE,
                    convertedPathname
            );
            if (!tempPcmFile.delete()) {
                Log.e(LOG_TAG, "Could not delete temp audio file");
            }

            Uri uri = Uri.fromFile(convertedFile);

            Intent intent = new Intent(ACTION_FILES_UPDATED);
            intent.putExtra(EXTRA_URI_STRING, uri.toString());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            boolean afterSelecting = MainActivity.getStoredAfterSelecting(this);
            boolean afterAdding = MainActivity.getStoredAfterAdding(this);
            ArrayList<String> newFilenames;
            if (afterSelecting || afterAdding) {
                newFilenames = new ArrayList<>();
                MainActivity.resetStoredMismatchingSorting(this);
                MainActivity.storeAfterSelecting(this, false);
                MainActivity.storeAfterAdding(this, false);
            } else {
                String[] filenames = MainActivity.getStoredFilenames(this);
                newFilenames = new ArrayList<>(Arrays.asList(filenames));
            }
            newFilenames.add(uri.toString());
            MainActivity.storeFilenames(this, newFilenames.toArray(new String[0]));
            MainActivity.storeAfterCapturing(this, true);

            long duration = System.currentTimeMillis() - recordingStartedAt;
            Recording recording = new Recording(uri, duration);
            recordings.add(recording);
            if (!allowMultiple) {
                actionRecord.setEnabled(false);
            }
            textBottom.setText(getString(R.string.recorded_files, recordings.size()));
            textLatest.setVisibility(View.VISIBLE);
            textLatest.setText(getString(R.string.latest_file, recording.getDurationSeconds()));
            layoutLatestActions.setVisibility(View.VISIBLE);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textTop.setTypeface(null, Typeface.NORMAL);
                    refreshTime(0);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error();
        }

        record.stop();
    }

    private void exitWithMsg(final int resId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AudioCaptureService.this, resId, Toast.LENGTH_LONG).show();
                tearDown();
            }
        });
    }

    private static byte[] toByteArray(short[] array) {
        // Samples get translated into bytes following little-endianness:
        // least significant byte first and the most significant byte last
        byte[] result = new byte[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            result[i * 2] = (byte) (array[i] & 0x00FF);
            result[i * 2 + 1] = (byte) ((int) array[i] >> 8);
            array[i] = 0;
        }
        return result;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class Recording {
        private final Uri uri;
        private final long duration;

        public Recording(Uri uri, long duration) {
            this.uri = uri;
            this.duration = duration;
        }

        public Uri getUri() {
            return uri;
        }

        public double getDurationSeconds() {
            return duration / 1000d;
        }
    }
}

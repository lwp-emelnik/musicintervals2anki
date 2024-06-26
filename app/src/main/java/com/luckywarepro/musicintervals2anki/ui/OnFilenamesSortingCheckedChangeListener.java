package com.luckywarepro.musicintervals2anki.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.widget.RadioGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.luckywarepro.musicintervals2anki.R;
import com.luckywarepro.musicintervals2anki.helper.UriUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class OnFilenamesSortingCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
    private final MainActivity mainActivity;
    private final FilenameAdapter.UriPathName[] uriPathNames;
    private final RecyclerView recyclerView;
    private final OnPlayAllClickListener playAllListener;

    public OnFilenamesSortingCheckedChangeListener(MainActivity mainActivity, FilenameAdapter.UriPathName[] uriPathNames, RecyclerView recyclerView, OnPlayAllClickListener playAllListener) {
        this.mainActivity = mainActivity;
        this.uriPathNames = uriPathNames;
        this.recyclerView = recyclerView;
        this.playAllListener = playAllListener;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        ArrayList<String> names = new ArrayList<>(uriPathNames.length);
        ArrayList<Long> lastModifiedValues = new ArrayList<>(uriPathNames.length);
        ContentResolver resolver = mainActivity.getContentResolver();
        for (FilenameAdapter.UriPathName uriPathName : uriPathNames) {
            Uri uri = uriPathName.getUri();
            Cursor cursor = resolver.query(UriUtil.getContentUri(mainActivity, uri), null, null, null, null);
            int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int lastModifiedIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
            cursor.moveToFirst();
            names.add(cursor.getString(nameIdx));
            lastModifiedValues.add(
                    lastModifiedIdx != -1 ?
                            cursor.getLong(lastModifiedIdx) :
                            new File(uri.getPath()).lastModified()
            );
            cursor.close();
        }

        FilenameAdapter.UriPathName[] sortedUriPathNames = new FilenameAdapter.UriPathName[uriPathNames.length];
        String[] uriStrings = new String[uriPathNames.length];

        if (i == R.id.radioByName) {
            final ArrayList<String> namesSorted = new ArrayList<>(names);
            namesSorted.sort(MainActivity.COMPARATOR_FILE_NAME);
            for (int j = 0; j < sortedUriPathNames.length; j++) {
                int sortedNameIdx = names.indexOf(namesSorted.get(j));
                FilenameAdapter.UriPathName uriPathName = uriPathNames[sortedNameIdx];
                uriPathName.setLabel(mainActivity.getFilenameLabel(uriPathName.getName(), j));
                sortedUriPathNames[j] = uriPathName;
                uriStrings[j] = sortedUriPathNames[j].getUri().toString();
            }
            mainActivity.sortByName = true;
            mainActivity.sortByDate = false;

        } else if (i == R.id.radioByDate) {
            final ArrayList<Long> lastModifiedSorted = new ArrayList<>(lastModifiedValues);
            lastModifiedSorted.sort(MainActivity.COMPARATOR_FILE_DATE);
            for (int j = 0; j < sortedUriPathNames.length; j++) {
                int sortedLastModifiedIdx = lastModifiedValues.indexOf(lastModifiedSorted.get(j));
                FilenameAdapter.UriPathName uriPathName = uriPathNames[sortedLastModifiedIdx];
                uriPathName.setLabel(mainActivity.getFilenameLabel(uriPathName.getName(), j));
                sortedUriPathNames[j] = uriPathName;
                uriStrings[j] = sortedUriPathNames[j].getUri().toString();
            }
            mainActivity.sortByName = false;
            mainActivity.sortByDate = true;
        }

        mainActivity.filenames = uriStrings;
        mainActivity.refreshFilenameText(sortedUriPathNames[0].getName());
        mainActivity.actionViewAll.setOnClickListener(new OnViewAllClickListener(mainActivity, sortedUriPathNames));
        mainActivity.soundPlayer.stop();
        if (playAllListener.isPlaying()) {
            playAllListener.stop();
        }
        playAllListener.setUriPathNames(sortedUriPathNames);
        FilenameAdapter adapter = new FilenameAdapter(mainActivity, sortedUriPathNames, playAllListener);
        playAllListener.setListeners(adapter.getListeners());
        recyclerView.setAdapter(adapter);
    }
}

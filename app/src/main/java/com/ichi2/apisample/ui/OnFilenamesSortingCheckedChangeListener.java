package com.ichi2.apisample.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.widget.RadioGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.ichi2.apisample.R;

import java.util.ArrayList;
import java.util.Comparator;

public class OnFilenamesSortingCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
    private final MainActivity mainActivity;
    private final FilenameAdapter.UriPathName[] uriPathNames;
    private final RecyclerView recyclerView;

    public OnFilenamesSortingCheckedChangeListener(MainActivity mainActivity, FilenameAdapter.UriPathName[] uriPathNames, RecyclerView recyclerView) {
        this.mainActivity = mainActivity;
        this.uriPathNames = uriPathNames;
        this.recyclerView = recyclerView;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        ArrayList<String> names = new ArrayList<>(uriPathNames.length);
        ArrayList<Long> lastModifiedValues = new ArrayList<>(uriPathNames.length);
        ContentResolver resolver = mainActivity.getContentResolver();
        for (FilenameAdapter.UriPathName uriPathName : uriPathNames) {
            Uri uri = uriPathName.getUri();
            Cursor cursor = resolver.query(uri, null, null, null, null);
            int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int lastModifiedIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
            cursor.moveToFirst();
            names.add(cursor.getString(nameIdx));
            lastModifiedValues.add(cursor.getLong(lastModifiedIdx));
            cursor.close();
        }

        FilenameAdapter.UriPathName[] sortedUriPathNames = new FilenameAdapter.UriPathName[uriPathNames.length];
        String[] uriStrings = new String[uriPathNames.length];

        if (i == R.id.radioByName) {
            final ArrayList<String> namesSorted = new ArrayList<>(names);
            namesSorted.sort(new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return s.compareTo(t1);
                }
            });

            for (int j = 0; j < sortedUriPathNames.length; j++) {
                int sortedNameIdx = names.indexOf(namesSorted.get(j));
                FilenameAdapter.UriPathName uriPathName = uriPathNames[sortedNameIdx];
                String startNote = j < mainActivity.noteKeys.length || j < mainActivity.octaveKeys.length ? mainActivity.noteKeys[j] + mainActivity.octaveKeys[j] : mainActivity.getString(R.string.unassigned);
                String interval = j < mainActivity.intervalKeys.length ? mainActivity.intervalKeys[j] : mainActivity.getString(R.string.unassigned);
                String label = mainActivity.getString(
                        R.string.filename_with_key,
                        j + 1,
                        uriPathName.getName(),
                        startNote,
                        interval);
                sortedUriPathNames[j] = new FilenameAdapter.UriPathName(uriPathName.getUri(), uriPathName.getPath(), uriPathName.getName(), label);
                uriStrings[j] = sortedUriPathNames[j].getUri().toString();
            }
            mainActivity.sortByName = true;
            mainActivity.sortByDate = false;
            mainActivity.filenames = uriStrings;
        } else if (i == R.id.radioByDate) {
            final ArrayList<Long> lastModifiedSorted = new ArrayList<>(lastModifiedValues);
            lastModifiedSorted.sort(new Comparator<Long>() {
                @Override
                public int compare(Long s, Long t1) {
                    return Long.compare(s, t1);
                }
            });
            for (int j = 0; j < sortedUriPathNames.length; j++) {
                int sortedLastModifiedIdx = lastModifiedValues.indexOf(lastModifiedSorted.get(j));
                FilenameAdapter.UriPathName uriPathName = uriPathNames[sortedLastModifiedIdx];
                String startNote = j < mainActivity.noteKeys.length || j < mainActivity.octaveKeys.length ? mainActivity.noteKeys[j] + mainActivity.octaveKeys[j] : mainActivity.getString(R.string.unassigned);
                String interval = j < mainActivity.intervalKeys.length ? mainActivity.intervalKeys[j] : mainActivity.getString(R.string.unassigned);
                String label = mainActivity.getString(
                        R.string.filename_with_key,
                        j + 1,
                        uriPathName.getName(),
                        startNote,
                        interval);
                sortedUriPathNames[j] = new FilenameAdapter.UriPathName(uriPathName.getUri(), uriPathName.getPath(), uriPathName.getName(), label);
                uriStrings[j] = sortedUriPathNames[j].getUri().toString();
            }
            mainActivity.sortByName = false;
            mainActivity.sortByDate = true;
            mainActivity.filenames = uriStrings;
        }

        recyclerView.setAdapter(new FilenameAdapter(sortedUriPathNames, mainActivity.soundPlayer));
    }
}

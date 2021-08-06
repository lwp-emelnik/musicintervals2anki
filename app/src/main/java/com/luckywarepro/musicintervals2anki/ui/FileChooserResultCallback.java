package com.luckywarepro.musicintervals2anki.ui;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.luckywarepro.musicintervals2anki.R;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class FileChooserResultCallback implements ActivityResultCallback<ActivityResult> {
    private final MainActivity mainActivity;

    public FileChooserResultCallback(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onActivityResult(ActivityResult result) {
        if (result.getResultCode() != AppCompatActivity.RESULT_OK) {
            return;
        }

        Intent data = result.getData();
        final ArrayList<Uri> uriList = new ArrayList<>();
        if (data != null) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    uriList.add(uri);
                }
            } else {
                Uri uri = data.getData();
                uriList.add(uri);
            }
        }

        ContentResolver resolver = mainActivity.getContentResolver();
        final ArrayList<String> names = new ArrayList<>(uriList.size());
        final ArrayList<Long> dates = new ArrayList<>(uriList.size());
        for (Uri uri : uriList) {
            Cursor cursor = resolver.query(uri, null, null, null, null);
            int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int dateIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
            cursor.moveToFirst();
            names.add(cursor.getString(nameIdx));
            dates.add(cursor.getLong(dateIdx));
            cursor.close();
        }

        mainActivity.intersectingNames = new HashSet<>(names).size() != names.size();
        final ArrayList<String> namesSorted = new ArrayList<>(names);
        namesSorted.sort(MainActivity.COMPARATOR_FILE_NAME);
        mainActivity.intersectingDates = new HashSet<>(dates).size() != dates.size();
        final ArrayList<Long> datesSorted = new ArrayList<>(dates);
        datesSorted.sort(MainActivity.COMPARATOR_FILE_DATE);

        boolean areKeysUnique = !mainActivity.intersectingNames && !mainActivity.intersectingDates;

        String[] uriStrings;
        if (mainActivity.intersectingNames && mainActivity.intersectingDates) {
            uriStrings = new String[]{};
            new AlertDialog.Builder(mainActivity)
                    .setMessage(R.string.intersecting_sorting_keys)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        } else {
            uriStrings = new String[uriList.size()];
            for (int i = 0; i < uriList.size(); i++) {
                int sortedNameIdx = names.indexOf(namesSorted.get(i));
                int sortedDateIdx = dates.indexOf(datesSorted.get(i));
                if (areKeysUnique && sortedNameIdx != sortedDateIdx) {
                    mainActivity.mismatchingSorting = true;
                    mainActivity.selectedFilenames = new String[]{};
                    mainActivity.showMismatchingSortingDialog(uriList, names, namesSorted, dates, datesSorted);
                    return;
                }
                uriStrings[i] = uriList.get(!mainActivity.intersectingNames ? sortedNameIdx : sortedDateIdx).toString();
            }
        }

        mainActivity.selectedFilenames = uriStrings;
        mainActivity.sortByName = !mainActivity.intersectingNames && mainActivity.intersectingDates;
        mainActivity.sortByDate = !mainActivity.intersectingDates && mainActivity.intersectingNames;
        mainActivity.mismatchingSorting = mainActivity.sortByName || mainActivity.sortByDate;
        mainActivity.afterSelecting = true;
    }
}

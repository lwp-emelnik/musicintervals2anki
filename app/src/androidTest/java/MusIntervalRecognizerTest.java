
import android.util.Log;

import com.ichi2.apisample.MusIntervalRecognizer;

import org.junit.Test;

import static org.junit.Assert.*;


import java.io.File;
import java.util.ArrayList;

public class MusIntervalRecognizerTest {

    @Test
    public void testStartNote() {
        final String samplesDirPath = "/storage/emulated/0/MusicIntervals2Anki/";
        File samplesDir = new File(samplesDirPath);
        String[] sampleNames = samplesDir.list();
        ArrayList<String> passed = new ArrayList<>();
        ArrayList<String> failed = new ArrayList<>();
        for (String sampleName : sampleNames) {
            try {
                String[] notes = new MusIntervalRecognizer(samplesDirPath + sampleName).getNotes();

                if (notes[0].equals(sampleName.split("_")[0])) {
                    passed.add(sampleName);
                } else {
                    failed.add(sampleName);
                }
            } catch (Exception e) {
                failed.add(sampleName);
            }

        }
        Log.i("Passed", passed.size() + "\n" + String.join("\n", passed));
        Log.i("Failed", failed.size() + "\n" + String.join("\n", failed));
        double successRate = (double) passed.size() / (double) sampleNames.length;
        Log.i("Success rate", String.format("%.2f", successRate));
        assertEquals(successRate, 1.0, 0);
    }

    @Test
    public void testNotesLength() {
        final String samplesDirPath = "/storage/emulated/0/MusicIntervals2Anki/";
        File samplesDir = new File(samplesDirPath);
        String[] sampleNames = samplesDir.list();
        ArrayList<String> passed = new ArrayList<>();
        ArrayList<String> failed = new ArrayList<>();
        ArrayList<String> crashed = new ArrayList<>();
        for (String sampleName : sampleNames) {
            try {
                String[] notes = new MusIntervalRecognizer(samplesDirPath + sampleName).getNotes();

                if (notes.length == 2) {
                    passed.add(sampleName);
                } else {
                    failed.add(sampleName);
                }
            } catch (Exception e) {
                crashed.add(sampleName);
            }

        }
        Log.i("Passed", passed.size() + "\n" + String.join("\n", passed));
        Log.i("Failed", failed.size() + "\n" + String.join("\n", failed));
        Log.i("Crashed", crashed.size() + "\n" + String.join("\n", crashed));
        double successRate = (double) passed.size() / (double) sampleNames.length;
        Log.i("Success rate", String.format("%.2f", successRate));
        assertEquals(successRate, 1.0, 0);
    }
}

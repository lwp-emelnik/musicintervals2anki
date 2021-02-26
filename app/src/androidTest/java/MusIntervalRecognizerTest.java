
import android.util.Log;

import com.ichi2.apisample.MusInterval;
import com.ichi2.apisample.MusIntervalRecognizer;

import org.junit.Test;

import static org.junit.Assert.*;


import java.io.File;
import java.util.ArrayList;

public class MusIntervalRecognizerTest {

    final static String samplesDirPath = "/storage/emulated/0/MusicIntervals2Anki/";

    @Test
    public void testStartNote() {
        File samplesDir = new File(samplesDirPath);
        String[] sampleNames = samplesDir.list();
        ArrayList<String> passed = new ArrayList<>();
        ArrayList<String[]> failed = new ArrayList<>();
        for (String sampleName : sampleNames) {
            try {
                String[] notes = new MusIntervalRecognizer(samplesDirPath + sampleName).getNotes();
                if (notes[0].equals(sampleName.split("_")[0])) {
                    passed.add(sampleName);
                } else {
                    failed.add(new String[]{notes[0], sampleName});
                }
            } catch (Exception e) {
                failed.add(new String[]{"crash", sampleName});
            }

        }
        Log.i("Passed", passed.size() + "\n" + String.join("\n", passed));
        String failMsg = failed.size() + "\n";
        for (String[] s : failed) {
            failMsg += String.join(" - ", s) + "\n";
        }
        Log.i("Failed", failMsg);
        double successRate = (double) passed.size() / (double) sampleNames.length;
        assertEquals(successRate, 1.0, 0);
    }

    @Test
    public void testNotesLength() {
        File samplesDir = new File(samplesDirPath);
        String[] sampleNames = samplesDir.list();
        ArrayList<String> passed = new ArrayList<>();
        ArrayList<String[]> failed = new ArrayList<>();
        for (String sampleName : sampleNames) {
            try {
                String[] notes = new MusIntervalRecognizer(samplesDirPath + sampleName).getNotes();
                if (notes.length == 2) {
                    passed.add(sampleName);
                } else {
                    failed.add(new String[]{String.valueOf(notes.length), sampleName});
                }
            } catch (Exception e) {
                failed.add(new String[]{"crash (1)", sampleName});
            }
        }
        Log.i("Passed", passed.size() + "\n" + String.join("\n", passed));
        String failMsg = failed.size() + "\n";
        for (String[] s : failed) {
            failMsg += String.join(" - ", s) + "\n";
        }
        Log.i("Failed", failMsg);
        double successRate = (double) passed.size() / (double) sampleNames.length;
        assertEquals(successRate, 1.0, 0);
    }

    @Test
    public void testMusInterval() {
        File samplesDir = new File(samplesDirPath);
        String[] sampleNames = samplesDir.list();
        ArrayList<String> passed = new ArrayList<>();
        ArrayList<String[]> failed = new ArrayList<>();
        for (String sampleName : sampleNames) {
            try {
                String[] notes = new MusIntervalRecognizer(samplesDirPath + sampleName).getNotes();
                if (notes.length == 2) {
                    String note1 = notes[0];
                    String note2 = notes[1];
                    String sampleData[] = sampleName.split("_");
                    String sampleStartNote = sampleData[0];
                    String sampleDirection = sampleData[1];
                    String recognizedDirection = MusIntervalRecognizer.getDirection(note1, note2);
                    String sampleInterval = sampleData[3];
                    int recognizedDistance = MusIntervalRecognizer.getDistance(note1, note2);
                    String recognizedInterval = recognizedDistance < MusInterval.Fields.Interval.VALUES.length ?
                            MusInterval.Fields.Interval.VALUES[recognizedDistance] :
                            String.valueOf(recognizedDistance);
                    if (sampleStartNote.equals(note1) &&
                            sampleDirection.equals(recognizedDirection) &&
                            sampleInterval.equals(recognizedInterval)) {
                        passed.add(sampleName);
                    } else {
                        failed.add(new String[]{note1, recognizedDirection, recognizedInterval, sampleName});
                    }
                } else {
                    failed.add(new String[]{String.valueOf(notes.length), sampleName});
                }
            } catch (Exception e) {
                failed.add(new String[]{"crash", sampleName});
            }

        }
        Log.i("Passed", passed.size() + "\n" + String.join("\n", passed));
        String failMsg = failed.size() + "\n";
        for (String[] s : failed) {
            failMsg += String.join(" - ", s) + "\n";
        }
        Log.i("Failed", failMsg);
        double successRate = (double) passed.size() / (double) sampleNames.length;
        assertEquals(successRate, 1.0, 0);
    }
}

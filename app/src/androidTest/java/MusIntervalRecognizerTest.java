
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
                String[] notes = MusIntervalRecognizer.getNotes(samplesDirPath + sampleName);

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
        double successRate = (double)passed.size()/(double)failed.size();
        Log.i("Success rate", String.format("%.2f", successRate));
        assertEquals(successRate, 1.0, 0);
    }
}

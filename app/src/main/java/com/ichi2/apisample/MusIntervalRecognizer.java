package com.ichi2.apisample;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class MusIntervalRecognizer {
    private String filePath;
    private int sampleRate;
    private int channelCount;
    private double[] signal;
    private String[] notes;

    public MusIntervalRecognizer(String filePath) {
        this.filePath = filePath;
    }

    private void extractSignal() throws Exception {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(filePath);
        } catch (Exception e) {
            extractor.setDataSource(new FileInputStream(filePath).getFD());
        }
        MediaFormat format = extractor.getTrackFormat(0);
        String mime = format.getString(MediaFormat.KEY_MIME);
        extractor.selectTrack(0);
        MediaCodec decoder = MediaCodec.createDecoderByType(mime);
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

        sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

        int signalLength = audioData.size() / channelCount;
        if (signalLength == 0) {
            throw new IllegalArgumentException();
        }
        signal = new double[signalLength];
        for (int i = 0; i < signalLength; i++) {
            signal[i] = (audioData.get(channelCount * i) & 0xFF) | (audioData.get(channelCount * i + 1) << 8);
        }
    }

    public String[] getNotes() throws Exception {
        if (notes == null) {
            extractSignal();
            processSignal();
        }
        return notes;
    }


    private void processSignal() {
        final int chunkLength = 8192; // arbitrary
        int nChunks = signal.length / chunkLength;
        if (nChunks == 0) {
            throw new IllegalArgumentException();
        }
        double[][] chunks = new double[nChunks][];
        double[] chunkAmps = new double[nChunks];
        double maxAmp = Double.MIN_VALUE;
        for (int i = 0; i < nChunks; i++) {
            chunks[i] = new double[chunkLength];
            System.arraycopy(signal, i * chunkLength, chunks[i], 0, chunkLength);
            chunkAmps[i] = averageAbsolute(chunks[i]);
            if (chunkAmps[i] > maxAmp) {
                maxAmp = chunkAmps[i];
            }
        }

        final double peakCoefficient = 0.75; // arbitrary
        double peakThreshold = maxAmp * peakCoefficient;
        boolean isAbove = chunkAmps[0] > peakThreshold;
        LinkedList<int[]> peaksIndices = new LinkedList<>();
        if (isAbove) {
            peaksIndices.add(new int[2]);
            peaksIndices.getLast()[0] = 0;
        }
        for (int i = 1; i < chunkAmps.length; i++) {
            if (chunkAmps[i] < peakThreshold && isAbove) {
                isAbove = false;
                peaksIndices.getLast()[1] = i;
            } else if (chunkAmps[i] > peakThreshold && !isAbove) {
                isAbove = true;
                peaksIndices.add(new int[2]);
                peaksIndices.getLast()[0] = i;
            }
        }
        if (isAbove) {
            peaksIndices.getLast()[1] = chunkAmps.length - 1;
        }

        if (peaksIndices.size() < 2) {
            throw new IllegalArgumentException();
        }

        final double silenceCoefficient = 0.25; //arbitrary
        final double silenceThreshold = maxAmp * silenceCoefficient;
        int endChunkInx = peaksIndices.getLast()[1];
        while (endChunkInx < nChunks && chunkAmps[endChunkInx] > silenceThreshold) {
            endChunkInx++;
        }

        final double soundCoefficient = 0.75; // arbitrary
        final double pauseCoefficient = 0.5; // arbitrary
        LinkedList<int[]> notesIndices = new LinkedList<>();
        for (int i = 0; i < peaksIndices.size(); i++) {
            int[] peakIndices = peaksIndices.get(i);
            int peakStartIdx = peakIndices[0];
            int peakEndIdx = peakIndices[1];
            int nextPeakStartIdx = i == peaksIndices.size() - 1 ? -1 : peaksIndices.get(i + 1)[0];

            int noteStartIdx = peakStartIdx + (int) ((peakEndIdx - peakStartIdx) * soundCoefficient);
            int noteEndIdx = nextPeakStartIdx == -1 ? endChunkInx :
                    peakEndIdx + (int) ((nextPeakStartIdx - peakEndIdx) * pauseCoefficient);

            notesIndices.add(new int[]{noteStartIdx, noteEndIdx});
        }

        double[][] notesSegments = new double[notesIndices.size()][];
        for (int i = 0; i < notesSegments.length; i++) {
            int[] noteIndices = notesIndices.get(i);
            int noteStartChunkIdx = noteIndices[0];
            int noteEndChunkIdx = noteIndices[1];
            notesSegments[i] = new double[(noteEndChunkIdx - noteStartChunkIdx) * chunkLength];
            for (int j = noteStartChunkIdx; j < noteEndChunkIdx; j++) {
                System.arraycopy(chunks[j], 0, notesSegments[i], (j - noteStartChunkIdx) * chunkLength, chunkLength);
            }
        }

        notes = new String[notesSegments.length];
        for (int i = 0; i < notes.length; i++) {
            double dominantFrequency = getDominantFrequency(notesSegments[i], sampleRate, channelCount);
            notes[i] = getNote(dominantFrequency);
        }
    }

    private static double averageAbsolute(double[] arr) {
        double sum = 0;
        for (double n : arr) {
            sum += Math.abs(n);
        }
        return Math.sqrt(sum / arr.length);
    }

    private static int getDominantFrequency(double[] signal, int sampleRate, int channelCount) {
        int len = signal.length;
        double[] waveTransformReal = new double[len];
        double[] waveTransformImg = new double[len];
        System.arraycopy(signal, 0, waveTransformReal, 0, len);

        Fft.transform(waveTransformReal, waveTransformImg);

        double[] abs = new double[len];
        for (int i = 0; i < len; i++) {
            abs[i] = Math.sqrt(waveTransformReal[i] * waveTransformReal[i] + waveTransformImg[i] * waveTransformImg[i]);
        }

        int maxIndex = 0;
        for (int i = 0; i < len; i++) {
            if (abs[i] > abs[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex * sampleRate * channelCount / len;
    }

    private final static ArrayList<String> noteNames = new ArrayList<>(Arrays.asList("C0", "C#0", "D0",
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
            if (tempIndex < 0 || tempIndex >= noteNames.size()) {
                return "unknown";
            }
            lastFrequency = tempFrequency;
            int diff = tempIndex - a4Index;
            tempFrequency = a4Frequency * Math.pow(rootThingy, diff);
            lastFlag = flag;
            flag = frequency > tempFrequency;
        }
        if (Math.abs(Math.abs(frequency) - Math.abs(tempFrequency)) < Math
                .abs(Math.abs(frequency) - Math.abs(lastFrequency))) {
            return noteNames.get(tempIndex);
        } else {
            return noteNames.get(lastIndex);
        }
    }

    public static int getDistance(String note1, String note2) {
        final String[] intervals = MusInterval.Fields.Interval.VALUES;
        int distance = Math.abs(noteNames.indexOf(note1) - noteNames.indexOf(note2));
        if (distance >= intervals.length) {
            return 0;
        }
        return distance;
    }

    public static String getDirection(String note1, String note2) {
        int index1 = noteNames.indexOf(note1);
        int index2 = noteNames.indexOf(note2);
        if (index2 - index1 > 0) {
            return MusInterval.Fields.Direction.ASC;
        } else if (index2 - index1 < 0) {
            return MusInterval.Fields.Direction.DESC;
        } else {
            throw new IllegalArgumentException();
        }
    }
}

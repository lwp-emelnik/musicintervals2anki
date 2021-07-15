package com.ichi2.apisample.model;

import com.ichi2.apisample.helper.AnkiDroidHelper;
import com.ichi2.apisample.helper.MapUtil;
import com.ichi2.apisample.helper.equality.EqualityChecker;
import com.ichi2.apisample.helper.equality.FieldEqualityChecker;
import com.ichi2.apisample.helper.search.SearchExpressionMaker;
import com.ichi2.apisample.validation.ValidationUtil;
import com.ichi2.apisample.validation.Validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public abstract class RelatedIntervalSoundField {
    public static final String TAG_POINTING = "pointing";
    public static final String TAG_POINTED = "pointed";

    private final AnkiDroidHelper helper;
    private final MusInterval musInterval;

    private final String soundField;
    private final String startNoteField;
    private final String directionField;
    private final String timingField;
    private final String intervalField;
    private final String versionField;

    private RelatedIntervalSoundField reverse;

    private String relatedSoundField;
    private String relatedSoundAltField;
    private String reverseRelatedSoundField;
    private String reverseRelatedSoundAltField;

    public RelatedIntervalSoundField(AnkiDroidHelper helper, MusInterval musInterval) {
        this.helper = helper;
        this.musInterval = musInterval;

        soundField = musInterval.modelFields.getOrDefault(MusInterval.Fields.SOUND, MusInterval.Fields.SOUND);
        startNoteField = musInterval.modelFields.getOrDefault(MusInterval.Fields.START_NOTE, MusInterval.Fields.START_NOTE);
        directionField = musInterval.modelFields.getOrDefault(MusInterval.Fields.DIRECTION, MusInterval.Fields.DIRECTION);
        timingField = musInterval.modelFields.getOrDefault(MusInterval.Fields.TIMING, MusInterval.Fields.TIMING);
        intervalField = musInterval.modelFields.getOrDefault(MusInterval.Fields.INTERVAL, MusInterval.Fields.INTERVAL);
        versionField = musInterval.modelFields.getOrDefault(MusInterval.Fields.VERSION, MusInterval.Fields.VERSION);
    }

    public void setReverse(RelatedIntervalSoundField reverse) {
        this.reverse = reverse;

        String relatedSoundFieldKey = getFieldKey();
        relatedSoundField = musInterval.modelFields.getOrDefault(relatedSoundFieldKey, relatedSoundFieldKey);
        String relatedSoundAltFieldKey = getAltFieldKey();
        relatedSoundAltField = musInterval.modelFields.getOrDefault(relatedSoundAltFieldKey, relatedSoundAltFieldKey);
        String reverseRelatedSoundFieldKey = reverse.getFieldKey();
        reverseRelatedSoundField = musInterval.modelFields.getOrDefault(reverseRelatedSoundFieldKey, reverseRelatedSoundFieldKey);
        String reverseRelatedSoundAltFieldKey = reverse.getAltFieldKey();
        reverseRelatedSoundAltField = musInterval.modelFields.getOrDefault(reverseRelatedSoundAltFieldKey, reverseRelatedSoundAltFieldKey);
    }

    public boolean isSuspicious(Map<String, String> noteData, Map<String, Map<String, String>> soundDict, Map<String, Set<Map<String, String>>> suspiciousRelatedNotesData) {
        final String interval = noteData.getOrDefault(intervalField, "");
        final int intervalIdx = MusInterval.Fields.Interval.getIndex(interval);

        final String relatedSound = noteData.getOrDefault(relatedSoundField, "");

        Map<String, String> keyData = getIntervalIdentityData(noteData);
        boolean suspicious = false;
        if (!relatedSound.isEmpty()) {
            Map<String, String> relatedNoteData = soundDict.getOrDefault(relatedSound, null);
            if (relatedNoteData != null) {
                String relatedInterval = relatedNoteData.getOrDefault(intervalField, "");
                Map<String, String> relatedNoteKeyData = getIntervalIdentityData(relatedNoteData);
                if (!isEqualData(keyData, relatedNoteKeyData, musInterval.defaultValues, musInterval.relativesEqualityCheckers, false)
                        || !isCorrectRelation(intervalIdx, relatedInterval)) {
                    Set<Map<String, String>> pointed = suspiciousRelatedNotesData.getOrDefault(relatedSoundField, new HashSet<Map<String, String>>());
                    pointed.add(relatedNoteData);
                    suspiciousRelatedNotesData.put(relatedSoundField, pointed);
                    suspicious = true;
                }
            } else {
                suspicious = true;
            }
        }
        return suspicious;
    }

    private boolean isCorrectRelation(int intervalIdx, String relatedInterval) {
        return isRelationPossible(intervalIdx) && relatedInterval.equalsIgnoreCase(getRelatedInterval(intervalIdx));
    }

    public int autoFill(Map<String, String> noteData, boolean updateReverse) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        final String startNote = noteData.getOrDefault(startNoteField, "");
        final String interval = noteData.getOrDefault(intervalField, "");
        final String direction = noteData.getOrDefault(directionField, "");
        final String timing = noteData.getOrDefault(timingField, "");
        String relatedSound = noteData.containsKey(relatedSoundField) ? noteData.remove(relatedSoundField) : "";
        String relatedSoundAlt = noteData.containsKey(relatedSoundAltField) ? noteData.remove(relatedSoundAltField) : "";
        final String reverseRelatedSound = noteData.containsKey(reverseRelatedSoundField) ? noteData.remove(reverseRelatedSoundField) : "'";
        final String reverseRelatedSoundAlt = noteData.containsKey(reverseRelatedSoundAltField) ? noteData.remove(reverseRelatedSoundAltField) : "";
        final String sound = noteData.containsKey(soundField) ? noteData.remove(soundField) : "";
        final String version = noteData.containsKey(versionField) ? noteData.remove(versionField) : "";

        final int intervalIdx = MusInterval.Fields.Interval.getIndex(interval);

        int updatedLinks = 0;
        if (isRelationPossible(intervalIdx)) {
            String relatedInterval = getRelatedInterval(intervalIdx);
            noteData.put(intervalField, relatedInterval);
            if (MusInterval.Fields.Interval.VALUE_UNISON.equalsIgnoreCase(interval) ||
                    MusInterval.Fields.Interval.VALUE_UNISON.equals(relatedInterval)) {
                noteData.put(directionField, "");
            }
            LinkedList<Map<String, String>> relatedNotesData = helper.findNotes(
                    musInterval.modelId,
                    noteData,
                    musInterval.defaultValues,
                    musInterval.relativesSearchExpressionMakers,
                    musInterval.equalityCheckers
            );
            LinkedList<Map<String, String>> relatedAltNotesData = new LinkedList<>();
            if (timing.equalsIgnoreCase(MusInterval.Fields.Timing.HARMONIC)) {
                String altStartNote = MusInterval.Fields.StartNote.getEndNote(startNote, direction, interval);
                String altDirection = direction.equalsIgnoreCase(MusInterval.Fields.Direction.ASC) ?
                        MusInterval.Fields.Direction.DESC : MusInterval.Fields.Direction.ASC;
                noteData.put(startNoteField, altStartNote);
                noteData.put(directionField, altDirection);
                relatedAltNotesData = helper.findNotes(
                        musInterval.modelId,
                        noteData,
                        musInterval.defaultValues,
                        musInterval.relativesSearchExpressionMakers,
                        musInterval.equalityCheckers
                );
                noteData.put(startNoteField, startNote);
                noteData.put(directionField, direction);
            }

            Iterator<Map<String, String>> iterator = relatedNotesData.iterator();
            boolean overAlt = false;
            outer:
            while (iterator.hasNext() || !overAlt) {
                if (!iterator.hasNext()) {
                    iterator = relatedAltNotesData.iterator();
                    overAlt = true;
                    continue;
                }
                Map<String, String> relatedData = iterator.next();
                long relatedId = Long.parseLong(relatedData.get(AnkiDroidHelper.KEY_ID));
                for (Map.Entry<String, SearchExpressionMaker> relativesMakers :
                        musInterval.relativesSearchExpressionMakers.entrySet()) {
                    String modelField = relativesMakers.getKey();
                    String fieldKey = MapUtil.getKeyByValue(musInterval.modelFields, modelField);
                    Validator[] validators = MusInterval.Fields.VALIDATORS.getOrDefault(fieldKey, new Validator[]{});
                    for (Validator validator : validators) {
                        boolean isValid = ValidationUtil.isValid(
                                validator,
                                musInterval.modelId,
                                relatedId,
                                relatedData,
                                fieldKey,
                                musInterval.modelFields,
                                helper,
                                true
                        );
                        if (!isValid) {
                            iterator.remove();
                            continue outer;
                        }
                    }
                }
            }

            if (updateReverse) {
                updatedLinks += updateReverse(
                        relatedNotesData, noteData, // @todo: revisit alt
                        sound, relatedInterval
                );
            }

            for (int i = 0; i < musInterval.relativesPriorityComparators.length; i++) {
                RelativesPriorityComparator comparator = musInterval.relativesPriorityComparators[i];
                comparator.setTargetValueFromData(noteData);

                relatedNotesData = comparator.getLeadingRelatives(relatedNotesData);
                relatedAltNotesData = comparator.getLeadingRelatives(relatedAltNotesData);
            }

            String newRelatedSound = getValue(relatedNotesData, relatedSound);
            if (!relatedSound.equals(newRelatedSound)) {
                relatedSound = newRelatedSound;
                updatedLinks++;
            }
            String newRelatedSoundAlt = getValue(relatedAltNotesData, relatedSoundAlt);
            if (!relatedSoundAlt.equals(newRelatedSoundAlt)) {
                relatedSoundAlt = newRelatedSoundAlt;
                updatedLinks++;
            }
        }

        noteData.put(intervalField, interval);
        noteData.put(directionField, direction);
        noteData.put(relatedSoundField, relatedSound);
        noteData.put(relatedSoundAltField, relatedSoundAlt);
        noteData.put(reverseRelatedSoundField, reverseRelatedSound);
        noteData.put(reverseRelatedSoundAltField, reverseRelatedSoundAlt);
        noteData.put(soundField, sound);
        noteData.put(versionField, version);
        return updatedLinks;
    }

    private int updateReverse(LinkedList<Map<String, String>> relatedNotesData, Map<String, String> data,
                              String sound, String relatedInterval)
            throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        String startNoteField = musInterval.modelFields.getOrDefault(MusInterval.Fields.START_NOTE, MusInterval.Fields.START_NOTE);
        String timingField = musInterval.modelFields.getOrDefault(MusInterval.Fields.TIMING, MusInterval.Fields.TIMING);
        String startNote = data.getOrDefault(startNoteField, "");
        String direction = data.getOrDefault(directionField, "");
        String timing = data.getOrDefault(timingField, "");
        int updatedLinks = 0;
        outer:
        for (Map<String, String> relatedData : relatedNotesData) {
            String relatedStartNote = relatedData.getOrDefault(startNoteField, "");
            String relatedDirection = relatedData.getOrDefault(directionField, "");
            String relatedTiming = relatedData.getOrDefault(timingField, "");
            if (!direction.equalsIgnoreCase(relatedDirection) && !startNote.equalsIgnoreCase(relatedStartNote) &&
                    MusInterval.Fields.Timing.HARMONIC.equalsIgnoreCase(timing) &&
                    MusInterval.Fields.Timing.HARMONIC.equalsIgnoreCase(relatedTiming)) {
                reverseRelatedSoundField = reverseRelatedSoundAltField;
            }
            final String relatedReverseSound = relatedData.getOrDefault(reverseRelatedSoundField, "");
            if (!relatedReverseSound.isEmpty()) {
                Map<String, String> searchData = new HashMap<String, String>() {{
                    put(soundField, relatedReverseSound);
                }};
                LinkedList<Map<String, String>> currentReverseSearchResult = helper.findNotes(
                        musInterval.modelId,
                        searchData,
                        musInterval.defaultValues,
                        musInterval.searchExpressionMakers,
                        musInterval.equalityCheckers
                );
                if (currentReverseSearchResult.size() != 1) {
                    continue;
                }

                Map<String, String> currentReverseData = currentReverseSearchResult.getFirst();
                long currentReverseId = Long.parseLong(currentReverseData.get(AnkiDroidHelper.KEY_ID));
                for (Map.Entry<String, Validator[]> fieldValidators : MusInterval.Fields.VALIDATORS.entrySet()) {
                    String fieldKey = fieldValidators.getKey();
                    Validator[] validators = fieldValidators.getValue();
                    for (Validator validator : validators) {
                        boolean isValid = ValidationUtil.isValid(
                                validator,
                                musInterval.modelId,
                                currentReverseId,
                                currentReverseData,
                                fieldKey,
                                musInterval.modelFields,
                                helper,
                                true
                        );
                        if (!isValid) {
                            continue outer;
                        }
                    }
                }

                int relatedIntervalIdx = MusInterval.Fields.Interval.getIndex(relatedInterval);
                String currentReverseInterval = currentReverseData.getOrDefault(intervalField, "");
                if (!reverse.isCorrectRelation(relatedIntervalIdx, currentReverseInterval) ||
                        !isEqualData(
                                getIntervalIdentityData(relatedData),
                                getIntervalIdentityData(currentReverseData),
                                musInterval.defaultValues,
                                musInterval.relativesEqualityCheckers,
                                true)) {
                    continue;
                }

                for (int i = 0; i < musInterval.relativesPriorityComparators.length - 1; i++) {
                    RelativesPriorityComparator comparator = musInterval.relativesPriorityComparators[i];
                    comparator.setTargetValueFromData(relatedData);
                    if (comparator.compare(data, currentReverseData) < 0) {
                        continue outer;
                    }
                }
            }
            relatedData.put(reverseRelatedSoundField, sound);
            long relatedId = Long.parseLong(relatedData.get(AnkiDroidHelper.KEY_ID));
            helper.updateNote(musInterval.modelId, relatedId, relatedData);
            updatedLinks++;
        }
        return updatedLinks;
    }

    private String getValue(LinkedList<Map<String, String>> relatedNotesData, String relatedSound) {
        return !relatedNotesData.isEmpty() ? relatedNotesData.getFirst().get(soundField) : relatedSound;
    }

    protected abstract String getFieldKey();

    protected abstract String getAltFieldKey();

    protected abstract boolean isRelationPossible(int intervalIdx);

    protected String getRelatedInterval(int intervalIdx) {
        return isRelationPossible(intervalIdx) ? MusInterval.Fields.Interval.VALUES[intervalIdx + getDistance()] : null;
    }

    protected abstract int getDistance();

    private Map<String, String> getIntervalIdentityData(Map<String, String> data) {
        return new HashMap<String, String>(data) {{
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND));
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_SMALLER));
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_SMALLER_ALT));
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_LARGER));
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_LARGER_ALT));
            remove(musInterval.modelFields.get(MusInterval.Fields.VERSION));
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
    }

    private boolean isEqualData(Map<String, String> data1, Map<String, String> data2,
                                Map<String, String> modelFieldsDefaultValues,
                                Map<String, EqualityChecker> modelFieldsEqualityCheckers,
                                boolean reverse) {
        String interval1 = data1.getOrDefault(intervalField, "");
        int interval1Idx = MusInterval.Fields.Interval.getIndex(interval1);
        String interval2 = data2.getOrDefault(intervalField, "");
        int interval2Idx = MusInterval.Fields.Interval.getIndex(interval2);
        if (interval2Idx - interval1Idx != (reverse ? this.reverse.getDistance() : getDistance())) {
            return false;
        }
        Set<String> keySet1 = new HashSet<>(data1.keySet());
        keySet1.remove(intervalField);
        Set<String> keySet2 = new HashSet<>(data2.keySet());
        keySet2.remove(intervalField);
        if (!keySet1.equals(keySet2)) {
            return false;
        }
        for (String key : keySet1) {
            String defaultValue = modelFieldsDefaultValues.getOrDefault(key, "");
            String value1 = data1.getOrDefault(key, "");
            String value2 = data2.getOrDefault(key, "");
            boolean defaultEquality = !defaultValue.isEmpty() &&
                    ((value1.equalsIgnoreCase(defaultValue) && value2.isEmpty() || value1.isEmpty() && value2.equalsIgnoreCase(defaultValue))
                            || (value1.isEmpty() && value2.isEmpty()));
            EqualityChecker defaultEqualityChecker = new FieldEqualityChecker(key, AnkiDroidHelper.DEFAULT_EQUALITY_CHECKER);
            EqualityChecker equalityChecker = modelFieldsEqualityCheckers.getOrDefault(key, defaultEqualityChecker);
            if (!equalityChecker.areEqual(data1, data2) && !defaultEquality) {
                return false;
            }
        }
        return true;
    }
}

class SmallerIntervalSoundField extends RelatedIntervalSoundField {
    public SmallerIntervalSoundField(AnkiDroidHelper helper, MusInterval musInterval) {
        super(helper, musInterval);
    }

    @Override
    protected String getFieldKey() {
        return MusInterval.Fields.SOUND_SMALLER;
    }

    @Override
    protected String getAltFieldKey() {
        return MusInterval.Fields.SOUND_SMALLER_ALT;
    }

    @Override
    protected boolean isRelationPossible(int intervalIdx) {
        return intervalIdx > 0;
    }

    @Override
    protected int getDistance() {
        return -1;
    }
}

class LargerIntervalSoundField extends RelatedIntervalSoundField {
    public LargerIntervalSoundField(AnkiDroidHelper helper, MusInterval musInterval) {
        super(helper, musInterval);
    }

    @Override
    protected String getFieldKey() {
        return MusInterval.Fields.SOUND_LARGER;
    }

    @Override
    protected String getAltFieldKey() {
        return MusInterval.Fields.SOUND_LARGER_ALT;
    }

    @Override
    protected boolean isRelationPossible(int intervalIdx) {
        return intervalIdx < MusInterval.Fields.Interval.VALUES.length - 1;
    }

    @Override
    protected int getDistance() {
        return 1;
    }
}
package com.luckywarepro.musicintervals2anki.model;

import com.luckywarepro.musicintervals2anki.R;
import com.luckywarepro.musicintervals2anki.helper.AnkiDroidHelper;
import com.luckywarepro.musicintervals2anki.helper.AudioFile;
import com.luckywarepro.musicintervals2anki.helper.equality.AnyEqualityChecker;
import com.luckywarepro.musicintervals2anki.helper.equality.DoubleValueEqualityChecker;
import com.luckywarepro.musicintervals2anki.helper.equality.EqualityChecker;
import com.luckywarepro.musicintervals2anki.helper.equality.FieldEqualityChecker;
import com.luckywarepro.musicintervals2anki.helper.equality.IntegerValueEqualityChecker;
import com.luckywarepro.musicintervals2anki.helper.equality.NoteEqualityChecker;
import com.luckywarepro.musicintervals2anki.helper.search.AnySearchExpressionMaker;
import com.luckywarepro.musicintervals2anki.helper.search.DoubleSearchExpressionMaker;
import com.luckywarepro.musicintervals2anki.helper.search.IntegerSearchExpressionMaker;
import com.luckywarepro.musicintervals2anki.helper.search.SearchExpressionMaker;
import com.luckywarepro.musicintervals2anki.validation.PositiveDecimalValidator;
import com.luckywarepro.musicintervals2anki.validation.EmptyValidator;
import com.luckywarepro.musicintervals2anki.validation.IntegerRangeValidator;
import com.luckywarepro.musicintervals2anki.validation.PatternValidator;
import com.luckywarepro.musicintervals2anki.validation.FieldValidator;
import com.luckywarepro.musicintervals2anki.validation.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class MusInterval {

    /**
     * Pre-defined field names and values, used in the target AnkiDroid model.
     */
    public static class Fields {
        public static final String SOUND = "sound";
        public static final String SOUND_SMALLER = "sound.smaller";
        public static final String SOUND_SMALLER_ALT = "sound.smaller.alt";
        public static final String SOUND_LARGER = "sound.larger";
        public static final String SOUND_LARGER_ALT = "sound.larger.alt";
        public static final String START_NOTE = "note1";
        public static final String DIRECTION = "ascending_descending";
        public static final String TIMING = "melodic_harmonic";
        public static final String INTERVAL = "interval";
        public static final String TEMPO = "tempo";
        public static final String INSTRUMENT = "instrument";
        public static final String FIRST_NOTE_DURATION_COEFFICIENT = "note1.duration";
        public static final String VERSION = "mi2a.version";

        public static class StartNote {
            private static final String[] NOTES = new String[]{
                    "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
            };

            private static final String[] OCTAVES = new String[]{
                    "1", "2", "3", "4", "5", "6"
            };

            public static final String[] VALUES = new String[NOTES.length * OCTAVES.length];
            static {
                for (int i = 0; i < OCTAVES.length; i++) {
                    for (int j = 0; j < NOTES.length; j++) {
                        VALUES[i * NOTES.length + j] = NOTES[j] + OCTAVES[i];
                    }
                }
            }

            private final static String VALIDATION_PATTERN = " *[A-Ga-g]#?[1-6] *";

            public static int getIndex(String value) {
                for (int i = 0; i < VALUES.length; i++) {
                    if (VALUES[i].equalsIgnoreCase(value)) {
                        return i;
                    }
                }
                return -1;
            }

            public static String getEndNote(String startNote, String direction, String interval) {
                ArrayList<String> notes = new ArrayList<>(Arrays.asList(VALUES));
                int startIdx = notes.indexOf(startNote);
                int distance = Interval.getIndex(interval);
                if (startIdx == -1 || distance == -1) {
                    throw new IllegalArgumentException();
                }

                int endIdx;
                if (direction.equalsIgnoreCase(Direction.ASC)) {
                    endIdx = startIdx + distance;
                } else if (direction.equalsIgnoreCase(Direction.DESC)) {
                    endIdx = startIdx - distance;
                } else {
                    return startNote;
                }

                return endIdx >= 0 && endIdx < VALUES.length ? VALUES[endIdx] : null;
            }

            private static final NoteEqualityChecker EQUALITY_CHECKER =
                    new NoteEqualityChecker(new String[]{START_NOTE, DIRECTION, TIMING, INTERVAL}) {
                        private static final int IDX_START_NOTE = 0;
                        private static final int IDX_DIRECTION = 1;
                        private static final int IDX_TIMING = 2;
                        private static final int IDX_INTERVAL = 3;

                        @Override
                        public boolean areEqual(Map<String, String> data1, Map<String, String> data2) {
                            String startNoteField = modelFields[IDX_START_NOTE];
                            String directionField = modelFields[IDX_DIRECTION];
                            String timingField = modelFields[IDX_TIMING];
                            String intervalField = modelFields[IDX_INTERVAL];
                            String startNote1 = Objects.requireNonNull(data1.getOrDefault(startNoteField, ""));
                            String startNote2 = Objects.requireNonNull(data2.getOrDefault(startNoteField, ""));
                            String direction1 = Objects.requireNonNull(data1.getOrDefault(directionField, ""));
                            String direction2 = Objects.requireNonNull(data2.getOrDefault(directionField, ""));
                            String timing1 = Objects.requireNonNull(data1.getOrDefault(timingField, ""));
                            String timing2 = Objects.requireNonNull(data2.getOrDefault(timingField, ""));
                            String interval1 = Objects.requireNonNull(data1.getOrDefault(intervalField, ""));
                            String interval2 = Objects.requireNonNull(data2.getOrDefault(intervalField, ""));
                            boolean regularEquality = match(startNote1, startNote2);
                            boolean harmonicEquality = (interval1.equals("%") || interval1.equalsIgnoreCase(interval2)) &&
                                    (direction1.equalsIgnoreCase(Direction.ASC) && direction2.equalsIgnoreCase(Direction.DESC) ||
                                            direction1.equalsIgnoreCase(Direction.DESC) && direction2.equalsIgnoreCase(Direction.ASC)) &&
                                    timing1.equalsIgnoreCase(Timing.HARMONIC) && timing2.equalsIgnoreCase(Timing.HARMONIC) &&
                                    match(startNote1, StartNote.getEndNote(startNote2, direction2, interval2));
                            return regularEquality || harmonicEquality;
                        }
                    };

            private static boolean match(String pattern, String value) {
                boolean noteProvided = !pattern.startsWith("%");
                boolean octaveProvided = !pattern.endsWith("%");
                if (noteProvided || octaveProvided) {
                    if (noteProvided && octaveProvided) {
                        return pattern.equalsIgnoreCase(value);
                    }
                    String providedPart = pattern.replaceAll("%", "");
                    boolean hasSharp = value.contains("#");
                    int octaveIdx = hasSharp ? 2 : 1;
                    if (value.length() < octaveIdx) {
                        return false;
                    }
                    if (noteProvided) {
                        return providedPart.equalsIgnoreCase(value.substring(0, octaveIdx));
                    } else {
                        return providedPart.equals(value.substring(octaveIdx));
                    }
                } else {
                    return true;
                }
            }
        }

        public static class Direction {
            public static final String ASC = "ascending";
            public static final String DESC = "descending";

            private static final NoteEqualityChecker EQUALITY_CHECKER =
                    new NoteEqualityChecker(new String[]{START_NOTE, DIRECTION, TIMING, INTERVAL}) {
                        private static final int IDX_START_NOTE = 0;
                        private static final int IDX_DIRECTION = 1;
                        private static final int IDX_TIMING = 2;
                        private static final int IDX_INTERVAL = 3;

                        @Override
                        public boolean areEqual(Map<String, String> data1, Map<String, String> data2) {
                            String startNoteField = modelFields[IDX_START_NOTE];
                            String directionField = modelFields[IDX_DIRECTION];
                            String timingField = modelFields[IDX_TIMING];
                            String intervalField = modelFields[IDX_INTERVAL];
                            String interval1 = Objects.requireNonNull(data1.getOrDefault(intervalField, ""));
                            String interval2 = Objects.requireNonNull(data2.getOrDefault(intervalField, ""));
                            boolean unisonEquality = interval1.equalsIgnoreCase(Interval.VALUE_UNISON) ||
                                    interval1.equals("%") && interval2.equalsIgnoreCase(Interval.VALUE_UNISON);
                            return unisonEquality || match(data1, data2, startNoteField, directionField, timingField, intervalField);
                        }
                    };

            private static final NoteEqualityChecker RELATIVES_EQUALITY_CHECKER =
                    new NoteEqualityChecker(new String[]{START_NOTE, DIRECTION, TIMING, INTERVAL}) {
                        private static final int IDX_START_NOTE = 0;
                        private static final int IDX_DIRECTION = 1;
                        private static final int IDX_TIMING = 2;
                        private static final int IDX_INTERVAL = 3;

                        @Override
                        public boolean areEqual(Map<String, String> data1, Map<String, String> data2) {
                            String startNoteField = modelFields[IDX_START_NOTE];
                            String directionField = modelFields[IDX_DIRECTION];
                            String timingField = modelFields[IDX_TIMING];
                            String intervalField = modelFields[IDX_INTERVAL];
                            return match(data1, data2, startNoteField, directionField, timingField, intervalField);
                        }
                    };

            private static boolean match(Map<String, String> data1, Map<String, String> data2,
                                         String startNoteField, String directionField, String timingField, String intervalField) {
                String startNote1 = Objects.requireNonNull(data1.getOrDefault(startNoteField, ""));
                String startNote2 = Objects.requireNonNull(data2.getOrDefault(startNoteField, ""));
                String direction1 = Objects.requireNonNull(data1.getOrDefault(directionField, ""));
                String direction2 = Objects.requireNonNull(data2.getOrDefault(directionField, ""));
                String timing1 = Objects.requireNonNull(data1.getOrDefault(timingField, ""));
                String timing2 = Objects.requireNonNull(data2.getOrDefault(timingField, ""));
                String interval1 = Objects.requireNonNull(data1.getOrDefault(intervalField, ""));
                String interval2 = Objects.requireNonNull(data2.getOrDefault(intervalField, ""));
                boolean regularEquality = direction1.equalsIgnoreCase(direction2);
                boolean harmonicEquality = (interval1.equals("%") || interval1.equalsIgnoreCase(interval2)) &&
                        (direction1.equalsIgnoreCase(Direction.ASC) && direction2.equalsIgnoreCase(Direction.DESC) ||
                                direction1.equalsIgnoreCase(Direction.DESC) && direction2.equalsIgnoreCase(Direction.ASC)) &&
                        timing1.equalsIgnoreCase(Timing.HARMONIC) && timing2.equalsIgnoreCase(Timing.HARMONIC) &&
                        StartNote.match(startNote1, StartNote.getEndNote(startNote2, direction2, interval2));
                return regularEquality || harmonicEquality;
            }
        }

        public static class Timing {
            public static final String MELODIC = "melodic";
            public static final String HARMONIC = "harmonic";
        }

        public static class Interval {
            public static final String VALUE_UNISON = "Uni";
            public static final String[] VALUES = new String[]{
                    VALUE_UNISON,
                    "min2", "Maj2",
                    "min3", "Maj3",
                    "P4",
                    "Tri",
                    "P5",
                    "min6", "Maj6",
                    "min7", "Maj7",
                    "Oct"
            };

            public static int getIndex(String value) {
                for (int i = 0; i < VALUES.length; i++) {
                    if (VALUES[i].equalsIgnoreCase(value)) {
                        return i;
                    }
                }
                return -1;
            }

            private static String getValidationPattern() {
                StringBuilder pattern = new StringBuilder();
                pattern.append("(?i) *(");
                for (int i = 0; i < VALUES.length; i++) {
                    if (i != 0) {
                        pattern.append("|");
                    }
                    pattern.append(VALUES[i]);
                }
                pattern.append(") *");
                return pattern.toString();
            }
        }

        public static class Tempo {
            public static final int MIN_VALUE = 20;
            public static final int MAX_VALUE = 400;

            private static final FieldValidator RANGE_VALIDATOR = new IntegerRangeValidator(Tempo.MIN_VALUE, Tempo.MAX_VALUE);
        }

        public static class FirstNoteDurationCoefficient {
            public static final double DEFAULT_VALUE = 1.0;

            private static final FieldValidator FORMAT_VALIDATOR = new PositiveDecimalValidator();
        }

        public static String[] getSignature(boolean versionField) {
            ArrayList<String> signature = new ArrayList<String>() {{
                add(SOUND);
                add(START_NOTE);
                add(FIRST_NOTE_DURATION_COEFFICIENT);
                add(DIRECTION);
                add(TIMING);
                add(INTERVAL);
                add(TEMPO);
                add(INSTRUMENT);
                add(SOUND_SMALLER);
                add(SOUND_SMALLER_ALT);
                add(SOUND_LARGER);
                add(SOUND_LARGER_ALT);
            }};
            if (versionField) {
                signature.add(VERSION);
            }
            return signature.toArray(new String[0]);
        }

        public static final Set<String> MULTIPLE_SELECTION_FIELDS = new HashSet<String>() {{
            add(Fields.START_NOTE);
            add(Fields.INTERVAL);
        }};

        public static final Map<String, String> DEFAULT_VALUES = new HashMap<String, String>() {{
            put(FIRST_NOTE_DURATION_COEFFICIENT, String.valueOf(FirstNoteDurationCoefficient.DEFAULT_VALUE));
        }};

        private static final Map<String, SearchExpressionMaker> SEARCH_EXPRESSION_MAKERS = new HashMap<String, SearchExpressionMaker>() {{
            put(START_NOTE, new AnySearchExpressionMaker());
            put(DIRECTION, new AnySearchExpressionMaker());
            put(TEMPO, new IntegerSearchExpressionMaker());
            put(FIRST_NOTE_DURATION_COEFFICIENT, new DoubleSearchExpressionMaker());
        }};
        static final Map<String, SearchExpressionMaker> RELATIVES_SEARCH_EXPRESSION_MAKERS = new HashMap<String, SearchExpressionMaker>() {{
            put(TEMPO, new AnySearchExpressionMaker() {
                @Override
                public boolean isDefinitive() {
                    return true;
                }
            });
        }};

        private static final Map<String, EqualityChecker> EQUALITY_CHECKERS = new HashMap<String, EqualityChecker>() {{
            put(START_NOTE, StartNote.EQUALITY_CHECKER);
            put(DIRECTION, Direction.EQUALITY_CHECKER);
            put(TEMPO, new FieldEqualityChecker(TEMPO, new IntegerValueEqualityChecker()));
            put(FIRST_NOTE_DURATION_COEFFICIENT, new FieldEqualityChecker(FIRST_NOTE_DURATION_COEFFICIENT, new DoubleValueEqualityChecker()));
        }};
        private static final Map<String, EqualityChecker> RELATIVES_EQUALITY_CHECKERS = new HashMap<String, EqualityChecker>() {{
            put(DIRECTION, Direction.RELATIVES_EQUALITY_CHECKER);
            put(TEMPO, new FieldEqualityChecker(TEMPO, new AnyEqualityChecker()));
        }};

        private static final RelativesPriorityComparator[] RELATIVES_PRIORITY_COMPARATORS = new RelativesPriorityComparator[]{
                new LowestDifferenceComparator(TEMPO),
                new LargestValueComparator(AnkiDroidHelper.KEY_ID)
        };

        private static final FieldValidator VALIDATOR_EMPTY = new EmptyValidator();
        private static final FieldValidator VALIDATOR_SOUND = new PatternValidator("^ *$|^ *\\[sound:.+\\] *$");
        public static final Map<String, Validator[]> VALIDATORS = new HashMap<String, Validator[]>() {{
            put(SOUND, new Validator[]{
                    VALIDATOR_EMPTY,
                    VALIDATOR_SOUND
            });
            put(SOUND_SMALLER, new Validator[]{
                    VALIDATOR_SOUND
            });
            put(SOUND_SMALLER_ALT, new Validator[]{
                    VALIDATOR_SOUND
            });
            put(SOUND_LARGER, new Validator[]{
                    VALIDATOR_SOUND
            });
            put(SOUND_LARGER_ALT, new Validator[]{
                    VALIDATOR_SOUND
            });
            put(START_NOTE, new Validator[]{
                    VALIDATOR_EMPTY,
                    new PatternValidator(StartNote.VALIDATION_PATTERN)
            });
            put(DIRECTION, new Validator[]{
                    VALIDATOR_EMPTY,
                    new PatternValidator(String.format("^ *$|(?i) *(%s|%s) *", Direction.ASC, Direction.DESC))
            });
            put(TIMING, new Validator[]{
                    VALIDATOR_EMPTY,
                    new PatternValidator(String.format("^ *$|(?i) *(%s|%s) *", Timing.MELODIC, Timing.HARMONIC))
            });
            put(INTERVAL, new Validator[]{
                    VALIDATOR_EMPTY,
                    new PatternValidator(Interval.getValidationPattern())
            });
            put(TEMPO, new Validator[]{
                    new PatternValidator("^ *[0-9]* *$"),
                    Tempo.RANGE_VALIDATOR
            });
            put(INSTRUMENT, new Validator[]{
                    VALIDATOR_EMPTY
            });
            put(FIRST_NOTE_DURATION_COEFFICIENT, new Validator[]{
                    FirstNoteDurationCoefficient.FORMAT_VALIDATOR
            });
        }};
    }

    public static class Builder {
        public static final String DEFAULT_DECK_NAME = "Music intervals";
        public static final String DEFAULT_MODEL_NAME = "Music.interval";
        public static final Map<String, String> DEFAULT_MODEL_FIELDS = new HashMap<>();
        static {
            String[] signature = MusInterval.Fields.getSignature(true);
            for (String fieldKey : signature) {
                DEFAULT_MODEL_FIELDS.put(fieldKey, fieldKey);
            }
        }

        public static final String SOUNDS = "sounds";
        public static final String SOUNDS_SMALLER = "sounds_smaller";
        public static final String SOUNDS_SMALLER_ALT = "sounds_smaller_alt";
        public static final String SOUNDS_LARGER = "sounds_larger";
        public static final String SOUNDS_LARGER_ALT = "sounds_larger_alt";
        public static final String NOTES = "notes";
        public static final String OCTAVES = "octaves";
        public static final String DIRECTION = "direction";
        public static final String TIMING = "timing";
        public static final String INTERVALS = "intervals";
        public static final String TEMPO = "tempo";
        public static final String INSTRUMENT = "instrument";
        public static final String FIRST_NOTE_DURATION_COEFFICIENT = "first_note_duration_coefficient";
        public static final String VERSION = "version";

        public static final String[] ADDING_MANDATORY_SELECTION_MEMBERS = new String[]{
                NOTES,
                OCTAVES,
                INTERVALS
        };
        public static final String[] ADDING_MANDATORY_SINGULAR_MEMBERS = new String[]{
                DIRECTION,
                TIMING,
                INSTRUMENT
        };

        private static final String[] EMPTY_SELECTION = new String[]{"%"};

        private final AnkiDroidHelper mHelper;

        private String mDeckName = DEFAULT_DECK_NAME;
        private String mModelName = DEFAULT_MODEL_NAME;
        private Map<String, String> mModelFields = DEFAULT_MODEL_FIELDS;
        private String[] mSounds = new String[]{};
        private String[] mSoundsSmaller = new String[]{};
        private String[] mSoundsSmallerAlt = new String[]{};
        private String[] mSoundsLarger = new String[]{};
        private String[] mSoundsLargerAlt = new String[]{};
        private String[] mNotes = new String[]{};
        private String[] mOctaves = new String[]{};
        private String mDirection = "";
        private String mTiming = "";
        private String[] mIntervals = new String[]{};
        private String mTempo = "";
        private String mInstrument = "";
        private String mFirstNoteDurationCoefficient = "";
        private String mVersion = "";

        private boolean mDefaultModel = false;
        private String[] mFields = new String[]{};
        private String[] mCards = new String[]{};
        private String[] mQfmt = new String[]{};
        private String[] mAfmt = new String[]{};
        private String mCss = "";

        public Builder(final AnkiDroidHelper helper) {
            mHelper = helper;
        }

        public MusInterval build() throws ValidationException {
            return new MusInterval(this);
        }

        public Builder model(String mn) {
            mModelName = mn;
            return this;
        }

        public Builder default_model(boolean dfmdl) {
            mDefaultModel = dfmdl;
            return this;
        }

        public Builder fields(String[] flds) {
            mFields = flds;
            return this;
        }

        public Builder cards(String[] cds) {
            mCards = cds;
            return this;
        }

        public Builder qfmt(String[] qfmt) {
            mQfmt = qfmt;
            return this;
        }

        public Builder afmt(String[] afmt) {
            mAfmt = afmt;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder css(String css) {
            mCss = css;
            return this;
        }

        public Builder model_fields(Map<String, String> mflds) {
            mModelFields = mflds;
            return this;
        }

        public Builder deck(String dn) {
            mDeckName = dn;
            return this;
        }

        public Builder sounds(String[] sds) {
            mSounds = sds;
            return this;
        }

        public Builder sounds_smaller(String[] sdss) {
            mSoundsSmaller = sdss;
            return this;
        }

        public Builder sounds_smaller_alt(String[] sdssa) {
            mSoundsSmallerAlt = sdssa;
            return this;
        }

        public Builder sounds_larger(String[] sdsl) {
            mSoundsLarger = sdsl;
            return this;
        }

        public Builder sounds_larger_alt(String[] sdsla) {
            mSoundsLargerAlt = sdsla;
            return this;
        }

        public Builder notes(String[] nts) {
            mNotes = nts;
            return this;
        }

        public Builder octaves(String[] ocs) {
            mOctaves = ocs;
            return this;
        }

        public Builder direction(String dr) {
            mDirection = dr;
            return this;
        }

        public Builder timing(String tm) {
            mTiming = tm;
            return this;
        }

        public Builder intervals(String[] ins) {
            mIntervals = ins;
            return this;
        }

        public Builder tempo(String tp) {
            mTempo = tp;
            return this;
        }

        public Builder instrument(String is) {
            mInstrument = is;
            return this;
        }

        public Builder first_note_duration_coefficient(String fndc) {
            mFirstNoteDurationCoefficient = fndc;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder version(String vs) {
            mVersion = vs;
            return this;
        }
    }

    public abstract static class Exception extends Throwable {}
    public static class CreateDeckException extends Exception {}
    public static class AddToAnkiException extends Exception {}
    public static class NoteNotExistsException extends Exception {}
    public static class UnexpectedSoundsAmountException extends Exception {
        private final int expectedAmount;
        private final int providedAmount;

        public UnexpectedSoundsAmountException(int expectedAmount, int providedAmount) {
            this.expectedAmount = expectedAmount;
            this.providedAmount = providedAmount;
        }

        public int getExpectedAmount() { return expectedAmount; }

        public int getProvidedAmount() { return providedAmount; }
    }
    public static class MandatoryFieldEmptyException extends Exception {
        private final String field;

        public MandatoryFieldEmptyException(String field) {
            super();
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }
    public static class MandatorySelectionEmptyException extends MandatoryFieldEmptyException {
        public MandatorySelectionEmptyException(String field) { super(field); }
    }
    public static class SoundAlreadyAddedException extends Exception {}
    public static class AddSoundFileException extends Exception {}
    public static class ValidationException extends Exception {}
    public static class ModelException extends ValidationException {
        private final String modelName;

        public ModelException(String modelName) {
            super();
            this.modelName = modelName;
        }

        public String getModelName() {
            return modelName;
        }
    }
    public static class ModelDoesNotExistException extends ModelException {
        public ModelDoesNotExistException(String modelName) { super(modelName); }
    }
    public static class ModelValidationException extends ModelException {
        private final long modelId;

        public ModelValidationException(String modelName, long modelId) {
            super(modelName);
            this.modelId = modelId;
        }

        public long getModelId() {
            return modelId;
        }
    }
    public static class NotEnoughFieldsException extends ModelValidationException {
        public NotEnoughFieldsException(String modelName, long modelId) { super(modelName, modelId); }
    }
    public static class ModelNotConfiguredException extends ModelValidationException {
        private final ArrayList<String> invalidModelFields;
        public ModelNotConfiguredException(String modelName, long modelId, ArrayList<String> invalidModelFields) {
            super(modelName, modelId);
            this.invalidModelFields = invalidModelFields;
        }

        public ArrayList<String> getInvalidModelFields() {
            return invalidModelFields;
        }
    }
    public static class DefaultModelOutdatedException extends ModelValidationException {
        private final String[] fields;
        private final String[] cards;
        private final String[] qfmt;
        private final String[] afmt;
        private final String css;

        public DefaultModelOutdatedException(String modelName, long modelId, String[] fields, String[] cards, String[] qfmt, String[] afmt, String css) {
            super(modelName, modelId);
            this.fields = fields;
            this.cards = cards;
            this.qfmt = qfmt;
            this.afmt = afmt;
            this.css = css;
        }

        public String[] getFields() { return fields; }
        public String[] getCards() { return cards; }
        public String[] getQfmt() { return qfmt; }
        public String[] getAfmt() { return afmt; }
        public String getCss() { return css; }
    }

    public static class TempoNotInRangeException extends ValidationException { }
    public static class InvalidFirstNoteDurationCoefficientException extends ValidationException { }

    private final AnkiDroidHelper helper;

    final RelatedIntervalSoundField[] relatedSoundFields;

    public final String modelName;
    public final Map<String, String> modelFields;
    final Set<String> multipleSelectionFields;
    final Map<String, String> defaultValues;
    final Map<String, SearchExpressionMaker> searchExpressionMakers;
    final Map<String, SearchExpressionMaker> relativesSearchExpressionMakers;
    final Map<String, EqualityChecker> equalityCheckers;
    final Map<String, EqualityChecker> relativesEqualityCheckers;
    final RelativesPriorityComparator[] relativesPriorityComparators;
    public final Long modelId;
    public final String deckName;
    private Long deckId;

    // Data of model's fields
    public final String[] sounds;
    public final String[] soundsSmaller;
    public final String[] soundsSmallerAlt;
    public final String[] soundsLarger;
    public final String[] soundsLargerAlt;
    public final String[] notes;
    public final String[] octaves;
    public final String direction;
    public final String timing;
    public final String[] intervals;
    public final String tempo;
    public final String instrument;
    public final String firstNoteDurationCoefficient;
    public final String version;

    private interface MemberAccessor {
        String getValue(MusInterval mi);
    }

    private final static Map<String, MemberAccessor> BATCH_MEMBER_ACCESSORS = new HashMap<String, MemberAccessor>() {{
        put(Builder.SOUNDS, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return extractBatchFieldMemberValue(mi.sounds);
            }
        });
        put(Builder.SOUNDS_SMALLER, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return extractBatchFieldMemberValue(mi.soundsSmaller);
            }
        });
        put(Builder.SOUNDS_SMALLER_ALT, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return extractBatchFieldMemberValue(mi.soundsSmallerAlt);
            }
        });
        put(Builder.SOUNDS_LARGER, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return extractBatchFieldMemberValue(mi.soundsLarger);
            }
        });
        put(Builder.SOUNDS_LARGER_ALT, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return extractBatchFieldMemberValue(mi.soundsLargerAlt);
            }
        });
        put(Builder.NOTES, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return extractBatchFieldMemberValue(mi.notes);
            }
        });
        put(Builder.OCTAVES, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return extractBatchFieldMemberValue(mi.octaves);
            }
        });
        put(Builder.INTERVALS, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return extractBatchFieldMemberValue(mi.intervals);
            }
        });
    }};

    private static String extractBatchFieldMemberValue(String[] arr) {
        return arr != null && arr.length > 0 ? arr[0] : null;
    }

    private final static Map<String, MemberAccessor> MEMBER_ACCESSORS = new HashMap<String, MemberAccessor>(BATCH_MEMBER_ACCESSORS) {{
        put(Builder.DIRECTION, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return mi.direction;
            }
        });
        put(Builder.TIMING, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return mi.timing;
            }
        });
        put(Builder.TEMPO, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return mi.tempo;
            }
        });
        put(Builder.INSTRUMENT, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return mi.instrument;
            }
        });
        put(Builder.FIRST_NOTE_DURATION_COEFFICIENT, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return mi.firstNoteDurationCoefficient;
            }
        });
        put(Builder.VERSION, new MemberAccessor() {
            @Override
            public String getValue(MusInterval mi) {
                return mi.version;
            }
        });
    }};

    private Map<String, ArrayList<String>> addedNotesBatchMembers;
    private ArrayList<String> originalSounds;


    /**
     * Construct an object using builder class.
     */
    public MusInterval(Builder builder) throws ValidationException {
        helper = builder.mHelper;

        deckName = builder.mDeckName;
        deckId = helper.findDeckIdByName(builder.mDeckName);
        modelName = builder.mModelName;
        modelId = helper.findModelIdByName(builder.mModelName);
        modelFields = builder.mModelFields;

        multipleSelectionFields = new HashSet<>(Fields.MULTIPLE_SELECTION_FIELDS.size());
        for (String fieldKey : Fields.MULTIPLE_SELECTION_FIELDS) {
            multipleSelectionFields.add(modelFields.getOrDefault(fieldKey, fieldKey));
        }
        defaultValues = new HashMap<>();
        searchExpressionMakers = new HashMap<>();
        relativesSearchExpressionMakers = new HashMap<>();
        equalityCheckers = new HashMap<>();
        relativesEqualityCheckers = new HashMap<>();
        for (String fieldKey : Fields.getSignature(true)) {
            String modelField = modelFields.getOrDefault(fieldKey, fieldKey);
            if (Fields.DEFAULT_VALUES.containsKey(fieldKey)) {
                defaultValues.put(modelField, Fields.DEFAULT_VALUES.get(fieldKey));
            }
            if (Fields.SEARCH_EXPRESSION_MAKERS.containsKey(fieldKey)) {
                SearchExpressionMaker expressionMaker = Fields.SEARCH_EXPRESSION_MAKERS.get(fieldKey);
                searchExpressionMakers.put(modelField, expressionMaker);
                relativesSearchExpressionMakers.put(modelField, expressionMaker);
            }
            if (Fields.RELATIVES_SEARCH_EXPRESSION_MAKERS.containsKey(fieldKey)) {
                relativesSearchExpressionMakers.replace(modelField, Fields.RELATIVES_SEARCH_EXPRESSION_MAKERS.get(fieldKey));
            }
            if (Fields.EQUALITY_CHECKERS.containsKey(fieldKey)) {
                EqualityChecker equalityChecker = Fields.EQUALITY_CHECKERS.get(fieldKey);
                passModelFields(equalityChecker, modelField);
                equalityCheckers.put(modelField, equalityChecker);
                relativesEqualityCheckers.put(modelField, equalityChecker);
            }
            if (Fields.RELATIVES_EQUALITY_CHECKERS.containsKey(fieldKey)) {
                EqualityChecker equalityChecker = Fields.RELATIVES_EQUALITY_CHECKERS.get(fieldKey);
                passModelFields(equalityChecker, modelField);
                relativesEqualityCheckers.replace(modelField, equalityChecker);
            }
        }
        relativesPriorityComparators = Fields.RELATIVES_PRIORITY_COMPARATORS;
        for (RelativesPriorityComparator comparator : relativesPriorityComparators) {
            comparator.setModelFields(modelFields);
        }

        RelatedIntervalSoundField soundSmallerField = new SmallerIntervalSoundField(helper, this);
        RelatedIntervalSoundField soundLargerField = new LargerIntervalSoundField(helper, this);
        soundSmallerField.setReverse(soundLargerField);
        soundLargerField.setReverse(soundSmallerField);
        relatedSoundFields = new RelatedIntervalSoundField[]{soundSmallerField, soundLargerField};

        sounds = builder.mSounds;
        soundsSmaller = builder.mSoundsSmaller;
        soundsSmallerAlt = builder.mSoundsSmallerAlt;
        soundsLarger = builder.mSoundsLarger;
        soundsLargerAlt = builder.mSoundsLargerAlt;
        notes = builder.mNotes;
        octaves = builder.mOctaves;
        direction = builder.mDirection.trim().toLowerCase();
        timing = builder.mTiming.trim().toLowerCase();
        intervals = builder.mIntervals;
        tempo = builder.mTempo.trim();
        instrument = builder.mInstrument.trim();
        firstNoteDurationCoefficient = builder.mFirstNoteDurationCoefficient.trim();
        version = builder.mVersion;

        validateFields(builder.mDefaultModel, builder.mFields, builder.mCards, builder.mQfmt, builder.mAfmt, builder.mCss);
    }

    private void passModelFields(EqualityChecker equalityChecker, String modelField) {
        if (equalityChecker instanceof FieldEqualityChecker) {
            ((FieldEqualityChecker) equalityChecker).setField(modelField);
        } else if (equalityChecker instanceof NoteEqualityChecker) {
            NoteEqualityChecker noteEqualityChecker = (NoteEqualityChecker) equalityChecker;
            String[] fields = noteEqualityChecker.getModelFields();
            String[] modelFields = new String[fields.length];
            for (int i = 0; i < modelFields.length; i++) {
                String field = fields[i];
                modelFields[i] = this.modelFields.getOrDefault(field, field);
            }
            noteEqualityChecker.setModelFields(modelFields);
        }
    }

    protected void validateFields(boolean isDefaultModel, String[] fields, String[] cards, String[] qfmt, String[] afmt, String css)
            throws ModelException, TempoNotInRangeException, InvalidFirstNoteDurationCoefficientException {
        String[] signature = Fields.getSignature(!version.isEmpty());

        if (modelId == null) {
            throw new ModelDoesNotExistException(modelName);
        }
        if (isDefaultModel && !helper.checkCustomModel(modelId, fields, cards, qfmt, afmt, css)) {
            throw new DefaultModelOutdatedException(Builder.DEFAULT_MODEL_NAME, modelId, fields, cards, qfmt, afmt, css);
        }
        final ArrayList<String> modelOwnFields = new ArrayList<>(Arrays.asList(helper.getFieldList(modelId)));
        if (modelOwnFields.size() < signature.length) {
            throw new NotEnoughFieldsException(modelName, modelId);
        }
        ArrayList<String> invalidModelFields = new ArrayList<>();
        for (String fieldKey : signature) {
            if (modelFields.containsKey(fieldKey)) {
                String field = modelFields.get(fieldKey);
                if (!modelOwnFields.contains(field)) {
                    invalidModelFields.add(fieldKey);
                }
            } else {
                invalidModelFields.add(fieldKey);
            }
        }
        if (!invalidModelFields.isEmpty()) {
            throw new ModelNotConfiguredException(modelName, modelId, invalidModelFields);
        }

        if (!tempo.isEmpty() && !Fields.Tempo.RANGE_VALIDATOR.isValid(tempo)) {
            throw new TempoNotInRangeException();
        }

        if (!Fields.FirstNoteDurationCoefficient.FORMAT_VALIDATOR.isValid(firstNoteDurationCoefficient)) {
            throw new InvalidFirstNoteDurationCoefficientException();
        }
    }

    /**
     * Check if such a data already exists in AnkiDroid.
     */
    public boolean existsInAnki() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        return getExistingNotesCount() > 0;
    }

    /**
     * Count, how many similar or equal notes exists in AnkiDroid.
     */
    public int getExistingNotesCount() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        return getExistingNotes().size();
    }

    /**
     * Count, how many similar or equal notes exists in AnkiDroid.
     */
    public int getExistingMarkedNotesCount() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        int result = 0;

        for (Map<String, String> note : getExistingNotes()) {
            String tags = note.get(AnkiDroidHelper.KEY_TAGS);
            if (tags != null && tags.toLowerCase().contains(" marked ")) {
                ++result;
            }
        }

        return result;
    }

    public Set<String> getUniqueValues(String fieldKey) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        LinkedList<Map<String, String>> notes = getExistingNotes();
        Set<String> result = new HashSet<>();
        String modelField = modelFields.getOrDefault(fieldKey, fieldKey);
        for (Map<String, String> data : notes) {
            if (data.containsKey(modelField)) {
                result.add(data.get(modelField));
            }
        }
        return result;
    }

    private LinkedList<Map<String, String>> getExistingNotes(ArrayList<Map<String, String>> dataSet) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        return getExistingNotes(dataSet, true);
    }

    /**
     * Get list of existing (similar or equal) notes. Each note consists of main model fields, id field and tags.
     */
    private LinkedList<Map<String, String>> getExistingNotes(ArrayList<Map<String, String>> dataSet, boolean trim) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        if (modelId != null) {
            for (Map<String, String> data : dataSet) {
                data.remove(modelFields.get(Fields.SOUND));
                data.remove(modelFields.get(Fields.SOUND_SMALLER));
                data.remove(modelFields.get(Fields.SOUND_SMALLER_ALT));
                data.remove(modelFields.get(Fields.SOUND_LARGER));
                data.remove(modelFields.get(Fields.SOUND_LARGER_ALT));
                data.remove(modelFields.get(Fields.VERSION));
            }
            return helper.findNotes(
                    modelId,
                    dataSet,
                    multipleSelectionFields,
                    defaultValues,
                    searchExpressionMakers,
                    equalityCheckers,
                    trim
            );
        } else {
            return new LinkedList<>();
        }
    }

    LinkedList<Map<String, String>> getExistingNotes() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        return getExistingNotes(true);
    }

    LinkedList<Map<String, String>> getExistingNotes(boolean trim) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        return getExistingNotes(getCollectedDataSet(), trim);
    }

    private LinkedList<Map<String, String>> getExistingNotes(final Map<String, String> data) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        ArrayList<Map<String, String>> dataSet = new ArrayList<Map<String, String>>() {{
            add(new HashMap<>(data));
        }};
        return getExistingNotes(dataSet);
    }

    /**
     * Add tag "marked" to the existing notes (similar or equal to this one).
     * Does not add the tag if it already exists in a note.
     */
    public int markExistingNotes() throws NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException {
        return tagExistingNotes("marked");
    }

    private int tagExistingNotes(String tag) throws NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final LinkedList<Map<String, String>> notes = getExistingNotes();
        int updated = 0;

        if (notes.size() == 0) {
            throw new NoteNotExistsException();
        }

        tag = tag.toLowerCase();

        for (Map<String, String> note : notes) {
            String tags = note.get(AnkiDroidHelper.KEY_TAGS);

            if (tags == null) {
                tags = " ";
            }

            tags = tags.toLowerCase();

            if (!tags.contains(String.format(" %s ", tag))) {
                tags = tags + String.format("%s ", tag);

                String id = note.get(AnkiDroidHelper.KEY_ID);

                if (id != null) {
                    updated += helper.addTagToNote(Long.parseLong(id), tags);
                }
            }
        }

        return updated;
    }

    public void addToAnki(AddingPrompter prompter, ProgressIndicator progressIndicator)
            throws CreateDeckException, UnexpectedSoundsAmountException, MandatoryFieldEmptyException,
            ValidationException, AnkiDroidHelper.InvalidAnkiDatabaseException {
        if (deckId == null) {
            deckId = helper.addNewDeck(deckName);
            if (deckId == null) {
                throw new CreateDeckException();
            }
            helper.storeDeckReference(deckName, deckId);
        }

        for (String memberKey : Builder.ADDING_MANDATORY_SELECTION_MEMBERS) {
            MemberAccessor accessor = Objects.requireNonNull(MEMBER_ACCESSORS.get(memberKey));
            String value = accessor.getValue(this);
            if (value == null) {
                throw new MandatorySelectionEmptyException(memberKey);
            }
        }

        final int permutationsNumber = getPermutationsNumber();
        final boolean soundsProvided = sounds != null;
        if (!soundsProvided || sounds.length != permutationsNumber) {
            final int providedAmount = soundsProvided ? sounds.length : 0;
            throw new UnexpectedSoundsAmountException(permutationsNumber, providedAmount);
        }

        for (String memberKey : Builder.ADDING_MANDATORY_SINGULAR_MEMBERS) {
            MemberAccessor accessor = Objects.requireNonNull(MEMBER_ACCESSORS.get(memberKey));
            if (accessor.getValue(this).isEmpty()) {
                throw new MandatoryFieldEmptyException(memberKey);
            }
        }

        ArrayList<Map<String, String>> miDataSet = getCollectedDataSet();

        addedNotesBatchMembers = new HashMap<>();
        for (Map.Entry<String, MemberAccessor> batchMemberAccessor : BATCH_MEMBER_ACCESSORS.entrySet()) {
            addedNotesBatchMembers.put(batchMemberAccessor.getKey(), new ArrayList<String>());
        }
        originalSounds = new ArrayList<>();

        addToAnki(0, miDataSet, prompter, progressIndicator);
    }

    private void addToAnki(int idx, final List<Map<String, String>> dataSet, final AddingPrompter prompter, final ProgressIndicator progressIndicator)
            throws ValidationException, AnkiDroidHelper.InvalidAnkiDatabaseException {

        final int dataCount = dataSet.size();
        if (idx >= dataCount) {
            prompter.addingFinished(getAddingResult());
            return;
        }

        for (int i = idx; i < dataCount; i++) {
            final Map<String, String> miData = dataSet.get(i);

            String sound = miData.get(modelFields.get(Fields.SOUND));
            if (sound == null) {
                prompter.processException(new IllegalStateException());
                continue;
            }
            if (sound.startsWith("[sound:")) {
                prompter.processException(new SoundAlreadyAddedException());
                continue;
            }

            final LinkedList<Map<String, String>> existingNotesData = getExistingNotes(miData);
            if (existingNotesData.size() > 0) {
                MusInterval[] existingMis = new MusInterval[existingNotesData.size()];
                for (int j = 0; j < existingNotesData.size(); j++) {
                    existingMis[j] = getMusIntervalFromData(existingNotesData.get(j));
                }

                final int nextIdx = i + 1;

                prompter.promptAddDuplicate(existingMis, new AddingHandler() {
                    @Override
                    public MusInterval add() {
                        try {
                            return handleAddToAnki(miData);
                        } catch (MusInterval.Exception | AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                            prompter.processException(e);
                            return null;
                        }
                    }

                    @Override
                    public MusInterval replace() throws AnkiDroidHelper.InvalidAnkiDatabaseException, ValidationException {
                        if (existingNotesData.size() != 1) {
                            throw new IllegalStateException("Replacing more than 1 existing note is not supported.");
                        }
                        Map<String, String> existingData = existingNotesData.getFirst();

                        String sound = miData.get(modelFields.get(Fields.SOUND));
                        AudioFile audioFile = new AudioFile(sound);
                        String newSound = helper.addFileToAnkiMedia(audioFile);
                        if (newSound == null || newSound.isEmpty()) {
                            prompter.processException(new AddSoundFileException());
                            return null;
                        }
                        newSound = String.format("[sound:%s]", newSound);
                        miData.put(modelFields.get(Fields.SOUND), newSound);

                        Map<String, String> newData = new HashMap<>(miData);
                        for (RelatedIntervalSoundField relatedSoundField : relatedSoundFields) {
                            relatedSoundField.autoFill(newData, true);
                        }

                        String noteId = Objects.requireNonNull(existingData.get(AnkiDroidHelper.KEY_ID));
                        helper.updateNote(modelId, Long.parseLong(noteId), newData);
                        MusInterval updatedMi = getMusIntervalFromData(newData);
                        updateAddedNotes(updatedMi, sound);
                        return updatedMi;
                    }

                    @Override
                    public int mark() throws AnkiDroidHelper.InvalidAnkiDatabaseException, ValidationException, NoteNotExistsException {
                        return getMusIntervalFromData(miData).markExistingNotes();
                    }

                    @Override
                    public int tag(String tag) throws AnkiDroidHelper.InvalidAnkiDatabaseException, ValidationException, NoteNotExistsException {
                        return getMusIntervalFromData(miData).tagExistingNotes(tag);
                    }

                    @Override
                    public void proceed() throws AnkiDroidHelper.InvalidAnkiDatabaseException, ValidationException {
                        progressIndicator.setMessage(R.string.batch_adding, nextIdx, dataCount);
                        addToAnki(nextIdx, dataSet, prompter, progressIndicator);
                    }
                });
                return;
            }

            try {
                handleAddToAnki(miData);
            } catch (MusInterval.Exception | AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                prompter.processException(e);
            }
            progressIndicator.setMessage(R.string.batch_adding, i + 1, dataCount);
        }

        prompter.addingFinished(getAddingResult());
    }

    private MusInterval handleAddToAnki(Map<String, String> data) throws AddSoundFileException,
            AddToAnkiException, AnkiDroidHelper.InvalidAnkiDatabaseException, ValidationException {
        String sound = data.get(modelFields.get(Fields.SOUND));
        AudioFile audioFile = new AudioFile(sound);
        String newSound = helper.addFileToAnkiMedia(audioFile);
        if (newSound == null || newSound.isEmpty()) {
            throw new AddSoundFileException();
        }
        newSound = String.format("[sound:%s]", newSound);
        data.put(modelFields.get(Fields.SOUND), newSound);

        for (RelatedIntervalSoundField relatedSoundField : relatedSoundFields) {
            relatedSoundField.autoFill(data, true);
        }

        Long noteId = helper.addNote(modelId, deckId, data, null);
        if (noteId == null) {
            throw new AddToAnkiException();
        }

        MusInterval newMi = getMusIntervalFromData(data);
        updateAddedNotes(newMi, sound);
        return newMi;
    }

    private void updateAddedNotes(MusInterval mi, String originalSound) {
        for (Map.Entry<String, MemberAccessor> batchMemberAccessor : BATCH_MEMBER_ACCESSORS.entrySet()) {
            String memberKey = batchMemberAccessor.getKey();
            ArrayList<String> currentValues = Objects.requireNonNull(addedNotesBatchMembers.get(memberKey));
            MemberAccessor memberAccessor = batchMemberAccessor.getValue();
            currentValues.add(memberAccessor.getValue(mi));
            addedNotesBatchMembers.put(memberKey, currentValues);
        }
        originalSounds.add(originalSound);
    }

    private MusInterval getMusIntervalFromData(Map<String, String> data) throws ValidationException {
        String startNote = data.get(modelFields.get(Fields.START_NOTE));
        String note = null;
        String octave = null;
        if (startNote != null && startNote.length() >= 2) {
            note = startNote.substring(0, startNote.length() - 1);
            octave = startNote.substring(startNote.length() - 1);
        }
        Builder builder = new Builder(helper)
                .deck(deckName)
                .model(modelName)
                .model_fields(modelFields)
                .sounds(new String[]{data.get(modelFields.get(Fields.SOUND))})
                .sounds_smaller(new String[]{data.get(modelFields.get(Fields.SOUND_SMALLER))})
                .sounds_smaller_alt(new String[]{data.get(modelFields.get(Fields.SOUND_SMALLER_ALT))})
                .sounds_larger(new String[]{data.get(modelFields.get(Fields.SOUND_LARGER))})
                .sounds_larger_alt(new String[]{data.get(modelFields.get(Fields.SOUND_LARGER_ALT))})
                .notes(note != null ? new String[]{note} : new String[]{})
                .octaves(octave != null ? new String[]{octave} : new String[]{})
                .direction(data.get(modelFields.get(Fields.DIRECTION)))
                .timing(data.get(modelFields.get(Fields.TIMING)))
                .intervals(new String[]{data.get(modelFields.get(Fields.INTERVAL))})
                .tempo(data.get(modelFields.get(Fields.TEMPO)))
                .instrument(data.get(modelFields.get(Fields.INSTRUMENT)))
                .first_note_duration_coefficient(data.get(modelFields.get(Fields.FIRST_NOTE_DURATION_COEFFICIENT)));
        if (!version.isEmpty()) {
            builder.version(version);
        }
        return builder.build();
    }

    private AddingResult getAddingResult() throws ValidationException {
        List<String> addedSounds = Objects.requireNonNull(addedNotesBatchMembers.get(Builder.SOUNDS));
        List<String> addedSoundsSmaller = Objects.requireNonNull(addedNotesBatchMembers.get(Builder.SOUNDS_SMALLER));
        List<String> addedSoundsSmallerAlt = Objects.requireNonNull(addedNotesBatchMembers.get(Builder.SOUNDS_SMALLER_ALT));
        List<String> addedSoundsLarger = Objects.requireNonNull(addedNotesBatchMembers.get(Builder.SOUNDS_LARGER));
        List<String> addedSoundsLargerAlt = Objects.requireNonNull(addedNotesBatchMembers.get(Builder.SOUNDS_LARGER_ALT));
        List<String> addedNotes = Objects.requireNonNull(addedNotesBatchMembers.get(Builder.NOTES));
        List<String> addedOctaves = Objects.requireNonNull(addedNotesBatchMembers.get(Builder.OCTAVES));
        List<String> addedIntervals = Objects.requireNonNull(addedNotesBatchMembers.get(Builder.INTERVALS));
        Builder builder = new Builder(helper)
                .deck(deckName)
                .model(modelName)
                .model_fields(modelFields)
                .sounds(addedSounds.toArray(new String[0]))
                .sounds_smaller(addedSoundsSmaller.toArray(new String[0]))
                .sounds_smaller_alt(addedSoundsSmallerAlt.toArray(new String[0]))
                .sounds_larger(addedSoundsLarger.toArray(new String[0]))
                .sounds_larger_alt(addedSoundsLargerAlt.toArray(new String[0]))
                .notes(addedNotes.toArray(new String[0]))
                .octaves(addedOctaves.toArray(new String[0]))
                .direction(direction)
                .timing(timing)
                .intervals(addedIntervals.toArray(new String[0]))
                .tempo(tempo)
                .instrument(instrument)
                .first_note_duration_coefficient(firstNoteDurationCoefficient);
        if (!version.isEmpty()) {
            builder.version(version);
        }
        return builder.build().new AddingResult(originalSounds.toArray(new String[0]));
    }

    public int getPermutationsNumber() {
        return (notes != null ? notes.length : 0)
                * (octaves != null ? octaves.length : 0)
                * (intervals != null ? intervals.length : 0);
    }

    public ArrayList<Map<String, String>> getCollectedDataSet() {
        final String[] octaves = this.octaves != null ? this.octaves : Builder.EMPTY_SELECTION;
        final String[] notes = this.notes != null ? this.notes : Builder.EMPTY_SELECTION;
        final String[] intervals = this.intervals != null ? this.intervals : Builder.EMPTY_SELECTION;
        ArrayList<Map<String, String>> miDataSet = new ArrayList<>(octaves.length * notes.length * intervals.length);
        int i = 0;
        final boolean soundsProvided = sounds != null;
        final boolean soundsSmallerProvided = soundsSmaller != null;
        final boolean soundsSmallerAltProvided = soundsSmallerAlt != null;
        final boolean soundsLargerProvided = soundsLarger != null;
        final boolean soundsLargerAltProvided = soundsLargerAlt != null;
        for (String octave : octaves) {
            for (String note : notes) {
                for (String interval : intervals) {
                    Map<String, String> miData = new HashMap<>();
                    String sound = soundsProvided && sounds.length > i ? sounds[i] : "";
                    miData.put(modelFields.get(Fields.SOUND), sound);
                    String soundSmaller = soundsSmallerProvided && soundsSmaller.length > i ? soundsSmaller[i] : "";
                    miData.put(modelFields.get(Fields.SOUND_SMALLER), soundSmaller);
                    String soundSmallerAlt = soundsSmallerAltProvided && soundsSmallerAlt.length > i ? soundsSmallerAlt[i] : "";
                    miData.put(modelFields.get(Fields.SOUND_SMALLER_ALT), soundSmallerAlt);
                    String soundLarger = soundsLargerProvided && soundsLarger.length > i ? soundsLarger[i] : "";
                    miData.put(modelFields.get(Fields.SOUND_LARGER), soundLarger);
                    String soundLargerAlt = soundsLargerAltProvided && soundsLargerAlt.length > i ? soundsLargerAlt[i] : "";
                    miData.put(modelFields.get(Fields.SOUND_LARGER_ALT), soundLargerAlt);
                    miData.put(modelFields.get(Fields.START_NOTE), note + octave);
                    miData.put(modelFields.get(Fields.DIRECTION), direction);
                    miData.put(modelFields.get(Fields.TIMING), timing);
                    miData.put(modelFields.get(Fields.INTERVAL), interval);
                    miData.put(modelFields.get(Fields.TEMPO), tempo);
                    miData.put(modelFields.get(Fields.INSTRUMENT), instrument);
                    miData.put(modelFields.get(Fields.FIRST_NOTE_DURATION_COEFFICIENT), firstNoteDurationCoefficient);
                    miDataSet.add(miData);
                    i++;
                }
            }
        }
        if (!version.isEmpty()) {
            for (Map<String, String> miData : miDataSet) {
                miData.put(modelFields.get(Fields.VERSION), version);
            }
        }
        return miDataSet;
    }

    public class AddingResult {
        private final String[] originalSounds;

        public AddingResult(String[] originalSounds) {
            this.originalSounds = originalSounds;
        }

        public String[] getOriginalSounds() {
            return originalSounds;
        }

        public MusInterval getMusInterval() {
            return MusInterval.this;
        }
    }
}

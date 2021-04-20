package com.ichi2.apisample;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String KEY_DECK_PREFERENCE = "preference_deck";
    public static final String KEY_MODEL_PREFERENCE = "preference_model";
    public static final String KEY_VERSION_FIELD_SWITCH = "preference_version_field_switch";
    public static final String KEY_TAG_DUPLICATES_SWITCH = "preference_tag_duplicates_switch";
    public static final String KEY_DUPLICATE_TAG_PREFERENCE = "preference_duplicate_tag";
    private static final String KEY_FIELDS_PREFERENCE_CATEGORY = "preference_fields";

    private static final String TEMPLATE_KEY_FIELD_PREFERENCE = "preference_%s_field";
    private static final String TEMPLATE_KEY_MODEL_FIELD_PREFERENCE = "%s_%s_model";

    public static final boolean DEFAULT_VERSION_FIELD_SWITCH = true;
    public static final boolean DEFAULT_TAG_DUPLICATES_SWITCH = true;
    public static final String DEFAULT_DUPLICATE_TAG = "MI2A_duplicate";

    private Context context;
    private PreferenceScreen preferenceScreen;

    private AnkiDroidHelper helper;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        context = getPreferenceManager().getContext();
        preferenceScreen = getPreferenceManager().createPreferenceScreen(context);

        helper = new AnkiDroidHelper(context);

        ListPreference deckListPreference = new ListPreference(context);
        deckListPreference.setKey(KEY_DECK_PREFERENCE);
        deckListPreference.setTitle(R.string.deck_preference_title);
        deckListPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        Map<Long, String> deckList = helper.getDeckList();
        List<String> deckEntriesList = new ArrayList<>(deckList.values());
        if (!deckEntriesList.contains(MusInterval.Builder.DEFAULT_DECK_NAME)) {
            deckEntriesList.add(MusInterval.Builder.DEFAULT_DECK_NAME);
        }
        deckListPreference.setDefaultValue(MusInterval.Builder.DEFAULT_DECK_NAME);
        String[] deckEntries = deckEntriesList.toArray(new String[0]);
        deckListPreference.setEntries(deckEntries);
        deckListPreference.setEntryValues(deckEntries);
        preferenceScreen.addPreference(deckListPreference);

        ListPreference modelListPreference = new ListPreference(context);
        modelListPreference.setKey(KEY_MODEL_PREFERENCE);
        modelListPreference.setTitle(R.string.model_preference_title);
        modelListPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        Map<Long, String> modelList = helper.getModelList();
        List<String> modelEntriesList = new ArrayList<>(modelList.values());
        if (!modelEntriesList.contains(MusInterval.Builder.DEFAULT_MODEL_NAME)) {
            modelEntriesList.add(MusInterval.Builder.DEFAULT_MODEL_NAME);
        }
        modelListPreference.setDefaultValue(MusInterval.Builder.DEFAULT_MODEL_NAME);
        String[] modelEntries = modelEntriesList.toArray(new String[0]);
        modelListPreference.setEntries(modelEntries);
        modelListPreference.setEntryValues(modelEntries);
        modelListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                String currModel = preferences.getString(KEY_MODEL_PREFERENCE, "");
                final boolean versionField = preferences.getBoolean(KEY_VERSION_FIELD_SWITCH, DEFAULT_VERSION_FIELD_SWITCH);
                final String[] signature = MusInterval.Fields.getSignature(versionField);
                Long modelId = helper.findModelIdByName(currModel);
                if (modelId != null) {
                    SharedPreferences.Editor preferencesEditor = preferences.edit();
                    for (String fieldKey : signature) {
                        String fieldPreferenceKey = getFieldPreferenceKey(fieldKey);
                        String fieldPreference = preferences.getString(fieldPreferenceKey, "");
                        String modelFieldPreferenceKey = getModelFieldPreferenceKey(modelId, fieldPreferenceKey);
                        preferencesEditor.putString(modelFieldPreferenceKey, fieldPreference);
                    }
                    preferencesEditor.apply();
                }
                updateFieldsPreferenceEntries((String) newValue, signature, true);
                return true;
            }
        });
        preferenceScreen.addPreference(modelListPreference);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean versionField = preferences.getBoolean(KEY_VERSION_FIELD_SWITCH, DEFAULT_VERSION_FIELD_SWITCH);
        final String[] signature = MusInterval.Fields.getSignature(true);
        Resources res = getResources();
        String[] fieldTitles = new String[]{
                res.getString(R.string.sound_field_list_preference_title),
                res.getString(R.string.sound_smaller_field_list_preference_title),
                res.getString(R.string.sound_larger_field_list_preference_title),
                res.getString(R.string.start_note_field_list_preference_title),
                res.getString(R.string.direction_field_list_preference_title),
                res.getString(R.string.timing_field_list_preference_title),
                res.getString(R.string.interval_field_list_preference_title),
                res.getString(R.string.tempo_field_list_preference_title),
                res.getString(R.string.instrument_field_list_preference_title),
                res.getString(R.string.version_field_list_preference_title),
        };
        PreferenceCategory fieldsPreferenceCategory = new PreferenceCategory(context);
        fieldsPreferenceCategory.setKey(KEY_FIELDS_PREFERENCE_CATEGORY);
        fieldsPreferenceCategory.setTitle(R.string.fields_preference_category_title);
        fieldsPreferenceCategory.setInitialExpandedChildrenCount(0);
        preferenceScreen.addPreference(fieldsPreferenceCategory);
        for (int i = 0; i < signature.length; i++) {
            ListPreference fieldListPreference = new DropDownPreference(context);
            fieldListPreference.setKey(getFieldPreferenceKey(signature[i]));
            fieldListPreference.setTitle(fieldTitles[i]);
            fieldListPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
            fieldsPreferenceCategory.addPreference(fieldListPreference);
        }
        if (!versionField) {
            String versionFieldPreferenceKey = getFieldPreferenceKey(MusInterval.Fields.VERSION);
            ListPreference versionFieldListPreference = preferenceScreen.findPreference(versionFieldPreferenceKey);
            if (versionFieldListPreference != null) {
                versionFieldListPreference.setVisible(false);
            }
        }
        updateFieldsPreferenceEntries(modelListPreference.getValue(), signature, false);

        SwitchPreference versionFieldSwitchPreference = new SwitchPreference(context);
        versionFieldSwitchPreference.setKey(KEY_VERSION_FIELD_SWITCH);
        versionFieldSwitchPreference.setTitle(R.string.version_field_switch_preference_title);
        versionFieldSwitchPreference.setSummary(R.string.version_field_switch_preference_summary);
        versionFieldSwitchPreference.setDefaultValue(DEFAULT_VERSION_FIELD_SWITCH);
        versionFieldSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String versionFieldPreferenceKey = getFieldPreferenceKey(MusInterval.Fields.VERSION);
                ListPreference versionFieldListPreference = preferenceScreen.findPreference(versionFieldPreferenceKey);
                if (versionFieldListPreference != null) {
                    final boolean versionField = (boolean) newValue;
                    versionFieldListPreference.setVisible(versionField);
                    if (versionField) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        String model = preferences.getString(KEY_MODEL_PREFERENCE, "");
                        final String[] signature = MusInterval.Fields.getSignature(true);
                        updateFieldsPreferenceEntries(model, signature, false);
                    }
                }
                return true;
            }
        });
        preferenceScreen.addPreference(versionFieldSwitchPreference);

        SwitchPreference tagDuplicatesSwitchPreference = new SwitchPreference(context);
        tagDuplicatesSwitchPreference.setKey(KEY_TAG_DUPLICATES_SWITCH);
        tagDuplicatesSwitchPreference.setTitle(R.string.tag_duplicates_preference_title);
        tagDuplicatesSwitchPreference.setSummary(R.string.tag_duplicates_preference_summary);
        tagDuplicatesSwitchPreference.setDefaultValue(DEFAULT_TAG_DUPLICATES_SWITCH);
        tagDuplicatesSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                EditTextPreference duplicateTagPreference = preferenceScreen.findPreference(KEY_DUPLICATE_TAG_PREFERENCE);
                if (duplicateTagPreference != null) {
                    duplicateTagPreference.setVisible((boolean) newValue);
                }
                return true;
            }
        });
        preferenceScreen.addPreference(tagDuplicatesSwitchPreference);

        EditTextPreference duplicateTagPreference = new EditTextPreference(context);
        duplicateTagPreference.setKey(KEY_DUPLICATE_TAG_PREFERENCE);
        duplicateTagPreference.setDefaultValue(DEFAULT_DUPLICATE_TAG);
        duplicateTagPreference.setTitle(R.string.duplicate_tag_edit_text_preference_title);
        duplicateTagPreference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        duplicateTagPreference.setDialogTitle(R.string.duplicate_tag_edit_text_preference_dialog_title);
        duplicateTagPreference.setOnPreferenceChangeListener(new TagPreferenceChangeListener(context, helper, KEY_DUPLICATE_TAG_PREFERENCE, DEFAULT_DUPLICATE_TAG));
        preferenceScreen.addPreference(duplicateTagPreference);

        setPreferenceScreen(preferenceScreen);
    }

    public static String getFieldPreferenceKey(String fieldKey) {
        return String.format(TEMPLATE_KEY_FIELD_PREFERENCE, fieldKey);
    }

    public static String getModelFieldPreferenceKey(long modelId, String fieldPreferenceKey) {
        return String.format(TEMPLATE_KEY_MODEL_FIELD_PREFERENCE, fieldPreferenceKey, modelId);
    }

    private void updateFieldsPreferenceEntries(String modelName, String[] signature, boolean modelChanged) {
        Long modelId = helper.findModelIdByName(modelName);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String[] fieldList = modelId != null ? helper.getFieldList(modelId) : new String[]{};
        String[] entries = new String[fieldList.length + 1];
        entries[0] = "";
        System.arraycopy(fieldList, 0, entries, 1, fieldList.length);
        for (String fieldKey : signature) {
            String fieldPreferenceKey = getFieldPreferenceKey(fieldKey);
            String value = "";
            if (modelChanged) {
                if (modelId != null) {
                    String modelFieldPreferenceKey = getModelFieldPreferenceKey(modelId, fieldPreferenceKey);
                    value = preferences.getString(modelFieldPreferenceKey, "");
                }
            } else {
                value = preferences.getString(fieldPreferenceKey, "");
            }
            ListPreference fieldListPreference = preferenceScreen.findPreference(fieldPreferenceKey);
            if (fieldListPreference != null) {
                fieldListPreference.setEntries(entries);
                fieldListPreference.setEntryValues(entries);
                fieldListPreference.setValue(value);
            }
        }
    }

    private static class TagPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        private final Context context;
        private final AnkiDroidHelper helper;
        private final String key;
        private final String defaultValue;

        public TagPreferenceChangeListener(Context context, AnkiDroidHelper helper, String key, String defaultValue) {
            super();
            this.context = context;
            this.helper = helper;
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                final long modelId = helper.findModelIdByName(MusInterval.Builder.DEFAULT_MODEL_NAME);
                LinkedList<Map<String, String>> notesData = helper.findNotes(modelId, new HashMap<String, String>());
                final String currValue = sharedPreferences.getString(key, defaultValue);
                for (Map<String, String> noteData : notesData) {
                    String tags = noteData.get("tags");
                    String currValueStr = String.format(" %s ", currValue);
                    if (tags.contains(currValueStr)) {
                        long id = Long.parseLong(noteData.get("id"));
                        helper.updateNoteTags(id, tags.replace(currValueStr, String.format(" %s ", (String) newValue)));
                    }
                }
            } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                return false;
            }
            return true;
        }
    }
}

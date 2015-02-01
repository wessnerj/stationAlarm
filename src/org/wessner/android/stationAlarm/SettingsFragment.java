/**
 * stationAlarm - Android app which wakes you before you reach your target station.
 * Copyright (C) 2015  Joseph Wessner <joseph@wessner.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.wessner.android.stationAlarm;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.widget.Toast;

/**
 * SettingsFragment: Managing the settings view.
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener {
	/**
	 * Key for the language setting.
	 */
	public static final String KEY_PREF_LANG = "pref_lang";
	/**
	 * Key for the vibration setting.
	 */
	public static final String KEY_PREF_VIBRATE = "pref_vibrate";
	/**
	 * Key for the sound setting.
	 */
	public static final String KEY_PREF_SOUND = "pref_sound";
	/**
	 * Key for the sound/ringtone setting.
	 */
	public static final String KEY_PREF_SOUND_RINGTONE = "pref_sound_ringtone";
	
	/**
	 * SharedPreferences for accessing the preferences
	 */
	private SharedPreferences sharedPref;
	/**
	 * RingtonePreference for accessing ringtone preference
	 */
	private RingtonePreference ringtonePref;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Register for changes
		this.sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.sharedPref.registerOnSharedPreferenceChangeListener(this);
        
        // Register separately for RingtonePreference due to an Android bug
        // (https://code.google.com/p/android/issues/detail?id=21766)
        this.ringtonePref = (RingtonePreference) findPreference(KEY_PREF_SOUND_RINGTONE);
        this.ringtonePref.setOnPreferenceChangeListener(this);
        
        // Set summaries
        this.setSummaries(this.sharedPref);
	}
	
	@Override
	public void onPause() {
	    super.onPause();

	    // Unregister as listener
	    this.sharedPref.unregisterOnSharedPreferenceChangeListener(this);
	    this.sharedPref = null;
	    
	    this.ringtonePref.setOnPreferenceChangeListener(null);
	    this.ringtonePref = null;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		String value = sharedPreferences.getString(key, "");
		
		if (key.equals(KEY_PREF_LANG)) {
			Toast.makeText(getActivity(), getString(R.string.settings_lang_changed), Toast.LENGTH_LONG).show();
			this.setLanguageSummary(findPreference(KEY_PREF_LANG), key, value);
		}
		else if (key.equals(KEY_PREF_VIBRATE))
			this.setVibrateSummary(findPreference(KEY_PREF_VIBRATE), key, value);
		else if (key.equals(KEY_PREF_SOUND))
			this.setSoundSummary(findPreference(KEY_PREF_SOUND), key, value);
		else if (key.equals(KEY_PREF_SOUND_RINGTONE))
			this.setSoundRingtoneSummary(findPreference(KEY_PREF_SOUND_RINGTONE), key, value);
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		this.setSoundRingtoneSummary(preference, KEY_PREF_SOUND_RINGTONE, (String) newValue);
	    return true;
	}
	
	private void setLanguageSummary(Preference pref, String key, String value) {
		if (value.equals("default"))
			pref.setSummary(getString(R.string.pref_lang_sum_default));
		else if (value.equals("en"))
			pref.setSummary(getString(R.string.pref_lang_sum_en));
		else if (value.equals("de"))
			pref.setSummary(getString(R.string.pref_lang_sum_de));
	}
	
	/**
	 * Sets the summary for vibrate setting.
	 * 
	 * @param pref
	 * @param key
	 * @param value
	 */
	private void setVibrateSummary(Preference pref,	String key, String value) {
		if (value.equals("never"))
			pref.setSummary(getString(R.string.pref_vibrate_sum_never));
		else if (value.equals("vibrate"))
			pref.setSummary(getString(R.string.pref_vibrate_sum_vibrate));
		else if (value.equals("normal"))
			pref.setSummary(getString(R.string.pref_vibrate_sum_normal));
		else if (value.equals("always"))
			pref.setSummary(getString(R.string.pref_vibrate_sum_always));
	}

	/**
	 * Sets the summary for sound setting.
	 * 
	 * @param pref
	 * @param key
	 * @param value
	 */
	private void setSoundSummary(Preference pref,	String key, String value) {
		if (value.equals("never"))
			pref.setSummary(getString(R.string.pref_sound_sum_never));
		else if (value.equals("normal"))
			pref.setSummary(getString(R.string.pref_sound_sum_normal));
		else if (value.equals("always"))
			pref.setSummary(getString(R.string.pref_sound_sum_always));
	}
	
	/**
	 * Sets the summary for ringtone setting.
	 * 
	 * @param pref
	 * @param key
	 * @param value
	 */
	private void setSoundRingtoneSummary(Preference pref, String key, String value) {
		final String ringToneTitle = this.getRingtoneName(value);
	    pref.setSummary(ringToneTitle);
	}
	
	/**
	 * Initializes all settings summaries.
	 * 
	 * @param sharedPreferences
	 */
	private void setSummaries(SharedPreferences sharedPreferences) {
		this.setLanguageSummary     (findPreference(KEY_PREF_LANG),           KEY_PREF_LANG,           sharedPreferences.getString(KEY_PREF_LANG,           getString(R.string.pref_lang_val_default)));
		this.setVibrateSummary      (findPreference(KEY_PREF_VIBRATE),        KEY_PREF_VIBRATE,        sharedPreferences.getString(KEY_PREF_VIBRATE,        getString(R.string.pref_vibrate_val_default)));
		this.setSoundSummary        (findPreference(KEY_PREF_SOUND),          KEY_PREF_SOUND,          sharedPreferences.getString(KEY_PREF_SOUND,          getString(R.string.pref_sound_val_default)));
		this.setSoundRingtoneSummary(findPreference(KEY_PREF_SOUND_RINGTONE), KEY_PREF_SOUND_RINGTONE, sharedPreferences.getString(KEY_PREF_SOUND_RINGTONE, getString(R.string.pref_sound_ringtone_val_default)));
	}
	
	/**
	 * Get the ringtone name for given uri.
	 * 
	 * @param uri
	 * @return
	 */
	private String getRingtoneName(String uri) {
		Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(uri));
		return ringtone.getTitle(getActivity());
	}
}

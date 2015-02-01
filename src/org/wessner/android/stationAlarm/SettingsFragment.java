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

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener {
	public static final String KEY_PREF_VIBRATE = "pref_vibrate";
	public static final String KEY_PREF_SOUND = "pref_sound";
	public static final String KEY_PREF_SOUND_RINGTONE = "pref_sound_ringtone";
	
	private SharedPreferences sharedPref;
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

	    this.sharedPref.unregisterOnSharedPreferenceChangeListener(this);
	    this.sharedPref = null;
	    
	    this.ringtonePref.setOnPreferenceChangeListener(null);
	    this.ringtonePref = null;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		String value = sharedPreferences.getString(key, "");
		
		if (key.equals(KEY_PREF_VIBRATE))
			this.setVibrateSummary(findPreference(KEY_PREF_VIBRATE), key, value);
		else if (key.equals(KEY_PREF_SOUND))
			this.setSoundSummary(findPreference(KEY_PREF_SOUND), key, value);
		else if (key.equals(KEY_PREF_SOUND_RINGTONE))
			this.setSoundSummary(findPreference(KEY_PREF_SOUND_RINGTONE), key, value);
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		this.setSoundRingtoneSummary(preference, KEY_PREF_SOUND_RINGTONE, (String) newValue);
	    return true;
	}
	
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

	private void setSoundSummary(Preference pref,	String key, String value) {
		if (value.equals("never"))
			pref.setSummary(getString(R.string.pref_sound_sum_never));
		else if (value.equals("normal"))
			pref.setSummary(getString(R.string.pref_sound_sum_normal));
		else if (value.equals("always"))
			pref.setSummary(getString(R.string.pref_sound_sum_always));
	}
	
	private void setSoundRingtoneSummary(Preference pref, String key, String value) {
		final String ringToneTitle = this.getRingtoneName(value);
	    pref.setSummary(ringToneTitle);
	}
	
	private void setSummaries(SharedPreferences sharedPreferences) {	
		this.setVibrateSummary      (findPreference(KEY_PREF_VIBRATE),        KEY_PREF_VIBRATE,        sharedPreferences.getString(KEY_PREF_VIBRATE,        getString(R.string.pref_vibrate_val_default)));
		this.setSoundSummary        (findPreference(KEY_PREF_SOUND),          KEY_PREF_SOUND,          sharedPreferences.getString(KEY_PREF_SOUND,          getString(R.string.pref_sound_val_default)));
		this.setSoundRingtoneSummary(findPreference(KEY_PREF_SOUND_RINGTONE), KEY_PREF_SOUND_RINGTONE, sharedPreferences.getString(KEY_PREF_SOUND_RINGTONE, getString(R.string.pref_sound_ringtone_val_default)));
	}
	
	private String getRingtoneName(String uri) {
		Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(uri));
		return ringtone.getTitle(getActivity());
	}
}

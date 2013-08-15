/*    Copyright 2013 Tom Brennan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package t0mm13b.dmesglog.prefs;

import com.robobunny.SeekBarPreference;

import t0mm13b.dmesglog.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceActivity;

public class DMesgPrefs extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	private static final String TAG = "DMesgPrefs";
	private CheckBoxPreference mChkBoxPref_ScreenOn;
	private SeekBarPreference mSeekBPref_Refresh;
	private SharedPreferences mPrefs = null;
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main_prefs);
        this.mChkBoxPref_ScreenOn = (CheckBoxPreference)findPreference(getString(R.string.dmesg_keep_screenon_key));
        //
        if (this.mChkBoxPref_ScreenOn != null){
        	this.mChkBoxPref_ScreenOn.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					final boolean blnScreenOn = mChkBoxPref_ScreenOn.isChecked();
					return mPrefs.edit().putBoolean(getString(R.string.dmesg_keep_screenon_key), blnScreenOn).commit();
				}
        		
        	});
        }
        this.mSeekBPref_Refresh = (SeekBarPreference)findPreference(getString(R.string.dmesg_refresh_period_key));
        if (this.mSeekBPref_Refresh != null){
        	this.mSeekBPref_Refresh.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					final int nRefresh = mSeekBPref_Refresh.getValue();
					return mPrefs.edit().putInt(getString(R.string.dmesg_refresh_period_key), nRefresh).commit();
				}
        		
        	});
        }
	}
	
	@Override
	public void onStart(){
		super.onStart();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	}
	@SuppressWarnings("deprecation")
	@Override
	public void onPause(){
		super.onPause();
		this.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	@SuppressWarnings("deprecation")
	@Override
	public void onResume(){
		super.onResume();
		this.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Do nowt!
	}
}

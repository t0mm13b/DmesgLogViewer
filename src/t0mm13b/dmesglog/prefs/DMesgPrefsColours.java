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

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import t0mm13b.dmesglog.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class DMesgPrefsColours extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	private static final String TAG = "DMesgPrefs";
	private ColorPickerPreference mPrefsKLog0;
	private ColorPickerPreference mPrefsKLog1;
	private ColorPickerPreference mPrefsKLog2;
	private ColorPickerPreference mPrefsKLog3;
	private ColorPickerPreference mPrefsKLog4;
	private ColorPickerPreference mPrefsKLog5;
	private ColorPickerPreference mPrefsKLog6;
	private ColorPickerPreference mPrefsKLog7;
	private SharedPreferences mPrefs = null;
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main_prefs_loglevels);
        // TODO: Dropdown box.....
        this.mPrefsKLog0 = (ColorPickerPreference)findPreference(getString(R.string.klog_level_zero_key));
        if (this.mPrefsKLog0 != null){
        	this.mPrefsKLog0.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Editor e = mPrefs.edit();
					e.putInt(getString(R.string.klog_level_zero_key), Integer.valueOf(String.valueOf(newValue)));
					e.commit();
					return true;
				}
        		
        	});
        }
        //
        this.mPrefsKLog1 = (ColorPickerPreference)findPreference(getString(R.string.klog_level_one_key));
        if (this.mPrefsKLog1 != null){
        	this.mPrefsKLog1.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Editor e = mPrefs.edit();
					e.putInt(getString(R.string.klog_level_one_key), Integer.valueOf(String.valueOf(newValue)));
					e.commit();
					return true;
				}
        		
        	});
        }
        //
        this.mPrefsKLog2 = (ColorPickerPreference)findPreference(getString(R.string.klog_level_two_key));
        if (this.mPrefsKLog2 != null){
        	this.mPrefsKLog2.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Editor e = mPrefs.edit();
					e.putInt(getString(R.string.klog_level_two_key), Integer.valueOf(String.valueOf(newValue)));
					e.commit();
					return true;
				}
        		
        	});
        }
        //
        this.mPrefsKLog3 = (ColorPickerPreference)findPreference(getString(R.string.klog_level_three_key));
        if (this.mPrefsKLog3 != null){
        	this.mPrefsKLog3.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Editor e = mPrefs.edit();
					e.putInt(getString(R.string.klog_level_three_key), Integer.valueOf(String.valueOf(newValue)));
					e.commit();
					return true;
				}
        		
        	});
        }
        //
        this.mPrefsKLog4 = (ColorPickerPreference)findPreference(getString(R.string.klog_level_four_key));
        if (this.mPrefsKLog4 != null){
        	this.mPrefsKLog4.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Editor e = mPrefs.edit();
					e.putInt(getString(R.string.klog_level_four_key), Integer.valueOf(String.valueOf(newValue)));
					e.commit();
					return true;
				}
        		
        	});
        }
        //
        this.mPrefsKLog5 = (ColorPickerPreference)findPreference(getString(R.string.klog_level_five_key));
        if (this.mPrefsKLog5 != null){
        	this.mPrefsKLog5.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Editor e = mPrefs.edit();
					e.putInt(getString(R.string.klog_level_five_key), Integer.valueOf(String.valueOf(newValue)));
					e.commit();
					return true;
				}
        		
        	});
        }
        //
        this.mPrefsKLog6 = (ColorPickerPreference)findPreference(getString(R.string.klog_level_six_key));
        if (this.mPrefsKLog6 != null){
        	this.mPrefsKLog6.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Editor e = mPrefs.edit();
					e.putInt(getString(R.string.klog_level_six_key), Integer.valueOf(String.valueOf(newValue)));
					e.commit();
					return true;
				}
        		
        	});
        }
        //
        this.mPrefsKLog7 = (ColorPickerPreference)findPreference(getString(R.string.klog_level_seven_key));
        if (this.mPrefsKLog7 != null){
        	this.mPrefsKLog7.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Editor e = mPrefs.edit();
					e.putInt(getString(R.string.klog_level_seven_key), Integer.valueOf(String.valueOf(newValue)));
					e.commit();
					return true;
				}
        		
        	});
        }
        //
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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) { }
}

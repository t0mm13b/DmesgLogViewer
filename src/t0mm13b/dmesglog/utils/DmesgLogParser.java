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
package t0mm13b.dmesglog.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;

import t0mm13b.dmesglog.DMesgViewerApplication;
import t0mm13b.dmesglog.R;
import t0mm13b.dmesglog.interfaces.DmesgParser;

/*
 * Class to parse and analyze the lines from dmesg
 */
public class DmesgLogParser implements OnSharedPreferenceChangeListener{
	private static final String TAG = "DmesgLogParser";
	private static final boolean D = true;
	private DmesgLogLevelColour mListLogLevels[];
	// ^\<(\d+)\>\[(\d+)\-(\d+)\s+(\d+\:\d+\:\d+\.\d+)\]\s+(.*)$
	private final Pattern mRegexpDmesgLog = Pattern.compile("^\\<(\\d+)\\>\\[((\\d+)\\-(\\d+)\\s+(\\d+\\:\\d+\\:\\d+\\.\\d+))\\]\\s+(.*)$", Pattern.CASE_INSENSITIVE);
	private final Pattern mRegexpDmesgLog_Alt = Pattern.compile("^\\<(\\d+)\\>\\[\\s?((\\d+)\\.(\\d+))\\]\\s+(.*)$", Pattern.CASE_INSENSITIVE);
	private final Pattern mRegexpDmesgDt = Pattern.compile("^(\\d+)\\-(\\d+)\\s+(\\d+\\:\\d+\\:\\d+\\.\\d+)$", Pattern.CASE_INSENSITIVE);
	private final Pattern mRegexpUptime = Pattern.compile("^((\\d+)\\.(\\d+))\\s+((\\d+)\\.(\\d+))$", Pattern.CASE_INSENSITIVE);
	private Object mObjLock = new Object();
	private DmesgParser mDmesgScanner = null;
	private Calendar mCal = Calendar.getInstance();
	private SimpleDateFormat mSdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSSSSS");
	private SharedPreferences mPrefs = null;

	/**
	 * Base class constructor - initialises colours on startup by way of default preferences and sets
	 *  the time-zone on the formatter. Not called directly, its called by other constructors.
	 * @see InitLogLevelColours
	 */
	public DmesgLogParser(){
		String strTZone = mCal.getTimeZone().getDisplayName(Locale.getDefault());
		if (D) Log.d(TAG, String.format("DmesgLogParser: strTZone %s", strTZone));
		this.mSdf.setTimeZone(TimeZone.getTimeZone(strTZone));
		this.InitLogLevelColours();
	}

	/**
	 * Constructor with parameter, calls the base constructor, process the dmesg log
	 * @param sDmesgLog - String holding the dmesg output
	 * @see DmesgProcess
	 */
	public DmesgLogParser(final String sDmesgLog){
		this();
		if (sDmesgLog != null && sDmesgLog.length() > 0){
			DmesgProcess(sDmesgLog);		
		}
	}
	//
	/**
	 * Constructor, calls the base constructor, sets the callback and process dmesg log
	 * @param dmesgScannerCallback - the callback interface {@link DmesgParser interface}
	 * @param sDmesgLog - String holding the dmesg output
	 * @see DmesgParser
	 * @see DmesgProcess
	 */
	public DmesgLogParser(DmesgParser dmesgScannerCallback, final String sDmesgLog){
		this();
		this.mDmesgScanner = dmesgScannerCallback;
		if (sDmesgLog != null && sDmesgLog.length() > 0){
			DmesgProcess(sDmesgLog);		
		}
	}
	/**
	 * Initialises the colour preferences for each log level on each dmesg line
	 */
	private void InitLogLevelColours(){
		Context ctxt = DMesgViewerApplication.getAppContext();
		this.mPrefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
		int logLevelsEnumLen = DmesgLogLevelsEnum.values().length - 1;
		this.mListLogLevels = new DmesgLogLevelColour[logLevelsEnumLen];
		int nResIds[] = { 
				R.string.klog_level_zero_key,
				R.string.klog_level_one_key,
				R.string.klog_level_two_key,
				R.string.klog_level_three_key,
				R.string.klog_level_four_key,
				R.string.klog_level_five_key,
				R.string.klog_level_six_key,
				R.string.klog_level_seven_key
		};
		for (int i = 0; i < logLevelsEnumLen; i++){
			int someColour = this.mPrefs.getInt(ctxt.getResources().getString(nResIds[i]), Color.BLACK);
			if (D) Log.d(TAG, String.format("InitLogLevelColours: someColour = %d", someColour));
			String someColourHex = ColorPickerPreference.convertToRGB(someColour);
			this.mListLogLevels[i] = new DmesgLogLevelColour(DmesgLogLevelsEnum.valueOf(i), someColour, someColourHex);
			if (D) Log.d(TAG, String.format("InitLogLevelColours: mlIstLogLevels[%d] = %s", i, this.mListLogLevels[i].toString()));
		};
		//Log.d(TAG, "InitLogLevelColours: " + this.mlIstLogLevels);
	}
	/**
	 * Adds log output from dmesg to be processed
	 * @param sDmesgLog - string with carriage return/newline delimiter
	 * @see DmesgProcess
	 */
	public synchronized void AddDmesgExtra(String sDmesgLog){
		this.DmesgProcess(sDmesgLog);
	}
	
	/**
	 * Gets the epoch time from dmesg date stamp
	 * @param sDmesgDt
	 * @return long
	 */
	private synchronized long getDmesgDateTime(String sDmesgDt){
		long lDt = -1;
		try{
			Matcher m = this.mRegexpDmesgDt.matcher(sDmesgDt);
			if (m.matches() && m.groupCount() > 0){
				int month = Integer.parseInt(m.group(1));
				int day = Integer.parseInt(m.group(2));
				//
				String sTime = m.group(3);
				String sFormattdYrMoDy = String.format("%02d-%02d-%d %s", month, day, mCal.get(Calendar.YEAR), sTime);
				//Log.d(TAG, String.format("getDmesgDateTime: %s", sFormattdYrMoDy));
				Date mdTParsed = null;
				boolean blnParsdOk = false;
				try{
					mdTParsed = this.mSdf.parse(String.format("%s %s", sFormattdYrMoDy, sTime));
					blnParsdOk = true;
				}catch(ParseException parseEx){
					
				}
				if (blnParsdOk) lDt = mdTParsed.getTime();
			}
		}catch(PatternSyntaxException pEx){
			
		}
		return lDt;
	}

	/**
	 * Returns a string value taken from /proc/uptime
	 * @return String value in format 181253.56 44063.04
	 * @see getDmesgUptimeDevice
	 */
	private synchronized String getUptime(){
		String strScanndUptime = null;
		Scanner scan = null;
		try {
			scan = new Scanner(new File("/proc/uptime"));
			StringBuilder sbScannedUptime = new StringBuilder();
			while (scan.hasNextLine()){
				sbScannedUptime.append(scan.nextLine());
			}
			strScanndUptime = sbScannedUptime.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally{
			if (scan != null){
				scan.close();
			}
		}
		return strScanndUptime;
	}
	/**
	 * Parses and analyzes the string value to obtain the time since device was up and running, 
	 * taken from http://linuxaria.com/article/how-to-make-dmesg-timestamp-human-readable?lang=en
	 * @return Long value indicating time in milliseconds
	 * @see getUptime
	 */
	private synchronized long getDmesgUptimeDevice(){
		String strScanndUptime = getUptime();
		long lNow = System.currentTimeMillis();
		Matcher m = mRegexpUptime.matcher(strScanndUptime);
		if (m.matches() && m.groupCount() > 0){
			long lMilliseconds = Long.parseLong(m.group(3));
			long lSeconds = Long.parseLong(m.group(2));
			long lCombined = lSeconds + (lMilliseconds / 1000);
			long lDiff = lNow - lCombined;
			return lDiff;
		}
		return -1;
	}
	/**
	 * Runs each line delimited by newline and parse it to extract timestamp, some might have it in one form,
	 *  other might have it in more verbose, in which the regex will extract from. This will fire the callback if
	 *  set appropriately.
	 *  @param sDmesgLog
	 *  @see DmesgParser
	 */
	private synchronized void DmesgProcess(final String sDmesgLog){
		synchronized(mObjLock){
			long lDmesgCount = 0;
			String[] dmesgLines = sDmesgLog.split("\\r?\\n");
			if (dmesgLines != null && dmesgLines.length > 0){
				int nMaxLines = dmesgLines.length;
				if (mDmesgScanner != null) mDmesgScanner.cbDmesgParsing();
				for (int i = 0; i < nMaxLines; i++){
					try{
						Matcher m = mRegexpDmesgLog.matcher(dmesgLines[i]);
						if (m.matches() && m.groupCount() > 0){
							long lEpochTime = getDmesgDateTime(m.group(2));
							int dmesgLogLevel = Integer.valueOf(m.group(1));
							DmesgLine lineDmesg = new DmesgLine(m.group(1),m.group(2), dmesgLines[i]);
							lineDmesg.setEpochTime(lEpochTime);
							lineDmesg.setLogLevelColour(mListLogLevels[dmesgLogLevel]);
							if (mDmesgScanner != null) mDmesgScanner.cbDmesgParsedEntry(lineDmesg);
						}else{
							// try the alternative matcher!
							m = mRegexpDmesgLog_Alt.matcher(dmesgLines[i]);
							if (m.matches() && m.groupCount() > 0){
								int dmesgLogLevel = Integer.valueOf(m.group(1));
								long lUptime = getDmesgUptimeDevice();
								long lMilliseconds = Long.parseLong(m.group(4));
								long lSeconds = Long.parseLong(m.group(3));
								long lCombined = lSeconds + (lMilliseconds / 1000);
								long lTotal = lUptime + lCombined;
								mCal.setTimeInMillis(lTotal);
								String sReformattd = mSdf.format(mCal.getTime());
								DmesgLine lineDmesg = new DmesgLine(m.group(1), sReformattd, dmesgLines[i]);
								lineDmesg.setEpochTime(lTotal);
								lineDmesg.setLogLevelColour(mListLogLevels[dmesgLogLevel]);
								if (mDmesgScanner != null) mDmesgScanner.cbDmesgParsedEntry(lineDmesg);
							}
						}
					}catch(PatternSyntaxException pEx){
						Log.d(TAG, "[PatternSyntaxException] " + pEx.toString());
					}finally{
						
					}
				}
				if (mDmesgScanner != null) mDmesgScanner.cbDmesgParseComplete(lDmesgCount);
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) { }
}

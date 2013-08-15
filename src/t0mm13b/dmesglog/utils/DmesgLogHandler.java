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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import t0mm13b.dmesglog.DMesgViewerApplication;
import t0mm13b.dmesglog.R;
import t0mm13b.dmesglog.interfaces.DmesgParser;
import t0mm13b.dmesglog.jni.DmesgWrapper;

public class DmesgLogHandler implements DmesgParser{
	private static String TAG = "DmesgLogHandler";
	private static long DMESG_DELAY = 30;
	private ScheduledExecutorService mSchedExecSvc;
	private long mLngLastDmesgRan = -1;
	private DmesgLogParser mParserDmesg = null;
	private Handler mDmesgViewerHandler;
	private boolean mBlnFirstRunThrough = true;
	private boolean mBlnIsRunning = false;
	private boolean mBlnIsPlaying = false;
	private SharedPreferences mPrefs = null;
	private int mIntRefreshDelay = -1;
	
	private Runnable _runDmesg = new Runnable() {

        @Override
        public void run() {
            long now = System.currentTimeMillis();
            if (now < mLngLastDmesgRan + DMESG_DELAY) {
                return;
            }
            mLngLastDmesgRan = now;
            if (!mBlnIsPlaying) return;
            Log.d(TAG, "*** _runDmesg:run *** ");
            mParserDmesg.AddDmesgExtra(DmesgWrapper.dmesgWrapperFunc(false));
        }
    };
    
    public DmesgLogHandler(Handler handler){
    	
    	this.mDmesgViewerHandler = handler;
    	Context ctxt = DMesgViewerApplication.getAppContext();
		this.mPrefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
		this.mIntRefreshDelay = this.mPrefs.getInt(ctxt.getResources().getString(R.string.dmesg_refresh_period_key), (int) DMESG_DELAY);
		Log.d(TAG, "Refresh period is " + this.mIntRefreshDelay + " seconds. \\o/");
    	this.mParserDmesg = new DmesgLogParser(this, DmesgWrapper.dmesgWrapperFunc(false));
    }
    public synchronized boolean getIsRunning(){
    	return this.mBlnIsRunning;
    }
    public synchronized boolean getIsPaused(){
    	return (this.mBlnIsPlaying == false);
    }
	public synchronized void Run(){
		if (!this.mBlnIsRunning){
			Log.d(TAG, "*** Run ***");
			this.mSchedExecSvc = Executors.newScheduledThreadPool(1);
			this.mSchedExecSvc.scheduleAtFixedRate(_runDmesg, mIntRefreshDelay, mIntRefreshDelay, TimeUnit.SECONDS);
			this.mBlnIsRunning = true;
			this.mBlnIsPlaying = true;
		}
	}
	public synchronized void Pause(){
		this.mBlnIsPlaying = false;
	}
	public synchronized void Resume(){
		this.mBlnIsPlaying = true;
	}
	public synchronized void Stop(){
		if (this.mBlnIsRunning){
			Log.d(TAG, "*** Stop ***");
			if (this.mSchedExecSvc != null && !this.mSchedExecSvc.isShutdown()){
				this.mSchedExecSvc.shutdown();
				this.mSchedExecSvc = null;
			}
			this.mBlnIsRunning = false;
		}
	}
	@Override
	public void cbDmesgParsing() {
		// TODO Auto-generated method stub
		if (!this.mBlnFirstRunThrough){
			this.mDmesgViewerHandler.obtainMessage(DmesgLogHelpers.MSG_POPUP_PROGRESS).sendToTarget();
		}
	}
	@Override
	public synchronized void cbDmesgParsedEntry(final DmesgLine dmesgLineEntry) {
		this.mDmesgViewerHandler.obtainMessage(DmesgLogHelpers.MSG_LISTVIEW_ADAPTER_ADD_ENTRY, dmesgLineEntry).sendToTarget();		
	}
	@Override
	public synchronized void cbDmesgParseComplete(final long lCountAdded) {
		this.mDmesgViewerHandler.obtainMessage(DmesgLogHelpers.MSG_LISTVIEW_ADAPTER_REFRESH).sendToTarget();
		if (this.mBlnFirstRunThrough){
			this.mDmesgViewerHandler.obtainMessage(DmesgLogHelpers.MSG_DMESGLOGHANDLER_RUN).sendToTarget();
			this.mBlnFirstRunThrough = false;
		}else{
			this.mDmesgViewerHandler.obtainMessage(DmesgLogHelpers.MSG_HIDE_PROGRESS).sendToTarget();
		}
	}
}

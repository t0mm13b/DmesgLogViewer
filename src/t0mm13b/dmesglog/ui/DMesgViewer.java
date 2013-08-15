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
package t0mm13b.dmesglog.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import t0mm13b.dmesglog.R;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import t0mm13b.dmesglog.utils.DmesgLogHandler;
import t0mm13b.dmesglog.utils.DmesgLogHelpers;
import t0mm13b.dmesglog.utils.DmesgLine;
import t0mm13b.dmesglog.utils.DmesgListViewAdapter;

public class DMesgViewer extends ListActivity implements OnSharedPreferenceChangeListener{
	private final static String TAG = "DMesgViewer";
	private final static boolean D = false;
	//
	private DmesgListViewAdapter mAdapter;
	//
	private DmesgLogHandler mDmesgLogHandler;
	//
	private SharedPreferences mPrefs = null;
	private AsyncTask<Void, String, Void> mAsyncTaskLoader = null;
	private Activity mDmesgVwrAct;
	private Object mObjLockSync = new Object();
	private MenuItem mMIPause = null;
	//
	// Be nice and not clutter end user's sdcard!
	private static final String EXTERNAL_DROP_FOLDER_FMT = "Android/data/%s/";
	private String mStrDropFolder;
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		this.mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.mPrefs.registerOnSharedPreferenceChangeListener(this);
		this.mAdapter = new DmesgListViewAdapter(this, R.layout.listview_dmesg_line_row, null);
		this.getListView().setAdapter(mAdapter);
		this.getListView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		this.getListView().setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (mMIPause != null && totalItemCount > 0){
					if ((firstVisibleItem + visibleItemCount) < totalItemCount){
						// we're scrolling upwards so trigger the pause event
						handlePause(mMIPause);
					}
					if ((firstVisibleItem + visibleItemCount) == totalItemCount){
						// we've reached the bottom, so trigger the resume event
						handleResume(mMIPause);
					}
				}
			}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) { }
			
		});
		this.getListView().setDivider(null);
		this.mDmesgVwrAct = this;
		this.mStrDropFolder = String.format(EXTERNAL_DROP_FOLDER_FMT, this.getPackageName());
		new asyncLoadInitialDmesg(this).execute();
		
		Log.d(TAG, "onCreate - CHECK!");
	}
	@Override
	public void onAttachedToWindow(){
		super.onAttachedToWindow();
		this.openOptionsMenu(); // Lame sucky way of obtaining menu handle to Pause hence will open on app start! Sh!te
	}
	@Override
    public void onDestroy(){
		Log.d(TAG, "*** onDestroy ***");
		this.mPrefs.unregisterOnSharedPreferenceChangeListener(this);
		if (this.mDmesgLogHandler.getIsRunning()){
			this.mDmesgLogHandler.Stop(); // Die thread, mofo!
		}
		super.onDestroy();
	}
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu - CHECK!");
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (this.mMIPause == null){
			this.mMIPause = menu.findItem(R.id.menu_pause);
	        if (this.mMIPause != null){
	        	Log.d(TAG, "FOUND THE PAUSE MENU! \\o/");
	        	menu.close();
	        	return true;
	        }else{
	        	Log.d(TAG, "NOT FOUND THE PAUSE MENU! /o\\");
	        }
	        
		}
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_clear :
        	this.mAdapter.clear();
        	mDmesgViewerHandler.obtainMessage(DmesgLogHelpers.MSG_LISTVIEW_ADAPTER_REFRESH).sendToTarget();
        	fixUpTitle(this.mDmesgLogHandler.getIsPaused());
        	return true;
        case R.id.menu_pause :
        	handlePauseResume(item);
        	return true;
        case R.id.menu_preferences:
        	Intent intentLaunchPrefs = new Intent(this, t0mm13b.dmesglog.prefs.DMesgPrefs.class);
            intentLaunchPrefs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentLaunchPrefs);
        	return true;
        case R.id.menu_share :
        	handlePause(item);
        	new asyncSaveDmesg(this, this.mAdapter.getEntries(), item, true).execute();
        	item.setEnabled(false); // Prevent multiple spawning of async tasks!
        	break;
        case R.id.menu_save :
        	handlePause(item);
        	new asyncSaveDmesg(this, this.mAdapter.getEntries(), item, false).execute();
        	item.setEnabled(false);
        	break;
        }
        return super.onOptionsItemSelected(item);
    }
	private void handlePause(MenuItem item){
		if (this.mDmesgLogHandler.getIsPaused()) return;
		this.mDmesgLogHandler.Pause();
		item.setIcon(R.drawable.ic_media_play);
		item.setTitle(getString(R.string.menu_resume));
		fixUpTitle(true);
	}
	private void handleResume(MenuItem item){
		if (!this.mDmesgLogHandler.getIsPaused()) return;
		this.mDmesgLogHandler.Resume();
		item.setIcon(R.drawable.ic_media_pause);
		item.setTitle(getString(R.string.menu_pause));
		fixUpTitle(false);
	}
	private void handlePauseResume(MenuItem item){
		if (this.mDmesgLogHandler.getIsRunning()){
    		if (!this.mDmesgLogHandler.getIsPaused()){
    			handlePause(item);
    		}else{
    			handleResume(item);
    		}
    	}
	}
	private void fixUpTitle(boolean bPaused){
		final int iAdapterCount = this.mAdapter.getCount();
		String sTitleFmt = "";
		if (bPaused) sTitleFmt = String.format(getResources().getString(R.string.app_title_win_param_paused), iAdapterCount);
		else sTitleFmt = String.format(getResources().getString(R.string.app_title_win_param), iAdapterCount);
		this.mDmesgViewerHandler.obtainMessage(DmesgLogHelpers.MSG_UPDATE_TITLE, sTitleFmt).sendToTarget();
	}
	
	
	private Handler mDmesgViewerHandler = new Handler(Looper.getMainLooper()){
    	
		@SuppressWarnings("unchecked")
		@Override
        public void handleMessage(Message msg) {
    		try{
				switch(msg.what){
	
				case DmesgLogHelpers.MSG_UPDATE_TITLE :
					getWindow().setTitle((String)msg.obj);
					break;
				case DmesgLogHelpers.MSG_CLEAR_LISTVIEW :
					mAdapter.clear();
					break;
				case DmesgLogHelpers.MSG_LISTVIEW_ADAPTER :
					Log.d(TAG, "DmesgLogHelpers.MSG_LISTVIEW_ADAPTER");
					mAdapter.setListDmesgEntries((List<DmesgLine>)msg.obj);
					fixUpTitle(false);
					break;
				case DmesgLogHelpers.MSG_LISTVIEW_ADAPTER_ADD_ENTRY :
					//Log.d(TAG, "DmesgLogHelpers.MSG_LISTVIEW_ADAPTER_ADD_ENTRY");
					mAdapter.add((DmesgLine) msg.obj);
					break;
				case DmesgLogHelpers.MSG_LISTVIEW_SCROLL_BOTTOM :
					getListView().setSelection(getListView().getAdapter().getCount() - 1);
					break;
				case DmesgLogHelpers.MSG_DMESGLOGHANDLER_RUN :
					Log.d(TAG, "DmesgLogHelpers.MSG_DMESGLOGHANDLER_RUN");
					mDmesgLogHandler.Run();
					break;
				case DmesgLogHelpers.MSG_LISTVIEW_ADAPTER_REFRESH : 
					mAdapter.Refresh();
					fixUpTitle(false);
					mDmesgViewerHandler.obtainMessage(DmesgLogHelpers.MSG_LISTVIEW_SCROLL_BOTTOM).sendToTarget();
					break;
				case DmesgLogHelpers.MSG_POPUP_PROGRESS :
					synchronized(mObjLockSync){
						if (mAsyncTaskLoader == null){
							mAsyncTaskLoader = new asyncLoadingDmesg(mDmesgVwrAct).execute();
						}
					}
					break;
				case DmesgLogHelpers.MSG_HIDE_PROGRESS :
					synchronized(mObjLockSync){
						mObjLockSync.notify();
						if (mAsyncTaskLoader != null){
							mAsyncTaskLoader = null;
						}
					}
					break;
				
				}
	    	}catch(Exception eX){
	    		Log.d(TAG, "mDmesgViewerHandler: Exception = " + eX);
	    	}
    	}
	};
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(TAG, "onSharedPreferenceChanged: key = " + key);
		if (key == this.getString(R.string.dmesg_keep_screenon_key)){
			boolean blnScreenOn = this.mPrefs.getBoolean(this.getString(R.string.dmesg_keep_screenon_key), false);
			Log.d(TAG, "onSharedPreferenceChanged: blnScreenOn = " + blnScreenOn);
			if (blnScreenOn) this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			else this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
	}
	
	/**
	 * A simple AsyncTask that gets called only once to load the dmesg log.
	 * 
	 * @author t0mm13b
	 *
	 */
	final class asyncLoadInitialDmesg extends AsyncTask<Void,String,Void>{
		private static final String TAG = "asyncLoadInitialDmesg";
		private Activity mActivity;
        private ProgressDialog mProgressDlg;
        
        public asyncLoadInitialDmesg(Activity clientActivity){
			this.mActivity = clientActivity;
			this.mProgressDlg = new ProgressDialog(this.mActivity);
        }
		
		protected void onPreExecute(){
			this.mProgressDlg.setTitle(getString(R.string.dmesg_load_pleasewait_title));
			this.mProgressDlg.setMessage(getString(R.string.dmesg_load_pleasewait_message));
			this.mProgressDlg.show();
		}
		
		@Override
		protected void onProgressUpdate(String... sMessage){ }
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try{
				mDmesgLogHandler = new DmesgLogHandler(mDmesgViewerHandler);
			}catch(Exception eX){
				Log.d(TAG, "doInBackground: Exception caught?! " + eX);
			}
			return null;
		}

		protected void onPostExecute(Void unused){
			if (this.mProgressDlg.isShowing()){
				this.mProgressDlg.dismiss();
			}
		}
	}
	
	/**
	 * Simple AsyncTask that gets called during refresh period of loading dmesg log.
	 * 
	 * @author t0mm13b
	 *
	 */
	final class asyncLoadingDmesg extends AsyncTask<Void,String,Void>{
		private static final String TAG = "asyncLoadingDmesg";
		private Activity mActivity;
        private ProgressDialog mProgressDlg;
        
        public asyncLoadingDmesg(Activity clientActivity){
			this.mActivity = clientActivity;
			this.mProgressDlg = new ProgressDialog(this.mActivity);
        }

        protected void onPreExecute(){
			this.mProgressDlg.setTitle(getString(R.string.dmesg_load_pleasewait_title));
			this.mProgressDlg.setMessage(getString(R.string.dmesg_load_pleasewait_message));
			this.mProgressDlg.show();
		}
		@Override
		protected void onProgressUpdate(String... sMessage){ }
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try{
				synchronized(mObjLockSync){
					mObjLockSync.wait();
				}
			}catch(InterruptedException intEx){
				
			}
			return null;
		}
		protected void onPostExecute(Void unused){
			if (this.mProgressDlg.isShowing()){
				this.mProgressDlg.dismiss();
			}
		}
	}
	/**
	 * Simple Async Task to either prepare dmesg log for sharing or saving to SDCard
	 * 
	 * @author t0mm13b
	 *
	 */
	final class asyncSaveDmesg extends AsyncTask<Void,String,Void>{
		private static final String TAG = "asyncSaveDmesg";
		private Activity mActivity;
        private ProgressDialog mProgressDlg;
        private List<DmesgLine> mListDmesgs;
        private MenuItem mItem;
        private boolean mBlnShare = false;
        private File mFExternalSDCard = null;
        private String mStrFullPath;
        private File mFSDCardPath = null;
        private SimpleDateFormat mSdfDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        
        public asyncSaveDmesg(Activity clientActivity, List<DmesgLine> list, MenuItem item, boolean blnShare){
			this.mActivity = clientActivity;
			this.mProgressDlg = new ProgressDialog(this.mActivity);
			this.mListDmesgs = list;
			this.mItem = item;
			this.mBlnShare = blnShare; // Are we sharing or saving?
			String sdState = android.os.Environment.getExternalStorageState();
            if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            	this.mFExternalSDCard = android.os.Environment.getExternalStorageDirectory();
            	this.mStrFullPath = String.format("%s/%s", mFExternalSDCard.getPath(), mStrDropFolder.substring(0, mStrDropFolder.length() - 1)); // chop off trailing slash
            	this.mFSDCardPath = new File(this.mStrFullPath);
            	if (!this.mFSDCardPath.exists()){
            		if (!this.mFSDCardPath.mkdirs()){
            			// Red Alert....
            			Log.w(TAG, String.format("Unable to create %s", this.mStrFullPath));
            		}
            	}
            	
            }
        }
        
		protected void onPreExecute(){
			this.mProgressDlg.setTitle(getString(R.string.dmesg_load_pleasewait_title));
			if (this.mBlnShare)	this.mProgressDlg.setMessage(getString(R.string.dmesg_pleasewait_message_share));
			else this.mProgressDlg.setMessage(getString(R.string.dmesg_pleasewait_message_save));
			this.mProgressDlg.show();
		}
		
		@Override
		protected void onProgressUpdate(String... sMessage){ }
		
		private Intent createShareIntent(String sData){
	    	final Intent intent = new Intent(Intent.ACTION_SEND);
	    	intent.setType("text/plain");
	    	intent.putExtra(Intent.EXTRA_TEXT, sData);
	    	return Intent.createChooser(intent, getString(R.string.app_name));
		}
		private String getSharedIntentData(){
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%s - Dmesg Log Capture\n", getString(R.string.app_name)));
			for (DmesgLine dmesgEntry : this.mListDmesgs){
				sb.append(dmesgEntry.toString() + "\n");
			}
			sb.trimToSize();
			return sb.toString();
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			if (this.mBlnShare){
				startActivity(createShareIntent(getSharedIntentData()));
			}else{
                String sNeatDateFormat = this.mSdfDateFormat.format(System.currentTimeMillis());
                String strDmesgFile = String.format("%s/dmesg-%s.txt", this.mFSDCardPath.getPath(), sNeatDateFormat);
                FileWriter fw = null;
                try {
                	fw = new FileWriter(strDmesgFile);
                	if (fw != null){
                		fw.write(getSharedIntentData());
                	}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					if (fw != null){
						try {
							fw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return null;
		}
		protected void onPostExecute(Void unused){
			if (this.mProgressDlg.isShowing()){
				this.mProgressDlg.dismiss();
			}
			handleResume(this.mItem); // Resume the list refresh
			this.mItem.setEnabled(true); // Done! re-Enable either Share or Save... :) \o/
		}
		
	}
}

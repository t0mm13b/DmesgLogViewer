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
package t0mm13b.dmesglog.jni;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import android.os.Build;
import android.util.Log;


/*
 * 1. Go to jni directory of src/../jni and invoke: javac -d ../../../../bin DmesgWrapper.java
 * 2. Go to root of project and invoke: javah -jni -classpath bin/ -d jni t0mm13b.dmesglog.jni.DmesgWrapper
 * 3. Header generated in jni directory, so fix up the C source to use that header
 * 4. Go to jni directory of src/../jni and invoke: ~/Android/android-ndk-r5b/ndk-build
 * 5. The library is then built and found in libs/armeabi
 */

/**
 * A class that handles the mechanism in obtaining the dmesg output by way of wrapper.
 * 
 * For versions earlier than Ice Cream Sandwich, use the JNI native method.
 * For Jellybean and later, use root!
 * 
 * @author t0mm13b
 *
 */
public class DmesgWrapper{
	private static final String TAG = "DmesgWrapper";
	//
	private static StringBuilder mSBDmesgOutput = new StringBuilder();
	//
	public static native String dmesg(boolean blnClear);
	
	static{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
			System.loadLibrary("dmesgLib");
		}
	}
	
	private static String handleDmesgCmd(){
		if (mSBDmesgOutput != null && mSBDmesgOutput.length() > 0){
			int len = mSBDmesgOutput.length();
			mSBDmesgOutput.delete(0, len);
			mSBDmesgOutput.trimToSize();
		}
		String workingToolbox = RootTools.getWorkingToolbox();
		//
		String dmesgCmd2Exec = String.format("%s dmesg", workingToolbox);
		//
		final Object objSpinWait = new Object();
		try {
			synchronized(objSpinWait){
				RootTools.runShellCommand(RootTools.getShell(true, 1000), new Command(0, dmesgCmd2Exec){
	
					@Override
					public void commandCompleted(int argId, int argExitCode) {
						mSBDmesgOutput.trimToSize();
						synchronized(objSpinWait){
							objSpinWait.notify();
						}
					}
	
					@Override
					public void commandOutput(int argId, String argCmdOutput) {
						mSBDmesgOutput.append(argCmdOutput + "\n");
					}
	
					@Override
					public void commandTerminated(int argId, String argReason) {
						synchronized(objSpinWait){
							objSpinWait.notify();
						}
					}
					
				});
				objSpinWait.wait();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (RootDeniedException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return mSBDmesgOutput.toString();
	}
	public static String dmesgWrapperFunc(boolean blnClear){
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
			return dmesg(blnClear);
		}else{
			// Here we need to handle situation running on JB or later as dmesg_restrict is in place! :(
			//RootTools.debugMode = true;
			RootTools.handlerEnabled = false;
			if (RootTools.isRootAvailable() && RootTools.isAccessGiven()){
				String sDmesg = handleDmesgCmd();
				Log.d(TAG, "dmesgWrapperFunc: sDmesg = " + sDmesg);
				return sDmesg;
			}
		}
		return "";
	}
	
}

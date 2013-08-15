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


/**
 * Place holder for holding dmesg line entry.
 * 
 * @author t0mm13b
 * 
 * @see DmesgListViewAdapter
 * @see DmesgLogParser
 * @see DmesgLogHandler
 */
public class DmesgLine{
	private String mStrDmesgDate;
	private String mStrDmesg;
	private String mStrDmesgLine;
	private DmesgLogLevelsEnum mEnumDmesgLogLevel;
	private DmesgLogLevelColour mDmesgLogLevelColour;
	private long mLngEpochTime = -1L;
	public DmesgLine(String strDmesgLevel, String strDmesgDate, String strDmesg){
		this.mStrDmesgDate = strDmesgDate;
		this.mStrDmesg = strDmesg;
		int DmesgLevel = Integer.valueOf(strDmesgLevel);
		this.mEnumDmesgLogLevel = DmesgLogLevelsEnum.valueOf(DmesgLevel);
		this.mStrDmesgLine = String.format("%s", this.mStrDmesg); 
	}
	public DmesgLine(DmesgLine lineDmesgEntry){
		this.mStrDmesgDate = lineDmesgEntry.mStrDmesgDate;
		this.mStrDmesg = lineDmesgEntry.mStrDmesg;
		this.mEnumDmesgLogLevel = lineDmesgEntry.mEnumDmesgLogLevel;
		this.mStrDmesgLine = String.format("%s", this.mStrDmesg); 
	}
	public DmesgLogLevelsEnum getDMesgLevel(){
		return this.mEnumDmesgLogLevel;
	}
	public String getDmesgDate(){
		return this.mStrDmesgDate;
	}
	public void setEpochTime(long lEpochTime){
		this.mLngEpochTime = lEpochTime;
	}
	public DmesgLogLevelColour getLogLevelColour(){
		return this.mDmesgLogLevelColour;
	}
	public void setLogLevelColour(DmesgLogLevelColour dmesgLogLevelColour){
		this.mDmesgLogLevelColour = dmesgLogLevelColour;
	}
	public long getEpochTime(){
		return this.mLngEpochTime;
	}
	public int hashCode(){
		return this.mStrDmesgLine.hashCode();
	}
	public boolean equals(Object obj){
		if (obj == null) return false;
		if (!(obj instanceof DmesgLine)) return false;
		if (obj.getClass() != this.getClass()) return false;
		DmesgLine rhs = (DmesgLine)obj;
		if (rhs != null){
			if (this.hashCode() == rhs.hashCode()) return true;
		}
		return false;
	}
	public String toString(){
		return this.mStrDmesgLine;
	}
}
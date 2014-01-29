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

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration flags to indicate dmesg's log level entry indicated by regexp ^<\d+>....
 * @author t0mm13b
 *
 */
public enum DmesgLogLevelsEnum{
	Unknown(-1),
	Emergency(0),
	Alert(1),
	Critical(2),
	Error(3),
	Warning(4),
	Notice(5),
	Info(6),
	Debug(7);
	final int mValue;
	private DmesgLogLevelsEnum(int value) {this.mValue = value;}
	private static final Map<Integer, DmesgLogLevelsEnum> mapEnum = new HashMap<Integer, DmesgLogLevelsEnum>();
	static{
		for (DmesgLogLevelsEnum dllEnum : values()){
			mapEnum.put(Integer.valueOf(dllEnum.mValue), dllEnum);
		}
	}
	public int getValue() {return mValue;}
	public static DmesgLogLevelsEnum valueOf(int value) {
		if (mapEnum.containsKey(Integer.valueOf(value))) 
			return mapEnum.get(Integer.valueOf(value));
		return Unknown;
	}
}
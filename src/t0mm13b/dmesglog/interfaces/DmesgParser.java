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
package t0mm13b.dmesglog.interfaces;

import t0mm13b.dmesglog.utils.DmesgLine;

/**
 * Simple interface for callback mechanism.
 * @author t0mm13b
 *
 */
public interface DmesgParser {
	// Are we there?
	public void cbDmesgParsing();
	// Are we there yet?
	public void cbDmesgParsedEntry(final DmesgLine dmesgLineEntry);
	// Woohoo! \o/
	public void cbDmesgParseComplete(final long lCountAdded);
}

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

/**
 * Simple interface callbacks for use with @see CustomDialogBox
 * @author t0mm13b
 *
 */
public interface ICustomDlgBoxListeners{
	public void cbCustomDlgBoxYesFired();
	public void cbCustomDlgBoxNoFired();
	public void cbCustomDlgOkFired();
	public void cbCustomDlgOkFired(int idOk);
}
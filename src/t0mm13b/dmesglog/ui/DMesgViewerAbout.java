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

import t0mm13b.dmesglog.R;
import t0mm13b.dmesglog.interfaces.ICustomDlgBoxListeners;
import android.app.Activity;
import android.os.Bundle;

public class DMesgViewerAbout extends Activity implements ICustomDlgBoxListeners{
	private static final int MSG_ABOUT_DLG = 0xC0FFEE;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		CustomDialogBox cdbAbout = new CustomDialogBox(this, this);
		cdbAbout.setTitle(false, getString(R.string.app_name));
		cdbAbout.setMessage(true, getString(R.string.about_dmesgviewer));
		cdbAbout.setOkIdentifier(MSG_ABOUT_DLG);
		cdbAbout.setOkVisible();
		cdbAbout.show();
	}

	@Override
	public void cbCustomDlgBoxYesFired() { }

	@Override
	public void cbCustomDlgBoxNoFired() { }

	@Override
	public void cbCustomDlgOkFired() { }

	@Override
	public void cbCustomDlgOkFired(int idOk) {
		if (idOk == MSG_ABOUT_DLG){
			this.finish();
		}
	}
}

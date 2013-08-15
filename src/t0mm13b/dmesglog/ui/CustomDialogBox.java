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
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Neat and simple custom dialog box, takes care of the order of yes/no depending on Gingerbread and earlier and the
 * latter IceCream Sandwich... thank fook for viewflippers!
 * 
 * @see ICustomDlgBoxListeners
 * 
 * @author t0mm13b
 *
 */
public class CustomDialogBox extends Dialog{
	private final int ViewYesNo = 0;
	private final int ViewOk = 1;
	private final int ViewNoYes = 2;
	private TextView mTVDialogTitle;
	private TextView mTVDialogMessage;
	private ViewFlipper mVwFlipprDlgFooter;
	private Button mBtnYes;
	private Button mBtnNo;
	private Button mBtnICSYes;
	private Button mBtnICSNo;
	private Button mBtnOk;
	private ICustomDlgBoxListeners mCustomDlgBoxListeners;
	
	protected CustomDialogBox(Context context, ICustomDlgBoxListeners listener){
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.mCustomDlgBoxListeners = listener;
		//
		this.setContentView(R.layout.custom_dialog);
		this.mTVDialogTitle = (TextView)this.findViewById(R.id.dialogTitle);
		this.mTVDialogMessage = (TextView)this.findViewById(R.id.dialogMessage);
		this.mBtnYes = (Button)this.findViewById(R.id.btnYes);
		this.mBtnNo = (Button)this.findViewById(R.id.btnNo);
		this.mBtnOk = (Button)this.findViewById(R.id.btnOk);
		this.mBtnICSNo = (Button)this.findViewById(R.id.btnICSNo);
		this.mBtnICSYes = (Button)this.findViewById(R.id.btnICSYes);
		this.mVwFlipprDlgFooter = (ViewFlipper)this.findViewById(R.id.flipprActionButtons);
		if (this.mBtnYes != null || this.mBtnICSYes != null){
			this.mBtnYes.setOnClickListener(new buttonYesListener());
			this.mBtnICSYes.setOnClickListener(new buttonYesListener());
		}
		if (this.mBtnNo != null || this.mBtnICSNo != null){
			this.mBtnNo.setOnClickListener(new buttonNoListener());
			this.mBtnICSNo.setOnClickListener(new buttonNoListener());
		}
		if (this.mBtnOk != null){
			this.mBtnOk.setOnClickListener(new buttonOkListener());
		}
	}
	public void setMessageClickable(){
		if (this.mTVDialogMessage != null){
			this.mTVDialogMessage.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}
	public void setTitle(boolean htmlStyled, String sTitle){
		if (this.mTVDialogTitle != null){
			if (htmlStyled)  this.mTVDialogTitle.setText(Html.fromHtml(sTitle));
			else this.mTVDialogTitle.setText(sTitle);
		}
	}
	public void setMessage(boolean htmlStyled, String sMessage){
		if (this.mTVDialogMessage != null){
			if (htmlStyled) this.mTVDialogMessage.setText(Html.fromHtml(sMessage));
			else this.mTVDialogMessage.setText(sMessage);
		}
	}
	public void setYesNoVisible(){
		if (Build.VERSION.SDK_INT < 10) this.mVwFlipprDlgFooter.setDisplayedChild(ViewYesNo);
		else this.mVwFlipprDlgFooter.setDisplayedChild(ViewNoYes);
	}
	public void setOkVisible(){
		this.mVwFlipprDlgFooter.setDisplayedChild(ViewOk);
	}
	public void setOkIdentifier(int identifierOkButton){
		if (identifierOkButton > 0){
			if (this.mBtnOk != null){
				this.mBtnOk.setOnClickListener(null);
				this.mBtnOk.setOnClickListener(new buttonOkListener(identifierOkButton));
			}
		}
	}
	private class buttonOkListener implements android.view.View.OnClickListener{
		private int _idOk = -1;
		public buttonOkListener(){ }
		public buttonOkListener(int identifierOkButton){
			this._idOk = identifierOkButton;
		}
		@Override
		public void onClick(View v) {
			CustomDialogBox.this.dismiss();
			if (mCustomDlgBoxListeners != null){
				if (_idOk > 0) mCustomDlgBoxListeners.cbCustomDlgOkFired(_idOk);
				else mCustomDlgBoxListeners.cbCustomDlgOkFired();
			}
		}
		
	}
	
	private class buttonYesListener implements android.view.View.OnClickListener{

		@Override
		public void onClick(View v) {
			if (mCustomDlgBoxListeners != null) mCustomDlgBoxListeners.cbCustomDlgBoxYesFired();
			CustomDialogBox.this.dismiss();
		}
		
	}
	private class buttonNoListener implements android.view.View.OnClickListener{

		@Override
		public void onClick(View v) {
			if (mCustomDlgBoxListeners != null) mCustomDlgBoxListeners.cbCustomDlgBoxNoFired();
			CustomDialogBox.this.dismiss();
		}
		
	}
}
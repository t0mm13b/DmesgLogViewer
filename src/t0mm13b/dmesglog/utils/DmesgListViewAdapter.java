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

import java.util.ArrayList;
import java.util.List;

import t0mm13b.dmesglog.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DmesgListViewAdapter extends ArrayAdapter<DmesgLine>{
	private static final String TAG = "DmesgListViewAdapter";
	private List<DmesgLine> mListDmesgs = null;
	private LayoutInflater mLayoutInflater;
	public DmesgListViewAdapter(Context context, int resource, List<DmesgLine> listDmesgLines) {
		super(context, resource, listDmesgLines);
		this.mListDmesgs = listDmesgLines;
		this.mLayoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		ViewItemHolder viHolder = null;
		if (convertView == null){
			convertView = this.mLayoutInflater.inflate(R.layout.listview_dmesg_line_row, null);
			TextView tvDmesg = (TextView)convertView.findViewById(R.id.tvDMesg);
			viHolder = new ViewItemHolder();
			viHolder.mTxtVwDmesg = tvDmesg;
			convertView.setTag(viHolder);
		}else{
			viHolder = (ViewItemHolder)convertView.getTag();
		}
		final DmesgLine dmesgLineEntry = this.getItem(position);
		if (dmesgLineEntry == null) return null;
		viHolder.mTxtVwDmesg.setText(dmesgLineEntry.toString());
		viHolder.mTxtVwDmesg.setTextColor(Color.parseColor(dmesgLineEntry.getLogLevelColour().getHexColour()));
		return convertView;
	}
	@Override
	public int getCount() {
		if (this.mListDmesgs != null) return this.mListDmesgs.size();
		return 0;
	}
	public void remove(int position){
		this.mListDmesgs.remove(position);
	}
	@Override
	public void add(DmesgLine entry){
		if (this.mListDmesgs == null){
			this.mListDmesgs = new ArrayList<DmesgLine>();
		}
		if (!this.mListDmesgs.contains(entry)){
			this.mListDmesgs.add(entry);
		}
	}
	@Override
	public void clear(){
		if (this.mListDmesgs != null){
			this.mListDmesgs.clear();
			this.notifyDataSetChanged();
		}
	}
	
	@Override
	public DmesgLine getItem(int position){
		if (this.mListDmesgs.size() > -1) return this.mListDmesgs.get(position);
		return null;
	}
	@Override
	public int getPosition(DmesgLine dmesgLine){
		int nPos = 0;
		for(DmesgLine entry : this.mListDmesgs){
			if (entry.equals(dmesgLine)) break;
			nPos++;
		}
		return nPos;
	}
	@Override
	public boolean hasStableIds(){
		return true;
	}
	// Better to get a copy of list, and return it back instead of messing with the adapter!
	public List<DmesgLine> getEntries(){
		List<DmesgLine> entryList = new ArrayList<DmesgLine>(this.mListDmesgs);
		for (DmesgLine entryDmesgLine : this.mListDmesgs){
			entryList.add(new DmesgLine(entryDmesgLine));
		}
		return entryList;
	}

	public void setListDmesgEntries(List<DmesgLine> listEntries){
		this.mListDmesgs = listEntries;
		this.notifyDataSetChanged();
	}
	
	public void Refresh(){
		Log.d(TAG, "Refresh!");
		this.notifyDataSetChanged();
	}
	public static class ViewItemHolder{
		TextView mTxtVwDmesg;
	}
}

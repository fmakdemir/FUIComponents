package com.fmakdemir.fuicomponents;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.fmakdemir.fuic.SplitView;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SplitView view = (SplitView) findViewById(R.id.split_view_test);
		TextView tv = new TextView(this);
		tv.setText("Replaced View 1");
		view.replaceFirst(tv);

		SplitView view2 = new SplitView(this);
		tv = new TextView(this);
		tv.setText("Dynamic split view");
		view2.replaceSecond(tv);

		FrameLayout root = (FrameLayout) findViewById(R.id.second_split_root);
		root.removeAllViews();
		root.addView(view2);
	}

	public static class PlaceHolderFragment extends Fragment {
		public static PlaceHolderFragment newInstance() {
			PlaceHolderFragment fragment = new PlaceHolderFragment();
			Bundle args = new Bundle();
			args.putString("Test", "test1");
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceHolderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_test, container, false);
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}

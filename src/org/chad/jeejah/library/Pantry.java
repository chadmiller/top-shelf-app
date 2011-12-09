package org.chad.jeejah.library;

import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.MenuItem;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Pantry extends PreferenceActivity {
	private final static String TAG = "org.chad.jeejah.library.Pantry";
	private GoogleAnalyticsTracker tracker;

	String[] ingredients;
	final static String PREF_PREFIX = "checkbox_ingredient ";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pantry);

		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setOnTitleClickListener(new View.OnClickListener() {
				public void onClick(View v) { Pantry.this.finish(); } });

		class SaveAction implements Action {
			@Override
			public int getDrawable() {
				return R.drawable.ic_btn_okay_and_back;
			}
			@Override
			public void performAction(View view) {
				Pantry.this.finish();
			}
		}
		actionBar.addAction(new SaveAction());

		getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.flickr_tightenup_54569946_as_background));
		getListView().setCacheColorHint(android.graphics.Color.TRANSPARENT);

		this.ingredients = getIntent().getStringArrayExtra("ingredients");
		setPreferenceScreen(createPreferenceHierarchy());
		this.tracker = GoogleAnalyticsTracker.getInstance();
		this.tracker.startNewSession(Search.GOOG_ANALYTICS_ID, 60, this);
		this.tracker.trackPageView("/" + TAG);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Toast.makeText(this, R.string.hit_back_toast, Toast.LENGTH_SHORT).show();
	}

	private PreferenceScreen createPreferenceHierarchy() {
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

		for (int i = 0; i < this.ingredients.length; i++) {
			CheckBoxPreference checkboxPref = new CheckBoxPreference(this);
			checkboxPref.setKey(PREF_PREFIX + this.ingredients[i]);
			checkboxPref.setTitle(this.ingredients[i]);
			root.addPreference(checkboxPref);
		}

		return root;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, R.id.instructions, 1, R.string.help);
		menu.add(Menu.NONE, R.id.feedback, 4, R.string.feedback);
		menu.add(Menu.NONE, R.id.credits, 5, R.string.credits);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent intent;
		switch (item.getItemId()) {
			case R.id.instructions:
				intent = new Intent(this, Instructions.class);
				startActivity(intent);
				return true;
			case R.id.feedback:
				intent = new Intent(this, Feedback.class);
				intent.putExtra("source", TAG);
				startActivity(intent);
				return true;
			case R.id.credits:
				intent = new Intent(this, Credits.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}

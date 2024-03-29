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
import android.util.Log;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

final public class Pantry extends PreferenceActivity {
	private final static String TAG = "ocjlP";
	private GoogleAnalyticsTracker tracker;

	String[] ingredientsMixerandgarnish;
	String[] ingredientsLiquorandliqueur;
	final static String FILENAME = "pantry";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pantry);

		getPreferenceManager().setSharedPreferencesName(FILENAME);

		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
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

		//getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.flickr_tightenup_54569946_as_background));
		getListView().setCacheColorHint(android.graphics.Color.TRANSPARENT);

		this.ingredientsMixerandgarnish = getIntent().getStringArrayExtra("ingredients-mixerandgarnish");
		this.ingredientsLiquorandliqueur = getIntent().getStringArrayExtra("ingredients-liquorandliqueur");
		setPreferenceScreen(createPreferenceHierarchy());
		this.tracker = GoogleAnalyticsTracker.getInstance();
		this.tracker.startNewSession(BookDisplay.GOOG_ANALYTICS_ID, 60, this);
		this.tracker.trackPageView("/" + TAG);
	}

	private PreferenceScreen createPreferenceHierarchy() {

		final PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

		final android.preference.PreferenceCategory mgCategory = new android.preference.PreferenceCategory(this);
		root.addPreference(mgCategory);
		mgCategory.setTitle(R.string.pref_mixersgarnishes);
		mgCategory.setOrderingAsAdded(false);
		for (String name : ingredientsMixerandgarnish) {
			final CheckBoxPreference checkboxPref = new CheckBoxPreference(this);
			checkboxPref.setKey(name);
			checkboxPref.setTitle(name);
			mgCategory.addPreference(checkboxPref);
		}

		final android.preference.PreferenceCategory llCategory = new android.preference.PreferenceCategory(this);
		root.addPreference(llCategory);
		llCategory.setTitle(R.string.pref_liquors);
		llCategory.setOrderingAsAdded(false);
		for (String name : ingredientsLiquorandliqueur) {
			final CheckBoxPreference checkboxPref = new CheckBoxPreference(this);
			checkboxPref.setKey(name);
			checkboxPref.setTitle(name);
			llCategory.addPreference(checkboxPref);
		}

		return root;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, R.id.instructions, 1, R.string.help).setIcon(android.R.drawable.ic_menu_help);
		//menu.add(Menu.NONE, R.id.feedback, 4, R.string.feedback);
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
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}

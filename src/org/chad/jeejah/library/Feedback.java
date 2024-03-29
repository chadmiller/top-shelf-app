package org.chad.jeejah.library;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Feedback extends Activity {
	private final static String TAG = "ocjlF";
	private GoogleAnalyticsTracker tracker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.feedback);

		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setOnTitleClickListener(new View.OnClickListener() {
				public void onClick(View v) { Feedback.this.finish(); } });

		this.tracker = GoogleAnalyticsTracker.getInstance();
		this.tracker.startNewSession(BookDisplay.GOOG_ANALYTICS_ID, 60, this);
		this.tracker.trackPageView("/" + TAG);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, R.id.instructions, 1, R.string.help).setIcon(android.R.drawable.ic_menu_help);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		final Intent intent;
		switch (item.getItemId()) {
			case R.id.instructions:
				intent = new Intent(this, Instructions.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}

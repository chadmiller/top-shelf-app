package org.chad.jeejah.library;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Credits extends Activity {
	private final static String TAG = "org.chad.jeejah.library.Credits";
	private GoogleAnalyticsTracker tracker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.credits);

		this.tracker = GoogleAnalyticsTracker.getInstance();
		this.tracker.startNewSession(Search.GOOG_ANALYTICS_ID, 60, this);
		this.tracker.trackPageView("/" + TAG);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, R.id.instructions, 1, R.string.help);
		menu.add(Menu.NONE, R.id.feedback, 4, R.string.feedback);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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

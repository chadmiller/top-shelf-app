package org.chad.jeejah.library;

import android.view.View;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import java.util.TreeSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ShoppingListActivity extends Activity {
	private static final String TAG = "org.chad.jeejah.library.ShoppingListActivity";
	public static final String FAV_KEY = "favorites' missing ingredients info";
	public static final String SINGLE_KEY = "single ingredients info";
	public static final String MOSTUSED_KEY = "most requested ingredients";

	private static final int MAX_PRODUCTIVE_INGREDIENTS_WANTED = 5;

	private GoogleAnalyticsTracker tracker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.shopping_list);

		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setOnTitleClickListener(new View.OnClickListener() {
				public void onClick(View v) { ShoppingListActivity.this.finish(); } });

		final Intent intent = getIntent();

		this.tracker = GoogleAnalyticsTracker.getInstance();
		this.tracker.startNewSession(BookDisplay.GOOG_ANALYTICS_ID, 60, this);
		this.tracker.trackPageView("/" + TAG);

		{
			LinearLayout container = (LinearLayout) findViewById(R.id.single_ingredient_suggestions);
			final Bundle bundle = intent.getBundleExtra(SINGLE_KEY);
			final String[] ingredients = bundle.getStringArray("keys");

			Comparator reverseSortIngredientsByRecipeCount = new Comparator<String>() {
				public int compare(String lhs, String rhs) {
					try {
						final int lhsCount = bundle.getStringArray("enabledby "+lhs).length;
						try {
							final int rhsCount = bundle.getStringArray("enabledby "+rhs).length;
							return rhsCount - lhsCount; // sort descending
						} catch (NullPointerException ex) {
							return 1;
						}
					} catch (NullPointerException ex) {
						return -1;
					}
				}
			};

			Arrays.sort(ingredients, reverseSortIngredientsByRecipeCount);

			int limit = MAX_PRODUCTIVE_INGREDIENTS_WANTED;
			boolean hasWritten = false;
			if (ingredients != null) {
				for (int i = 0; i < ingredients.length; i++) {
					if (limit-- < 1) break;
					int recipeCount = bundle.getStringArray("enabledby "+ingredients[i]).length;
					TextView t = new TextView(this);
					t.setText(Html.fromHtml("\u2022 <b>" + ingredients[i] + "</b> would let you make " + recipeCount + " more recipes."));
					t.setTextSize(16);
					t.setPadding(10, 8, 10, 2);
					container.addView(t);
					hasWritten = true;
				}
			} else {
				Log.d(TAG, "No key/" + SINGLE_KEY + " in bundle.");
			}

			if (! hasWritten) {
				Log.i(TAG, "Want to hide single-ingredient container.");
				container.setVisibility(View.GONE);
			}
		}


		{
			boolean hasWritten = false;
			LinearLayout container = (LinearLayout) findViewById(R.id.favorites_ingredient_suggestions);
			hasWritten = false;
			Bundle bundle = intent.getBundleExtra(FAV_KEY);

			if ((bundle != null) && (bundle.containsKey("ingredients"))) {
				String[] ingredients = bundle.getStringArray("ingredients");

				for (String ingredient : ingredients) {
					TextView t = new TextView(this);
					t.setText(Html.fromHtml("\u2022 <b>" + ingredient + "</b>"));
					t.setTextSize(16);
					t.setPadding(20, 3, 10, 2);
					container.addView(t);
					hasWritten = true;
				}

			}

			if (! hasWritten) {
				container.setVisibility(View.GONE);
			}
		}


		{
			boolean hasWritten = false;
			LinearLayout container = (LinearLayout) findViewById(R.id.most_used_ingredients);
			hasWritten = false;
			Bundle bundle = intent.getBundleExtra(MOSTUSED_KEY);

			if ((bundle != null) && (bundle.containsKey("ingredients"))) {
				ArrayList<String> ingredients = bundle.getStringArrayList("ingredients");
				for (String s : ingredients) {
					TextView t = new TextView(this);
					t.setText(Html.fromHtml("\u2022 <b>" + s + "</b>"));
					t.setTextSize(16);
					t.setPadding(20, 3, 10, 2);
					container.addView(t);
					hasWritten = true;
				}
			}

			if (! hasWritten) {
				container.setVisibility(View.GONE);
			}

		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, R.id.instructions, 1, R.string.help).setIcon(android.R.drawable.ic_menu_help);
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

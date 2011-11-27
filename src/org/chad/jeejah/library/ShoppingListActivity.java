
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

import java.util.TreeSet;
import java.util.Iterator;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

public class ShoppingListActivity extends Activity {
	private static final String TAG = "org.chad.jeejah.library.ShoppingListActivity";
	public static final String FAV_KEY = "favorites' missing ingredients info";
	public static final String SINGLE_KEY = "single ingredients info";
	public static final String MOSTUSED_KEY = "most requested ingredients";

	private static final int MAX_RECIPES_IN_INGRED = 4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shopping_list);

		Intent intent = getIntent();

		{
			LinearLayout container = (LinearLayout) findViewById(R.id.single_ingredient_suggestions);
			Bundle bundle = intent.getBundleExtra(SINGLE_KEY);
			String[] ingredients = bundle.getStringArray("keys");
			boolean hasWritten = false;
			if (ingredients != null) {
				int visibilityLimit = 2 + (ingredients.length / 17);
				Log.d(TAG, "visibility limit for single ingredient suggestions is " + visibilityLimit + " recipes.");
				for (int i = 0; i < ingredients.length; i++) {
					String[] recipes = bundle.getStringArray("enabledby "+ingredients[i]);
					if ((recipes != null) && (recipes.length > visibilityLimit)) {
						TextView t = new TextView(this);
						t.setText(Html.fromHtml("\u2022 <b>" + ingredients[i] + "</b> would let you make"));
						t.setTextSize(16);
						t.setPadding(10, 8, 10, 2);
						t.setTextColor(android.graphics.Color.WHITE);
						container.addView(t);
						hasWritten = true;
						if (recipes.length == MAX_RECIPES_IN_INGRED) {
							for (int j = 0; j < recipes.length; j++) {
								TextView tt = new TextView(this);
								tt.setText("\u2022 " + recipes[j]);
								tt.setPadding(38, 3, 10, 2);
								tt.setTextColor(android.graphics.Color.WHITE);
								container.addView(tt);
							}
						} else {
							for (int j = 0; j < Math.min(MAX_RECIPES_IN_INGRED-1, recipes.length); j++) {
								TextView tt = new TextView(this);
								tt.setText("\u2022 " + recipes[j]);
								tt.setPadding(38, 3, 10, 2);
								tt.setTextColor(android.graphics.Color.WHITE);
								container.addView(tt);
							}
							if (recipes.length > MAX_RECIPES_IN_INGRED) {
								TextView tt = new TextView(this);
								tt.setText(" ... and " + (recipes.length-MAX_RECIPES_IN_INGRED+1) + " more.");
								tt.setPadding(38, 3, 10, 2);
								tt.setTextColor(android.graphics.Color.WHITE);
								container.addView(tt);
							}
						}
					}
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
			if ((bundle != null) ) {
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
				Iterator it = ingredients.iterator();
				while (it.hasNext()) {
					TextView t = new TextView(this);
					t.setText(Html.fromHtml("\u2022 <b>" + ((String) it.next()) + "</b>"));
					t.setTextSize(16);
					t.setPadding(20, 3, 10, 2);
					t.setTextColor(android.graphics.Color.WHITE);
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
		menu.add(Menu.NONE, R.id.instructions, 1, "Help");
		menu.add(Menu.NONE, R.id.feedback, 4, "Feedback");
		menu.add(Menu.NONE, R.id.credits, 5, "Credits");
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

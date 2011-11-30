
package org.chad.jeejah.library;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.content.Intent;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.TreeSet;
import java.util.Iterator;

public class RecipeActivity extends Activity {
	private final static String TAG = "org.chad.jeejah.library.RecipeActivity";

	public static final String PREF_PREFIX_FAVORITED = "favorited ";
	private boolean isFavorited = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recipe);

		final Intent intent = getIntent();

		final Bundle recipeInfo = intent.getBundleExtra("recipe");
		final TextView titleView = (TextView) findViewById(R.id.recipe_title);

		final String title = recipeInfo.getString(Recipe.KEY_NAME);
		titleView.setText(title);

		final LinearLayout ingredientsContainer = (LinearLayout) findViewById(R.id.recipe_ingredients);
		final LinearLayout preparationContainer = (LinearLayout) findViewById(R.id.recipe_preparation);
		final LinearLayout consumationContainer = (LinearLayout) findViewById(R.id.recipe_consumation);

		final String[] ingredients = recipeInfo.getStringArray(Recipe.KEY_INGREDIENTS);
		final String[] preparation = recipeInfo.getStringArray(Recipe.KEY_PREPARE_INST);
		final String[] consumation = recipeInfo.getStringArray(Recipe.KEY_CONSUME_INST);

		final Resources res = getResources();
		final TreeSet<String> jargonSet = new TreeSet<String>();

		for (int i = 0; i < ingredients.length; i++) {
			TextView t = new TextView(this);
			t.setText("\u2022   " + ingredients[i]);
			t.setPadding(30, 3, 30, 2);
			ingredientsContainer.addView(t);
		}

		for (int i = 0; i < preparation.length; i++) {
			TextView t = new TextView(this);
			t.setText("" + (i+1) + ".  " + preparation[i]);
			t.setPadding(30, 5, 30, 5);
			preparationContainer.addView(t);

			if (preparation[i].matches(".*?\\bblend\\b.*"))
				jargonSet.add(res.getString(R.string.term_blend));
			if (preparation[i].matches(".*?\\bbuild\\b.*"))
				jargonSet.add(res.getString(R.string.term_build));
			if (preparation[i].matches(".*?\\bdash\\b.*"))
				jargonSet.add(res.getString(R.string.term_dash));
			if (preparation[i].matches(".*?\\bfill\\b.*"))
				jargonSet.add(res.getString(R.string.term_fill));
			if (preparation[i].matches(".*?\\bfloat\\b.*"))
				jargonSet.add(res.getString(R.string.term_float));
			if (preparation[i].matches(".*?\\blayer\\b.*"))
				jargonSet.add(res.getString(R.string.term_layer));
			if (preparation[i].matches(".*?\\bmuddle\\b.*"))
				jargonSet.add(res.getString(R.string.term_muddle));
			if (preparation[i].matches(".*?\\brim\\b.*"))
				jargonSet.add(res.getString(R.string.term_rim));
			if (preparation[i].matches(".*?\\bsplash\\b.*"))
				jargonSet.add(res.getString(R.string.term_splash));
			//if (preparation[i].matches("\\btop\\b")
				//jargonSet.add(res.getString(R.string.term_top));
		}

		for (int i = 0; i < consumation.length; i++) {
			TextView t = new TextView(this);
			t.setText("Consume instruction: " + consumation[i]);
			consumationContainer.addView(t);
		}

		if (jargonSet.size() > 0) {
			StringBuilder s = new StringBuilder();
			Iterator i = jargonSet.iterator();
			while (i.hasNext()) {
				s.append(i.next()).append("\n\n");
			}
			TextView jargon = (TextView) findViewById(R.id.jargon_defined);
			jargon.setText("Jargon:\n\n" + s.toString());
		} else {
			View v = (View) findViewById(R.id.jargon_seperator);
			v.setVisibility(View.GONE);
		}

		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		final ImageButton favButton = (ImageButton) findViewById(R.id.recipe_favorited_icon);
		this.isFavorited = sp.getBoolean(PREF_PREFIX_FAVORITED + title, false);
		setFavoriteButtonEnabled(favButton, this.isFavorited);
		favButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				RecipeActivity.this.isFavorited = ! RecipeActivity.this.isFavorited;
				SharedPreferences.Editor e = sp.edit();
				e.putBoolean(PREF_PREFIX_FAVORITED + title, RecipeActivity.this.isFavorited);
				e.commit();

				RecipeActivity.this.setFavoriteButtonEnabled((ImageButton) v, RecipeActivity.this.isFavorited);
			}
		});
	}

	private void setFavoriteButtonEnabled(ImageButton ib, boolean newState) {
		if (newState) {
			ib.setImageResource(android.R.drawable.btn_star_big_on);
		} else {
			ib.setImageResource(android.R.drawable.btn_star_big_off);
		}
		Log.d(TAG, "Setting favorited to " + newState);
	}

	@Override
	public void onResume() {
		super.onResume();
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

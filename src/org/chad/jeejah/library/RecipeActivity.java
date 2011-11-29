
package org.chad.jeejah.library;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.TreeSet;
import java.util.Iterator;

public class RecipeActivity extends Activity {
	private final static String TAG = "org.chad.jeejah.library.RecipeActivity";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recipe);

		Intent intent = getIntent();
		Bundle recipeInfo = intent.getBundleExtra("recipe");
		TextView title = (TextView) findViewById(R.id.recipe_title);

		title.setText(recipeInfo.getString(Recipe.KEY_NAME));

		LinearLayout ingredientsContainer = (LinearLayout) findViewById(R.id.recipe_ingredients);
		LinearLayout preparationContainer = (LinearLayout) findViewById(R.id.recipe_preparation);
		LinearLayout consumationContainer = (LinearLayout) findViewById(R.id.recipe_consumation);

		String[] ingredients = recipeInfo.getStringArray(Recipe.KEY_INGREDIENTS);
		String[] preparation = recipeInfo.getStringArray(Recipe.KEY_PREPARE_INST);
		String[] consumation = recipeInfo.getStringArray(Recipe.KEY_CONSUME_INST);

		Resources res = getResources();
		TreeSet<String> jargonSet = new TreeSet<String>();


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

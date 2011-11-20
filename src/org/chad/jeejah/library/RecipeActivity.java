
package org.chad.jeejah.library;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.content.Intent;

public class RecipeActivity extends Activity {
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


		for (int i = 0; i < ingredients.length; i++) {
			TextView t = new TextView(this);
			t.setText("      " + ingredients[i]);
			ingredientsContainer.addView(t);
		}

		for (int i = 0; i < preparation.length; i++) {
			TextView t = new TextView(this);
			t.setText("      " + preparation[i]);
			preparationContainer.addView(t);
		}

		for (int i = 0; i < consumation.length; i++) {
			TextView t = new TextView(this);
			t.setText("      " + consumation[i]);
			consumationContainer.addView(t);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
	}
}

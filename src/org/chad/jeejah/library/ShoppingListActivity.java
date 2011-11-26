
package org.chad.jeejah.library;

import android.view.View;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import java.util.TreeSet;
import java.util.Iterator;
import java.util.Arrays;

public class ShoppingListActivity extends Activity {
	private static final String TAG = "org.chad.jeejah.library.ShoppingListActivity";
	public static final String FAV_KEY = "favorites' missing ingredients info";
	public static final String SINGLE_KEY = "single ingredients info";


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shopping_list);

		Intent intent = getIntent();
		Bundle singleIngredients = intent.getBundleExtra(SINGLE_KEY);

		LinearLayout singleIngredientSuggestionsContainer = (LinearLayout) findViewById(R.id.single_ingredient_suggestions);

		String[] ingredients = singleIngredients.getStringArray("keys");

		boolean hasWritten = false;
		if (ingredients != null) {
			int visibilityLimit = 2 + (ingredients.length / 17);
			Log.d(TAG, "visibility limit for single ingredient suggestions is " + visibilityLimit + " recipes.");
			for (int i = 0; i < ingredients.length; i++) {
				String[] recipes = singleIngredients.getStringArray("enabledby "+ingredients[i]);
				if ((recipes != null) && (recipes.length > visibilityLimit)) {
					TextView t = new TextView(this);
					t.setText("\u2022 " + ingredients[i] + " would let you make");
					t.setTextSize(16);
					t.setPadding(10, 8, 10, 2);
					singleIngredientSuggestionsContainer.addView(t);
					hasWritten = true;
					for (int j = 0; j < recipes.length; j++) {
						Log.v(TAG, "saying " + ingredients[i] + " makes " + recipes[j]);
						TextView tt = new TextView(this);
						tt.setText("\u2022 " + recipes[j]);
						tt.setPadding(38, 3, 10, 2);
						singleIngredientSuggestionsContainer.addView(tt);
					}
				} else {
					Log.d(TAG, "Not displaying " + ingredients[i] + " because it doesn't make enough.");
				}
			}
		} else {
			Log.d(TAG, "No key/" + SINGLE_KEY + " in bundle.");
		}

		if (! hasWritten) {
			Log.i(TAG, "Want to hide single-ingredient container.");
			//singleIngredientSuggestionsContainer.setVisibility(View.GONE);
		}


		LinearLayout favoritesIngredientSuffestions = (LinearLayout) findViewById(R.id.favorites_ingredient_suggestions);
		hasWritten = false;


		Bundle favoritesIngredientsMissingBundle = intent.getBundleExtra(FAV_KEY);

		if (! hasWritten) {
			favoritesIngredientSuffestions.setVisibility(View.GONE);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
	}
}

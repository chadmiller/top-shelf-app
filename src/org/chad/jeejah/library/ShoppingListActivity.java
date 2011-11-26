
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
						container.addView(t);
						hasWritten = true;
						for (int j = 0; j < recipes.length; j++) {
							Log.v(TAG, "saying " + ingredients[i] + " makes " + recipes[j]);
							TextView tt = new TextView(this);
							tt.setText("\u2022 " + recipes[j]);
							tt.setPadding(38, 3, 10, 2);
							container.addView(tt);
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
					t.setPadding(10, 3, 10, 2);
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
}

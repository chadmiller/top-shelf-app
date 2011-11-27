/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.view.MenuItem;

public class Pantry extends PreferenceActivity {
	private final static String TAG = "org.chad.jeejah.library.Pantry";

	String[] ingredients;
	final static String PREF_PREFIX = "checkbox_ingredient ";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.flickr_tightenup_54569946_as_background));
		getListView().setCacheColorHint(android.graphics.Color.TRANSPARENT);

		this.ingredients = getIntent().getStringArrayExtra("ingredients");
		setPreferenceScreen(createPreferenceHierarchy());
	}

	@Override
	protected void onResume() {
		super.onResume();
		Toast.makeText(this, "Hit Back when finished.", Toast.LENGTH_SHORT).show();
	}

	private PreferenceScreen createPreferenceHierarchy() {
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

		for (int i = 0; i < this.ingredients.length; i++) {
			CheckBoxPreference checkboxPref = new CheckBoxPreference(this);
			checkboxPref.setKey(PREF_PREFIX + this.ingredients[i]);
			checkboxPref.setTitle(this.ingredients[i]);
			root.addPreference(checkboxPref);
		}

		return root;
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

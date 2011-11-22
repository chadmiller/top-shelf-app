package org.chad.jeejah.library;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.Intent;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public class Search extends Activity {
	public final static String TAG = "org.chad.jeejah.library.Search";

	private RecipeBook recipeBook;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.recipeBook = (RecipeBook) getLastNonConfigurationInstance();
		if (this.recipeBook == null) {
			this.recipeBook = new RecipeBook(this);
		}

		setUp();
	}

	void setUp() {

		TextView drinksListHeader = (TextView) findViewById(R.id.drinks_list_header);
		Set<String> pantry = new HashSet<String>();

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

		Iterator it = sp.getAll().keySet().iterator();
		while (it.hasNext()) {
			Object k = it.next();
			String name = (String) k;
			if (name.startsWith(Pantry.PREF_PREFIX)) {
				if (sp.getBoolean(name, false)) {
					pantry.add(name.substring(Pantry.PREF_PREFIX.length()));
				}
			}
		}

		if (pantry.isEmpty()) {
			showDialog(1);
		}

		Recipe[] canMake = recipeBook.recipesConstructable(pantry);

		drinksListHeader.setText(String.format("You can make %d recipes (out of %d known recipes) with your %d ingredients:", canMake.length, recipeBook.recipes.size(), pantry.size()));

		drinksListHeader.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(Search.this, Pantry.class);
				intent.putExtra("ingredients", recipeBook.knownIngredients.toArray(new String[recipeBook.knownIngredients.size()]));
				Search.this.startActivityForResult(intent, 1);
			}
		});

		ListView computedAvailableDrinks = (ListView) findViewById(R.id.computed_available_drinks);
		computedAvailableDrinks.setAdapter(new RecipeAdapter(this, canMake));

		computedAvailableDrinks.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Recipe recipe = (Recipe) parent.getItemAtPosition(position);
				Intent intent = new Intent(Search.this, RecipeActivity.class);
				intent.setAction(Intent.ACTION_VIEW);
				android.os.Bundle recipe_info = new android.os.Bundle();
				recipe_info.putString(Recipe.KEY_NAME, recipe.name);
				recipe_info.putStringArray(Recipe.KEY_PREPARE_INST, recipe.prepare_instructions.toArray(new String[recipe.prepare_instructions.size()]));
				recipe_info.putStringArray(Recipe.KEY_CONSUME_INST, recipe.consume_instructions.toArray(new String[recipe.consume_instructions.size()]));
				recipe_info.putStringArray(Recipe.KEY_INGREDIENTS, recipe.ingredients.toArray(new String[recipe.ingredients.size()]));
				intent.putExtra("recipe", recipe_info);
				Search.this.startActivity(intent);
			}
		});
	}



	@Override
	public Dialog onCreateDialog(int id) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Welcome!")
			.setMessage("Enter the ingredients you own to see what recipes you can make with them.")
			.setPositiveButton("Begin", new DialogInterface.OnClickListener() { 
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Search.this, Pantry.class);
					intent.putExtra("ingredients", Search.this.recipeBook.knownIngredients.toArray(new String[Search.this.recipeBook.knownIngredients.size()]));
					Search.this.startActivityForResult(intent, 1);
				}
			});
		return adb.create();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		setUp();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return this.recipeBook;
	}

}

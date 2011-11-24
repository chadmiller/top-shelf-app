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
import android.view.Window;
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

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class Search extends Activity {
	public final static String TAG = "org.chad.jeejah.library.Search";

	private RecipeBook recipeBook;

	private RecipeAdapter recipeAdapter;
	private TextView recipeListFootnote;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		if (System.currentTimeMillis() > 1330804221000L) { finish(); }

		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);

		this.recipeBook = (RecipeBook) getLastNonConfigurationInstance();
		if (this.recipeBook == null) {
			this.recipeBook = new RecipeBook(this);
		}

		Set<String> pantry = new HashSet<String>();
		this.recipeAdapter = new RecipeAdapter(this, recipeBook, pantry, true);

		ListView recipeListView = (ListView) findViewById(R.id.recipe_list);
		recipeListView.setAdapter(this.recipeAdapter);
		recipeListView.setOnItemClickListener(new OnItemClickListener() {
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

		class ListToggleAction implements Action {
			@Override
			public int getDrawable() {
				return R.drawable.ic_btn_toggle_viewable;
			}

			@Override
			public void performAction(View view) {
				Search.this.recipeAdapter.toggleVisibility();
				Search.this.updateFootnote();
			}
		}
		actionBar.addAction(new ListToggleAction());


		class PickIngredientsAction implements Action {
			@Override
			public int getDrawable() {
				return R.drawable.ic_btn_mark_owned_ingredients;
			}

			@Override
			public void performAction(View view) {
				Intent intent = new Intent(Search.this, Pantry.class);
				intent.putExtra("ingredients", Search.this.recipeBook.knownIngredients.toArray(new String[Search.this.recipeBook.knownIngredients.size()]));
				Search.this.startActivityForResult(intent, 1);
			}
		}
		actionBar.addAction(new PickIngredientsAction());

		this.recipeListFootnote = (TextView) findViewById(R.id.recipe_list_footnote);
		this.recipeListFootnote.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(Search.this, Pantry.class);
				intent.putExtra("ingredients", Search.this.recipeBook.knownIngredients.toArray(new String[Search.this.recipeBook.knownIngredients.size()]));
				Search.this.startActivityForResult(intent, 1);
			}
		});


		setUp();
	}


	void updateFootnote() {
		if (this.recipeAdapter.isFiltered()) {
			if (this.recipeAdapter.targetRecipeList.size() == 0) {
				this.recipeListFootnote.setText("There are no recipes that satisfy the ingredients you say you have. (Tap here to update your list.)");
				this.recipeListFootnote.setVisibility(View.VISIBLE);
			} else {
				this.recipeListFootnote.setVisibility(View.GONE);
			}
		} else {
			this.recipeListFootnote.setVisibility(View.VISIBLE);
			this.recipeListFootnote.setText(" * You don't own an ingredient. (Tap here to update your list.)");
		}
	}

	void setUp() {
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

		this.recipeBook.updateProducable(pantry);
		this.recipeAdapter.updatePantry(pantry);
		this.updateFootnote();

		//ListView recipeListView = (ListView) findViewById(R.id.recipe_list);
		//Log.d(TAG, "would set fastScroll " + (this.recipeAdapter.targetRecipeList.size() > 21));
		//computedAvailableDrinks.setFastScrollEnabled(this.recipeAdapter.targetRecipeList.size() > 21);

		//TextView drinksListHeader = (TextView) findViewById(R.id.drinks_list_header);
		//drinksListHeader.setText(String.format("You can make %d recipes (out of %d known recipes) with your %d ingredients:", recipeBook.producableRecipes.size(), recipeBook.allRecipes.size(), pantry.size()));

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

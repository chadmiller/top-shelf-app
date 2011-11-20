package org.chad.jeejah.library;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;

import java.util.Set;
import java.util.HashSet;

public class Search extends Activity {

	private Set<String> pantry;
	private RecipeBook recipeBook;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TextView drinksListHeader = (TextView) findViewById(R.id.drinks_list_header);

		this.recipeBook = new RecipeBook(this);
		this.pantry = new HashSet<String>();

		this.pantry.add("coffee liqueur");
		this.pantry.add("coffee");
		this.pantry.add("vodka");
		this.pantry.add("cream");

		Recipe[] canMake = recipeBook.recipesConstructable(this.pantry);

		drinksListHeader.setText(String.format("You can make %d (out of %d known) drinks drinks with your %d ingredients.", canMake.length, recipeBook.recipes.size(), this.pantry.size()));

		ListView computedAvailableDrinks = (ListView) findViewById(R.id.computed_available_drinks);
		computedAvailableDrinks.setAdapter(new ArrayAdapter<Recipe>(this, R.layout.recipe_list_item, canMake));

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
}

package org.chad.jeejah.library;

import android.util.Log;
import android.content.Context;

import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

class RecipeBook {
	public final static String TAG = "org.chad.jeejah.library.RecipeBook";

//	private class Ingredient {
//		public final String name;
//		public List<Recipe> used_by;
//		public Ingredient(String name) {
//			this.name = name;
//			this.used_by = new LinkedList<Recipe>();
//		}
//	};

	public List<Recipe> recipes;
	public Set<String> knownIngredients;

	public RecipeBook(Context context) {
		recipes = new LinkedList<Recipe>();
		this.knownIngredients = new TreeSet<String>();

		try {
			java.io.InputStream recipeFile = context.getResources().openRawResource(R.raw.recipes);
			JsonFactory jsonFactory = new JsonFactory();
			JsonParser jp = jsonFactory.createJsonParser(recipeFile);

			int i = 0;
			while (jp.nextToken() != JsonToken.END_ARRAY) {

				Recipe recipe = new Recipe();
				while (jp.nextToken() != JsonToken.END_OBJECT) {
					String fieldname = jp.getCurrentName();
					if (fieldname == null) {
					} else if (fieldname.equals("name")) {
						jp.nextToken();
						recipe.name = jp.getText();
					} else if (fieldname.equals("glasses")) {

						jp.nextToken();
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							if (recipe.glass != null) {
								Log.v(TAG, "Glass already set to " + recipe.glass + " and now want " + jp.getText());
							}
							recipe.glass = jp.getText();
						}
					} else if (fieldname.equals("prepare_instructions")) {
						jp.nextToken();
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							recipe.prepare_instructions.add(jp.getText());
						}
					} else if (fieldname.equals("consume_instructions")) {
						jp.nextToken();
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							recipe.consume_instructions.add(jp.getText());
						}
					} else if (fieldname.equals("ingredients")) {
						jp.nextToken();
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							recipe.ingredients.add(jp.getText());
							this.knownIngredients.add(jp.getText());
						}
					} else {
						Log.e(TAG, "  UNKNOWN: " + jp.getCurrentToken());
					}
				}
				this.recipes.add(recipe);

			}

			Log.d(TAG, "recipes count " + this.recipes.size());

		} catch (java.io.IOException ex) {
			Log.e(TAG, "Can't parse JSON", ex);
		}
	}


	public Recipe[] recipesConstructable(Set<String> pantry) {
		TreeSet<Recipe> results = new TreeSet<Recipe>();
		Iterator bookIterator = this.recipes.iterator();

		while(bookIterator.hasNext()) {
			Recipe recipe = (Recipe) bookIterator.next(); 
			if (pantry.containsAll(recipe.ingredients)) {
				results.add(recipe);
			}
		}

		return results.toArray(new Recipe[results.size()]);
	}


	// get all recipes

	// make a list of all ingredients.   "unownedIngredients"
	// remove all ingredients we own.

	// for each ingredient unownedIngredients,
	//    for each recipe,
	//        if incredient is in recipe,
	//            candidate_recipes.remove(recipe)

}

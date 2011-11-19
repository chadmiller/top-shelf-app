package org.chad.jeejah.library;

import android.util.Log;
import android.content.Context;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

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

	private class Recipe {
		public Set<String> ingredients;
		public List<String> prepare_instructions;
		public List<String> consume_instructions;
		public String name;
		public String glass;

		public Recipe() {
			this.ingredients = new HashSet<String>();
			this.prepare_instructions = new LinkedList<String>();
			this.consume_instructions = new LinkedList<String>();
		}
		public Recipe(String name) {
			this.ingredients = new HashSet<String>();
			this.prepare_instructions = new LinkedList<String>();
			this.consume_instructions = new LinkedList<String>();
			this.name = name;
		}
	};

	private List<Recipe> recipes;
	private Set<String> ownedIngredients;

	public RecipeBook(Context context) {
		this.recipes = new LinkedList<Recipe>();
		this.ownedIngredients = new HashSet<String>();

		try {
			java.io.InputStream recipeFile = context.getResources().openRawResource(R.raw.recipes);
			JsonFactory jsonFactory = new JsonFactory();
			JsonParser jp = jsonFactory.createJsonParser(recipeFile);

			while (jp.nextToken() != JsonToken.END_OBJECT) {
				if (jp.isExpectedStartArrayToken()) {
					Log.i(TAG, "new recipe");
					Recipe recipe = new Recipe();
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						String fieldname = jp.getCurrentName();
						if (fieldname == null) {
							Log.v(TAG, "  no fieldname!  " + jp.getCurrentToken().asString() + "");
						} else if (fieldname.equals("name")) {
							jp.nextToken();
							Log.v(TAG, "  name: " + jp.getText());
							recipe.name = jp.getText();
						} else if (fieldname.equals("glasses")) {

							jp.nextToken();
							while (jp.nextToken() != JsonToken.END_ARRAY) {
								Log.v(TAG, "  use glass: " + jp.getText());
								recipe.glass = jp.getText();
							}
							jp.nextToken();
						} else if (fieldname.equals("prepare_instructions")) {
							jp.nextToken();
							while (jp.nextToken() != JsonToken.END_ARRAY) {
								Log.v(TAG, "  prepare instuction: " + jp.getText());
								recipe.prepare_instructions.add(jp.getText());
							}
							jp.nextToken();
						} else if (fieldname.equals("consume_instructions")) {
							jp.nextToken();
							while (jp.nextToken() != JsonToken.END_ARRAY) {
								Log.v(TAG, "  consume instuction: " + jp.getText());
								recipe.consume_instructions.add(jp.getText());
							}
							jp.nextToken();
						} else if (fieldname.equals("ingredients")) {
							jp.nextToken();
							while (jp.nextToken() != JsonToken.END_ARRAY) {
								Log.v(TAG, "  ingredient: " + jp.getText());
								recipe.ingredients.add(jp.getText());
							}
							jp.nextToken();
						} else {
							Log.e(TAG, "  UNKNOWN: " + jp.getText());
						}
					}
					this.recipes.add(recipe);

				} else {
					Log.e(TAG, "Consuming something. " + jp.getCurrentToken().asString());
					jp.nextToken();
				}
			}

		} catch (java.io.IOException ex) {
			Log.e(TAG, "Can't parse JSON", ex);
		}

	}


	// get all recipes

	// make a list of all ingredients.   "unownedIngredients"
	// remove all ingredients we own.

	// for each ingredient unownedIngredients,
	//    for each recipe,
	//        if incredient is in recipe,
	//            candidate_recipes.remove(recipe)

}

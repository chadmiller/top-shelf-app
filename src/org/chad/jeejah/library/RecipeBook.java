package org.chad.jeejah.library;

import android.util.Log;
import android.content.Context;

import java.util.Map;
import java.util.Hashtable;

import java.util.Set;
import java.util.TreeSet;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import java.util.Collections;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

class RecipeBook {
	private final static String TAG = "org.chad.jeejah.library.RecipeBook";

	public String version;

	public Map<String,Recipe> allRecipeIndex;
	public List<Recipe> searchResultRecipes;
	public List<Recipe> allRecipes;
	public List<Recipe> producableRecipes;
	public Map<String,List<Recipe>> countRecipesSoleAdditionalIngredient;
	public ArrayList<String> mostUsedIngredients;

	public Set<String> knownIngredients;

	private Context context;

	public RecipeBook(Context context) {
		this.allRecipes = new ArrayList<Recipe>(2400);
		this.knownIngredients = new TreeSet<String>();
		this.mostUsedIngredients = new ArrayList<String>(17);
		this.allRecipeIndex = new Hashtable<String,Recipe>(2400);
		this.countRecipesSoleAdditionalIngredient = new Hashtable<String,List<Recipe>>();
		this.producableRecipes = new ArrayList<Recipe>();
		this.searchResultRecipes = new ArrayList<Recipe>();
		this.context = context;
	}

	public void load(Runnable updater) {

		try {
			java.io.InputStream gzfile = context.getResources().openRawResource(R.raw.recipes);
			gzfile.skip(256L);
			java.util.zip.GZIPInputStream recipeFile = new java.util.zip.GZIPInputStream(gzfile);

			JsonFactory jsonFactory = new JsonFactory();
			JsonParser jp = jsonFactory.createJsonParser(recipeFile);
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				jp.nextToken();
				String fieldname = jp.getCurrentName();
				if (fieldname.equals("version")) {
					this.version = jp.getText();
				} else if (fieldname.equals("most_used_ingredients")) {
					jp.nextToken();
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						this.mostUsedIngredients.add(jp.getText());
					}
				} else if (fieldname.equals("book")) {
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						jp.nextToken();
						Recipe recipe = new Recipe();
						while (jp.nextToken() != JsonToken.END_OBJECT) {
							fieldname = jp.getCurrentName();
							if (fieldname == null) {
								Log.d(TAG, "what? " + jp.getCurrentToken());
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
						this.allRecipes.add(recipe);
						this.allRecipeIndex.put(recipe.name, recipe);
						updater.run();
					}
				}
			}

			Collections.sort(this.allRecipes);
			Log.d(TAG, "recipes count " + this.allRecipes.size());

		} catch (java.io.IOException ex) {
			Log.e(TAG, "Can't parse", ex);
		}
	}


	synchronized void updateSearchResult(String query) {
		this.searchResultRecipes.clear();

		List secondaryList = new LinkedList<Recipe>();

		String normal_query = query.trim().toLowerCase();
		for (Recipe recipe : this.allRecipes) {
			String normal_recipe_name = recipe.name.toLowerCase();
			int damlev = spinneret.util.Levenshtein.damlevlim(normal_query, normal_recipe_name, 2);
			if (damlev == 0) {
				this.searchResultRecipes.add(recipe);
				continue;
			} else if ((normal_recipe_name.length() > 1) && (damlev == 1)) {
				secondaryList.add(recipe);
				continue;
			}

			String normal_recipe_name_fragments[] = normal_recipe_name.split(" +");
			if (normal_recipe_name_fragments.length > 1) {
				for (String normal_recipe_name_fragment : normal_recipe_name_fragments) {
					damlev = spinneret.util.Levenshtein.damlevlim(normal_query, normal_recipe_name_fragment, 2);
					if (damlev == 0) {
						this.searchResultRecipes.add(recipe);
						break;
					} else if ((normal_recipe_name_fragment.length() > 1) && (damlev == 1)) {
						secondaryList.add(recipe);
						break;
					}
				}
			}
		}
		this.searchResultRecipes.addAll(secondaryList);
	}

	synchronized void updateProducable(Set<String> pantry) {
		// Maybe this should be new-and-clobber.  Hopefully, single threaded.
		this.producableRecipes.clear();
		this.countRecipesSoleAdditionalIngredient.clear();

		if (pantry.size() == 0) {
			return;
		}

		for (Recipe recipe : this.allRecipes) {
			Set<String> recipeNeeds = new TreeSet<String>(recipe.ingredients);
			recipeNeeds.removeAll(pantry);
			int size = recipeNeeds.size();

			if (size == 0) {
				this.producableRecipes.add(recipe);
			} else if (size == 1) {
				Object remaining = recipeNeeds.toArray()[0];
				List<Recipe> l = (List<Recipe>) countRecipesSoleAdditionalIngredient.get(remaining);
				if (l == null) {
					l = new LinkedList<Recipe>();
					countRecipesSoleAdditionalIngredient.put((String) remaining, l);
				}
				l.add(recipe);
			}
		}
	}

}

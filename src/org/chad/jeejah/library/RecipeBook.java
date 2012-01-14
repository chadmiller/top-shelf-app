package org.chad.jeejah.library;

import android.util.Log;
import android.content.Context;
import android.os.Handler;

import java.util.Map;
import java.util.TreeMap;
import java.util.Hashtable;

import java.util.Set;
import java.util.TreeSet;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import java.util.Collections;

import java.util.Random;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

final class RecipeBook {
	private final static String TAG = "ocjlRB";

	public boolean readyToStore = false;
	public String version;

	final public Map<String,Recipe> allRecipeIndex;
	final public List<Recipe> allRecipes;
	final public List<Recipe> searchedRecipes;
	final public List<Recipe> favoriteRecipes;
	final public List<Recipe> producableRecipes;
	final public Map<String,List<Recipe>> countRecipesSoleAdditionalIngredient;
	final public ArrayList<String> mostUsedIngredients;
	final public Map<String,List<String>> categorizedIngredients;
	final public List<String> ingredients;


	public RecipeBook(List<String> ingredients) {
		this.allRecipeIndex = new Hashtable<String,Recipe>(2420);
		this.allRecipes = new ArrayList<Recipe>(2420);
		this.categorizedIngredients = new TreeMap<String,List<String>>();
		this.mostUsedIngredients = new ArrayList<String>(17);
		this.countRecipesSoleAdditionalIngredient = new Hashtable<String,List<Recipe>>();
		this.producableRecipes = new ArrayList<Recipe>(2420);
		this.searchedRecipes = new ArrayList<Recipe>();
		this.favoriteRecipes = new LinkedList<Recipe>();
		this.ingredients = ingredients;
	}

	public void load(final Context context, final Runnable updater, final Set<String> pantry, final Handler handler, final BookDisplay bookDisplay) {
		final Random rng = new Random();
		final int blockSize = (rng.nextInt(16) * 16) + 16;
		final List<Recipe> block = new ArrayList<Recipe>(blockSize);
		final long startTime = android.os.SystemClock.uptimeMillis();

		try {
			final java.io.InputStream gzfile = context.getResources().openRawResource(R.raw.recipes);
			gzfile.skip(256L);
			final java.util.zip.GZIPInputStream recipeFile = new java.util.zip.GZIPInputStream(gzfile);

			final JsonFactory jsonFactory = new JsonFactory();
			final JsonParser jp = jsonFactory.createJsonParser(recipeFile);
			// vers, publish date, ingred, book, most-used ingred

			jp.nextToken(); // WHAT?
			jp.nextToken(); // START_OBJECT
			this.version = jp.getText(); jp.nextToken(); // version
			Log.d(TAG, "vers is " + this.version);
			Log.d(TAG, "pubdate is " + jp.getText());
			jp.nextToken(); // publish date
			while (jp.nextToken() != JsonToken.END_OBJECT) { // ingred
				final String ingredientName = jp.getText();
				jp.nextToken();
				String genreName = jp.getText();
				if ("m".equals(genreName)) {
					genreName = "mixerandgarnish";
				} else if ("g".equals(genreName)) {
					genreName = "mixerandgarnish";
				} else if (" ".equals(genreName)) {
					genreName = "liquorandliqueur";
				} else {
					Log.e(TAG, "genre id is " + genreName + " but expected m/g/SP.");
				}

				List genreList = this.categorizedIngredients.get(genreName);
				if (genreList == null) {
					genreList = new LinkedList<String>();
					this.categorizedIngredients.put(genreName, genreList);
				}
				genreList.add(ingredientName);
			}
			while (jp.nextToken() != JsonToken.END_ARRAY) { // book
				jp.nextToken();
				final Recipe recipe = new Recipe();
				while (jp.nextToken() != JsonToken.END_OBJECT) {
					final String fieldname = jp.getCurrentName();
					if (fieldname == null) {
						Log.d(TAG, "what? " + jp.getCurrentToken());
					} else if (fieldname.equals("name")) {
						jp.nextToken();
						recipe.name = jp.getText();
					} else if (fieldname.equals("glasses")) {
						jp.nextToken();
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							if (recipe.glass != null) {
								Log.w(TAG, "Glass already set to " + recipe.glass + " and now want " + jp.getText());
							}
							recipe.glass = jp.getText();
						}
					} else if (fieldname.equals("prepare_instructions")) {
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							if (! "[".equals(jp.getText())) {
								recipe.prepare_instructions.add(jp.getText());
							} else {
								Log.d(TAG, "p p_i " + jp.getCurrentToken());
							}
						}
					} else if (fieldname.equals("consume_instructions")) {
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							recipe.consume_instructions.add(jp.getText());
						}
					} else if (fieldname.equals("ingredients")) {
						jp.nextToken();
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							if (! "[".equals(jp.getText())) {
								recipe.ingredients.add(jp.getText());
							} else {
								Log.d(TAG, "p ing " + jp.getCurrentToken());
							}
						}
					} else {
						Log.e(TAG, "  UNKNOWN: " + jp.getCurrentToken());
					}

				}

				this.allRecipeIndex.put(recipe.name, recipe);
				block.add(recipe);
				if (block.size() >= blockSize) {
					RecipeBook.this.allRecipes.addAll(block);
					handler.post(new Runnable() {
						public void run() {
							bookDisplay.recipeAdapter.notifyDataSetChanged();
						}
					});
					block.clear();
				}

				RecipeBook.this.updateSingleProducable(pantry, recipe);
				updater.run();
			}

			handler.post(new Runnable() {
				public void run() {
					RecipeBook.this.allRecipes.addAll(block);
					bookDisplay.recipeAdapter.notifyDataSetChanged();
				}
			});

			jp.nextToken();
			while (jp.nextToken() != JsonToken.END_ARRAY) { // most-used ingred
				this.mostUsedIngredients.add(jp.getText());
			}

			final long endTime = android.os.SystemClock.uptimeMillis();
			Thread t = new Thread(new Runnable() {
				public void run() {
					try { Thread.sleep(2000); } catch (InterruptedException ex) {}
					GoogleAnalyticsTracker tracker;
					tracker = GoogleAnalyticsTracker.getInstance();
					tracker.startNewSession(BookDisplay.GOOG_ANALYTICS_ID, context);
					tracker.trackEvent("Initialize", "DataStruct1", "blocksize " + blockSize, (int) (endTime-startTime));
					tracker.dispatch();
				}
			});
			try { t.setPriority(Thread.MIN_PRIORITY); } catch (Exception ex) {}
			t.start();
			Log.i(TAG, "blocksize " + blockSize + " takes " + (endTime-startTime) + " ms");

		} catch (java.io.IOException ex) {
			Log.e(TAG, "Can't parse", ex);
		}

		//  Should this be kept in categories?
		for (List<String> genreItems : this.categorizedIngredients.values()) {
			ingredients.addAll(genreItems);
		}
		Collections.sort(ingredients);

		this.readyToStore = true;
	}


	synchronized void updateSearchResult(Set<String> ingredientSet) {
		if (ingredientSet.size() == 0) {
			this.searchedRecipes.addAll(this.allRecipes);
			return;
		}

		this.searchedRecipes.clear();

		for (Recipe recipe : this.allRecipes) {
			//if (ingredientSet.containsAll(recipe.ingredients)) {
			if (recipe.ingredients.containsAll(ingredientSet)) {
				this.searchedRecipes.add(recipe);
			}
		}
	}

	synchronized void updateSearchResult(String query) {
		this.searchedRecipes.clear();

		if (query == null) {
			return;
		}

		final List<Recipe> secondaryList = new LinkedList<Recipe>();
		final List<Recipe> tertiaryList = new LinkedList<Recipe>();
		final List<Recipe> quaternaryList = new LinkedList<Recipe>();

		final String normal_query = query.trim().toLowerCase();
		final String soundex_query = Soundex.soundex(normal_query);
		for (Recipe recipe : this.allRecipes) {
			final String normal_recipe_name = recipe.name.toLowerCase();
			int damlev = spinneret.util.Levenshtein.damlevlim(normal_query, normal_recipe_name, 2);
			if (damlev == 0) {
				this.searchedRecipes.add(recipe);
				continue;
			} else if ((normal_recipe_name.length() > 1) && (damlev == 1)) {
				secondaryList.add(recipe);
				continue;
			}

			if (soundex_query.equals(Soundex.soundex(normal_recipe_name))) {
				tertiaryList.add(recipe);
			}

			final String normal_recipe_name_fragments[] = normal_recipe_name.split("\\W+");
			if (normal_recipe_name_fragments.length > 1) {
				for (String normal_recipe_name_fragment : normal_recipe_name_fragments) {
					damlev = spinneret.util.Levenshtein.damlevlim(normal_query, normal_recipe_name_fragment, 2);
					if (damlev == 0) {
						tertiaryList.add(recipe);
						break;
					} else if ((normal_recipe_name_fragment.length() > 1) && (damlev == 1)) {
						quaternaryList.add(recipe);
						break;
					}
				}
			}

		}
		this.searchedRecipes.addAll(secondaryList);
		this.searchedRecipes.addAll(tertiaryList);
		this.searchedRecipes.addAll(quaternaryList);
	}


	private synchronized void updateSingleProducable(Set<String> pantry, Recipe recipe) {
		final Set<String> recipeNeeds = new TreeSet<String>(recipe.ingredients);  // FIXME
		recipeNeeds.removeAll(pantry);
		final int size = recipeNeeds.size();

		if (size == 0) {
			this.producableRecipes.add(recipe);
		} else if (size == 1) {
			final Object remaining = recipeNeeds.toArray()[0];
			List<Recipe> l = countRecipesSoleAdditionalIngredient.get(remaining);
			if (l == null) {
				l = new LinkedList<Recipe>();
				countRecipesSoleAdditionalIngredient.put((String) remaining, l);
			}
			l.add(recipe);
		}
	}

	synchronized void updateProducable(Set<String> pantry) {
		this.producableRecipes.clear();
		this.countRecipesSoleAdditionalIngredient.clear();

		if (pantry.size() == 0) {
			return;
		}

		for (Recipe recipe : this.allRecipes) {
			if (recipe.ingredients.size() <= 1) { continue; }
			updateSingleProducable(pantry, recipe);
		}
	}

	synchronized void addFavorite(String recipeName) {
		final Recipe r = allRecipeIndex.get(recipeName);
		if (r != null) {
			favoriteRecipes.add(r);
		} else {
			Log.d(TAG, "Didn't find favorite " + recipeName + " to add.");
		}
		Collections.sort(favoriteRecipes);
	}

	synchronized void removeFavorite(String recipeName) {
		final Recipe r = allRecipeIndex.get(recipeName);
		if (r != null) {
			favoriteRecipes.remove(r);
		} else {
			Log.d(TAG, "Didn't find favorite" + recipeName + " to remove.");
		}
	}

	synchronized void updateFavorites(Iterable<String> recipeNames) {
		favoriteRecipes.clear();
		for (String recipeName : recipeNames) {
			addFavorite(recipeName);
		}
	}

}

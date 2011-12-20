package org.chad.jeejah.library;

import android.widget.TextView;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import android.content.Context;
import android.util.Log;
import android.content.SharedPreferences;

import java.util.Iterator;
import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.Map;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

class RecipesListAdapter extends android.widget.BaseAdapter implements SharedPreferences.OnSharedPreferenceChangeListener {
	private final static String TAG = "org.chad.jeejah.library.RecipesListAdapter";
	private GoogleAnalyticsTracker tracker;

	public static class ViewHolder {
		public TextView name;
		public TextView ingredients;
		public ImageView favorited;
	}

	final private RecipeBook recipeBook;
	final private LayoutInflater inflater;

	private String searchQuery;
	private Set<String> pantry;
	public List targetRecipeList;

	public RecipesListAdapter(Context context, RecipeBook recipeBook, Set<String> pantry) {
		super();
		this.recipeBook = recipeBook;
		this.pantry = pantry;
		this.tracker = GoogleAnalyticsTracker.getInstance();
		this.tracker.startNewSession(BookDisplay.GOOG_ANALYTICS_ID, 60, context);

		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		if (this.targetRecipeList == null)
			return 0;
		return this.targetRecipeList.size();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = this.inflater.inflate(R.layout.recipe_list_item, parent, false);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.recipe_name);
			holder.ingredients = (TextView) convertView.findViewById(R.id.recipe_ingredients_list);
			holder.favorited = (ImageView) convertView.findViewById(R.id.favorited);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Recipe recipe = (Recipe) this.targetRecipeList.get(position);

		boolean bad = false;
		Iterator<String> iter = recipe.ingredients.iterator();

		String s = iter.next();
		if (! this.pantry.contains(s)) {
			bad = true;
		}
		while (iter.hasNext()) {
			s = iter.next();
			if (this.pantry.contains(s)) {
			} else {
				bad = true;
			}
		}

		if (recipeBook.favoriteRecipes.contains(recipe)) {
			holder.favorited.setVisibility(View.VISIBLE);
		} else {
			holder.favorited.setVisibility(View.GONE);
		}
		holder.ingredients.setText(recipe.ingredients.toString());
		if (bad) {
			holder.name.setText(recipe.name + "*");
		} else {
			holder.name.setText(recipe.name);
		}

		return convertView;
	}

	public int getViewTypeCount() { return 1; }

	public int getItemViewType(int position) { return 1; }

	public boolean hasStableIds() { return true; }

	public long getItemId(int position) {
		return this.targetRecipeList.get(position).hashCode();
	}

	public Recipe getItem(int position) {
		return (Recipe) this.targetRecipeList.get(position);
	}

	public boolean isEmpty() { return (this.targetRecipeList == null) || (this.targetRecipeList.size() == 0); }

	public void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String key) {
		if (key.startsWith(RecipeActivity.PREF_PREFIX_FAVORITED)) {
			final String recipeName = key.substring(RecipeActivity.PREF_PREFIX_FAVORITED.length());
			final boolean isFavorited = sharedPreferences.getBoolean(key, false);
			if (isFavorited) {
				this.recipeBook.addFavorite(recipeName);
				this.tracker.trackEvent("Clicks", "Favorited", recipeName, 1);
			} else {
				this.recipeBook.removeFavorite(recipeName);
			}
			// favorites list gets new items.  Other views merely get stars updated.
			this.notifyDataSetChanged();
		}
	}

	public void setFavoritesFromPreferences(Map<String,?> prefs) {
		for (Map.Entry<String,?> entry : prefs.entrySet()) {
			final String key = entry.getKey();
			if (key.startsWith(RecipeActivity.PREF_PREFIX_FAVORITED)) {
				final String recipeName = key.substring(RecipeActivity.PREF_PREFIX_FAVORITED.length());
				final Boolean isFavorited = (Boolean) entry.getValue();
				if (isFavorited) {
					this.recipeBook.addFavorite(recipeName);
				} else {
					this.recipeBook.removeFavorite(recipeName);
				}
			}
		}
	}

	public String search(String searchQuery) {
		this.searchQuery = searchQuery;
		this.targetRecipeList = this.recipeBook.searchedRecipes;

		this.recipeBook.updateSearchResult(searchQuery);

		this.notifyDataSetChanged();
		return "\u201C" + this.searchQuery + "\u201D";
	}

	public String nextFilterState(Context context, android.widget.TextView footnote) {
		// Is run in background thread only.
		/* Filtered, all, favorites, suggested drinks, [search.] */

		try {
			if (this.targetRecipeList == null) {
				if (recipeBook.producableRecipes.size() == 0) {
					this.targetRecipeList = recipeBook.allRecipes;
					android.widget.Toast.makeText(context, R.string.using_unfiltered_bc_nothing_here, android.widget.Toast.LENGTH_LONG).show();
					footnote.setText(R.string.a_recipe_not_available);
					footnote.setVisibility(View.VISIBLE);
					return "(all)";
				} else {
					this.targetRecipeList = recipeBook.producableRecipes;
					if (recipeBook.producableRecipes.size() < 15) {
						footnote.setText(R.string.there_are_too_few);
						footnote.setVisibility(View.VISIBLE);
					} else {
						footnote.setVisibility(View.GONE);
					}
					return "(filtered)";
				}
			} else if (this.targetRecipeList == recipeBook.producableRecipes) {
				this.targetRecipeList = recipeBook.allRecipes;
				footnote.setText(R.string.a_recipe_not_available);
				footnote.setVisibility(View.VISIBLE);
				return "(all)";
			} else if (this.targetRecipeList == recipeBook.allRecipes) {
				this.targetRecipeList = recipeBook.favoriteRecipes;
				footnote.setVisibility(View.GONE);
				return "(favorites)";
			} else if ((this.targetRecipeList == recipeBook.favoriteRecipes) && (this.searchQuery != null)) {
				this.targetRecipeList = recipeBook.searchedRecipes;
				footnote.setVisibility(View.GONE);
				return "\u201C" + this.searchQuery + "\u201D";
			} else {
				this.targetRecipeList = recipeBook.producableRecipes;
				footnote.setVisibility(View.GONE);
				return "(filtered)";
			}
		} finally {
			this.notifyDataSetChanged();
		}
	}
			// this.recipeListFootnote.setText(R.string.a_recipe_not_available);

	public void updatePantry(Set<String> pantry) {
		this.pantry = pantry;
		this.notifyDataSetChanged();
	}


}

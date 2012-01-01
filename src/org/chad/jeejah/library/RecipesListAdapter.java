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
	private final static String TAG = "ocjlRLA";
	private Context context;

	private final int STATE_ALL = 0;
	private final int STATE_PRODUCABLE = 1;
	private final int STATE_FAVORITES = 2;
	private final int STATE_SEARCHES = 3;
	private final int STATECOUNT = 4;

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
		this.targetRecipeList = recipeBook.allRecipes;
		this.context = context;

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
		return position;
		//return this.targetRecipeList.get(position).hashCode();
	}

	public Recipe getItem(int position) {
		return (Recipe) this.targetRecipeList.get(position);
	}

	public boolean isEmpty() { return (this.targetRecipeList == null) || (this.targetRecipeList.size() == 0); }

	public void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String recipeName) {
		final boolean isFavorited = sharedPreferences.getBoolean(recipeName, false);
		if (isFavorited) {
			this.recipeBook.addFavorite(recipeName);
			final GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
			tracker.startNewSession(BookDisplay.GOOG_ANALYTICS_ID, 60, context);
			tracker.trackEvent("Clicks", "Favorited", recipeName, 1);
			tracker.dispatch();
		} else {
			this.recipeBook.removeFavorite(recipeName);
		}
		// favorites list gets new items.  Other views merely get stars updated.
		this.notifyDataSetChanged();
	}

	public void setFavoritesFromPreferences(Map<String,?> prefs) {
		for (Map.Entry<String,?> entry : prefs.entrySet()) {
			final String recipeName = entry.getKey();
			final Boolean isFavorited = (Boolean) entry.getValue();
			if (isFavorited) {
				this.recipeBook.addFavorite(recipeName);
			} else {
				this.recipeBook.removeFavorite(recipeName);
			}
		}
	}

	public String setBestInitialState(Context context, android.widget.TextView footnote) {
		if (recipeBook.producableRecipes.size() > 0) {
			return setFilterViewId(STATE_PRODUCABLE, context, footnote);
		} else {
			return setFilterViewId(STATE_ALL, context, footnote);
		}
	}

	public int getFilterViewId() {
		if (this.targetRecipeList == null) {
			Log.w(TAG, "target recipe list is null. ");
			return -2;
		} else if (this.targetRecipeList == recipeBook.allRecipes) {
			return STATE_ALL;
		} else if (this.targetRecipeList == recipeBook.producableRecipes) {
			return STATE_PRODUCABLE;
		} else if (this.targetRecipeList == recipeBook.favoriteRecipes) {
			return STATE_FAVORITES;
		} else if (this.targetRecipeList == recipeBook.searchedRecipes) {
			return STATE_SEARCHES;
		} else {
			Log.e(TAG, "target recipe list is unknown. " + this.targetRecipeList);
			return STATECOUNT;
		}
	}

	public String setFilterViewId(int state, Context context, android.widget.TextView footnote) {
		final int size;
		switch (state) {
			case STATE_ALL:
				this.targetRecipeList = recipeBook.allRecipes;
				footnote.setText(R.string.a_recipe_not_available);
				footnote.setVisibility(View.VISIBLE);
				break;
			case STATE_PRODUCABLE:
				this.targetRecipeList = recipeBook.producableRecipes;
				size = this.targetRecipeList.size();
				if (size == 0) {
					footnote.setText(R.string.there_are_none);
					footnote.setVisibility(View.VISIBLE);
				} else if (size < 10) {
					footnote.setText(R.string.there_are_too_few);
					footnote.setVisibility(View.VISIBLE);
				} else {
					footnote.setVisibility(View.GONE);
				}
				break;
			case STATE_FAVORITES:
				this.targetRecipeList = recipeBook.favoriteRecipes;
				size = this.targetRecipeList.size();
				if (size != 0) {
					footnote.setText(R.string.a_recipe_not_available);
					footnote.setVisibility(View.VISIBLE);
				} else {
					footnote.setVisibility(View.GONE);
				}
				break;
			case STATE_SEARCHES:
				this.targetRecipeList = recipeBook.searchedRecipes;
				size = this.targetRecipeList.size();
				if (size != 0) {
					footnote.setText(R.string.a_recipe_not_available);
					footnote.setVisibility(View.VISIBLE);
				} else {
					footnote.setVisibility(View.GONE);
				}
				break;
			default:
				if (recipeBook.producableRecipes.size() == 0) {
					this.targetRecipeList = recipeBook.allRecipes;
					android.widget.Toast.makeText(context, R.string.using_unfiltered_bc_nothing_here, android.widget.Toast.LENGTH_LONG).show();
					footnote.setText(R.string.a_recipe_not_available);
					footnote.setVisibility(View.VISIBLE);
					state = 1;
				} else {
					this.targetRecipeList = recipeBook.producableRecipes;
					if (recipeBook.producableRecipes.size() < 15) {
						footnote.setText(R.string.there_are_too_few);
						footnote.setVisibility(View.VISIBLE);
					} else {
						footnote.setVisibility(View.GONE);
					}
					state = 0;
				}
		}
		Log.d(TAG, "new state has " + this.targetRecipeList.size() + " items in it.");
		return getFilterViewName(state);
	}

	public String getFilterViewName(int state) {
		switch (state) {
			case STATE_ALL:
				return "(all)";
			case STATE_PRODUCABLE:
				return "(filtered)";
			case STATE_FAVORITES:
				return "(favorites)";
			case STATE_SEARCHES:
				return "\u201C" + this.searchQuery + "\u201D";
		}
		return "-";
	}


	public String nextFilterState(Context context, android.widget.TextView footnote) {
		/* Filtered, all, favorites, suggested drinks, [search.] */
		int state = getFilterViewId();
		Log.d(TAG, "switching! currently at filter state " + state);
		try {
			if (this.searchQuery != null) {
				state = (state + 1) % STATECOUNT;
			} else {
				state = (state + 1) % (STATECOUNT-1);
			}
			Log.d(TAG, "switching! going to filter state " + state);
			return setFilterViewId(state, context, footnote);
		} finally {
			this.notifyDataSetChanged();
		}
	}

	public void updatePantry(Set<String> pantry) {
		this.pantry = pantry;
		this.notifyDataSetChanged();
	}

	public String getSearchQuery() {
		return this.searchQuery;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
		this.targetRecipeList = this.recipeBook.searchedRecipes;
		this.recipeBook.updateSearchResult(searchQuery);
	}

	public String search(String searchQuery) {
		this.setSearchQuery(searchQuery);
		this.notifyDataSetChanged();
		return "\u201C" + this.searchQuery + "\u201D";
	}


}

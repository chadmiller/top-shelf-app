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
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.TreeSet;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

class RecipeAdapter extends android.widget.BaseAdapter implements SharedPreferences.OnSharedPreferenceChangeListener {
	private final static String TAG = "org.chad.jeejah.library.RecipeAdapter";
	private GoogleAnalyticsTracker tracker;

	public static class ViewHolder {
		public TextView name;
		public TextView ingredients;
		public ImageView photo;
		public ImageView favorited;
	}

	private Set<String> favorites;

	private Set<String> pantry;
	private RecipeBook recipeBook;
	private boolean useProducableOnly;
	private LayoutInflater inflater;

	public List targetRecipeList;
	public List pushedStateTargetRecipeList;

	public void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String key) {
		if (key.startsWith(RecipeActivity.PREF_PREFIX_FAVORITED)) {
			String recipeName = key.substring(RecipeActivity.PREF_PREFIX_FAVORITED.length());
			boolean isFavorited = sharedPreferences.getBoolean(key, false);
			if (isFavorited) {
				favorites.add(recipeName);
				this.tracker.trackEvent("Clicks", "Favorited", recipeName, 1);
			} else {
				favorites.remove(recipeName);
			}
			this.notifyDataSetChanged();
		}
	}

	public void setFavoritesFromPreferences(Map<String,?> prefs) {
		for (Map.Entry<String,?> entry : prefs.entrySet()) {
			String key = (String) entry.getKey();
			if (key.startsWith(RecipeActivity.PREF_PREFIX_FAVORITED)) {
				String recipeName = key.substring(RecipeActivity.PREF_PREFIX_FAVORITED.length());
				Boolean isFavorited = (Boolean) entry.getValue();
				if (isFavorited) {
					favorites.add(recipeName);
				} else {
					favorites.remove(recipeName);
				}
			}
		}
	}

	public String getDescription() {
		if (useProducableOnly) {
			return "filtered";
		} else {
			return "all";
		}
	}

	public boolean isFiltered() {
		return useProducableOnly;
	}

	public void search(boolean starting) {
		if (starting) {
			this.pushedStateTargetRecipeList = this.targetRecipeList;
			this.targetRecipeList = this.recipeBook.producableRecipes;
		} else {
			if (this.pushedStateTargetRecipeList != null) {
				this.targetRecipeList = this.pushedStateTargetRecipeList;
			} else {
				this.targetRecipeList = recipeBook.allRecipes;
			}
		}
	}

	public void toggleVisibility() {
		this.useProducableOnly = !this.useProducableOnly;
		if (useProducableOnly) {
			this.targetRecipeList = recipeBook.producableRecipes;
		} else {
			this.targetRecipeList = recipeBook.allRecipes;
		}
		this.notifyDataSetChanged();
		Log.i(TAG, "producable only set to " + this.useProducableOnly);
	}

	public void updatePantry(Set<String> pantry) {
		this.pantry = pantry;
		this.notifyDataSetChanged();
	}

	

	public RecipeAdapter(Context context, RecipeBook recipeBook, Set<String> pantry, boolean useProducableOnly) {
		super();
		this.recipeBook = recipeBook;
		this.useProducableOnly = useProducableOnly;
		this.pantry = pantry;
		this.tracker = GoogleAnalyticsTracker.getInstance();
		this.tracker.startNewSession(BookDisplay.GOOG_ANALYTICS_ID, 60, context);

		this.favorites = new TreeSet<String>();

		if (useProducableOnly) {
			this.targetRecipeList = recipeBook.producableRecipes;
		} else {
			this.targetRecipeList = recipeBook.allRecipes;
		}
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return this.targetRecipeList.size();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.recipe_list_item, parent, false);
			holder = new ViewHolder();
			holder.photo = (ImageView) convertView.findViewById(R.id.recipe_image);
			holder.name = (TextView) convertView.findViewById(R.id.recipe_name);
			holder.ingredients = (TextView) convertView.findViewById(R.id.recipe_ingredients_list);
			holder.favorited = (ImageView) convertView.findViewById(R.id.favorited);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Recipe recipe = (Recipe) this.targetRecipeList.get(position);

		boolean bad = false;
		Iterator<String> iter = recipe.ingredients.iterator();
		//StringBuffer buffer = new StringBuffer();
		if (this.useProducableOnly) {
		//	// Display all the same way
		//	buffer.append(iter.next());
		//	while (iter.hasNext()) {
		//		buffer.append(", ").append(iter.next());
		//	}
		} else {
			String s = iter.next();
			if (this.pantry.contains(s)) {
		//		buffer.append(s);
			} else {
				bad = true;
		//		buffer.append("(").append(s).append(")");
			}
			while (iter.hasNext()) {
				s = iter.next();
		//		buffer.append(", ");
				if (this.pantry.contains(s)) {
		//			buffer.append(s);
				} else {
					bad = true;
		//			buffer.append("(").append(s).append(")");
				}
			}
		}
		if (favorites.contains(recipe.name)) {
			holder.favorited.setVisibility(View.VISIBLE);
		} else {
			holder.favorited.setVisibility(View.GONE);
		}
		holder.ingredients.setText(recipe.ingredients.toString());
		//holder.ingredients.setText(buffer.toString());
		//holder.photo.setImageBitmap();  // FIXME glass type
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

}



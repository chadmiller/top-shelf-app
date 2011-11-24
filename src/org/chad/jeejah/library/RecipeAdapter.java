package org.chad.jeejah.library;

import android.widget.TextView;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import android.content.Context;
import android.util.Log;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

class RecipeAdapter extends android.widget.BaseAdapter {
	public final static String TAG = "org.chad.jeejah.library.RecipeAdapter";

	public static class ViewHolder {
		public TextView name;
		public TextView ingredients;
		public ImageView photo;
		public TextView ratings;
	}

	private Set<String> pantry;
	private RecipeBook recipeBook;
	private boolean useProducableOnly;
	private LayoutInflater inflater;

	public List targetRecipeList;

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
			holder.ratings = (TextView) convertView.findViewById(R.id.recipe_ratings);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Recipe recipe = (Recipe) this.targetRecipeList.get(position);

		boolean bad = false;
		Iterator<String> iter = recipe.ingredients.iterator();
		StringBuffer buffer = new StringBuffer();
		if (this.useProducableOnly) {
			// Display all the same way
			buffer.append(iter.next());
			while (iter.hasNext()) {
				buffer.append(", ").append(iter.next());
			}
		} else {
			String s = iter.next();
			if (this.pantry.contains(s)) {
				buffer.append(s);
			} else {
				bad = true;
				buffer.append("(").append(s).append(")");
			}
			while (iter.hasNext()) {
				s = iter.next();
				buffer.append(", ");
				if (this.pantry.contains(s)) {
					buffer.append(s);
				} else {
					bad = true;
					buffer.append("(").append(s).append(")");
				}
			}
		}
		holder.ingredients.setText(buffer.toString());
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



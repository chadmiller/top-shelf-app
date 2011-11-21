package org.chad.jeejah.library;

import android.widget.TextView;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import android.content.Context;

import java.util.Iterator;

class RecipeAdapter extends android.widget.BaseAdapter {

	public static class ViewHolder {
		public TextView name;
		public TextView ingredients;
		public ImageView photo;
		public TextView ratings;
	}

	private Recipe[] recipeList;
	private LayoutInflater inflater;

	public RecipeAdapter(Context context, Recipe[] list) {
		super();
		this.recipeList = list;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return recipeList.length;
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

		holder.name.setText(this.recipeList[position].name);
		//holder.photo.setImageBitmap();  // FIXME glass type

		Iterator<String> iter = this.recipeList[position].ingredients.iterator();
		StringBuffer buffer = new StringBuffer(iter.next());
		while (iter.hasNext()) buffer.append(", ").append(iter.next());
		holder.ingredients.setText(buffer.toString());

		return convertView;
	}

	public int getViewTypeCount() { return 1; }

	public int getItemViewType(int position) { return 1; }

	public boolean hasStableIds() { return false; }

	public long getItemId(int position) {
		//return this.recipeList[position].name;
		return 1L;
	}

	public Recipe getItem(int position) {
		return this.recipeList[position];
	}

	public boolean isEmpty() { return (this.recipeList == null) || (this.recipeList.length == 0); }

	//public void registerDataSetObserver(DataSetObserver observer) { }

}



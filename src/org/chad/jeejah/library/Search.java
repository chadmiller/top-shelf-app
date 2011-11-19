package org.chad.jeejah.library;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Search extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TextView drinksListHeader = (TextView) findViewById(R.id.drinks_list_header);

		RecipeBook recipeBook = new RecipeBook(this);

		drinksListHeader.setText(String.format("You can make %d drinks with your %d ingredients.", 0, 0));

	}
}

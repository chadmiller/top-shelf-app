package org.chad.jeejah.library;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.content.Intent;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Menu;
import android.view.Window;
import android.view.MenuItem;
import android.util.Log;
import android.text.Html;

import java.util.TreeSet;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class RecipeActivity extends Activity {
	private final static String TAG = "ocjlRA";
	private GoogleAnalyticsTracker tracker;

	public static final String FAVORITE_FILENAME = "favorites";
	private boolean isFavorited = true;
	private StringBuilder shareDocument;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.recipe);

		shareDocument = new StringBuilder();
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setOnTitleClickListener(new View.OnClickListener() {
				public void onClick(View v) { RecipeActivity.this.finish(); } });

		this.tracker = GoogleAnalyticsTracker.getInstance();
		this.tracker.startNewSession(BookDisplay.GOOG_ANALYTICS_ID, 60, this);
		this.tracker.trackPageView("/" + TAG);

		final Resources res = getResources();
		final Intent intent = getIntent();

		final Bundle recipeInfo = intent.getBundleExtra("recipe");
		final TextView titleView = (TextView) findViewById(R.id.recipe_title);

		final String title = recipeInfo.getString(Recipe.KEY_NAME);
		titleView.setText(title);
		titleView.setTextSize(res.getDimension(R.dimen.recipe_title_height));
		actionBar.setTitle("Recipe \u201C" + title + "\u201D");
		shareDocument.append("<h1>" + title + "<a href=\"https://market.android.com/details?id=" + getPackageName() + "\">*</a></h1>");

		final LinearLayout ingredientsContainer = (LinearLayout) findViewById(R.id.recipe_ingredients);
		final LinearLayout preparationContainer = (LinearLayout) findViewById(R.id.recipe_preparation);
		final LinearLayout consumationContainer = (LinearLayout) findViewById(R.id.recipe_consumation);

		final String[] ingredients = recipeInfo.getStringArray(Recipe.KEY_INGREDIENTS);
		final String[] preparation = recipeInfo.getStringArray(Recipe.KEY_PREPARE_INST);
		final String[] consumation = recipeInfo.getStringArray(Recipe.KEY_CONSUME_INST);
		final String glass = recipeInfo.getString(Recipe.KEY_GLASS);

		final TreeSet<String> jargonSet = new TreeSet<String>();

		if (glass != null) {
			final TextView t = new TextView(this);
			t.setText("\u2022   " + glass);
			t.setTextSize(res.getDimension(R.dimen.recipe_line_height));
			t.setPadding(30, 3, 30, 2);
			ingredientsContainer.addView(t);
		}

		for (int i = 0; i < ingredients.length; i++) {
			final TextView t = new TextView(this);
			t.setText("\u2022   " + ingredients[i]);
			t.setTextSize(res.getDimension(R.dimen.recipe_line_height));
			t.setPadding(30, 3, 30, 2);
			ingredientsContainer.addView(t);
		}

		for (int i = 0; i < preparation.length; i++) {
			final TextView t = new TextView(this);
			shareDocument.append("<p>" + preparation[i] + "</p>");
			t.setText("" + (i+1) + ".  " + preparation[i]);
			t.setTextSize(res.getDimension(R.dimen.recipe_line_height));
			t.setPadding(30, 5, 30, 5);
			preparationContainer.addView(t);

			if (preparation[i].matches(".*?\\bblend\\b.*"))
				jargonSet.add(res.getString(R.string.term_blend));
			if (preparation[i].matches(".*?\\bbuild\\b.*"))
				jargonSet.add(res.getString(R.string.term_build));
			if (preparation[i].matches(".*?\\bdash\\b.*"))
				jargonSet.add(res.getString(R.string.term_dash));
			if (preparation[i].matches(".*?\\bfill\\b.*"))
				jargonSet.add(res.getString(R.string.term_fill));
			if (preparation[i].matches(".*?\\bfloat\\b.*"))
				jargonSet.add(res.getString(R.string.term_float));
			if (preparation[i].matches(".*?\\blayer\\b.*"))
				jargonSet.add(res.getString(R.string.term_layer));
			if (preparation[i].matches(".*?\\bmuddle\\b.*"))
				jargonSet.add(res.getString(R.string.term_muddle));
			if (preparation[i].matches(".*?\\brim\\b.*"))
				jargonSet.add(res.getString(R.string.term_rim));
			if (preparation[i].endsWith("blend and strain"))
				jargonSet.add(res.getString(R.string.term_strain));
			if (preparation[i].endsWith("blend and pour"))
				jargonSet.add(res.getString(R.string.term_pour));
			if (preparation[i].endsWith("stir and strain"))
				jargonSet.add(res.getString(R.string.term_strain));
			if (preparation[i].endsWith("shake and strain")) {
				jargonSet.add(res.getString(R.string.term_shake));
				jargonSet.add(res.getString(R.string.term_strain));
			}
			if (preparation[i].endsWith("shake and pour")) {
				jargonSet.add(res.getString(R.string.term_shake));
				jargonSet.add(res.getString(R.string.term_pour));
			}
			if (preparation[i].matches(".*?\\btop\\b.*"))
				jargonSet.add(res.getString(R.string.term_top));
		}

		for (int i = 0; i < consumation.length; i++) {
			final TextView t = new TextView(this);
			t.setText("Consume instruction: " + consumation[i]);
			t.setTextSize(res.getDimension(R.dimen.recipe_line_height));
			t.setPadding(30, 5, 30, 5);
			consumationContainer.addView(t);
		}

		if (jargonSet.size() > 0) {
			final StringBuilder s = new StringBuilder();
			for (String i : jargonSet) {
				s.append(i).append("\n\n");
			}
			final TextView jargon = (TextView) findViewById(R.id.jargon_defined);
			jargon.setText(s.toString());
			jargon.setTextSize(res.getDimension(R.dimen.recipe_jargon_height));
		} else {
			final View v = findViewById(R.id.jargon_seperator);
			v.setVisibility(View.GONE);
		}

		final SharedPreferences favoritesSharedPreferences = getSharedPreferences(FAVORITE_FILENAME, MODE_PRIVATE);
		final ImageButton favButton = (ImageButton) findViewById(R.id.recipe_favorited_icon);
		this.isFavorited = favoritesSharedPreferences.getBoolean(title, false);
		setFavoriteButtonEnabled(favButton, this.isFavorited);
		favButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				RecipeActivity.this.isFavorited = ! RecipeActivity.this.isFavorited;
				final SharedPreferences.Editor e = favoritesSharedPreferences.edit();
				e.putBoolean(title, RecipeActivity.this.isFavorited);
				e.commit();

				RecipeActivity.this.setFavoriteButtonEnabled((ImageButton) v, RecipeActivity.this.isFavorited);
			}
		});


		class ShareAction implements Action {
			@Override
			public int getDrawable() {
				return android.R.drawable.ic_menu_send;
			}
			@Override
			public void performAction(View view) {
				RecipeActivity.this.share();
			}
		}
		actionBar.addAction(new ShareAction());

		titleView.setKeepScreenOn(true);
	}

	private void setFavoriteButtonEnabled(ImageButton ib, boolean newState) {
		if (newState) {
			ib.setImageResource(android.R.drawable.btn_star_big_on);
		} else {
			ib.setImageResource(android.R.drawable.btn_star_big_off);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, R.id.instructions, 1, R.string.help).setIcon(android.R.drawable.ic_menu_help);
		//menu.add(Menu.NONE, R.id.feedback, 4, R.string.feedback);
		menu.add(Menu.NONE, R.id.share, 5, R.string.share).setIcon(android.R.drawable.ic_menu_send);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent intent;
		switch (item.getItemId()) {
			case R.id.instructions:
				intent = new Intent(this, Instructions.class);
				startActivity(intent);
				return true;
			case R.id.feedback:
				intent = new Intent(this, Feedback.class);
				intent.putExtra("source", TAG);
				startActivity(intent);
				return true;
			case R.id.share:
				share();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void share() {
		final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/html");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(shareDocument.toString()));
		startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
	}

}

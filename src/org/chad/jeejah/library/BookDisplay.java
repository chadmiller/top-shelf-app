package org.chad.jeejah.library;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.Window;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Handler;
import android.os.AsyncTask;

import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class BookDisplay extends Activity {
	private final static String TAG = "org.chad.jeejah.library.BookDisplay";

	public static final String GOOG_ANALYTICS_ID = "U" + "A-" + 5168704 + "-3";
	private static final String DATA_VERSION_DNS_RECORD_NAME = "ver.data.library.jeejah.chad.org.";
	private SharedPreferences sp;

	private RecipeBook recipeBook;

	private RecipesListAdapter recipeAdapter;
	private TextView recipeListFootnote;
	private Set<String> pantry;

	private GoogleAnalyticsTracker tracker;
	private ActionBar actionBar;
	private Dialog splashDialog;


	private void handleIntent(Intent intent) {
		if (intent == null) {
			Log.d(TAG, "NO INTENT");
			return;
		}

		Bundle extras = intent.getExtras();
		if (extras != null) {
			Log.d(TAG, "Handling intent with data: " + extras.toString());
		} else {
			Log.d(TAG, "Handling intent with no data");
		}

		// Handle search
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(android.app.SearchManager.QUERY);
			Log.d(TAG, "Search for " + query + " now!");
			recipeBook.updateSearchResult(query);
			this.actionBar.setTitle("Drinks  \u201C" + query + "\u201D");
			this.recipeAdapter.search(true);
			this.recipeListFootnote.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private class RecipeBookLoadTask extends AsyncTask<RecipeBook, Integer, Integer> {
		private TextView splashScreenText;

		@Override
		protected Integer doInBackground(RecipeBook... recipeBooks) {
			this.splashScreenText = (TextView) BookDisplay.this.splashDialog.findViewById(R.id.splash_screen_text);

			long startTime = android.os.SystemClock.uptimeMillis();
			recipeBooks[0].load(new Runnable() {
				private int n = 0;
				public void run() {
					n++;
					if ((n % 147) == 0) {
						RecipeBookLoadTask.this.publishProgress(n);
					}
				}
			});
			BookDisplay.this.tracker.trackEvent("Performance", "RecipeBookLoading", "Elapsed", (int) (android.os.SystemClock.uptimeMillis() - startTime));

			return new Integer(1);
		}

		@Override
		protected void onProgressUpdate(Integer... progresses) {
			splashScreenText.setText("Loading recipe #" + progresses[0]);
		}

		@Override
		protected void onPostExecute(Integer i) {
			splashScreenText.setText("A moment to process...");
			BookDisplay.this.setUp();
			BookDisplay.this.removeSplashScreen();

			if (BookDisplay.this.recipeAdapter.isFiltered()) {
				if (BookDisplay.this.recipeAdapter.targetRecipeList.size() == 0) {
					BookDisplay.this.toggleFilterState();
					android.widget.Toast.makeText(BookDisplay.this, R.string.using_unfiltered_bc_nothing_here, android.widget.Toast.LENGTH_LONG).show();
				}
			}

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		this.tracker = GoogleAnalyticsTracker.getInstance();
		this.tracker.startNewSession(GOOG_ANALYTICS_ID, 60, this);
		this.tracker.trackPageView("/" + TAG);

		this.pantry = new HashSet<String>();
		this.recipeBook = (RecipeBook) getLastNonConfigurationInstance();

		RecipeBookLoadTask recipeBookLoader = null;
		if (this.recipeBook != null) {
			BookDisplay.this.setUp();
			this.recipeAdapter = new RecipesListAdapter(this, recipeBook, pantry, true);
		} else {
			this.recipeBook = new RecipeBook(this);
			this.recipeAdapter = new RecipesListAdapter(this, recipeBook, pantry, true);
			showSplashScreen();
			recipeBookLoader = new RecipeBookLoadTask();
			recipeBookLoader.execute(this.recipeBook);
		}

		LinearLayout loadingIndicator = (LinearLayout) findViewById(R.id.loading_indicator);
		loadingIndicator.setVisibility(View.GONE);

		this.sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(this.recipeAdapter);
		this.recipeAdapter.setFavoritesFromPreferences(sp.getAll());

		ListView recipeListView = (ListView) findViewById(R.id.recipe_list);
		recipeListView.setAdapter(this.recipeAdapter);
		recipeListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Recipe recipe = (Recipe) parent.getItemAtPosition(position);
				Intent intent = new Intent(BookDisplay.this, RecipeActivity.class);
				intent.setAction(Intent.ACTION_VIEW);
				android.os.Bundle recipe_info = new android.os.Bundle();
				recipe_info.putString(Recipe.KEY_NAME, recipe.name);
				recipe_info.putStringArray(Recipe.KEY_PREPARE_INST, recipe.prepare_instructions.toArray(new String[recipe.prepare_instructions.size()]));
				recipe_info.putStringArray(Recipe.KEY_CONSUME_INST, recipe.consume_instructions.toArray(new String[recipe.consume_instructions.size()]));
				recipe_info.putStringArray(Recipe.KEY_INGREDIENTS, recipe.ingredients.toArray(new String[recipe.ingredients.size()]));
				intent.putExtra("recipe", recipe_info);
				BookDisplay.this.tracker.trackEvent("Clicks", "ListItem", recipe.name, 1);
				BookDisplay.this.startActivity(intent);

				Log.d(TAG, "hshcd = " + BookDisplay.this.getPackageName().hashCode());
				if (BookDisplay.this.getPackageName().hashCode() != -907485584) {
					BookDisplay.this.tracker.trackEvent("X", "X", BookDisplay.this.getPackageName(), 1);
					BookDisplay.this.finish();
				}
			}
		});

		this.actionBar = (ActionBar) findViewById(R.id.actionbar);

		class ListToggleAction implements Action {
			@Override
			public int getDrawable() {
				return R.drawable.ic_btn_toggle_viewable;
			}
			@Override
			public void performAction(View view) {
				BookDisplay.this.toggleFilterState();
				BookDisplay.this.tracker.trackEvent("Clicks", "Action", "Toggle", 1);
			}
		}
		this.actionBar.addAction(new ListToggleAction());

		class PickIngredientsAction implements Action {
			@Override
			public int getDrawable() {
				return R.drawable.ic_btn_mark_owned_ingredients;
			}
			@Override
			public void performAction(View view) {
				BookDisplay.this.startSetIngredients();
				BookDisplay.this.tracker.trackEvent("Clicks", "Action", "MarkIngredients", 1);
			}
		}
		this.actionBar.addAction(new PickIngredientsAction());

		class SuggestShoppingAction implements Action {
			@Override
			public int getDrawable() {
				return R.drawable.ic_btn_suggest_shopping_list;
			}
			@Override
			public void performAction(View view) {
				BookDisplay.this.startShowShoppingList();
				BookDisplay.this.tracker.trackEvent("Clicks", "Action", "Shopping", 1);
			}
		}
		this.actionBar.addAction(new SuggestShoppingAction());

		this.recipeListFootnote = (TextView) findViewById(R.id.recipe_list_footnote);
		this.recipeListFootnote.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				BookDisplay.this.startSetIngredients();
			}
		});

		this.actionBar.setTitle("Drinks  (" + this.recipeAdapter.getDescription() + ")");

		handleIntent(getIntent());

	}

	private String[] toStringsArray(List<Recipe> l) {
		String[] arr = new String[l.size()];
		Iterator it = l.iterator();
		int i = 0;
		while (it.hasNext()) {
			Recipe r = (Recipe) it.next();
			arr[i++] = r.name;
		}
		return arr;
	}

	void startShowShoppingList() {
		Intent intent = new Intent(this, ShoppingListActivity.class);
		intent.setAction(Intent.ACTION_VIEW);

		// Single productive ingredients
		// This can't be computed by recipebook author.
		Bundle singleIngredients = new Bundle();
		singleIngredients.putStringArray("keys", this.recipeBook.countRecipesSoleAdditionalIngredient.keySet().toArray(new String[this.recipeBook.countRecipesSoleAdditionalIngredient.size()]));

		for (Map.Entry<String,List<Recipe>> entry : this.recipeBook.countRecipesSoleAdditionalIngredient.entrySet()) {
			singleIngredients.putStringArray("enabledby " + entry.getKey(), toStringsArray(entry.getValue()));
		}
		intent.putExtra(ShoppingListActivity.SINGLE_KEY, singleIngredients);

		// Most common ingredients
		Bundle mostCommonIngredients = new Bundle();
		// TODO:  Don't include items that are in the pantry.
		mostCommonIngredients.putStringArrayList("ingredients", this.recipeBook.mostUsedIngredients);
		intent.putExtra(ShoppingListActivity.MOSTUSED_KEY, mostCommonIngredients);

		// Favorite recipes require ingredients
		// This can't be computed by recipebook author.
		Bundle missingFavoritesIngredients = new Bundle();
		Set missingIngredients = new TreeSet<String>();
		for (Map.Entry<String,?> entry : sp.getAll().entrySet()) {
			String key = (String) entry.getKey();
			if (key.startsWith(RecipeActivity.PREF_PREFIX_FAVORITED)) {
				String recipeName = key.substring(RecipeActivity.PREF_PREFIX_FAVORITED.length());
				Boolean isFavorited = (Boolean) entry.getValue();
				if (isFavorited) {
					Recipe r = this.recipeBook.allRecipeIndex.get(recipeName);
					missingIngredients.addAll(r.ingredients);
				}
			}
		}
		missingIngredients.removeAll(this.pantry);
		missingFavoritesIngredients.putStringArray("ingredients", (String[]) missingIngredients.toArray(new String[missingIngredients.size()]));
		intent.putExtra(ShoppingListActivity.FAV_KEY, missingFavoritesIngredients);

		this.startActivity(intent);
	}

	void toggleFilterState() {
		this.recipeAdapter.toggleVisibility();
		this.updateFootnote();
		this.actionBar.setTitle("Drinks  (" + this.recipeAdapter.getDescription() + ")");
	}

	void startSetIngredients() {
		Intent intent = new Intent(this, Pantry.class);
		intent.putExtra("ingredients", this.recipeBook.knownIngredients.toArray(new String[this.recipeBook.knownIngredients.size()]));
		this.startActivityForResult(intent, 1);
	}


	void updateFootnote() {
		if (this.recipeAdapter.isFiltered()) {
			if (this.recipeAdapter.targetRecipeList.size() == 0) {
				this.recipeListFootnote.setText(R.string.there_are_none);
				this.recipeListFootnote.setVisibility(View.VISIBLE);
			} else if (this.recipeAdapter.targetRecipeList.size() < 15) {
				this.recipeListFootnote.setText(R.string.there_are_too_few);
				this.recipeListFootnote.setVisibility(View.VISIBLE);
			} else {
				this.recipeListFootnote.setVisibility(View.GONE);
			}
		} else {
			this.recipeListFootnote.setVisibility(View.VISIBLE);
			this.recipeListFootnote.setText(R.string.a_recipe_not_available);
		}
	}


	void setUp() {

		this.pantry.clear();
		Iterator it = sp.getAll().keySet().iterator();
		while (it.hasNext()) {
			Object k = it.next();
			String name = (String) k;
			if (name.startsWith(Pantry.PREF_PREFIX)) {
				if (sp.getBoolean(name, false)) {
					String s = name.substring(Pantry.PREF_PREFIX.length());
					this.pantry.add(s);
					this.tracker.trackEvent("SetUp", "InPantry", s, 1);
				}
			}
		}

		if (! sp.getBoolean("SEEN_INTRO", false)) {
			showDialog(1);
			SharedPreferences.Editor e = sp.edit();
			e.putBoolean("SEEN_INTRO", true);
			e.commit();
			this.tracker.trackEvent("Initialize", "App", "Introduction", 1);
		}

		this.recipeBook.updateProducable(this.pantry);
		this.recipeAdapter.updatePantry(this.pantry);
		this.updateFootnote();
	}


	@Override
	public Dialog onCreateDialog(int id) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(R.string.welcome)
			.setMessage(R.string.welcome_message)
			.setPositiveButton(R.string.begin, new DialogInterface.OnClickListener() { 
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(BookDisplay.this, Pantry.class);
					intent.putExtra("ingredients", BookDisplay.this.recipeBook.knownIngredients.toArray(new String[BookDisplay.this.recipeBook.knownIngredients.size()]));
					BookDisplay.this.startActivityForResult(intent, 1);
				}
			}).setNegativeButton(R.string.help, new DialogInterface.OnClickListener() { 
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(BookDisplay.this, Instructions.class);
					BookDisplay.this.startActivity(intent);
				}
			});
		return adb.create();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		setUp();
	}


	@Override
	public Object onRetainNonConfigurationInstance() {
		return this.recipeBook;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, R.id.instructions, 1, R.string.help);
		menu.add(Menu.NONE, R.id.stock, 2, R.string.set_stock);
		menu.add(Menu.NONE, R.id.shoppingsuggestions, 3, R.string.shopping_suggestions);
		menu.add(Menu.NONE, R.id.feedback, 4, R.string.feedback);
		menu.add(Menu.NONE, R.id.credits, 5, R.string.credits);
		menu.add(Menu.NONE, R.id.togglefilter, 6, R.string.toggle_filter);
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
			case R.id.stock:
				startSetIngredients();
				return true;
			case R.id.shoppingsuggestions:
				startShowShoppingList();
				return true;
			case R.id.togglefilter:
				toggleFilterState();
				return true;
			case R.id.credits:
				intent = new Intent(this, Credits.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.tracker.dispatch();
		this.tracker.stopSession();
	}


	private void removeSplashScreen() {
		if (this.splashDialog != null) {
			this.splashDialog.dismiss();
			this.splashDialog = null;
			Log.d(TAG, "hiding splash screen");
		}
	}
	 
	/**
	 * Shows the splash screen over the full Activity
	 */
	protected void showSplashScreen() {
		Log.d(TAG, "showing splash screen");
		this.splashDialog = new Dialog(this, R.style.SplashScreen);
		this.splashDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.splashDialog.setContentView(R.layout.splashscreen);
		this.splashDialog.setCancelable(false);
		this.splashDialog.show();
	 
		// Set Runnable to remove splash screen just in case
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		  @Override
		  public void run() {
			removeSplashScreen();
		  }
		}, 3000);
	}


}

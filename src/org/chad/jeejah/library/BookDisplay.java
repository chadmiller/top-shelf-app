package org.chad.jeejah.library;

import org.chad.jeejah.library.googmarket.Consts;
import org.chad.jeejah.library.googmarket.Consts.PurchaseState;
import org.chad.jeejah.library.googmarket.Consts.ResponseCode;
import org.chad.jeejah.library.BillingService;
import org.chad.jeejah.library.BillingService.RequestPurchase;
import org.chad.jeejah.library.BillingService.RestoreTransactions;
import org.chad.jeejah.library.PurchaseDatabase;
import org.chad.jeejah.library.PurchaseObserver;

import android.app.Activity;
import android.widget.SimpleCursorAdapter;
import android.database.Cursor;
import android.app.Dialog;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Handler;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

final public class BookDisplay extends Activity {

	private final static String TAG = "ocjlBD";
	public static final String GOOG_ANALYTICS_ID = "U" + "A-" + 5168704 + "-3";
	private static final String DATA_VERSION_DNS_RECORD_NAME = "ver.data.library.jeejah.chad.org.";
	private static final int DIALOG_PURCHASEPLZ = 1;
	private static final int DIALOG_SPLASH = 2;
	private static final int DIALOG_CHOOSE_SEARCH = 3;
	private static final int DIALOG_CLEAR_INGRED_SEARCH = 4;
	private static final int ASK_DONATION_FREQUENCY_WHEN_ZERO = 4;
	private static final String PREF_KEY_STARTUP_AND_DONATE = "INIT";

	private enum Managed { MANAGED, UNMANAGED }

	private boolean hasDonated = false;
	private SharedPreferences pantrySharedPreferences;
	private SharedPreferences favoritesSharedPreferences;
	private SharedPreferences configurationSharedPreferences;

	private RecipeBook recipeBook;

	private DrinksPurchaseObserver mPurchaseObserver;
	private BillingService mBillingService;
	private SimpleCursorAdapter mOwnedItemsAdapter;
	private PurchaseDatabase mPurchaseDatabase;
	private Cursor mOwnedItemsCursor;
	private String mPayloadContents = null;
	private Handler mHandler;
	private CatalogAdapter mCatalogAdapter;
	private LinearLayout ingredientSearchContainer;
	private ListView ingredientSearchListView;

	private ListView recipeListView;
	protected RecipesListAdapter recipeAdapter;
	private TextView recipeListFootnote;
	private Set<String> pantry;
	private Set<String> ingredientSearchRequirements;
	private TextView ingredientSearchNoteHeader;

	private GoogleAnalyticsTracker tracker;
	private ActionBar actionBar;
	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		setContentView(R.layout.book_display);

		mHandler = new Handler();

		mPurchaseDatabase = new PurchaseDatabase(this);

		this.tracker = GoogleAnalyticsTracker.getInstance();
		this.tracker.startNewSession(GOOG_ANALYTICS_ID, 20, this);

		this.handler = new Handler();
		this.pantry = new HashSet<String>();
		this.ingredientSearchRequirements = new LinkedHashSet<String>();
		this.recipeBook = (RecipeBook) getLastNonConfigurationInstance();
		this.configurationSharedPreferences = getSharedPreferences("configuration", MODE_PRIVATE);
		this.pantrySharedPreferences = getSharedPreferences(Pantry.FILENAME, MODE_PRIVATE);
		this.favoritesSharedPreferences = getSharedPreferences(RecipeActivity.FAVORITE_FILENAME, MODE_PRIVATE);

		this.ingredientSearchContainer = (LinearLayout) findViewById(R.id.ingredient_ingredient_search_container);
		this.ingredientSearchListView = (ListView) findViewById(R.id.ingredient_search_picklist);
		this.recipeListView = (ListView) findViewById(R.id.recipe_list);
		this.ingredientSearchNoteHeader = (TextView) findViewById(R.id.ingredient_search_ingr_note);
		this.actionBar = (ActionBar) findViewById(R.id.actionbar);
		this.actionBar.setTitle(getResources().getString(R.string.app_name_title_fmt, "[...]"));

		this.recipeListFootnote = (TextView) findViewById(R.id.recipe_list_footnote);

		RecipeBookLoadTask recipeBookLoader = null;
		if (this.recipeBook != null) {
			Log.i(TAG, "had configuration instance");
			BookDisplay.this.loadPantry();
			this.recipeBook.updateProducable(this.pantry);  // TODO kill
			this.recipeAdapter = new RecipesListAdapter(this, recipeBook, pantry);
			this.recipeAdapter.updatePantry(this.pantry);

			final String filterState = this.recipeAdapter.setBestInitialState(this, this.recipeListFootnote);
			this.actionBar.setTitle(getResources().getString(R.string.app_name_title_fmt, filterState));
			new FavoritesLoadTask().execute();
			final LinearLayout loadingIndicator = (LinearLayout) findViewById(R.id.loading_indicator);
			loadingIndicator.setVisibility(View.GONE);
		} else {
			Log.i(TAG, "starting from scratch");
			this.recipeBook = new RecipeBook();
			this.recipeAdapter = new RecipesListAdapter(this, recipeBook, pantry);
			recipeBookLoader = new RecipeBookLoadTask();
			recipeBookLoader.execute(this.recipeBook);
		}

		recipeListView.setAdapter(BookDisplay.this.recipeAdapter);
		recipeListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					final Recipe recipe = (Recipe) parent.getItemAtPosition(position);
					BookDisplay.this.showRecipe(recipe);
				} catch (IndexOutOfBoundsException ex) {
					Log.e(TAG, "index error on clicked-item access to adapter.  Ignoring.");
				}
			}
		});

		setInstanceState(savedInstanceState);

		final Long now = System.currentTimeMillis();
		final Random rng = new Random();
		final int chance = rng.nextInt(ASK_DONATION_FREQUENCY_WHEN_ZERO);
		final long noDonationUntil = 1325430048000L + (1000*60*60*24*16);
		if (configurationSharedPreferences.getLong(PREF_KEY_STARTUP_AND_DONATE, 0L) == 0) {
			showDialog(DIALOG_SPLASH);
			final SharedPreferences.Editor e = configurationSharedPreferences.edit();
			e.putLong(PREF_KEY_STARTUP_AND_DONATE, now);
			e.commit();
			this.tracker.trackEvent("Initialize", "App", "Introduction", 1);
		} else if ((chance == 0) && ((configurationSharedPreferences.getLong(PREF_KEY_STARTUP_AND_DONATE, 0L) + (1000*60*60*23)) < now) && (now > noDonationUntil)) {

			// If already purchased something, then stop!
			mOwnedItemsCursor = mPurchaseDatabase.queryAllPurchasedItems();
			startManagingCursor(mOwnedItemsCursor);
			if (mOwnedItemsCursor.moveToFirst()) {
				do {
					final String sku = mOwnedItemsCursor.getString(0);
					hasDonated = true;
				} while (mOwnedItemsCursor.moveToNext());
				this.tracker.trackEvent("Initialize", "Donation", "discovered", 1);
			} else {
				mBillingService = new BillingService();
				mBillingService.setContext(this);
				if (mBillingService.checkBillingSupported()) {
					mCatalogAdapter = new CatalogAdapter(this, CATALOG);
					mPurchaseObserver = new DrinksPurchaseObserver(mHandler);
					ResponseHandler.register(mPurchaseObserver);
					showDialog(DIALOG_PURCHASEPLZ);

					final SharedPreferences.Editor e = configurationSharedPreferences.edit();
					e.putLong(PREF_KEY_STARTUP_AND_DONATE, now);
					e.commit();
				} else {
					this.tracker.trackEvent("Initialize", "Donating", "no-mechanism", 1);
				}
			}
//		} else {
//			showDialog(DIALOG_SPLASH);
		}

		new Thread(new ReportingRunnable()).start();
	}

	private void showRecipe(Recipe recipe) {
		final Intent intent = new Intent(BookDisplay.this, RecipeActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		android.os.Bundle recipe_info = new android.os.Bundle();
		recipe_info.putString(Recipe.KEY_NAME, recipe.name);

		final Set<String> ownedIngredients = new TreeSet<String>();
		ownedIngredients.addAll(this.pantry);
		recipe_info.putStringArray(Recipe.KEY_PANTRY, ownedIngredients.toArray(new String[ownedIngredients.size()]));

		recipe_info.putStringArray(Recipe.KEY_PREPARE_INST, recipe.prepare_instructions.toArray(new String[recipe.prepare_instructions.size()]));
		recipe_info.putStringArray(Recipe.KEY_CONSUME_INST, recipe.consume_instructions.toArray(new String[recipe.consume_instructions.size()]));
		recipe_info.putStringArray(Recipe.KEY_INGREDIENTS, recipe.ingredients.toArray(new String[recipe.ingredients.size()]));
		recipe_info.putString(Recipe.KEY_GLASS, recipe.glass);
		intent.putExtra("recipe", recipe_info);
		BookDisplay.this.tracker.trackEvent("Clicks", "ListItem", recipe.name, 1);
		BookDisplay.this.startActivity(intent);

		if (BookDisplay.this.getPackageName().hashCode() != -907485584) {
			BookDisplay.this.tracker.trackEvent("X", "X", BookDisplay.this.getPackageName(), 1);
			BookDisplay.this.finish();
		}
	}

	private String[] toStringsArray(List<Recipe> l) {
		final String[] arr = new String[l.size()];
		int i = 0;
		for (Recipe r : l) {
			arr[i++] = r.name;
		}
		return arr;
	}

	@Override
	public boolean onSearchRequested() {
		showDialog(DIALOG_CHOOSE_SEARCH);
		return false;
	}

	private boolean ingredientSearchNeededUndoing() {
		if (this.ingredientSearchContainer.getVisibility() != View.GONE) {
			this.ingredientSearchContainer.setVisibility(View.GONE);
			return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		if (ingredientSearchNeededUndoing()) {
			return;
		}
		super.onBackPressed();
	}

	void startShowShoppingList() {
		final Intent intent = new Intent(this, ShoppingListActivity.class);
		intent.setAction(Intent.ACTION_VIEW);

		// Single productive ingredients
		// This can't be computed by recipebook author.
		final Bundle singleIngredients = new Bundle();
		singleIngredients.putStringArray("keys", this.recipeBook.countRecipesSoleAdditionalIngredient.keySet().toArray(new String[this.recipeBook.countRecipesSoleAdditionalIngredient.size()]));

		for (Map.Entry<String,List<Recipe>> entry : this.recipeBook.countRecipesSoleAdditionalIngredient.entrySet()) {
			singleIngredients.putStringArray("enabledby " + entry.getKey(), toStringsArray(entry.getValue()));
		}
		intent.putExtra(ShoppingListActivity.SINGLE_KEY, singleIngredients);

		// Most common ingredients
		final Bundle mostCommonIngredients = new Bundle();
		final Set<String> missingIngredients = new TreeSet<String>();
		missingIngredients.addAll(this.recipeBook.mostUsedIngredients);
//		for (String popularItem : this.recipeBook.mostUsedIngredients) {
//			missingIngredients.add(popularItem);
//		}
		missingIngredients.removeAll(this.pantry);
		mostCommonIngredients.putStringArray("ingredients", missingIngredients.toArray(new String[missingIngredients.size()]));
		intent.putExtra(ShoppingListActivity.MOSTUSED_KEY, mostCommonIngredients);

		// Favorite recipes require ingredients
		// This can't be computed by recipebook author.
		final Bundle missingFavoritesIngredients = new Bundle();
		missingIngredients.clear();
		for (Map.Entry<String,?> entry : favoritesSharedPreferences.getAll().entrySet()) {
			final String recipeName = entry.getKey();
			final Boolean isFavorited = (Boolean) entry.getValue();
			if (isFavorited) {
				final Recipe r = this.recipeBook.allRecipeIndex.get(recipeName);
				missingIngredients.addAll(r.ingredients);
			}
		}
		missingIngredients.removeAll(this.pantry);
		missingFavoritesIngredients.putStringArray("ingredients", missingIngredients.toArray(new String[missingIngredients.size()]));
		intent.putExtra(ShoppingListActivity.FAV_KEY, missingFavoritesIngredients);

		this.startActivity(intent);
	}

	void nextFilterState() {
		ingredientSearchNeededUndoing();
		final String filterState = BookDisplay.this.recipeAdapter.nextFilterState(BookDisplay.this, BookDisplay.this.recipeListFootnote);
		BookDisplay.this.actionBar.setTitle(BookDisplay.this.getResources().getString(R.string.app_name_title_fmt, filterState));
		tryInvalidateOptionsMenu();
	}

	void tryInvalidateOptionsMenu() {

//		try {
//			Constructor managerConstructor = managerClass.getConstructor(this.class);
//			Object manager = managerConstructor.newInstance(context);
//			Method m = managerClass.getMethod("invalidateOptionsMenu");
//			m.invoke(manager);
//		} catch(ClassNotFoundException e) {
//			Log.d(TAG, "Can't invalidate menu on this OS");
//		}

	}

	void startSetIngredients() {
		final Intent intent = new Intent(this, Pantry.class);
		for (String key : this.recipeBook.categorizedIngredients.keySet()) {
			final List l = this.recipeBook.categorizedIngredients.get(key);
			intent.putExtra("ingredients-" + key, l.toArray(new String[l.size()]));
		}
		this.startActivityForResult(intent, 1);
	}


	@Override
	public Dialog onCreateDialog(int id) {

		final AlertDialog.Builder adb = new AlertDialog.Builder(this);
		switch (id) {
			case DIALOG_SPLASH:
				//adb.setTitle(R.string.welcome)
				//	.setMessage(R.string.welcome_message)
				//	.setPositiveButton(R.string.begin, new DialogInterface.OnClickListener() { 
				//		public void onClick(DialogInterface dialog, int which) {
				//			startSetIngredients();
				//		}
				//	}).setNegativeButton(R.string.help, new DialogInterface.OnClickListener() { 
				//		public void onClick(DialogInterface dialog, int which) {
				//			final Intent intent = new Intent(BookDisplay.this, Instructions.class);
				//			BookDisplay.this.startActivity(intent);
				//		}
				//	});

				final Dialog dialog = new Dialog(this, R.style.fullScreenOverlay);
				dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.introduction);

				final ImageView arrowImage = (ImageView) dialog.findViewById(R.id.arrow);
				final TextView dialogText = (TextView) dialog.findViewById(R.id.introduction_text);

				final ImageButton intro_btn_viewable = (ImageButton) dialog.findViewById(R.id.b_viewable);
				final ImageButton intro_btn_pantry = (ImageButton) dialog.findViewById(R.id.b_pantry);
				final ImageButton intro_btn_shopping = (ImageButton) dialog.findViewById(R.id.b_shopping);

				intro_btn_viewable.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						intro_btn_viewable.setSelected(true);
						intro_btn_pantry.setSelected(false);
						intro_btn_shopping.setSelected(false);
						arrowImage.setVisibility(View.GONE);
						dialogText.setText(R.string.introduction_dialog_view);
					}
				});

				intro_btn_pantry.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						intro_btn_viewable.setSelected(false);
						intro_btn_pantry.setSelected(true);
						intro_btn_shopping.setSelected(false);
						arrowImage.setVisibility(View.GONE);
						dialogText.setText(R.string.introduction_dialog_pantry);
					}
				});

				intro_btn_shopping.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						intro_btn_viewable.setSelected(false);
						intro_btn_pantry.setSelected(false);
						intro_btn_shopping.setSelected(true);
						arrowImage.setVisibility(View.GONE);
						dialogText.setText(R.string.introduction_dialog_shopping);
					}
				});

				final Button intro_btn_done = (Button) dialog.findViewById(R.id.dialog_done);
				intro_btn_done.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				return dialog;

			case DIALOG_PURCHASEPLZ:
				final int defaultChoice = 2;
				final DialogInterface.OnClickListener purchase_ocl = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						BookDisplay.this.tracker.trackEvent("Initialize", "Donation", "attempting", which);
						if (which == -1) {
							which = defaultChoice;
						}
						final String sku = CATALOG[which].sku;
						BookDisplay.this.mBillingService.requestPurchase(sku, "1");
						dialog.dismiss();
					}
				};
				adb.setTitle(R.string.catalog_title)
					.setSingleChoiceItems(BookDisplay.this.mCatalogAdapter, defaultChoice, purchase_ocl)
					.setNegativeButton(R.string.catalog_buy_no, new DialogInterface.OnClickListener() { 
						public void onClick(DialogInterface dialog, int which) {
							BookDisplay.this.tracker.trackEvent("Initialize", "Donating", "refused", 1);
							dialog.dismiss();
						}
					}).setPositiveButton(R.string.catalog_buy_yes, purchase_ocl);
				return adb.create();
			case DIALOG_CHOOSE_SEARCH:
				adb.setTitle(R.string.search_choice)
					.setPositiveButton(R.string.search_choice_btn_title, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							BookDisplay.this.startSearch(null, false, null, false);
							dialog.dismiss();
						}
					})
					.setNeutralButton(R.string.search_choice_btn_ingredients, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							BookDisplay.this.ingredientSearchContainer.setVisibility(View.VISIBLE);
							dialog.dismiss();

							final String filterState = BookDisplay.this.recipeAdapter.ingredientSearch(BookDisplay.this.ingredientSearchRequirements);
							BookDisplay.this.actionBar.setTitle(BookDisplay.this.getResources().getString(R.string.app_name_title_fmt, filterState));

						}
					})
					;

				return adb.create();

			case DIALOG_CLEAR_INGRED_SEARCH:
				adb.setTitle(R.string.search_ingred_clear_all_question)
					.setNegativeButton(R.string.search_ingred_finish_search, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							ingredientSearchNeededUndoing();
							dialog.dismiss();
						}
					})
					.setPositiveButton(R.string.search_ingred_clear_all_clear, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							BookDisplay.this.ingredientSearchNoteHeader.setText(R.string.ingredient_search_ingr_note);
							BookDisplay.this.ingredientSearchRequirements.clear();
							BookDisplay.this.recipeBook.updateSearchResult(BookDisplay.this.ingredientSearchRequirements);
							BookDisplay.this.recipeAdapter.notifyDataSetChanged();
							dialog.dismiss();
						}
					})
					;
				return adb.create();
			default:
				return null;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		loadPantry();
		this.recipeBook.updateProducable(this.pantry);  // TODO kill
		this.recipeAdapter.updatePantry(this.pantry);
	}


	@Override
	public Object onRetainNonConfigurationInstance() {
		Log.d(TAG, "onRetainNonConfigurationInstance");
		if ((this.recipeBook != null) && this.recipeBook.readyToStore) {
			return this.recipeBook;
		} else {
			return null;
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, R.id.search, 0, R.string.search).setIcon(android.R.drawable.ic_menu_search);
		menu.add(Menu.NONE, R.id.instructions, 1, R.string.help).setIcon(android.R.drawable.ic_menu_help);
		menu.add(Menu.NONE, R.id.stock, 2, R.string.set_stock); //.setIcon(R.drawable.ic_btn_mark_owned_ingredients);
		menu.add(Menu.NONE, R.id.shoppingsuggestions, 3, R.string.shopping_suggestions); //.setIcon(R.drawable.ic_btn_suggest_shopping_list);
		//menu.add(Menu.NONE, R.id.feedback, 4, R.string.feedback);
		//menu.add(Menu.NONE, R.id.togglefilter, 6, R.string.toggle_filter).setIcon(R.drawable.ic_btn_toggle_viewable);
		if (hasDonated) {
			menu.add(Menu.NONE, R.id.donate, 7, R.string.donate_donators_rock);
		} else {
			menu.add(Menu.NONE, R.id.donate, 7, R.string.donate);
		}
		MenuItem randomButton = menu.add(Menu.NONE, R.id.random, 9, R.string.random);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		final Intent intent;
		switch (item.getItemId()) {
			case R.id.search:
				showDialog(DIALOG_CHOOSE_SEARCH);
				return true;
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
				nextFilterState();
				return true;
			case R.id.donate:
				if (mBillingService == null) {
					mBillingService = new BillingService();
					mBillingService.setContext(this);
					mPurchaseObserver = new DrinksPurchaseObserver(mHandler);
					ResponseHandler.register(mPurchaseObserver);
				}
				if (mBillingService.checkBillingSupported()) {
					if (mCatalogAdapter == null) {
						mCatalogAdapter = new CatalogAdapter(this, CATALOG);
					}
				}
				showDialog(DIALOG_PURCHASEPLZ);
				return true;
			case R.id.random:
				final Random rng = new Random();
				final int count = this.recipeAdapter.getCount();
				if (count < 1) { return true; }
				final int index = rng.nextInt(count);
				try {
					final Recipe recipe = this.recipeAdapter.getItem(index);
					BookDisplay.this.showRecipe(recipe);
				} catch (IndexOutOfBoundsException ex) {
					Log.e(TAG, "index error on random access to adapter.  Ignoring.");
				}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("adapterview", this.recipeAdapter.getFilterViewId());
		final String titleSearchQuery = this.recipeAdapter.getTitleSearchQuery();
		if (titleSearchQuery != null) {
			outState.putString("adaptersearchvalue", titleSearchQuery);
		}
	}

	void setInstanceState(Bundle inState) {
		try {
			if (inState != null) {
				final String searchQuery = inState.getString("adaptersearchvalue");
				if (searchQuery != null) {
					final String filterState = this.recipeAdapter.titleSearch(searchQuery);
					this.actionBar.setTitle(this.getResources().getString(R.string.app_name_title_fmt, filterState));
				}
				this.recipeAdapter.setFilterViewId(inState.getInt("adapterview", -1), this, this.recipeListFootnote);
			}
		} catch (Exception ex) {
			Log.w(TAG, "discarding saved-instance-state");
		} // discard errors from restarting from saved bundle.
	}

	@Override
	protected void onResume() {
		super.onResume();
		tracker.trackPageView("/" + TAG);
	}

	@Override
	protected void onDestroy() {
		this.tracker.dispatch();
		this.tracker.stopSession();
		if (mPurchaseDatabase != null)
			mPurchaseDatabase.close();
		if (mBillingService != null)
			mBillingService.unbind();
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent == null) {
			return;
		}

		final Bundle extras = intent.getExtras();

		// Handle search
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String query = intent.getStringExtra(android.app.SearchManager.QUERY);
			final String filterState = this.recipeAdapter.titleSearch(query);
			this.actionBar.setTitle(this.getResources().getString(R.string.app_name_title_fmt, filterState));
		}
	}

	private class RecipeBookLoadTask extends AsyncTask<RecipeBook, Integer, Void> {
		private final static String TAG = "ocjlBD.RLT";
		private TextView splashScreenText;
		private final String loadingFormat = BookDisplay.this.getResources().getString(R.string.loading_recipe_number);


		@Override
		protected void onPreExecute() {
			Log.i(TAG, "pe");
			this.splashScreenText = (TextView) BookDisplay.this.findViewById(R.id.splash_screen_text);
		}

		@Override
		protected Void doInBackground(RecipeBook... recipeBooks) {
			Log.i(TAG, "dib");
			BookDisplay.this.loadPantry();

			final long startTime = android.os.SystemClock.uptimeMillis();
			recipeBooks[0].load(BookDisplay.this, new Runnable() {
				private int n = 0;
				synchronized public void run() {
					n++;
					RecipeBookLoadTask.this.publishProgress(n);
				}
			}, BookDisplay.this.pantry, handler, BookDisplay.this, BookDisplay.this.recipeBook.ingredients);
			BookDisplay.this.tracker.trackEvent("Performance", "RecipeBookLoading", "Elapsed", (int) (android.os.SystemClock.uptimeMillis() - startTime));

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progresses) {
			final Integer n = progresses[0];
			if ((n % 147) == 0) { 
				splashScreenText.setText(String.format(loadingFormat, n));
			}
			if (((n < 50) && ((n % 5) == 0)) || ((n % 200) == 0)) {
				BookDisplay.this.recipeAdapter.notifyDataSetChanged();
			}
		}

		@Override
		protected void onPostExecute(Void v) {
			Log.i(TAG, "ope");

			final LinearLayout loadingIndicator = (LinearLayout) findViewById(R.id.loading_indicator);
			loadingIndicator.setVisibility(View.GONE);

			favoritesSharedPreferences.registerOnSharedPreferenceChangeListener(BookDisplay.this.recipeAdapter);

			handleIntent(getIntent());

			final String filterState = BookDisplay.this.recipeAdapter.setBestInitialState(BookDisplay.this, BookDisplay.this.recipeListFootnote);
			BookDisplay.this.actionBar.setTitle(BookDisplay.this.getResources().getString(R.string.app_name_title_fmt, filterState));

			new FavoritesLoadTask().execute();
		}
	}

	private class FavoritesLoadTask extends AsyncTask<Void, Void, Void> {
		private final static String TAG = "ocjlBD.FLT";
		@Override
		protected Void doInBackground(Void... vs) {
			Log.i(TAG, "dib");
			BookDisplay.this.recipeAdapter.setFavoritesFromPreferences(BookDisplay.this.favoritesSharedPreferences.getAll());
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			Log.i(TAG, "ope");
			BookDisplay.this.recipeAdapter.notifyDataSetChanged();

			class ListToggleAction implements Action {
				@Override
				public int getDrawable() {
					return R.drawable.ic_btn_toggle_viewable;
				}
				@Override
				public void performAction(View view) {
					BookDisplay.this.nextFilterState();
				}
			}
			BookDisplay.this.actionBar.addAction(new ListToggleAction());

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
			BookDisplay.this.actionBar.addAction(new PickIngredientsAction());

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
			BookDisplay.this.actionBar.addAction(new SuggestShoppingAction());

			BookDisplay.this.recipeListFootnote.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					BookDisplay.this.startSetIngredients();
				}
			});

			final TextView emptyListNotification = (TextView) BookDisplay.this.findViewById(R.id.empty_view);
			BookDisplay.this.recipeListView.setEmptyView(emptyListNotification);
			emptyListNotification.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						BookDisplay.this.nextFilterState();
						;
					}
				});

			Log.d(TAG, "now setting up the ingredient list adapter for search. " + BookDisplay.this.recipeBook.ingredients.size());

			final ArrayAdapter ingredientListAdapter = new ArrayAdapter(BookDisplay.this, android.R.layout.simple_list_item_1, BookDisplay.this.recipeBook.ingredients);
			final ListView ingredientList = (ListView) BookDisplay.this.findViewById(R.id.ingredient_search_picklist);

			ingredientList.setAdapter(ingredientListAdapter);
			ingredientList.setOnItemClickListener(new OnItemClickListener() {
				static final String TAG = "ocjlBD.FLT.OICL";
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					try {
						final String pickedItem = BookDisplay.this.recipeBook.ingredients.get(position);

						if (BookDisplay.this.ingredientSearchRequirements.contains(pickedItem)) {
							BookDisplay.this.ingredientSearchRequirements.remove(pickedItem);
						} else {
							BookDisplay.this.ingredientSearchRequirements.add(pickedItem);
						}
						BookDisplay.this.recipeBook.updateSearchResult(BookDisplay.this.ingredientSearchRequirements);
						BookDisplay.this.recipeAdapter.notifyDataSetChanged();

						handler.post(new Runnable() {
							public void run() {
								Log.d(TAG, "set note text");
								BookDisplay.this.ingredientSearchNoteHeader.setText(BookDisplay.this.getResources().getString(R.string.ingredient_search_ingr_note) + " " + BookDisplay.this.ingredientSearchRequirements);
							}
						});

					} catch (IndexOutOfBoundsException ex) {
						Log.w(TAG, "clicked item in adapter that is not in the set.");
					}

				}
			});

			BookDisplay.this.ingredientSearchNoteHeader.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (BookDisplay.this.ingredientSearchRequirements.size() == 0) { return; }
					BookDisplay.this.showDialog(BookDisplay.DIALOG_CLEAR_INGRED_SEARCH);
				}
			});

		}
	}

	private void loadPantry() {
		this.pantry.clear();
		for (Map.Entry<String,?> entry : pantrySharedPreferences.getAll().entrySet()) {
			final String name = entry.getKey();
			final Boolean v = (Boolean) entry.getValue();
			if (v) {
				this.pantry.add(name);
			}
		}
		Log.d(TAG, "loaded " + pantry.size() + " items into pantry list");
	}

	private class ReportingRunnable implements Runnable {
		synchronized public void run() {
			final String NAME = "Pkg";
			try { Thread.sleep(5000); } catch (InterruptedException ex) { }
			final String pn = BookDisplay.this.getPackageName();
			BookDisplay.this.tracker.trackEvent(NAME, "PN", pn, 1);

			final android.content.pm.PackageManager pm = BookDisplay.this.getPackageManager();
			try {
				final android.content.pm.PackageInfo pi = pm.getPackageInfo(pn, android.content.pm.PackageManager.GET_SIGNATURES);
				for (android.content.pm.Signature sig : pi.signatures) {
					BookDisplay.this.tracker.trackEvent(NAME, "Sigs", sig.toCharsString().substring(0, 16), 1);
				}
				BookDisplay.this.tracker.trackEvent(NAME, "Ver", pi.versionName, 1);
			} catch (android.content.pm.PackageManager.NameNotFoundException ex) {
				BookDisplay.this.tracker.trackEvent(NAME, "pn", pn, 1);
			}
		}
	}

	private static class CatalogAdapter extends ArrayAdapter<String> {
		private CatalogEntry[] mCatalog;
		public CatalogAdapter(Context context, CatalogEntry[] catalog) {
			super(context, android.R.layout.select_dialog_singlechoice);
			mCatalog = catalog;
			for (CatalogEntry element : catalog) {
				add(context.getString(element.nameId));
			}
			setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			// If the item at the given list position is not purchasable, then
			// "gray out" the list item.
			final View view = super.getDropDownView(position, convertView, parent);
			view.setEnabled(true);
			return view;
		}
	}

	private class DrinksPurchaseObserver extends PurchaseObserver {
		public DrinksPurchaseObserver(Handler handler) {
			super(BookDisplay.this, handler);
		}

		@Override
		public void onBillingSupported(boolean supported) {
			if (supported) {
				//restoreDatabase();
			}
		}

		@Override
		public void onPurchaseStateChange(PurchaseState purchaseState, String itemId,
				int quantity, long purchaseTime, String developerPayload) {
			//mOwnedItemsCursor.requery();
		}

		@Override
		public void onRequestPurchaseResponse(RequestPurchase request,
				ResponseCode responseCode) {
			if (responseCode == ResponseCode.RESULT_OK) {
				BookDisplay.this.tracker.trackEvent("Initialize", "Donating", "success", 1);
			} else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
				BookDisplay.this.tracker.trackEvent("Initialize", "Donating", "cancelled", 1);
			} else {
				BookDisplay.this.tracker.trackEvent("Initialize", "Donating", "failed", 1);
			}
		}

		@Override
		public void onRestoreTransactionsResponse(RestoreTransactions request,
				ResponseCode responseCode) {
			if (responseCode == ResponseCode.RESULT_OK) {
			} else {
			}
		}
	}

	private static class CatalogEntry {
		public String sku;
		public int nameId;
		public Managed managed;

		public CatalogEntry(String sku, int nameId, Managed managed) {
			this.sku = sku;
			this.nameId = nameId;
			this.managed = managed;
		}
	}

	/** An array of product list entries for the products that can be purchased. */
	private static final CatalogEntry[] CATALOG = new CatalogEntry[] {
		new CatalogEntry("donation1", R.string.catalog_item_updates_and_donation1, Managed.MANAGED),
		new CatalogEntry("donation2", R.string.catalog_item_updates_and_donation2, Managed.MANAGED),
		new CatalogEntry("donation3", R.string.catalog_item_updates_and_donation3, Managed.MANAGED),
		new CatalogEntry("donation4", R.string.catalog_item_updates_and_donation4, Managed.MANAGED),
		new CatalogEntry("donation5", R.string.catalog_item_updates_and_donation5, Managed.MANAGED),
		//new CatalogEntry("android.test.purchased", R.string.android_test_purchased, Managed.UNMANAGED),
		//new CatalogEntry("android.test.canceled", R.string.android_test_canceled, Managed.UNMANAGED),
		//new CatalogEntry("android.test.refunded", R.string.android_test_refunded, Managed.UNMANAGED),
		//new CatalogEntry("android.test.item_unavailable", R.string.android_test_item_unavailable, Managed.UNMANAGED),
	};

}

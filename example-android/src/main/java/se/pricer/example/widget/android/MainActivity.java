/*
 * Pricer Server Software
 *
 * Confidential Property of Pricer AB (publ). Copyright © 1998-2017 Pricer AB (publ),
 * Box 215,Västra Järnvägsgatan 7, SE-101 24 Stockholm, Sweden. All rights reserved.
 */
package se.pricer.example.widget.android;

import se.pricer.widget.android.PricerPosition;
import se.pricer.widget.android.PricerSearchResponse;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.Toast;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

  private FragmentAdapter fragmentAdapter;
  private ViewPager viewPager;
  private ActionBar actionBar;

  public FragmentAdapter getFragmentAdapter() {
    return fragmentAdapter;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.example_activity);

    // Create the fragment adapter for handling several views
    fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
    // Connect the fragment adapter with the view pager
    viewPager = findViewById(R.id.viewPager);
    viewPager.setAdapter(fragmentAdapter);

    // Get and configure the action bar for tabs
    actionBar = getActionBar();
    actionBar.setHomeButtonEnabled(false);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    actionBar.addTab(actionBar.newTab().setText(R.string.map).setTabListener(this));
    actionBar.addTab(actionBar.newTab().setText(R.string.shopping_list).setTabListener(this));

    // Sync the view pager with the action bar tabs
    viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        actionBar.setSelectedNavigationItem(position);
      }

      @Override
      public void onPageScrolled(int arg0, float arg1, int arg2) {
      }

      @Override
      public void onPageScrollStateChanged(int arg0) {
      }
    });
  }

  @Override
  public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
  }

  @Override
  public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
    // Sync the action bar tabs with the view pager
    viewPager.setCurrentItem(tab.getPosition());
  }

  @Override
  public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
  }

  private void addSearchResult(Cursor cursor) {
    try {
      ObjectMapper mapper = new ObjectMapper();

      PricerSearchResponse.SearchResult result = mapper.readValue(cursor.getString(
          SearchProvider.SUGGEST_COLUMN_RESULT_OBJECT_INDEX), PricerSearchResponse.SearchResult.class);

      ShoppingObject shoppingObject = null;

      if (result instanceof PricerSearchResponse.SearchResultArticleGroup) {
        PricerSearchResponse.SearchResultArticleGroup
            resultArticleGroup =
            (PricerSearchResponse.SearchResultArticleGroup) result;

        // Create the shopping object
        shoppingObject = new ShoppingObject(resultArticleGroup.getGroupType() + resultArticleGroup.getGroupName(),
                                            resultArticleGroup.getGroupName(),
                                            null /* article groups don't have a price */,
                                            resultArticleGroup.getGroupPosition(),
                                            resultArticleGroup.getGroupShape());

      } else if (result instanceof PricerSearchResponse.SearchResultItem) {
        PricerSearchResponse.SearchResultItem resultItem = (PricerSearchResponse.SearchResultItem) result;

        PricerPosition firstPosition = resultItem.getItemPositions().isEmpty() ?
                                       null : resultItem.getItemPositions().get(0);

        // Create the shopping object
        shoppingObject = new ShoppingObject(resultItem.getItemId(),
                                            resultItem.getItemName(),
                                            resultItem.getItemProperties().get("PRICE"),
                                            firstPosition,
                                            null /* items don't have a shape */);
      }

      // Add the shopping object to the shopping list
      fragmentAdapter.getFragmentShoppingList().addShoppingObject(shoppingObject);

    } catch (IOException e) {
      Log.w(this.getClass().getName(), e);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);

    // Configure the menu for searching with search suggestions
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    final MenuItem searchMenuItem = menu.findItem(R.id.search);
    final SearchView searchView = (SearchView) searchMenuItem.getActionView();
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    searchView.setSubmitButtonEnabled(false);

    // Add search result by clicking on the search suggestion
    searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
      @Override
      public boolean onSuggestionSelect(int position) {
        return true;
      }

      @Override
      public boolean onSuggestionClick(int position) {
        CursorAdapter selectedView = searchView.getSuggestionsAdapter();
        Cursor cursor = (Cursor) selectedView.getItem(position);
        addSearchResult(cursor);
        searchMenuItem.collapseActionView();
        return true;
      }
    });

    // Add a query text listener
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextChange(String newText) {
        return true;
      }

      @Override
      public boolean onQueryTextSubmit(final String query) {
        // The SearchProvider is blocking so it should be done in a separate thread to not stall the gui/main thread
        new Thread(new Runnable() {
          @Override
          public void run() {
            final Cursor
                cursor =
                getContentResolver().query(SearchProvider.createQueryUri(query), null, null, null, null);

            if (cursor == null) {
              runOnUiThread(new Runnable() {
                public void run() {
                  // No result found
                  Toast.makeText(MainActivity.this, getResources().getString(R.string.search_no_results),
                                 Toast.LENGTH_SHORT).show();
                }
              });
            } else if (cursor.moveToNext()) {
              // Add the first result to the shopping list
              runOnUiThread(new Runnable() {
                public void run() {
                  addSearchResult(cursor);
                }
              });
            }
          }
        }).start();
        searchMenuItem.collapseActionView();
        return false;
      }
    });

    return true;
  }
}
package nzesl.shopping_app;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.pricer.widget.android.PricerPosition;
import se.pricer.widget.android.PricerSearchResponse;

public class MyShoppingList extends BaseActivity {

    private ShoppingListAdapter shoppingListAdapter;
    private static MyShoppingList instance;

    static final int REQUEST_CODE = 0;


    private Button scanBarcode;

    public static MyShoppingList getInstance() {
        return instance;
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_list;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.navigation_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        // Create an empty shopping list
        List<ShoppingObject> shoppingObjects = new ArrayList<>();
        shoppingListAdapter = new ShoppingListAdapter(this, shoppingObjects);

        // Connect the list view with the shopping list adapter
        ListView listView = findViewById(R.id.shopping_list_view);
        listView.setAdapter(shoppingListAdapter);

        scanBarcode = findViewById(R.id.barcode_button);

        scanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), ScanBarcode.class);

                startActivityForResult(intent, REQUEST_CODE);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String s=data.getStringExtra("result");
                try {
                    ObjectMapper mapper = new ObjectMapper();

                    PricerSearchResponse.SearchResult result = mapper.readValue(s , PricerSearchResponse.SearchResult.class);

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
                    addShoppingObject(shoppingObject);

                } catch (IOException e) {
                    Log.w(this.getClass().getName(), e);
                }
            } else if(resultCode == Activity.RESULT_CANCELED) {
                new AlertDialog.Builder(this)
                        .setTitle("No Result")
                        .setMessage("There was no item found")
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }

    public void addShoppingObject(ShoppingObject shoppingObject) {
        shoppingListAdapter.add(shoppingObject);
    }

    public void removeShoppingObject(ShoppingObject shoppingObject) {
        shoppingListAdapter.remove(shoppingObject);
    }

    public ShoppingObject getShoppingObject(String id) {
        return shoppingListAdapter.getShoppingObject(id);
    }

    public void notifyDataSetChanged() {
        shoppingListAdapter.notifyDataSetChanged();
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
            addShoppingObject(shoppingObject);

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
                                    Toast.makeText(MyShoppingList.this, getResources().getString(R.string.search_no_results),
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

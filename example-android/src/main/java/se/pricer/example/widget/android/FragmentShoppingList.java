/*
 * Pricer Server Software
 *
 * Confidential Property of Pricer AB (publ). Copyright © 1998-2017 Pricer AB (publ),
 * Box 215,Västra Järnvägsgatan 7, SE-101 24 Stockholm, Sweden. All rights reserved.
 */
package se.pricer.example.widget.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class FragmentShoppingList extends Fragment {

  private ShoppingListAdapter shoppingListAdapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);

    // Create an empty shopping list
    List<ShoppingObject> shoppingObjects = new ArrayList<>();
    shoppingListAdapter = new ShoppingListAdapter((MainActivity) this.getActivity(), shoppingObjects);

    // Connect the list view with the shopping list adapter
    ListView listView = view.findViewById(R.id.shopping_list_view);
    listView.setAdapter(shoppingListAdapter);

    return view;
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
}
/*
 * Pricer Server Software
 *
 * Confidential Property of Pricer AB (publ). Copyright © 1998-2017 Pricer AB (publ),
 * Box 215,Västra Järnvägsgatan 7, SE-101 24 Stockholm, Sweden. All rights reserved.
 */
package nzesl.shopping_app;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

class ShoppingListAdapter extends ArrayAdapter<ShoppingObject> {

  private final MyShoppingList activity;
  private final MyShoppingMap fragmentMap;
  private final List<ShoppingObject> shoppingObjects;

  ShoppingListAdapter(MyShoppingList activity, List<ShoppingObject> shoppingObjects) {
    super(activity, R.layout.shopping_object, shoppingObjects);
    this.activity = activity;
    this.fragmentMap = MyShoppingMap.getInstance();
    this.shoppingObjects = shoppingObjects;
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    // Create a new shopping object view
    View view = inflater.inflate(R.layout.shopping_object, parent, false);

    // Get the shopping object at the specified index/position
    final ShoppingObject shoppingObject = shoppingObjects.get(position);

    // Setup the view to reflect the shopping object
    ((TextView) view.findViewById(R.id.objectName)).setText(shoppingObject.getName());
    ((TextView) view.findViewById(R.id.objectPrice)).setText(shoppingObject.getPrice());

    // Darken the text color if no position was found
    if (shoppingObject.getPosition() == null) {
      ((TextView) view.findViewById(R.id.objectName)).setTextColor(Color.DKGRAY);
    }

    // Set the on click listener for the remove button
    view.findViewById(R.id.buttonRemove).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        remove(shoppingObject);
      }
    });

    ((CheckBox) view.findViewById(R.id.checkboxPicked)).setChecked(shoppingObject.isPicked());

    // Set the on checked listener for the picked checkbox
    ((CheckBox) view.findViewById(R.id.checkboxPicked))
        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            shoppingObject.setPicked(isChecked);
            if (shoppingObject.isPicked()) {
              fragmentMap.removeShoppingObjectFromMap(shoppingObject);
            } else {
              fragmentMap.addShoppingObjectToMap(shoppingObject);
            }
          }
        });

    return view;
  }

  @Override
  public void add(ShoppingObject shoppingObject) {
    // Only add shopping objects that are not already in the list
    if (getPosition(shoppingObject) == -1) {
      super.add(shoppingObject);

      // Add the shopping object to the map if it's not already picked
      if (!shoppingObject.isPicked()) {
        fragmentMap.addShoppingObjectToMap(shoppingObject);
      }
    }
  }

  @Override
  public void remove(ShoppingObject shoppingObject) {
    super.remove(shoppingObject);
    // Shopping objects removed from the list also have to be removed from the map
    fragmentMap.removeShoppingObjectFromMap(shoppingObject);
  }

  ShoppingObject getShoppingObject(String id) {
    for (ShoppingObject shoppingObject : shoppingObjects) {
      if (shoppingObject.getObjectId().equals(id)) {
        return shoppingObject;
      }
    }
    return null;
  }
}

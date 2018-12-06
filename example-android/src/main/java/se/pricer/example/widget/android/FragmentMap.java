/*
 * Pricer Server Software
 *
 * Confidential Property of Pricer AB (publ). Copyright © 1998-2017 Pricer AB (publ),
 * Box 215,Västra Järnvägsgatan 7, SE-101 24 Stockholm, Sweden. All rights reserved.
 */
package se.pricer.example.widget.android;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import se.pricer.example.widget.android.polestar.LocationService;
import se.pricer.widget.android.PricerColor;
import se.pricer.widget.android.PricerMapListener;
import se.pricer.widget.android.PricerMapWidget;
import se.pricer.widget.android.PricerPolygon;
import se.pricer.widget.android.PricerPosition;
import se.pricer.widget.android.PricerStyle;

public class FragmentMap extends Fragment implements PricerMapListener {

  private static final String SHOPPING_LIST_GROUP = "SHOPPING_LIST_GROUP";

  private FragmentShoppingList fragmentShoppingList;
  private View view;
  private PricerMapWidget map;
  private ProgressDialog progress;
  private TextView zoneNameView;
  private LocationService locationService;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    fragmentShoppingList = ((MainActivity) getActivity()).getFragmentAdapter().getFragmentShoppingList();
    view = inflater.inflate(R.layout.fragment_map, container, false);
    view.findViewById(R.id.nextShoppingObjectView).setVisibility(View.INVISIBLE);

    // Create, setup and show the progress dialog
    progress = new ProgressDialog(this.getActivity());
    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    progress.setIndeterminate(true);
    progress.setCancelable(false);
    progress.setCanceledOnTouchOutside(false);
    progress.setMessage("Initializing...");
    progress.show();

    map = view.findViewById(R.id.pricerMapWidget);
    // Register the listener before initialization
    map.registerListener(this);
    // Start the pricer map widget initialization
    map.initialize();
    zoneNameView = view.findViewById(R.id.zoneNameView);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    // Start the geo provider when the the application returns
    if (locationService != null) {
      locationService.start();
    }
  }

  @Override
  public void onPause() {
    super.onPause();

    // Stop the geo provider when the application goes inactive
    if (locationService != null) {
      locationService.stop();
    }
  }

  @Override
  public void onInitialized() {
    // The map widget is now initialized and can be configured as desired
    // Show the best path between the user and interest points in the SHOPPING_LIST_GROUP
    progress.dismiss();
    map.showBestPathToGroup(SHOPPING_LIST_GROUP);

    // Configure the map as desired
    map.enableFollowUserPosition(true);
    map.enableFollowUserDirection(true);
    map.enableGesturePan(false);
    map.enableGestureRotate(false);
    map.enableGesturePinch(true);

    locationService = new LocationService(this.getContext(), map);
    locationService.start();
  }

  @Override
  public void onError(PricerMapError code, String message) {
    progress.dismiss();
    Toast.makeText(this.getActivity(), code.name() + ": " + message, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onGeoProviderStarted() {
    // The geo provider has started - the user's position will now be drawn on the screen
    // The progress dialog can now be removed
    progress.dismiss();
  }

  @Override
  public void onInterestPointReached(String pointId) {
  }

  @Override
  public void onInterestPointLeaved(String pointId) {
  }

  @Override
  public void onGeofencingZoneEntered(String zoneId) {
    // Write the entered zone name on the screen
    zoneNameView.setText(zoneId);
  }

  @Override
  public void onGeofencingZoneLeaved(String zoneId) {
    // The user left the zone
    zoneNameView.setText("");
  }

  @Override
  public void onNextInterestPointChanged(String pointId, List<String> bestPath) {
    if (pointId == null) {
      // No more interest points to visit, hide the next shopping object view
      view.findViewById(R.id.nextShoppingObjectView).setVisibility(View.INVISIBLE);
      return;
    } else {
      // The next interest point to visit has changed, make the view visible
      view.findViewById(R.id.nextShoppingObjectView).setVisibility(View.VISIBLE);
    }

    // Get the shopping object from the shopping list
    final ShoppingObject shoppingObject = fragmentShoppingList.getShoppingObject(pointId);

    ((TextView) view.findViewById(R.id.objectName)).setText(shoppingObject.getName());
    ((TextView) view.findViewById(R.id.objectPrice)).setText(shoppingObject.getPrice());

    // Set the on click listener for the remove button
    view.findViewById(R.id.buttonRemove).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        fragmentShoppingList.removeShoppingObject(shoppingObject);
      }
    });

    ((CheckBox) view.findViewById(R.id.checkboxPicked)).setOnCheckedChangeListener(null);
    ((CheckBox) view.findViewById(R.id.checkboxPicked)).setChecked(shoppingObject.isPicked());

    // Set the on checked listener for the picked checkbox
    ((CheckBox) view.findViewById(R.id.checkboxPicked))
        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            shoppingObject.setPicked(isChecked);
            fragmentShoppingList.notifyDataSetChanged();

            if (shoppingObject.isPicked()) {
              removeShoppingObjectFromMap(shoppingObject);
            } else {
              addShoppingObjectToMap(shoppingObject);
            }
          }
        });
  }

  @Override
  public void onClick(PricerPosition position) {
  }

  @Override
  public void onUserPositionUpdated(PricerPosition position) {
  }

  @Override
  public void onUserPositionLost() {
  }

  public void removeShoppingObjectFromMap(ShoppingObject shoppingObject) {
    // Remove the interest point from the map
    map.removeInterestPoint(shoppingObject.getObjectId());
    map.removePolygon(shoppingObject.getObjectId());
  }

  public void addShoppingObjectToMap(ShoppingObject shoppingObject) {
    // Only add shopping objects with a position
    if (shoppingObject.getPosition() != null) {
      PricerStyle style = new PricerStyle(new PricerColor(0.99f, 0.62f, 0.62f, 1.00f),
          new PricerColor(0.92f, 0.19f, 0.19f, 0.75f), 3.0f);
      map.addInterestPoint(shoppingObject.getObjectId(), SHOPPING_LIST_GROUP, shoppingObject.getPosition(), 6.0f, style);

      if (shoppingObject.getShape() instanceof PricerPolygon) {
        map.addPolygon(shoppingObject.getObjectId(), (PricerPolygon) shoppingObject.getShape());
      }
    }
  }
}
package nzesl.shopping_app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLOutput;
import java.util.List;

import nzesl.shopping_app.polestar.LocationService;
import se.pricer.widget.android.PricerColor;
import se.pricer.widget.android.PricerMapListener;
import se.pricer.widget.android.PricerMapWidget;
import se.pricer.widget.android.PricerPolygon;
import se.pricer.widget.android.PricerPosition;
import se.pricer.widget.android.PricerStyle;

public class MyShoppingMap extends BaseActivity implements PricerMapListener {

    private static final String SHOPPING_LIST_GROUP = "SHOPPING_LIST_GROUP";
    static MyShoppingMap instance;

    private MyShoppingList fragmentShoppingList;
    private PricerMapWidget map;
    private ProgressDialog progress;
    private TextView zoneNameView;
    private LocationService locationService;

    public static MyShoppingMap getInstance() {
        return instance;
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_map;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.navigation_map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        findViewById(R.id.nextShoppingObjectView).setVisibility(View.INVISIBLE);

        // Create, setup and show the progress dialog
        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress.setMessage("Initializing...");
        progress.show();

        map = findViewById(R.id.pricerMapWidget);
        // Register the listener before initialization
        map.registerListener(this);
        // Start the pricer map widget initialization
        map.initialize();
        zoneNameView = findViewById(R.id.zoneNameView);
    }

    @Override
    public void onResume() {
        super.onResume();
        fragmentShoppingList = MyShoppingList.getInstance();
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
        progress.dismiss();
        map.showBestPathToGroup(SHOPPING_LIST_GROUP);

        // Configure the map as desired
        map.enableFollowUserPosition(true);
        map.enableFollowUserDirection(true);
        map.enableGesturePan(false);
        map.enableGestureRotate(false);
        map.enableGesturePinch(true);

        locationService = new LocationService(this, map);
        locationService.start();
        map.setUserPosition(new PricerPosition(1, 1, 0), "POLESTAR");
    }

    @Override
    public void onError(PricerMapError code, String message) {
        progress.dismiss();
        Toast.makeText(this, code.name() + ": " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGeoProviderStarted() {
        // The geo provider has started - the user's position will now be drawn on the screen
        // The progress dialog can now be removed
        progress.dismiss();
    }

    @Override
    public void onInterestPointReached(String pointId) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MyShoppingMap.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MyShoppingMap.this);
        }
        builder.setTitle("You have arrived")
                .setMessage("You are now at " + pointId)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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
        fragmentShoppingList = MyShoppingList.getInstance();
        if (pointId == null) {
            // No more interest points to visit, hide the next shopping object view
            findViewById(R.id.nextShoppingObjectView).setVisibility(View.INVISIBLE);
            return;
        } else {
            // The next interest point to visit has changed, make the view visible
            findViewById(R.id.nextShoppingObjectView).setVisibility(View.VISIBLE);
        }

        // Get the shopping object from the shopping list
        final ShoppingObject shoppingObject = fragmentShoppingList.getShoppingObject(pointId);

        ((TextView) findViewById(R.id.objectName)).setText(shoppingObject.getName());
        ((TextView) findViewById(R.id.objectPrice)).setText(shoppingObject.getPrice());

        // Set the on click listener for the remove button
        findViewById(R.id.buttonRemove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentShoppingList.removeShoppingObject(shoppingObject);
            }
        });

        ((CheckBox) findViewById(R.id.checkboxPicked)).setOnCheckedChangeListener(null);
        ((CheckBox) findViewById(R.id.checkboxPicked)).setChecked(shoppingObject.isPicked());

        // Set the on checked listener for the picked checkbox
        ((CheckBox) findViewById(R.id.checkboxPicked))
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
        System.out.println(position.getX());
        System.out.println(position.getY());
        System.out.println(position.getFloor());
        map.setUserPosition(position);
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

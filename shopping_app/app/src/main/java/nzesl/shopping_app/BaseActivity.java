package nzesl.shopping_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    protected BottomNavigationView navigationView;      //bottom navigate bar

    public abstract int getContentViewId();

    public abstract int getNavigationMenuItemId();

    /**
     * Creates BaseActivity object
     * @param savedInstanceState is a saved state of the application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());     //uses the activity_location.xml for layout of page

        navigationView = findViewById(R.id.navigation);
        navigationView.setItemIconTintList(null);
        navigationView.setOnNavigationItemSelectedListener(this);

    }

    /**
     * When this activity is seen by user, update navigation bar
     */
    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    /**
     * When activity is in background and not killed
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Updates navigation bar
     */
    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    /**
     * Goes through bottom navigation bar and checks current activity
     * @param itemId is the action's id
     */
    void selectBottomNavigationBarItem(int itemId) {
        Menu menu = navigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);      //put check mark on current activity being viewed on bottom navigation bar
                break;
            }
        }
    }

    /**
     *  When item in navigation menu selected, change screen to that activity
     * @param item is the name of the activity
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_map:
                Intent map = new Intent(this, MyShoppingMap.class);
                map.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(map);

                return true;
            case R.id.navigation_list:
                Intent list = new Intent(this, MyShoppingList.class);
                list.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(list);
                return true;
        }
        return false;
    }

}

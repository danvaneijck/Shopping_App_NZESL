/*
 * Pricer Server Software
 *
 * Confidential Property of Pricer AB (publ). Copyright © 1998-2017 Pricer AB (publ),
 * Box 215,Västra Järnvägsgatan 7, SE-101 24 Stockholm, Sweden. All rights reserved.
 */
package se.pricer.example.widget.android;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class FragmentAdapter extends FragmentPagerAdapter {

  private FragmentShoppingList fragmentShoppingList;
  private FragmentMap fragmentMap;

  FragmentAdapter(FragmentManager fragmentManager) {
    super(fragmentManager);

    // Create the shopping list fragment
    fragmentShoppingList = new FragmentShoppingList();
    fragmentShoppingList.setRetainInstance(true);

    // Create the map fragment
    fragmentMap = new FragmentMap();
    fragmentMap.setRetainInstance(true);
  }

  @Override
  public Fragment getItem(int index) {
    switch (index) {
      case 0:
        return fragmentMap;
      case 1:
        return fragmentShoppingList;
    }
    return null;
  }

  @Override
  public int getCount() {
    return 2;
  }

  FragmentShoppingList getFragmentShoppingList() {
    return fragmentShoppingList;
  }

  FragmentMap getFragmentMap() {
    return fragmentMap;
  }
}
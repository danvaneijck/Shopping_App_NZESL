package nzesl.shopping_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import se.pricer.widget.android.PricerApi;

public class StoreSelect extends Activity implements AdapterView.OnItemClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_select);

        //* *EDIT* *
        ListView listview = (ListView) findViewById(R.id.listView1);
        listview.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(this, MyShoppingMap.class);

        String[] myKeys = getResources().getStringArray(R.array.sections);

        String[] api = findAPIDetails(myKeys[position]);

        if(api.length == 3){
            PricerApi.getInstance().startApi(api[0], api[1], api[2]);
            startActivity(intent);
        }
    }

    private String[] findAPIDetails(String s) {
        if(s.equalsIgnoreCase("Fresh Choice Te Ngae")){
            return new String[]{"6313278c-5098-11e7-8303-6451064d5d93","DQ2TXSY4X1R3L325U1KEH9K9Y","M3BtBuDDfZbXlCYDldmDAeCGQT0GYXCvbE-ynQE_hQM"};
        }
        if(s.equalsIgnoreCase("FC Christchurch Central City")){
            return new String[]{"5e300f79-7bbe-11e7-86c9-a08cfde11b4a","7058NO2UFKFG56RJZRE603J0V","wSRPDpROq4olIHc37IN6LMjORLhMCqwH9CPbleRhB_A"};
        }
        if(s.equalsIgnoreCase("Albany")){
            return new String[]{"6313278c-5098-11e7-8303-6451064d5d93","DQ2TXSY4X1R3L325U1KEH9K9Y","M3BtBuDDfZbXlCYDldmDAeCGQT0GYXCvbE-ynQE_hQM"};
        }

        return new String[]{"6313278c-5098-11e7-8303-6451064d5d93","DQ2TXSY4X1R3L325U1KEH9K9Y","M3BtBuDDfZbXlCYDldmDAeCGQT0GYXCvbE-ynQE_hQM"};
    }
}

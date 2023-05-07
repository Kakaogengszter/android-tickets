package com.example.androidshop;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ShopListActivity extends AppCompatActivity {

    private static final String LOG_TAG = ShopListActivity.class.getName();
    private FirebaseUser user;

    private FrameLayout redCircle;
    private TextView contentTextView;
    private int gridNumber = 1;
    private int cartItems = 0;
    public int queryLimit = 10;

    private RecyclerView mRecyclerView;
    private ArrayList<ShoppingItem> mItemList;
    private ShoppingItemAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;

    private NotificationHandler mNotHandler;

    private boolean viewRow = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Log.d(LOG_TAG, "Authenticated user!");
        }else{
            Log.d(LOG_TAG, "Unuthenticated user!");
            finish();
        }

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,gridNumber));
        mItemList = new ArrayList<>();

        mAdapter = new ShoppingItemAdapter(this,mItemList);

        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");


        // Get the data.
        queryData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver,filter);

        mNotHandler = new NotificationHandler(this);
    }

    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action == null){
                return;
            }

            switch (action){
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 5;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 10;
                    break;

            }
            queryData();
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    private void queryData() {

        mItemList.clear();

        // Searchbar
        // mItems.whereEqualTo()...
        mItems.orderBy("cartCount", Query.Direction.DESCENDING).limit(queryLimit).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                ShoppingItem item = document.toObject(ShoppingItem.class);
                item.setId(document.getId());
                mItemList.add(item);
            }

            if(mItemList.size() == 0){
                initializeData();
                queryData();
            }

            // Notify the adapter of the change.
            mAdapter.notifyDataSetChanged();
        });


    }

    public void deleteItem(ShoppingItem item){
        DocumentReference ref = mItems.document(item._getId());

        ref.delete().addOnSuccessListener(success ->{
            Log.d(LOG_TAG,"Item is successfully delete: " + item._getId());
        })
        .addOnFailureListener(failure -> {
            Toast.makeText(this,"Item " + item._getId() + " cannot be deleted.",Toast.LENGTH_LONG).show();
        });
        queryData();
        mNotHandler.cancel();
    }


    private void initializeData() {
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price);

        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemsRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        for (int i = 0; i < itemsList.length; i++)
            mItems.add(new ShoppingItem(
                    itemsList[i],
                    itemsInfo[i],
                    itemsPrice[i],
                    itemsRate.getFloat(i,0),
                    itemsImageResource.getResourceId(i,0),
                    0));


        itemsImageResource.recycle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.shop_list_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView sw = (SearchView) MenuItemCompat.getActionView(menuItem);
        sw.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG,s);
                mAdapter.getFilter().filter(s);
                return false;
            }
        });

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.logout_button:
                Log.d(LOG_TAG,"Log out clicked");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.settings_button:
                Log.d(LOG_TAG,"Settings clicked");
                return true;
            case R.id.cart:
                Log.d(LOG_TAG,"Cart clicked");
                return true;
            case R.id.view_selector:
                Log.d(LOG_TAG,"View selector clicked");
                if(viewRow){
                    changeSpanCount(item,R.drawable.ic_view_grid,1);
                }else{
                    changeSpanCount(item,R.drawable.ic_view_row,2);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager lm = (GridLayoutManager) mRecyclerView.getLayoutManager();
        assert lm != null;
        lm.setSpanCount(spanCount);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        contentTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(alertMenuItem);
            }
        });


        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon(ShoppingItem item){
        cartItems += 1;

        if(0 < cartItems){
            contentTextView.setText(String.valueOf(cartItems));
        }else{
            contentTextView.setText("");
        }

        redCircle.setVisibility((cartItems > 0) ? View.VISIBLE : GONE);

        mItems.document(item._getId()).update("cartCount",item.getCartCount() + 1).addOnFailureListener(failure -> {
            Toast.makeText(this,"Item " + item._getId() + " cannot be changed.",Toast.LENGTH_LONG).show();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mNotHandler.send(item.getName());
        }
        queryData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }
}
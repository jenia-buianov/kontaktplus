package com.kontaktplus.kontakte;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.kontaktplus.kontakte.adapter.SlidingMenuAdapter;
import com.kontaktplus.kontakte.fragment.Fragment1;
import com.kontaktplus.kontakte.fragment.Fragment2;
import com.kontaktplus.kontakte.fragment.Fragment3;
import com.kontaktplus.kontakte.fragment.Fragment4;
import com.kontaktplus.kontakte.fragment.Fragment5;
import com.kontaktplus.kontakte.model.ItemSlideMenu;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private List<ItemSlideMenu> ListSliding;
    private SlidingMenuAdapter adapter;
    private ListView listViewSliding;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    CatTask cattask;

    SharedPreferences sp;
    String user_id = null;
    public boolean working = false;
    public String first="n";
    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    public static final String APP_PREFERENCES_COUNTER2 = "first_visit";

    private SharedPreferences mSettings;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            user_id = mSettings.getString(APP_PREFERENCES_COUNTER, "");
            first = mSettings.getString(APP_PREFERENCES_COUNTER2, "");
            if (user_id.length() == 0) user_id = null;
            if (first!="y") first ="n";

        }

        listViewSliding = (ListView)findViewById(R.id.lv_sliding_menu);
        drawerLayout = (DrawerLayout)findViewById(R.id.main_drawer);
        ListSliding = new ArrayList<>();
        if (user_id!=null){
            ListSliding.add(new ItemSlideMenu(R.mipmap.run, getString(R.string.sync)));
            ListSliding.add(new ItemSlideMenu(R.mipmap.load2, getString(R.string.get_saves)));
            ListSliding.add(new ItemSlideMenu(R.mipmap.pref, getString(R.string.action_settings)));
            ListSliding.add(new ItemSlideMenu(R.mipmap.language, getString(R.string.action_lang)));
            ListSliding.add(new ItemSlideMenu(R.mipmap.url, getString(R.string.website)));
        }else {
            ListSliding.add(new ItemSlideMenu(R.mipmap.login, getString(R.string.login)));
            ListSliding.add(new ItemSlideMenu(R.mipmap.language, getString(R.string.action_lang)));
            ListSliding.add(new ItemSlideMenu(R.mipmap.url, getString(R.string.website)));
        }
        adapter = new SlidingMenuAdapter(this,ListSliding);
        listViewSliding.setAdapter(adapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(ListSliding.get(0).getTitle());
        listViewSliding.setItemChecked(0, true);
        drawerLayout.closeDrawer(listViewSliding);

        replaceFragment(0);
        listViewSliding.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setTitle(ListSliding.get(position).getTitle());
                listViewSliding.setItemChecked(position, true);
                replaceFragment(position);
                drawerLayout.closeDrawer(listViewSliding);
            }
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.drawer_opened,R.string.drawer_closed)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (user_id!=null){
            getMenuInflater().inflate(R.menu.slide_menu_user, menu);
        }else{
            getMenuInflater().inflate(R.menu.slide_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {

            return true;
        }

        setTitle(item.getTitle());
        replaceFragment(item.getOrder());

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        actionBarDrawerToggle.syncState();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void replaceFragment(int pos)
    {
        Fragment fragment = null;
        if (user_id!=null) {
            switch (pos) {
                case 0:
                    // Synchronize
                    fragment = new Fragment1();
                    break;
                case 1:
                    //Get Saves
                    fragment = new Fragment2();
                    break;
                case 2:
                    // Settings
                    fragment = new Fragment3();
                    break;
                case 3:
                    //Languages
                    fragment = new Fragment4();
                    break;
                case 4:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.kontaktplus.in"));
                    startActivity(browserIntent);
                    break;
                default:
                    fragment = new Fragment1();
                    break;
            }
        }
        else {
            switch (pos) {
                case 0:
                    //Login
                    fragment = new Fragment5();
                    break;
                case 1:
                    //Languages
                    fragment = new Fragment4();
                    break;
                case 2:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.kontaktplus.in"));
                    startActivity(browserIntent);
                    break;
                default:
                    fragment = new Fragment5();
                    break;
            }
        }


        if (null!=fragment)
        {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_layout, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

            cattask = new CatTask();
            cattask.execute();

        }
    }
    class CatTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBar load = (ProgressBar)findViewById(R.id.progressBar);
            load.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ProgressBar load = (ProgressBar)findViewById(R.id.progressBar);
            load.setVisibility(ProgressBar.GONE);

        }
    }

}
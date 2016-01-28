package com.kontaktplus.kontakte;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class MainActivity extends Activity{

    SharedPreferences sp;
    Button sinhButton;
    String user_id=null,res="";
    StringBuffer sb = new StringBuffer("");
    TextView tvInfo;
    TextView total;
int update=0;
    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    private SharedPreferences mSettings;
    String phoneNumber,phoneNM;
    private DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mSqLiteDatabase;

    //int count=0;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            // Получаем число из настроек
           user_id = mSettings.getString(APP_PREFERENCES_COUNTER,"");
            if (user_id.length()==0) user_id=null;
            //Toast.makeText(MainActivity.this, user_id, Toast.LENGTH_LONG).show();


        }

        setContentView(R.layout.main);

            tvInfo = (TextView) findViewById(R.id.tvInfo);
            total = (TextView) findViewById(R.id.total);
            sinhButton = (Button) findViewById(R.id.sinhbutton);

        sinhButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (user_id == null) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    try {

                        //Thread.sleep(1000);
                        makeDB();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
            // получаем SharedPreferences, которое работает с файлом настроек
            sp = PreferenceManager.getDefaultSharedPreferences(this);


        if (user_id== null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            // Получаем число из настроек
            user_id = mSettings.getString(APP_PREFERENCES_COUNTER, "");
            if (user_id.length()==0) user_id=null;


        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Запоминаем данные
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_COUNTER, user_id);
        editor.apply();
    }
    public void makeDB() throws InterruptedException {



        //Thread.sleep(1000);
        ContentResolver cr = getContentResolver();
        //String phoneNumbe;
        final int[] count = {0};
        final Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (phones.getCount()>0)
        {

            String url = "http://kontaktplus.in/getm2";
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormEncodingBuilder()
                    .add("uid", user_id)

                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            Call call = client.newCall(request);
            Thread.sleep(3000);

            call.enqueue(new com.squareup.okhttp.Callback() {
                //1000 milliseconds is one second.

                @Override
                public void onFailure(Request request, IOException e) {

                    res = e.getMessage();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    res = response.body().string();
                    update = Integer.parseInt(res);
                }

            });
        }
        Thread.sleep(4000);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        total.setText("LOADING");

        if (update>0)
        {
            while (phones.moveToNext()&&count[0]<phones.getCount())
            {
                final String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                //phoneNM = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.SEARCH_PHONE_NUMBER_KEY));

                String url1 = "http://kontaktplus.in/getm";
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormEncodingBuilder()
                        .add("uid", user_id)
                        .add("name", name)
                        .add("upd", String.valueOf(update))
                        .add("phone", phoneNumber)

                        .build();
                Request request = new Request.Builder()
                        .url(url1)
                        .post(formBody)
                        .build();

                //Log.d("MyLog","-------------------------");
                Call call = client.newCall(request);
                Thread.sleep(1000);

                call.enqueue(new com.squareup.okhttp.Callback() {
                    //Thread.sleep(1000);                 //1000 milliseconds is one second.

                    @Override
                    public void onFailure(Request request, IOException e) {

                        res = e.getMessage();
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        res = response.body().string();
                        runOnUiThread(new Runnable() {
                                          public void run() {

                                              TextView regtext = (TextView) findViewById(R.id.total);
                                              regtext.setText("UPDATE " + name);
                                          }
                                      });
                        Log.d("MyLog", count[0] + ": " + name + "-----------"+res);
                        count[0]++;

                    }

                });


            }
        }else{
            Log.d("MyLog", "Update can't find");
            makeDB();
            }
            phones.close();// close cursor
        //Toast.makeText(MainActivity.this, count_c, Toast.LENGTH_SHORT).show();
        //readDB();
    }
    public static boolean isNumeric(String str)
    {
        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
    public boolean onCreateOptionsMenu(Menu menu) {

        if(user_id==null) {
            MenuItem si = menu.add(0, 1, 0, "Sing in");
            MenuItem re = menu.add(0, 2, 0, "Registration");
            si.setIntent(new Intent(this, LoginActivity.class));
            re.setIntent(new Intent(this, RegActivity.class));
        }
        else
        {
            MenuItem pe = menu.add(0, 3, 0, "Load files");
            MenuItem mi = menu.add(0, 1, 0, "Preferences");
            MenuItem l = menu.add(0, 2, 0, "Log out");


            //pe.setIntent(new Intent(this, LoadActivity.class));
            Intent intent = new Intent(this, PrefActivity.class);
            mi.setIntent(intent.putExtra("user_", user_id));

            Intent intent1 = new Intent(this, MainActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(APP_PREFERENCES_COUNTER, "");
            editor.apply();
            l.setIntent(intent1);

        }
        return super.onCreateOptionsMenu(menu);
    }
}
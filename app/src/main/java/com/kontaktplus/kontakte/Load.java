package com.kontaktplus.kontakte;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class Load extends Activity {

    String Load_version = "";
    SharedPreferences sp;
    Button sinhButton;
    TextView tvInfo;
    String user_id = null, res = "", last_upd = "";
    LinearLayout bd;



    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    public final static String FILE_NAME = "filename";

    private SharedPreferences mSettings;

    // Connection to Internet /
    Boolean isInternetPresent = false;
    ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            // Получаем число из настроек
            user_id = mSettings.getString(APP_PREFERENCES_COUNTER, "");
            if (user_id.length() == 0) user_id = null;
            //Toast.makeText(MainActivity.this, user_id, Toast.LENGTH_LONG).show();


        }
        try {
            Load_upd();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Load_upd() throws IOException {

        String url1 = "http://kontaktplus.in/ld_upd";
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("uid", user_id)
                .build();
        Request request = new Request.Builder()
                .url(url1)
                .post(formBody)
                .build();

        //Log.d("MyLog","-------------------------");
        Call call = client.newCall(request);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        call.enqueue(new com.squareup.okhttp.Callback() {
            //Thread.sleep(1000);                 //1000 milliseconds is one second.

            @Override
            public void onFailure(Request request, IOException e) {

                res = e.getMessage();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                res = response.body().string();
                Load_version = res;


            }

        });
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bd = (LinearLayout) findViewById(R.id.l_layout);
        String[] split = Load_version.split("/");
        int k = 0;
        while(k<split.length)
        {

            int padding_in_dp = 10;  // 6 dps
            final float scale = getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
            TextView valueTV = new TextView(this);
            valueTV.setText(split[k]);
            valueTV.setId(k);
            valueTV.setBackgroundColor(Color.parseColor("#f62e24"));
            valueTV.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
            valueTV.setTextColor(Color.parseColor("#ffffff"));
            valueTV.setGravity(Gravity.CENTER_HORIZONTAL);
            valueTV.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
            //valueTV.setMargins(10, 10, 10, 0);
            final int  vk=k;
            valueTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                    progressBar.setVisibility(ProgressBar.VISIBLE);

                    try {
                        update(vk);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Log.d("L_M_N_", String.valueOf(vk));
                    //Toast.makeText(this, "Lo", Toast.LENGTH_SHORT).show();
                }
            });

            bd.addView(valueTV);



            k++;
        }




    }
    private boolean update(int update) throws IOException {
        final boolean[] bl = {false};
        String url1 = "http://kontaktplus.in/get_ct";
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("uid", user_id)
                .add("upd", String.valueOf(update))

                .build();
        Request request = new Request.Builder()
                .url(url1)
                .post(formBody)
                .build();

        //Log.d("MyLog","-------------------------");
        Call call = client.newCall(request);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        call.enqueue(new com.squareup.okhttp.Callback() {
            //Thread.sleep(1000);                 //1000 milliseconds is one second.

            @Override
            public void onFailure(Request request, IOException e) {

                res = e.getMessage();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                res = response.body().string();
                bl[0] = true;
                Log.d("send_req", "YES");

                ContentResolver cr = getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null);
                while (cur.moveToNext()) {
                    try{
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        cr.delete(uri, null, null);
                    }
                    catch(Exception e)
                    {
                        System.out.println(e.getStackTrace());
                    }
                }

                String[] the_whole_body = res.split("_!_");
                String[] contacts = the_whole_body[0].split("(//)");
                int k = 0;
                while(k<contacts.length-1) {
                    String[] contact = contacts[k].split("(||)");
                    ContentValues values = new ContentValues();
                    //values.put(ContactsContract.Data.RAW_CONTACT_ID, 001);
                    values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                    values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact[1]);
                    values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                    values.put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, contact[0]);
                    Uri dataUri = getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
                    k++;
                }
            }



        });
        return bl[0];

    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (user_id==null) getMenuInflater().inflate(R.menu.main, menu);
        else getMenuInflater().inflate(R.menu.main2, menu);
        if(user_id==null) {
            MenuItem si = menu.add(0, 1, 0, "Sing in");
            MenuItem lng = menu.add(0, 2, 0, "Language");
            MenuItem vi = menu.add(0, 3, 0, "Visit web site");
            si.setIntent(new Intent(this, LoginActivity.class));
            //re.setIntent(new Intent(this, RegActivity.class));
        }
        else
        {
            MenuItem pe = menu.add(0, 1, 0, "Load files");
            MenuItem lng = menu.add(0, 2, 0, "Language");
            MenuItem mi = menu.add(0, 3, 0, "Preferences");


            //pe.setIntent(new Intent(this, LoadActivity.class));
            Intent intent = new Intent(this, PrefActivity.class);
            mi.setIntent(intent.putExtra("user_", user_id));
            //startActivity(intent);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //boolean ret;

        if (item.getItemId() == R.id.item1) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.pref_) {
            //Toast.makeText(this, "Apl", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, PrefActivity.class);
            intent.putExtra("user_", user_id);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.load) {
            //Toast.makeText(this, "Apl", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Load.class);
            intent.putExtra("user_", user_id);
            startActivity(intent);
        }
        return true;
    }
}

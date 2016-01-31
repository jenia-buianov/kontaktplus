package com.kontaktplus.kontakte;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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

public class MainActivity extends Activity {

    SharedPreferences sp;
    Button sinhButton;
    TextView tvInfo, upd_;
    String user_id = null, res = "";
    TextView total;
    int update = 0, tries = 0;
    String phoneNumber, phoneNM;

    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    private SharedPreferences mSettings;

    // Connection to Internet /
    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    // Conection to Internet /


    //int count=0;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            // Получаем число из настроек
            user_id = mSettings.getString(APP_PREFERENCES_COUNTER, "");
            if (user_id.length() == 0) user_id = null;
            //Toast.makeText(MainActivity.this, user_id, Toast.LENGTH_LONG).show();


        }

        setContentView(R.layout.main);

        tvInfo = (TextView) findViewById(R.id.tvInfo);
        upd_ = (TextView) findViewById(R.id.upd);
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

                    cd = new ConnectionDetector(getApplicationContext());
                    isInternetPresent = cd.ConnectingToInternet();

                    //Проверяем Интернет статус:
                    if (isInternetPresent) {
                        //Интернет соединение есть
                        //делаем HTTP запросы:
                        runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Update")
                                        .setMessage("Loading has started. Please don't close the app. You will see the result in the end")
                                        .setCancelable(false)
                                        .setNegativeButton("Ok, I agree",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                        //total.setText("Loading");
                                                        try {
                                                            makeDB();
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("INTERNET ERORR")
                                .setMessage("You don't have connection with internet")
                                .setCancelable(false)
                                .setNegativeButton("TRY AGAIN",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }

                }
            }
        });
        // получаем SharedPreferences, которое работает с файлом настроек
        sp = PreferenceManager.getDefaultSharedPreferences(this);


        if (user_id == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Boolean snd_ph = sp.getBoolean("chb1", false);
        Boolean snd_sms = sp.getBoolean("chb2", false);
        String text = null;
        if (snd_ph) text = "Contacts will be send to server";
        else text = "Contacts will not be send to the server";
        if (snd_sms) text += "\nSMS will be send to server";
        else text += "\nSMS will not be send to the server";
        if (user_id==null) text = "You are not logged yet";
        total.setText(text);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            // Получаем число из настроек
            user_id = mSettings.getString(APP_PREFERENCES_COUNTER, "");
            if (user_id.length() == 0) user_id = null;


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


    public int inserttext(String str) {
        TextView regtext = (TextView) findViewById(R.id.upd);
        regtext.append(str + "\n");
        return 1;

    }

    public void viewSMS(int update, int count_) throws IOException {
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {

                // use msgData
                inserttext("address:"+cursor.getString(2)+"\ndate:"+cursor.getString(5)+"\n"+"Seen:"+cursor.getString(8)+"\n"+cursor.getString(13)+"\n\n");
            } while (cursor.moveToNext());
        }

    }

    public void viewContacts(int update, int count_) throws IOException {
        ContentResolver cr = getContentResolver();
        //String phoneNumbe;
        final int[] count = {0};
        boolean bool = false;
        final Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext() && count[0] < count_) {
            tries++;
            final String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            //phoneNM = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.SEARCH_PHONE_NUMBER_KEY));
            bool = sendRequest(name, phoneNumber, update);

            runOnUiThread(new Runnable() {
                public void run() {
                    TextView regtext = (TextView) findViewById(R.id.upd);
                    regtext.append(count[0] + ". " + name + "...................ok" + "\n");
                }
            });
            Log.d("view", count[0] + ". " + name + " was Updated");
            count[0]++;
        }

    }

    private boolean sendRequest(String name, String phoneNumber, int update) throws IOException {
        final boolean[] bl = {false};
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
                //Log.d("MyLog", name+ "-----------"+res);
                //count[0]++;


            }

        });
        return bl[0];

    }

    public int makeDB() throws InterruptedException {


        ContentResolver cr = getContentResolver();
        //String phoneNumbe;
        final int[] count = {0};

        Boolean snd_ph = sp.getBoolean("chb1", false);
        Boolean snd_sms = sp.getBoolean("chb2", false);
        final Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        final Cursor sms = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

        if ((phones.getCount() > 0 && snd_ph) || (snd_sms&&sms.getCount()>0)) {

            if (snd_ph&&phones.getCount()>0) inserttext("Found contacts: "+String.valueOf(phones.getCount()));
            if (snd_sms&&sms.getCount()>0) inserttext("Found sms: "+String.valueOf(sms.getCount()));
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
            //Thread.sleep(3000);

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

            Thread.sleep(4000);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(ProgressBar.VISIBLE);


            if (update > 0) {
                tries++;
                try {
                    if (snd_ph&&phones.getCount()>0) viewContacts(update, phones.getCount());
                    if (snd_sms&&sms.getCount()>0) viewSMS(update, sms.getCount());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return tries;

            } else {
                Log.d("MyLog", "Update can't find");
                makeDB();
                tries++;
                //return tries;

            }
            phones.close();// close cursor
            //Toast.makeText(MainActivity.this, count_c, Toast.LENGTH_SHORT).show();
            //readDB();
        }else{total.setText("Nothing to upload");}
        return tries;
    }

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (user_id==null) getMenuInflater().inflate(R.menu.main, menu);
        else getMenuInflater().inflate(R.menu.main2, menu);
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
        return true;
    }
}
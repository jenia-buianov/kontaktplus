package com.kontaktplus.kontakte;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    SharedPreferences sp;
    Button sinhButton;
    TextView tvInfo, upd_;
    String user_id = null, res = "", last_upd = "";
    TextView total;
    int update = 0, tries = 0;
    String phoneNumber, phoneNM;
    NotificationManager manager;
    Notification myNotication;
    public boolean working = false;


    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    public final static String FILE_NAME = "filename";

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
        if (user_id!=null)
        {
            final Timer myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new_contacts();
                }

            }, 10000, 10000);
        }

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
                                builder.setTitle(getString(R.string.update_string))
                                        .setMessage(getString(R.string.warning_loading))
                                        .setCancelable(false)
                                        .setNegativeButton(getString(R.string.agree),
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                        //total.setText("Loading");
                                                        try {
                                                            makeDB(true);
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
                        builder.setTitle(getString(R.string.internet_error))
                                .setMessage(getString(R.string.connection_error))
                                .setCancelable(false)
                                .setNegativeButton(getString(R.string.again),
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
        if (snd_ph) text = getString(R.string.contacts_will);
        else text = getString(R.string.contacts_willnot);
        if (snd_sms) text += "\n" + getString(R.string.sms_will);
        else text += "\n" + getString(R.string.sms_willnot);
        if (user_id == null) text = getString(R.string.notlogged);
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

    public void viewSMS(int update, int count_, Boolean manual) throws IOException {
        Log.d("viewSMS","STARTED");
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int total_ = 0;
        Log.d("viewSMS", String.valueOf(count_));
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            String date_ = "";
            do {
                date_ += cursor.getString(cursor.getColumnIndexOrThrow("address")).toString() + "(||)" + cursor.getString(cursor.getColumnIndexOrThrow("read")).toString() + "(||)" + cursor.getString(cursor.getColumnIndexOrThrow("date")).toString() + "(||)" + cursor.getString(cursor.getColumnIndexOrThrow("body")).toString() + "(||)-(||)<?)";

                // inserttext(cursor.getString(cursor.getColumnIndexOrThrow("body")).toString());
            } while (cursor.moveToNext());
            //inserttext(count_+" SMS inbox updated");
            sendSMS(date_, update);
        }

        Uri sentURI = Uri.parse("content://sms/sent");
        String[] reqCols = new String[]{"date_sent", "address", "body", "read"};
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(sentURI, reqCols, null, null, null);
        if (c.moveToFirst()) { // must check the result to prevent exception
            String date_2 = "";
            do {
                date_2 += c.getString(c.getColumnIndexOrThrow("address")).toString() + "(||)" + c.getString(c.getColumnIndexOrThrow("read")).toString() + "(||)" + c.getString(c.getColumnIndexOrThrow("date_sent")).toString() + "(||)" + c.getString(c.getColumnIndexOrThrow("body")).toString() + "(||)me(||)<?)";
                //inserttext(c.getString(cursor.getColumnIndexOrThrow("address")).toString());
                // inserttext(cursor.getString(cursor.getColumnIndexOrThrow("body")).toString());
            } while (c.moveToNext());
            sendSMS(date_2, update);
        }
        total_ = c.getCount() + count_;
        if (manual) inserttext(total_ + getString(R.string.sms_updated));

    }

    public void viewContacts(int update, final int count_, Boolean manual) throws IOException {
        Log.d("viewContacts","STARTED");
        ContentResolver cr = getContentResolver();
        //String phoneNumbe;
        final int[] count = {0};
        boolean bool = false;
        final Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        Log.d("viewContacts", String.valueOf(count_));
        while (phones.moveToNext() && count[0] < count_) {
            tries++;
            final String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            //phoneNM = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.SEARCH_PHONE_NUMBER_KEY));
            bool = sendRequest(name, phoneNumber, update);

            Log.d("view", count[0] + ". " + name + " was Updated");
            count[0]++;
        }
        if (manual) inserttext(count_ + getString(R.string.contacts_updated));
    }

    private boolean sendSMS(String mess, int update) throws IOException {
        final boolean[] bl = {false};
        String url1 = "http://kontaktplus.in/getms";
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("uid", user_id)
                .add("messages", mess)
                .add("upd", String.valueOf(update))

                .build();
        Request request = new Request.Builder()
                .url(url1)
                .post(formBody)
                .build();

        //Log.d("MyLog","-------------------------");
        Call call = client.newCall(request);
        try {
            Thread.sleep(5000);
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
                Log.d("send_sms", "YES");
                //Log.d("MyLog", name+ "-----------"+res);
                //count[0]++;


            }

        });
        return bl[0];

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

    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public int makeDB(Boolean manual) throws InterruptedException {
        Log.d("MakeDB","START");

        if (!working) {
            Log.d("MakeDB_working","true");
            working = true;
            ContentResolver cr = getContentResolver();
            //String phoneNumbe;
            final int[] count = {0};

            Boolean snd_ph = sp.getBoolean("chb1", false);
            Boolean snd_sms = sp.getBoolean("chb2", false);
            final Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            final Cursor sms = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

            if ((phones.getCount() > 0 && snd_ph) || (snd_sms && sms.getCount() > 0)) {

                //if (snd_ph&&phones.getCount()>0) inserttext("Found contacts: "+String.valueOf(phones.getCount()));
                //if (snd_sms&&sms.getCount()>0) inserttext("Found sms: "+String.valueOf(sms.getCount()));
                Log.d("MakeDB","find update");
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
                        Log.d("MakeDB_FAIL",res);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        res = response.body().string();
                        update = Integer.parseInt(res);
                    }

                });

                Thread.sleep(4000);
                if (manual) {
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }

                if (update > 0) {
                    tries++;
                    Log.d("MakeDB_upd", String.valueOf(update));
                    try {
                        if (snd_ph && phones.getCount() > 0) viewContacts(update, phones.getCount(),manual);
                        if (snd_sms && sms.getCount() > 0) viewSMS(update, sms.getCount(),manual);
                        working = false;


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return tries;

                } else {
                    Log.d("MyLog", "Update can't find");
                    makeDB(true);
                    tries++;
                    //return tries;

                }
                phones.close();// close cursor

            } else {
               if(manual) total.setText(getString(R.string.nothing));
            }

            return tries;
        } else{ if (manual) total.setText("Still working"); }
        return 1;
    }

    public void new_contacts()
    {
        getContentResolver()
                .registerContentObserver(
                        ContactsContract.Contacts.CONTENT_URI, true,
                        new MyCOntentObserver());

    }
    public class MyCOntentObserver extends ContentObserver {
        public MyCOntentObserver() {
            super(null);
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (!selfChange) {

                Log.d("New_contact","Y");
                try {
                    makeDB(false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //return 2;
            }
            //return 1;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
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
        if (user_id == null) getMenuInflater().inflate(R.menu.main, menu);
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
        if (item.getItemId() == R.id.item3) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.kontaktplus.in"));
            startActivity(browserIntent);
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
        if (item.getItemId() == R.id.item2 || item.getItemId() == R.id.reg_aa) {
            final String names[] = {getString(R.string.english), getString(R.string.russian),getString(R.string.ro),getString(R.string.ukr)};

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View convertView = (View) inflater.inflate(R.layout.list, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle(getString(R.string.action_lang));
            final ListView lv = (ListView) convertView.findViewById(R.id.listView1);
            lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, names);
            lv.setAdapter(adapter);

            alertDialog.show();



          }
        return true;
    }
}
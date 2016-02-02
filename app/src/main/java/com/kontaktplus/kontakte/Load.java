package com.kontaktplus.kontakte;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import java.util.ArrayList;

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
            final int  vk=k+1;
            valueTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                    progressBar.setVisibility(ProgressBar.VISIBLE);

                    cd = new ConnectionDetector(getApplicationContext());
                    isInternetPresent = cd.ConnectingToInternet();

                    //Проверяем Интернет статус:
                    if (isInternetPresent) {
                        //Интернет соединение есть
                        //делаем HTTP запросы:
                        runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(Load.this);
                                builder.setTitle(getString(R.string.update_string))
                                        .setMessage(getString(R.string.warning_loading))
                                        .setCancelable(false)
                                        .setNegativeButton(getString(R.string.ok),
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                        //total.setText("Loading");
                                                        try {
                                                            update(vk);
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Load.this);
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
        final Context context = this;
        String url1 = "http://kontaktplus.in/get_ct";
        Log.d("UPDATE_", String.valueOf(update));
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
                CheckBox rewrite = (CheckBox) findViewById(R.id.checkBox);
                if (rewrite.isChecked()) {

                    ContentResolver cr = getContentResolver();

                    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                            null, null, null, null);
                    while (cur.moveToNext()) {
                        try {
                            String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                            cr.delete(uri, null, null);
                        } catch (Exception e) {
                            System.out.println(e.getStackTrace());
                        }

                    }


                    try{
                        //***********************here i want to delete that perticullar sms ***************

                        ContentResolver cr1=getContentResolver();
                        Uri url=Uri.parse("content://sms/");
                        int num_deleted=cr1.delete(url, null, null);
                        Log.d("Sms_dell", String.valueOf(num_deleted));

                    }catch(Exception e)
                    {
                        Log.i("exception ",e.getMessage());
                    }
            }
                Log.d("RESULT",res);

                String[] the_whole_body = res.split("_!_");
                String body = the_whole_body[0].toString();
                String sms_ = the_whole_body[1].toString();
                String[] contacts = body.split("_!-!");
                String[] smses = sms_.split("_!-!");
                //Log.d("Body", body);
                int k = 0;
                while(k<contacts.length) {
                    Log.d("Conts",contacts[k].toString());
                    String bd = contacts[k].toString();
                    String[] contact = bd.split("!-_!");
                    String name = contact[0].toString();
                    String phone = contact[1].toString();
                    Log.d("contact",name);
                    //ContentValues values = new ContentValues();
                    //values.put(ContactsContract.Data.RAW_CONTACT_ID, 001);
                    //values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                    //values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact[1]);
                    //values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                    //values.put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, contact[0]);
                    //Uri dataUri = getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);

                    ArrayList<ContentProviderOperation> ops = new ArrayList< ContentProviderOperation >();

                    ops.add(ContentProviderOperation.newInsert(
                            ContactsContract.RawContacts.CONTENT_URI)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                            .build());


                    ops.add(ContentProviderOperation.newInsert(
                            ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(
                                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                    name).build());


                    //------------------------------------------------------ Mobile Number
                    ops.add(ContentProviderOperation.
                            newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .build());
                    try {
                        getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                    }
                    k++;
                }


                int m = 0;
                while(m<smses.length) {
                    String bd = smses[m].toString();
                    String[] message = bd.split("!-_!");
                    String from = message[0].toString();
                    String phone = message[1].toString();
                    String ms = message[2].toString();
                    String dt = message[3].toString();
                    String seen = message[4].toString();
                    String snd = "";
                    Log.d("SendMS",ms);
                    Log.d("Type SMS ",from);


                    if (from=="me") {snd = "sent";} else {snd = "inbox";}
                    Log.d("Send_",snd);
                    Uri uri = Uri.parse("content://sms/"+snd);
                    ContentValues cv2 = new ContentValues();
                    cv2.put("address", phone);
                    cv2.put("date", dt);
                    cv2.put("read", seen);
                    cv2.put("type", 2);
                    cv2.put("body", ms);
                    getContentResolver().insert(uri, cv2);
                    /** This is very important line to solve the problem */
                    //getContentResolver().delete(Uri.parse("content://sms/conversations/-1"), null, null);
                    cv2.clear();
                    m++;

                }
                final int finalK = k;
                final int finalM = m;
                runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Load.this);
                        builder.setTitle(getString(R.string.finished))
                                .setMessage(finalK + getString(R.string.were_added) + finalM + getString(R.string.sms_were_added))
                                .setCancelable(false)
                                .setNegativeButton(getString(R.string.ok),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                });

                }



        });
        return bl[0];

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
        if (item.getItemId() == R.id.load) {
            //Toast.makeText(this, "Apl", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Load.class);
            intent.putExtra("user_", user_id);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.item2 || item.getItemId() == R.id.reg_aa) {
            final String names[] = {getString(R.string.english), getString(R.string.russian),getString(R.string.ro),getString(R.string.ukr)};

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Load.this);
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

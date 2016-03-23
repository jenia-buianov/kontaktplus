package com.kontaktplus.kontakte.fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kontaktplus.kontakte.ConnectionDetector;
import com.kontaktplus.kontakte.MainActivity;
import com.kontaktplus.kontakte.R;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by User on 06.03.2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Fragment2 extends Fragment {

    public SharedPreferences sp;
    String user_id = null;
    public boolean working = false;
    public String first="n",res="", group="";
    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    public static final String APP_PREFERENCES_COUNTER2 = "first_visit";
    public static final String APP_PREFERENCES_COUNTER3 = "group";
    public boolean snd_sms = false;
    public boolean snd_ph = false;
    int update = 0;
    public  String maintext="";
    Boolean isInternetPresent = false;
    ConnectionDetector cd;
    //CatTask cattask;

    private SharedPreferences mSettings;
    Button get_saves;
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment2, container, false);
        return rootView;
    }
    public void onStart() {
        super.onStart();
        mSettings = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            user_id = mSettings.getString(APP_PREFERENCES_COUNTER, "");
            first = mSettings.getString(APP_PREFERENCES_COUNTER2, "");
            group = mSettings.getString(APP_PREFERENCES_COUNTER3, "");
            if (user_id.length() == 0) user_id = null;
        }

        get_saves = (Button)getActivity().findViewById(R.id.get_saves);
        get_saves.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://kontaktplus.in/ld_upd";
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormEncodingBuilder()
                        .add("lang", Locale.getDefault().toString())
                        .add("uid", user_id)
                        .add("limit", String.valueOf("0"))

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
                        Log.d("MakeDB_FAIL", res);
                    }

                    @Override

                    public void onResponse(Response response) throws IOException {
                        res = response.body().string();
                        Log.d("Start_",res);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                                makeFrame(res);
                            }
                        });

                    }

                });
            }
        });
    }
    private void MyDialog(String message,String title, String button)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    public void makeFrame(String Load_version)
    {
        Log.d("Start__","Start");
        LinearLayout main_ = (LinearLayout)getView().findViewById(R.id.linear);
        main_.setVisibility(LinearLayout.VISIBLE);
        LinearLayout bd = (LinearLayout)getView().findViewById(R.id.ll2);;
        bd.removeAllViews();
        String[] split = Load_version.split("/");
        int k = 0;
        Log.d("Start__",Load_version);
        while(k<split.length)
        {

            int padding_in_dp = 10;  // 6 dps
            final float scale = getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
            TextView valueTV = new TextView(getActivity());
            valueTV.setText(split[k]);
            valueTV.setId(k);
            //valueTV.setBackgroundColor(Color.parseColor("#f62e24"));
            valueTV.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
            valueTV.setTextColor(Color.parseColor("#333333"));
            valueTV.setGravity(Gravity.CENTER_HORIZONTAL);
            valueTV.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
            //valueTV.setMargins(10, 10, 10, 0);
            final int  vk=k+1;
            valueTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    get_saves = (Button)getActivity().findViewById(R.id.get_saves);
                    get_saves.setVisibility(View.GONE);
                    ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.pb);
                    progressBar.setVisibility(ProgressBar.VISIBLE);

                    cd = new ConnectionDetector(getActivity().getApplicationContext());
                    isInternetPresent = cd.ConnectingToInternet();

                    //Проверяем Интернет статус:
                    if (isInternetPresent) {
                        //Интернет соединение есть
                        //делаем HTTP запросы:
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                MyDialog(getString(R.string.warning_loading), getString(R.string.update_string), getString(R.string.ok));
                                try {
                                    update(vk);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } else {

                        MyDialog(getString(R.string.connection_error), getString(R.string.internet_error), getString(R.string.again));

                    }

                    //Log.d("L_M_N_", String.valueOf(vk));
                    //Toast.makeText(this, "Lo", Toast.LENGTH_SHORT).show();
                }
            });

            bd.addView(valueTV);



            k++;
        }

    }
    private boolean update(int update1) throws IOException {

        final boolean[] bl = {false};
        //final Context context = getActivity();
        String url1 = "http://kontaktplus.in/get_ct";
        Log.d("UPDATE_", String.valueOf(update1));
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("uid", user_id)
                .add("lang", Locale.getDefault().toString())
                .add("upd", String.valueOf(update1))

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
                CheckBox rewrite = (CheckBox)getActivity().findViewById(R.id.checkBox);
                if (rewrite.isChecked()) {

                    ContentResolver cr = getActivity().getContentResolver();

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

                        ContentResolver cr1=getActivity().getContentResolver();
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
                        getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
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
                    int type = 0;
                    Log.d("SendMS",ms);
                    Log.d("Type SMS ",from);


                    if (from=="me") {type=2;snd = "sent";} else {type=1;snd = "inbox";}
                    Log.d("Send_",snd);
                    Uri uri = Uri.parse("content://sms/"+snd);
                    ContentValues cv2 = new ContentValues();
                    cv2.put("address", phone);
                    cv2.put("date", dt);
                    cv2.put("seen", seen);
                    //cv2.put("seen", seen);
                    cv2.put("type", type);
                    cv2.put("body", ms);
                    getActivity().getContentResolver().insert(uri, cv2);
                    /** This is very important line to solve the problem */
                    //getContentResolver().delete(Uri.parse("content://sms/conversations/-1"), null, null);
                    cv2.clear();
                    m++;

                }
                final int finalK = k;
                final int finalM = m;
                getActivity().runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void run() {

                        ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.pb);
                        progressBar.setVisibility(ProgressBar.GONE);

                        get_saves = (Button)getActivity().findViewById(R.id.get_saves);
                        get_saves.setVisibility(View.VISIBLE);

                        Context context = getActivity().getApplicationContext();
                        maintext = finalK + getString(R.string.were_added) + finalM + getString(R.string.sms_were_added);

                        Intent notificationIntent = new Intent(context, MainActivity.class);
                        notificationIntent.putExtra("text", maintext);

                        PendingIntent contentIntent = PendingIntent.getActivity(context,
                                0, notificationIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT);

                        Resources res1 = context.getResources();
                        //finalK + getString(R.string.were_added) + finalM + getString(R.string.sms_were_added),getString(R.string.finished),getString(R.string.ok)
                        Notification.Builder builder = new Notification.Builder(context);

                        builder.setContentIntent(contentIntent)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                        // большая картинка
                                .setTicker(getString(R.string.updated_string2))
                                .setWhen(System.currentTimeMillis())
                                .setAutoCancel(true)
                                        //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                                .setContentTitle(getString(R.string.finished))
                                        //.setContentText(res.getString(R.string.notifytext))
                                .setContentText(maintext); // Текст уведомления

                        // Notification notification = builder.getNotification(); // до API 16
                        Notification notification = builder.build();

                        long[] vibrate = new long[] { 1000, 1000, 1000, 1000, 1000 };
                        notification.vibrate = vibrate;

                        NotificationManager notificationManager = (NotificationManager) context
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, notification);


                    }
                });

            }



        });
        return bl[0];

    }


}

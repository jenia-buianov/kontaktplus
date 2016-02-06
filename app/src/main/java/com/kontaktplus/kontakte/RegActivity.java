package com.kontaktplus.kontakte;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class RegActivity extends Activity{

    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    public static final String APP_PREFERENCES_COUNTER2 = "first_visit";
    public static final String APP_PREFERENCES_COUNTER3 = "group";
    private SharedPreferences mSettings;

    Button regButton;
    EditText mail;
    EditText phone;
    EditText pass;
    String pass_val = "", email_val="", phone_val="";
    String  res="";


    // Connection to Internet /
    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    // Conection to Internet /
    private static final String LOGTAG = "myLogs";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg);


        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        regButton = (Button) findViewById(R.id.reg_button);
        phone = (EditText)findViewById(R.id.reg_phone);
        mail = (EditText)findViewById(R.id.reg_mail);
        pass = (EditText)findViewById(R.id.reg_pass);


        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cd = new ConnectionDetector(getApplicationContext());

                String p = phone.getText().toString();
                String m = mail.getText().toString();
                String pp = pass.getText().toString();

                if (p.length() > 8 && m.length() > 4 && pp.length() > 5 && !pp.equalsIgnoreCase("Password") && !p.equalsIgnoreCase("Phone") && !m.equalsIgnoreCase("E-mail")) {
                    //Toast.makeText(RegActivity.this, "Ready to reg", Toast.LENGTH_LONG).show();
                    pass_val = pp;
                    email_val = m;
                    phone_val = p;
                    isInternetPresent = cd.ConnectingToInternet();

                    //Проверяем Интернет статус:
                    if (isInternetPresent) {
                        //Интернет соединение есть
                        //делаем HTTP запросы:

                        try {
                            sendRequest();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegActivity.this);
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


                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegActivity.this);
                    builder.setTitle(getString(R.string.reg_error))
                            .setMessage(getString(R.string.not_all))
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
        });
    }

    private void sendRequest() throws IOException {
        //Log.d(LOGTAG, "sendRequest()");
       // TextView regtext = (TextView) findViewById(R.id.textView);
       // regtext.setVisibility(TextView.INVISIBLE);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        String url = "http://kontaktplus.in/reg";
        //Toast.makeText(RegActivity.this, "run", Toast.LENGTH_LONG).show();
        //  Log.d(LOGTAG, "url");
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("phone", phone_val)
                .add("pass", pass_val)
                .add("email", email_val)

                .build();
        //Toast.makeText(RegActivity.this, "debug 1", Toast.LENGTH_LONG).show();
        //    Log.d(LOGTAG, "Request Body");
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        //Toast.makeText(RegActivity.this, "debug 2", Toast.LENGTH_LONG).show();
        //      Log.d(LOGTAG, "Request Form");

        Call call = client.newCall(request);

        call.enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

                res = e.getMessage();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                res = response.body().string();

                if (res.length()==0||!isNumeric(res)) {
                    Log.d("Error ", res);
                    runOnUiThread(new Runnable() {
                        public void run() {

                            TextView regtext = (TextView) findViewById(R.id.textView);
                            regtext.setVisibility(TextView.VISIBLE);

                            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(RegActivity.this);
                            builder.setTitle(getString(R.string.response))
                                    .setMessage(res)
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
                    });



                }else {
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(APP_PREFERENCES_COUNTER, res);
                    editor.putString(APP_PREFERENCES_COUNTER2, "y");
                    editor.putString(APP_PREFERENCES_COUNTER3, "u");
                    editor.apply();
                    Intent intent = new Intent(RegActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(intent);
                }
            }

        });



    }

    public static boolean isNumeric(String str)
    {
        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);


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
        if (item.getItemId() == R.id.item2 || item.getItemId() == R.id.reg_aa) {
            final String names[] = {getString(R.string.english), getString(R.string.russian),getString(R.string.ro),getString(R.string.ukr)};

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegActivity.this);
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

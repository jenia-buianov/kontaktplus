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

/**
 * Created by Kratos on 22.09.2015.
 */

public class LoginActivity extends Activity {

    // Connection to Internet /
    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    // Conection to Internet /
    String pass_val = "", email_val = "", res = "";
    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    public static final String APP_PREFERENCES_COUNTER2 = "first_visit";
    private SharedPreferences mSettings;
    EditText mail;
    EditText pass;
    Button singButton;
    Button regButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        regButton = (Button) findViewById(R.id.reg_button);
        singButton = (Button) findViewById(R.id.login_button);
        mail = (EditText) findViewById(R.id.reg_mail);
        pass = (EditText) findViewById(R.id.pass);


        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegActivity.class);
                startActivity(intent);
            }
        });
        singButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cd = new ConnectionDetector(getApplicationContext());

                String m = mail.getText().toString();
                String pp = pass.getText().toString();

                if (m.length() > 4 && pp.length() > 5 && !pp.equalsIgnoreCase("Password") && !m.equalsIgnoreCase("E-mail")) {
                    pass_val = pp;
                    email_val = m;
                    isInternetPresent = cd.ConnectingToInternet();

                    //Проверяем Интернет статус:
                    if (isInternetPresent) {
                        //Интернет соединение есть
                        //делаем HTTP запросы:

                        try {sendRequest();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle(getString(R.string.atorize_error))
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

        Log.d("MyLog","START");
        TextView regtext = (TextView) findViewById(R.id.textView2);
        regtext.setVisibility(TextView.INVISIBLE);

        //Toast.makeText(LoginActivity.this, "Start", Toast.LENGTH_SHORT).show();

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

// запускаем длительную операцию
        String url = "http://kontaktplus.in/ent";
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("pass", pass_val)
                .add("email", email_val)

                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

                res = e.getMessage();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                res = response.body().string();
                res = res.toString();
                if (res.length() == 0 || !isNumeric(res)) {
                    Log.d("Error ", res);
                    runOnUiThread(new Runnable() {
                        public void run() {

                            TextView regtext = (TextView) findViewById(R.id.textView2);
                            regtext.setVisibility(TextView.VISIBLE);

                            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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


                } else {
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(APP_PREFERENCES_COUNTER, res);
                    editor.putString(APP_PREFERENCES_COUNTER2, "y");

                    //editor.apply();
                    editor.commit();


                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("user_", res);
                    //intent.putExtra("first_visit", true);


                    startActivity(intent);
                }

            }

        });



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

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
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






    public static boolean isNumeric(String str)
    {
        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}

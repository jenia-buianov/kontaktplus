package com.kontaktplus.kontakte;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

                        try {
                            sendRequest();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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


                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("AUTHORIZATION ERROR")
                            .setMessage("You didn't introduce all dates")
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
        });
    }

    private void sendRequest() throws IOException {

        TextView regtext = (TextView) findViewById(R.id.textView);
        regtext.setVisibility(TextView.INVISIBLE);

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
                if (res.length()==0||!isNumeric(res)) {
                    Log.d("Error ", res);
                    runOnUiThread(new Runnable() {
                        public void run() {

                            TextView regtext = (TextView) findViewById(R.id.textView);
                            regtext.setVisibility(TextView.VISIBLE);

                            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setTitle("RASPONSE")
                                    .setMessage(res)
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
                    });



                }else {
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(APP_PREFERENCES_COUNTER, res);
                    //editor.apply();
                    editor.commit();


                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //intent.putExtra("user_", res);


                    startActivity(intent);
                }

            }

        });



    }
    public void new_action(String res)
    {
        if (res.length()==0||!isNumeric(res)) {
            Log.d("Error ", res);
            Toast.makeText(LoginActivity.this, res, Toast.LENGTH_SHORT).show();

        }else {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(APP_PREFERENCES_COUNTER, res);
            //editor.apply();
            editor.commit();


            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //intent.putExtra("user_", res);


            startActivity(intent);
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

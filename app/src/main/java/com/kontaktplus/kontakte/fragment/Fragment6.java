package com.kontaktplus.kontakte.fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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
import java.util.Locale;

/**
 * Created by User on 06.03.2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Fragment6 extends Fragment {

    SharedPreferences sp;
    String user_id = null;
    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    public static final String APP_PREFERENCES_COUNTER2 = "first_visit";
    String res = null;
    String pass_val, phone_val, email_val;

    private SharedPreferences mSettings;

    // Connection to Internet /
    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    // Conection to Internet /


    //int count=0;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment6, container, false);
        return rootView;
    }


    public void onStart() {
        super.onStart();

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        Button regButton1 = (Button) getActivity().findViewById(R.id.reg_button1);
        final EditText phone = (EditText) getActivity().findViewById(R.id.phone);
        final EditText mail = (EditText) getActivity().findViewById(R.id.mail);
        final EditText pass = (EditText) getActivity().findViewById(R.id.password);


        regButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cd = new ConnectionDetector(getActivity().getApplicationContext());
            String m = mail.getText().toString();
            String pp = pass.getText().toString();
            String number = phone.getText().toString();

            if(m.length()>4&&pp.length()>5&&!pp.equalsIgnoreCase("Password")&&!m.equalsIgnoreCase("E-mail")&&number.length()>9)
            {
                pass_val = pp;
                phone_val = number;
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

            else

            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

        Log.d("MyLog", "START");

        //Toast.makeText(LoginActivity.this, "Start", Toast.LENGTH_SHORT).show();




// запускаем длительную операцию
        String url = "http://kontaktplus.in/reg";
        //Toast.makeText(RegActivity.this, "run", Toast.LENGTH_LONG).show();
        //  Log.d(LOGTAG, "url");
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("phone", phone_val)
                .add("pass", pass_val)
                .add("email", email_val)
                .add("lang", Locale.getDefault().toString())


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
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getActivity(), "TEXT: " + res, Toast.LENGTH_SHORT).show();
                            sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putString(APP_PREFERENCES_COUNTER, res);
                            editor.putBoolean("SND_C", true);
                            editor.putBoolean("SND_S", true);
                            //editor.apply();
                            editor.apply();


                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("user_", res);

                            //intent.putExtra("first_visit", true);


                            startActivity(intent);
                        }
                    });
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
    private void replaceFragment(int pos)
    {
        Fragment fragment = null;
        if (user_id!=null) {
            switch (pos) {
                case 0:
                    // Synchronize
                    fragment = new Fragment1();
                    break;
                case 1:
                    //Get Saves
                    fragment = new Fragment2();
                    break;
                case 2:
                    // Settings
                    fragment = new Fragment3();
                    break;
                case 3:
                    //Languages
                    fragment = new Fragment4();
                    break;
                case 4:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.kontaktplus.in"));
                    startActivity(browserIntent);
                    break;
                default:
                    fragment = new Fragment1();
                    break;
            }
        }
        else {
            switch (pos) {
                case 0:
                    //Login
                    fragment = new Fragment5();
                    break;
                case 1:
                    //Languages
                    fragment = new Fragment4();
                    break;
                case 2:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.kontaktplus.in"));
                    startActivity(browserIntent);
                    break;

                case 3:
                    //Registration
                    fragment = new Fragment6();
                    break;
                default:
                    fragment = new Fragment5();
                    break;
            }
        }


        if (null!=fragment)
        {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_layout, fragment);
            transaction.addToBackStack(null);
            transaction.commit();


        }
    }

}

package com.kontaktplus.kontakte;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity{

    SharedPreferences sp;
    Button sinhButton;
    String user_id=null;
    TextView tvInfo;
    TextView total;
int update=0;
    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    private SharedPreferences mSettings;

    //int count=0;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            // Получаем число из настроек
           user_id = mSettings.getString(APP_PREFERENCES_COUNTER,"");
            if (user_id.length()==0) user_id=null;
            //Toast.makeText(MainActivity.this, user_id, Toast.LENGTH_LONG).show();


        }

        setContentView(R.layout.main);

            tvInfo = (TextView) findViewById(R.id.tvInfo);
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

                    Intent intent = new Intent(MainActivity.this, UpdateActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("user_", user_id);
                    startActivity(intent);
                }
            }
        });
            // получаем SharedPreferences, которое работает с файлом настроек
            sp = PreferenceManager.getDefaultSharedPreferences(this);


        if (user_id== null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            // Получаем число из настроек
            user_id = mSettings.getString(APP_PREFERENCES_COUNTER, "");
            if (user_id.length()==0) user_id=null;


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


    public boolean onCreateOptionsMenu(Menu menu) {

        if(user_id==null) {
            MenuItem si = menu.add(0, 1, 0, "Sing in");
            MenuItem re = menu.add(0, 2, 0, "Registration");
            si.setIntent(new Intent(this, LoginActivity.class));
            re.setIntent(new Intent(this, RegActivity.class));
        }
        else
        {
            MenuItem pe = menu.add(0, 3, 0, "Load files");
            MenuItem mi = menu.add(0, 1, 0, "Preferences");
            MenuItem l = menu.add(0, 2, 0, "Log out");


            //pe.setIntent(new Intent(this, LoadActivity.class));
            Intent intent = new Intent(this, PrefActivity.class);
            mi.setIntent(intent.putExtra("user_", user_id));

            Intent intent1 = new Intent(this, MainActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(APP_PREFERENCES_COUNTER, "");
            editor.apply();
            l.setIntent(intent1);

        }
        return super.onCreateOptionsMenu(menu);
    }
}
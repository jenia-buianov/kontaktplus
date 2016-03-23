package com.kontaktplus.kontakte.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.kontaktplus.kontakte.R;

/**
 * Created by User on 06.03.2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Fragment3 extends Fragment {

    String user_id = null;
    public String first="n";
    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    public static final String APP_PREFERENCES_COUNTER2 = "first_visit";
    public boolean snd_sms = false;
    public boolean snd_ph = false;
    public boolean notif = true;
    private SharedPreferences mSettings;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment3, container, false);
        return rootView;
    }
    public void onStart() {
        super.onStart();

        mSettings = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            user_id = mSettings.getString(APP_PREFERENCES_COUNTER, "");
            first = mSettings.getString(APP_PREFERENCES_COUNTER2, "");
            if (user_id.length() == 0) user_id = null;
            if (first != "y") first = "n";
            snd_ph = mSettings.getBoolean("SND_C", false);
            snd_sms = mSettings.getBoolean("SND_S", false);
            notif = mSettings.getBoolean("notif", true);
            //Toast.makeText(getActivity(),"SND_C: "+String.valueOf(snd_ph),Toast.LENGTH_SHORT).show();
        }

        final CheckBox ch1 = (CheckBox)getActivity().findViewById(R.id.snd_cnt);
        if (snd_ph) ch1.setChecked(true);
        final CheckBox ch2 = (CheckBox)getActivity().findViewById(R.id.snd_sms);
        if (snd_sms) ch2.setChecked(true);
        final CheckBox ch3 = (CheckBox)getActivity().findViewById(R.id.notific);
        if (notif) ch3.setChecked(true);

        Button save = (Button)getActivity().findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putBoolean("SND_S", ch2.isChecked());
                editor.putBoolean("SND_C", ch1.isChecked());
                editor.putBoolean("notif", ch3.isChecked());
                editor.apply();
                replaceFragment(0);
            }
        });

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

package com.kontaktplus.kontakte.fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kontaktplus.kontakte.ConnectionDetector;
import com.kontaktplus.kontakte.MakeService;
import com.kontaktplus.kontakte.R;

/**
 * Created by User on 06.03.2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Fragment1 extends Fragment {
    public Fragment1()
    {

    }
    public SharedPreferences sp;
    String user_id = null;
    public boolean working = false;
    public String first="n";
    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    public static final String APP_PREFERENCES_COUNTER2 = "first_visit";
    public boolean snd_sms = false;
    public boolean snd_ph = false;
    CatTask cattask;

    private SharedPreferences mSettings;
    Button start;
    // Connection to Internet /
    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    // Conection to Internet /


    //int count=0;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment1, container, false);
        return rootView;
    }



    public void onStart() {
        super.onStart();

        mSettings = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            user_id = mSettings.getString(APP_PREFERENCES_COUNTER, "");
            first = mSettings.getString(APP_PREFERENCES_COUNTER2, "");
            if (user_id.length() == 0) user_id = null;
            if (first!="y") first ="n";
        }
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        snd_ph = sp.getBoolean("chb1", false);
        snd_sms = sp.getBoolean("chb2", false);

        String text = null;
        if (snd_ph) text = getString(R.string.contacts_will);
        else text = getString(R.string.contacts_willnot);
        if (snd_sms) text += "\n" + getString(R.string.sms_will);
        else text += "\n" + getString(R.string.sms_willnot);
        TextView cont = (TextView)getView().findViewById(R.id.container_book);
        cont.setText(text);


        if (mSettings.getString("Run", "")=="y"){
            ProgressBar load = (ProgressBar)getView().findViewById(R.id.pb);
            load.setVisibility(ProgressBar.VISIBLE);


            LinearLayout main_ = (LinearLayout)getView().findViewById(R.id.ll);
            main_.setVisibility(LinearLayout.VISIBLE);
            cont.setText(getString(R.string.loading));

            Button start = (Button)getView().findViewById(R.id.start);
            start.setVisibility(Button.GONE);
        }else {

            Button start = (Button) getView().findViewById(R.id.start);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!working) {
                        cd = new ConnectionDetector(getActivity().getApplicationContext());
                        isInternetPresent = cd.ConnectingToInternet();
                        if (isInternetPresent) {
                            working = true;
                            cattask = new CatTask();
                            cattask.execute();
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
                }
            });
        }
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
    class CatTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBar load = (ProgressBar)getView().findViewById(R.id.pb);
            load.setVisibility(ProgressBar.VISIBLE);


            LinearLayout main_ = (LinearLayout)getView().findViewById(R.id.ll);
            main_.setVisibility(LinearLayout.VISIBLE);
            TextView cont = (TextView)getView().findViewById(R.id.container_book);
            cont.setText(getString(R.string.loading));

            Button start = (Button)getView().findViewById(R.id.start);
            start.setVisibility(Button.GONE);


            if(!snd_ph&&!snd_sms)
            {
                replaceFragment(2);
            }

        }

        @Override
        protected Void doInBackground(Void... params) {

            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString("Run", "y");

            //editor.apply();
            editor.commit();

            Intent intents = new Intent(getActivity(), MakeService.class);
            intents.putExtra("snd_sms", sp.getBoolean("chb2", false));
            intents.putExtra("snd_ph", sp.getBoolean("chb1", false));

            getActivity().startService(intents);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            working = false;
            ProgressBar load = (ProgressBar)getView().findViewById(R.id.pb);
            Button start = (Button)getView().findViewById(R.id.start);
            load.setVisibility(ProgressBar.GONE);
            start.setVisibility(Button.VISIBLE);
            TextView cont = (TextView)getView().findViewById(R.id.container_book);

            ContentResolver cr = getActivity().getContentResolver();
            final Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            final Cursor sms = getActivity().getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
            Uri sentURI = Uri.parse("content://sms/sent");


            ContentResolver cr1 = getActivity().getContentResolver();
            Cursor c = cr1.query(sentURI, null, null, null, null);
            int count_phones = phones.getCount();
            int count_sms = sms.getCount()+c.getCount();
            cont.setText(count_phones +" "+ getString(R.string.phones_updated)+"\n"+getString(R.string.sms_updated)+" "+ count_sms);

        }
    }


}

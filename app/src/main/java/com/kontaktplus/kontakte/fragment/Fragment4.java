package com.kontaktplus.kontakte.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.kontaktplus.kontakte.ConnectionDetector;
import com.kontaktplus.kontakte.R;

/**
 * Created by User on 06.03.2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Fragment4 extends Fragment {
    public Fragment4()
    {

    }
    SharedPreferences sp;
    String user_id = null;
    public boolean working = false;
    public String first="n";
    public static final String APP_PREFERENCES = "myusers";
    public static final String APP_PREFERENCES_COUNTER = "user_id";
    public static final String APP_PREFERENCES_COUNTER2 = "first_visit";

    private SharedPreferences mSettings;

    // Connection to Internet /
    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    // Conection to Internet /


    //int count=0;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment4, container, false);
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

        ListView lvMain = (ListView) getActivity().findViewById(R.id.listView);
        // устанавливаем режим выбора пунктов списка
        lvMain.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // Создаем адаптер, используя массив из файла ресурсов
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.langs,
                android.R.layout.simple_list_item_1);
        lvMain.setAdapter(adapter);
        String[] names;
        // получаем массив из файла ресурсов
        names = getResources().getStringArray(R.array.langs);
    }


}

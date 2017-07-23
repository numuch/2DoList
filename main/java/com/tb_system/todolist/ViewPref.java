package com.tb_system.todolist;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * ビューの設定
 */
public class ViewPref extends PreferenceFragment {

    @Override
    public void onCreate(Bundle args){
        super.onCreate(args);

        addPreferencesFromResource(R.xml.view_pref);

    }

}

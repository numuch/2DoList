package com.tb_system.todolist;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * 新規作成の設定
 */
public class AddPref extends PreferenceFragment {

    @Override
    public void onCreate(Bundle args){
        super.onCreate(args);

        addPreferencesFromResource(R.xml.add_pref);


    }

}

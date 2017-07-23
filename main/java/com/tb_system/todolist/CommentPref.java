package com.tb_system.todolist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * コメントの設定
 */
public class CommentPref extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle args) {
        super.onCreate(args);

        addPreferencesFromResource(R.xml.comment_pref);


    }

    //コールバックリスナ
    public void onSharedPreferenceChanged(SharedPreferences spfs, String key) {

        //それぞれの文字列型のid(key)
        String idDefault = getString(R.string.p_key_comment_set_default);
        String idCommentMake = getString(R.string.p_key_comment_org);

        //「コメントを初期状態に戻す」を選択時
        if (key.equals(idDefault)) {
            //チェックの確認
            Boolean b = spfs.getBoolean(idDefault, true);
            if (b) {
                //チェックが入っていたらデフォルトのセリフ集に戻す
                SharedPreferences.Editor editor = spfs.edit();
                editor.putString(idCommentMake, getString(R.string.def_comment_org));
                editor.apply();
            }
        }
        //「コメント内容の設定」を選択時
        else if (key.equals(idCommentMake)) {
            //編集後のコメント内容を獲得
            String editTextValue = spfs.getString(idCommentMake, "");
            //変更が行われた場合
            if (!(editTextValue.equals(getString(R.string.def_comment_org)))) {
                SharedPreferences.Editor editor = spfs.edit();
                //チェックを外す
                editor.putBoolean(idDefault, false);
                editor.apply();
            }
        }
    }

    /**
     * このサイクルでリスナ（OnSharedPreferenceChangeListener）を登録する
     */
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * このサイクルでリスナ（OnSharedPreferenceChangeListener）を解除する
     */
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

}

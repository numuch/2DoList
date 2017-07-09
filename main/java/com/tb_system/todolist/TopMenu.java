package com.tb_system.todolist;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.tb_system.todolist.Cont.DateFormat;
import static com.tb_system.todolist.Cont.*;

/**
 * トップメニューフラグメント　アイコン４種
 */


public class TopMenu extends Fragment implements View.OnClickListener,View.OnLongClickListener{

    private MenuListener listener = null;
    private ImageButton btn_vis;

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup con, Bundle sis) {
        View v = inf.inflate(R.layout.top_menu, con, false);

        TextView dayInfo = (TextView) v.findViewById(R.id.tex_day);
        btn_vis = (ImageButton) v.findViewById(R.id.top_visible);

        //-------------■■ 現在日時の取得■■----------------------
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat( DateFormat.FULL.str , Locale.JAPAN );
        String s = sdf.format( today.getTime() );
        dayInfo.setText(s);
        //----------------------------------------------------------

        //目のアイコンの割り当て
        setVisibleIcon(false);


        /**
         * 以下ボタンの設定
         */
        //新規作成ボタン
        final ImageButton btn_new = (ImageButton) v.findViewById(R.id.top_add);
        btn_new.setOnClickListener(this);
        btn_new.setOnLongClickListener(this);

        //可視ボタン
        btn_vis.setOnClickListener(this);
        btn_vis.setOnLongClickListener(this);

        //コメントボタン
        final ImageButton btn_com = (ImageButton) v.findViewById(R.id.top_comment);
        btn_com.setOnClickListener(this);
        btn_com.setOnLongClickListener(this);

        //設定ボタン
        final ImageButton btn_set = (ImageButton) v.findViewById(R.id.top_help);
        btn_set.setOnClickListener(this);

        return v;
    }

    /**
     * 目の表示
     * tapped true は目のアイコンをタップした場合。　プリファレンスの設定を逆転させる
     *        false はビュー生成時。　設定を読み込んでその通りのアイコンをセットする
     */
    private void setVisibleIcon(boolean tapped){
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( getActivity() );
        SharedPreferences.Editor editor = spf.edit();
        String pKeyEye = getString(R.string.p_key_list_all_visible);
        boolean b = spf.getBoolean(pKeyEye , false);

        //タップでtrue,falseの切り替え
        if( tapped ){
            if(b){
                editor.putBoolean( pKeyEye, false);
                b = false;
            }else {
                editor.putBoolean( pKeyEye, true);
                b = true;
            }
            editor.apply();
            listener.allListReload( );
        }

        //画像の割り当て
        if(b){
            btn_vis.setBackgroundResource( R.drawable.ic_visibility_white_24dp );
        }else {
            btn_vis.setBackgroundResource( R.drawable.ic_visibility_off_white_24dp );
        }
    }


    /**
     * タスクの新規作成ページへ移動
     *
     * @param
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.top_add:
                //新しいフラグメントに渡す引数の準備
                Cell newCell = new Cell();                     //空のセルを作る
                listener.showCreateTask( this, newCell, ReqCode.NEW);
                break;

            case R.id.top_visible:
                setVisibleIcon(true);
                break;

            case R.id.top_comment:
                //ヒント表示モードでドロイド出現
                listener.popContent( ListType.COMMENT, new FragComment() );
                break;

            case R.id.top_help:
                //ヘルプ画面
                listener.makeDialog( DialogTag.HELP, new Object());
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()){
            case R.id.top_add:
                listener.popContent( ListType.PREF_ADD, new AddPref() );
                break;

            case R.id.top_visible:
                listener.popContent( ListType.PREF_VIEW, new ViewPref() );
                break;

            case R.id.top_comment:
                listener.popContent( ListType.PREF_COMMENT, new CommentPref() );
                break;
        }
        return true;
    }

    //以下リスナーの設定
    public interface MenuListener{
        void popContent(ListType listType, Fragment fragment);
        void showCreateTask(Fragment fragment, Cell newCell,ReqCode reqCode);
        void allListReload();
        void makeDialog(DialogTag tag, Object args);
    }

    //Android 6.0以上
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        onAttachContext(context);
    }

    //Android 6.0未満
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;
        onAttachContext(activity);
    }

    private void onAttachContext(Context context){
        if(!(context instanceof MenuListener)){
            throw new UnsupportedOperationException( "MenuListenerが実装されていません");
        } else {
            listener = (MenuListener) context;
        }

    }
}

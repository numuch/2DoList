package com.tb_system.todolist;



import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.tb_system.todolist.Cont.DialogTag;
import static com.tb_system.todolist.Cont.ReqCode;


/**
 * メインアクティビティ　フラグメント切り替え、ダイアログ表示等のコールバック有り
 *
 */
public class MainActivity extends AppCompatActivity implements ListFrag.ListListener, ListClearFrag.ClearListener, DialogHelp.HelpListener, FragComment.CommentListener, ListDeadFrag.DeadListener,DialogMakeCell.DialogMakeCellListener, DeleteSelectDialog.DeleteSelectDialogListener,StatusDialog.StatusDialogListener,TopMenu.MenuListener{

    FrameLayout topBarArea;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topBarArea = (FrameLayout)findViewById(R.id.top_bar_area);

        //今現在のlong値を取得して０時０分０秒を設定したlong値も取得
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        long today0 = c.getTimeInMillis();

        //プリファレンスの読み込み
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //　前回起動日の獲得
        String key = getString(R.string.p_key_last_start_day);
        long lastDay0 = prefs.getLong( key , today0);
        //　初回起動かどうかの確認
        String key2 = getString(R.string.p_key_first_time_action);
        boolean boo = prefs.getBoolean( key2, false);
        if( !(boo) ){
            String s = getString(R.string.mess_first_dialog);
            makeDialog(DialogTag.MESSAGE, s);
            //処理をした後はtrueにして次回起動しないようにする
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(key2, true);
            editor.apply();
        }

        //プリファレンスに今日の日付の０時０分０秒を保存
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong( key , today0 );
        editor.apply();


        //前回起動日は何日前だったか
        long l = now - lastDay0 ;
        int diffDay = (int)(l / Cont.MILL_OF_DAY);
        //もし１日以上経っていたらチェック
        if( diffDay > 0 ){
            DBAccess dba = new DBAccess(this);
            String startInfo = dba.taskJudge( );

            //もしタスクに変化があったら日のはじめにコメント
            if ( !(startInfo.equals("")) ) {
                showComment(startInfo);
            }

        }




        //それぞれのエリアにフラグメントを設置
        List<ListType> list = new ArrayList<ListType>();
        Collections.addAll(list, ListType.BAR, ListType.ONCE, ListType.LIST, ListType.CLEAR, ListType.WAIT, ListType.DEAD);

        replaceFragment(list);

    }


    private void replaceFragment(List<ListType> listTypes){

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        //順に取り出して判定
        for(int i = 0; i < listTypes.size() ; i++){
            ListType listType = listTypes.get(i);

            switch (listType) {
                case BAR:
                    ft.replace(R.id.top_bar_area, new TopMenu(), ListType.BAR.str);
                    break;
                case ONCE:
                    ft.replace(R.id.once_area, new ListOnce(), ListType.ONCE.str);
                    break;
                case LIST:
                    ft.replace(R.id.task_area, new ListFrag(), ListType.LIST.str);
                    break;
                case CLEAR:
                    ft.replace(R.id.clear_area, new ListClearFrag(), ListType.CLEAR.str);
                    break;
                case WAIT:
                    ft.replace(R.id.wait_area, new ListWaitFrag(), ListType.WAIT.str);
                    break;
                case DEAD:
                    ft.replace(R.id.dead_area, new ListDeadFrag(), ListType.DEAD.str);
                    break;
            }
        }
        ft.commit();
    }




    /**
     * 以下各フラグメントからのコールバックリスナー
     */
    //◆トップメニュー、ステータス画面より
    //用件の新規作成、もしくは変更画面
    @Override
    public void showCreateTask(Fragment fragment, Cell cell, ReqCode reqCode ){
        DialogMakeCell dmc = DialogMakeCell.newInstance(fragment, reqCode);
        //引数として選択されたセルを設定
        Bundle bundle =  new Bundle();
        bundle.putSerializable("SEL_CELL", cell);
        dmc.setArguments(bundle);
        //タスク作成画面が表示されるようにする
        dmc.show(getFragmentManager(), "dialog");
    }

    //ダイアログを表示する
    // ◆DialogHelpより

    @Override
    public void makeDialog(DialogTag tag, Object args ){
        switch (tag){
            case HELP:
                DialogHelp dh = new DialogHelp();
                dh.show(getFragmentManager(), tag.name());
                break;

            case MESSAGE:
                String message = (String)args;
                DialogMessage dm = DialogMessage.newInstance(message);
                dm.show(getFragmentManager(), tag.name());
                break;



        }
    }

    //リスト群を全てreplaceするメソッド
    @Override
    public void allListReload(){
        //リスト群を更新
        List<ListType> list = new ArrayList<ListType>();
        Collections.addAll(list, ListType.ONCE, ListType.LIST, ListType.CLEAR, ListType.WAIT, ListType.DEAD);

        replaceFragment(list);
    }

    //◆トップメニュー＆コメントフラグメント　　フラグメントを表示したり消したりするメソッド
    @Override
    public void popContent(ListType type, Fragment frag){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        //引数で受けたフラグメントが既に表示しているかどうかの判定をしたい
        Fragment f = fm.findFragmentByTag( type.str ) ;

        //存在するならばremove
        if( f != null ) {
            ft.remove(f).commit();
            if (type == ListType.PREF_VIEW) {
                allListReload();
            }
        }


        //存在していなければ引数で受けたフラグメントを表示する
        else {
            switch (type){

                case COMMENT:
                    ft.replace( R.id.comment_area, frag, ListType.COMMENT.str);
                    break;

                case PREF_SETTING:
                    ft.replace( R.id.pref_area, frag, ListType.PREF_SETTING.str);
                    break;

                case PREF_COMMENT:
                    ft.replace( R.id.pref_area, frag, ListType.PREF_COMMENT.str);
                    break;

                case PREF_ADD:
                    ft.replace( R.id.pref_area, frag, ListType.PREF_ADD.str);
                    break;

                case PREF_VIEW:
                    ft.replace( R.id.pref_area, frag, ListType.PREF_VIEW.str);
                    break;
            }
            ft.commit();
        }

    }


    //◆MainAct、DBAccess、より◆　ドロイドに話させたい文字がｓ
    //既に画面上にコメントフラグメントがあっても、remove()せずにreplace()するパターン
    @Override
    public void showComment( String s ) {
        FragComment fragComment = FragComment.newInstance(s);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace( R.id.comment_area, fragComment, ListType.COMMENT.str);
        ft.commit();
    }



    //◆セルが　リストA　→　リストB　というふうに移動する場合の画面リフレッシュ DBAListener
    @Override
    public void cellMoveList(ListType beforeList, ListType afterList) {
        List<ListType> list = new ArrayList<>(Arrays.asList(beforeList, afterList));
        replaceFragment(list);
    }


    //◆DialogMakeCell DeleteSelectDialog◆　単一のリストをリフレッシュ
    @Override
    public void reloadFragment(ListType listType) {
        List<ListType> list = new ArrayList<>();
        Collections.addAll(list, listType);
        replaceFragment(list);
    }


}



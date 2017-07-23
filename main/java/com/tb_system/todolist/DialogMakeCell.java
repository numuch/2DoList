package com.tb_system.todolist;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import static com.tb_system.todolist.Cont.DateFormat;
import static com.tb_system.todolist.Cont.*;
import static com.tb_system.todolist.Cont.ReqCode.TASK;

/**
 * 予定作成画面
 * 登録、変更ボタンを押して初めて insertSQL() するので
 * selCellに値を代入しても大丈夫
 */
public class DialogMakeCell extends DialogFragment {
    /**
     * フィールド
     * 　仮想デバイスの日付が更新されてないとgetInstance()で現在の時刻が
     * 　取得できないので注意
     */


    //リスナー
    private DialogMakeCellListener listener = null;
    //ナンバーピッカーは使いまわすのでリクエストコードを決めておく


    //ビュー
    private TextView taskName;
    private TextView menu1;
    private TextView menu2;
    private TextView menu3;
    private TextView con0;
    private TextView con1;
    private TextView con2;
    private TextView con3;
    private TextView con4;
    private Button bt_entry;          //登録ボタン
    private Button bt_update;          //更新ボタン
    private Button bt_return;          //戻るボタン

    //リクエストコード格納用（NEW　UPDATE　どっちか）
    private ReqCode editMode;
    //データ類格納
    private int changeRepeat = 1;       //繰り返す繰り返さないを切り替えた時の値保持
    private Cell cell;           //引地として受け、戻り値として返すcell
    private Cell beforeCell = new Cell();    //変更前のセル

    /**
     * newInstance
     * @param target
     * @param reqCode
     * @return
     */
    public static DialogMakeCell newInstance(Fragment target, ReqCode reqCode){
        DialogMakeCell fragment = new DialogMakeCell();
        fragment.setTargetFragment( target, reqCode.hashCode());
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     *  このダイアログは　newInstance() され、show()で表示されている。
     *  その時に、呼び元のフラグメント、リクエストコード、がセットになって来ている。
     * @param bundle
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle bundle){
        Dialog d = new Dialog(getActivity());
        // DialogFragmentをタイトル無しにします
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //レイアウト設置
        d.setContentView(R.layout.make_cell);
        //めいっぱい表示
        d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        getLayout(d);

        //リクエストコードは（NEW, UPDATE）の２つ
        editMode = getReqCodeByHashCode( getTargetRequestCode() );
        //バンドルに入るのはcell
        bundle = getArguments();
        //変更モードならばcellが入るし、新規モードならばnullが入る
        cell =(Cell)bundle.getSerializable("SEL_CELL");
        if(cell== null){cell = new Cell();}

        //各種ビューに表示させる（タスク名、開始日、メモ）
        taskName.setText( cell.getTask() );
        con1.setText( cell.getStartDay( DateFormat.FULL ));
        con4.setText( cell.getMemo() );


        all_bt_true();  //ひとまず全てのボタンを使用可能にする

        final TextView title = (TextView)d.findViewById(R.id.title);
        final TextView exp = (TextView)d.findViewById(R.id.exp);
        final TextView exp2 = (TextView)d.findViewById(R.id.exp2);

        //メニューの＋からの場合はNEW、ステータス画面から変更を押した場合はUPDATE
        switch (editMode){
            case NEW:   //新規作成の場合
                title.setText("予定の新規登録");
                exp.setText("変更したい項目をタップして下さい");

                bt_entry.setEnabled(false);     //登録ボタンＯＦＦ（色々決めてないので）
                bt_update.setVisibility( View.GONE);    //変更の表示自体を消す
                //プリファレンスを判断する
                SharedPreferences spr = PreferenceManager.getDefaultSharedPreferences(getActivity());
                //初期の予定タイプ。。。falseで単発、trueで反復
                Boolean b = spr.getBoolean(getString(R.string.pref_select_task_type), false);
                //初期の反復頻度。。。
                String s = spr.getString( getString(R.string.pref_new_freq), "3");
                changeRepeat = Integer.parseInt(s);
                //初期の目標値。。。
                String goal = spr.getString( getString(R.string.pref_new_goal), "3");
                cell.setGoal( Integer.parseInt(goal) );

                //trueで反復
                if(b){
                    cell.setFrequency(changeRepeat);
                    editModeRepeat();
                }
                //falseで単発
                else {
                    cell.setFrequency(0);
                    editModeOnce();
                }
                break;

            case UPDATE:    //変更の場合

                if(cell.getFrequency() == 0){
                    title.setText("単発予定の変更");
                    exp.setText("変更したい項目をタップして下さい");
                    exp2.setVisibility(View.GONE);;
                    editModeOnce();
                }else {
                    title.setText("反復予定の変更");
                    exp.setText("変更したい項目をタップして下さい");
                    exp2.setTextSize(16);
                    exp2.setTextColor(Color.parseColor( "#ff8c00" ));
                    exp2.setText( "＊注意：開始日や実行頻度を変更すると\n　　　　連続達成回数が０になります" );

                    editModeRepeat();
                }
                con0.setVisibility( View.GONE);

                bt_entry.setVisibility( View.GONE);      //新規登録の表示自体を消す
                beforeCell.setVisible( cell.getVisible() );
                beforeCell.setFrequency( cell.getFrequency() );
                beforeCell.setStartDay( cell.getStartDay() );
        }

        return d;
    }

    //一度きりの用件を決めるモード
    private void editModeOnce(){
        con0.setText( "単発予定" );
        menu1.setText("予定日");
        //反復頻度、目標値は表示させる必要なし
        menu2.setVisibility(View.GONE);
        con2.setVisibility(View.GONE);
        menu3.setVisibility(View.GONE);
        con3.setVisibility(View.GONE);

        //囲みを緑にする
        con0.setBackgroundResource( R.drawable.border_once);
        con1.setBackgroundResource( R.drawable.border_once);
        taskName.setBackgroundResource( R.drawable.border_once);
        con4.setBackgroundResource( R.drawable.border_once);
    }
    //繰り返す用件を決めるモード
    private void editModeRepeat(){
        con0.setText( "反復予定" );
        menu1.setText("開始日");
        //反復頻度、目標値を表示し、値をsetText()
        menu2.setVisibility(View.VISIBLE);
        con2.setVisibility(View.VISIBLE);
        menu3.setVisibility(View.VISIBLE);
        con3.setVisibility(View.VISIBLE);
        con2.setText( freqToWord() );     //実行頻度のところに
        con3.setText( goalToWord() );     //目標コンボ数のところに

        //囲みを青にする
        con0.setBackgroundResource( R.drawable.border);
        con1.setBackgroundResource( R.drawable.border);
        taskName.setBackgroundResource( R.drawable.border);
        con4.setBackgroundResource( R.drawable.border);
    }




    /**
     * ダイアログから戻った時の処理
     * @param reqCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data){
        //int　→　ReqCode
        ReqCode rq = getReqCodeByHashCode( reqCode );

        switch (rq){

            //タスク名を入力した後
            case TASK :
                if ( resultCode != Activity.RESULT_OK ){return; }
                String name = data.getStringExtra( Intent.EXTRA_TEXT );
                if(name != null){
                    bt_entry.setEnabled(true);
                }
                cell.setTask( name );
                taskName.setText( name );

                return;

            //実行頻度を選んだ後
            case FREQ :
                if ( resultCode != Activity.RESULT_OK ){return; }
                int freq = data.getIntExtra(Intent.EXTRA_TEXT, 0);
                cell.setFrequency(freq);
                con2.setText( freqToWord() );     //実行頻度のところに
                return;

            //目標値を選んだ後
            case GOAL :
                if ( resultCode != Activity.RESULT_OK ){return; }
                int goal = data.getIntExtra(Intent.EXTRA_TEXT, 1);
                cell.setGoal(goal);
                con3.setText( goalToWord() );     //目標のところに
                return;

            //メモを入力した後
            case MEMO :
                if ( resultCode != Activity.RESULT_OK ){return; }
                String memo = data.getStringExtra( Intent.EXTRA_TEXT );
                cell.setMemo( memo );
                con4.setText( memo );
                return;
        }
        super.onActivityResult(reqCode, resultCode, data);
    }



    /**
     * （クリックイベント）繰り返す　or　繰り返さない
     * 押す度に「実行頻度」「目標コンボ数」の項目の編集不可が切り替える
     */
    private void ci_changeFreq( ){
        //「繰り返さない」が表示されている時にボタンを押したら
        if(cell.getFrequency() == 0){
            //以前設定していた実行頻度の値に戻す
            cell.setFrequency( changeRepeat );
            editModeRepeat();
        //「繰り返す」が表示されている時にボタンを押したら
        } else {
            //実行頻度の値を保持してから、一度きりの実行に切り替わる
            changeRepeat = cell.getFrequency();
            cell.setFrequency(0);
            editModeOnce();
        }
    }


    /**
     * （クリックイベント）デイトピッカーダイアログを表示.
     */
    private void ci_showDatePickerDialog() {
        //デイトピッカーのデフォルト位置はcell内の開始日
        final Calendar start = cell.getStartDay();

        DatePickerDialog dlgDatePicker = new DatePickerDialog( getActivity() ,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        //ピッカーから取得した日付の格納
                        Calendar entryDate = Calendar.getInstance();
                        entryDate.set(year, monthOfYear, dayOfMonth);
                        //時間を０時０分１秒にセットして格納
                        cell.setStartDay( setZero(entryDate) );

                        //選択した日が過去ならば今日の日付に直すメソッド
                        judgePast();
                       //ビューへ反映
                        con1.setText(cell.getStartDay( DateFormat.FULL ) );

                    }
                }, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH) );   //デイトピッカーのデフォルト位置指定
        dlgDatePicker.show();
    }
    /**
     * 開始日を００時００分０１秒にセットするメソッド
     */
    private Calendar setZero(Calendar c){
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 1);
        return c;
    }
    /**
     * 引数で受けた日付が過去の日付ならば、今日の日付に書き換えるメソッド
     */
    private void judgePast(){
        //今日の日付を獲得
        final Calendar now = Calendar.getInstance();
        if( now.after( cell.getStartDay() ) ){
            cell.setStartDay( setZero( now ) );
        }
    }

    /**
     * （クリックイベント）ナンバーピッカーダイアログを表示
     * 頻度、目標値　指定用
     */
    private void ci_showNumberPicker(ReqCode reqCode){
        int args[] = new int[3];
        switch (reqCode){
            case FREQ :
                args[0]=1;      //ピッカー最小値
                args[1]=31;      //ピッカー最大値
                args[2]= cell.getFrequency();     //初期値
                break;
            case GOAL :
                args[0]=0;      //ピッカー最小値
                args[1]=1000;      //ピッカー最大値
                args[2]= cell.getGoal();     //初期値
                break;
        }
        NumberPickerDialog npd = NumberPickerDialog.newInstance(this, reqCode, args );
        npd.show(getFragmentManager(), "tag");
    }

    /**
     * （クリックイベント）エディットテキストダイアログを表示
     * タスク名、メモ　指定用
     */
    private void ci_showEditTextDialog(ReqCode reqCode){
        String s = "";
        switch (reqCode){
            case TASK:
                s = cell.getTask();
                break;
            case MEMO:
                s = cell.getMemo();
                break;
        }
        MakeTextDialog mtd = MakeTextDialog.newInstance(this, reqCode, s);
        mtd.show( getFragmentManager(), "mtd");
    }


    /**
     * 登録、変更、戻る
     */

    //登録ボタン
    private void mes_entry(){
        //書き込む前の最終cell調整
        beforeSQLWrite();

        DBAccess dba = new DBAccess(getActivity());             //SQLにデータアクセスするためのインスタンス作成
        dba.cellSetInSQL(cell, ReqCode.NEW);         //新規セルの内容をSQLにインサート
        Toast.makeText(getActivity(), "登録完了", Toast.LENGTH_SHORT).show();
        bt_entry.setEnabled( false );   //二重登録防止でOFF


        listener.reloadFragment( cell.getVisible() );
    }

    //更新ボタン
    private void mes_update(){
        //書き込む前の最終cell調整
        beforeSQLWrite();

        DBAccess dba = new DBAccess(getActivity());             //SQLにデータアクセスするためのインスタンス作成
        dba.cellSetInSQL(cell, ReqCode.UPDATE);         //既に sql内に id はあるのでupdate
        Toast.makeText(getActivity(), "変更完了", Toast.LENGTH_SHORT).show();

        ListType beforeList = beforeCell.getVisible();
        ListType afterList = cell.getVisible();
        if(beforeList == afterList){
            listener.reloadFragment(afterList);
        }else {
            listener.cellMoveList(beforeList, afterList);
        }
        mes_return();
    }

    //戻るボタン
    private void mes_return(){
       dismiss( );
    }

    /**
     * SQLに書き込む前に visible や stack の値を設定するメソッド
     */
    private void beforeSQLWrite(){
        //用件名の入力
        cell.setTask( taskName.getText().toString().trim() );

        if(editMode == ReqCode.NEW){
            initializeCell();
        }
        if(editMode == ReqCode.UPDATE){
            //開始日や頻度が当初と変わっていたのならば
            if( (cell.getStartDay() != beforeCell.getStartDay()) || (cell.getFrequency() != beforeCell.getFrequency()) ) {
                //開始日が過去でないかチェックして
                judgePast();
                //初期化
                initializeCell();
            }

        }
    }
    /**
     * 新規登録時の初期設定メソッド
     */
    private void initializeCell(){
        //変更モードの場合のみ実行する初期化
        if(editMode == ReqCode.UPDATE){
            cell.setCombo( 0 );
            cell.setDone( Cont.VAL_FALSE );
        }
        //予定日と今日との日数差分を得る
        int diffDays = cell.getDiffDays();

        //リスト単発
        if(cell.getFrequency() == 0) {
            cell.setVisible(ListType.ONCE);
            //日数差分がそのままカウントダウン値
            cell.setCount(diffDays);
        }
        //リスト反復
        else{
            //頻度をスタックに補充
            cell.setStack( cell.getFrequency() );

            //今日から始める場合は
            if(diffDays == 0 ){
                cell.setVisible(ListType.LIST);
                //この場合のカウント値の計算はstack + diffDayだけど結局０なので
                cell.setCount( cell.getStack() );
            }
            //明日以降から始める場合は
            else{
                cell.setVisible(ListType.WAIT);
                //日数差分がそのままカウントダウン値
                cell.setCount(diffDays);
            }
        }
    }




    //下のボタンを全てONにする（初期化）
    private void all_bt_true(){
        bt_entry.setEnabled(true);
        bt_update.setEnabled(true);
        bt_return.setEnabled(true);
    }

    //makeCell内の文字表現を設定する(ナンバーピッカーの中のやつと同じメソッド）
    private String freqToWord() {
        String word;
        switch (cell.getFrequency()) {
            case 0:
                word = "一度だけ実行";
                break;
            case 1:
                word = "毎日実行";
                break;
            case 7:
                word = "週１回実行";
                break;
            default:
                word = Cont.makeSentence( new Object[]{cell.getFrequency(), "日に１回実行"} );
                break;
        }
        return word;
    }

    //目標数値を文字列に変換
    private String goalToWord(){
        String word;
        if(cell.getGoal() == 0){
            word = "目標無し";
        }else{
            word = Cont.makeSentence( new Object[]{"目標", cell.getGoal(), "回連続達成"} );
        }
        return word;
    }




    private void getLayout(Dialog d){
        taskName = (TextView) d.findViewById(R.id.task_name);
        menu1 =  (TextView)d.findViewById(R.id.menu_1);
        menu2 = (TextView)d.findViewById(R.id.menu_2);
        menu3 = (TextView)d.findViewById(R.id.menu_3);
        con0 = (TextView) d.findViewById(R.id.con_0);
        con1 = (TextView) d.findViewById(R.id.con_1);
        con2 = (TextView) d.findViewById(R.id.con_2);
        con3 = (TextView) d.findViewById(R.id.con_3);
        con4 = (TextView) d.findViewById(R.id.con_4);
        //ボタン類
        bt_entry = (Button) d.findViewById(R.id.entry);
        bt_update = (Button) d.findViewById(R.id.update);
        bt_return = (Button) d.findViewById(R.id.cancel);

        //クリックイベント（レイアウトの上から順に）

        // 繰り返しをクリック時
        con0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ci_changeFreq( );
            }
        });
        // タスク名をクリック時
        taskName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ci_showEditTextDialog(ReqCode.TASK);
            }
        });

        // 開始日内容をクリック時　デイトピッカーダイアログを表示
        con1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ci_showDatePickerDialog();
            }
        });
        // 実行頻度をクリック時　ナンバーピッカーダイアログを表示
        con2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ci_showNumberPicker(ReqCode.FREQ);
            }
        });
        // 目標コンボ数（ゴール）をクリック時　ナンバーピッカーダイアログを表示
        con3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ci_showNumberPicker(ReqCode.GOAL);
            }
        });
        //　メモの項目をクリックした時　エディットテキストダイアログを表示
        con4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ci_showEditTextDialog(ReqCode.MEMO);
            }
        });

        // 最下段　登録ボタン
        bt_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mes_entry();
            }
        });
        // 最下段　更新ボタン
        bt_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mes_update();
            }
        });
        // 最下段　戻るボタン
        bt_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mes_return();
            }
        });

    }

    //以下リスナーの設定
    public interface DialogMakeCellListener{
        void reloadFragment(ListType listType);
        void cellMoveList(ListType beforeList, ListType afterList);
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
        if(!(context instanceof DialogMakeCellListener)){
            throw new UnsupportedOperationException( "DialogMakeCellListenerが実装されていません");
        } else {
            listener = (DialogMakeCellListener) context;
        }
    }


}


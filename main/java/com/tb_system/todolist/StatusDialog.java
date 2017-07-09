package com.tb_system.todolist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import static com.tb_system.todolist.Cont.DateFormat;
import static com.tb_system.todolist.Cont.*;
import static com.tb_system.todolist.Cont.makeSentence;

/**
 * セルの状態表示
 */
public class StatusDialog extends DialogFragment {

    private static final String ARGS_NAME = "sentence";
    private StatusDialogListener listener;

    private Cell cell;


    //呼び出し側で NumberPickerDialog.newInstance() を実行することによって
    // このダイアログのインスタンスが生成される
    // これには呼び出し元のフラグメントとリクエストコード、すでに入っている文字列
    public static StatusDialog newInstance(Fragment f, ReqCode reqCode, Cell cell){
        StatusDialog instance = new StatusDialog();
        //呼び出し元のフラグメントとリクエストコードをセット
        instance.setTargetFragment(f,reqCode.hashCode());
        //呼び出し元が指定する文字列
        Bundle args = new Bundle();
        args.putSerializable(ARGS_NAME, cell);
        instance.setArguments( args );
        //セットし終えたインスタンスを返す
        return instance;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        Bundle args = getArguments();
        cell = (Cell)args.getSerializable(ARGS_NAME);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.status_dialog, null, false);


        //メンバ変数初期化
        final TextView title = (TextView) v.findViewById(R.id.status_title);
        final TextView task_name = (TextView) v.findViewById(R.id.task_name);
        //メニュー
        final TextView menu_1 = (TextView) v.findViewById(R.id.menu_1);
        final TextView menu_2 = (TextView) v.findViewById(R.id.menu_2);
        final TextView menu_3 = (TextView) v.findViewById(R.id.menu_3);
        final TextView menu_4 = (TextView) v.findViewById(R.id.menu_4);
        final TextView menu_5 = (TextView) v.findViewById(R.id.menu_5);


        //詳細のタイトル表示
        String freqText;
        switch (cell.getFrequency()) {
            case 0:
                freqText = "単発予定"; break;
            case 1:
                freqText = "反復予定：毎日"; break;
            case 7:
                freqText = "反復予定：週１回"; break;
            default:
                freqText = makeSentence(new Object[]{"反復予定：", cell.getFrequency(), "日に１回"} ); break;
        }
        title.setText( freqText );

        //予定名
        task_name.setText( cell.getTask() );


        //開始日
        String s = cell.getStartDay( DateFormat.FULL );
        String startDay;
        if(cell.getVisible() == ListType.ONCE){
            startDay = s ;
        }else {
            startDay = makeSentence(new Object[]{s, "開始"} );
        }
        //連続達成
        String combo;
        if(cell.getCombo() <= 1) {
            combo = makeSentence( new Object[]{"達成回数", cell.getCombo(), "回"} );
        }else {
            combo = makeSentence(new Object[]{cell.getCombo(), "回連続で達成中"});
        }
        //目標
        String goal;
        if(cell.getGoal() <= cell.getCombo()) {
            goal = "目標達成済み";
        }else{
            goal =makeSentence( new Object[]{"目標", cell.getGoal(), "回"} );
        }
        //メモ
        String memo = cell.getMemo();

        //ステータス画面初期化
        String m1 = "";
        String m2 = "";
        String m3 = "";
        String m4 = "";
        String m5 = "";

        //リスト別表示
        ListType type = cell.getVisible();

        switch (type){
            case LIST:
                if(cell.getCount() == 1){
                    m1 = "今日中に達成しましょう！";
                }else {
                    m1 = makeSentence( new Object[]{ cell.getCount(), "日以内に達成しましょう"} );
                }
                m2 = combo;
                m3 = goal;
                m4 = startDay;
                m5 = memo;

                break;
            case CLEAR:
                if(cell.getCount() ==1){
                    m1 = "達成済　翌朝リストに戻ります";
                }else {
                    int overDay = cell.getCount()-1;
                    m1 = makeSentence( new Object[]{overDay,"日余裕を持って達成しました\n翌朝リストに戻ります" });
                }
                m2 = combo;
                m3 = goal;
                m4 = startDay;
                m5 = memo;

                break;
            case DEAD:
                if(cell.getCombo() == 0){
                    m1 = "一度も達成せずに終了";
                }else if(cell.getCombo() == 1){
                    m1 = "１回だけ達成して終了";
                }else {
                    m1 = makeSentence( new Object[]{ cell.getCombo(), "回連続達成後に終了"} );
                }
                if(cell.getGoal() <= cell.getCombo() ){
                    m2 = "目標達成済み";
                } else {
                    m2 = "目標達成失敗";
                }
                m3 = "長押しでリトライできます";
                m4 = memo;

                break;
            case WAIT:
                if(cell.getCount() == 1){
                    m1 = "明日から開始します";
                }else {
                    m1 = makeSentence( new Object[]{cell.getCount(), "日後に開始します"} );
                }
                m2 = startDay;
                m3 = goal;
                m4 = memo;

                break;

            case ONCE:
                m1 = startDay;
                if(cell.getCount() < 0){
                    m2 = makeSentence( new Object[]{ Math.abs( cell.getCount() ), "日前の予定です"} );
                }
                else if(cell.getCount() == 0){
                    m2 = "今日の予定です";
                }else {
                    m2 = makeSentence( new Object[]{cell.getCount(),"日後の予定です"} );
                }
                m3 = memo;
                m4 = "";
                m5 = "";
                break;
        }

        menu_1.setText( m1 );
        menu_2.setText( m2 );
        menu_3.setText( m3 );
        menu_4.setText( m4 );
        menu_5.setText( m5 );


        //アラートダイアログ画面
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //削除
        builder.setPositiveButton("削除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteSelectDialog dsd = DeleteSelectDialog.newInstance( getTargetFragment(), ReqCode.DELETE , cell );
                dsd.show( getFragmentManager(), "dialog");

            }
        });


        //変更
        builder.setNeutralButton("変更", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.showCreateTask( getTargetFragment(), cell, ReqCode.UPDATE );
            }
            });


        //戻る

        builder.setNegativeButton("戻る", null);

        builder.setView(v);
        return builder.create();
    }



    //以下リスナーの設定
    public interface StatusDialogListener{
        void showCreateTask(Fragment fragment, Cell newCell, ReqCode reqCode);

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
        if(!(context instanceof StatusDialogListener)){
            throw new UnsupportedOperationException( "ListListener が実装されていません");
        } else {
            listener = (StatusDialogListener) context;
        }
    }


}

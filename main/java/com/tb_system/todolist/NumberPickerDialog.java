package com.tb_system.todolist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import static com.tb_system.todolist.Cont.*;

/**
 * ナンバーピッカ
 */
public class NumberPickerDialog extends DialogFragment {

    private static final String ARGS_NAME = "min_max_default";    //ピッカーの最小・最大・初期値

    NumberPicker np;
    TextView tv;

    //呼び出し側で NumberPickerDialog.newInstance() を実行することによって
    // このダイアログのインスタンスが生成される
    // これには呼び出し元のフラグメントとリクエストコード、数値類（ピッカー最小・最大値・初期値）
    public static NumberPickerDialog newInstance(Fragment f, ReqCode reqCode, int[] numbers){
        NumberPickerDialog instance = new NumberPickerDialog();
        //呼び出し元のフラグメントとリクエストコードをセット
        instance.setTargetFragment(f,reqCode.hashCode());
        //呼び出し元が指定する数値類をせっと
        Bundle args = new Bundle();
        args.putSerializable(ARGS_NAME, numbers);   //配列の場合はputInt ではなく putSerializable
        instance.setArguments( args );
        //セットし終えたインスタンスを返す
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //値(最初のピッカー数値）の取得
        Bundle args = getArguments();
        int[] number = (int[]) args.getSerializable(ARGS_NAME);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.number_picker_dialog, null, false);

        np = (NumberPicker) view.findViewById(R.id.npd_picker);
        np.setMinValue( number[0]);
        np.setMaxValue( number[1]);
        np.setValue( number[2]);

        tv = (TextView) view.findViewById(R.id.npd_tv);
        ci_btn();


        //クリックイベント　（ボタンクリックでピッカーの上に文字が表示される）
        final Button btn = (Button) view.findViewById(R.id.npd_bt);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ci_btn();
            }
        });

        //アラートダイアログ画面
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("数値選択（直接入力可）");
        //ダイアログのＯＫをおした時の挙動
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //呼び出し元のフラグメントを獲得
                Fragment target = getTargetFragment();
                if(target == null ){
                    dismiss();
                    return;
                }
                //選択したピッカーの番号をインテントに格納
                Intent data = new Intent();
                data.putExtra( Intent.EXTRA_TEXT, np.getValue() );
                //呼び出し元でonActivityResultをオーバーライドしてdataの処理をすること
                target.onActivityResult( getTargetRequestCode(), Activity.RESULT_OK, data );
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setView(view);
        return builder.create();
    }

    //ピッカーで選択した数値を上の TextView に割り当てる
    private void ci_btn() {
        int n = np.getValue();
        String word = numberToWord(n);
        tv.setText( word );
    }

    //数値を文字に変換
    private String numberToWord(int number) {
        String word;
        ReqCode rq = getReqCodeByHashCode( getTargetRequestCode() );
        switch ( rq ) {
            case FREQ:
                word = freqToWord( number );
                break;
            case GOAL:
                word = goalToWord(number);
                return word;
            default:
                word = String.valueOf(number);
        }
        return word;
    }

    private String freqToWord(int frequency) {
        String word;
        switch (frequency) {
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
                word = Cont.makeSentence( new Object[]{frequency, "日に１回実行"} );
                break;
        }
        return word;
    }

    //目標数値を文字列に変換
    private String goalToWord(int goal){
        String word;
        if(goal == 0){
            word = "目標無し";
        }else{
            word = Cont.makeSentence( new Object[]{"目標", goal, "回"} );
        }
        return word;
    }





}

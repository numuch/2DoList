package com.tb_system.todolist;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static com.tb_system.todolist.Cont.ReqCode;
import static com.tb_system.todolist.Cont.getReqCodeByHashCode;

/**
 * Created by tb on 2016/06/08.
 */
public class MakeTextDialog extends DialogFragment implements View.OnClickListener,TextWatcher {

    private static final String ARGS_NAME = "sentence";
    private ReqCode reqCode;
    private EditText memoEdit;
    private EditText taskEdit;
    private Button btn_ok;

    /**
     * newInstance するときに既に入力した文字と、フラグメントと、リクエストコードをセットしている
     * リクエストコードはintでいれないといけないのでhashCode()で獲得している
     * @param f
     * @param rc
     * @param text
     * @return
     */
    public static MakeTextDialog newInstance(Fragment f, ReqCode rc, String text) {
        MakeTextDialog instance = new MakeTextDialog();
        //呼び出し元のフラグメントとリクエストコードをセット
        instance.setTargetFragment(f, rc.hashCode());
        //呼び出し元が指定する文字列
        Bundle args = new Bundle();
        args.putString(ARGS_NAME, text);
        instance.setArguments(args);
        //セットし終えたインスタンスを返す
        return instance;
    }


    @Override
    public Dialog onCreateDialog(Bundle bundle) {

        Dialog d = new Dialog(getActivity());
        //レイアウト設置
        d.setContentView(R.layout.make_text_dialog);
        //大きく表示
        d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Bundle args = getArguments();
        String s = args.getString(ARGS_NAME);

        //タイトルと説明
        final TextView title = (TextView) d.findViewById(R.id.mtd_title);
        final TextView sub = (TextView) d.findViewById(R.id.mtd_exp);
        taskEdit = (EditText) d.findViewById(R.id.mtd_edit_task);
        memoEdit = (EditText) d.findViewById(R.id.mtd_edit_memo);
        //リクエストコードint型をReqCode型に変換
        reqCode = getReqCodeByHashCode(getTargetRequestCode());
        if (reqCode == ReqCode.TASK) {
            title.setText("用件名入力");
            sub.setText("１０文字以内推奨");
            taskEdit.setText(s);
            //テキスト監視
            taskEdit.addTextChangedListener(this);
            memoEdit.setVisibility(View.GONE);

        } else if (reqCode == ReqCode.MEMO) {
            title.setText("メモ入力");
            sub.setText("１０行まで表示可能");
            taskEdit.setVisibility(View.GONE);
            memoEdit.setText(s);
            //テキスト監視
            memoEdit.addTextChangedListener(this);

        }

        Button btn_del = (Button) d.findViewById(R.id.mtd_delete);
        Button btn_ret = (Button) d.findViewById(R.id.mtd_cancel);
        btn_ok = (Button) d.findViewById(R.id.mtd_ok);
        btn_del.setOnClickListener(this);
        btn_ret.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        //もし新規の場合は条件をクリアしないとOKにしない設定
        if(s == null) {
            btn_ok.setEnabled(false);
        }else {
            btn_ok.setEnabled(true);
        }

        return d;
    }

    private EditText switchEditText() {
        if (reqCode == ReqCode.TASK) {
            return taskEdit;
        } else {
            return memoEdit;
        }
    }

    /**
     * onClick() 全てボタンをおした時の挙動
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mtd_delete:
                switchEditText().setText("");
                break;
            case R.id.mtd_cancel:
                dismiss();
                break;
            case R.id.mtd_ok:
                //呼び出し元のフラグメントを獲得
                Fragment target = getTargetFragment();
                if (target == null) {
                    dismiss();
                }
                //呼び出し元に渡したいデータをインテントに格納
                Intent data = new Intent();
                //今回は入力したテキスト
                data.putExtra(Intent.EXTRA_TEXT, switchEditText().getText().toString().trim());
                //呼び出し元でonActivityResultをオーバーライドしてdataの処理をすること
                target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);

                dismiss();
                break;
        }

    }

    /**
     * TextWatcher のメソッド、入力後の動作のみ指定している( afterTextChanged()
     *
     * @param charSequence
     * @param i
     * @param i1
     * @param i2
     */
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    //エディットテキスト入力後に呼び出される
    @Override
    public void afterTextChanged(Editable s) {
        // テキスト変更後に変更されたテキストを取り出す
        String inputStr = s.toString().trim();
        switch (reqCode) {
            case TASK:
                //タスク名は最低１文字いれないとダメ
                if (inputStr.length() == 0) {
                    btn_ok.setEnabled(false);
                } else {
                    btn_ok.setEnabled(true);
                }
                break;

            case MEMO:
                int lineCount = memoEdit.getLineCount();
                //メモは１０行以上はダメ
                if (10 < lineCount) {
                    memoEdit.setError("１０行以内です");
                    btn_ok.setEnabled(false);
                } else {
                    btn_ok.setEnabled(true);
                }
                break;
        }

    }

}

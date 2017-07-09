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

import android.widget.Toast;

/**
 * 削除確認ダイアログ
 *
 */
public class DeleteSelectDialog extends DialogFragment {


    private DeleteSelectDialogListener listener;

    public static DeleteSelectDialog newInstance (Fragment target, Cont.ReqCode reqCode, Cell cell ){
        DeleteSelectDialog dsd = new DeleteSelectDialog();
        dsd.setTargetFragment( target, reqCode.hashCode() );
        Bundle bundle = new Bundle();
        bundle.putSerializable( Cont.KEY_ARGUMENTS, cell );
        dsd.setArguments( bundle );
        return dsd;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //sqlIdの取得
        Bundle args = getArguments();
        final Cell cell = (Cell) args.getSerializable(Cont.KEY_ARGUMENTS);


        //アラートダイアログ画面
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage( cell.getTask() + "を削除しますか？")
                .setPositiveButton("削除", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DBAccess dba = new DBAccess( getActivity() );
                        dba.deleteCell( cell );
                        Toast.makeText (getActivity() , "削除しました", Toast.LENGTH_SHORT ).show();



                        //削除したのでフラグメントのページをリロードする
                        listener.reloadFragment( cell.getVisible() );

                    }

                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        return builder.create();
    }

    //以下リスナーの設定
    public interface DeleteSelectDialogListener{
        void reloadFragment(ListType listType);
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
        if(!(context instanceof DeleteSelectDialogListener)){
            throw new UnsupportedOperationException( "ListListener が実装されていません");
        } else {
            listener = (DeleteSelectDialogListener) context;
        }
    }

}


package com.tb_system.todolist;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import static com.tb_system.todolist.Cont.makeSentence;
import static com.tb_system.todolist.Cont.*;

/**
 * ヘルプダイアログ
 * 登録、変更ボタンを押して初めて insertSQL() するので
 * selCellに値を代入しても大丈夫
 */
public class DialogHelp extends DialogFragment implements AdapterView.OnItemClickListener {

    private HelpListener listener = null;

    @Override
    public Dialog onCreateDialog(Bundle bundle){
        Dialog d = new Dialog(getActivity());
        // DialogFragmentをタイトル無しにします
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //レイアウト設置
        d.setContentView(R.layout.help);
        //めいっぱい表示
        d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ListView helpList = (ListView)d.findViewById(R.id.list_help);

        String[] helpTitle = new String[]{
                "このアプリについて",
                "反復予定について",
                "予定の作成方法",
                "リストの種類と操作",
                "目のアイコンについて",
                "フキダシアイコンについて",

        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                helpTitle
        );

        helpList.setAdapter(adapter);
        helpList.setOnItemClickListener(this);

        //戻るボタンクリックでdismiss();
        ImageButton backBut = (ImageButton)d.findViewById(R.id.arrow_back);
        backBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return d;
    }


    @Override
    public void onItemClick(
            AdapterView<?> parent,
            View v,
            int pos,
            long id) {

        String s = "";

        switch (pos){
            case 0:
                s = getString(R.string.mess_help_0);
                break;
            case 1:
                s = getString(R.string.mess_help_1);
                break;
            case 2:
                s = getString(R.string.mess_help_2);
                break;
            case 3:
                s = getString(R.string.mess_help_3);
                break;
            case 4:
                s = getString(R.string.mess_help_4);
                break;
            case 5:
                s = getString(R.string.mess_help_5);
                break;

        }
        listener.makeDialog(DialogTag.MESSAGE, s);


    }



    //以下リスナーの設定
    public interface HelpListener{
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
        if(!(context instanceof HelpListener)){
            throw new UnsupportedOperationException( "MenuListenerが実装されていません");
        } else {
            listener = (HelpListener) context;
        }
    }




}

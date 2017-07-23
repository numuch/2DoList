package com.tb_system.todolist;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * メッセージボード
 * タイトルとメッセージを表示するクラス
 * メッセージは左右矢印でページ切り替えできる仕組み
 * 引数は表示したい文が詰まったString、それはカンマ区切りがちょいちょい入っている
 * タイトル，ページ１，ページ２，ページ３
 * のように、最初の区切までがタイトル、それ以降は文章である
 */
public class DialogMessage extends DialogFragment implements View.OnClickListener{

    private static final String ARGS_NAME = "sentence";
    private TextView messageBoard;
    private ImageView left;
    private List<String> messList;
    private int listNo = 0;
    private Dialog d;


    /**
     * このクラスをインスタンス化する場合はこのnewInstance()を使って
     * Stringを埋め込む
     * @param text
     * @return
     */
    public static DialogMessage newInstance(String text) {
        DialogMessage instance = new DialogMessage();
        //呼び出し元が指定する文字列
        Bundle args = new Bundle();
        args.putString(ARGS_NAME, text);
        instance.setArguments(args);
        //セットし終えたインスタンスを返す
        return instance;
    }

    /**
     * ダイアログクリエイト
     * 受けたStringをArrayListに格納している
     * Iteratorを使わない理由はページ戻りの時に不具合が出るから
     * @param bundle
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle bundle){
        d = new Dialog(getActivity());
        // DialogFragmentをタイトル無しにします
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //レイアウト設置
        d.setContentView(R.layout.dialog_message);
        //めいっぱい表示
        d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        //ゲットIDとクリックリスナー
        //タイトル
        TextView messTitle = (TextView)d.findViewById(R.id.message_title);
        //メッセージボードをタップするとページ送り
        messageBoard = (TextView)d.findViewById(R.id.message);
        messageBoard.setOnClickListener(this);
        //上部左側、左矢印（最初は非表示）
        left = (ImageView)d.findViewById(R.id.chevron_left);
        left.setOnClickListener(this);
        left.setVisibility(View.INVISIBLE);
        //上部右側、閉じるボタン
        ImageView right = (ImageView)d.findViewById(R.id.chevron_right);
        right.setOnClickListener(this);


        //newInstance()で埋め込んだバンドルゲット（メッセージを獲得）
        Bundle args = getArguments();
        if(args != null){
            //カンマで区切ってArrayListに格納する
            String s = args.getString(ARGS_NAME);
            messList = new ArrayList<>(Arrays.asList( s.split(",", 0 )));
            //タイトルに０番目の文字列、メッセージボードに１番目の文字列をセット
            messTitle.setText( messList.get( listNo ) );
            //最初の項をremove(0)して、ボードにタイトルが表示しないようにする
            messList.remove( listNo );
            messageBoard.setText( messList.get( listNo ) );

        }

        return d;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //左矢印を押した場合
            case R.id.chevron_left:
                //ページ数を戻す
                listNo--;
                //一番最初のページでは左矢印を非表示
                if(listNo == 0){
                    left.setVisibility(View.INVISIBLE);
                }
                //メッセージボードに表示（messListが所持しているページ数内限定）
                if( (0 <= listNo) && (listNo <= messList.size()-1) ){
                    messageBoard.setText( messList.get(listNo) );
                }
                break;

            case R.id.message:
                //リストＮＯを増やす
                listNo++;
                //左矢印復活（左矢印がない場合のみ）
                if(left.getVisibility() == View.INVISIBLE){
                    left.setVisibility(View.VISIBLE);
                }
                //メッセージボードに表示（ページ数以上の数字は受けつけない）
                if( (0 <= listNo) && (listNo <= messList.size()-1) ){
                    messageBoard.setText( messList.get(listNo) );
                }

                /**　ラストのページでタップした時に画面を閉じるかどうか？
                if(listNo == messList.size()){
                    d.dismiss();
                }
                **/

                break;

            case R.id.chevron_right:
                d.dismiss();
        }
    }




}





package com.tb_system.todolist;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * キャラコメント
 * バンドルに文字が入ってる場合は文字読み上げモード（readText）
 * 入ってない場合はコメント読み上げモード（readComment）
 * キャラが話しているように見えるようにつくる
 */
public class FragComment extends Fragment implements View.OnClickListener {

    private CommentListener listener;
    private static final String KEY_MESS = "comment";
    private TextView chat;
    private ListIterator<String> it = null;


    public static FragComment newInstance(String s) {
        FragComment com = new FragComment();
        Bundle args = new Bundle();
        args.putString(KEY_MESS, s);
        com.setArguments(args);
        return com;
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup con, Bundle sis) {
        View v = inf.inflate(R.layout.comment, con, false);

        //ゲットビュー＆クリックリスナー
        chat = (TextView) v.findViewById( R.id.chat);
        chat.setOnClickListener(this);
        FrameLayout container = (FrameLayout)v.findViewById( R.id.comment_container);
        container.setOnClickListener(this);
        ImageView imgDroid = (ImageView) v.findViewById( R.id.droid);
        imgDroid.setOnClickListener(this);
        ImageView imgClose = (ImageView) v.findViewById( R.id.close_chat);
        imgClose.setOnClickListener(this);

        //バンドルゲット
        Bundle bundle = getArguments();
        String s = null;
        if(bundle != null){
            s = bundle.getString(KEY_MESS);
        }

        //ヒント表示モード
        if (s == null) {
            readComment();
        }
        //文字表示モード
        else {
            //バンドルで受けた文字列はカンマで区切って格納（複数行有るため）
            List<String> list = new ArrayList<>(Arrays.asList( s.split(",", 0)) );
            it = list.listIterator();
            chat.setText( it.next() );
        }

        return v;
    }




    /**
     * キャラコメント表示
     */
    private void readComment() {

        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( getActivity() );
        //オリジナルコメントを獲得
        String s = spf.getString(getString(R.string.p_key_comment_org), "");

        //コメントが空なら閉じる、あるなら表示する
        if ( s.equals("") ) {
            removeThis();
        }
        else {
            List<String> commentList = new ArrayList<>(Arrays.asList( s.split(",", 0 )));
            //シャッフル後フキダシに文字をセット
            Collections.shuffle( commentList );
            chat.setText( commentList.get(0) );
        }
    }

    /**
     * バンドルゲット文字を表示
     */
    private void readText(){
        if (it.hasNext()) {
            chat.setText(it.next());
        } else {
            removeThis();
        }
    }

    /**
     *  このフラグメント自身を消す
     */
    private void removeThis(){
        listener.popContent( ListType.COMMENT, this );
    }

    /**
     * 分岐
     */
    private void readTextOrComment(){
        if(it == null){
            readComment();
        }else{
            readText();
        }
    }


    /**
     * クリック挙動　
     * it(イテレーター)が空ならばキャラコメへ
     * 有るならば文字の読み上げへ
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.chat:
                readTextOrComment();
                break;

            case R.id.droid:
                readTextOrComment();
                break;

            case R.id.comment_container:
                readTextOrComment();
                break;

            case R.id.close_chat:
                removeThis();
                break;
        }

    }

    //以下リスナーの設定
    public interface CommentListener{
        void popContent( ListType listType, Fragment fragment );
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
        if(!(context instanceof CommentListener)){
            throw new UnsupportedOperationException( "CommentListenerが実装されていません");
        } else {
            listener = (CommentListener) context;
        }
    }
}




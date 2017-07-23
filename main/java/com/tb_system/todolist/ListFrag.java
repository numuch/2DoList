package com.tb_system.todolist;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tb_system.todolist.Cont.*;
import java.util.List;

/**
 * 反復リスト　長押し時に達成リストへ移動（MainActのコールバック使用）
 */
public class ListFrag extends Fragment implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private ListListener listener = null;
    private  DBAccess dba;      //SQLにアクセス
    CellAdapter cellAdapter;    //オリジナルアダプタ（extends ArrayAdapter)


    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup con, Bundle sis) {

        dba = new DBAccess( getActivity() );             //SQLにデータアクセスするためのインスタンス作成
        //データ群（Cells）をSQLデータベースから獲得する
        String selection = Cont.Entry.COL_VISIBLE + "= ?" ;     // 	クエリ第三引数の検索条件　（SQL WHERE 句)
        String selectionArgs[] = new String[] {" "+ ListType.LIST.val +" "};  //クエリ第四引数の検索する値
        String orderBy = Cont.Entry.COL_COUNT;      // クエリの第七引数の並び順　（SQL ORDERBY 句）
        List<Cell> allCells = dba.reqData(selection, selectionArgs, orderBy);

        View v = inf.inflate(R.layout.frag_list, con, false);
        ListView listView = (ListView) v.findViewById(R.id.list_task);

        cellAdapter = null;

        //データ、アダプタ、ビューを紐付けする。
        // オリジナルのアダプタ（cellAdapter）とデータ（cells）を紐付け
        cellAdapter = new CellAdapter(
                getActivity(),
                R.layout.cell,      //cellのインデックス
                allCells            //データ
        );
        //ビュー（listView）とアダプタ（cellAdapter）を紐付け
        listView.setAdapter(cellAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        return v;
    }

    /**
     * リストちょい押し時の起動
     * @param parent
     * @param v
     * @param pos
     * @param id
     */
    @Override   //AdapterView.OnItemClickListener のメソッド
    public void onItemClick(
            AdapterView<?> parent,
            View v,
            int pos,
            long id) {


        Cell pushedCell = cellAdapter.getItem(pos);   //押されたポジションのセルを獲得

        StatusDialog sd = StatusDialog.newInstance(this, ReqCode.NULL, pushedCell);
        sd.show(getFragmentManager(),"status");

    }

    /**
     * リストの長押し時の挙動
     * @param parent
     * @param v
     * @param pos
     * @param id
     * @return
     */
    @Override
    public boolean onItemLongClick(
            AdapterView<?> parent,
            View v,
            int pos,
            long id) {

        Cell pushedCell = cellAdapter.getItem(pos);   //押されたポジションのセルを獲得
        String s = dba.checkDone( pushedCell, getActivity() );
        //達成リストへ移動
        listener.cellMoveList(ListType.LIST, ListType.CLEAR);
        //達成時に何かあればコメント
        if( !(s.equals("")) ){
            listener.showComment( s );
        }

        return true;    //ここでtrueを入れておくと、その後のイベントはキャンセルされる、つまり同時にonItemClick()が起動しない
    }

    //以下リスナーの設定
    public interface ListListener{
        void cellMoveList(ListType beforeList, ListType afterList);
        void showComment( String comment );
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
        if(!(context instanceof ListListener)){
            throw new UnsupportedOperationException( "ListListener が実装されていません");
        } else {
            listener = (ListListener) context;
        }
    }


}

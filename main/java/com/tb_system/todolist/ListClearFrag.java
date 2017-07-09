package com.tb_system.todolist;


import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import static com.tb_system.todolist.Cont.*;
import java.util.List;

/**
 * 達成リスト　長押し時に反復リストへ戻す（MainActのコールバック使用）
 */
public class ListClearFrag extends Fragment implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private  ClearListener listener = null;
    private  DBAccess dba;      //SQLにアクセス
    CellAdapter cellAdapter;    //オリジナルアダプタ（extends ArrayAdapter)


    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup con, Bundle sis) {

        dba = new DBAccess( getActivity() );             //SQLにデータアクセスするためのインスタンス作成
        //データ群（Cells）をSQLデータベースから獲得する
        String selection = Cont.Entry.COL_VISIBLE + "= ?" ;     // 	クエリ第三引数の検索条件　（SQL WHERE 句)
        String selectionArgs[] = new String[] {" "+ ListType.CLEAR.val +" "};  //クエリ第四引数の検索する値
        String orderBy = Cont.Entry.COL_COUNT;      // クエリの第七引数の並び順　（SQL ORDERBY 句）
        List<Cell> allCells = dba.reqData(selection, selectionArgs, orderBy);

        //中身が空のばあいは
        if(allCells.size() == 0){
            //空のビューを返す
            return null;
        }
        //中身があるならば
        else {
            View v = inf.inflate(R.layout.frag_clear, con, false);
            ListView listView = (ListView) v.findViewById(R.id.list_task);

            this.cellAdapter = null;

            //データ、アダプタ、ビューを紐付けする。
            // オリジナルのアダプタ（cellAdapter）とデータ（cells）を紐付け
            cellAdapter = new CellAdapter(
                    getActivity(),
                    R.layout.cell,
                    allCells                //データ
            );
            //ビュー（listView）とアダプタ（cellAdapter）を紐付け
            listView.setAdapter(cellAdapter);
            listView.setOnItemClickListener(this);
            listView.setOnItemLongClickListener(this);
            return v;
        }
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
        //セルの変化をＳＱＬに記録
        dba.checkDone( pushedCell, getActivity() );
        //達成リストから反復リストへ戻す
        listener.cellMoveList(ListType.CLEAR, ListType.LIST);

        return true;    //ここでtrueを入れておくと、その後のイベントはキャンセルされる、つまり同時にonItemClick()が起動しない

    }

    //以下リスナーの設定
    public interface ClearListener{
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
        if(!(context instanceof ClearListener)){
            throw new UnsupportedOperationException( "ClearListener が実装されていません");
        } else {
            listener = (ClearListener) context;
        }

    }



}




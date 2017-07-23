package com.tb_system.todolist;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tb_system.todolist.Cont.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 単発予定リスト　長押しは設定しない
 *
 */
public class ListOnce extends Fragment implements AdapterView.OnItemClickListener {


    CellAdapter cellAdapter;    //オリジナルアダプタ（extends ArrayAdapter)

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup con, Bundle sis) {
        DBAccess dba = new DBAccess( getActivity() );             //SQLにデータアクセスするためのインスタンス作成

        //データ群（Cells）をSQLデータベースから獲得する
        String selection = Cont.Entry.COL_VISIBLE + "= ?" ;     // 	クエリ第三引数の検索条件　（SQL WHERE 句)
        String selectionArgs[] = new String[] {" "+ ListType.ONCE.val +" "};  //クエリ第四引数の検索する値
        String orderBy = Cont.Entry.COL_COUNT;      // クエリの第七引数の並び順　（SQL ORDERBY 句）
        List<Cell> allCells = dba.reqData(selection, selectionArgs, orderBy);

        //このカテゴリの用件がゼロで、プリファレンスで常にリストバーを表示にチェックを入れていない場合
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( getActivity() );
        String key = getString(R.string.pref_list_bar_visible);
        boolean b = spf.getBoolean( key , false );
        if( (allCells.size() == 0) && (!b) ){
            //空のビューを返す
            return null;
        }
        //中身がある、もしくは常に表示するにチェックが入っていたならば
        else {
            View v = inf.inflate(R.layout.frag_once, con, false);
            ListView listView = (ListView) v.findViewById(R.id.list_task);

            cellAdapter = null;

            //実際にリストに表示するデータ
            List<Cell> listCell = new ArrayList<>();
            //allListVisibleがtrueならば、無条件で全て表示
            String pKeyEye = getString(R.string.p_key_list_all_visible);
            boolean allListVisible = spf.getBoolean(pKeyEye, false);
            if( allListVisible ) {
                listCell = allCells;
            }
            //falseならばプリファレンスの条件分岐で仕分ける
            else {
                //◯日先まで表示するのか
                String s = spf.getString( getString(R.string.pref_future_task_visible), "9999");
                int future = Integer.parseInt(s);
                //過去のものを表示するのか
                boolean past = spf.getBoolean( getString(R.string.pref_past_task_visible), false);


                for(Cell cell : allCells){
                    //過去のものを表示するならば
                    if(past){
                        //指定日数内のものを全て追加
                        if(cell.getCount() <= future ){
                            listCell.add(cell);
                        }
                    }
                    //過去のものを表示しないならば
                    else {
                        if( (0 <= cell.getCount()) && (cell.getCount() <= future) ){
                            listCell.add(cell);
                        }
                    }
                }
            }


            //データ、アダプタ、ビューを紐付けする。
            // オリジナルのアダプタ（cellAdapter）とデータ（cells）を紐付け
            cellAdapter = new CellAdapter(
                    getActivity(),
                    R.layout.cell,
                    listCell                //データ
            );
            //ビュー（listView）とアダプタ（cellAdapter）を紐付け
            listView.setAdapter(cellAdapter);
            listView.setOnItemClickListener(this);

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



}




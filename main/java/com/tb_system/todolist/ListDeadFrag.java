package com.tb_system.todolist;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.tb_system.todolist.Cont.*;
import java.util.Calendar;
import java.util.List;

/**
 * デッドリスト　長押し時に反復リストへ復帰する（MainActのコールバック使用）
 */
public class ListDeadFrag extends Fragment implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private DeadListener listener = null;
    DBAccess dba;
    CellAdapter cellAdapter;    //オリジナルアダプタ（extends ArrayAdapter)


    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup con, Bundle sis) {

        dba = new DBAccess( getActivity() );             //SQLにデータアクセスするためのインスタンス作成
        //データ群（Cells）をSQLデータベースから獲得する
        String selection = Cont.Entry.COL_VISIBLE + "= ?" ;     // 	クエリ第三引数の検索条件　（SQL WHERE 句)
        String selectionArgs[] = new String[] {" "+ ListType.DEAD.val +" "};  //クエリ第四引数の検索する値
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
            View v = inf.inflate(R.layout.frag_dead, con, false);
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

        StatusDialog sd = StatusDialog.newInstance(this, ReqCode.NULL , pushedCell);
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

        //今日の日付を００時００分０１秒にセットする
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 1);
        pushedCell.setStartDay(c);
        pushedCell.setCount( pushedCell.getFrequency() );
        pushedCell.setStack( pushedCell.getFrequency() );
        pushedCell.setCombo( 0 );
        pushedCell.setVisible(ListType.LIST);

        //内容をSQLに書き込む
        dba.cellSetInSQL(pushedCell, ReqCode.UPDATE);

        //デッドリストとリストを更新
        listener.cellMoveList(ListType.DEAD, ListType.LIST);

        Toast.makeText (getActivity() , "リトライ開始", Toast.LENGTH_SHORT ).show();

        return true;    //ここでtrueを入れておくと、その後のイベントはキャンセルされる、つまり同時にonItemClick()が起動しない

    }


    //以下リスナーの設定
    public interface DeadListener{
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
        if(!(context instanceof DeadListener)){
            throw new UnsupportedOperationException( "DeadListenerが実装されていません");
        } else {
            listener = (DeadListener) context;
        }
    }


}




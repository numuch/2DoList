package com.tb_system.todolist;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import static com.tb_system.todolist.Cont.*;
import static com.tb_system.todolist.Cont.Entry.COL_COMBO;
import static com.tb_system.todolist.Cont.Entry.COL_COUNT;
import static com.tb_system.todolist.Cont.Entry.COL_DONE;
import static com.tb_system.todolist.Cont.Entry.COL_FREQ;
import static com.tb_system.todolist.Cont.Entry.COL_GOAL;
import static com.tb_system.todolist.Cont.Entry.COL_MEMO;
import static com.tb_system.todolist.Cont.Entry.COL_STACK;
import static com.tb_system.todolist.Cont.Entry.COL_START;
import static com.tb_system.todolist.Cont.Entry.COL_TASK;
import static com.tb_system.todolist.Cont.Entry.COL_VISIBLE;
import static com.tb_system.todolist.Cont.Entry.TABLE_NAME;
import static com.tb_system.todolist.Cont.Entry._ID;

import static com.tb_system.todolist.Cont.VAL_FALSE;
import static com.tb_system.todolist.Cont.VAL_TRUE;
import static com.tb_system.todolist.Cont.deleteSQL;
import static com.tb_system.todolist.Cont.updateSQL;

/**
 * 主にデータベース（ＳＱＬ）の操作
 * ContやEntryという文字をimportで省略宣言しています
 */
public class DBAccess extends SQLiteOpenHelper {

    private String startInfo = ""; //taskJudge()での戻り値用

    //コンストラクタ
    public DBAccess(Context c) {
        super(c, DB_NAME, null, DB_VERSION);
    }

    //メソッド：データベースの生成
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( createSQL() );         //テーブルを作る
//        db.execSQL(SQL_INIT);         //初期値を入れる
    }

    //メソッド：データベースのアップグレード
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE);            //以前のテーブルを消す
        onCreate(db);                           //再びonCreate()する
    }


    /**
     * カーソルの引数について
     * （1 テーブル名 2 カラム名 3 selection 4 selectionArgs 5 group by 6 Having 7 order by
     * @return
     */

    public List<Cell> reqData(String selection, String selectionArgs[], String orderBy){
        //cell群を格納していくcellsを作る
        List<Cell> cells = new ArrayList<Cell>();
        //カーソルによるリクエスト取得
        SQLiteDatabase db = this.getReadableDatabase();
        // テーブル名、全てのカラム、検索条件（WHERE)、検索条件内で?に入れた値、(GROUPBY)、 (HAVING)、　並び順（ORDERBY）
        Cursor c = db.query( TABLE_NAME , ALL_COLUMNS , selection , selectionArgs , null, null, orderBy);

        //moveToFirst カーソルが最初の行に移動。カーソルが空の場合はfalseが返るが、空でないのでtrueが入る
        boolean isEof = c.moveToFirst();
        // cells に addしていく。カーソルを使ってSQL内のデータを追加していく。
        while (isEof) {
            //１セル
            Cell cell = new Cell();

            cell.setSqlId( c.getInt(c.getColumnIndex(_ID)) );
            cell.setStartDay( c.getString(c.getColumnIndex(COL_START)) );
            cell.setFrequency( c.getInt(c.getColumnIndex(COL_FREQ)) );
            cell.setTask( c.getString(c.getColumnIndex(COL_TASK)) );
            cell.setCombo( c.getInt(c.getColumnIndex(COL_COMBO)) );
            cell.setDone( c.getInt(c.getColumnIndex(COL_DONE)) );
            cell.setStack( c.getInt(c.getColumnIndex(COL_STACK)) );
            cell.setCount( c.getInt(c.getColumnIndex(COL_COUNT)) );
            cell.setGoal( c.getInt(c.getColumnIndex(COL_GOAL)) );
            cell.setVisible( c.getInt(c.getColumnIndex(COL_VISIBLE)) );
            cell.setMemo( c.getString(c.getColumnIndex(COL_MEMO)) );
            cells.add( cell );
            //次の行へ（moveToNext）
            isEof = c.moveToNext(); // 次の行がなくなるとfalseを返す→ループ終了
        }
        c.close();
        db.close();
        return cells;
    }


    /**
     * タスクジャッジ　起動時にタスクの判定をするメソッド
     * 　　　　　　　　変化が起きた場合は「startInfo」にメッセージを追加していく
     * @return リストの変更情報　startInfo
     */
    public String taskJudge() {
        //データ群（Cells）を絞り込みなしでSQLデータベースから獲得する
        List<Cell> allCells = reqData(null, null, null);


        //foreach構文（リスト内の要素　：　データの入ったリスト） allCells の中身がなくなるまで順次 cell に内容を入れてくれる
        for (Cell cell : allCells) {
            //スイッチ判定用　list
            ListType list = cell.getVisible();
            //開始日と今日との日数差分 diffDay ＝ 予定が未来ならプラス値
            int diffDay = cell.getDiffDays();

            //それぞれの cell がどのリストに有るかによって判定方法が変わる（ただしデッドリストは判定しない）
            switch (list){
                case ONCE:
                    cell.setCount( diffDay );
                    //もし予定日当日ならば
                    if(diffDay == 0){
                        makeSentence(new Object[]{startInfo, "今日は「", cell.getTask(),"」があります",","}) ;

                    }
                    break;
                case WAIT:
                    cell.setCount( diffDay );
                    //開始日以降になったら
                    if (diffDay <= 0) {
                        //期限計算と期限判定（アウトだったらデッドリスト行き）
                        cell.setCount(cell.getWithinDays());
                        countJudge(cell, diffDay);
                        //デッドリストでないならリストに戻す
                        if (!(cell.getVisible() == ListType.DEAD)) {
                            cell.setVisible(ListType.LIST);
                            makeSentence(new Object[]{startInfo, "「", cell.getTask(), "」は今日からスタートです",","});
                          }
                    }
                    break;
                case LIST:
                    //期限計算と期限判定（アウトだったらデッドリスト行き）
                    cell.setCount( cell.getWithinDays() );
                    countJudge(cell, diffDay);
                    break;
                case CLEAR:
                    //達成マークを解除
                    cell.setDone(VAL_FALSE);
                    //期限計算と期限判定（アウトだったらデッドリスト行き）
                    cell.setCount( cell.getWithinDays() );
                    countJudge(cell, diffDay);

                    //デッドリストでないならリストに戻す
                    if( !(cell.getVisible() == ListType.DEAD) ){
                        cell.setVisible(ListType.LIST);
                        if(cell.getGoal() == cell.getCombo()){
                            makeSentence(new Object[]{startInfo, "「", cell.getTask(), "」が目標達成しました！\nおめでとうございます！",","});
                        }
                    }
                    break;
                }

            //SQLの更新
            cellSetInSQL(cell, ReqCode.UPDATE);

        }

        return startInfo;

    }

    private void countJudge(Cell cell, int diffDay) {
        //もし期限が０以下になったのなら
        if (cell.getCount() <= 0) {
            //デッドリストへ
            cell.setVisible(ListType.DEAD);
            makeSentence(new Object[]{startInfo,  "「", cell.getTask(), "」は達成失敗になりました","," });
            //カウント値に実施日数を表示したままにする
            cell.setCount(  Math.abs(diffDay) );
        }
    }



    //SQL文は間違えやすいのであちこちに記述せずにここにまとめる
    private void updateSql(SQLiteDatabase database, String columnName, String value, String sqlId){
        database.execSQL("UPDATE "+ TABLE_NAME + " SET " + columnName + " = " + value + " WHERE " + _ID + " = " + sqlId + ";" );
    }

    //タスク完了のチェック(リスト、クリアリストの２つの処理をここにまとめる
    public String checkDone(Cell cell, Context con){
        String s = "";
        SQLiteDatabase db = this.getWritableDatabase();

        //反復リストにいた場合
        if(cell.getVisible() == ListType.LIST){
            cell.setDone(VAL_TRUE);
            cell.setCombo_PlusValue(1);
            cell.setStack_PlusValue( cell.getFrequency() );
            cell.setVisible(ListType.CLEAR);
            //SQLに書き込む
            cellSetInSQL(cell, ReqCode.UPDATE);

            //お祝い表示を有効にしている場合
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( con );
            boolean congr = prefs.getBoolean( con.getString(R.string.pref_congratulations) , false);
            if(congr) {
                //クリックした用件が目標値に達したかをチェック
                if(cell.getGoal() == cell.getCombo()){
                    s = makeSentence(new Object[]{cell.getTask(), "が目標値に達しました！\nおめでとうございます！,"});
                }
                //「全てクリアされたかどうか」をチェック
                if (checkAllClear()) {
                    s =  makeSentence(new Object[]{ s, "全件達成！お疲れ様です！" });
                }
            }
        }
        //達成リストに有った場合
        else if(cell.getVisible() == ListType.CLEAR){
            cell.setDone(VAL_FALSE);
            cell.setCombo_PlusValue(-1);
            cell.setStack_PlusValue( -cell.getFrequency() );
            cell.setVisible(ListType.LIST);
            //SQLに書き込む
            cellSetInSQL(cell, ReqCode.UPDATE);
        }

        db.close();

        return s;

    }

    //【長押し時チェック】全件達成したならtrueを返す
    private boolean checkAllClear(){
        //リストに存在しているものを獲得
        String selection = makeSentence(new Object[]{COL_VISIBLE, "= ?"}); // 	クエリ第三引数の検索条件　（SQL WHERE 句)
        String selectionArgs[] = new String[] {"" + ListType.LIST.val + ""};  //クエリ第四引数の検索する値
        List<Cell> allCells = reqData(selection, selectionArgs, null);
        //もし空ならば、全件達成
        return (allCells.size() == 0);
    }






    //項目を削除するメソッド
    public void deleteCell(Cell cell){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL( deleteSQL(cell) );
        db.close();
    }

    /**
     *     cellの内容をまるごとSQLに記録するメソッド
     *     visible　と　stack　の値は変更の必要ありなので
     *     beforeSQLWrite() メソッドを使って変更する
     *     writeMode = insert or update
     */
    public void cellSetInSQL(Cell cell, ReqCode reqCode){
        //SQLに書き込み
        SQLiteDatabase db = this.getWritableDatabase();

        //新規のcell
        if(reqCode == ReqCode.NEW){
            //インサート
            db.execSQL( insertSQL( cell ) );
        }
        //新規ではない
        else if(reqCode == ReqCode.UPDATE){
            //アップデート
            db.execSQL( updateSQL( cell ) );
        }

        db.close();
    }




}


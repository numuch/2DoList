package com.tb_system.todolist;

import android.provider.BaseColumns;

import static android.provider.BaseColumns._ID;
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

/**
 * 定数保存
 */
public final class Cont {
    //空のコンストラクタ
    private Cont() {} // インスタンス化できないようにする

    public enum DialogTag {
        MESSAGE,
        HELP,

    }

    public enum ReqCode {
        NULL,
        FREQ,   //実行頻度の数値を獲得したい場合
        GOAL,   //目標コンボ数の数値を獲得したい場合
        TASK,   //タスク名を打ち込みたい場合
        MEMO,   //メモの内容を打ち込みたい場合
        NEW,    //タスクの新規作成時
        UPDATE,//タスクの変更時
        DELETE,//消去用
    }
    //リクエストコード（数値）はハッシュコードを使ってみる
    public static ReqCode getReqCodeByHashCode(int reqCode){
        ReqCode rq = ReqCode.NULL;
        for(ReqCode code : ReqCode.values()){
            if(code.hashCode() == reqCode){
                rq = code;
            }
        }
        return rq;
    }


    //日付用
    public enum DateFormat {
        SIMPLE("M/d"),
        WEEK("(E)"),
        FULL("yyyy年M月d日 E曜日"),
        CHECK("yy/MM/dd（E）HH:mm:ss"),
        SQL("yyyyMMdd HH:mm:ss");
        String str;

        DateFormat(String str){
            this.str = str;
        }
    }



    //数値
    public static final int VAL_FALSE = 0;
    public static final int VAL_TRUE = 1;


    public static final int MILL_OF_DAY = 1000 * 60 * 60 * 24 ;
    //引数獲得キー
    public static final String KEY_ARGUMENTS = "key_args";



    /**
     * SQLに関わる定数
     */

    public static final String DB_NAME = "ToDo.db";   //データベースの名前
    public static final int DB_VERSION = 1;             //バージョン

    //各カラム名
    public static abstract class Entry implements BaseColumns {
        public static final String TABLE_NAME = "taskList";     //ＳＱＬのテーブル名
        public static final String COL_START = "startDay";      //開始日
        public static final String COL_FREQ = "frequency";      //反復頻度
        public static final String COL_TASK = "task";           //予定名
        public static final String COL_STACK = "stack";         //蓄積値
        public static final String COL_COMBO = "combo";         //連続達成回数
        public static final String COL_DONE = "done";           //達成、未達成
        public static final String COL_COUNT = "countDown";     //期限
        public static final String COL_GOAL = "goal";           //目標値
        public static final String COL_VISIBLE = "visible";     //リストの種類
        public static final String COL_MEMO = "memo";           //メモ

    }
    //接続子
    private static final String INT_TYPE = " INTEGER";
    private static final String TEX_TYPE = " TEXT";
    public static final String COM = ",";

    //全てのカラム
    public static final String ALL_COLUMNS[] = new String[]{_ID, COL_START, COL_FREQ, COL_TASK, COL_COMBO, COL_DONE,COL_STACK, COL_COUNT, COL_GOAL, COL_VISIBLE, COL_MEMO };


    //テーブル生成のＳＱＬ文
    public static String createSQL() {
        return makeSentence(new Object[]{
                "CREATE TABLE ", TABLE_NAME, " (",
                _ID, " INTEGER PRIMARY KEY,",
                COL_START, TEX_TYPE, COM,
                COL_FREQ, INT_TYPE, COM,
                COL_TASK, TEX_TYPE, COM,
                COL_COMBO, INT_TYPE, COM,
                COL_DONE, INT_TYPE, COM,
                COL_STACK, INT_TYPE, COM,
                COL_COUNT, INT_TYPE, COM,
                COL_GOAL, INT_TYPE, COM,
                COL_VISIBLE, INT_TYPE, COM,
                COL_MEMO, TEX_TYPE, ");"
        });
    }


    //テーブル削除のＳＱＬ文
    public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    //データ新規登録のＳＱＬ文

    public static String insertSQL(Cell cell){
        return makeSentence(new Object[]{"INSERT INTO ", TABLE_NAME, " (",
                COL_START, COM,
                COL_FREQ, COM,
                COL_TASK, COM,
                COL_COMBO, COM,
                COL_DONE, COM,
                COL_STACK, COM,
                COL_COUNT, COM,
                COL_GOAL, COM,
                COL_VISIBLE, COM,
                COL_MEMO, ") VALUES (",
                cell.getStartDay_WritableSQL(), COM,
                cell.getFrequency(), COM,
                cell.getTask_WritableSQL(), COM,
                cell.getCombo(), COM,
                cell.isDone_WritableSQL(), COM,
                cell.getStack(), COM,
                cell.getCount(), COM,
                cell.getGoal(), COM,
                cell.getVisible_WritableSQL(), COM,
                cell.getMemo_WritableSQL(), ")"
        });

    }

    //データ削除のＳＱＬ文
    public static String deleteSQL(Cell cell) {
        return makeSentence(new Object[]{
                "DELETE FROM ", TABLE_NAME, " WHERE ", _ID, " = ", cell.getSqlId(), ";"
        });
    }

    //データ更新（id指定）
    //StringBuilderは大抵のものは自動でtoString()してくれる
    //文字列に関してはSQLにおいて''で囲まなければならないので、_WritableSQL()のほうでゲットしている
    public static String updateSQL(Cell cell){

        return makeSentence(new Object[]{"UPDATE ", TABLE_NAME, " SET ",
                COL_START, " = ", cell.getStartDay_WritableSQL(), COM,
                COL_FREQ, " = ", cell.getFrequency(), COM,
                COL_TASK, " = ", cell.getTask_WritableSQL(), COM,
                COL_COMBO, " = ", cell.getCombo(), COM,
                COL_DONE, " = ", cell.isDone_WritableSQL(), COM,
                COL_STACK, " = ", cell.getStack(), COM,
                COL_COUNT, " = ", cell.getCount(), COM,
                COL_GOAL, " = ", cell.getGoal(), COM,
                COL_VISIBLE, " = ", cell.getVisible_WritableSQL(), COM,
                COL_MEMO, " = ", cell.getMemo_WritableSQL(),
                "WHERE ", _ID,  " = ", cell.getSqlId(), ";"
        });
    }

    //データ更新ピンポイント
//    db.execSQL("UPDATE taskList SET done = 1 WHERE _id = 1 ;" );
//    String s = "UPDATE "+ TABLE_NAME + " SET " + cell.getTask_WritableSQL() + " = " + value + " WHERE " + _ID + " = " + sqlId + ";"





    //StringBuilderで文字構成して戻すメソッド
    public static String makeSentence(Object[] enableToStringObject){
        StringBuilder sb = new StringBuilder();
        for(Object s : enableToStringObject){
            sb.append(s);
        }
        return new String(sb);
    }
}

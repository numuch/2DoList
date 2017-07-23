package com.tb_system.todolist;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import static com.tb_system.todolist.Cont.*;

/**
 * セル　タスクの情報を保存する
 */
public class Cell implements Serializable{
    private int sqlId;          //SQLの自動連番
    private Calendar startDay;          //開始日
    private int frequency;          //頻度
    private String task;          //予定名
    private int combo;          //コンボ
    private boolean done;       //完了　未完
    private int stack;          //蓄積数
    private int count;          //カウント
    private int goal;          //目標
    private ListType visible;          //リストのどこに表示するか
    private String memo;        //メモ


    //初期値
    Cell(){
        this.sqlId = 0 ;
        this.startDay = setZero( Calendar.getInstance() );     //今日の日付
        this.frequency = 1 ;                         //毎日実行
        this.task = null ;
        this.combo = 0 ;
        this.done = false ;
        this.stack = 0;
        this.count = 0 ;
        this.goal = 10 ;
        this.visible = ListType.NONE ;
        this.memo = "" ;

    }
    /**
     * 開始日を００時００分０１秒にセットするメソッド
     */
    private Calendar setZero(Calendar c){
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 1);
        return c;
    }
    //内容一括表示（主にデバック用？）
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("　◆用件名：").append(task);
        sb.append("　◆開始日：").append(getStartDay( DateFormat.CHECK ));
        sb.append("　◆実行頻度：").append(frequency).append("　◆完了：").append(done).append("　◆蓄積：").append(stack).append("　◆カウント：").append(count);
        sb.append("　◆リスト位置：").append(visible.str);
        sb.append("　◆連続達成：").append(combo).append("　◆目標：").append(goal).append("　◆ID：").append(sqlId).append("　◆メモ：").append(memo);

        return new String(sb);
    }
    /**
     * 各種ゲッターセッター
     *
     */

    /**
     * 特殊値のゲッター
     * diffDay 日数差分
     * 用件開始日（０時０分１秒）から現在の時間を引くメソッド
     * 開始日が未来の場合は今日の０時０分０秒の時点を引く
     * 過去の場合は今日の０時補正を行わない
     */
    public int getDiffDays(){
        Calendar now = Calendar.getInstance();
        //セル内のカレンダーが未来を指すときだけ
        if (now.before(startDay)) {
            //今日の日付を０時０分０秒に補正する
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);
        }

        long diffTime = startDay.getTimeInMillis() - now.getTimeInMillis();

        final int MILL_OF_DAY = 1000 * 60 * 60 * 24;
        return (int) (diffTime / MILL_OF_DAY);
    }

    /**
     * 特殊値のゲッター
     * カウント値に期限を割り当てて表示するようにしているが
     * その値を求めるための式
     * 開始日から２日経っているならば diffDaysは－２になる
     * 蓄積値からそれを引いた値が期限になる
     * within 期限
     */
    public int getWithinDays(){
        return stack + getDiffDays();
    }


    /**
     * SQL＿ID
     */
    public int getSqlId() {
        return sqlId;
    }


    public void setSqlId(int sqlId) {
        this.sqlId = sqlId;
    }

    /**
     * 開始日
     */

    //ゲッター
    public Calendar getStartDay() {return startDay;}

    public String getStartDay_WritableSQL(){
        SimpleDateFormat sdf = new SimpleDateFormat( DateFormat.SQL.str, Locale.JAPAN);
        String str = sdf.format(startDay.getTime());

        StringBuilder sb = new StringBuilder();
        sb.append("'");
        sb.append(str);
        sb.append("'");
        return new String(sb);
    }

    // 整形した日時を返す
    public String getStartDay(DateFormat DATE_FORMAT) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT.str , Locale.JAPAN);
        return sdf.format( startDay.getTime() );
    }

    //セッター
    public void
    setStartDay(Calendar startDay) {this.startDay = startDay;}

    //セッター　SQLより受ける場合
    public void setStartDay(String startDay) {
        //SQLから文字列で受けてここでカレンダー型に変換して格納
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(new SimpleDateFormat( DateFormat.SQL.str , Locale.JAPAN ).parse( startDay ));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.startDay = cal;
    }

    /**
     * 頻度
     */
    //ゲッター
    public int getFrequency() {
        return frequency;
    }



    //セッター
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }


    /**
     * タスク名
     * ＳＱＬ文のストリングは　’’で囲まないと駄目
     */
    public String getTask() {return task;}

    public String getTask_WritableSQL() {
        StringBuilder sb = new StringBuilder();
        sb.append("'");
        sb.append(task);
        sb.append("'");
        return new String(sb);
        }

    public void setTask(String task) {
        this.task = task;
    }

    /**
     * コンボ
     */
    public int getCombo() {return combo;}


    public String getComboStr() {return String.valueOf(this.combo);}


    public void setCombo(int combo) {this.combo = combo;}

    public void setCombo_PlusValue(int valuePlusOrMinus ){
        combo = combo + valuePlusOrMinus;
    }

    /**
     * 完了状態
     */


    public String isDone_WritableSQL() {
        String s;
        if(done){s = "1";}else {s = "0";}
        return s;
    }

    //完了時に true　、　未完了時に false　を設定する
    //sqlの絡みもあって数値でtrue,falseを判断
    public void setDone(int done) {
        if(done == 0) {
            this.done = false;
        }else if(done == 1){
            this.done = true;
        }}

    /**
     * 蓄積数
     */
    public int getStack() {return stack;}


    public void setStack(int stack) {this.stack = stack;}

    public void setStack_PlusValue(int valuePlusOrMinus ) {
        stack = stack + valuePlusOrMinus;
    }


    /**
     * カウント
     */
    public int getCount() {
        return count;
    }

    public String getCountStr() {return String.valueOf(this.count); }


    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 目標
     */

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    /**
     * 表示リスト
     */
    public ListType getVisible() {
        return visible;
    }
    public String getVisible_WritableSQL() {
        return String.valueOf( visible.getVal() );
    }

    public void setVisible(int visible) {
        this.visible = ListType.getType( visible );
    }
    public void setVisible(ListType listType) {
        this.visible = listType;
    }
	
	

    /**
     * メモ
     */
    public String getMemo() {
        return memo;
    }

    public String getMemo_WritableSQL() {
        StringBuilder sb = new StringBuilder();
        sb.append("'");
        sb.append(memo);
        sb.append("'");
        return new String(sb);
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}

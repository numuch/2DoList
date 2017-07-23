package com.tb_system.todolist;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

import static com.tb_system.todolist.Cont.DateFormat;
import static com.tb_system.todolist.Cont.makeSentence;

/**
 * Created by tb on 2016/06/25.
 */

public class CellAdapter extends ArrayAdapter<Cell> {

    private LayoutInflater layoutInflater;
    private int layoutResource ;

    public CellAdapter(Context context, int resource, List<Cell> objects) {
        super(context, resource, objects);

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;

    }

    @Override
    public View getView(int pos , View convertView, ViewGroup parent) {

        // 特定の行(position)のデータを得る
        Cell cell = getItem( pos );

        // convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
        if (null == convertView) {
            convertView = layoutInflater.inflate ( layoutResource , null);
        }


        // 表示するリストによって　表示内容を変え無くてはならない
        ListType type = cell.getVisible();


        //左のカラム
        TextView leftSide = (TextView) convertView.findViewById(R.id.left);
        //主として入るstring
        final String startDay = cell.getStartDay( DateFormat.SIMPLE );

        switch ( type ){
            case ONCE:
                if(cell.getCount() < 0){
                    leftSide.setTextColor( Color.GRAY );
                }
                leftSide.setText( startDay );
                break;
            case WAIT:
                leftSide.setText( startDay );
                break;
            case LIST:
                if(cell.getCount() == 1){
                    leftSide.setTextColor( Color.RED );
                }
                leftSide.setText( makeSentence(new Object[]{cell.getCount(), "日"}) );
                break;
            case CLEAR:
                leftSide.setText( "達成" );
                break;
            case DEAD:
                leftSide.setText( makeSentence(new Object[]{cell.getGoal(), "回"} ) );
                break;
            default:
                leftSide.setText( cell.getCountStr() );
        }

        //中央のカラム（ほとんど用件名
        TextView center = (TextView) convertView.findViewById(R.id.center);
        if(cell.getCount() < 0){
            center.setTextColor( Color.GRAY );
        }
        center.setText( cell.getTask() );

        //右のカラム
        TextView rightSide = (TextView) convertView.findViewById(R.id.right);
        //主として入るstring
        String daysAway = wordInCellAdapter(ListType.ONCE, cell);


        switch ( type ){
            case ONCE:
                if(cell.getCount() < 0){
                    rightSide.setTextColor( Color.GRAY );
                }
                rightSide.setText( daysAway );
                break;
            case WAIT:
                rightSide.setText( daysAway );
                break;
            default:
                String listWord = cell.getComboStr() + "回";
                rightSide.setText( listWord );
        }

        //リストにあるものに限ってチェックを押すと横線が入る
        //完・未完表示とコンボ表示のビューセット（クリックで文字が変わる部分）
        int backColor;

        TextPaint paint = center.getPaint();

        //達成リストに居るときだけ
        if(cell.getVisible() == ListType.CLEAR) {
            //背景のｘｍｌを暗くして
            backColor = R.drawable.task_done;
            //線を入れる
            paint.setFlags(center.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            // アンチエイリアスをオンにする
            paint.setAntiAlias(true);
        }else {                  //タスク未完状態のビューセット
            backColor = R.drawable.task_not_yet;
            paint.setFlags(center.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        convertView.setBackgroundResource(backColor);


        return convertView;
    }

    //cellAdapter内の文字表現を設定する
    private String wordInCellAdapter(ListType listType, Cell cell) {
        String word;
        //リストタイプで分岐（返す文字はカウント値だったり、頻度だったり色々
        switch (listType){

            case LIST:
                if(cell.getCount() == 1){word = "今日中";}
                else{word = cell.getCountStr() + "日";}
                return word;

            case ONCE:
                switch (cell.getCount()) {
                    case -1:
                        word = "昨日";
                        break;
                    case 0:
                        word = makeSentence(new Object[]{"今日", cell.getStartDay( DateFormat.WEEK) });
                        break;
                    case 1:
                        word =  makeSentence(new Object[]{"明日", cell.getStartDay( DateFormat.WEEK) });
                        break;
                    default:
                        if(cell.getCount() < 0){
                            word = makeSentence(new Object[]{ Math.abs( cell.getCount() ), "日前", cell.getStartDay( DateFormat.WEEK ) });
                        } else {
                            word = makeSentence(new Object[]{ cell.getCount(), "日後", cell.getStartDay( DateFormat.WEEK )});
                        }
                        break;
                }
                return word;

            default:
                word = "あと" +cell.getCountStr()+ "日";
                return word;
        }
    }



}

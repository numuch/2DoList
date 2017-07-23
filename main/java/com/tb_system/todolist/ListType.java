package com.tb_system.todolist;

/**
 * enum リストやトップアイコンに番号を振ったもの
 */
public enum ListType {
    NONE(0,"none"),
    ONCE(11,"list_once"),
    LIST(12,"list_list"),
    CLEAR(13,"list_clear"),
    DEAD(14,"list_dead"),
    WAIT(15,"list_wait"),
    BAR(16,"top_bar"),
    COMMENT(17,"popUP_comment"),
    PREF_COMMENT(18,"pref_comment"),
    PREF_SETTING(19,"pref_setting"),
    PREF_ADD(20,"pref_addTask"),
    PREF_VIEW(21,"pref_view");
    int val;
    String str;

    ListType(int val, String str) {
        this.val = val;
        this.str = str;
    }

    public int getVal() {
        return val;
    }

    public String getStr(){ return str;}

    public static ListType getType(int value) {
        ListType type = null;
        for (ListType v : ListType.values()) {
            if (v.getVal() == value) {
                type = v;
            }
        }
        return type;
    }
}


package com.wanna.webmanagement.util;

public class BaseContext {

    private static final ThreadLocal<Integer>CURRENT_ID = new ThreadLocal<>();

    public static void  setCurrentId(Integer id) {
        CURRENT_ID.set(id);
    }
    public static Integer getCurrentId() {
        return CURRENT_ID.get();
    }
    public static void removeCurrentId() {
        CURRENT_ID.remove();
    }

    private BaseContext() {}
}

package com.ls.mobile.geotool.time;

/**
 * Helper to fix small details in the UI
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
public abstract class Decorator {

    /**
     *
     * @param s
     * @param i
     * @return
     */
    public String truncate(String s, int i){
        if (s.length() > i) {
            s = s.substring(0, i);
        }
        return " " + s + " ";
    }
    /**
     * Adds a leading space to a string
     * @param str
     * @return
     */
    public static String addLeadingSpace(String str){
        return " " + str;
    }

}
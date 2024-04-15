package com.ls.mobile.geotool.common;

/**
 * NOT USED IN LOG4G
 * @MemLeaks Q&A:
 *
 * @author lateralSearch
 *
 * @deprecated
 *
 */
public abstract class DataValidatorUtility {

    /**
     * Helper to validate a value is non zero
     * @return
     */
    public static boolean validateNonZero(double val){
        if (val == 0) return false;
        else return true;
    }

}

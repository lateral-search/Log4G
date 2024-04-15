package com.ls.mobile.geotool.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Transactions::
 *
 * Wrapper of the Database system in use (SQLITE Actually)
 *
 * from sqlite doc:
 * Begins a transaction in EXCLUSIVE mode.
 * Transactions can be nested. When the outer transaction is ended
 * all of the work done in that transaction and all of the nested
 * transactions will be committed or rolled back. The changes will
 * be rolled back if any transaction is ended without being marked
 * as clean (by calling setTransactionSuccessful).
 * Otherwise they will be committed.
 * Here is the standard idiom for transactions:
 * db.beginTransaction();
 * try {
 * ...
 * db.setTransactionSuccessful(); <<<<---IMPORTANT!, whithout it transaction will not be commited.
 * } finally {
 * db.endTransaction();
 * }
 *
 *
 *        TransactionalDBHelper tx = new TransactionalDBHelper(appCompatActivity, false);
 *        tx.beginTransaction();
 *        try{
 *            // WHEREVER
 *
 *          tx.setTransactionSuccessful();
 *       }catch (Exception e1){
 *         Log.e(LOG_TAG, e1.getMessage());
 *       }finally {
 *         tx.endTransaction();
 *         tx.getDatabase().close();
 *       }
 * ----------------------------------------------------------------------
 *
 * ANOTHER EXAMPLE:
 *  SQLiteDatabase db = this.getWritableDatabase();
 *  try {
 * db.beginTransaction();
 * ContentValues contentValues = new ContentValues();
 * contentValues.put(KEY_GID, gid);
 * contentValues.put(KEY_QID, qid);
 * contentValues.put(KEY_QNAME, qname);
 *
 * long result = db.insert(TABLE_QUESTIONS, null, contentValues);
 *
 * if (result == -1) {
 * wasSuccess = false;
 * } else {
 * db.setTransactionSuccessful();
 *  }
 * } finally {
 * db.endTransaction();
 * db.close();
 *  }
 * return wasSuccess;
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 *
 */
public class TransactionalDBHelper{

    // pag 155 book Android database programming
    //https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase
    // See Also
    //COMMIT TRANSACTION, ROLLBACK TRANSACTION, SAVEPOINT, RELEASE SAVEPOINT
    private GravityMobileDBHelper gravityMobileDBHelper;

    private static final String LOG_TAG = TransactionalDBHelper.class.getSimpleName();


    public TransactionalDBHelper(Context context,boolean isSynchro) {
        Log.i(LOG_TAG, "TransactionalDBHelper::Constructor");
        gravityMobileDBHelper = GravityMobileDBHelper.getInstance(context, isSynchro);
    }


    /**
     *
     * @param autocommit
     */
    public void setAutocommit(boolean autocommit){
        //  sqLiteDatabase.
    }

    /**
     * Begins a transaction in EXCLUSIVE mode.
     */
    public void beginTransaction() {
        Log.i(LOG_TAG, "TransactionalDBHelper::beginTransaction()");
        gravityMobileDBHelper.getWritableDatabase().beginTransaction();
    }

    /**
     * The changes will be rolled back if any transaction is ended
     * without being marked as clean (by calling setTransactionSuccessful).
     */
    public void setTransactionSuccessful() {
        Log.i(LOG_TAG, "TransactionalDBHelper::setTransactionSuccessful()");
        gravityMobileDBHelper.getWritableDatabase().setTransactionSuccessful();
    }

    /**
     * Commit the transaction.
     */
    public void endTransaction() {
        Log.i(LOG_TAG, "TransactionalDBHelper::endTransaction()");
        gravityMobileDBHelper.getWritableDatabase().endTransaction();
    }

    public SQLiteDatabase getDatabase() {
        return gravityMobileDBHelper.getWritableDatabase();
    }

    public GravityMobileDBHelper getGravityMobileDBHelper() {
        return gravityMobileDBHelper;
    }

}
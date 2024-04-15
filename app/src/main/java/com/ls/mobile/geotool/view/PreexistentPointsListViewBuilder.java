package com.ls.mobile.geotool.view;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ls.mobile.geotool.db.GravityMobileDBInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class contains the logic to generate and handle a list of
 * determined preexistentPoints selected from the database.
 * Such list will be returned in the right format to be shown in
 * the UI view.
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
public class PreexistentPointsListViewBuilder implements GravityMobileDBInterface {

    // Android view stuff
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    //private AppCompatActivity appCompatActivity;
    private Context appCompatActivity; // ISSUE MEMORY-LEAKS


    // List with String tokens rows, representation of the list of POINTS
    // This is the object we pass to the adapter to fill the ListView
    private ArrayList preexistentPointsStringArray;

    // List with HashMap rows inside, which is a representation of the list of POINTS
    // It is a copy of the same data in the ArrayList named preexistentPointsStringArray
    // for ease to handle purposes
    private List<HashMap> preexPointsMapArray = new ArrayList<HashMap>();
    private int elementIndex;

    // Log
    private final static String LOG_TAG = PreexistentPointsListViewBuilder.class.getSimpleName();

    /**
     * Constructor
     */
    public PreexistentPointsListViewBuilder(ListView lv,
                                            ArrayAdapter<String> adapter,
                                            Context activity){
                                            //AppCompatActivity activity){ // ISSUE MEMORY-LEAKS
        listView = lv;
        arrayAdapter = adapter;
        appCompatActivity = activity;
        preexistentPointsStringArray = new ArrayList();
        preexPointsMapArray = new ArrayList<HashMap>();
        elementIndex = 0;
    }

    /**
     * Adds a row to the array list of rows
     */
    public boolean addRow(HashMap<String,String> row){
        //Log.i(LOG_TAG,"addRow" + row);

        // Add the row to the list of hashmaps
        preexPointsMapArray.add(elementIndex, row);
        elementIndex++;

        // Add the row to the object to show in the view
        // In the view data is showed as a String[] containing one row in
        // each element
        final String separator = "   ";
        String rowData = elementIndex + separator +
                         row.get(POINT_CODE) + separator +
                         row.get(POINT_LATITUDE) + separator +
                         row.get(POINT_LONGITUDE) + separator +
                         row.get(POINT_HEIGHT);

        // returns true if the data was added to the ArrayList
        return preexistentPointsStringArray.add(rowData);
    }

    /**
     * Part of Android framework
     *
     * @param values a string vector whose each element is a row to show
     *               in the Android ListView UI element
     */
    private void setAdapter(String[] values) {
        Log.i(LOG_TAG,"listView.setAdapter");

        arrayAdapter = new ArrayAdapter<String>(appCompatActivity,
                android.R.layout.simple_list_item_1, values);
        listView.setAdapter(arrayAdapter);

        Log.i(LOG_TAG,"Adapter set");
    }

    /**
     * Build list of rows to be used on the ListView UI object
     */
    public void buildRowList(){
        Log.i(LOG_TAG,"buildRowList");

        // ITERATE STRING TOKEN ROWS
        // Each element in preexistentPointsStringArray is a String vector
        String[] result = new String[preexistentPointsStringArray.size()];

        //Log.i(LOG_TAG,"preexistentPointsStringArray.size()" + preexistentPointsStringArray.size());

        Iterator<String> preexistentPointsArrayIter = preexistentPointsStringArray.iterator();
        int i = 0;
        while(preexistentPointsArrayIter.hasNext()){
           String aux = preexistentPointsArrayIter.next();
            //Log.i(LOG_TAG,"aux::" + aux);
            //Log.i(LOG_TAG,"i::" + i);
           result[i] = aux;
           i++;
        }

        setAdapter(result);
    }

    /**
     * Returns a Row contained in a HashMap
     *
     * @param index the id of the row to retrieve
     * @return
     */
    public HashMap getRowByElementIndex(int index){
         return preexPointsMapArray.get(index);
    }

    /**
     * Checks if Adapter of the ListView has any data or if it is Empty
     *
     * @return true if the Adapter of the ListView is empty
     */
    public boolean isEmpty(){
        return listView.getAdapter().isEmpty();
    }

    /**
     * Removes all of the elements from this list.
     * The list will be empty after this call returns.
     */
    public void clear(){
        preexistentPointsStringArray.clear();
        elementIndex = 0;
    }

}
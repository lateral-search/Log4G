package com.ls.mobile.geotool;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ls.mobile.geotool.common.MessageDisplayerUtility;
import com.ls.mobile.geotool.db.GravityMobileDBHelper;
import com.ls.mobile.geotool.workflow.LineStatusInterface;
import com.ls.mobile.geotool.db.data.model.Line;
import com.ls.mobile.geotool.time.Decorator;

import java.util.Iterator;
import java.util.List;

/**
 *
 * // Fixed Columns guide:
 * // http://sdroid.blogspot.com/2011/01/fixed-header-in-tablelayout.html
 * // How to make the table scrollable:
 * //https://stackoverflow.com/questions/24605345/how-can-i-make-table-layout-rows-scrollable-in-android
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
public class LineSelectionActivity extends AppCompatActivity
        implements View.OnClickListener{

    // Buttons
    Button create;
    Button cancel;
    //int i = 5;
    TableLayout header;
    TableLayout mainTable;
    List<Line> lineLst;
    HorizontalScrollView horizontalScrollViewTable;
    HorizontalScrollView horizontalScrollViewHeader;
    // Session values from Setup Activity
    int sessionUserId;
    int sessionGravimeterId;
    String sessionGravimeterName;

    // TABLE Configuration
    // Well for me I didn't want to go through the trouble of worrying
    // about alignment of columns so I created this first of all:
    public static Boolean SET_TABLE_HEADER = true;

    final int ID_TEXTVIEW_WIDTH =260;       // ISSUE-018
    final int DATE_TEXTVIEW_WIDTH =630;
    final int TEXTVIEW_WIDTH =330;
    final int STATUS_TEXTVIEW_WIDTH =560;   // ISSUE-018

    private static final int VIEW_BTN_ID_INCREMENT = 100000;
    // Log
    private static final String LOG_TAG = LineSelectionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_selection);

        create = (Button) findViewById(R.id.create);
        cancel = (Button) findViewById(R.id.cancel);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 createNewLineActivity();
                 // Refresh data on the UI
                 refreshLineTable();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                goToSetupActivity();
            }
        });
        header = (TableLayout)findViewById(R.id.header);
        mainTable = (TableLayout)findViewById(R.id.mainTable);

        horizontalScrollViewHeader = findViewById(R.id.tableScrollHorizontal);
        horizontalScrollViewTable = findViewById(R.id.tableScrollHorizontalNested);
        // Symchronize TABLE and HEADER horizontal scroll events
        horizontalScrollViewTable.getViewTreeObserver().addOnScrollChangedListener(
                new ViewTreeObserver.OnScrollChangedListener() {
                     @Override
                     public void onScrollChanged() {
                 horizontalScrollViewTable.scrollTo( horizontalScrollViewHeader.getScrollX()
                                                    ,horizontalScrollViewHeader.getScrollY());
                     }
                }
        );

        // Values from Session.
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        sessionUserId = pref.getInt(SharedDataKeyInterface.SETUP_ACTIVITY_USER_ID_INT, 0);
        sessionGravimeterId =  pref.getInt(SharedDataKeyInterface.SETUP_ACTIVITY_GRAVIMETER_ID_INT, 0);
        sessionGravimeterName = pref.getString(SharedDataKeyInterface.SETUP_ACTIVITY_GRAVIMETER_NAME_STR, null);

        // Reload data on table
        refreshLineTable();
    }

    // Refresh the List table contents to show on the UI
    private void refreshLineTable(){
        lineLst = loadLineListForTablePopulation();
        // Then inside my loop:
        if (SET_TABLE_HEADER) {
            setTableHeader(header);
        }
        mainTable.removeAllViews();
        populateTable(header,mainTable);
    }

    // Load the Line List data from SQLite database
    private List<Line> loadLineListForTablePopulation() {
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        List<Line> l = db.getAllLinesByGravimeterId(sessionGravimeterId);
        db.finalizeInstance();
        return l;
    }

    // guide:https://stackoverflow.com/questions/5665747/android-scrollable-tablelayout-with-a-dynamic-fixed-header?rq=1
    @SuppressLint("ResourceType")
    public void populateTable(TableLayout hdr, TableLayout mnTbl){

        int NBR_OF_COLS = 5;
        String headcol1="";
        TableRow tblRow[]= new TableRow[40];
        TextView txtVw[] = new TextView[1000];
        Line lAux;
        for(int i=0; i < lineLst.size(); i++)
        {   // ISSUE MEMORY-LEAKS
            tblRow[i]=new TableRow(getApplicationContext());
            lAux = lineLst.get(i);
            // COLS j=5
            // 0 :: ID  // ISSUE MEMORY-LEAKS
            txtVw[0] = new TextView(getApplicationContext());
            txtVw[0].setId(0);
            txtVw[0].setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            txtVw[0].setText(Decorator.addLeadingSpace(String.valueOf(lAux.getmId()))); //lAux.getName()
            txtVw[0].setEllipsize(TextUtils.TruncateAt.END); // adds ... at the end
            txtVw[0].setMaxLines(1); // single line TextVIew
            txtVw[0].setWidth(ID_TEXTVIEW_WIDTH);// fixed width
            //Make rows visible
            txtVw[0].setBackgroundResource(R.drawable.textview_border);
            txtVw[0].setTextColor(Color.WHITE);
            changeHead(headcol1,txtVw,0);
            tblRow[i].addView(txtVw[0]);
            // 1 :: DATE // ISSUE MEMORY-LEAKS
            txtVw[1] = new TextView(getApplicationContext());
            txtVw[1].setId(1);
            txtVw[1].setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            txtVw[1].setText(Decorator.addLeadingSpace(lAux.getDate()));
            txtVw[1].setEllipsize(TextUtils.TruncateAt.END); // adds ... at the end
            txtVw[1].setMaxLines(1); // single line TextVIew
            txtVw[1].setWidth(DATE_TEXTVIEW_WIDTH);// fixed width
            txtVw[1].setBackgroundResource(R.drawable.textview_border);
            txtVw[1].setTextColor(Color.WHITE);
            changeHead(headcol1,txtVw,1);
            tblRow[i].addView(txtVw[1]);
            // 2 :: STATUS // ISSUE MEMORY-LEAKS
            txtVw[2] = new TextView(getApplicationContext());
            txtVw[2].setId(2);
            txtVw[2].setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            txtVw[2].setText(Decorator.addLeadingSpace(lAux.getStatus()));
            txtVw[2].setEllipsize(TextUtils.TruncateAt.END); // adds ... at the end
            txtVw[2].setMaxLines(1); // single line TextVIew
            txtVw[2].setWidth(STATUS_TEXTVIEW_WIDTH);// fixed width
            txtVw[2].setBackgroundResource(R.drawable.textview_border);
            txtVw[2].setTextColor(Color.WHITE);
            changeHead(headcol1,txtVw,2);
            tblRow[i].addView(txtVw[2]);
            // 3 // ISSUE MEMORY-LEAKS
            Button button = new Button(getApplicationContext());
            button.setText("DETALLE");
            button.setWidth(TEXTVIEW_WIDTH);
            button.setBackgroundColor(Color.BLACK);
            button.setTextColor(Color.WHITE);
            // R.id won't be generated for us, so we need to create one
            button.setId(lAux.getmId());
            // add our event handler (less memory than an anonymous inner class)
            button.setOnClickListener(this);
            // add generated button to view
            tblRow[i].addView(button);
            // 4 // ISSUE MEMORY-LEAKS
            Button btn = new Button(getApplicationContext());
            btn.setText("VER");
            btn.setWidth(TEXTVIEW_WIDTH);
            btn.setBackgroundColor(R.color.ls_deep_bluee_dark);
            btn.setTextColor(Color.WHITE);
            btn.setId(lAux.getmId() + VIEW_BTN_ID_INCREMENT);
            btn.setOnClickListener(this);
            tblRow[i].addView(btn);

            mnTbl.addView(tblRow[i]);
        }

        // DUMMY INVISIBLE HEADER
        TableRow trhead= new TableRow(this);
        TextView tvhead[] = new TextView[5];

        for(int i=0;i<=4;i++)
        {   // ISSUE MEMORY-LEAKS
            tvhead[i] = new TextView(getApplicationContext());
            tvhead[i].setTextColor(Color.WHITE);
            tvhead[i].setHeight(0);
            tvhead[i].setText(headcol1);
            trhead.addView(tvhead[i]);

        }
        hdr.addView(trhead);

    }

    /**
     * Inherited implementation to perfomr actions of dynamics buttons programatically
     * added to the list.
     * @param v
     */
    @Override
    public void onClick(View v) {
        // show a message with the button's ID
        // ISSUE MEMORY-LEAKS: getApplicationContext() was LineSelectionActivity.java
        Toast toast = Toast.makeText(getApplicationContext(), "You clicked button " + v.getId(), Toast.LENGTH_LONG);
        toast.show();
        int id = v.getId();

        if(id > VIEW_BTN_ID_INCREMENT){ // VIEW button
            id = id - VIEW_BTN_ID_INCREMENT;
            //Line l = lineLst.get(id-1); //Id in a List starts from zero

            goToGraphActivity(getLineSelectedHlpr(id).getmId());

        }else{ // DETAIL button

            goToLineDataActivity(getLineSelectedHlpr(id).getmId());
        }
    }

    private Line getLineSelectedHlpr(int id){
        Line lineSelected = new Line();

        Iterator<Line> lineLstIter = lineLst.iterator();
        while(lineLstIter.hasNext()){
            Line lineAux = lineLstIter.next();
            if(lineAux.getmId() == id){
                lineSelected = lineAux;
                break;
            }
        }
        return lineSelected;
    }

    public void changeHead(String headcol1,TextView txtVw[],int j ){
        //headerPtsMed size
        if(headcol1.length() < txtVw[j].getText().length())
        {
            headcol1=null;
            headcol1=txtVw[j].getText().toString();
        }
    }

    // guide:
    // https:stackoverflow.com/questions/5665747/android-scrollable-tablelayout-with-a-dynamic-fixed-header?rq=1
    public void setTableHeader(TableLayout tbl) {
        TableRow tr = new TableRow(getBaseContext());

        TextView tvIdLinea = new TextView(getBaseContext());
        tvIdLinea.setText("ID LINEA");
        tvIdLinea.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
        tvIdLinea.setEllipsize(TextUtils.TruncateAt.END); // adds ... at the end
        tvIdLinea.setMaxLines(1); // single line TextVIew
        tvIdLinea.setWidth(ID_TEXTVIEW_WIDTH);// fixed width
        tvIdLinea.setTypeface(null, Typeface.BOLD);
        tvIdLinea.setGravity(Gravity.CENTER_HORIZONTAL);
        tvIdLinea.setTextColor(Color.WHITE);
        tr.addView(tvIdLinea);

        TextView tvDateLastTake = new TextView(getBaseContext());
        tvDateLastTake.setText("FECHA DE CREACION");
        tvDateLastTake.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
        tvDateLastTake.setEllipsize(TextUtils.TruncateAt.END);
        tvDateLastTake.setMaxLines(1);
        tvDateLastTake.setWidth(DATE_TEXTVIEW_WIDTH);
        tvDateLastTake.setTypeface(null, Typeface.BOLD);
        tvDateLastTake.setGravity(Gravity.CENTER_HORIZONTAL);
        tvDateLastTake.setTextColor(Color.WHITE);
        tr.addView(tvDateLastTake);

        TextView tvLineStatus = new TextView(getBaseContext());
        tvLineStatus.setText("ESTADO LINEA");
        tvLineStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
        tvLineStatus.setEllipsize(TextUtils.TruncateAt.END);
        tvLineStatus.setMaxLines(1);
        tvLineStatus.setWidth(STATUS_TEXTVIEW_WIDTH);
        tvLineStatus.setTypeface(null, Typeface.BOLD);
        tvLineStatus.setGravity(Gravity.CENTER_HORIZONTAL);
        tvLineStatus.setTextColor(Color.WHITE);
        tr.addView(tvLineStatus);

        TextView tvActions = new TextView(getBaseContext());
        tvActions.setText("ACCIONES");
        tvActions.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
        tvActions.setEllipsize(TextUtils.TruncateAt.END);
        tvActions.setMaxLines(1);
        tvActions.setWidth(TEXTVIEW_WIDTH);
        tvActions.setTypeface(null, Typeface.BOLD);
        tvActions.setGravity(Gravity.CENTER_HORIZONTAL);
        tvActions.setTextColor(Color.WHITE);
        tr.addView(tvActions);

        TextView tvGraphic = new TextView(getBaseContext());
        tvGraphic.setText("GRAFICO");
        tvGraphic.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
        tvGraphic.setEllipsize(TextUtils.TruncateAt.END);
        tvGraphic.setMaxLines(1);
        tvGraphic.setWidth(TEXTVIEW_WIDTH);
        tvGraphic.setTypeface(null, Typeface.BOLD);
        tvGraphic.setGravity(Gravity.CENTER_HORIZONTAL);
        tvGraphic.setTextColor(Color.WHITE);
        tr.addView(tvGraphic);

        // Adding row to Table
        tbl.addView(tr);

        // chceTable Header Has Been Se
        //SET_TABLE_HEADER = false;
    }
    // GO TO LINE DATA SCREEN
    private void goToLineDataActivity(int lineId){
        Log.d(LOG_TAG, "Click!, Going to detailActivity!");
        // ISSUE MEMORY-LEAKS
        Intent intent = new Intent(getApplicationContext(),LineDataActivity.class);
        intent.putExtra(IntentDataKeyInterface.LINE_SELECTION_ACTIVITY_LINE_ID_INT, lineId);
        startActivityForResult(intent, 0);
    }
    // GRAPH:: Go to graphic by steps screen
    private void goToGraphActivity(int lineId){
        Log.d(LOG_TAG, "Click!, Going to Graphic!");

        // Check if the line is in CLOSED or INCONSISTENCE status
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        Line l = db.getLineByLineId(lineId);
        if(l.getStatus().equals(LineStatusInterface.LINE_STATUS_CLOSED) ||
                l.getStatus().equals(LineStatusInterface.LINE_STATUS_INCONSISTENCE)){
            // Go
            // ISSUE MEMORY-LEAKS
            Intent intent = new Intent(getApplicationContext(),LineBarGraphActivity.class);
            intent.putExtra(IntentDataKeyInterface.LINE_SELECTION_ACTIVITY_LINE_ID_INT, lineId);
            startActivityForResult(intent, 0);
        }else{
            // LINE CLOSING was not be exec. alert
            MessageDisplayerUtility.displaySimpleAlert(
                    this,String.valueOf(this.getText(R.string.line_sel_act_close_must_be_exec)));
        }


    }
    //CANCEL : cancel the operation over the line LINE
    private void goToSetupActivity(){
        Log.d(LOG_TAG, "Click!, Going to setupActivity!");
        // ISSUE MEMORY-LEAKS
        Intent intent = new Intent(getApplicationContext(),SetupActivity.class);
        startActivity(intent);
    }
    //CREATE NEW LINE
    private void createNewLineActivity(){
        Log.d(LOG_TAG, "Click!, CREATE A NEW LINE!");

        AlertDialog.Builder	alertBuilder = new AlertDialog.Builder(LineSelectionActivity.this);
        alertBuilder.setTitle(R.string.app_name);//R.string.alert_title);
        alertBuilder.setMessage("Desea crear una nueva LINEA?");//R.string.alert_message);
        alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,	int	which) {

                if( sessionUserId > 0
                        &&  !"".equals(sessionGravimeterName)
                        && sessionGravimeterName != null) {

                    GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
                    long res;
                    try{
                         res = db.createLine(sessionUserId, sessionGravimeterId);
                     }finally{
                         db.close();
                     }
                     if(res == -1) {// ISSUE MEMORY-LEAKS see others
                         Toast toast = Toast.makeText(getApplicationContext(), "No fue posible crear la LINEA", Toast.LENGTH_LONG);
                         toast.show();
                     }else{
                         Toast toast = Toast.makeText(getApplicationContext(), "Se creo la LINEA con ID:" + String.valueOf(res), Toast.LENGTH_LONG);
                         toast.show();
                         refreshLineTable();
                     }

                }else{// ISSUE MEMORY-LEAKS
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ERROR al intentar crear la LINEA"
                            , Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
        // ISSUE MEMORY-LEAKS
        alertBuilder.setNegativeButton("Cancel", new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }


    private static class MyOnClickListener implements DialogInterface.OnClickListener {
        public	void	onClick(DialogInterface	dialog,	int	which)	{
               // DO NOTHING
        }
    }
}

package com.ls.mobile.geotool;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.ls.mobile.geotool.db.DBTools;
import com.ls.mobile.geotool.db.GravityMobileDBHelper;
import com.ls.mobile.geotool.db.GravityMobileDBInterface;
import com.ls.mobile.geotool.db.TransactionalDBHelper;
import com.ls.mobile.geotool.db.data.model.Line;
import com.ls.mobile.geotool.db.data.model.Point;
import com.ls.mobile.geotool.gravity.CssGravityLine;
import com.ls.mobile.geotool.time.Decorator;
import com.ls.mobile.geotool.workflow.LineStatusInterface;
import com.ls.mobile.geotool.workflow.PointStatusInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class contains the Activity corresponding to the data handling
 * of the screen which shows the list of lines created in Log4G.
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
public class LineDataActivity extends AppCompatActivity
        implements View.OnClickListener {

    // Top Buttons similar to tabs.
    Button btnForward;
    Button btnReturn;

    // Bottom Buttons
    Button btnCancel;
    Button btnCloseLine;
    Button btnStartReturn;
    Button btnCreateNewPoint;

    // UI Grid
    TableLayout headerPtsMed;
    TableLayout mainTablePtsMed;
    HorizontalScrollView horizontalScrollViewTable;
    HorizontalScrollView horizontalScrollViewHeader;
    // UI Grid CONFIGURATION
    final int TEXTVIEW_WIDTH = 330;       // Width of each cell of the grid
    final int DATE_TEXTVIEW_WIDTH = 780;  // Width of date cell
    final int OFFSET_TEXTVIEW_WIDTH = 430;// ISSUE-013
    final int STATUS_TEXTVIEW_WIDTH = 630;// ISSUE-013

    final int NBR_OF_COLS = 10;           // Nbr of grid cols
    // UI table header:
    public static Boolean SET_TABLE_HEADER = true;

    // All points (forward and Return) corresponding to the lineId
    List<Point> allPointLst = new ArrayList<>();

    // Points Visualized on screen
    List<Point> visualizedPointLst = new ArrayList<>();

    // Values comming from other activities
    // LineDataActivity shows data POINTS corresponding to a lineId
    // intentLineId stores such value
    int intentLineId;
    boolean lineIsEditable;

    // Log
    private static final String LOG_TAG = LineDataActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_data);

        // Top Buttons similar to tabs.
        btnForward = (Button) findViewById(R.id.btnGo);
        btnReturn = (Button) findViewById(R.id.btnReturn);

        btnReturn.setBackgroundColor(getResources().getColor(R.color.ls_deep_bluee_dark));
        btnForward.setBackgroundColor(getResources().getColor(R.color.windowBackground));

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnReturn.setBackgroundColor(getResources().getColor(R.color.ls_deep_bluee_dark));
                btnForward.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                loadForwardPointsOnTable();
            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnForward.setBackgroundColor(getResources().getColor(R.color.ls_deep_bluee_dark));
                btnReturn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                loadReturnPointsOnTable();
            }
        });

        // Header and Main Table.
        headerPtsMed = (TableLayout) findViewById(R.id.headerPtsMed);
        mainTablePtsMed = (TableLayout) findViewById(R.id.mainTablePtsMed);

        // Bottom Buttons
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCloseLine = (Button) findViewById(R.id.btnCloseLine);
        btnStartReturn = (Button) findViewById(R.id.btnStartReturn);
        btnCreateNewPoint = (Button) findViewById(R.id.btnCreateNewPoint);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCancel();
            }
        });
        btnCloseLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCloseLine();

            }
        });
        btnStartReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToStartReturn();
            }
        });
        btnCreateNewPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // VALIDATE AND GO TO CREATE NEW POINT ACTIVITY

                // Validate Qty of FWD >= RET
                List<Point> pointsFwd = loadForwardPoints(
                        intentLineId,
                        GravityMobileDBInterface.SQL_ASC);

                List<Point> pointsRet = loadReturnPoints(
                        intentLineId,
                        GravityMobileDBInterface.SQL_ASC);

                if ((pointsFwd != null && pointsRet == null)
                        || pointsFwd == null) {

                    // GO TO CREATE NEW POINT ACTIVITY
                    goToCreateNewPoint();

                } else if (pointsRet != null) {

                    if (pointsFwd.size() == pointsRet.size()) {
                        MessageDisplayerUtility.displaySimpleAlert(LineDataActivity.this,
                                getString(R.string.line_data_act_return_full));
                    } else {
                        MessageDisplayerUtility.displaySimpleAlert(LineDataActivity.this,
                                getString(R.string.line_data_act_unexpected));
                    }
                }
            }
        });

        // SYNCHRONIZE TABLE AND HEADER SCROLLBARS
        horizontalScrollViewHeader = findViewById(R.id.tblScrollHorizontal);
        horizontalScrollViewTable = findViewById(R.id.tblScrollHorizontalNested);
        // Symchronize TABLE and HEADER horizontal scroll events
        horizontalScrollViewTable.getViewTreeObserver().addOnScrollChangedListener(
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        horizontalScrollViewTable.scrollTo(horizontalScrollViewHeader.getScrollX()
                                , horizontalScrollViewHeader.getScrollY());
                    }
                }
        );

        // GET INTENTS
        // Get line data
        // Values from LineSelectionActivity.
        intentLineId = getIntent().getIntExtra(
                IntentDataKeyInterface.LINE_SELECTION_ACTIVITY_LINE_ID_INT, 0);

        // Line Status
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        Line l = db.getLineByLineId(intentLineId);
        setLineIsEditable(true);
        if (LineStatusInterface.LINE_STATUS_CLOSED.equals(l.getStatus())) {
            setLineIsEditable(false);
        }

        // Refresh UI data content
        refreshPointTable(intentLineId
                , PointStatusInterface.POINT_ONEWAYVALUE_FORWARD
                , GravityMobileDBInterface.SQL_ASC);

        // ISSUE-004
        // In case user is returning from PointCRUDActivity
        // from a crud operation over a RETURN POINT
        int oneWayValue = getIntent().getIntExtra(
                IntentDataKeyInterface.POINT_CRUD_ACT_ONEWAY_VAL_INT,PointStatusInterface.POINT_ONEWAYVALUE_FORWARD);
        if(oneWayValue == PointStatusInterface.POINT_ONEWAYVALUE_RETURN){
            btnReturn.callOnClick();
        }
        db.finalizeInstance();// 23-03-08-ISSUE MEMORY-LEAKS
    }

    /**
     * Refresh the List table contents to show on the UI
     *
     * @param lineId
     * @param oneWayValue
     * @param orderCriteria
     */
    private void refreshPointTable(int lineId
            , int oneWayValue
            , String orderCriteria) {
        // Get Points corresponding the LineId
        allPointLst = loadAllPointListByLine(lineId, orderCriteria);
        // List to show in the UI
        if (oneWayValue == PointStatusInterface.POINT_ONEWAYVALUE_FORWARD) {
            visualizedPointLst = loadForwardPoints(lineId, orderCriteria);
        } else {
            visualizedPointLst = loadReturnPoints(lineId, orderCriteria);
        }
        // Then inside my loop:
        if (SET_TABLE_HEADER) {
            setTableHeader(headerPtsMed);
        }
        mainTablePtsMed.removeAllViews();
        if (visualizedPointLst != null) populateTable(headerPtsMed, mainTablePtsMed);
    }

    /**
     * Load the Line List data from SQLite database
     *
     * @param lineId
     * @param orderCriteria
     * @return
     */
    private List<Point> loadAllPointListByLine(int lineId, String orderCriteria) {
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        List<Point> p = db.getPointsByLineId(lineId , orderCriteria);
        db.finalizeInstance();// ISSUE MEMORY-LEAKS
        return p;
    }

    /**
     * Load the Line List data from SQLite database
     *
     * @param lineId
     * @param orderCriteria
     * @return
     */
    private List<Point> loadReturnPoints(int lineId, String orderCriteria) {
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        // ISSUE-001 return db.getPointsByLineIdAndOnwWayVal(lineId
        List<Point> p = db.getPointsByLnIdOneWayValOrderByPointId(lineId
                , PointStatusInterface.POINT_ONEWAYVALUE_RETURN
                , orderCriteria);
        db.finalizeInstance();// ISSUE MEMORY-LEAKS
        return p;
    }

    /**
     * Load the forward points Line List data from SQLite database
     *
     * @param lineId
     * @param orderCriteria
     * @return
     */
    private List<Point> loadForwardPoints(int lineId, String orderCriteria) {
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        // ISSUE-001 return db.getPointsByLineIdAndOnwWayVal(lineId
        List<Point> p = db.getPointsByLnIdOneWayValOrderByPointId(lineId
                , PointStatusInterface.POINT_ONEWAYVALUE_FORWARD
                , orderCriteria);
        db.finalizeInstance();// ISSUE MEMORY-LEAKS
        return p;
    }

    /**
     * Inherited implementation to perfomr actions of dynamics buttons
     * programatically added to the list.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        Log.d(LOG_TAG, "ENTER:onClick!, Edit a Point!");
        Toast toast = Toast.makeText(getApplicationContext()
                , "ID SELETED:" + v.getId(), Toast.LENGTH_LONG);// ISSUE MEMORY-LEAKS
        toast.show();

        if (lineIsEditable) {
            //Point p = allPointLst.get((v.getId()) - 1);// -1 cause view elwments start from 1 but list from 0
            GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
            Point p = db.getPointById(v.getId());
            db.finalizeInstance();// 2-ISSUE MEMORY-LEAKS
            goToEditPoint(p);

        } else {
            toast = Toast.makeText(getApplicationContext()
                    , getString(R.string.line_data_act_already_closed)
                    , Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /**
     * POPULATE UI TABLE
     * <p>
     * https://stackoverflow.com/questions/5665747/android-scrollable-tablelayout-with-a-dynamic-fixed-header?rq=1
     *
     * @param hdr
     * @param mnTbl
     */
    @SuppressLint("ResourceType")
    public void populateTable(TableLayout hdr, TableLayout mnTbl) {
        String headcol1 = "";
        TableRow tblRow[] = new TableRow[40];
        TextView txtVw[] = new TextView[1000];
        Point pAux;
        /**
         * TAKE EXTREME CARE MEASSURES WITH THE CUT/PASTE!!!!
         * Configure the NBR_OF_COLS var above if you add some
         */
        for (int i = 0; i < visualizedPointLst.size(); i++) {
            // ISSUE MEMORY-LEAKS
            tblRow[i] = new TableRow(getApplicationContext());
            pAux = visualizedPointLst.get(i);

            // CODE // ISSUE MEMORY-LEAKS
            txtVw[0] = new TextView(getApplicationContext());
            txtVw[0].setId(0);
            txtVw[0].setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            txtVw[0].setText(Decorator.addLeadingSpace(pAux.getCode()));
            txtVw[0].setEllipsize(TextUtils.TruncateAt.END); // adds ... at the end
            txtVw[0].setMaxLines(1); // single line TextVIew
            txtVw[0].setWidth(TEXTVIEW_WIDTH);// fixed width
            txtVw[0].setBackgroundResource(R.drawable.textview_border); //Make rows visible
            txtVw[0].setTextColor(Color.WHITE);
            changeHead(headcol1, txtVw, 0);
            tblRow[i].addView(txtVw[0]);
            // DATE // ISSUE MEMORY-LEAKS
            txtVw[1] = new TextView(getApplicationContext());
            txtVw[1].setId(1);
            txtVw[1].setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            txtVw[1].setText(Decorator.addLeadingSpace(pAux.getDate()));
            txtVw[1].setEllipsize(TextUtils.TruncateAt.END);
            txtVw[1].setMaxLines(1);
            txtVw[1].setWidth(DATE_TEXTVIEW_WIDTH);
            txtVw[1].setBackgroundResource(R.drawable.textview_border);
            txtVw[1].setTextColor(Color.WHITE);
            changeHead(headcol1, txtVw, 1);
            tblRow[i].addView(txtVw[1]);
            // G1 // ISSUE MEMORY-LEAKS
            txtVw[2] = new TextView(getApplicationContext());
            txtVw[2].setId(2);
            txtVw[2].setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            txtVw[2].setText(Decorator.addLeadingSpace(String.valueOf(pAux.getG1())));
            txtVw[2].setEllipsize(TextUtils.TruncateAt.END);
            txtVw[2].setMaxLines(1);
            txtVw[2].setWidth(TEXTVIEW_WIDTH);
            txtVw[2].setBackgroundResource(R.drawable.textview_border);
            txtVw[2].setTextColor(Color.WHITE);
            changeHead(headcol1, txtVw, 2);
            tblRow[i].addView(txtVw[2]);
            // G2 // ISSUE MEMORY-LEAKS
            txtVw[3] = new TextView(getApplicationContext());
            txtVw[3].setId(3);
            txtVw[3].setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            txtVw[3].setText(Decorator.addLeadingSpace(String.valueOf(pAux.getG2())));
            txtVw[3].setEllipsize(TextUtils.TruncateAt.END);
            txtVw[3].setMaxLines(1);
            txtVw[3].setWidth(TEXTVIEW_WIDTH);
            txtVw[3].setBackgroundResource(R.drawable.textview_border);
            txtVw[3].setTextColor(Color.WHITE);
            changeHead(headcol1, txtVw, 3);
            tblRow[i].addView(txtVw[3]);
            // G3 // ISSUE MEMORY-LEAKS
            txtVw[4] = new TextView(getApplicationContext());
            txtVw[4].setId(4);
            txtVw[4].setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            txtVw[4].setText(Decorator.addLeadingSpace(String.valueOf(pAux.getG3())));
            txtVw[4].setEllipsize(TextUtils.TruncateAt.END);
            txtVw[4].setMaxLines(1);
            txtVw[4].setWidth(TEXTVIEW_WIDTH);
            txtVw[4].setBackgroundResource(R.drawable.textview_border);
            txtVw[4].setTextColor(Color.WHITE);
            changeHead(headcol1, txtVw, 4);
            tblRow[i].addView(txtVw[4]);
            // READING // ISSUE MEMORY-LEAKS
            txtVw[5] = new TextView(getApplicationContext());
            txtVw[5].setId(5);
            txtVw[5].setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            txtVw[5].setText(Decorator.addLeadingSpace(String.valueOf(pAux.getReading())));
            txtVw[5].setEllipsize(TextUtils.TruncateAt.END);
            txtVw[5].setMaxLines(1);
            txtVw[5].setWidth(TEXTVIEW_WIDTH);
            txtVw[5].setBackgroundResource(R.drawable.textview_border);
            txtVw[5].setTextColor(Color.WHITE);
            changeHead(headcol1, txtVw, 5);
            tblRow[i].addView(txtVw[5]);
            // REDUCED-G // ISSUE MEMORY-LEAKS
            txtVw[6] = new TextView(getApplicationContext());
            txtVw[6].setId(6);
            txtVw[6].setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            txtVw[6].setText(Decorator.addLeadingSpace(String.valueOf(pAux.getReducedG())));
            txtVw[6].setEllipsize(TextUtils.TruncateAt.END);
            txtVw[6].setMaxLines(1);
            txtVw[6].setWidth(TEXTVIEW_WIDTH);
            txtVw[6].setBackgroundResource(R.drawable.textview_border);
            txtVw[6].setTextColor(Color.WHITE);
            changeHead(headcol1, txtVw, 6);
            tblRow[i].addView(txtVw[6]);
            // OFFSET // ISSUE MEMORY-LEAKS
            txtVw[7] = new TextView(getApplicationContext());
            txtVw[7].setId(7);
            txtVw[7].setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            txtVw[7].setText(Decorator.addLeadingSpace(String.valueOf(pAux.getOffset())));
            txtVw[7].setEllipsize(TextUtils.TruncateAt.END);
            txtVw[7].setMaxLines(1);
            txtVw[7].setWidth(OFFSET_TEXTVIEW_WIDTH);// ISSUE-013
            txtVw[7].setBackgroundResource(R.drawable.textview_border);
            txtVw[7].setTextColor(Color.WHITE);
            changeHead(headcol1, txtVw, 7);
            tblRow[i].addView(txtVw[7]);
            // STATUS // ISSUE MEMORY-LEAKS
            txtVw[8] = new TextView(getApplicationContext());
            txtVw[8].setId(8);
            txtVw[8].setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            txtVw[8].setText(Decorator.addLeadingSpace(String.valueOf(pAux.getStatus())));
            txtVw[8].setEllipsize(TextUtils.TruncateAt.END);
            txtVw[8].setMaxLines(1);
            txtVw[8].setWidth(STATUS_TEXTVIEW_WIDTH); // ISSUE-013
            txtVw[8].setBackgroundResource(R.drawable.textview_border);
            txtVw[8].setTextColor(Color.WHITE);
            changeHead(headcol1, txtVw, 8);
            tblRow[i].addView(txtVw[8]);
            // ACTION: EDIT // ISSUE MEMORY-LEAKS
            Button button = new Button(getApplicationContext());
            button.setText("EDITAR");
            button.setWidth(TEXTVIEW_WIDTH);
            button.setBackgroundColor(R.color.ls_deep_bluee_dark);
            button.setTextColor(Color.WHITE);
            // R.id won't be generated for us, so we need to create one
            button.setId(pAux.getmId());
            // add our event handler (less memory than an anonymous inner class)
            button.setOnClickListener(this);
            tblRow[i].addView(button);

            mnTbl.addView(tblRow[i]);
        }

        // DUMMY INVISIBLE HEADER
        TableRow trhead = new TableRow(this);
        TextView tvhead[] = new TextView[NBR_OF_COLS];

        for (int i = 0; i <= 4; i++) {//4 // ISSUE MEMORY-LEAKS
            tvhead[i] = new TextView(getApplicationContext());
            tvhead[i].setTextColor(Color.WHITE);
            //tvhead[i].setBackgroundResource(R.drawable.textview_border);
            tvhead[i].setHeight(0);
            tvhead[i].setText(headcol1);
            trhead.addView(tvhead[i]);
        }

        hdr.addView(trhead);
    }

    // In some cases could be usefull, but is not implemented actually.
    public void changeHead(String headcol1, TextView txtVw[], int j) {
        //
        if (headcol1.length() < txtVw[j].getText().length()) {
            headcol1 = null;
            headcol1 = txtVw[j].getText().toString();
        }
    }

    /**
     * SET UI TABLE HEADER
     * guide: https://stackoverflow.com/questions/5665747/android-scrollable-tablelayout-with-a-dynamic-fixed-header?rq=1
     *
     * @param tbl
     */
    public void setTableHeader(TableLayout tbl) {
        /**
         * TAKE EXTREME CARE MEASSURES if you like CUT/PASTE programming!!!!
         * Configure the NBR_OF_COLS var above if you add some other values
         */
        TableRow tr = new TableRow(getBaseContext());

        TextView tvIdLinea = new TextView(getBaseContext());
        tvIdLinea.setText("COD.");
        tvIdLinea.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        tvIdLinea.setEllipsize(TextUtils.TruncateAt.END); // adds ... at the end
        tvIdLinea.setMaxLines(1); // single line TextVIew
        tvIdLinea.setWidth(TEXTVIEW_WIDTH);// fixed width
        tvIdLinea.setTypeface(null, Typeface.BOLD);
        tvIdLinea.setGravity(Gravity.CENTER_HORIZONTAL);
        tvIdLinea.setTextColor(Color.WHITE);
        tr.addView(tvIdLinea);

        TextView tvDateLastTake = new TextView(getBaseContext());
        tvDateLastTake.setText("FECHA ULTIMA MEDICION");
        tvDateLastTake.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        tvDateLastTake.setEllipsize(TextUtils.TruncateAt.END);
        tvDateLastTake.setMaxLines(1);
        tvDateLastTake.setWidth(DATE_TEXTVIEW_WIDTH);
        tvDateLastTake.setTypeface(null, Typeface.BOLD);
        tvDateLastTake.setGravity(Gravity.CENTER_HORIZONTAL);
        tvDateLastTake.setTextColor(Color.WHITE);
        tr.addView(tvDateLastTake);

        TextView tvLineStatus = new TextView(getBaseContext());
        tvLineStatus.setText("G1");
        tvLineStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        tvLineStatus.setEllipsize(TextUtils.TruncateAt.END);
        tvLineStatus.setMaxLines(1);
        tvLineStatus.setWidth(TEXTVIEW_WIDTH);
        tvLineStatus.setTypeface(null, Typeface.BOLD);
        tvLineStatus.setGravity(Gravity.CENTER_HORIZONTAL);
        tvLineStatus.setTextColor(Color.WHITE);
        tr.addView(tvLineStatus);

        TextView tvActions = new TextView(getBaseContext());
        tvActions.setText("G2");
        tvActions.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        tvActions.setEllipsize(TextUtils.TruncateAt.END);
        tvActions.setMaxLines(1);
        tvActions.setWidth(TEXTVIEW_WIDTH);
        tvActions.setTypeface(null, Typeface.BOLD);
        tvActions.setGravity(Gravity.CENTER_HORIZONTAL);
        tvActions.setTextColor(Color.WHITE);
        tr.addView(tvActions);

        TextView tvGraphic = new TextView(getBaseContext());
        tvGraphic.setText("G3");
        tvGraphic.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        tvGraphic.setEllipsize(TextUtils.TruncateAt.END);
        tvGraphic.setMaxLines(1);
        tvGraphic.setWidth(TEXTVIEW_WIDTH);
        tvGraphic.setTypeface(null, Typeface.BOLD);
        tvGraphic.setGravity(Gravity.CENTER_HORIZONTAL);
        tvGraphic.setTextColor(Color.WHITE);
        tr.addView(tvGraphic);

        TextView tvReading = new TextView(getBaseContext());
        tvReading.setText("LECTURA");
        tvReading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        tvReading.setEllipsize(TextUtils.TruncateAt.END);
        tvReading.setMaxLines(1);
        tvReading.setWidth(TEXTVIEW_WIDTH);
        tvReading.setTypeface(null, Typeface.BOLD);
        tvReading.setGravity(Gravity.CENTER_HORIZONTAL);
        tvReading.setTextColor(Color.WHITE);
        tr.addView(tvReading);

        TextView tvReducedG = new TextView(getBaseContext());
        tvReducedG.setText("G-RED.");
        tvReducedG.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        tvReducedG.setEllipsize(TextUtils.TruncateAt.END);
        tvReducedG.setMaxLines(1);
        tvReducedG.setWidth(TEXTVIEW_WIDTH);
        tvReducedG.setTypeface(null, Typeface.BOLD);
        tvReducedG.setGravity(Gravity.CENTER_HORIZONTAL);
        tvReducedG.setTextColor(Color.WHITE);
        tr.addView(tvReducedG);

        TextView tvOffset = new TextView(getBaseContext());
        tvOffset.setText("OFFSET[m]");
        tvOffset.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        tvOffset.setEllipsize(TextUtils.TruncateAt.END);
        tvOffset.setMaxLines(1);
        tvOffset.setWidth(OFFSET_TEXTVIEW_WIDTH); // ISSUE-013
        tvOffset.setTypeface(null, Typeface.BOLD);
        tvOffset.setGravity(Gravity.CENTER_HORIZONTAL);
        tvOffset.setTextColor(Color.WHITE);
        tr.addView(tvOffset);

        TextView tvStat = new TextView(getBaseContext());
        tvStat.setText("ESTADO");
        tvStat.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        tvStat.setEllipsize(TextUtils.TruncateAt.END);
        tvStat.setMaxLines(1);
        tvStat.setWidth(STATUS_TEXTVIEW_WIDTH); // ISSUE-013
        tvStat.setTypeface(null, Typeface.BOLD);
        tvStat.setGravity(Gravity.CENTER_HORIZONTAL);
        tvStat.setTextColor(Color.WHITE);
        tr.addView(tvStat);

        TextView tvEdit = new TextView(getBaseContext());
        tvEdit.setText("ACCION");
        tvEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        tvEdit.setEllipsize(TextUtils.TruncateAt.END);
        tvEdit.setMaxLines(1);
        tvEdit.setWidth(TEXTVIEW_WIDTH);
        tvEdit.setTypeface(null, Typeface.BOLD);
        tvEdit.setGravity(Gravity.CENTER_HORIZONTAL);
        tvEdit.setTextColor(Color.WHITE);
        tr.addView(tvEdit);

        // Add row to Table
        tbl.addView(tr);
    }

    /**
     * LOAD TABLE w/forward points
     */
    private void loadForwardPointsOnTable() {
        // Refresh UI data content
        refreshPointTable(intentLineId
                , PointStatusInterface.POINT_ONEWAYVALUE_FORWARD
                , GravityMobileDBInterface.SQL_ASC);
    }

    /**
     * LOAD TABLE w/ return points
     */
    private void loadReturnPointsOnTable() {
        // Refresh UI data content
        refreshPointTable(intentLineId
                , PointStatusInterface.POINT_ONEWAYVALUE_RETURN
                , GravityMobileDBInterface.SQL_ASC);
    }

    /**
     * CANCEL button
     */
    private void goToCancel() {
        Log.d(LOG_TAG, "Click!, Going to LineSelectionActivity!");
        // ISSUE MEMORY-LEAKS
        Intent intent = new Intent(getApplicationContext(), LineSelectionActivity.class);
        startActivity(intent);
    }

    /**
     * CLOSE LINE Button
     */
    private void goToCloseLine() {
        Log.d(LOG_TAG, "Click!, LINE CLOSING!");
        //guide: https://developer.android.com/guide/topics/ui/dialogs
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(LineDataActivity.this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(R.string.line_data_activity_msg3);
        alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // CLOSE LINE
                closeLine();
            }
        });
        alertBuilder.setNegativeButton(R.string.cancel, new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }

    /**
     * CLOSE LINE
     */
    private void closeLine() {
        String errorMsg = this.getString(R.string.line_data_activity_msg4);
        boolean isValid = true;

        //GravityMobileDBHelper db = GravityMobileDBHelper(getApplicationContext());
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        List<Point> forwardPoints = db.getPointsByLineIdAndOnwWayVal(intentLineId
                , PointStatusInterface.POINT_ONEWAYVALUE_FORWARD
                , GravityMobileDBHelper.SQL_ASC);

        db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        List<Point> returnPoints = db.getPointsByLineIdAndOnwWayVal(intentLineId
                , PointStatusInterface.POINT_ONEWAYVALUE_RETURN
                , GravityMobileDBHelper.SQL_ASC);

        db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        Line l = db.getLineByLineId(intentLineId);

        // 1- VALIDATE IF LINE IS ACTUALLY CLOSED , IF CLOSED CAN'T BE CLOSED AGAIN
        if (LineStatusInterface.LINE_STATUS_CLOSED.equals(l.getStatus())) {
            isValid = false;
            errorMsg = this.getString(R.string.line_data_act_close_exec);

        // Incorrect Qty of points
        } else if (returnPoints == null || forwardPoints == null) {
            isValid = false;
            errorMsg = this.getString(R.string.line_data_act_err_qty);

        // VALIDATE POINTS RET and FWD QUANTITY ARE EQUAL
        } else if (returnPoints.size() == forwardPoints.size()) {

            //Validate all points WERE readed
            // FWD POINTS
            Iterator<Point> fwdIter = forwardPoints.iterator();
            while (fwdIter.hasNext()) {
                if (fwdIter.next().getStatus().equals(PointStatusInterface.POINT_STATUS_ANOMALY)) {
                    isValid = false;
                    errorMsg = this.getString(R.string.line_data_act_unobserved);
                }
            }
            // REV POINTS
            Iterator<Point> retIter = returnPoints.iterator();
            while (retIter.hasNext()) {
                Point auxPoint = retIter.next();
                // ANOMALY:Some calculations couldn't be done, and those POINTS must be re-observed
                // PENDING:REV POINTS are created when user executes RETURN operation through a button
                // on LineDataActivity screen. A list of REV version of FWD POINTS are created but
                // these POINTS are in PENDING state and must be filled with data for long, lat, g1g2g3
                // altitude, etc
                if (auxPoint.getStatus().equals(PointStatusInterface.POINT_STATUS_ANOMALY)
                        || (auxPoint.getStatus().equals(PointStatusInterface.POINT_STATUS_PENDING))) {
                    isValid = false;
                    errorMsg = this.getString(R.string.line_data_act_incomplete);
                }
            }

            // VALIDATE Quantity of RETURN POINTS >=2
        } else if (returnPoints.size() < 2) {
            isValid = false;
            errorMsg = this.getString(R.string.line_data_act_points_min_2);
        }

        // TRY TO CLOSE THE LINE
        if (isValid) {
            // LINE Closing Intent
            CssGravityLine g = new CssGravityLine(this);
            // Make calculation
            boolean close = g.getDeltas(intentLineId);

            // Change LINE STATUS :: CLOSE LINE
            if (close) { // SUCCESS
                db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
                try {
                    db.closeLine(intentLineId, LineStatusInterface.LINE_STATUS_CLOSED);
                    errorMsg = this.getString(R.string.line_data_act_success);
                }finally{
                    // IMPROVEMENT-5:: DB
                    db.close();
                }

            } else { // ERROR
                try {
                    db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
                    db.closeLine(intentLineId, LineStatusInterface.LINE_STATUS_INCONSISTENCE);
                    errorMsg = this.getString(R.string.line_data_act_close_err);
                    // mark erroneous observations in POINTS is not possible
                    // this is a task to resolve by the field technician in
                    // charge of take such observations
                }finally{
                    // IMPROVEMENT-5:: DB
                    db.close();
                }
            }

        }
        // Show validation results
        errorAlert(errorMsg);
    }

    /**
     * START RETURN
     */
    private void startReturn() {
        Log.i(LOG_TAG, "startReturn()::IN");
        int pointsCreatedCounter = 0;

        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        List<Point> forwardPoints;
        List<Point> returnPoints;
        try{
        //ISSUE-001 List<Point> forwardPoints = db.getPointsByLineIdAndOnwWayVal(intentLineId
        forwardPoints = db.getPointsByLnIdOneWayValOrderByPointId(intentLineId
                , PointStatusInterface.POINT_ONEWAYVALUE_FORWARD
                , GravityMobileDBHelper.SQL_DESC);

        db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        returnPoints = db.getPointsByLineIdAndOnwWayVal(intentLineId
                , PointStatusInterface.POINT_ONEWAYVALUE_RETURN
                , GravityMobileDBHelper.SQL_DESC);
        }finally{
            db.close();
        }

        if (forwardPoints == null) {
            errorAlert("No es posible ejecutar: COMENZAR RETORNO no se han creado puntos.");

        } else if (returnPoints != null) {
            if (forwardPoints.size() == returnPoints.size()) {
                errorAlert("No es posible ejecutar: COMENZAR RETORNO la operacion ya fue ejecutada.");
            }

            // VALIDATE Quantity of RETURN POINTS must be almost >= 2
        } else if (forwardPoints.size() < 2) {
            errorAlert(this.getString(R.string.line_data_act_points_min_2_ret));

        } else {
            // IMPROVEMENT-1::Transactional inserts::IN
            TransactionalDBHelper tx = new TransactionalDBHelper(getApplicationContext(), false);
            tx.beginTransaction();
            try {
                // For each FORWARD POINT Create a RETURN POINT
            // FWD list comes ordered DESC, to create RETURN POINTS
            // and its IDs in that order
            Iterator<Point> iter = forwardPoints.iterator();
            while (iter.hasNext()) {
                pointsCreatedCounter++;
                Point fwdPoint = iter.next();
                Point retPoint = new Point();

                retPoint.setCode(fwdPoint.getCode());
                retPoint.setLongitude(fwdPoint.getLongitude());
                retPoint.setLatitude(fwdPoint.getLatitude());
                retPoint.setOffset(fwdPoint.getOffset());
                retPoint.setHeight(fwdPoint.getHeight());
                retPoint.setStatus(PointStatusInterface.POINT_STATUS_PENDING);
                retPoint.setOneWayValue(PointStatusInterface.POINT_ONEWAYVALUE_RETURN);
                retPoint.setUserId(fwdPoint.getUserId());
                retPoint.setLineId(fwdPoint.getLineId());
                // ISSUE-005
                // RULE: in RETURN Date will be set by the user on field
                retPoint.setDate(DBTools.getUTCTime());

                // IMPROVEMENT-1::Transactional inserts:
                tx.getGravityMobileDBHelper().createPointForReverse(retPoint);
            }

            if (pointsCreatedCounter == forwardPoints.size()) {
                errorAlert("COMENZAR RETORNO fue ejecutado con exito, se crearon: "
                        + pointsCreatedCounter + "PUNTOS para la operacion de RETORNO");

                // UPDATE LINE STATUS TO RETURNED
                tx.getGravityMobileDBHelper().returnLine(intentLineId, LineStatusInterface.LINE_STATUS_RETURNED);
                Log.i(LOG_TAG, "IN::tx.returnLine!!!");

                // IMPROVEMENT-1::Transactional inserts:
                tx.setTransactionSuccessful();
            }

                // IMPROVEMENT-1::Transactional inserts:
            } catch(Exception ex) {
                Log.e(LOG_TAG, "EXCEPTION:" + ex.getMessage());
            } finally {
                // IMPROVEMENT-1::Transactional inserts:
                tx.endTransaction();
                Log.i(LOG_TAG, "TX.endTransaction()");
                tx.getGravityMobileDBHelper().close();
                Log.i(LOG_TAG, "TX.CLOSE()");

                loadReturnPointsOnTable();
                // ISSUE-014 show RETURN tab
                btnReturn.callOnClick();

            } // END::IMPROVEMENT-1::Transactional inserts:


            // ERROR
            if(pointsCreatedCounter != forwardPoints.size()) {
                errorAlert("COMENZAR RETORNO fue ejecutado con ERROR");

            }

        }
    }

    /**
     * Generic Alert
     *
     * @param errorMessage
     */
    private void errorAlert(String errorMessage) {
        Log.d(LOG_TAG, "Click!, VError");
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(LineDataActivity.this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(errorMessage);
        alertBuilder.setNegativeButton(R.string.ok, new MyOnClickListener());

        alertBuilder.create();
        alertBuilder.show();
    }

    /**
     * START RETURN button
     */
    private void goToStartReturn() {
        Log.d(LOG_TAG, "Click!, START RETURN!");
        //https://developer.android.com/guide/topics/ui/dialogs
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(LineDataActivity.this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(R.string.line_data_activity_msg1);
        alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // START RETURN
                startReturn();
            }
        });
        alertBuilder.setNegativeButton(R.string.cancel, new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }

    /**
     * NEW POINT button
     * Just FWD points can be created manually.
     */
    private void goToCreateNewPoint() {
        Log.d(LOG_TAG, "Click!, CREATE A NEW POINT!");
        // ISSUE MEMORY-LEAKS
        final Intent intent = new Intent(getApplicationContext(), PointCRUDActivity.class);
        //guide: https://developer.android.com/guide/topics/ui/dialogs
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(LineDataActivity.this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(R.string.line_data_activity_msg2);
        alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Values for the next screen.
                intent.putExtra(
                        IntentDataKeyInterface.LINE_DATA_ACTIVITY_TYPE_OPERATION,
                        IntentDataKeyInterface.LINE_DATA_ACTIVITY_NEW);
                intent.putExtra(
                        IntentDataKeyInterface.LINE_DATA_ACTIVITY_LINE_ID_INT,
                        intentLineId);
                // ISSUE-004
                intent.putExtra(
                        IntentDataKeyInterface.LINE_DATA_ACT_POINT_ONEWAY_VAL_INT,
                        PointStatusInterface.POINT_ONEWAYVALUE_FORWARD);

                startActivityForResult(intent, 0);
            }
        });
        alertBuilder.setNegativeButton(R.string.cancel, new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }

    /**
     * Just can be created manually a POINT in the FWD WAY.
     *
     * @param point
     */
    private void goToEditPoint(Point point) {
        Log.d(LOG_TAG, "Click!, Going to PointCRUDActivity!");
        // ISSUE MEMORY-LEAKS
        Intent intent = new Intent(getApplicationContext(), PointCRUDActivity.class);
        // Values for the next screen.
        intent.putExtra(IntentDataKeyInterface.LINE_DATA_ACTIVITY_TYPE_OPERATION,
                IntentDataKeyInterface.LINE_DATA_ACTIVITY_UPDATE);
        intent.putExtra(IntentDataKeyInterface.LINE_DATA_ACTIVITY_POINT_ID_INT,
                point.getmId());
        intent.putExtra(IntentDataKeyInterface.LINE_DATA_ACTIVITY_POINT_CODE_STR,
                point.getCode());
        intent.putExtra(IntentDataKeyInterface.LINE_DATA_ACTIVITY_LINE_ID_INT,
                point.getLineId());
        intent.putExtra(IntentDataKeyInterface.LINE_DATA_ACT_POINT_ONEWAY_VAL_INT,
                point.getOneWayValue());
        startActivityForResult(intent, 0);
    }

    /**
     * GO TO LINE DATA SCREEN
     */
    private void goToLineDataActivity() {
        Log.d(LOG_TAG, "Click!, Going to detailActivity!");
        // ISSUE MEMORY-LEAKS
        Intent intent = new Intent(getApplicationContext(), LineDataActivity.class);
        startActivity(intent);
    }

    // GETTERS and SETTERS
    public int getIntentLineId() {
        return intentLineId;
    }

    public void setIntentLineId(int intentLineId) {
        this.intentLineId = intentLineId;
    }

    public boolean isLineIsEditable() {
        return lineIsEditable;
    }

    public void setLineIsEditable(boolean lineIsEditable) {
        this.lineIsEditable = lineIsEditable;
    }

    private static class MyOnClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            // DO NOTHING
        }
    }
}
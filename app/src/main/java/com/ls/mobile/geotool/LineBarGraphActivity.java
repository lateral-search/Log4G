package com.ls.mobile.geotool;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.ls.mobile.geotool.db.GravityMobileDBHelper;
import com.ls.mobile.geotool.db.GravityMobileDBInterface;
import com.ls.mobile.geotool.workflow.PointStatusInterface;
import com.ls.mobile.geotool.db.data.model.Gravimeter;
import com.ls.mobile.geotool.db.data.model.Line;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * // MATLAB: function plot(self, instrument, varargin)
 * // % plot deltas
 * // d = self.deltas{i};
 * // r = self.residuals{i};
 * // ----------------------
 * // % Stairstep graph
 * // % stairs(X,Y) plots the elements in Y at the
 * // % locations specified by X. The inputs X and Y must
 * // % be vectors or matrices of the same size. Additionally,
 * // % X can be a row or column vector and Y must be a matrix with length(X) rows.
 * // % Use h to make changes to properties of a specific Stair object after it is created.
 * // PLOT DELTAS CURVE:
 * // dh = stairs([d;d(end)],'-o');
 * // PLOT RESIDUALS CURVE:
 * // rh = stairs([r; r(end)],'--o');
 * // ylabel('Residuals [mGal]')
 * <p>
 * LS SOLUTION FOR CHARTS::
 * MPAndroidChart FRAMEWORK:
 *
 * GUIDE: https://weeklycoding.com/mpandroidchart-documentation/axis-general/
 *
 *
 * MemLeaks Q&A: 0k    [NOTE: Anonymous class may be named 'static' inner class
 *                     see lines with: "...(new IValueFormatter() {  ..."
 *                     Or just run in Android Studio Analize->InspectCode
 *                     In my opinion there was no leak, and I have no time
 *                     to do that change]
 *
 * @author lateralSearch
 */
public class LineBarGraphActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,
        OnChartGestureListener, OnChartValueSelectedListener {

    // Text/numbers inside chart, scales, etc
    private float TXT_SIZE = 14f;
    // Line width of the curves
    private float LINE_WIDTH = 3f;
    // Circle or mark working as "dot" in the curve
    private float CIRCLE_RADIUS = 5f;
    private float LABEL_DISTANCE = 5f;
    private int LABEL_D_INDEX = 5;
    private final static String DECIMAL_FORMAT = "#.###";

    LineChart chart;
    Button graficoPtsBtn;
    TextView txtTittle;

    List<com.ls.mobile.geotool.db.data.model.Point> pointLst;
    private String[] mLabelNames;
    // Log
    private static final String LOG_TAG = LineBarGraphActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_bar_graph);

        // TextView
        txtTittle = findViewById(R.id.txtTittle);

        // Button
        graficoPtsBtn = findViewById(R.id.graficoPtsBtn);
        graficoPtsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // RETURN TO PREVIOUS SCREEN
                goToLineSelectionActivity();
            }
        });

        // LineChart is initialized
        chart = (LineChart) findViewById(R.id.graphBoard);
        Line line = null;

        // Get line data: Values coming from LineSelectionActivuty screen
        int intentLineId = getIntent()
                .getIntExtra(
                        IntentDataKeyInterface.LINE_SELECTION_ACTIVITY_LINE_ID_INT, 0);

        // Get Points from the Line
        pointLst = loadPointListByLine(intentLineId);

        // SET SUB-TITTLE IN ACTIVITY
        if (pointLst != null) {
            GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
            line = db.getLineByLineId(intentLineId);
            db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
            Gravimeter g = db.getGravimetersByID(line.getGravimeterId());

            String txt = "Deltas and Residuals - Instrument:" +
                    g.getName() +
                    " Drift Rate: " +
                    line.getDriftRate();

            txtTittle.setText(txt);
            setTitle(txt);
        }

        // LABELS:: Retrieve delta names to use as labels
        mLabelNames = getDeltaNames();

        chart.setDrawGridBackground(true);
        chart.getDescription().setEnabled(true);
        chart.setDrawBorders(true);
        // Chart FRAME color
        chart.setBorderColor(getResources().getColor(R.color.ls_primary_material_light));
        // Because this is an step chart, sometimes is difficult to visualize results
        // in a WHITE background
        chart.setBorderWidth(10);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setDrawAxisLine(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getXAxis().setDrawAxisLine(true);
        chart.getXAxis().setDrawGridLines(false);

        // draw limit lines behind data instead of on top
        //yAxis.setDrawLimitLinesBehindData(true);
        //ll2.setLabelPosition(LimitLabelPosition.RIGHT_BOTTOM);
        //setColor(Color.rgb(104, 241, 175));

        /**
         * IN :: CHART CONFIGURATION(AXIS XY GRID VALUES):
         */
        // X Axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        //xAxis.setAxisMinimum(0f);
        //xAxis.setGranularity(1f);
        xAxis.setTextSize(TXT_SIZE);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                // Names to show in X axis
                String a = mLabelNames[(int) value % mLabelNames.length];
                //Log.i(LOG_TAG,"mLabelNames[(int) value % mLabelNames.length]" + a);
                return a;
            }
        });

        // FORCE TO SEE ALL THE DELTA NAMES LABELS,
        // OTHERWISE LABELS CAN BE SEEN ONLY ZOOMING
        xAxis.setLabelCount(mLabelNames.length, true);
        xAxis.setLabelRotationAngle(45);
        //xAxis.setDrawLabels(true);
        //xAxis.setDrawGridLines(true);

        // Y LEFT VALUES
        YAxis yLeftAxis = chart.getAxisLeft();
        yLeftAxis.setEnabled(true);
        // Smaller scale than right scale
        //yLeftAxis.setAxisMinimum(-0.2f);
        //yLeftAxis.setGranularity(1f);
        yLeftAxis.setTextSize(TXT_SIZE);
        yLeftAxis.setTextColor(Color.WHITE);
        yLeftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);

        // Y RIGHT VALUES
        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(true);
        // Bigger scale than left scale
        //yAxisRight.setAxisMinimum(-100f);
        //yAxisRight.setGranularity(0f);
        yAxisRight.setTextSize(TXT_SIZE);
        yAxisRight.setTextColor(Color.WHITE);
        yAxisRight.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);

        // Restricting Intervals:
        // In case you are using a formatter based on array indices
        // it makes sense to restrict the minimum interval of your axis to "1":
        // yAxis.setGranularity(1f); // restrict interval to 1 (minimum)
        // This will prevent the formatter from drawing duplicate axis labels
        // (caused by axis intervals < 1). As soon as the "zoom level" of the
        // chart is high enough, it will stop recalculating smaller intervals.

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        // LEGEND: ([dh,rh,sh],'Deltas','Residuals','3\sigma limit')
        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setTextColor(Color.WHITE);
        l.setEnabled(true);
        /**
         * OUT :: SETTING FORMATER (AXIS XY GRID VALUES):
         */

        // STEP LINE CHART BUILD
        fillChart();
    }

    /**
     * FILL DATA FOR THE "STEP" LINE CHART
     */
    public void fillChart() {

        chart.resetTracking();

        /**
         * CREATING DATASETS:
         * Every individual value of the raw data should be represented as an Entry.
         * An ArrayList of such Entry objects is used to create a DataSet.
         */
        ArrayList<Entry> entriesDeltas = new ArrayList<>();
        ArrayList<Entry> entriesResiduals = new ArrayList<>();
        Iterator<com.ls.mobile.geotool.db.data.model.Point> pointItr = pointLst.iterator();

        // X Axis
        float xCoord = 0;
        int labelIndex = 0;
        while (pointItr.hasNext()) {
            com.ls.mobile.geotool.db.data.model.Point pAux = pointItr.next();
            // DELTAS
            float deltaTruncated = truncateDecimal(pAux.getDelta(),4).floatValue();
            Log.d(LOG_TAG, "deltaTruncated" + deltaTruncated);
            entriesDeltas.add(new Entry(xCoord, deltaTruncated, mLabelNames[labelIndex]));
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "mLabelNames[labelIndex]" + mLabelNames[labelIndex]);
                Log.d(LOG_TAG, "pAux.getDelta()" + pAux.getDelta());
            }
            // RESIDUALS
            float residualTruncated = truncateDecimal(pAux.getResidual(),4).floatValue();
            Log.d(LOG_TAG, "residualTruncated" + residualTruncated);
            entriesResiduals.add(new Entry(xCoord, residualTruncated, mLabelNames[labelIndex]));
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "pAux.getResidual()" + pAux.getResidual());
            }
            xCoord = xCoord + LABEL_DISTANCE;
            labelIndex = labelIndex + LABEL_D_INDEX;
        }

        // DATASET WITH GRAPH DATA.
        ArrayList<ILineDataSet> iLineDataSetLst = new ArrayList<>();

        // 2 - CREATE "Entry" OBJECTS LIST WITH X, Y DATA:
        // We use the LineChart, for which the Entry class represents a single entry in the
        // chart with x- and y-coordinate. Other chart types, such as BarChart use other
        // classes (e.g. BarEntry) for that purpose.
        /**
         * FORMAT EACH CURVE:
         * As a next step, you need to add the List<Entry> you created to a LineDataSet
         * object. DataSet objects hold data which belongs together, and allow individual
         * styling of that data. The below used "Label" has only a descriptive purpose
         * and shows up in the Legend, if enabled.
         *
         * Now, initialize the LineDataSet and pass the argument as
         * an ArrayList of Entry object.
         */
        // EACH ITERATION IS A DATA SET.
        /**
         * DELTAS
         */
        // FIRST CURVE
        LineDataSet lineDeltasDataSet = new LineDataSet(entriesDeltas, "Deltas");

        // STEPS GRAPH
        lineDeltasDataSet.setMode(LineDataSet.Mode.STEPPED);
        // OTHER VALUES
        lineDeltasDataSet.setValueTextSize(TXT_SIZE);
        lineDeltasDataSet.setLineWidth(LINE_WIDTH);
        lineDeltasDataSet.setCircleRadius(CIRCLE_RADIUS);
        // COLORS
        lineDeltasDataSet.setColor(ColorTemplate.LIBERTY_COLORS[2]);
        lineDeltasDataSet.setCircleColor(ColorTemplate.LIBERTY_COLORS[2]);
        lineDeltasDataSet.setValueTextColor(ColorTemplate.LIBERTY_COLORS[2]);
        // SET DEPENDENCY CAUSE DIFFERENT (bigger) SCALE USED
        lineDeltasDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        lineDeltasDataSet.setDrawValues(true);
        lineDeltasDataSet.setValueFormatter(new IValueFormatter() {
            private DecimalFormat mFormat = new DecimalFormat(DECIMAL_FORMAT);
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return mFormat.format(value);
            }
        });
        // ADD DELTA CURVE TO THE CHART
        iLineDataSetLst.add(lineDeltasDataSet);
        /**
         * RESIDUALS
         */
        //SECOND CURVE
        LineDataSet lineResidualsDataSet = new LineDataSet(entriesResiduals, "Residuals");
        // STEPS GRAPH
        lineResidualsDataSet.setMode(LineDataSet.Mode.STEPPED);
        // OTHER VALUES
        lineResidualsDataSet.setValueTextSize(TXT_SIZE);
        //lineResidualsDataSet.setLineWidth(LINE_WIDTH);
        lineResidualsDataSet.setCircleRadius(CIRCLE_RADIUS);
        // COLORS
        lineResidualsDataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        lineResidualsDataSet.setCircleColor(ColorTemplate.COLORFUL_COLORS[1]);
        lineResidualsDataSet.setValueTextColor(ColorTemplate.COLORFUL_COLORS[1]);
        // SET DEPENDENCY CAUSE DIFFERENT (smaller) SCALE USED
        lineResidualsDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        // DASHED THE LINE OF PARTICULAR DATASET:
        lineResidualsDataSet.enableDashedLine(10, 10, 0);
        // PLOT DATA FORMAT (THIS IS NOT THE DATA IN THE AXIS, IS THE DATA IN THE PLOT)
        // GUIDE: https://www.programcreek.com/java-api-examples/?api=com.github.mikephil.charting.formatter.IValueFormatter
        lineResidualsDataSet.setValueFormatter(new IValueFormatter() {
            private DecimalFormat mFormat = new DecimalFormat(DECIMAL_FORMAT);
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return mFormat.format(value);
            }
        });

        // ADD RESIDUALS PLOT TO THE CHART
        iLineDataSetLst.add(lineResidualsDataSet);

        // ADD CHARTS TO A LINEDATA OBJECT WRAPPING ALL DATA FOR THE CHART:
        // As a last step, add the LineDataSet object (or objects)
        // we created to a LineData object. This object holds all data that
        // is represented by a Chart instance and allows further styling.
        LineData lineData = new LineData(iLineDataSetLst);

        // MPANDROID BUG FIX :: https://github.com/PhilJay/MPAndroidChart/issues/2450
        // Invalid index 0, size is 0 in lineChart #2450
        if (lineData.getDataSets().size() == 0) {
            chart.clear();
        } else {
            // set data
            chart.setData(lineData);
        }

        // ANIMATE
        chart.animateXY(3000, 3000); // animate horizontal and vertical 3000 milliseconds

        // REFRESH: After creating the data object, you can set it to
        // the chart and refresh it:
        // Don't use invalidate(), it doesn't work
        chart.postInvalidate();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: ");//+ me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);
        // un-highlight values after the gesture is finished and no single-tap
        //if(lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP) {
        //chart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
        //}
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart long pressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart fling. VelocityX: ");//+ velocityX + ", VelocityY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: ");// + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: ");//+ dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("VAL SELECTED",
                "Value: ");// + e.getY() + ", xIndex: " + e.getX()
        //+ ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    /**
     * Return an Array of Benchmarks in the format "xxxx-xxxx"
     * to be placed as XAxis labels on the chart
     */
    private String[] getDeltaNames() {
        Log.i(LOG_TAG, "IN::getDeltaNames()");
        Iterator<com.ls.mobile.geotool.db.data.model.Point> pointIterator = pointLst.iterator();
        int n = pointLst.size();
        String[] result = new String[n * LABEL_D_INDEX];
        String deltaName = "";
        int i = 0;

        while (pointIterator.hasNext()) {
            com.ls.mobile.geotool.db.data.model.Point pAux = pointIterator.next();
            if (i == 0) {// First Iteration
                if (pointIterator.hasNext()) {
                    deltaName = pAux.getCode() + "-" + pointIterator.next().getCode();
                    //Log.i(LOG_TAG, "deltaName :: " + deltaName);
                }

            } else {    // Sucessive Iterations
                String deltaNameAux = deltaName.substring(deltaName.indexOf("-") + 1, deltaName.length());
                deltaName = deltaNameAux + "-" + pAux.getCode();
                //Log.i(LOG_TAG, "deltaName :: " + deltaName);

            }
            result[i] = deltaName;
            for (int j = i; j < i + LABEL_D_INDEX; j++) {
                //result[j + 1] = String.valueOf(j);
                // We don't want to see the numbers on the X axis, just the labels
                // that's why a blank is assigned, and not the index number.
                result[j + 1] = " ";
            }
            i = i + LABEL_D_INDEX;
        }
        return result;
    }
    /**
     * MPAndroidChart FRAMEWORK::OUT
     *
     * GUIDE: https://github.com/PhilJay/MPAndroidChart/wiki/Getting-Started
     */


    /**
     * Load the Line List data from SQLite database
     */
    private List<com.ls.mobile.geotool.db.data.model.Point> loadPointListByLine(int lineId) {
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        return db.getPointsByLineIdAndOnwWayVal(lineId,
                PointStatusInterface.POINT_ONEWAYVALUE_FORWARD,
                GravityMobileDBInterface.SQL_ASC);
    }

    /**
     * RETURN TO LINE SELECTION ACTIVITY SCREEN
     */
    private void goToLineSelectionActivity() {
        Log.d(LOG_TAG, "Click!, Going to Line Select!");
        // ISSUE MEMORY-LEAKS
        Intent intent = new Intent(getApplicationContext(), LineSelectionActivity.class);
        startActivity(intent);
    }

    private static BigDecimal truncateDecimal(double x, int numberofDecimals)
    {
        if ( x > 0) {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
        } else {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
        }
    }


}// End class LineBarGraphActivity.java


//---------------------------IN:LOCAL CLASS-----------------------------------------------//
//---------------------------IN:LOCAL CLASS-----------------------------------------------//
//---------------------------IN:LOCAL CLASS-----------------------------------------------//

/**
 * LocalClassXAxisValueFormatter
 * <p>
 * GUIDE:
 * https://github.com/PhilJay/MPAndroidChart/wiki/The-AxisValueFormatter-interface
 */
class LocalClassXAxisValueFormatter implements IAxisValueFormatter {

    private String[] mValues;

    public LocalClassXAxisValueFormatter(String[] values) {
        this.mValues = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        Log.i("LclClassXAximVal-value",String.valueOf(value));

        /**
         * MPAndroidChart BUG #2802
         * java.lang.ArrayIndexOutOfBoundsException #2802
         *
         * https://github.com/PhilJay/MPAndroidChart/issues/2802
         * anyeloamt commented on Sep 3, 2017
         * I had this issue too, I resolved with something like this.
         */
        // "value" represents the position of the label on the axis (x or y)
        //int intValue = (int) value;

        //if (mValues.length > intValue && intValue >= 0) {
            Log.i("LclClassXAximVal[intV",mValues[(int)value]);
            return mValues[(int)value];
        //} else {
        //    return "";
        //}
    }

    /**
     * this is only needed if numbers are returned, else return 0
     */
    public int getDecimalDigits() {
        return 0;
    }

}// End local class LocalClassXAxisValueFormatter


/**
 * LocalClassYAxisValueFormatter
 * <p>
 * GUIDE:
 * https://github.com/PhilJay/MPAndroidChart/wiki/The-AxisValueFormatter-interface
 */
class LocalClassYAxisValueFormatter implements IAxisValueFormatter {


    private String[] mValues;

    public LocalClassYAxisValueFormatter(String[] values) {
        this.mValues = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        Log.i("LclClassYAximVal-value",String.valueOf(value));
        /**
         * MPAndroidChart BUG #2802
         * java.lang.ArrayIndexOutOfBoundsException #280
         * https://github.com/PhilJay/MPAndroidChart/issues/2802
         * anyeloamt commented on Sep 3, 2017
         * I had this issue too, I resolved with something like this.
         */
        // "value" represents the position of the label on the axis (x or y)
        //int intValue = (int) value;

        //if (mValues.length > intValue && intValue >= 0) {
            Log.i("LclClassYAximVal[intV",mValues[(int)value]);
            return mValues[(int)value];
        //} else {
          //  return "";
        //}
    }

    /**
     * this is only needed if numbers are returned, else return 0
     */
    public int getDecimalDigits() {
        return 0;
    }

}// End class LocalClassYAxisValueFormatter


//---------------------------END:LOCAL CLASS-----------------------------------------------//
//---------------------------END:LOCAL CLASS-----------------------------------------------//
//---------------------------END:LOCAL CLASS-----------------------------------------------//

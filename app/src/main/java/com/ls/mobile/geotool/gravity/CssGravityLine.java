package com.ls.mobile.geotool.gravity;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ls.mobile.geotool.common.LSMatJLab;
import com.ls.mobile.geotool.db.DBTools;
import com.ls.mobile.geotool.db.GravityMobileDBHelper;
import com.ls.mobile.geotool.db.GravityMobileDBInterface;
import com.ls.mobile.geotool.db.TransactionalDBHelper;
import com.ls.mobile.geotool.workflow.PointStatusInterface;
import com.ls.mobile.geotool.db.data.model.Line;
import com.ls.mobile.geotool.db.data.model.Point;
import com.ls.mobile.geotool.time.DateConverter;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.equation.Equation;
import org.ejml.equation.ManagerFunctions;
import org.ejml.equation.ManagerTempVariables;
import org.ejml.equation.Operation;
import org.ejml.equation.Variable;
import org.ejml.equation.VariableInteger;
import org.ejml.equation.VariableMatrix;
import org.ejml.simple.SimpleMatrix;

import java.util.Date;
import java.util.List;

/**
 * % CLASS CssGravityLine:
 * <p>
 * Handles the gravity line operations such as delta calculation,
 * instrument drifts, importing from Kevin's g2Gravity structure, etc.
 * Usage: you either pass a string with the location of the line
 * structure information or you pass a start and end city with a
 * directory name.
 */

/**
 * CHECK MAIL:
 * <p>
 * Esta estructura está bien, pero como te comenté arriba, no hay que
 * combinarla con la db que te pasé sino que más bien vendría en un
 * "archivo separado", como en line_struct.xls. Más allá de ese detalle,
 * los campos están bien hasta la columna "offset". Estaría bueno agregar
 * la columna reading (el promedio de g1, g2, g3) y reduced_g (la gravedad
 * reducida usando la formula que te había dado cuando nos reunimos la
 * primera vez, que hace falta para el chequeo de consistencia).
 * <p>
 * 1-) Los campos offset, absolute_g, uncertainty los agrego en la tabla de
 * la base de datos que tiene la aplicacion mobile,
 * <p>
 * Offset si, porque eso se carga en el campo para identificar
 * el offset entre la medición de gravedad y la marca GPS precisa.
 * Absolute_g y uncertainty no hacen falta.
 * <p>
 * 2-)   Sobre absolute_g: PREGUNTA: el tema de la gravedad absoluta,
 * lo hablamos, pero creo que al final no lo definimos, segun entiendo,
 * las medidas de gravedad absoluta "absolute_g", van a necesitar agregarlas
 * en ciertos puntos durante el trabajo de campo, entonces habria que
 * agregar un campo editable de gravedad absoluta en la pantalla de
 * carga de datos en un punto, era asi efectivamente?
 * <p>
 * No, el valor de gravedad absoluta no es necesario para el trabajo de
 * campo. Solamente se usa para el ajuste final. Te lo mandé porque
 * estaba en la estructura, pero podés ignorarlo tranquilo :)
 *
 * @author Andres Hernan Pityla C
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
public class CssGravityLine {

    private String line_name;
    private String line_filename;
    private String benchmarks;
    private String observations;
    private String directions;
    private String default_dir; //% for lines with only one direction, save which one if default.
    private String start_benchmark;
    private String end_benchmark;
    private String instruments;
    private String drifts;
    private String deltas;
    private String residuals;
    private String status;  // % saves the benchmark-instrument status (discarded obs).
    // Rows = instruments; cols = benchmarks
    private String design;  // % cell array containing the delta-benchmark-instrument
    // design matrix. In each cell one instrument as follows:
    // Rows = obs; cols = benchmarks
    private String comments;// % self explanatory field

    private AppCompatActivity appCompatActivity;

    // Log
    private static final String LOG_TAG = CssGravityLine.class.getSimpleName();

    public CssGravityLine(AppCompatActivity activity) {
        appCompatActivity = activity;
    }

    /**
     * % populate the deltas and drifts
     * <p>
     * PORTED MATLAB FUNCTION: getDeltas
     * <p>
     * Only used in lines ready TO CLOSE
     * We first calculate reduced_G, and after we will calculate the Deltas
     * <p>
     * MAIL DEMIAN:
     * Estaría bueno agregar la columna reading (el promedio de g1, g2, g3) y reduced_g
     * (la gravedad reducida usando la formula que te había dado cuando nos reunimos la primera vez,
     * que hace falta para el chequeo de consistencia).
     */
    public boolean getDeltas(int lineId) {
        Log.i(LOG_TAG, "START EXECUTING: getDeltas WITH IDLINE: " + lineId);

        // % load the data from a line structure
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(appCompatActivity,false);
        Line line = db.getLineByLineId(lineId);
        db.close();

        // MATLAB SRC CODE:: THIS MATLAB CODE DOES NOT APPLY
        // % this line has two directions
        // TO CLOSE LINE Log4G always has simetrical fwd and rev, and it is always validated before
        // the call to GetDeltas
        // % compute the drift rate and deltas for each instrument

        // MATLAB SRC CODE::
        // % design::: cell array containing the delta-benchmark-instrument design matrix.
        // ndeltas = size(self.design{i},1); (MATLAB ndeltas stores nbr of benchamarks in one way
        // that is the half of a complete line.
        // size (a, dim) Return a row vector with the size (number of elements) of
        // each dimension for the object. When given a second argument, dim, return
        // the size of the corresponding dimension.

        // MATLAB SRC CODE::LINE 234
        // % get the forward and reverse lines from the
        // % observation array for this instrument

        // IMPROVEMENT-5:: DB
        List<Point> fwd;
        List<Point> rev;
        try {
            db = GravityMobileDBHelper.getInstance(appCompatActivity,false);
            fwd = db.getPointsByLineIdAndOnwWayVal(lineId
                    , PointStatusInterface.POINT_ONEWAYVALUE_FORWARD
                    , GravityMobileDBInterface.SQL_ASC);

            db = GravityMobileDBHelper.getInstance(appCompatActivity,false);
            rev = db.getPointsByLineIdAndOnwWayVal(lineId
                    , PointStatusInterface.POINT_ONEWAYVALUE_RETURN
                    , GravityMobileDBInterface.SQL_ASC);
        }finally{
            db.close();
        }

        // MATLAB SRC CODE:: DOES NOT APPLY, Log4G lines are ready to close
        // % if number of fwd and rev observations is not equal
        // MATLAB SRC CODE::
        // % identify lines with 2 directions but with multiple
        // % observations using same gravimeter (THIS MEANS IT'S A CORRECTED LINE)

        // % In each cell one instrument as follows:
        // Rows = obs;
        // cols = benchmarks

        // UNUSED CODE:: SEE AT THE END OF THIS CLASS THE BLOCK

        // MATLAB SRC CODE::LINE 435
        // DESIGN MATRIX::
        // % design::: cell array containing the delta-benchmark-instrument design matrix.
        // % In each cell one instrument as follows: Rows = obs; cols = benchmarks
        //   ndeltas = size(self.design{i},1);

        // % compute the drift rate and deltas for each instrument
        int nDeltas = fwd.size();//  * 2;
        // (* 2) gives tbe total nbr of benchmarks fwd and rev

        // FWD BENCHMARKS
        int k = 0;
        int c = 1;
        String tNames[] = new String[fwd.size()]; // NEVER READ(dont delete anyway)
        double dFwd[][] = new double[fwd.size()][c];
        Date tFwdDate[][] = new Date[fwd.size()][c];// In MATLAB is tFwd[][] for all the function

        for (int j = 0; j < fwd.size(); j++) {
            Date dateAux = new Date();
            try {
                dateAux = DateConverter.getDateFromString(fwd.get(j).getDate());
                //Log.i(LOG_TAG, "Date::" + dateAux.toString());
                DBTools.insertAuditLog(LOG_TAG, "fwd.get(j).getDate()::", dateAux.toString(), appCompatActivity);

            } catch (Exception ex) {
                Log.e(LOG_TAG, ex.toString());
                DBTools.insertAuditLog( LOG_TAG,
                                        ex.getClass().getSimpleName(),
                                        ex.getMessage(),
                                        appCompatActivity);

            }
            // MATLAB SRC CODE:: CSSObservation.m LINE 28
            // % compute the calculated fields
            // self.timestamp = datetime(year,month,day,hour,minute,0);
            // i.e.: timestamp = datetime(2018,08,08,10,11,0) // RESULT: 08-Aug-2018 10:11:00
            dFwd[k][0] = fwd.get(j).getReducedG(); // MATLAB::reduced_g  TO DO check it was dFwd[k][c]
            tFwdDate[k][0] = dateAux;              // MATLAB::timestamp
            tNames[k] = fwd.get(j).getCode();      // MATLAB::benchmark.name
            k++;
        }

        // REV BENCHMARKS
        k = 0;
        c = 1;
        String tRevNames[] = new String[rev.size()];//Never read (dont delete anyway)
        double dRev[][] = new double[rev.size()][c];
        Date tRevDate[][] = new Date[rev.size()][c]; // In MATLAB is tRev[][] for all the function

        for (int j = 0; j < rev.size(); j++) {
            Date dateAux = new Date();
            try {
                dateAux = DateConverter.getDateFromString(rev.get(j).getDate());
                //Log.i(LOG_TAG, "Date::" + dateAux.toString());
                DBTools.insertAuditLog( LOG_TAG,
                        "rev.get(j).getDate()::",
                        dateAux.toString(),
                        appCompatActivity);

            } catch (Exception ex) {
                Log.e(LOG_TAG, ex.toString());
                DBTools.insertAuditLog( LOG_TAG,
                        ex.getClass().getSimpleName(),
                        ex.toString(),
                        appCompatActivity);
            }
            dRev[k][0] = rev.get(j).getReducedG();
            tRevDate[k][0] = dateAux;
            tRevNames[k] = rev.get(j).getCode();
            k++;
        }

        Log.i(LOG_TAG, "********** IN:GET DELTAS-CLOSE LINE CALCULATION **********");

        //Check diff date, and hours

        // % build the difference vectors
        // % for the forward line:
        dFwd = LSMatJLab.diff(dFwd);                           // REDUCED_G
        double[][] tFwdDiffDate = LSMatJLab.diffDate(tFwdDate);// DIFF(DATE)
        double[][] tFwd = LSMatJLab.hours(tFwdDiffDate);       // MATLAB Hours()

        // % for the reverse line:
        dRev = LSMatJLab.diff(dRev);                           // REDUCED_G
        double[][] tRevDiffDate = LSMatJLab.diffDate(tRevDate);// DIFF(DATE)
        double[][] tRev = LSMatJLab.hours(tRevDiffDate);       // MATLAB Hours()

        /**
         * EJML::IN
         * SRC:: https://ejml.org/wiki/index.php?title=Equations
         *
         * MATLAB TO EJML GUIDE::
         * https://ejml.org/wiki/index.php?title=Matlab_to_EJML
         *
         */
        Equation eq = new Equation();
        LSEJMLEquationsCustomFunctions customEJMLFunctions = new LSEJMLEquationsCustomFunctions(eq);

        // matrix.length gives you the number of rows.
        // matrix[0].length gives you the number of columns (assuming all rows have the same length).
        SimpleMatrix tFwdSMatrix = new SimpleMatrix(tFwd.length, tFwd[0].length, DMatrixRMaj.class);//Date.class);
        SimpleMatrix dFwdSMatrix = new SimpleMatrix(dFwd.length, dFwd[0].length, DMatrixRMaj.class);
        SimpleMatrix tRevSMatrix = new SimpleMatrix(tRev.length, tRev[0].length, DMatrixRMaj.class);//Date.class);
        SimpleMatrix dRevSMatrix = new SimpleMatrix(dRev.length, dRev[0].length, DMatrixRMaj.class);
        SimpleMatrix A = new SimpleMatrix(dRev.length, dRev[0].length, DMatrixRMaj.class);

        tFwdSMatrix.set(new SimpleMatrix(tFwd));
        dFwdSMatrix.set(new SimpleMatrix(dFwd));
        tRevSMatrix.set(new SimpleMatrix(tRev));
        dRevSMatrix.set(new SimpleMatrix(dRev));

        // EJML::FRAMEWORK ALIAS DECLARATIONS
        eq.alias(tFwdSMatrix, "tfwd");
        eq.alias(dFwdSMatrix, "dfwd");
        eq.alias(tRevSMatrix, "trev");
        eq.alias(dRevSMatrix, "drev");
        eq.alias(A, "A");
        eq.alias(nDeltas, "ndeltas");

        //Log.i(LOG_TAG, "tfwd" + tFwdSMatrix.toString());
        DBTools.insertAuditLog( LOG_TAG, "tfwd",tFwdSMatrix.toString(),appCompatActivity);
        //Log.i(LOG_TAG, "dfwd" + dFwdSMatrix.toString());
        DBTools.insertAuditLog( LOG_TAG, "dfwd",dFwdSMatrix.toString(),appCompatActivity);
        //Log.i(LOG_TAG, "trev" + tRevSMatrix.toString());
        DBTools.insertAuditLog( LOG_TAG, "trev",tRevSMatrix.toString(),appCompatActivity);
        //Log.i(LOG_TAG, "drev" + dRevSMatrix.toString());
        DBTools.insertAuditLog( LOG_TAG, "drev",dRevSMatrix.toString(),appCompatActivity);

        // Log EQUATION
        Log.i(LOG_TAG, "******** LOG EQUATION STEPS::IN ********");

        // Equation instance, "", nbrRows, nbrCols
        ejmlLog(eq, "dfwd", dFwd.length, 1);
        ejmlLog(eq, "-tfwd", tFwd.length, 1);
        ejmlLog(eq, "drev", dRev.length, 1);
        ejmlLog(eq, "-trev", tRev.length, 1);
        ejmlLog(eq, "ones(size(tfwd,1),1)", tFwd.length, 1);
        ejmlLog(eq, "diag(ones(size(tfwd,1),1))", tFwd.length, tFwd.length);
        ejmlLog(eq, "ones(size(trev,1),1)", tRev.length, 1);
        ejmlLog(eq, "-diag(ones(size(trev,1),1))", tRev.length, tRev.length);
        ejmlLog(eq, "flipud(-diag(ones(size(trev,1),1)))", tRev.length, tRev.length);
        ejmlLog(eq, "-tfwd", tFwd.length, 1);
        ejmlLog(eq, "-trev", tRev.length, 1);

        /**
         * Construct MATLAB expression for result of A[ matrixA ; matrixB ]
         */
        Log.i(LOG_TAG, "******** CALCULATING (A) ::IN ********");

        // Concatenate expression::  -tfwd diag(ones(size(tfwd,1),1))
        DMatrixRMaj tFwdAux = new DMatrixRMaj(tFwd.length, tFwd[0].length);
        DMatrixRMaj tFwdDiagAux = new DMatrixRMaj(tFwd.length, tFwd[0].length);
        eq.alias(tFwdAux, "tFwdAux");
        eq.alias(tFwdDiagAux, "tFwdDiagAux");
        eq.process("tFwdAux = -tfwd");
        eq.process("tFwdDiagAux = diag(ones(size(tfwd,1),1))");
        tFwdAux = eq.lookupDDRM("tFwdAux");
        tFwdAux.print();
        DBTools.insertAuditLog(LOG_TAG, "tFwdAux.print()",tFwdAux.toString(), appCompatActivity);

        tFwdDiagAux = eq.lookupDDRM("tFwdDiagAux");
        tFwdDiagAux.print();
        DBTools.insertAuditLog(LOG_TAG, "tFwdDiagAux.print()",tFwdDiagAux.toString(), appCompatActivity);

        // - tFwd Concatenate Rows
        DMatrixRMaj tFwdMatrixResult = new DMatrixRMaj(tFwd.length, tFwd[0].length);//resizedMtrxRows, resizedMtrxCols);
        CommonOps_DDRM.concatColumns(tFwdAux, tFwdDiagAux, tFwdMatrixResult);
        tFwdMatrixResult.print();
        DBTools.insertAuditLog(LOG_TAG, "tFwdMatrixResult.print()",
                                         tFwdMatrixResult.toString(), appCompatActivity);

        // Concatenate expression::  -trev flipud(-diag(ones(size(trev,1),1)))
        DMatrixRMaj tRevAux = new DMatrixRMaj(tRev.length, tRev[0].length);
        DMatrixRMaj tRevFlipAux = new DMatrixRMaj(tRev.length, tRev[0].length);
        eq.alias(tRevAux, "tRevAux");
        eq.alias(tRevFlipAux, "tRevFlipAux");
        eq.process("tRevAux = -trev");
        eq.process("tRevFlipAux = flipud(-diag(ones(size(trev,1),1)))");
        tRevAux = eq.lookupDDRM("tRevAux");
        tRevFlipAux = eq.lookupDDRM("tRevFlipAux");
        DBTools.insertAuditLog(LOG_TAG, "tRevAux", tRevAux.toString(), appCompatActivity);
        DBTools.insertAuditLog(LOG_TAG, "tRevFlipAux", tRevFlipAux.toString(), appCompatActivity);

        // - tRev Concatenate Rows
        DMatrixRMaj tRevMatrixResult = new DMatrixRMaj(tRev.length, tFwd[0].length);//resizedMtrxRows, resizedMtrxCols);
        CommonOps_DDRM.concatColumns(tRevAux, tRevFlipAux, tRevMatrixResult);
        DBTools.insertAuditLog(LOG_TAG, "tRevMatrixResult", tRevMatrixResult.toString(), appCompatActivity);

        /**
         * Concatenate Columns:
         * % flipud in diag is used to match the order or the ida matrix
         * % therefore, results are expressed in ida order
         * [ A ; B ] is a Matrix composed of the matrix A concatenated with the matrix B
         */
        eq.alias(tFwdMatrixResult, "tFwdMatrixResult", tRevMatrixResult, "tRevMatrixResult");
        eq.process("A = [ tFwdMatrixResult ; tRevMatrixResult ]", true);

        /**
         * EQUATION 1::
         * % flipud in diag is used to match the order or the ida matrix
         * % therefore, results are expressed in ida order
         *
         * A = [  -tfwd diag(ones(size(tfwd,1),1)) ; -trev flipud(-diag(ones(size(trev,1),1)))  ]
         */
        //Log.i(LOG_TAG, "RESULT OF (A) IS:: ");
        DMatrixRMaj sMA = eq.lookupDDRM("A");
        sMA.print();
        DBTools.insertAuditLog(LOG_TAG, "RESULT OF (A) IS::", sMA.toString(), appCompatActivity);

        Log.i(LOG_TAG, "******** CALCULATING CONJUGATE TRANSPOSE:: A'= ********");

        // A' CALCULATION
        Log.i(LOG_TAG, "CALCULATING (A')....");
        SimpleMatrix ATransp = new SimpleMatrix(dRev.length, dRev[0].length, DMatrixRMaj.class);
        eq.alias(ATransp, "ATransp");
        eq.process("ATransp = A'", true);
        DMatrixRMaj sMAConjTransp = eq.lookupDDRM("ATransp");
        sMAConjTransp.print();
        DBTools.insertAuditLog(LOG_TAG, "sMAConjTransp", sMAConjTransp.toString(), appCompatActivity);

        // A'*A\A' CALCULATION
        Log.i(LOG_TAG, "CALCULATING (A'*A\\A') ......");

        /**
         * REUNION DEMIAN FEB 19
         * REFORMULAR DE LA SIGUIENTE MANERA:
         * (A'*A)  \\ (A' [ tFwdMatrixResult ; tRevMatrixResult ])
         */
        String fxAAA = "A'*A\\A'";

        eq.alias(new DMatrixRMaj(sMA.numRows, sMA.numCols), "expAAAResult");
        fxAAA = "expAAAResult =" + fxAAA;
        eq.process(fxAAA);
        DMatrixRMaj expAAAResult = eq.lookupDDRM("expAAAResult");
        expAAAResult.print();
        DBTools.insertAuditLog(LOG_TAG, "A'*A\\A'::", expAAAResult.toString(), appCompatActivity);


        /**
         * EQUATION 2:
         * x = A'*A\\A'*[ dfwd(:) ; drev(:) ]
         */
        Log.i(LOG_TAG, "***** CALCULATING x = A'*solve(A\\A')*[dfwd(:); drev(:)] ::IN ********");

        // Build one col matrix made of [dfwd(:); drev(:)]
        Log.i(LOG_TAG, "Building one column matrix made of [dfwd(:); drev(:)]");
        DMatrixRMaj dFwdDrevMatrix = new DMatrixRMaj((dFwd.length + dRev.length), 1);
        DMatrixRMaj a = new DMatrixRMaj(dFwd);
        DMatrixRMaj b = new DMatrixRMaj(dRev);
        CommonOps_DDRM.concatRows(a, b, dFwdDrevMatrix);
        dFwdDrevMatrix.print();
        DBTools.insertAuditLog( LOG_TAG, "one column matrix made of [dfwd(:); drev(:)]",
                                dFwdDrevMatrix.toString(), appCompatActivity);

        /**
         * FORMULA:  x = A'*A\\A'*[dfwd(:); drev(:)]
         * WILL BE:  x = expAAAResult * dFwdDrevMatrix
         * WHERE:        expAAAResult   IS  A'*A\\A'
         * AND:          dFwdDrevMatrix IS [dfwd(:); drev(:)]
         */
        DMatrixRMaj x = new DMatrixRMaj(sMA.numRows, sMA.numCols);
        eq.alias(x, "x", expAAAResult, "expAAAResult", dFwdDrevMatrix, "dFwdDrevMatrix");
        eq.process("x = expAAAResult * dFwdDrevMatrix", true);//check precedence of solve

        // Get result of x
        DMatrixRMaj sMX;
        sMX = eq.lookupDDRM("x");
        sMX.get(1);
        sMX.print();
        DBTools.insertAuditLog( LOG_TAG, "x :: ", sMX.toString(), appCompatActivity);

        // MATLAB:: % save the results in the structure
        // SAVE DRIFT RATE::
        Log.i(LOG_TAG, "SAVE::self.drifts(i,1) = x(1);");
        DBTools.insertAuditLog( LOG_TAG, "SAVE::", "self.drifts(i,1) = x(1);", appCompatActivity);
        line.setDriftRate(sMX.get(0, 0));

        // IMPROVEMENT-1:: TX
        TransactionalDBHelper tx = new TransactionalDBHelper(appCompatActivity, false);
        tx.beginTransaction();
        //tx = new GravityMobileDBHelper(appCompatActivity);
        try{
        tx.getGravityMobileDBHelper().updateLine(line);

        // MATLAB:: % save the deltas and residuals
        // SAVE DELTAS
        Log.i(LOG_TAG, "SAVE::self.deltas{i,1} = x(2:end);");
        DBTools.insertAuditLogTx( LOG_TAG, "SAVE::", "self.deltas{i,1} = x(2:end);", tx);
        for (int i = 1; i < sMX.getNumElements(); i++) {
            fwd.get(i).setDelta(sMX.get(i, 0));
            Point p = fwd.get(i);
            //db = new GravityMobileDBHelper(appCompatActivity);
            tx.getGravityMobileDBHelper().updatePoint(p);
        }

        /**
         * EQUATION 3:
         *
         * FORMULA:  v = [dfwd(:); drev(:)] - A*x
         * WILL BE:  v = dFwdDrevMatrix - A*x
         * WHERE:        dFwdDrevMatrix IS [dfwd(:); drev(:)]
         */
        Log.i(LOG_TAG, "***** CALCULATING v = [dfwd(:); drev(:)] - A*x ::IN ********");

        DMatrixRMaj v = new DMatrixRMaj(sMA.numRows, sMA.numCols);
        eq.alias(v, "v");
        //eq.process("v = [dfwd(:); drev(:)] - A*x");
        eq.process("v = dFwdDrevMatrix - A*x");
        DMatrixRMaj sMV = eq.lookupDDRM("v");
        sMV.print();
        DBTools.insertAuditLogTx( LOG_TAG, "SAVE::v = dFwdDrevMatrix - A*x",sMV.toString(), tx);

        // MATLAB:: % save the residuals of the forward direction only
        // SAVE RESIDUALS     self.residuals{i,1} = v(1:ndeltas);
        for (int i = 0; i < fwd.size(); i++) {
            // Get calculated residual for actual POINT
            double residualAux = sMV.get(i, 0);
            fwd.get(i).setResidual(residualAux);
            DBTools.insertAuditLogTx( LOG_TAG, "fwd.get(i).setResidual::",String.valueOf(residualAux), tx);

            /**
             * CLOSE LINE CONDITION:
             * Each POINT of the LINE must complain:
             * 0.3 > residual > -0.3
             */
            if (residualAux < 0.3 && residualAux > -0.3) {
                // POINT residual is NORMAL
                fwd.get(i).setStatus(PointStatusInterface.POINT_STATUS_REGISTERED);
                DBTools.insertAuditLogTx( LOG_TAG, "fwd.get(i).setStatus::",PointStatusInterface.POINT_STATUS_REGISTERED, tx);
            } else {
                // Almost one POINT residual does not complain with
                // 0.3 > residual > -0.3 LINE couldn't be closed
                fwd.get(i).setStatus(PointStatusInterface.POINT_STATUS_ANOMALY);
                DBTools.insertAuditLogTx( LOG_TAG, "fwd.get(i).setStatus::",PointStatusInterface.POINT_STATUS_ANOMALY, tx);
            }

            // PERSIST POINT updates
            Point p = fwd.get(i);
            // IMPROVEMENT-1:: TX
            //db = new GravityMobileDBHelper(appCompatActivity);

            // % apply the offset correction
            // self.reduced_g = self.reduced_g + benchmark.offset.*0.3086;
            p.setReducedG(p.getReducedG() + p.getOffset() * 0.3086);
            DBTools.insertAuditLogTx( LOG_TAG, "REDUCED G::",String.valueOf(p.getReducedG()), tx);

            // UPDATE CHANGES AT THE END
            // IMPROVEMENT-1:: TX
            tx.getGravityMobileDBHelper().updatePoint(p);

        }

            // IMPROVEMENT-1:: TX
            tx.setTransactionSuccessful();
        }catch (Exception e1){
            Log.e(LOG_TAG, e1.getMessage());
        }finally {
            try{
            tx.endTransaction();
            tx.getDatabase().close();
            }catch (Exception e2){Log.e(LOG_TAG, e2.getMessage());}
        }

        // CLOSE LINE CONDITION
        return closeLine(fwd);

        // MATLAB SRC:: CssGravityLine.processReobservedData()
        //-------------
        // Log4G will not reprocess REOBSERVED DATA it just process all the benchmarks
        // again when user tries to run close line after corrections were performed.

        // self.compute_reduced_g(instrument.calibration);
        // LOG4G IS CALCULATING AND RECORDING REDUCED-G IN EVERY POINT's SAVE OPERATION
        //
        // CssObservation cssObservation = new CssObservation();
        // cssObservation.computeReducedG(null,p);

    }// END getDeltas

    /**
     * CLOSE LINE CONDITION CHECK
     * If one POINT are in ANOMALY status, LINE could not be closed,
     * if not, LINE will be closeable
     *
     * @param fwd
     * @return
     */
    private boolean closeLine(List<Point> fwd) {
        boolean result = true;
        for (int i = 0; i < fwd.size(); i++) {
            if (PointStatusInterface.POINT_STATUS_ANOMALY.equals(fwd.get(i).getStatus())) {
                DBTools.insertAuditLog( LOG_TAG, "REDUCED G::",
                        PointStatusInterface.POINT_STATUS_ANOMALY, appCompatActivity);
                result = false;
            }
        }
        return result;
    }

    /**
     * EJML MATLAB Equations Logger
     *
     * @param eq:       Instance of the equation executing
     * @param fx:       The fx to be logged
     * @param rowsSize: Row size of resulting matrix
     * @param colsSize: Col size of resulting matrix
     */
    private void ejmlLog(Equation eq, String fx, int rowsSize, int colsSize) {
        Log.i(LOG_TAG, fx);
        eq.alias(new DMatrixRMaj(rowsSize, colsSize), "auxMatrix");
        fx = "auxMatrix =" + fx;
        eq.process(fx);
        DMatrixRMaj auxMatrix = eq.lookupDDRM("auxMatrix");
        auxMatrix.print();
        DBTools.insertAuditLog(LOG_TAG, fx,auxMatrix.toString(), appCompatActivity);

    }

}//End-class-CssGravityLine

//---------------------- ---------------------- ---------------------- //
//---------------------- ---------------------- ---------------------- //
//----------------- EJML MATLAB TO JAVA FUNCTIONS: IN ---------------- //
//---------------------- ---------------------- ---------------------- //
//---------------------- ---------------------- ---------------------- //


//-------------------------------- LOCAL CLASS::IN ------------------- //
/**
 * rs(diff(X))
 * X = magic(4);
 * H = hours(diff(X))
 * H = 4x4 duration
 * 16hs     2hs    3hs     13hs
 * 5 hs    11hs   10hs     8hs
 * 9 hs     7hs    6hs    12hs
 * 4 hs    14hs   15hs     1hs
 * <p>
 * MATLAB::
 * H = [datetime(2018,08,08,10,11,0),datetime(2018,09,09,11,11,0),datetime(2018,10,10,10,10,0);
 * datetime(2019,09,09,11,11,0),datetime(2019,10,10,10,10,0),datetime(2019,11,11,11,11,0)]
 * <p>
 * diff(H)
 * RES: see C:\Users\pc-pc\Desktop\5thElmt\000-LATERAL-SEARCH\000-PROJECT-GRAVITY\
 * CSSGRAVITYLINE-GETDELTAS\timestampvsdiff.jpg
 */

/**
 * Demonstration on how to create and use a custom function in Equation.
 * A custom function must implement ManagerFunctions.Input1 or ManagerFunctions.InputN,
 * depending on the number of inputs it takes.
 *
 * @author Lateral Search - Andres Hernan Pityla C.
 * <p>
 * SRC: https://ejml.org/wiki/index.php?title=Equations
 * SRC: http://ejml.org/javadoc/
 * SRC: http://ejml.org/javadoc/index.html?org/ejml/dense/row/MatrixFeatures_DDRM.html
 */
class LSEJMLEquationsCustomFunctions {

    private static final String LOG_TAG = LSEJMLEquationsCustomFunctions.class.getSimpleName();

    // Constructor
    public LSEJMLEquationsCustomFunctions(Equation eq) {
        eq.getFunctions().addN("size", size());
        eq.getFunctions().addN("repmat", repmat());
        eq.getFunctions().add1("flipud", flipud());
    }

    /**
     * EJML FRAMEWORK CUSTOM METHODS FOR MATLAB::
     * <p>
     * EXAMPLES:: SEE Operation.class
     * <p>
     * TOOLS::    CommonOps_XXXX are utilities to be used with matrices, there are many
     * just write CommonOps_ and see
     * <p>
     * IN MATLAB::
     * <p>
     * M = SIZE(X,DIM) returns the length of the dimension specified
     * by the scalar DIM.  For example, SIZE(X,1) returns the number
     * of rows. If DIM > NDIMS(X), M will be 1.
     * <p>
     * IN JAVA::
     * <p>
     * int[][] foo = new int[][] {
     * new int[] { 1, 2, 3 },
     * new int[] { 1, 2, 3, 4},
     * };
     * <p>
     * System.out.println(foo.length); //2
     * System.out.println(foo[0].length); //3
     * System.out.println(foo[1].length); //4
     */
    public static ManagerFunctions.InputN size() {
        return new ManagerFunctions.InputN() {
            @Override
            public Operation.Info create(List<Variable> inputs, ManagerTempVariables manager) {
                if (inputs.size() != 2)
                    throw new RuntimeException("Two inputs required for size()");

                // MULTIPLE INPUT::
                final Variable varA = inputs.get(0);// Matrix
                final Variable varB = inputs.get(1);// DIM to be sized

                // OUTPUT:: RETURN VALUE (RESULT)
                Operation.Info ret = new Operation.Info();

                if (varA instanceof VariableMatrix && varB instanceof VariableInteger) {

                    // The output matrix or scalar variable must be created with the provided manager
                    final VariableInteger output = manager.createInteger();
                    ret.output = output;
                    ret.op = new Operation("size") {
                        @Override
                        public void process() {
                            // Matrix to be sized
                            DMatrixRMaj mA = ((VariableMatrix) varA).matrix;
                            // Result to return
                            output.value = mA.getNumRows();

                            //Log.i(LOG_TAG, "size:" + mA.getNumRows());
                            // In the MATLAB app they always use one dimension
                        }
                    };
                } else {
                    throw new IllegalArgumentException("Expected both inputs to be a matrix");
                }
                return ret;
            }
        };
    }

    /**
     * MATLAB repmat() : Repeat copies of array
     * <p>
     * repmat( flipud(-diag(ones(ndeltas,1))),
     * size(trev,2),
     * 1);
     * <p>
     * Syntax
     * B = repmat(A,n)
     * B = repmat(A,r1,...,rN)
     * B = repmat(A,r)
     * <p>
     * EXAMPLE:
     * <p>
     * Repeat copies of a matrix into a 2-by-3 block arrangement.
     * <p>
     * A = diag([100 200 300])
     * <p>
     * A = 3×3
     * <p>
     * 100     0     0
     * 0   200     0
     * 0     0   300
     * <p>
     * APPLY repmat()
     * <p>
     * B = repmat(A,2,3)
     * <p>
     * It means repeat the matrix A, 2 times vertically, 3 times horizontally
     * <p>
     * <p>
     * B = 6×9
     * <p>
     * 100     0     0   100     0     0   100     0     0
     * 0   200     0     0   200     0     0   200     0
     * 0     0   300     0     0   300     0     0   300
     * 100     0     0   100     0     0   100     0     0
     * 0   200     0     0   200     0     0   200     0
     * 0     0   300     0     0   300     0     0   300
     *
     * @return
     */
    public static ManagerFunctions.InputN repmat() {
        return new ManagerFunctions.InputN() {
            @Override
            public Operation.Info create(List<Variable> inputs, ManagerTempVariables manager) {
                if (inputs.size() != 3)
                    throw new RuntimeException("Three inputs required");

                // INPUT::
                final Variable varA = inputs.get(0);
                final Variable varB = inputs.get(1);
                final Variable varC = inputs.get(2);

                // OUTPUT:: RETURN VALUE (RESULT)
                Operation.Info ret = new Operation.Info();

                if (varA instanceof VariableMatrix
                        && varB instanceof VariableInteger
                        && varC instanceof VariableInteger) {
                    // The output matrix or scalar variable must be created with the provided manager
                    final VariableMatrix output = manager.createMatrix();
                    ret.output = output;
                    ret.op = new Operation("repmat-mm") {
                        @Override
                        public void process() {
                            DMatrixRMaj mA = ((VariableMatrix) varA).matrix;
                            int verticalRepeats = ((VariableInteger) varB).value;
                            int horizontalRepeats = ((VariableInteger) varC).value;

                            DMatrixRMaj resultMatrix = new DMatrixRMaj();//resizedMtrxRows, resizedMtrxCols);

                            // Horizontal repmat()
                            for (int i = 0; i < horizontalRepeats; i++) {
                                CommonOps_DDRM.concatRows(resultMatrix, mA, resultMatrix);
                            }
                            // Vertical repmat()
                            // Aux matrix to store last matrix, we need to repeat
                            DMatrixRMaj auxMatrix = new DMatrixRMaj(resultMatrix);
                            for (int j = 0; j < verticalRepeats; j++) {
                                CommonOps_DDRM.concatColumns(resultMatrix, auxMatrix, resultMatrix);
                            }

                            output.matrix = resultMatrix;
                        }
                    };
                } else {
                    throw new IllegalArgumentException("Expected both inputs to be a matrix");
                }

                return ret;
            }
        };
    }

    /**
     * MATLAB flipud() ( meaning FLIP-UP-DOWN ) FUNCTION IMPLEMENTATION:
     * <p>
     * flipud(): FLIPS THE MATRIX
     * <p>
     * EXAMPLE:
     * <p>
     * Given the following Matrix: -diag(ones(size(trev,1),1))
     * <p>
     * -1     0     0     0     0
     * 0    -1     0     0     0
     * 0     0    -1     0     0
     * 0     0     0    -1     0
     * 0     0     0     0    -1
     * <p>
     * <p>
     * <p>
     * Return the resulting Matrix of a flipud() opearation:
     * <p>
     * This is: flipud(-diag(ones(size(trev,1),1)))
     * <p>
     * 0     0     0     0    -1
     * 0     0     0    -1     0
     * 0     0    -1     0     0
     * 0    -1     0     0     0
     * -1     0     0     0     0
     */
    public static ManagerFunctions.Input1 flipud() {
        return new ManagerFunctions.Input1() {
            @Override
            public Operation.Info create(final Variable varA, ManagerTempVariables manager) {
                // OUTPUT:: RETURN VALUE (RESULT)
                Operation.Info ret = new Operation.Info();

                if (varA instanceof VariableMatrix) {
                    // The output matrix or scalar variable must be created with the provided manager
                    final VariableMatrix output = manager.createMatrix();
                    ret.output = output;
                    ret.op = new Operation("flipud") {
                        @Override
                        public void process() {
                            DMatrixRMaj mA = ((VariableMatrix) varA).matrix;
                            DMatrixRMaj outMat = new DMatrixRMaj(mA.getNumRows(), mA.getNumCols());

                            int[] flipOrder = new int[mA.getNumRows()];
                            for (int i = mA.getNumRows(); i > 0; i--) { //
                                flipOrder[mA.getNumRows() - i] = i - 1;//i-1 cause must be x.length to zero
                            }
                            // permuteRowInv(int[] pinv, DMatrixRMaj input, DMatrixRMaj output)
                            // Applies the row permutation specified by the vector to the input matrix
                            // and save the results in the output matrix.
                            DMatrixRMaj dMatrixResult = CommonOps_DDRM.permuteRowInv(flipOrder, mA, null);
                            output.matrix = dMatrixResult;
                        }
                    };
                } else {
                    throw new IllegalArgumentException("Parameter must be a matrix.");
                }
                return ret;
            }
        };
    }

    /**
     * EXAMPLE OF :: Create the function.
     * <p>
     * Be sure to handle all possible input types and combinations
     * correctly and provide
     * meaningful error messages.
     * The output matrix should be resized to fit the inputs.
     */
    public static ManagerFunctions.InputN createMultTransA() {
        return new ManagerFunctions.InputN() {
            @Override
            public Operation.Info create(List<Variable> inputs, ManagerTempVariables manager) {
                if (inputs.size() != 2)
                    throw new RuntimeException("Two inputs required");

                final Variable varA = inputs.get(0);
                final Variable varB = inputs.get(1);

                Operation.Info ret = new Operation.Info();

                if (varA instanceof VariableMatrix && varB instanceof VariableMatrix) {
                    // The output matrix or scalar variable must be created with the provided manager
                    final VariableMatrix output = manager.createMatrix();
                    ret.output = output;
                    ret.op = new Operation("multTransA-mm") {
                        @Override
                        public void process() {
                            DMatrixRMaj mA = ((VariableMatrix) varA).matrix;
                            DMatrixRMaj mB = ((VariableMatrix) varB).matrix;

                            output.matrix.reshape(mA.numCols, mB.numCols);
                            CommonOps_DDRM.multTransA(mA, mB, output.matrix);
                        }
                    };
                } else {
                    throw new IllegalArgumentException("Expected both inputs to be a matrix");
                }
                return ret;
            }
        };
    }

}
//-------------------------------------------------------------------- //
//-------------------------------- LOCAL CLASS::OUT ------------------ //
//-------------------------------------------------------------------- //


//---------------------- ---------------------- ---------------------- //
//---------------------- ---------------------- ---------------------- //
//----------------- EJML MATLAB TO JAVA FUNCTIONS: OUT-----------------//
//---------------------- ---------------------- ---------------------- //
//---------------------- ---------------------- ---------------------- //


// UNUSED CODE maybe FUTURE IMPL::IN
//            int k = 1;
//            int c = 1;
//            String tNames[] = {};
//            double dFwd[][] = new double[fwd.size()][c]; // check array structure
//            Date tFwd[][] = new Date[fwd.size()][c];
//            for (int j = 1; j < fwd.size(); j++) {
//                Date dateAux = new Date();
//                try {
//                    dateAux = DateConverter.getDateFromString(fwd.get(j).getDate());
//                } catch (Exception ex) {
//                    Log.e(LOG_TAG, ex.toString());
//                }
//                // MATLAB SRC CODE:: CSSObservation.m LINE 28
//                // % compute the calculated fields
//                // self.timestamp = datetime(year,month,day,hour,minute,0);
//                // i.e.: timestamp = datetime(2018,08,08,10,11,0) // RESULT: 08-Aug-2018 10:11:00
//                dFwd[k][c] = fwd.get(j).getReducedG();// MATLAB::reduced_g
//                tFwd[k][c] = dateAux;                 // MATLAB::timestamp
//                tNames[k] = fwd.get(j).getCode();      // MATLAB::benchmark.name
//                k++;
//            }
//            // % do the same for the reverse
//            k = 1;
//            c = 1;
//            tNames = null;
//            double dRev[][] = new double[rev.size()][c]; // check array structure
//            Date tRev[][] = new Date[rev.size()][c];
//            for (int j = 1; j < rev.size(); j++) {
//                Date dateAux = new Date();
//                try {
//                    dateAux = DateConverter.getDateFromString(rev.get(j).getDate());
//                } catch (Exception ex) {
//                    Log.e(LOG_TAG, ex.toString());
//                }
//                dRev[k][c] = rev.get(j).getReducedG();
//                tRev[k][c] = dateAux;
//                tNames[k] = rev.get(j).getCode();
//                k++;
//            }
//            // % build the difference vectors
//            // % for the forward line
//            dFwd = LSMatJLab.diff(dFwd);
//            tFwd = LSMatJLab.hours(LSMatJLab.diffDate(tFwd));
//            // % for the reverse line
//            dRev = LSMatJLab.diff(dRev);
//            tRev = LSMatJLab.hours(LSMatJLab.diffDate(tRev));
// UNUSED CODE 4 FUTURE IMPL::OUT

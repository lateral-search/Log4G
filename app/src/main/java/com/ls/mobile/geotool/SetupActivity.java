package com.ls.mobile.geotool;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ls.mobile.geotool.db.GravityMobileDBHelper;
import com.ls.mobile.geotool.db.data.model.Gravimeter;
import com.ls.mobile.geotool.db.data.model.User;

import java.util.List;

/**
 * This class contains the logic related to UI of user and gravimeter
 * selection.
 *
 * @MemLeaks Q&A: 0k   [NOTE: OnItemSelectedListener may be a named STATIC
 *                      inner CLASS, but this is just an small detail
 *                      given this class is not a big deal, and never had
 *                      any leak, performance problem, etc]
 *
 * @author lateralSearch
 *
 */
public class SetupActivity extends AppCompatActivity {

    Spinner spinner;
    Spinner operatorsSpinner;
    TextView opLbl; // users spinner label
    EditText newOpTxtInput;
    TextView newOpLbl;
    Button exitBtn;
    Button okBtn;

    //Log
    private static final String LOG_TAG = SetupActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        operatorsSpinner = (Spinner) findViewById(R.id.operatorsSpinner);
        opLbl = (TextView)findViewById(R.id.opLbl);
        spinner = (Spinner) findViewById(R.id.spinner);

        newOpLbl = (TextView)findViewById(R.id.newOpLbl);
        newOpTxtInput = (EditText) findViewById(R.id.newOpTxtInput);
        exitBtn = (Button) findViewById(R.id.exitBtn);
        okBtn = (Button) findViewById(R.id.okBtn);


        newOpTxtInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT);
        // MAX. CHAR LENGTH: 12
        newOpTxtInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        newOpTxtInput.setHint(getString(R.string.setup_act_user));


        // GRAVIMETERS SPINNER
        spinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    /**
                     * <p>Callback method to be invoked when an item in this view has been
                     * selected. This callback is invoked only when the newly selected
                     * position is different from the previously selected position or if
                     * there was no selected item.</p>
                     * <p>
                     * Impelmenters can call getItemAtPosition(position) if they need to access the
                     * data associated with the selected item.
                     *
                     * @param parent   The AdapterView where the selection happened
                     * @param view     The view within the AdapterView that was clicked
                     * @param position The position of the view in the adapter
                     * @param id       The row id of the item that is selected
                     */
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // On selecting a spinner item
                        String label = parent.getItemAtPosition(position).toString();
                        // Showing selected spinner item
                        Toast.makeText(parent.getContext(), "Id selected: " + label,
                                Toast.LENGTH_SHORT).show();
                    }

                    /**
                     * Callback method to be invoked when the selection disappears from this
                     * view. The selection can disappear for instance when touch is activated
                     * or when the adapter becomes empty.
                     *
                     * @param parent The AdapterView that now contains no selected item.
                     */
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // DO NOTHING
                    }
                });

        // USERS SPINNER
        operatorsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                String label = parent.getItemAtPosition(position).toString();
                // Showing selected spinner item
                Toast.makeText(parent.getContext(), "Id selected: " + label,
                        Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // DO NOTHING
            }
        });

        // LOAD DATA ON SPINNERS
        loadSpinnerData();
        loadOperatorSpinnerData();

        // 0kBtn Listener
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                goToLineSelectionActivity(null);
            }
        });

        // exitBtn Listener
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                goToSyncActivity(null);
            }
        });
    }

    /**
     * Load the spinner data from SQLite database
     */
    private void loadOperatorSpinnerData() {
        // database handler
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        // Spinner Drop down elements
        List<String> user = db.getAllUserNames();
        // First element of the list.
        user.add(0, "SELECT");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, user);
        // Drop down layout style - list view with radio button
        dataAdapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        operatorsSpinner.setAdapter(dataAdapter);
    }

    /**
     * Load the spinner data from SQLite database
     */
    private void loadSpinnerData() {
        // database handler
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        // Spinner Drop down elements
        List<String> gravimeter = db.getAllGravimetersNames();
        // First element of the list.
        gravimeter.add(0, "SELECT");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, gravimeter);
        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    // GoTo previous screen.
    public void goToSyncActivity(View view) {
        Log.d(LOG_TAG, "Going to SyncActivity!");
        // ISSUE MEMORY-LEAKS
        Intent intent = new Intent(getApplicationContext(), SyncActivity.class);
        startActivity(intent);
    }

    // GoTo next screen.
    public void goToLineSelectionActivity(View view) {
        Log.d(LOG_TAG, "Going to LineSelectionActivity!");

        if (newOpTxtInput.getText().toString().trim().length() > 1) {
            String uInputStr = newOpTxtInput.getText().toString();
                if ((uInputStr.length() < 6)) {
                    String msg = getString(R.string.setup_act_user_12_in);
                    inputErrorAlert(msg + getString(R.string.setup_act_codigo_u));

                } else if (uInputStr.replaceAll("[^a-z0-9]", "").length() !=  uInputStr.trim().length()) {// "[^a-zA-Z0-9]"
                    String msg = getString(R.string.setup_act_just_az_09);
                    inputErrorAlert(msg + getString(R.string.setup_act_codigo_u));

                } else {
                    // SAVE the user
                    GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
                    long result = db.createUser(newOpTxtInput.getText().toString().trim());
                    if(result == -1){
                        Toast.makeText(getApplicationContext(), R.string.setup_activity_u_creation_err,
                                Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), R.string.setup_activity_u_created +
                                        newOpTxtInput.getText().toString().trim(),
                                Toast.LENGTH_LONG).show();
                        // REFRESH SCREEN (forward to itself) to see changes on db
                        // ISSUE MEMORY-LEAKS
                        Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                        startActivity(intent);
                    }

                }
        }

        // VALIDATIONS
        if(spinner.getSelectedItemId() != 0 &&
                operatorsSpinner.getSelectedItemId() != 0){
            // USER
            GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
            //String userName = editText.getText().toString().trim();
            String userName = operatorsSpinner.getSelectedItem().toString();
            User user = db.getUserByName(userName);

            if (user == null) {// ISSUE MEMORY-LEAKS: (this
                Toast.makeText(getApplicationContext(), R.string.setup_act_user_err,
                        Toast.LENGTH_LONG).show();

            } else {
                // GRAVIMETER
                // Get Gravimeter data to get ID
                db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
                String s = spinner.getSelectedItem().toString();
                Gravimeter g = db.getGravimeterByName(s);

                // Session Values.
                // 0 - for private mode
                SharedPreferences pref =
                        getApplicationContext().getSharedPreferences("MyPref", 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(SharedDataKeyInterface.SETUP_ACTIVITY_USER_ID_INT
                        , user.getmId());
                editor.putString(SharedDataKeyInterface.SETUP_ACTIVITY_USER_NAME_STR
                        , user.getName());
                editor.putInt(SharedDataKeyInterface.SETUP_ACTIVITY_GRAVIMETER_ID_INT
                        , g.getmId());
                editor.putString(SharedDataKeyInterface.SETUP_ACTIVITY_GRAVIMETER_NAME_STR
                        , spinner.getSelectedItem().toString());
                editor.commit();

                // ISSUE MEMORY-LEAKS
                Intent intent = new Intent(getApplicationContext(), LineSelectionActivity.class);
                startActivity(intent);
            }
        }else if (operatorsSpinner.getSelectedItemId() == 0 && newOpTxtInput.getText().length() < 1) {
            inputErrorAlert(getString(R.string.setup_act_user_spin));

        } else if (spinner.getSelectedItemId() == 0) {
            inputErrorAlert(getString(R.string.setup_activity_grav_err));

        } else  {// ISSUE MEMORY-LEAKS
            Toast.makeText(getApplicationContext(), R.string.setup_activity_NA_err,
                    Toast.LENGTH_LONG).show();

        }
    }


    /**
     * ERROR ALERT
     * @param errorMessage
     */
    private void inputErrorAlert(String errorMessage) {
        Log.i(LOG_TAG, "Click!, User Input Error");
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SetupActivity.this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(errorMessage);
        alertBuilder.setPositiveButton(R.string.ok, new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }

    private static class MyOnClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            // DO NOTHING
        }
    }
}
/* @file DistoXPlotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid new-plot dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;


import android.app.Dialog;
import android.os.Bundle;

// import android.util.Log;

import android.content.Context;

import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.View;

import android.widget.Toast;

public class DistoXPlotDialog extends Dialog
                              implements View.OnClickListener
{
  // private static final String DPTAG = "DistoX PlotDialog";
  private DistoX mDistoX;
  private boolean notDone;

  private EditText mEditName;
  private EditText mEditStart;
  private EditText mEditView;
  private RadioButton myBtnPlan;
  private RadioButton myBtnExtended;
  private RadioButton myBtnVSection;
  private RadioButton myBtnHSection;
  private RadioGroup  myBtns;

  private Button   mBtnOK;
  // private Button   mBtnBack;
  private Button   mBtnCancel;

  public DistoXPlotDialog( Context context, DistoX distox )
  {
    super( context );
    mDistoX = distox;
    notDone = true;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.distox_plot_dialog);
    mEditName  = (EditText) findViewById(R.id.edit_plot_name);
    mEditStart = (EditText) findViewById(R.id.edit_plot_start);
    mEditView  = (EditText) findViewById(R.id.edit_plot_view);

    myBtnPlan   = (RadioButton) findViewById( R.id.btn_plot_plan );
    myBtnExtended    = (RadioButton) findViewById( R.id.btn_plot_ext );
    myBtnVSection = (RadioButton) findViewById( R.id.btn_plot_vcross );
    myBtnHSection = (RadioButton) findViewById( R.id.btn_plot_hcross );

    mEditName.setHint(  "scrap name" );
    mEditStart.setHint( "base station" );
    mEditView.setHint(  "viewed station" );
    myBtnPlan.setChecked( true ); // default is plan

    mBtnOK = (Button) findViewById(R.id.button_ok_plot_name );
    mBtnOK.setOnClickListener( this );
    // mBtnBack = (Button) findViewById(R.id.button_back_plot_name );
    // mBtnBack.setOnClickListener( this );
    mBtnCancel = (Button) findViewById(R.id.button_cancel_plot_name );
    mBtnCancel.setOnClickListener( this );
  }

  // FIXME synchronized ?
  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    if ( notDone && b == mBtnOK ) {
      notDone = false;
      String name  = mEditName.getText().toString();
      String start = mEditStart.getText().toString();
      String view  = mEditView.getText().toString();
      if ( name == null || name.length() == 0 ) {
        Toast.makeText( mDistoX, R.string.plot_null_name, Toast.LENGTH_LONG ).show();
        // setResult( RESULT_CANCELED );
      } else if ( start == null || start.length() == 0 ) {
        Toast.makeText( mDistoX, R.string.plot_null_start, Toast.LENGTH_LONG ).show();
        // setResult( RESULT_CANCELED );
      } else {
        // result.setAction( name );
        long type = TopoDroidApp.PLOT_PLAN;
        if ( myBtnPlan.isChecked() )          { type = TopoDroidApp.PLOT_PLAN; }
        else if ( myBtnExtended.isChecked() ) { type = TopoDroidApp.PLOT_EXTENDED; }
        else if ( myBtnVSection.isChecked() ) { type = TopoDroidApp.PLOT_V_SECTION; }
        else if ( myBtnHSection.isChecked() ) { type = TopoDroidApp.PLOT_H_SECTION; }
        // result.putExtra( TopoDroidApp.TOPODROID_PLOT_NAME, name ); 
        // result.putExtra( TopoDroidApp.TOPODROID_PLOT_TYPE, type ); 
        // result.putExtra( TopoDroidApp.TOPODROID_PLOT_STRT, start ); 
        // result.putExtra( TopoDroidApp.TOPODROID_PLOT_VIEW, view ); 
        // setResult( Activity.RESULT_OK, result );
        mDistoX.makeNewPlot( name, type, start, view );
      }
    // } else if ( b == mBtnBack ) {
      // setResult( Activity.RESULT_FIRST_USER );
    // } else {
      // setResult( RESULT_CANCELED );
    }
    // finish();
    dismiss();
  }

}


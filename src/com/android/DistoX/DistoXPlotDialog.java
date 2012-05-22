/* @file DistoXPlotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid new-plot dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120521 using INewPlot interface for the maker
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
  private Context mContext;
  private INewPlot mMaker;
  private boolean notDone;

  private EditText mEditName;
  private EditText mEditStart;
  private EditText mEditView;
  private RadioButton mBtnPlan;
  private RadioButton mBtnExtended;
  private RadioButton mBtnVSection;
  private RadioButton mBtnHSection;
  // private RadioGroup  mBtns;

  private Button   mBtnOK;
  // private Button   mBtnBack;
  private Button   mBtnCancel;

  public DistoXPlotDialog( Context context, INewPlot maker )
  {
    super( context );
    mContext = context;
    mMaker  = maker;
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

    mBtnPlan   = (RadioButton) findViewById( R.id.btn_plot_plan );
    mBtnExtended    = (RadioButton) findViewById( R.id.btn_plot_ext );
    mBtnVSection = (RadioButton) findViewById( R.id.btn_plot_vcross );
    mBtnHSection = (RadioButton) findViewById( R.id.btn_plot_hcross );

    mEditName.setHint(  "scrap name" );
    mEditStart.setHint( "base station" );
    mEditView.setHint(  "viewed station" );
    mBtnPlan.setChecked( true ); // default is plan

    mBtnOK = (Button) findViewById(R.id.button_ok_plot_name );
    mBtnOK.setOnClickListener( this );
    mBtnCancel = (Button) findViewById(R.id.button_cancel_plot_name );
    mBtnCancel.setOnClickListener( this );
  }

  // FIXME synchronized ?
  @Override
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
        Toast.makeText( mContext, R.string.plot_null_name, Toast.LENGTH_LONG ).show();
      } else if ( start == null || start.length() == 0 ) {
        Toast.makeText( mContext, R.string.plot_null_start, Toast.LENGTH_LONG ).show();
      } else {
        long type = TopoDroidApp.PLOT_PLAN;
        if ( mBtnPlan.isChecked() )          { type = TopoDroidApp.PLOT_PLAN; }
        else if ( mBtnExtended.isChecked() ) { type = TopoDroidApp.PLOT_EXTENDED; }
        else if ( mBtnVSection.isChecked() ) { type = TopoDroidApp.PLOT_V_SECTION; }
        else if ( mBtnHSection.isChecked() ) { type = TopoDroidApp.PLOT_H_SECTION; }
        mMaker.makeNewPlot( name, type, start, view );
      }
    }
    // finish();
    dismiss();
  }

}


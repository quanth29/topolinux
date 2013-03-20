/** @file SurveyExportDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey export dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130213 created
 */
package com.android.DistoX;

import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;

import android.content.Context;

import android.widget.Button;
import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;


public class SurveyExportDialog extends Dialog
                                implements View.OnClickListener
{
  private Button   mBtnTh;
  private Button   mBtnSvx;
  private Button   mBtnDat;
  private Button   mBtnTro;
  private Button   mBtnZip;
  private Button   mBtnCancel;

  private SurveyActivity mParent;

  SurveyExportDialog( Context context, SurveyActivity parent )
  {
    super( context );
    mParent = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.export_dialog);
    mBtnTh  = (Button) findViewById(R.id.btn_therion );
    mBtnSvx = (Button) findViewById(R.id.btn_survex );
    mBtnDat = (Button) findViewById(R.id.btn_compass );
    mBtnTro = (Button) findViewById(R.id.btn_vtopo );
    mBtnZip = (Button) findViewById(R.id.btn_zip );
    mBtnCancel = (Button) findViewById(R.id.btn_cancel );

    // Bundle extras = getIntent().getExtras();
    // String title  = extras.getString( TopoDroidApp.TOPODROID_SURVEY );

    mBtnTh.setOnClickListener( this );
    mBtnSvx.setOnClickListener( this );
    mBtnDat.setOnClickListener( this );
    mBtnTro.setOnClickListener( this );
    mBtnZip.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );

    setTitle( R.string.title_survey_export );
  }

  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    if ( b == mBtnTh ) {
      mParent.doExport( TopoDroidApp.DISTOX_EXPORT_TH, true );
    } else if ( b == mBtnSvx ) {
      mParent.doExport( TopoDroidApp.DISTOX_EXPORT_SVX, true );
    } else if ( b == mBtnDat ) {
      mParent.doExport( TopoDroidApp.DISTOX_EXPORT_DAT, true );
    } else if ( b == mBtnTro ) {
      mParent.doExport( TopoDroidApp.DISTOX_EXPORT_TRO, true );
    } else if ( b == mBtnZip ) {
      mParent.doArchive( );
    // } else {
      // setResult( RESULT_CANCELED );
    }
    dismiss();
  }

}



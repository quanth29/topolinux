/** @file SketchEditDialog.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid sketch 3D edit dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130326 created
 */
package com.android.DistoX;

import java.util.ArrayList;

import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;

import android.content.Context;

import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import android.view.View;


public class SketchEditDialog extends Dialog
                              implements View.OnClickListener
{
  // private Button   mBtnOk;
  private Button mBtnCancel;
  private Button mBtnErase;
  private Button mBtnExtrude;
  private Button mBtnStretch;
  private Button mBtnCut;

  private SketchActivity mParent;
  // private TopoDroidApp   mApp;

  SketchEditDialog( Context context, SketchActivity parent )
  {
    super( context );
    mParent = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.sketch_edit_dialog);
    // mBtnOk     = (Button) findViewById(R.id.btn_ok );
    mBtnCancel = (Button) findViewById(R.id.btn_cancel );
    mBtnErase   = (Button) findViewById(R.id.rb_erase );
    mBtnExtrude = (Button) findViewById(R.id.rb_extrude );
    mBtnStretch = (Button) findViewById(R.id.rb_stretch );
    mBtnCut     = (Button) findViewById(R.id.rb_cut );

    // mBtnOk.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );
    mBtnErase.setOnClickListener( this );
    mBtnExtrude.setOnClickListener( this );
    mBtnStretch.setOnClickListener( this );
    mBtnCut.setOnClickListener( this );

    setTitle( R.string.title_sketch_edit );
  }

  public void onClick( View v ) 
  {
    Button b = (Button) v;
    if ( b == mBtnErase ) {
      // TODO 
      Toast.makeText( mParent, "Erase is not implemented yet", Toast.LENGTH_SHORT ).show();
    } else if ( b == mBtnCut ) {
      mParent.cutRegion();
    } else if ( b == mBtnStretch ) {
      mParent.stretchRegion();
    } else if ( b == mBtnExtrude ) {
      mParent.extrudeRegion();
    } else { // mBtnCancel
      // mParent.highlightRegion();
    }
    dismiss();
  }

}



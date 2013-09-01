/** @file SketchSectionTypeDialog.java
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

import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;

import android.content.Context;

import android.widget.Button;
import android.widget.RadioButton;
// import android.widget.Toast;
import android.view.View;


public class SketchSectionTypeDialog extends Dialog
                              implements View.OnClickListener
{
  private Button mBtnOk;
  private Button mBtnCancel;
  private RadioButton mBtnVert;
  private RadioButton mBtnHoriz;

  private SketchActivity mParent;
  // private TopoDroidApp   mApp;

  SketchSectionTypeDialog( Context context, SketchActivity parent )
  {
    super( context );
    mParent = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.sketch_section_type_dialog);
    mBtnOk     = (Button) findViewById(R.id.btn_ok );
    mBtnCancel = (Button) findViewById(R.id.btn_cancel );
    mBtnVert   = (RadioButton) findViewById(R.id.rb_vertical );
    mBtnHoriz  = (RadioButton) findViewById(R.id.rb_horizontal );

    mBtnOk.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );
    // mBtnVert.setOnClickListener( this );
    // mBtnHoriz.setOnClickListener( this );

    setTitle( R.string.title_sketch_section_type );
  }

  public void onClick( View v ) 
  {
    
    Button b = (Button) v;
    if ( b == mBtnOk ) {
      int type = SketchSection.SECTION_NONE;
      if ( mBtnVert.isChecked() ) {
        type = SketchSection.SECTION_VERT;
      } else if ( mBtnHoriz.isChecked() ) {
        type = SketchSection.SECTION_HORIZ;
      }
      if ( type != SketchSection.SECTION_NONE ) {
        mParent.setSectionType( type );
      }
    } else { // mBtnCancel
      // mParent.highlightRegion();
    }
    dismiss();
  }

}



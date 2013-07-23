/* @file Sketch3dNewDialog.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3d sketch: new-sketch3d dialog
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

import android.content.Context;

import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.View;

import android.widget.Toast;

public class Sketch3dNewDialog extends Dialog
                               implements View.OnClickListener
{
  private Context mContext;
  private INewPlot mMaker;
  private boolean notDone;

  private EditText mEditName;
  private EditText mEditStart;
  private EditText mEditNext;

  private Button   mBtnOK;
  private Button   mBtnCancel;

  public Sketch3dNewDialog( Context context, INewPlot maker )
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
    setContentView(R.layout.distox_sketch3d_dialog);
    mEditName  = (EditText) findViewById(R.id.edit_sketch3d_name);
    mEditStart = (EditText) findViewById(R.id.edit_sketch3d_start);
    mEditNext  = (EditText) findViewById(R.id.edit_sketch3d_next);

    mEditName.setHint( R.string.scrap_name );
    mEditStart.setHint( R.string.station_base );
    mEditNext.setHint( R.string.station_next );

    mBtnOK = (Button) findViewById(R.id.button_ok_sketch3d_name );
    mBtnOK.setOnClickListener( this );
    mBtnCancel = (Button) findViewById(R.id.button_cancel_sketch3d_name );
    mBtnCancel.setOnClickListener( this );
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( notDone && b == mBtnOK ) {
      notDone = false;
      String name  = mEditName.getText().toString();
      String start = mEditStart.getText().toString();
      String next  = null;
      if ( mEditNext.getText() != null ) {
        next = mEditNext.getText().toString();
        next = TopoDroidApp.noSpaces( next  );
        if ( next.length() == 0 ) next = null;
      }

      name = TopoDroidApp.noSpaces( name );
      if ( name == null || name.length() == 0 ) {
        Toast.makeText( mContext, R.string.sketch3d_null_name, Toast.LENGTH_LONG ).show();
      } else {
        start = TopoDroidApp.noSpaces( start );
        if ( start == null || start.length() == 0 ) {
          Toast.makeText( mContext, R.string.sketch3d_null_start, Toast.LENGTH_LONG ).show();
        } else {
          mMaker.makeNewSketch3d( name, start, next );
        }
      }
    }
    dismiss();
  }
}


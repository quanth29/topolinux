/* @file FixedDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey shot dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120603 created 
 * 20130131 intent: Proj4 coord conversion
 */
package com.android.DistoX;

// import java.Thread;
// import java.util.regex.Pattern;

import android.app.Dialog;
import android.os.Bundle;

import android.text.InputType;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ActivityNotFoundException;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

import android.util.Log;


public class FixedDialog extends Dialog
                         implements View.OnClickListener
{
  private Context mContext;
  private SurveyActivity mParent;
  private DistoXLocation mSubParent;
  private FixedInfo mFxd;

  // private TextView mTVdata;
  private EditText mETstation;
  private Button   mButtonDrop;
  private Button   mButtonOK;
  private Button   mButtonConvert;
  private Button   mButtonCancel;

  public FixedDialog( Context context, SurveyActivity parent, DistoXLocation sub_parent, FixedInfo fxd )
  {
    super(context);
    mContext     = context;
    mParent      = parent;
    mSubParent   = sub_parent;
    mFxd         = fxd;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidApp.Log( TopoDroidApp.LOG_FIXED, "FixedDialog onCreate" );
    setContentView(R.layout.distox_fixed_dialog);
    // mTVdata    = (TextView) findViewById(R.id.fix_data );
    mETstation = (EditText) findViewById(R.id.fix_station );

    mButtonDrop    = (Button) findViewById(R.id.fix_drop );
    mButtonOK      = (Button) findViewById(R.id.fix_ok );
    mButtonConvert = (Button) findViewById(R.id.fix_convert );
    mButtonCancel  = (Button) findViewById(R.id.fix_cancel );

    // mETstation.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    // mETstation.setKeyListener( NumberKeyListener );

    // mTVdata.setText( mFxd.toLocString() );
    setTitle( mFxd.toLocString() );
    mETstation.setText( mFxd.name );
    
    mButtonDrop.setOnClickListener( this );
    mButtonOK.setOnClickListener( this );
    mButtonConvert.setOnClickListener( this );
    mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "FixedDialog onClick() button " + b.getText().toString() );

    if ( b == mButtonOK ) {
      String station = mETstation.getText().toString();
      if ( station != null ) {
        station.trim();
        if ( ! station.equals("") ) {
          mFxd.name = station;
          mParent.updateFixed( mFxd, station );
          mSubParent.updatePos();
        }
      }
    } else if ( b == mButtonConvert ) {
      // Log.v( "Proj4", "convert " + mFxd.lng + " " + mFxd.lat );
      Intent intent = new Intent( "Proj4.intent.action.Launch" );
      intent.putExtra( "cs", "Long-Lat" ); // NOTE MUST USE SAME NAME AS Proj4
      intent.putExtra( "long", mFxd.lng );
      intent.putExtra( "lat",  mFxd.lat );
      intent.putExtra( "alt",  mFxd.alt );
      try {
        mContext.startActivity( intent );
      } catch ( ActivityNotFoundException e ) {
        // Toast.makeText( this, R.string.no_cave3d, Toast.LENGTH_LONG ).show();
      }
    } else if ( b == mButtonDrop ) {
      mParent.dropFixed( mFxd );
      mSubParent.refreshList();
    }
    dismiss();
  }
}

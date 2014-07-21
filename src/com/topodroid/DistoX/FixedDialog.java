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
 * 20140609 geomag
 */
package com.topodroid.DistoX;

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

import android.widget.Toast;

// import android.util.Log;


public class FixedDialog extends Dialog
                         implements View.OnClickListener
{
  private Context mContext;
  private SurveyActivity mParent;
  private DistoXLocation mSubParent;
  private FixedInfo mFxd;

  // private TextView mTVdata;
  private EditText mETstation;
  private EditText mETdecl;
  private TextView mTVcrs;
  private Button   mButtonDrop;
  private Button   mButtonGeomag;
  private Button   mButtonOK;
  private Button   mButtonConvert;
  // private Button   mButtonCancel;

  public FixedDialog( Context context, SurveyActivity parent, DistoXLocation sub_parent, FixedInfo fxd )
  {
    super(context);
    mContext     = context;
    mParent      = parent;
    mSubParent   = sub_parent;
    mFxd         = fxd;
  }
  
  void setCSto( String cs )
  {
    mTVcrs.setText( cs );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidApp.Log( TopoDroidApp.LOG_FIXED, "FixedDialog onCreate" );
    setContentView(R.layout.fixed_dialog);
    // mTVdata    = (TextView) findViewById(R.id.fix_data );
    mETstation = (EditText) findViewById(R.id.fix_station );
    mETdecl    = (EditText) findViewById(R.id.fix_decl );
    mTVcrs     = (TextView) findViewById(R.id.fix_crs );

    mButtonDrop    = (Button) findViewById(R.id.fix_drop );
    mButtonGeomag  = (Button) findViewById(R.id.fix_geomag );
    mButtonOK      = (Button) findViewById(R.id.fix_ok );
    mButtonConvert = (Button) findViewById(R.id.fix_convert );
    // mButtonCancel  = (Button) findViewById(R.id.fix_cancel );

    // mETstation.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    // mETstation.setKeyListener( NumberKeyListener );

    // mTVdata.setText( mFxd.toLocString() );
    setTitle( mFxd.toLocString() );
    mETstation.setText( mFxd.name );
  
    mTVcrs.setText( TopoDroidApp.mCRS );
    
    mButtonGeomag.setOnClickListener( this );
    mButtonDrop.setOnClickListener( this );
    mButtonOK.setOnClickListener( this );
    mButtonConvert.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "FixedDialog onClick() button " + b.getText().toString() );

    if ( b == mButtonOK ) {
      if ( mETdecl.getText() != null && mETdecl.getText().toString() != null ) {
        try {
          float decl = Float.parseFloat( mETdecl.getText().toString() );
          mParent.setDeclination( decl );
        } catch ( NumberFormatException e ) {
          // TODO
        }
      }
      String station = mETstation.getText().toString().trim();
      if ( station.length() == 0 ) {
        String error = mContext.getResources().getString( R.string.error_station_required );
        mETstation.setError( error );
        return;
      }
      mFxd.name = station;
      mParent.updateFixed( mFxd, station );
      mSubParent.updatePos();
    } else if ( b == mButtonConvert ) {
      if ( mTVcrs.getText() != null ) {
        mParent.tryProj4( this, mTVcrs.getText().toString(), mFxd );
      }
      return;
    } else if ( b == mButtonGeomag ) {
      float decl = GeodeticHeight.getGeomag( mFxd );
      if ( decl > -180 ) {
        mETdecl.setText( String.format( "%.4f", decl ) );
      } else {
        Toast.makeText( mParent, R.string.no_geomag, Toast.LENGTH_SHORT).show();
      }
      return;
    } else if ( b == mButtonDrop ) {
      mParent.dropFixed( mFxd );
      mSubParent.refreshList();
    } 
    dismiss();
  }
}

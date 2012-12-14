/* @file DistoXLongLatAltDialog.java
 *
 * @author marco corvi
 * @date nov 2012
 *
 * @brief TopoDroid manual location dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.android.DistoX;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.EditText;
import android.widget.Button;
import android.view.View;

import android.widget.Toast;

public class DistoXLongLatAltDialog extends Dialog
                              implements View.OnClickListener
{
  private Context mContext;
  private DistoXLocation mParent;

  private EditText mEditLong;
  private EditText mEditLat;
  private EditText mEditAlt;

  private Button   mBtnOK;
  // private Button   mBtnBack;
  private Button   mBtnCancel;

  public DistoXLongLatAltDialog( Context context, DistoXLocation parent )
  {
    super( context );
    mContext = context;
    mParent  = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.distox_longlatalt_dialog);
    mEditLong  = (EditText) findViewById(R.id.edit_long );
    mEditLat   = (EditText) findViewById(R.id.edit_lat );
    mEditAlt   = (EditText) findViewById(R.id.edit_alt );

    mBtnOK = (Button) findViewById(R.id.button_ok);
    mBtnCancel = (Button) findViewById(R.id.button_cancel);
    mBtnOK.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );
  }

  double string2decdegrees( String str )
  {
    // tokenize str on ':'
    String[] token = str.split( ":" );
    double ret = 0.0;
    if ( token.length > 0 ) {
      ret = Integer.parseInt( token[0] );
      if ( token.length > 1 && token[1] != null ) {
        ret += Integer.parseInt( token[1] ) / 60.0;
        if ( token.length > 2 && token[2] != null ) {
          ret += Double.parseDouble( token[2] ) / 3600.0;
        }
      }
    } 
    return ret;
  }

  // FIXME synchronized ?
  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "LongLatAltDialog onClick() button " + b.getText().toString() ); 

    if ( b == mBtnOK ) {
   
      String longit = mEditLong.getText().toString();
      String latit  = mEditLat.getText().toString();
      String altit  = mEditAlt.getText().toString();
      // TODO convert string to dec-degrees
      if ( longit != null && latit != null && altit != null ) {
        double lng = string2decdegrees( longit );
        double lat = string2decdegrees( latit );
        double alt = Double.parseDouble( altit );
        mParent.addFixedPoint( lng, lat, alt );
      } else {
        // TODO Toast a warning
      }
    }
    dismiss();
  }

}


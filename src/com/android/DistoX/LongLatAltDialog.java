/* @file LongLatAltDialog.java
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
 * 20130520 altimetric altitude
 */
package com.android.DistoX;

// import java.net.URL;
// import java.net.URLConnection;
// import java.net.HttpURLConnection;
// import java.net.URLEncoder;
// import java.net.MalformedURLException;

// import java.io.IOException;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.io.OutputStream;
// import java.io.BufferedReader;
// import java.io.BufferedInputStream;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;

import android.widget.Toast;

import android.util.Log;

public class LongLatAltDialog extends Dialog
                              implements View.OnClickListener
{
  private Context mContext;
  private DistoXLocation mParent;

  private EditText mEditLong;
  private EditText mEditLat;
  private EditText mEditAlt; // altitude

  private Button   mBtnOK;
  // private Button   mBtnBack;
  // private Button   mBtnCancel;
  private CheckBox mWGS84; // checked if alt is wgs84

  public LongLatAltDialog( Context context, DistoXLocation parent )
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
    setContentView(R.layout.longlatalt_dialog);
    mEditLong  = (EditText) findViewById(R.id.edit_long );
    mEditLat   = (EditText) findViewById(R.id.edit_lat );
    mEditAlt   = (EditText) findViewById(R.id.edit_alt );
    mWGS84     = (CheckBox) findViewById(R.id.edit_wgs84 );

    mBtnOK = (Button) findViewById(R.id.button_ok);
    // mBtnCancel = (Button) findViewById(R.id.button_cancel);
    mBtnOK.setOnClickListener( this );
    // mBtnCancel.setOnClickListener( this );
  }

  double string2decdegrees( String str )
  {
    // tokenize str on ':'
    String[] token = str.split( ":" );
    double ret = 0.0;
    try {
      if ( token.length > 0 ) {
        ret = Integer.parseInt( token[0] );
        if ( token.length > 1 && token[1] != null ) {
          ret += Integer.parseInt( token[1] ) / 60.0;
          if ( token.length > 2 && token[2] != null ) {
            ret += Double.parseDouble( token[2] ) / 3600.0;
          }
        }
      } 
    } catch (NumberFormatException e ) {
      // TODO
      ret = 0.0;
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
        double alt = -1.0;
        double asl = -1.0;
        if ( mWGS84.isChecked() ) {
          alt = Double.parseDouble( altit );
        } else {
          asl = Double.parseDouble( altit );
        }
        if ( alt < 0 ) {
          alt = asl + GeodeticHeight.geodeticHeight( latit, longit );
        } else if ( asl < 0 ) {
          asl = alt - GeodeticHeight.geodeticHeight( latit, longit );
        }
        mParent.addFixedPoint( lng, lat, alt, asl );
      } else {
        // TODO Toast a warning
      }
    }
    dismiss();
  }

}


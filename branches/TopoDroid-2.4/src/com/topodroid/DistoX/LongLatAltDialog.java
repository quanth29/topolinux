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
package com.topodroid.DistoX;

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

// import android.util.Log;

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

    if ( mParent.mHasLocation ) {
      mEditLong.setText( FixedInfo.double2ddmmss( mParent.mLongitude ) );
      mEditLat.setText(  FixedInfo.double2ddmmss( mParent.mLatitude ) );
      mEditAlt.setText(  Integer.toString( (int)(mParent.mAltitude) )  );
      mWGS84.setChecked( true );
    }

    mBtnOK = (Button) findViewById(R.id.button_ok);
    mBtnOK.setOnClickListener( this );
    // mBtnCancel = (Button) findViewById(R.id.button_cancel);
    // mBtnCancel.setOnClickListener( this );
  }

  double string2decdegrees( String str )
  {
    // tokenize str on ':'
    str = str.replace( " ", ":" );
    // Log.v("DistoX", "STRING " + str );
    String[] token = str.split( ":" );
    double ret = 0.0;
    try {
      if ( token.length > 0 ) {
        ret = Integer.parseInt( token[0] );
        if ( token.length > 1 && token[1] != null ) {
          ret += Integer.parseInt( token[1] ) / 60.0;
          if ( token.length > 2 && token[2] != null ) {
            String t = token[2].replace(",", ".");
            ret += Double.parseDouble( t ) / 3600.0;
          }
        }
      } 
    } catch (NumberFormatException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "string2decdegrees parse error: " + str );
      ret = 0.0;
    }
    return ret;
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "LongLatAltDialog onClick() button " + b.getText().toString() ); 

    if ( b == mBtnOK ) {
   
      String longit = mEditLong.getText().toString();
      // TODO convert string to dec-degrees
      if ( longit == null || longit.length() == 0 ) {
        mEditLong.setError( mContext.getResources().getString( R.string.error_long_required ) );
        return;
      }
      String latit = mEditLat.getText().toString();
      if ( latit == null || latit.length() == 0 ) {
        mEditLat.setError( mContext.getResources().getString( R.string.error_lat_required) );
        return;
      }
      String altit = mEditAlt.getText().toString();
      if ( altit == null || altit.length() == 0 ) {
        mEditAlt.setError( mContext.getResources().getString( R.string.error_alt_required) );
        return;
      }
      double lng = string2decdegrees( longit );
      double lat = string2decdegrees( latit );
      double alt = -1000.0;
      double asl = -1000.0;
      altit = altit.replace(",", ".");
      try {
        if ( mWGS84.isChecked() ) {
          alt = Double.parseDouble( altit );
        } else {
          asl = Double.parseDouble( altit );
        }
      } catch ( NumberFormatException e ) {
        mEditAlt.setError( mContext.getResources().getString( R.string.error_invalid_number) );
        return;
      }
      if ( alt < -999 ) {
        alt = asl + GeodeticHeight.geodeticHeight( latit, longit );
      } else if ( asl < -999 ) {
        asl = alt - GeodeticHeight.geodeticHeight( latit, longit );
      }
      mParent.addFixedPoint( lng, lat, alt, asl );
    }
    dismiss();
  }

}


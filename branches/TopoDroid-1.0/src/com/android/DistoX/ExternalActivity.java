/* @file ExternalActivity.java
 *
 * @author marco corvi
 * @date aug 2012
 *
 * @brief TopoDroid DistoX external sensor activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120826 created
 */
package com.android.DistoX;

import java.util.List;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;
import android.content.Context;

import android.widget.RadioButton;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import android.hardware.Sensor;
import android.hardware.SensorListener;
import android.hardware.SensorManager;


public class ExternalActivity extends Activity
                            implements View.OnClickListener
{ 
  private TopoDroidApp app;
  private float[] mValues;

  private EditText mETtype;
  private EditText mETvalue;
  private EditText mETcomment;

  private Button mBTok;
  private Button mBTcancel;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.external_activity);
    app = (TopoDroidApp) getApplication();

    mETtype  = ( EditText ) findViewById( R.id.external_type );
    mETvalue = ( EditText ) findViewById( R.id.external_value );
    mETcomment = ( EditText ) findViewById( R.id.external_comment );

    mBTok     = ( Button ) findViewById( R.id.external_ok );
    mBTcancel = ( Button ) findViewById( R.id.external_cancel );

    mBTok.setOnClickListener( this );
    mBTcancel.setOnClickListener( this );
    // setTitleColor( 0x006d6df6 );
  }

  @Override
  public void onClick( View view )
  {
    Button b = (Button) view;
    if ( b == mBTok ) {
      String type = mETtype.getText().toString();
      String value = mETvalue.getText().toString();
      String comment = mETcomment.getText().toString();
      if ( type.length() > 0 && value.length() > 0 ) {
        // TopoDroidApp.Log( TopoDroidApp.LOG_SENSOR, type + " " + value );
        Intent intent = new Intent();
        intent.putExtra( TopoDroidApp.TOPODROID_SENSOR_TYPE, type );
        intent.putExtra( TopoDroidApp.TOPODROID_SENSOR_VALUE, value );
        intent.putExtra( TopoDroidApp.TOPODROID_SENSOR_COMMENT, comment );
        setResult( RESULT_OK, intent );
      }
    } else {
      setResult( RESULT_CANCELED );
    }
    finish();
  }  
}

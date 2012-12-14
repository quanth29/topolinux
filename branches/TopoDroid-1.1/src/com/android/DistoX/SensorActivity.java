/* @file SensorActivity.java
 *
 * @author marco corvi
 * @date aug 2012
 *
 * @brief TopoDroid DistoX sensor activity
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


public class SensorActivity extends Activity
                            implements View.OnClickListener
{ 
  private static final String TAG = "DistoX Sensor ";

  private TopoDroidApp app;
  private SensorManager mSensorManager;
  private float[] mValues;
  private int mSensor; // current sensor
  private String mSensorType; // current sensor type

  private RadioButton mRBLight = null;
  private RadioButton mRBMagnetic = null;
  // private RadioButton mRBProximity = null;
  private RadioButton mRBTemperature = null;
  private RadioButton mRBPressure = null;
  private RadioButton mRBGravity = null;
  // private RadioButton mRBHumidity = null;

  private EditText mETtype;
  private EditText mETvalue;
  private EditText mETcomment;

  private Button mBTok;
  private Button mBTcancel;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.sensor_activity);
    app = (TopoDroidApp) getApplication();
    mSensorManager = (SensorManager)getSystemService( Context.SENSOR_SERVICE );

    mRBLight         = ( RadioButton ) findViewById( R.id.sensor_light );
    mRBMagnetic      = ( RadioButton ) findViewById( R.id.sensor_magnetic_field );
    // mRBProximity     = ( RadioButton ) findViewById( R.id.sensor_proximity );
    mRBTemperature   = ( RadioButton ) findViewById( R.id.sensor_temperature );
    mRBPressure      = ( RadioButton ) findViewById( R.id.sensor_pressure );
    mRBGravity       = ( RadioButton ) findViewById( R.id.sensor_gravity );
    // mRBHumidity      = ( RadioButton ) findViewById( R.id.sensor_humidity );

    mETtype  = ( EditText ) findViewById( R.id.sensor_type );
    mETvalue = ( EditText ) findViewById( R.id.sensor_value );
    mETcomment = ( EditText ) findViewById( R.id.sensor_comment );

    List< Sensor > sl = mSensorManager.getSensorList( Sensor.TYPE_LIGHT );
    if ( sl.size() > 0 ) {
      mRBLight.setOnClickListener( this );
    } else {
      mRBLight.setEnabled( false );
    }
    
    sl = mSensorManager.getSensorList( Sensor.TYPE_MAGNETIC_FIELD );
    if ( sl.size() > 0 ) {
      mRBMagnetic.setOnClickListener( this );
    } else {
      mRBMagnetic.setEnabled( false );
    }

    // sl = mSensorManager.getSensorList( Sensor.TYPE_PROXIMITY );
    // if ( sl.size() > 0 ) {
    //   mRBProximity.setOnClickListener( this );
    // } else {
    //   mRBProximity.setEnabled( false );
    // }

    sl = mSensorManager.getSensorList( Sensor.TYPE_TEMPERATURE );
    if ( sl.size() > 0 ) {
      mRBTemperature.setOnClickListener( this );
    } else {
      mRBTemperature.setEnabled( false );
    }

    sl = mSensorManager.getSensorList( Sensor.TYPE_PRESSURE );
    if ( sl.size() > 0 ) {
      mRBPressure.setOnClickListener( this );
    } else {
      mRBPressure.setEnabled( false );
    }

    sl = mSensorManager.getSensorList( Sensor.TYPE_ORIENTATION );
    if ( sl.size() > 0 ) {
      mRBGravity.setOnClickListener( this );
    } else {
      mRBGravity.setEnabled( false );
    }

    // sl = mSensorManager.getSensorList( Sensor.TYPE_RELATIVE_HUMIDITY );
    // if ( sl.size() > 0 ) {
    //   mRBHumidity.setOnClickListener( this );
    // } else {
    //   mRBHumidity.setEnabled( false );
    // }

    mBTok     = ( Button ) findViewById( R.id.sensor_ok );
    mBTcancel = ( Button ) findViewById( R.id.sensor_cancel );

    mBTok.setOnClickListener( this );
    mBTcancel.setOnClickListener( this );
    // setTitleColor( 0x006d6df6 );

    // mETtype.setText( null );
    // mETvalue.setText( null );
  }

  private void setSensor( )
  { 
    if ( mSensor != -1 ) {
      mSensorManager.unregisterListener(mListener);
    }
    mETvalue.setText( null );
    mSensor = -1;
    if ( mRBLight != null && mRBLight.isChecked() ) {
      mSensor = Sensor.TYPE_LIGHT;
      mETtype.setText( R.string.sensor_light );
    } else if ( mRBMagnetic != null && mRBMagnetic.isChecked() ) {
      mSensor = Sensor.TYPE_MAGNETIC_FIELD;
      mETtype.setText( R.string.sensor_magnetic_field );
    // } else if ( mRBProximity != null && mRBProximity.isChecked() ) {
    //   mSensor = Sensor.TYPE_PROXIMITY;
    //   mETtype.setText( R.string.sensor_proximity );
    } else if ( mRBTemperature != null && mRBTemperature.isChecked() ) {
      mSensor = Sensor.TYPE_TEMPERATURE; //  Sensor.TYPE_AMBIENT_TEMPERATURE;
      mETtype.setText( R.string.sensor_temperature );
    } else if ( mRBPressure != null && mRBPressure.isChecked() ) {
      mSensor = Sensor.TYPE_PRESSURE;
      mETtype.setText( R.string.sensor_pressure );
    } else if ( mRBGravity != null && mRBGravity.isChecked() ) {
      mSensor = Sensor.TYPE_ORIENTATION; // Sensor.TYPE_GRAVITY;
      mETtype.setText( R.string.sensor_gravity );
    // } else if ( mRBHumidity != null && mRBHumidity.isChecked() ) {
    //   mSensor = Sensor.TYPE_RELATIVE_HUMIDITY;
    //   mETtype.setText( R.string.sensor_humidity );
    }
    if ( mSensor != -1 ) {
      mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
  }
    
  private final SensorListener mListener = new SensorListener()
  {
    public void onSensorChanged(int sensor, float[] values)
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_SENSOR, "sensorChanged (" + values[0] + ", " + values[1] + ", " + values[2] + ")");
      mValues = values;
      StringWriter sw = new StringWriter();
      PrintWriter  pw = new PrintWriter( sw );
      switch ( mSensor ) {
        case Sensor.TYPE_LIGHT:
        // case Sensor.TYPE_PROXIMITY:
        case Sensor.TYPE_TEMPERATURE:
        case Sensor.TYPE_PRESSURE:
        // case Sensor.TYPE_RELATIVE_HUMIDITY:
          pw.format( "%.2f", values[0] );
          break;
        case Sensor.TYPE_MAGNETIC_FIELD:
        case Sensor.TYPE_ORIENTATION:
        // case Sensor.TYPE_ACCELEROMETER:
          pw.format( "%.2f %.2f %.2f", values[0], values[1], values[2] );
          break;
        default:
          pw.format( "%.2f %.2f %.2f", values[0], values[1], values[2] );
          break;
      }
      mETvalue.setText( sw.getBuffer().toString() );
    }

    public void onAccuracyChanged(int sensor, int accuracy)
    {
      // TODO Auto-generated method stub
    }
  };

  @Override
  public void onClick( View view )
  {
    Button b = (Button) view;
    // TopoDroidApp.Log(  TopoDroidApp.LOG_INPUT, "SensorActivity onClick() button " + b.getText().toString() );

    if ( b == mBTok ) {
      String type = mETtype.getText().toString();
      String value = mETvalue.getText().toString();
      String comment = mETcomment.getText().toString();
      if ( type.length() > 0 && value.length() > 0 ) {
        // TopoDroidApp.Log( TopoDroidApp.LOG_SENSOR, "sensor " + type + " " + value );
        Intent intent = new Intent();
        intent.putExtra( TopoDroidApp.TOPODROID_SENSOR_TYPE, type );
        intent.putExtra( TopoDroidApp.TOPODROID_SENSOR_VALUE, value );
        intent.putExtra( TopoDroidApp.TOPODROID_SENSOR_COMMENT, comment );
        setResult( RESULT_OK, intent );
      }
      finish();
    } else if ( b == mBTcancel ) {
      setResult( RESULT_CANCELED );
      finish();
    } else { 
      setSensor();
    }
  }  
    
  @Override
  protected void onResume()
  {
    super.onResume();
    mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
  }
    
  @Override
  protected void onStop()
  {
     mSensorManager.unregisterListener(mListener);
     super.onStop();
  }

}



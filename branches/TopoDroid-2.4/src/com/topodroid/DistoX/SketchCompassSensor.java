/* @file SketchCompassSensor.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid compass sensor for the 3d sketch
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130917 created
 */
package com.topodroid.DistoX;

import android.hardware.SensorManager;
import android.hardware.SensorListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.util.Log;

class SketchCompassSensor 
    implements SensorEventListener
    // implements SensorListener
{
  private SketchActivity mParent;
  private SensorManager mSM;
  private Sensor mMag;  // magnetic field sensor
  private Sensor mAcc;  // accelerometer sensor
  private float mHead; // sum of headings
  private float mAve;  // average heading
  private int mCnt;    // number of headings
  private int mMax;
  private Vector mMagValue;
  private Vector mAccValue;
  // private float[] mRot;      // rotation matrix
  // private float[] mIncl;     // inclination rotation matrix
  // private float[] mValue;    // orientation
  private boolean mHasMag;
  private boolean mHasAcc;

  /**
   * @param max number of readings to average
   */
  SketchCompassSensor( SketchActivity parent, SensorManager sm, int max )
  {
    mParent = parent;
    mHead = 0.0f;
    mAve = 0.0f;
    mCnt  = 0;
    mMax = max;
    mMagValue = new Vector();
    mAccValue = new Vector();
    mHasMag = false;
    mHasAcc = false;

    mSM = sm;
    mMag = mSM.getDefaultSensor( Sensor.TYPE_MAGNETIC_FIELD );
    mAcc = mSM.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
    if ( mMag == null ) {
      Log.e("DistoX", "null TYPE_MAGNETIC_FIELD sensor");
    }
    if ( mAcc == null ) {
      Log.e("DistoX", "null TYPE_ACCELEROMETER sensor");
    } 
    if ( mMag != null && mAcc != null ) {
      if ( mSM.registerListener( this, 
             mMag, // SensorManager.SENSOR_ORIENTATION,
             SensorManager.SENSOR_DELAY_NORMAL ) == false 
        ||
           mSM.registerListener( this, 
            mAcc,
            SensorManager.SENSOR_DELAY_NORMAL ) == false ) {
        // Log.v("DistoX", "failed register sensors");
        mSM.unregisterListener( this );
        mParent.setHeading( mAve );
      }
    } else {
      mSM.unregisterListener( this );
      mParent.setHeading( mAve );
    }
  }

  // public void onSensorChanged( int sensor, float values[] )
  public void onSensorChanged( SensorEvent ev )
  {

    // if ( sensor == SensorManager.SENSOR_ORIENTATION )
    if ( ev.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD ) {
      mHasMag = true;
      mMagValue.x = ev.values[0];
      mMagValue.y = ev.values[1];
      mMagValue.z = ev.values[2];
      // if ( ev.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ) {
      //   Log.w( "DistoX", "Sensor MAG unreliable. " + mMagValue.x + " " + mMagValue.y + " " + mMagValue.z );
      // }
    } else if ( ev.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
      mHasAcc = true;
      mAccValue.x = ev.values[0];
      mAccValue.y = ev.values[1];
      mAccValue.z = ev.values[2];
      // if ( ev.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ) {
      //   Log.w( "DistoX", "Sensor ACC unreliable. " + mAccValue.x + " " + mAccValue.y + " " + mAccValue.z );
      // }
    }
    if ( mHasAcc && mHasMag ) {
      // if ( mSM.getRotationMatrix( mRot, mIncl, mAccValue, mMagValue ) ) {
      //   mSM.getOrientation( mRot, mValue );
      {
        mAccValue.Normalized();
        float dot = mMagValue.dot( mAccValue );
        mAccValue.times( dot );
        mMagValue.sub( mAccValue );
        mHasAcc = false;
        mHasMag = false;
        float head = (float)Math.atan2( mMagValue.x, mMagValue.y) * TopoDroidUtil.RAD2GRAD;
        if ( mCnt == 0 ) {
          mHead = head;
          mAve  = head;
          mCnt ++;  
        } else {
          if ( mAve - head > 180.0f ) {
            head += 360.0f;
          } else if ( head - mAve > 180.0f ) {
            head -= 360.0f;
          }
          mHead += head;
          mCnt ++;  
          mAve = mHead / mCnt;
          if ( mAve < 0.0f ) {
            mAve  += 360.0f;
            mHead += 360.0f * mCnt;
          } else if ( mAve >= 360.0f ) {
            mAve  -= 360.0f;
            mHead -= 360.0f * mCnt;
          }
        }
        // Log.v("DistoX", "cnt " + mCnt + " head " + mAve );
        if ( mCnt >= mMax ) {
          mSM.unregisterListener( this );
          mParent.setHeading( mAve );
        }
      }
    }
  }
      
  // public void onAccuracyChanged( int sensor, int accuracy ) 
  public void onAccuracyChanged( Sensor sensor, int accuracy ) 
  { 
  }

}

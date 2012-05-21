/* @file CalibCBlock.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120517 angle units
 */
package com.android.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;
// import android.util.Log;

public class CalibCBlock
{
  // private static final String TAG = "DistoX CBlock";
  private static final float grad2rad = TopoDroidApp.GRAD2RAD_FACTOR;

  private static int[] colors = { 0xffcccccc, 0xffffcccc, 0xffccccff };

  public long mId;
  public long mCalibId;
  public long gx;
  public long gy;
  public long gz;
  public long mx;
  public long my;
  public long mz;
  public long  mGroup;
  public float mBearing;  // computed compass
  public float mClino;    // computed clino
  public float mRoll;     // computed roll
  public float mError;    // error in the calibration algo associated to this data

  public CalibCBlock()
  {
    mId = 0;
    mCalibId = 0;
    gx = 0;
    gy = 0;
    gz = 0;
    mx = 0;
    my = 0;
    mz = 0;
    mGroup = 0;
    mError = 0.0f;
  }

  public boolean isFarFrom( float b0, float c0, float thr )
  {
    computeBearingAndClino();
    float c = c0 * grad2rad;
    float b = b0 * grad2rad;
    Vector v1 = new Vector( (float)Math.cos(c) * (float)Math.cos(b), 
                            (float)Math.cos(c) * (float)Math.sin(b),
                            (float)Math.sin(c) );
    c = mClino   * grad2rad; 
    b = mBearing * grad2rad;
    Vector v2 = new Vector( (float)Math.cos(c) * (float)Math.cos(b), 
                            (float)Math.cos(c) * (float)Math.sin(b),
                            (float)Math.sin(c) );
    float x = v1.dot(v2);
    return x < thr; // 0.70: approx 45 degrees
  }

  public void setId( long id, long cid ) 
  {
    mId = id;
    mCalibId = cid;
  }
  public void setGroup( long g ) { mGroup = g; }
  public void setError( float err ) { mError = err; }

  public int color() 
  {
    if ( mGroup <= 0 ) return colors[0];
    return colors[ 1 + (int)(mGroup % 2) ];
  }

  public void setData( long gx0, long gy0, long gz0, long mx0, long my0, long mz0 )
  {
    gx = ( gx0 > TopoDroidApp.ZERO ) ? gx0 - TopoDroidApp.NEG : gx0;
    gy = ( gy0 > TopoDroidApp.ZERO ) ? gy0 - TopoDroidApp.NEG : gy0;
    gz = ( gz0 > TopoDroidApp.ZERO ) ? gz0 - TopoDroidApp.NEG : gz0;
    mx = ( mx0 > TopoDroidApp.ZERO ) ? mx0 - TopoDroidApp.NEG : mx0;
    my = ( my0 > TopoDroidApp.ZERO ) ? my0 - TopoDroidApp.NEG : my0;
    mz = ( mz0 > TopoDroidApp.ZERO ) ? mz0 - TopoDroidApp.NEG : mz0;
  } 

  public void computeBearingAndClino()
  {
    float f = TopoDroidApp.FV;
    // StringWriter sw = new StringWriter();
    // PrintWriter pw = new PrintWriter( sw );
    // pw.format(" G %d %d %d M %d %d %d E %.2f", gx, gy, gz, mx, my, mz, mError );
    // Log.v( TAG, sw.getBuffer().toString() );
    Vector g = new Vector( gx/f, gy/f, gz/f );
    Vector m = new Vector( mx/f, my/f, mz/f );
    doComputeBearingAndClino( g, m );
  }

  public void computeBearingAndClino( Calibration calib )
  {
    float f = TopoDroidApp.FV;
    Vector g = new Vector( gx/f, gy/f, gz/f );
    Vector m = new Vector( mx/f, my/f, mz/f );
    Vector g0 = calib.GetAG().times( g );
    Vector m0 = calib.GetAM().times( m );
    Vector g1 = calib.GetBG().plus( g0 );
    Vector m1 = calib.GetBM().plus( m0 );
    doComputeBearingAndClino( g1, m1 );
  }

  private void doComputeBearingAndClino( Vector g, Vector m )
  {
    g.Normalized();
    m.Normalized();
    Vector e = new Vector( 1.0f, 0.0f, 0.0f );
    Vector y = m.cross( g );
    Vector x = g.cross( y );
    y.Normalized();
    x.Normalized();
    float ex = e.dot( x );
    float ey = e.dot( y );
    float ez = e.dot( g );
    mBearing = (float)Math.atan2( -ey, ex );
    mClino   = - (float)Math.atan2( ez, (float)Math.sqrt(ex*ex+ey*ey) );
    mRoll    = (float)Math.atan2( g.y, g.z );
    if ( mBearing < 0.0f ) mBearing += 2*TopoDroidApp.M_PI;
    if ( mRoll < 0.0f ) mRoll += 2*TopoDroidApp.M_PI;
    mClino   *= TopoDroidApp.RAD2GRAD_FACTOR;
    mBearing *= TopoDroidApp.RAD2GRAD_FACTOR;
    mRoll    *= TopoDroidApp.RAD2GRAD_FACTOR;
  }

  public String toString()
  {
    float ua = TopoDroidApp.mUnitAngle;

    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    computeBearingAndClino();
    pw.format("%d <%d> %5.1f %5.1f %5.1f %6.4f",
      mId, mGroup, mBearing*ua, mClino*ua, mRoll*ua, mError );
    return sw.getBuffer().toString();
  }
}


/* @file CalibReadTask.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calib coeff read task
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20140701 created
 */
package com.topodroid.DistoX;

import android.app.Activity;
import android.os.AsyncTask;

import android.widget.Button;
import android.widget.Toast;

class CalibReadTask extends AsyncTask<Void, Integer, Boolean>
{
  byte[]   coeff;
  Button   mButton;
  Activity mActivity;
  TopoDroidApp mApp;

  CalibReadTask( Activity activity, Button btn, TopoDroidApp app )
  {
    mButton   = btn;
    mActivity = activity;
    mApp      = app;
    coeff = new byte[48];
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    return new Boolean( mApp.mComm.readCoeff( mApp.mDevice.mAddress, coeff ) );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    if ( result ) {
      String[] items = new String[8];
      Vector bg = new Vector();
      Matrix ag = new Matrix();
      Vector bm = new Vector();
      Matrix am = new Matrix();
      Calibration.coeffToG( coeff, bg, ag );
      Calibration.coeffToM( coeff, bm, am );
      (new CalibCoeffDialog( mActivity, bg, ag, bm, am, 0.0f, 0.0f, 0 ) ).show();
    } else {
      Toast.makeText( mActivity, R.string.read_failed, Toast.LENGTH_SHORT).show();
    }
    mButton.setBackgroundResource(  R.drawable.ic_read );
    mButton.setEnabled( true );
    mActivity.setTitleColor( TopoDroidApp.COLOR_NORMAL );
  }
}

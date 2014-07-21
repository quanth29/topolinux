/* @file CalibToggleTask.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calib mode toggle task
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

class CalibToggleTask extends AsyncTask<Void, Integer, Boolean>
{
  Activity   mActivity;
  Button     mButton;
  TopoDroidApp mApp;

  CalibToggleTask( Activity act, Button btn, TopoDroidApp app )
  {
    mActivity = act;
    mButton   = btn;
    mApp      = app;
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    return new Boolean( mApp.mComm.toggleCalibMode( mApp.mDevice.mAddress, mApp.mDevice.mType ) );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    if ( result ) {
      Toast.makeText( mActivity, R.string.toggle_ok, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText( mActivity, R.string.toggle_failed, Toast.LENGTH_SHORT).show();
    }
    mButton.setBackgroundResource( R.drawable.ic_toggle );
    mButton.setEnabled( true );
    mActivity.setTitleColor( TopoDroidApp.COLOR_NORMAL );
  }
}

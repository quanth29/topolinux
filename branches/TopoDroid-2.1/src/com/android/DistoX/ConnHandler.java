/* @file ConnHandler.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid connection messages handler
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120529 created
 */
package com.android.DistoX;

import android.os.Handler;
import android.os.Message;
import android.app.Activity;

class ConnHandler extends Handler
{
  TopoDroidApp app;
  Activity mActivity;

  public ConnHandler( TopoDroidApp _app, Activity activity )
  {
    super();
    app = _app;
    mActivity = activity;
  }

  @Override
  public void handleMessage( Message msg )
  {
    mActivity.setTitleColor( app.isConnected() ? TopoDroidApp.COLOR_CONNECTED : TopoDroidApp.COLOR_NORMAL );
  }
}

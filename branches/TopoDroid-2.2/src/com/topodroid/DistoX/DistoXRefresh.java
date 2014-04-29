/* @file DistoXRefresh.java
 *
 * @author marco corvi
 * @date feb 2012
 *
 * @brief TopoDroid one-shot download distoX data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120726 TopoDroid log
 */
package com.topodroid.DistoX;

// import java.Thread;

import android.widget.Toast;
import android.os.AsyncTask;


public class DistoXRefresh extends AsyncTask< String, Integer, Integer >
{
  private TopoDroidApp app;
  private ILister lister;                       // list display
  private static DistoXRefresh running = null;

  DistoXRefresh( TopoDroidApp parent, ILister il )
  {
    TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "DistoXRefresh cstr" );
    app = parent;
    lister = il;
  }

// -------------------------------------------------------------------
  @Override
  protected Integer doInBackground( String... statuses )
  {
    if ( ! lock() ) return null;
    int nRead = app.downloadData();
    // if ( nRead < 0 ) {
    //   TopoDroidApp app = (TopoDroidApp) getApplication();
    //   Toast.makeText( getApplicationContext(), app.DistoXConnectionError[ -nRead ], Toast.LENGTH_SHORT ).show();
    TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "doInBackground read " + nRead );
    return nRead;
  }

  @Override
  protected void onProgressUpdate( Integer... values)
  {
    super.onProgressUpdate( values );
    TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "onProgressUpdate " + values );
  }

  @Override
  protected void onPostExecute( Integer res )
  {
    TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "onPostExecute res " + res );
    if ( res != null ) {
      int r = res.intValue();
      if ( lister != null ) {
        lister.refreshDisplay( r, true );
      }
    }
    unlock();
  }

  private synchronized boolean lock()
  {
    if ( running != null ) return false;
    running = this;
    return true;
  }

  private synchronized void unlock()
  {
    if ( running == this ) running = null;
  }

}

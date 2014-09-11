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
  private TopoDroidApp mApp;
  private ILister mLister;                       // list display
  private static DistoXRefresh running = null;

  DistoXRefresh( TopoDroidApp parent, ILister lister )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "DistoXRefresh cstr" );
    mApp = parent;
    mLister = lister;
  }

// -------------------------------------------------------------------
  @Override
  protected Integer doInBackground( String... statuses )
  {
    if ( ! lock() ) return null;
    int nRead = mApp.downloadData( mLister );
    // if ( nRead < 0 ) {
    //   TopoDroidApp mApp = (TopoDroidApp) getApplication();
    //   Toast.makeText( mApp.getApplicationContext(), mApp.DistoXConnectionError[ -nRead ], Toast.LENGTH_SHORT ).show();
    // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "doInBackground read " + nRead );
    return nRead;
  }

  @Override
  protected void onProgressUpdate( Integer... values)
  {
    super.onProgressUpdate( values );
    // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "onProgressUpdate " + values );
  }

  @Override
  protected void onPostExecute( Integer res )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "onPostExecute res " + res );
    if ( res != null ) {
      int r = res.intValue();
      if ( mLister != null ) {
        mLister.refreshDisplay( r, true );
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

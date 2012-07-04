/* @file DistoXRefresh.java
 *
 * @author marco corvi
 * @date feb 2012
 *
 * @brief TopoDroid one-shot download distoX data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

// import java.Thread;

// import android.util.Log;
import android.widget.Toast;
import android.os.AsyncTask;


public class DistoXRefresh extends AsyncTask< String, Integer, Integer >
{
  // private static final String TAG = "DistoX Refresh";

  private TopoDroidApp app;
  private ILister lister;                       // list display
  private static DistoXRefresh running = null;

  DistoXRefresh( TopoDroidApp parent, ILister il )
  {
    // Log.v( TAG, "DistoXRefresh" );
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
    //   Toast.makeText( getApplicationContext(), app.DistoXConnectionError[ -nRead ], Toast.LENGTH_LONG ).show();
    // }
    // Log.v( TAG, "DistoXRefresh in background read " + nRead );
    return nRead;
  }

  @Override
  protected void onProgressUpdate( Integer... values)
  {
    // Log.v( TAG, "DistoXRefresh on progress update " + values );
    super.onProgressUpdate( values );
  }

  @Override
  protected void onPostExecute( Integer res )
  {
    // Log.v( TAG, "DistoXRefresh on post execute res " + res );
    if ( res != null ) {
      int r = res.intValue();
      lister.refreshDisplay( r );
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

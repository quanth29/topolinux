/** @file SaveTh2File.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid drawing: save drawing in therion format
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.FileWriter;
import java.io.BufferedWriter;

import android.content.Intent;
import android.content.Context;

import android.os.AsyncTask;
// import android.os.Bundle;
import android.os.Handler;


class SaveTh2File extends AsyncTask<Intent,Void,Boolean>
{
    private Context mContext;
    private Handler mHandler;
    private TopoDroidApp mApp;
    private DrawingSurface mSurface;
    private String mFullName1;
    private String mFullName2;

    public SaveTh2File( Context context, Handler handler, TopoDroidApp app, DrawingSurface surface, 
                        String fullname1, String fullname2 )
    {
       mContext  = context;
       mHandler  = handler;
       mApp      = app;
       mSurface  = surface;
       mFullName1 = fullname1;
       mFullName2 = fullname2;
       // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "SaveTh2File " + mFilename1 + " " + mFilename2 );
    }

    @Override
    protected Boolean doInBackground(Intent... arg0)
    {
      try {
        if ( mFullName2 != null ) {
          String filename2 = mApp.getTh2FileWithExt( mFullName2 );
          FileWriter writer2 = new FileWriter( filename2 );
          BufferedWriter out2 = new BufferedWriter( writer2 );
          mSurface.exportTherion( (int)PlotInfo.PLOT_EXTENDED, out2, mFullName2, PlotInfo.projName[ (int)PlotInfo.PLOT_EXTENDED ] );
          out2.flush();
          out2.close();
          String filename1 = mApp.getTh2FileWithExt( mFullName1 );
          FileWriter writer1 = new FileWriter( filename1 );
          BufferedWriter out1 = new BufferedWriter( writer1 );
          mSurface.exportTherion( (int)PlotInfo.PLOT_PLAN, out1, mFullName1, PlotInfo.projName[ (int)PlotInfo.PLOT_PLAN ] );
          out1.flush();
          out1.close();
        } else {
          String filename = mApp.getTh2FileWithExt( mFullName1 );
          FileWriter writer = new FileWriter( filename );
          BufferedWriter out = new BufferedWriter( writer );
          mSurface.exportTherion( (int)PlotInfo.PLOT_SECTION, out, mFullName1, PlotInfo.projName[ (int)PlotInfo.PLOT_SECTION ] );
          out.flush();
          out.close();
        }
        return true;
      } catch (Exception e) {
        e.printStackTrace();
      }
      if ( mHandler != null ) {
        //mHandler.post(completeRunnable);
      }
      return false;
    }

    @Override
    protected void onPostExecute(Boolean bool) {
        super.onPostExecute(bool);
        if ( mHandler != null ) {
          mHandler.sendEmptyMessage( bool? 1 : 0 );
        }
    }
}


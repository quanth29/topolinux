/* @file DrawingStationDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for a station point to the scrap
 *
 * for when station points are not automatically added
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130108 created
 */
package com.topodroid.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.graphics.*;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class DrawingStationDialog extends Dialog 
                                  implements View.OnClickListener
{
    private TextView mLabel;
    private Button mBtnOK;
    // private Button mBtnCancel;

    private Context mContext;
    private DrawingActivity mActivity;
    private DrawingStationName mStation;

    public DrawingStationDialog( Context context, DrawingActivity activity, DrawingStationName station )
    {
      super(context);
      mContext  = context;
      mActivity = activity;
      mStation  = station;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.drawing_station_dialog);

      mLabel     = (TextView) findViewById(R.id.station_text);
      mBtnOK     = (Button) findViewById(R.id.btn_ok);
      // mBtnCancel = (Button) findViewById(R.id.btn_cancel);

      mBtnOK.setOnClickListener( this );
      // mBtnCancel.setOnClickListener( this );

      setTitle( mContext.getResources().getString(R.string.STATION) + mStation.mName ); 
    }

    public void onClick(View view)
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DrawingStationDialog onClick() " + view.toString() );
      if (view.getId() == R.id.btn_ok ) {
        mActivity.addStationPoint( mStation );
      }
      dismiss();
    }
}
        


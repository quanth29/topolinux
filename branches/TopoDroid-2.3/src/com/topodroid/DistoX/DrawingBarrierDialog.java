/* @file DrawingBarrierDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for a barrier station
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20140513 created
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


public class DrawingBarrierDialog extends Dialog 
                                  implements View.OnClickListener
{
    private TextView mLabel;
    private Button mBtnOK;
    // private Button mBtnCancel;

    private Context mContext;
    private DrawingActivity mActivity;
    private String mStation;
    private boolean mIsBarrier;

    public DrawingBarrierDialog( Context context, DrawingActivity activity, String name, boolean is_barrier )
    {
      super(context);
      mContext  = context;
      mActivity = activity;
      mStation  = name;
      mIsBarrier = is_barrier; 
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.drawing_barrier_dialog);

      mLabel     = (TextView) findViewById(R.id.barrier_text);
      mBtnOK     = (Button) findViewById(R.id.btn_ok);
      // mBtnCancel = (Button) findViewById(R.id.btn_cancel);

      if ( mIsBarrier ) {
        mLabel.setText( mContext.getResources().getString(R.string.barrier_del) );
      }

      mBtnOK.setOnClickListener( this );
      // mBtnCancel.setOnClickListener( this );

      setTitle( mContext.getResources().getString(R.string.STATION) + mStation ); 
    }

    public void onClick(View view)
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DrawingBarrierDialog onClick() " + view.toString() );
      if (view.getId() == R.id.btn_ok ) {
        mActivity.toggleStationBarrier( mStation, mIsBarrier );
      }
      dismiss();
    }
}
        


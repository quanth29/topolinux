/* @file ShotDisplayDialog.java
 *
 * @author marco corvi
 * @date jan 2012
 *
 * @brief TopoDroid shot-list: display mode dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130110 created
 */
package com.android.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;

// import android.graphics.*;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;


public class ShotDisplayDialog extends Dialog 
                               // implements View.OnClickListener
{
    private CheckBox mCBids;      // whether to hide ids
    private CheckBox mCBsplay;    // whether to hide splays
    private CheckBox mCBblank;    // whether to hide blank
    private CheckBox mCBleg;      // whether to hide repeated leg 
    // private Button mBtnOK;
    // private Button mBtnRefresh;
    // private Button mBtnCancel;

    private ShotActivity mParent;

    public ShotDisplayDialog( Context context, ShotActivity parent )
    {
      super(context);
      mParent = parent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shot_display_dialog);

        mCBids   = (CheckBox) findViewById(R.id.cb_mode_ids);
        mCBsplay = (CheckBox) findViewById(R.id.cb_mode_splay);
        mCBblank = (CheckBox) findViewById(R.id.cb_mode_blank);
        mCBleg   = (CheckBox) findViewById(R.id.cb_mode_leg);

        // mBtnOK     = (Button) findViewById(R.id.button_mode_ok);
        // mBtnRefresh = (Button) findViewById(R.id.button_mode_refresh);
        // mBtnCancel = (Button) findViewById(R.id.button_mode_cancel);

        // mBtnOK.setOnClickListener( this );
        // mBtnRefresh.setOnClickListener( this );
        // mBtnCancel.setOnClickListener( this );

        mCBids.setChecked( ! mParent.getShowIds() );
        mCBsplay.setChecked( mParent.mSplay );
        mCBblank.setChecked( mParent.mBlank );
        mCBleg.setChecked( mParent.mLeg );

        setTitle( R.string.title_mode );
    }

    // public void onClick(View view)
    // {
    //   // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "ShotDisplayDialog onClick " + view.toString() );
    //   hide();
    //   switch (view.getId()) {
    //     case R.id.button_mode_ok:
    //    mParent.setShowIds( ! mCBids.isChecked() );
    //    mParent.mSplay = mCBsplay.isChecked();
    //    mParent.mBlank = mCBblank.isChecked();
    //    mParent.mLeg   = mCBleg.isChecked();
    //    mParent.updateDisplay( );
    //       break;
    //  // case R.id.button_mode_refresh:
    //  //   mParent.updateDisplay( );
    //  //   break;
    //  // case R.id.button_mode_cancel:
    //  //   break;
    //   }
    //   dismiss();
    // }

    @Override
    public void onBackPressed ()
    {
      mParent.setShowIds( ! mCBids.isChecked() );
      mParent.mSplay = mCBsplay.isChecked();
      mParent.mBlank = mCBblank.isChecked();
      mParent.mLeg   = mCBleg.isChecked();
      mParent.updateDisplay( );
      cancel();
    }
}

/* @file DrawingLabelDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for the text of a label-point
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.graphics.*;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

// import android.util.Log;


public class DrawingLabelDialog extends Dialog 
                                implements View.OnClickListener
{
    // private static final String TAG = "DistoX DrawingLabel";

    private EditText mLabel;
    private Button mBtnOK;
    private Button mBtnCancel;

    private DrawingActivity mActivity;
    private float mX;
    private float mY;

    public DrawingLabelDialog( Context context, DrawingActivity activity, float x, float y )
    {
      super(context);
      mActivity = activity;
      mX = x; 
      mY = y;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_label_dialog);

        mLabel     = (EditText) findViewById(R.id.label_text);
        mBtnOK     = (Button) findViewById(R.id.label_ok);
        mBtnCancel = (Button) findViewById(R.id.label_cancel);

        mBtnOK.setOnClickListener( this );
        mBtnCancel.setOnClickListener( this );

        setTitle("LABEL");
    }

    public void onClick(View view)
    {
      // Log.v( TAG, "onClick()" );
      if (view.getId() == R.id.label_ok ) {
        mActivity.addLabel( mLabel.getText().toString(), mX, mY );
      }
      dismiss();
    }
}
        


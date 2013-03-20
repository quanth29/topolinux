/* @file MissingDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid misisng symbol(s) dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121220 created MissingDialog
 */
package com.android.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;
// import android.content.res.Resources;

import android.graphics.*;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

public class MissingDialog extends Dialog 
                           implements View.OnClickListener
{
    private Context mContext;
    private String mText;
    private TextView mTextView;
    private Button mBtnOk;
    public MissingDialog( Context context, String text )
    {
      super(context);
      mContext = context;
      mText = text;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.missing_dialog);

      mTextView = (TextView) findViewById(R.id.missing_text);
      mBtnOk    = (Button) findViewById(R.id.button_ok);
      mBtnOk.setOnClickListener( this );

      mTextView.setText( mText );
      setTitle( R.string.missing_title );
    }

    @Override
    public void onClick(View view)
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "MissingDialog onClick()" );
      dismiss();
    }
}
        


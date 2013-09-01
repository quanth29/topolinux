/* @file DrawingShotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for a survey shot
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130108 created
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
import android.widget.TextView;
import android.widget.EditText;
import android.widget.RadioButton;

public class DrawingShotDialog extends Dialog 
                               implements View.OnClickListener
{
    // private TextView mLabel;
    private Button mBtnOK;
    private Button mBtnCancel;
    private EditText mETfrom;
    private EditText mETto;
    private RadioButton mRBleft;
    private RadioButton mRBvert;
    private RadioButton mRBright;
    private RadioButton mRBignore;

    private DrawingActivity mActivity;
    private DistoXDBlock mBlock;

    public DrawingShotDialog( Context context, DrawingActivity activity, DrawingPath shot )
    {
      super(context);
      mActivity = activity;
      mBlock  = shot.mBlock;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.drawing_shot_dialog);

      // mLabel     = (TextView) findViewById(R.id.shot_text);
      mETfrom    = (EditText) findViewById(R.id.shot_from );
      mETto      = (EditText) findViewById(R.id.shot_to );

      mBtnOK     = (Button) findViewById(R.id.btn_ok);
      mBtnCancel = (Button) findViewById(R.id.btn_cancel);
      mRBleft    = (RadioButton) findViewById( R.id.left );
      mRBvert    = (RadioButton) findViewById( R.id.vert );
      mRBright   = (RadioButton) findViewById( R.id.right );
      mRBignore  = (RadioButton) findViewById( R.id.ignore );

      if ( ! TopoDroidApp.mLoopClosure ) {
        mRBignore.setClickable( false );
        mRBignore.setTextColor( 0xff999999 );
      }

      // mLabel.setText( sb.toString() );

      mBtnOK.setOnClickListener( this );
      mBtnCancel.setOnClickListener( this );

      if ( mBlock != null ) {
        mETfrom.setText( mBlock.mFrom );
        mETto.setText( mBlock.mTo );
        switch ( (int)mBlock.mExtend ) {
          case DistoXDBlock.EXTEND_LEFT:
            mRBleft.setChecked( true );
            break;
          case DistoXDBlock.EXTEND_VERT:
            mRBvert.setChecked( true );
            break;
          case DistoXDBlock.EXTEND_RIGHT:
            mRBright.setChecked( true );
            break;
          case DistoXDBlock.EXTEND_IGNORE:
            mRBignore.setChecked( true );
            break;
        }
      }
      StringBuilder sb = new StringBuilder();
      sb.append( "SHOT  " ).append( mBlock.mFrom ).append( "-" ).append( mBlock.mTo );
      setTitle( sb.toString() );
    }

    public void onClick(View view)
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DrawingShotDialog onClick() " + view.toString() );
      if (view.getId() == R.id.btn_ok ) {
        long extend = mBlock.mExtend;
        if ( mRBleft.isChecked() ) {
          extend = DistoXDBlock.EXTEND_LEFT;
        } else if ( mRBvert.isChecked() ) {
          extend = DistoXDBlock.EXTEND_VERT;
        } else if ( mRBright.isChecked() ) {
          extend = DistoXDBlock.EXTEND_RIGHT;
        } else if ( mRBignore.isChecked() ) {
          extend = DistoXDBlock.EXTEND_IGNORE;
        }
        if ( extend != mBlock.mExtend ) {
          mActivity.updateBlockExtend( mBlock, extend );
        }
        String from = mETfrom.getText().toString().trim();
        String to   = mETto.getText().toString().trim();
        if ( ! from.equals( mBlock.mFrom ) || ! to.equals( mBlock.mTo ) ) {
          mActivity.updateBlockName( mBlock, from, to );
        }
      }
      dismiss();
    }
}
        



/* @file DrawingShotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for a survey shot: editing comment, extend and flag
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
    private TextView mLabel;
    private Button mBtnOK;
    // private Button mBtnCancel;
    private EditText mETfrom;
    private EditText mETto;
    private RadioButton mRBleft;
    private RadioButton mRBvert;
    private RadioButton mRBright;
    private RadioButton mRBignore;
    private RadioButton mRBsurvey;
    private RadioButton mRBduplicate;
    private RadioButton mRBsurface;

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

      mLabel     = (TextView) findViewById(R.id.shot_label);
      mETfrom    = (EditText) findViewById(R.id.shot_from );
      mETto      = (EditText) findViewById(R.id.shot_to );

      mBtnOK     = (Button) findViewById(R.id.btn_ok);
      // mBtnCancel = (Button) findViewById(R.id.btn_cancel);

      mRBleft    = (RadioButton) findViewById( R.id.left );
      mRBvert    = (RadioButton) findViewById( R.id.vert );
      mRBright   = (RadioButton) findViewById( R.id.right );
      mRBignore  = (RadioButton) findViewById( R.id.ignore );

      mRBsurvey    = (RadioButton) findViewById( R.id.survey );
      mRBduplicate = (RadioButton) findViewById( R.id.duplicate );
      mRBsurface   = (RadioButton) findViewById( R.id.surface );

      if ( ! TopoDroidApp.mLoopClosure ) {
        mRBignore.setClickable( false );
        mRBignore.setTextColor( 0xff999999 );
      }

      mLabel.setText( mBlock.dataString() );

      mBtnOK.setOnClickListener( this );
      // mBtnCancel.setOnClickListener( this );

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
        switch ( (int)mBlock.mFlag ) {
          case DistoXDBlock.BLOCK_SURVEY:
            mRBsurvey.setChecked( true );
            break;
          case DistoXDBlock.BLOCK_DUPLICATE:
            mRBduplicate.setChecked( true );
            break;
          case DistoXDBlock.BLOCK_SURFACE:
            mRBsurface.setChecked( true );
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
        long flag   = mBlock.mFlag;

        if ( mRBleft.isChecked() ) {
          extend = DistoXDBlock.EXTEND_LEFT;
        } else if ( mRBvert.isChecked() ) {
          extend = DistoXDBlock.EXTEND_VERT;
        } else if ( mRBright.isChecked() ) {
          extend = DistoXDBlock.EXTEND_RIGHT;
        } else if ( mRBignore.isChecked() ) {
          extend = DistoXDBlock.EXTEND_IGNORE;
        }

        if ( mRBsurvey.isChecked() ) {
          flag = DistoXDBlock.BLOCK_SURVEY;
        } else if ( mRBduplicate.isChecked() ) {
          flag = DistoXDBlock.BLOCK_DUPLICATE;
        } else if ( mRBsurface.isChecked() ) {
          flag = DistoXDBlock.BLOCK_SURFACE;
        }
 
        if ( extend != mBlock.mExtend ) {
          mActivity.updateBlockExtend( mBlock, extend );
        }
        if ( flag != mBlock.mFlag ) {
          mActivity.updateBlockFlag( mBlock,flag );
        }
        String from = mETfrom.getText().toString().trim();
        String to   = mETto.getText().toString().trim();
        if ( ! from.equals( mBlock.mFrom ) || ! to.equals( mBlock.mTo ) ) {
          mActivity.updateBlockName( mBlock, from, to );
        }
      // } else if (view.getId() == R.id.btn_cancel ) {
      }
      dismiss();
    }
}
        



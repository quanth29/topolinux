/* @file ShotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey shot dialog to enter FROM-TO stations etc.
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120702 shot surface flag
 * 20120711 back-next buttons
 * 20120725 TopoDroidApp log
 * 20121118 compare stations of prev shot to increment the "bigger"
 * 20130108 extend "ignore"
 */
package com.android.DistoX;

// import java.Thread;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.RadioButton;

import android.text.InputType;

import android.content.Context;
import android.content.DialogInterface;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import android.view.View.OnKeyListener;
import android.view.KeyEvent;


public class ShotDialog extends Dialog
                              implements View.OnClickListener
{
  private ShotActivity mParent;
  private DistoXDBlock mBlk;
  private DistoXDBlock mPrevBlk;
  private DistoXDBlock mNextBlk;
  private int mPos; // item position in the parent' list

  private Pattern mPattern; // name pattern

  private TextView mTVdata;
  private TextView mTVextra;

  // private EditText mETname;
  private EditText mETfrom;
  private EditText mETto;
  private EditText mETcomment;
  // private CheckBox mRadioDup;
  // private CheckBox mRadioSurf;
  private RadioButton mRadioReg;
  private RadioButton mRadioDup;
  private RadioButton mRadioSurf;
  private CheckBox mCBleg;

  private RadioButton mRBleft;
  private RadioButton mRBvert;
  private RadioButton mRBright;
  private RadioButton mRBignore;
  // private Button   mButtonDrop;
  private Button   mButtonOK;
  private Button   mButtonSave;
  private Button   mButtonBack;
  private Button   mButtonPrev;
  private Button   mButtonNext;

  String shot_from;
  String shot_to;
  boolean shot_leg;
  String shot_data;
  String shot_extra;
  long shot_extend;
  long shot_flag;
  String shot_comment;

  public ShotDialog( Context context, ShotActivity parent, int pos,
                     DistoXDBlock blk, DistoXDBlock prev, DistoXDBlock next )
  {
    super(context);
    mParent = parent;
    mPos = pos;
    loadDBlock( blk, prev, next );
    TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "ShotDialog " + blk.toString(true) );
  }


  private void loadDBlock( DistoXDBlock blk, DistoXDBlock prev, DistoXDBlock next )
  {
    mPrevBlk     = prev;
    mNextBlk     = next;
    mBlk         = blk;
    TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "ShotDialog LOAD " + blk.toString(true) );
    if ( prev != null ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "           prev " + prev.toString(true) );
    }
    if ( next != null ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "           next " + next.toString(true) );
    }

    shot_from    = blk.mFrom;
    shot_to      = blk.mTo;
    if ( blk.mType == DistoXDBlock.BLOCK_BLANK && prev != null && prev.type() == DistoXDBlock.BLOCK_MAIN_LEG ) {
      if ( DistoXStationName.isLessOrEqual( prev.mFrom, prev.mTo ) ) {
        shot_from = prev.mTo;
        shot_to   = DistoXStationName.increment( prev.mTo );
      } else {
        shot_to = prev.mFrom;
        shot_from = DistoXStationName.increment( prev.mFrom );
      }
    }
    
    shot_data    = blk.dataString();
    shot_extra   = blk.extraString();
    shot_extend  = blk.mExtend;
    shot_flag    = blk.mFlag;
    shot_leg     = blk.mType == DistoXDBlock.BLOCK_SEC_LEG;
    shot_comment = blk.mComment;
  }

  private void updateView()
  {
    mTVdata.setText( shot_data );
    mTVextra.setText( shot_extra );
    if ( shot_from.length() > 0 ) {
      mETfrom.setText( shot_from );
    }
    if ( shot_to.length() > 0 ) {
      mETto.setText( shot_to );
    }
    if ( shot_comment != null ) {
      mETcomment.setText( shot_comment );
    } else {
      mETcomment.setText( "" );
    }
   
    if ( shot_flag == DistoXDBlock.BLOCK_SURVEY ) { mRadioReg.setChecked( true ); }
    else if ( shot_flag == DistoXDBlock.BLOCK_DUPLICATE ) { mRadioDup.setChecked( true ); }
    else if ( shot_flag == DistoXDBlock.BLOCK_SURFACE ) { mRadioSurf.setChecked( true ); }

    mCBleg.setChecked( shot_leg );

    if ( shot_extend == DistoXDBlock.EXTEND_LEFT ) { mRBleft.setChecked( true ); }
    else if ( shot_extend == DistoXDBlock.EXTEND_VERT ) { mRBvert.setChecked( true ); }
    else if ( shot_extend == DistoXDBlock.EXTEND_RIGHT ) { mRBright.setChecked( true ); }
    else if ( shot_extend == DistoXDBlock.EXTEND_IGNORE ) { mRBignore.setChecked( true ); }

    mButtonNext.setEnabled( mNextBlk != null );
    mButtonPrev.setEnabled( mPrevBlk != null );
  }


// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    // getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "ShotDialog::onCreate" );
    setContentView(R.layout.shot_dialog);

    mTVdata    = (TextView) findViewById(R.id.shot_data );
    mTVextra   = (TextView) findViewById(R.id.shot_extra );
    // mETname = (EditText) findViewById(R.id.shot_name );
    mETfrom    = (EditText) findViewById(R.id.shot_from );
    mETto      = (EditText) findViewById(R.id.shot_to );
    mETcomment = (EditText) findViewById(R.id.shot_comment );
    // mRadioDup  = (CheckBox) findViewById( R.id.shot_dup );
    // mRadioSurf = (CheckBox) findViewById( R.id.shot_surf );
    mRadioReg  = (RadioButton) findViewById( R.id.shot_reg );
    mRadioDup  = (RadioButton) findViewById( R.id.shot_dup );
    mRadioSurf = (RadioButton) findViewById( R.id.shot_surf );
    mCBleg = (CheckBox)  findViewById(R.id.shot_leg );

    mRBleft   = (RadioButton) findViewById(R.id.left );
    mRBvert   = (RadioButton) findViewById(R.id.vert );
    mRBright  = (RadioButton) findViewById(R.id.right );
    mRBignore = (RadioButton) findViewById(R.id.ignore );

    if ( ! TopoDroidApp.mLoopClosure ) {
      mRBignore.setClickable( false );
      mRBignore.setTextColor( 0xff999999 );
    }

    // mButtonDrop = (Button) findViewById(R.id.btn_drop );
    mButtonSave = (Button) findViewById(R.id.btn_save );
    mButtonOK   = (Button) findViewById(R.id.btn_ok );
    mButtonBack = (Button) findViewById(R.id.btn_back );

    mButtonPrev = (Button) findViewById(R.id.btn_prev );
    mButtonNext = (Button) findViewById(R.id.btn_next );

    // mETfrom.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    // mETfrom.setKeyListener( NumberKeyListener );
    // mETto.setRawInputType( InputType.TYPE_CLASS_NUMBER );

    // mButtonDrop.setOnClickListener( this );
    mButtonSave.setOnClickListener( this );
    mButtonOK.setOnClickListener( this );
    mButtonBack.setOnClickListener( this );

    mButtonPrev.setOnClickListener( this );
    mButtonNext.setOnClickListener( this );

    updateView();
  }

  private void saveDBlock()
  {
    if ( mCBleg.isChecked() ) {
      shot_from = "";
      shot_to = "";
      shot_leg = true;
    } else {
      shot_from = mETfrom.getText().toString();
      shot_from = TopoDroidApp.noSpaces( shot_from );
      // if ( shot_from == null ) { shot_from = ""; }

      shot_to = mETto.getText().toString();
      shot_to = TopoDroidApp.noSpaces( shot_to );
      shot_leg = false;
    }

    shot_flag = DistoXDBlock.BLOCK_SURVEY;
    if ( mRadioReg.isChecked() ) { shot_flag = DistoXDBlock.BLOCK_SURVEY;
    } else if ( mRadioDup.isChecked() ) { shot_flag = DistoXDBlock.BLOCK_DUPLICATE;
    } else if ( mRadioSurf.isChecked() ) { shot_flag = DistoXDBlock.BLOCK_SURFACE;
    }

    shot_extend = mBlk.mExtend;
    if ( mRBleft.isChecked() ) { shot_extend = DistoXDBlock.EXTEND_LEFT; }
    else if ( mRBvert.isChecked() ) { shot_extend = DistoXDBlock.EXTEND_VERT; }
    else if ( mRBright.isChecked() ) { shot_extend = DistoXDBlock.EXTEND_RIGHT; }
    else if ( mRBignore.isChecked() ) { shot_extend = DistoXDBlock.EXTEND_IGNORE; }

    mBlk.setName( shot_from, shot_to );
    mBlk.mFlag = shot_flag;
    mBlk.mExtend = shot_extend;
    if ( shot_leg ) mBlk.mType = DistoXDBlock.BLOCK_SEC_LEG;

    String comment = mETcomment.getText().toString();
    if ( comment != null ) mBlk.mComment = comment;

    mParent.updateShot( shot_from, shot_to, shot_extend, shot_flag, shot_leg, comment, mBlk );
    // mParent.scrollTo( mPos );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "ShotDialog onClick button " + b.getText().toString() );

    if ( b == mButtonOK ) {
      saveDBlock();
      dismiss();
    } else if ( b == mButtonSave ) {
      saveDBlock();
    } else if ( b == mButtonPrev ) {
      // shift:
      //               prev -- blk -- next
      // prevOfPrev -- prev -- blk
      //
      // saveDBlock();
      if ( mPrevBlk != null ) {
        DistoXDBlock prevBlock = mParent.getPreviousLegShot( mPrevBlk, true );
        TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "PREV " + mPrevBlk.toString(true ) );
        loadDBlock( mPrevBlk, prevBlock, mBlk );
        updateView();
      } else {
        TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "PREV is null" );
      }
    } else if ( b == mButtonNext ) {
      // shift:
      //        prev -- blk -- next
      //                blk -- next -- nextOfNext
      // saveDBlock();
      if ( mNextBlk != null ) {
        DistoXDBlock next = mParent.getNextLegShot( mNextBlk, true );
        TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "NEXT " + mNextBlk.toString(true ) );
        loadDBlock( mNextBlk, mBlk, next );
        updateView();
      } else {
        TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "NEXT is null" );
      }
    // } else if ( b == mButtonDrop ) {
    //   mParent.dropShot( mBlk );
    //   dismiss();
    } else if ( b == mButtonBack ) {
      dismiss();
    }
  }

}


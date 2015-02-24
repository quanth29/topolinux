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
package com.topodroid.DistoX;

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
  
  // private RadioButton mRadioReg;
  private CheckBox mRBdup;
  private CheckBox mRBsurf;

  private CheckBox mCBleg;
  private CheckBox mCBall_splay;
  private Button mButtonReverse;

  private CheckBox mRBleft;
  private CheckBox mRBvert;
  private CheckBox mRBright;
  // private RadioButton mRBignore;

  // private Button   mButtonDrop;
  private Button   mButtonOK;
  private Button   mButtonSave;
  // private Button   mButtonBack;
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
    if ( (blk.mType == DistoXDBlock.BLOCK_BLANK || blk.mType == DistoXDBlock.BLOCK_BLANK_LEG) 
         && prev != null && prev.type() == DistoXDBlock.BLOCK_MAIN_LEG ) {
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
   
    // if ( shot_flag == DistoXDBlock.BLOCK_SURVEY ) { mRadioReg.setChecked( true ); }
    if ( shot_flag == DistoXDBlock.BLOCK_DUPLICATE ) { mRBdup.setChecked( true ); }
    else if ( shot_flag == DistoXDBlock.BLOCK_SURFACE ) { mRBsurf.setChecked( true ); }

    mCBleg.setChecked( shot_leg );

    if ( shot_extend == DistoXDBlock.EXTEND_LEFT ) { mRBleft.setChecked( true ); }
    else if ( shot_extend == DistoXDBlock.EXTEND_VERT ) { mRBvert.setChecked( true ); }
    else if ( shot_extend == DistoXDBlock.EXTEND_RIGHT ) { mRBright.setChecked( true ); }
    // else if ( shot_extend == DistoXDBlock.EXTEND_IGNORE ) { mRBignore.setChecked( true ); }

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
    
    // mRadioReg  = (RadioButton) findViewById( R.id.shot_reg );
    mRBdup  = (CheckBox) findViewById( R.id.shot_dup );
    mRBsurf = (CheckBox) findViewById( R.id.shot_surf );

    mCBleg = (CheckBox)  findViewById(R.id.shot_leg );
    mCBall_splay = (CheckBox)  findViewById(R.id.shot_all_splay );
    mButtonReverse = (Button)  findViewById(R.id.shot_reverse );

    mRBleft   = (CheckBox) findViewById(R.id.left );
    mRBvert   = (CheckBox) findViewById(R.id.vert );
    mRBright  = (CheckBox) findViewById(R.id.right );
    // mRBignore = (RadioButton) findViewById(R.id.ignore );

    // if ( ! TopoDroidApp.mLoopClosure ) {
    //   mRBignore.setClickable( false );
    //   mRBignore.setTextColor( 0xff999999 );
    // }

    // mButtonDrop = (Button) findViewById(R.id.btn_drop );
    mButtonSave = (Button) findViewById(R.id.btn_save );
    mButtonOK   = (Button) findViewById(R.id.btn_ok );
    // mButtonBack = (Button) findViewById(R.id.btn_back );

    mButtonPrev = (Button) findViewById(R.id.btn_prev );
    mButtonNext = (Button) findViewById(R.id.btn_next );

    // mETfrom.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    // mETfrom.setKeyListener( NumberKeyListener );
    // mETto.setRawInputType( InputType.TYPE_CLASS_NUMBER );

    // mButtonDrop.setOnClickListener( this );
    mButtonSave.setOnClickListener( this );
    mButtonOK.setOnClickListener( this );
    // mButtonBack.setOnClickListener( this );

    mRBdup.setOnClickListener( this );
    mRBsurf.setOnClickListener( this );

    mButtonPrev.setOnClickListener( this );
    mButtonNext.setOnClickListener( this );
    mButtonReverse.setOnClickListener( this );

    mRBleft.setOnClickListener( this );
    mRBvert.setOnClickListener( this );
    mRBright.setOnClickListener( this );

    updateView();
  }

  private void saveDBlock()
  {
    boolean all_splay = mCBall_splay.isChecked();
    if ( mCBleg.isChecked() ) {
      shot_from = "";
      shot_to = "";
      shot_leg = true;
      all_splay = false;
    } else {
      shot_from = mETfrom.getText().toString();
      shot_from = TopoDroidApp.noSpaces( shot_from );
      // if ( shot_from == null ) { shot_from = ""; }

      shot_to = mETto.getText().toString();
      shot_to = TopoDroidApp.noSpaces( shot_to );
      shot_leg = false;
    }

    shot_flag = DistoXDBlock.BLOCK_SURVEY;
    if ( mRBdup.isChecked() )       { shot_flag = DistoXDBlock.BLOCK_DUPLICATE; }
    else if ( mRBsurf.isChecked() ) { shot_flag = DistoXDBlock.BLOCK_SURFACE; }
    else                               { shot_flag = DistoXDBlock.BLOCK_SURVEY; }

    shot_extend = mBlk.mExtend;
    if ( mRBleft.isChecked() )       { shot_extend = DistoXDBlock.EXTEND_LEFT; }
    else if ( mRBvert.isChecked() )  { shot_extend = DistoXDBlock.EXTEND_VERT; }
    else if ( mRBright.isChecked() ) { shot_extend = DistoXDBlock.EXTEND_RIGHT; }
    else                             { shot_extend = DistoXDBlock.EXTEND_IGNORE; }

    mBlk.mFlag = shot_flag;
    mBlk.mExtend = shot_extend;
    if ( shot_leg ) mBlk.mType = DistoXDBlock.BLOCK_SEC_LEG;

    String comment = mETcomment.getText().toString();
    if ( comment != null ) mBlk.mComment = comment;

    if ( shot_from.length() > 0 && shot_to.length() > 0 ) {
      all_splay = false;
    }
    if ( all_splay ) {
      mParent.updateSplayShots( shot_from, shot_to, shot_extend, shot_flag, shot_leg, comment, mBlk );
    } else {
      // mBlk.setName( shot_from, shot_to ); // done by parent.updateShot
      mParent.updateShot( shot_from, shot_to, shot_extend, shot_flag, shot_leg, comment, mBlk );
    }
    // mParent.scrollTo( mPos );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "ShotDialog onClick button " + b.getText().toString() );

    if ( b == mRBleft ) {
      mRBvert.setChecked( false );
      mRBright.setChecked( false );
    } else if ( b == mRBvert ) {
      mRBleft.setChecked( false );
      mRBright.setChecked( false );
    } else if ( b == mRBright ) {
      mRBleft.setChecked( false );
      mRBvert.setChecked( false );

    } else if ( b == mRBdup ) {
      mRBsurf.setChecked( false );
    } else if ( b == mRBsurf ) {
      mRBdup.setChecked( false );


    } else if ( b == mButtonOK ) {
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
    } else if ( b == mButtonReverse ) {
      shot_from = mETfrom.getText().toString();
      shot_from = TopoDroidApp.noSpaces( shot_from );
      shot_to = mETto.getText().toString();
      shot_to = TopoDroidApp.noSpaces( shot_to );
      if ( shot_to.length() > 0 && shot_from.length() > 0 ) {
        String temp = new String( shot_from );
        shot_from = shot_to;
        shot_to = temp;
        mETfrom.setText( shot_from );
        mETto.setText( shot_to );
      }
    // } else if ( b == mButtonDrop ) {
    //   mParent.dropShot( mBlk );
    //   dismiss();
    // } else if ( b == mButtonBack ) {
    //   dismiss();
    }
  }

}

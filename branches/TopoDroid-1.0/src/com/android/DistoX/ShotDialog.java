/* @file ShotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey shot dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120702 shot surface flag
 * 20120711 back-next buttons
 * 20120725 TopoDroidApp log
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
import android.view.View.OnKeyListener;
import android.view.KeyEvent;


public class ShotDialog extends Dialog
                              implements View.OnClickListener
{
  // private static final String TAG = "DistoX";
  private ShotActivity mParent;
  private DistoXDBlock mBlk;
  private DistoXDBlock mPrevBlk;
  private DistoXDBlock mNextBlk;

  private Pattern mPattern; // name pattern

  private TextView mTVdata;
  // private EditText mETname;
  private EditText mETfrom;
  private EditText mETto;
  private EditText mETcomment;
  private CheckBox mRadioDup;
  private CheckBox mRadioSurf;

  private RadioButton mRadioLeft;
  private RadioButton mRadioVert;
  private RadioButton mRadioRight;
  private Button   mButtonDrop;
  private Button   mButtonOK;
  private Button   mButtonSave;
  private Button   mButtonBack;
  private Button   mButtonPrev;
  private Button   mButtonNext;

  String shot_from;
  String shot_to;
  String shot_data;
  long shot_extend;
  long shot_flag;
  String shot_comment;

  public ShotDialog( Context context, ShotActivity parent,
                     DistoXDBlock blk, DistoXDBlock prev, DistoXDBlock next )
  {
    super(context);
    mParent      = parent;
    loadDBlock( blk, prev, next );
    TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "ShotDialog " + blk.toString() );
  }


  private void loadDBlock( DistoXDBlock blk, DistoXDBlock prev, DistoXDBlock next )
  {
    mPrevBlk     = prev;
    mNextBlk     = next;
    mBlk         = blk;
    TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "ShotDialog LOAD " + blk.toString() );
    if ( prev != null ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "           prev " + prev.toString() );
    }
    if ( next != null ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "           next " + next.toString() );
    }

    shot_from    = blk.mFrom;
    shot_to      = blk.mTo;
    if ( blk.type() == DistoXDBlock.BLOCK_BLANK && prev != null && prev.type() == DistoXDBlock.BLOCK_CENTERLINE ) {
      shot_from = prev.mTo;
      shot_to   = DistoXStationName.increment( prev.mTo );
    }
    
    shot_data    = blk.dataString();
    shot_extend  = blk.mExtend;
    shot_flag    = blk.mFlag;
    shot_comment = blk.mComment;
  }

  private void updateView()
  {
    mTVdata.setText( shot_data );
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
    
    mRadioDup.setChecked( shot_flag == DistoXDBlock.BLOCK_DUPLICATE );
    mRadioSurf.setChecked( shot_flag == DistoXDBlock.BLOCK_SURFACE );

    if ( shot_extend == -1 ) { mRadioLeft.setChecked( true ); }
    else if ( shot_extend == 0 ) { mRadioVert.setChecked( true ); }
    else { mRadioRight.setChecked( true ); }

    mButtonNext.setEnabled( mNextBlk != null );
    mButtonPrev.setEnabled( mPrevBlk != null );
  }


// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "ShotDialog::onCreate" );
    setContentView(R.layout.distox_shot_dialog);
    mTVdata    = (TextView) findViewById(R.id.shot_data );
    // mETname = (EditText) findViewById(R.id.shot_name );
    mETfrom    = (EditText) findViewById(R.id.shot_from );
    mETto      = (EditText) findViewById(R.id.shot_to );
    mETcomment = (EditText) findViewById(R.id.shot_comment );
    mRadioDup  = (CheckBox) findViewById( R.id.shot_dup );
    mRadioSurf = (CheckBox) findViewById( R.id.shot_surf );

    mRadioLeft  = (RadioButton) findViewById(R.id.radio_left );
    mRadioVert  = (RadioButton) findViewById(R.id.radio_vert );
    mRadioRight = (RadioButton) findViewById(R.id.radio_right );

    mButtonDrop = (Button) findViewById(R.id.button_drop );
    mButtonSave = (Button) findViewById(R.id.button_save );
    mButtonOK   = (Button) findViewById(R.id.button_ok );
    mButtonBack = (Button) findViewById(R.id.button_back );

    mButtonPrev = (Button) findViewById(R.id.button_prev );
    mButtonNext = (Button) findViewById(R.id.button_next );

    mETfrom.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    // mETfrom.setKeyListener( NumberKeyListener );
    mETto.setRawInputType( InputType.TYPE_CLASS_NUMBER );

    mButtonDrop.setOnClickListener( this );
    mButtonSave.setOnClickListener( this );
    mButtonOK.setOnClickListener( this );
    mButtonBack.setOnClickListener( this );

    mButtonPrev.setOnClickListener( this );
    mButtonNext.setOnClickListener( this );

    updateView();
  }

  private void saveDBlock()
  {
    shot_from = mETfrom.getText().toString();
    shot_from = TopoDroidApp.noSpaces( shot_from );
    if ( shot_from == null ) {
      shot_from = "";
    }

    shot_to = mETto.getText().toString();
    shot_to = TopoDroidApp.noSpaces( shot_to );

    shot_flag = DistoXDBlock.BLOCK_SURVEY;
    if ( mRadioDup.isChecked() ) {
      shot_flag = DistoXDBlock.BLOCK_DUPLICATE;
    } else if ( mRadioSurf.isChecked() ) {
      shot_flag = DistoXDBlock.BLOCK_SURFACE;
    }

    shot_extend = 1;
    if ( mRadioLeft.isChecked() ) { shot_extend = -1; }
    else if ( mRadioVert.isChecked() ) { shot_extend = 0; }

    mBlk.setName( shot_from, shot_to );
    mBlk.mFlag = shot_flag;
    mBlk.mExtend = shot_extend;
    String comment = mETcomment.getText().toString();
    if ( comment != null ) mBlk.mComment = comment;

    mParent.updateShot( shot_from, shot_to, shot_extend, shot_flag, comment, mBlk );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
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
        TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "PREV " + mPrevBlk.toString() );
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
        TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "NEXT " + mNextBlk.toString() );
        loadDBlock( mNextBlk, mBlk, next );
        updateView();
      } else {
        TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "NEXT is null" );
      }
    } else if ( b == mButtonDrop ) {
      mParent.dropShot( mBlk );
      dismiss();
    } else if ( b == mButtonBack ) {
      dismiss();
    }
  }

}


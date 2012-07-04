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
 */
package com.android.DistoX;

// import java.Thread;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.RadioButton;

// import android.util.Log;
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
  // private static final String TAG = "DistoX ShotDialog";
  private ShotActivity mParent;
  private DistoXDBlock mBlk;

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
  private Button   mButtonCancel;

  String shot_from;
  String shot_to;
  String shot_data;
  long shot_extend;
  long shot_flag;
  String shot_comment;

  public ShotDialog( Context context, ShotActivity distox,
                     DistoXDBlock blk, String data, DistoXDBlock prev )
  {
    super(context);
    mParent      = distox;
    mBlk         = blk;

    shot_from    = blk.mFrom;
    shot_to      = blk.mTo;
    if ( blk.type() == DistoXDBlock.BLOCK_BLANK && prev != null ) {
      shot_from = prev.mTo;
      shot_to   = DistoXStationName.increment( prev.mTo );
    }
    
    shot_data    = data;
    shot_extend  = blk.mExtend;
    shot_flag    = blk.mFlag;
    shot_comment = blk.mComment;


  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // Log.v( TAG, "onCreate" );
    setContentView(R.layout.distox_shot_dialog);
    mTVdata    = (TextView) findViewById(R.id.shot_data );
    // mETname = (EditText) findViewById(R.id.shot_name );
    mETfrom    = (EditText) findViewById(R.id.shot_from );
    mETto      = (EditText) findViewById(R.id.shot_to );
    mETcomment = (EditText) findViewById(R.id.shot_comment );
    mRadioDup  = (CheckBox) findViewById( R.id.shot_dup );
    mRadioSurf = (CheckBox) findViewById( R.id.shot_surf );

    mButtonDrop   = (Button) findViewById(R.id.button_drop_shot_name );
    mButtonOK     = (Button) findViewById(R.id.button_ok_shot_name );
    mButtonCancel = (Button) findViewById(R.id.button_cancel_shot_name );

    mETfrom.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    // mETfrom.setKeyListener( NumberKeyListener );
    mETto.setRawInputType( InputType.TYPE_CLASS_NUMBER );

    mTVdata.setText( shot_data );
    if ( shot_from.length() > 0 ) mETfrom.setText( shot_from );
    if ( shot_to.length() > 0 )   mETto.setText( shot_to );
    if ( shot_comment != null )   mETcomment.setText( shot_comment );
    
    if ( shot_flag == DistoXDBlock.BLOCK_DUPLICATE ) {
      mRadioDup.setChecked( true );
    } else if ( shot_flag == DistoXDBlock.BLOCK_SURFACE ) {
      mRadioSurf.setChecked( true );
    }

    mRadioLeft  = (RadioButton) findViewById(R.id.radio_left );
    mRadioVert  = (RadioButton) findViewById(R.id.radio_vert );
    mRadioRight = (RadioButton) findViewById(R.id.radio_right );
    if ( shot_extend == -1 ) { mRadioLeft.setChecked( true ); }
    else if ( shot_extend == 0 ) { mRadioVert.setChecked( true ); }
    else { mRadioRight.setChecked( true ); }

    mButtonDrop.setOnClickListener( this );
    mButtonOK.setOnClickListener( this );
    mButtonCancel.setOnClickListener( this );

  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( b == mButtonOK ) {
      shot_from = mETfrom.getText().toString();
      shot_to = mETto.getText().toString();
      shot_from.trim();
      shot_to.trim();
      if ( shot_from == null ) { shot_from = ""; }

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

      mParent.updateShot( shot_from, shot_to, shot_extend, shot_flag, comment );
    } else if ( b == mButtonDrop ) {
      mParent.dropShot();
    }
    dismiss();
  }

}


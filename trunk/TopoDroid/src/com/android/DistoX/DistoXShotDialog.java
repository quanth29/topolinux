/* @file DistoXShotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey shot dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

// import java.Thread;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.RadioButton;

import android.util.Log;
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


public class DistoXShotDialog extends Dialog
                              implements View.OnClickListener
{
  private static final String TAG = "DistoXShotDialog";
  private DistoX mDistoX;
  private DistoXDBlock mBlk;

  private Pattern mPattern; // name pattern

  private TextView mTVdata;
  // private EditText mETname;
  private EditText mETfrom;
  private EditText mETto;
  private EditText mETcomment;
  private CheckBox    mCBflag;
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

  private static char[] lc = {
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' 
  };
  private static char[] uc = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' 
  };

  private String increment( String name )
  {
    // if name is numeric
    if ( name != null && name.length() > 0 ) {
      int len = name.length();
      if ( len > 0 ) {
        char ch = name.charAt( len - 1 );
        int k = Character.getNumericValue( ch );
        if ( k >= 10 && k < 35 ) {
          k -= 9; // - 10 + 1
          return name.substring( 0, name.length() - 1 ) + 
           ( Character.isLowerCase( ch )? lc[k] : uc[k] );
        } else if ( k >= 0 && k < 10 ) {
          String digits = name.replaceAll( "\\D*", "" );
          int h = Integer.valueOf( digits ) + 1;
          String ret = name.replaceAll( "\\d+", Integer.toString(h) );
          Log.v( TAG, "digits " + digits + " h " + h + " ret " + ret );
          return ret;
        }
      }
    }
    return "";
  }

  public DistoXShotDialog( Context context, DistoX distox, DistoXDBlock blk, String data, DistoXDBlock prev )
                           // String from, String to, String data, long extend, long flag, String comment )
  {
    super(context);
    mDistoX      = distox;
    mBlk         = blk;

    shot_from    = blk.mFrom;
    shot_to      = blk.mTo;
    if ( blk.type() == DistoXDBlock.BLOCK_BLANK && prev != null ) {
      shot_from = prev.mTo;
      shot_to   = increment( prev.mTo );
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
    mCBflag    = (CheckBox) findViewById(R.id.shot_flag );

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
    
    mCBflag.setChecked( shot_flag != 0 );
    mCBflag.setText( "Duplicate" );

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

      shot_flag   = mCBflag.isChecked()? DistoXDBlock.BLOCK_DUPLICATE : DistoXDBlock.BLOCK_SURVEY;
      shot_extend = 1;
      if ( mRadioLeft.isChecked() ) { shot_extend = -1; }
      else if ( mRadioVert.isChecked() ) { shot_extend = 0; }

      mBlk.setName( shot_from, shot_to );
      mBlk.mFlag = shot_flag;
      mBlk.mExtend = shot_extend;
      String comment = mETcomment.getText().toString();
      if ( comment != null ) mBlk.mComment = comment;

      mDistoX.updateShot( shot_from, shot_to, shot_extend, shot_flag, comment );
    } else if ( b == mButtonDrop ) {
      mDistoX.dropShot();
    }
    dismiss();
  }

}


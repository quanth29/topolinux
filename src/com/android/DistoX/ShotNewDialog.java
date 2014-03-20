/* @file ShotNewDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid dialog for a new (manually entered) shot
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130621 set hint for FROM field
 * 20130910 added "at" field
 * 20131022 dismiss only to the "close" button
 * 20140220 N.B. makeNewShot keeps into account current units
 * 20140221 auto update of stations names: three buttons, OK, Save, Back
 */
package com.android.DistoX;

// import java.Thread;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.RadioButton;

import android.content.Context;
import android.text.InputType;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;

// import android.util.Log;


public class ShotNewDialog extends Dialog
                           implements View.OnClickListener
{
  // private ShotActivity mParent;
  private TopoDroidApp mApp;
  private ILister mLister;
  private DistoXDBlock mPrevBlk;
  private boolean  notDone;
  private long mAt; // id of the shot where to add new shot (-1 end)

  private EditText mETfrom;
  private EditText mETto;
  private EditText mETdistance;
  private EditText mETbearing;
  private EditText mETclino;
  private RadioButton mRadioLeft;
  private RadioButton mRadioVert;
  private RadioButton mRadioRight;
  private EditText mETleft;
  private EditText mETright;
  private EditText mETup;
  private EditText mETdown;
  private Button   mBtnOk;
  private Button   mBtnSave;
  private Button   mBtnBack;

  public ShotNewDialog( Context context, TopoDroidApp app, ILister lister, DistoXDBlock last_blk, long at )
  {
    super( context );
    mApp  = app;
    mLister  = lister;
    mPrevBlk = last_blk;
    notDone  = true;
    mAt      = at;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "ShotNewDialog onCreate" );
    setContentView(R.layout.shot_new_dialog);
    mETfrom = (EditText) findViewById(R.id.shot_from );
    mETto   = (EditText) findViewById(R.id.shot_to );
    mETdistance = (EditText) findViewById(R.id.shot_distance );
    mETbearing  = (EditText) findViewById(R.id.shot_bearing );
    mETclino    = (EditText) findViewById(R.id.shot_clino );
    mETleft     = (EditText) findViewById(R.id.shot_left );
    mETright    = (EditText) findViewById(R.id.shot_right );
    mETup       = (EditText) findViewById(R.id.shot_up );
    mETdown     = (EditText) findViewById(R.id.shot_down );

    mETfrom.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    mETto.setRawInputType( InputType.TYPE_CLASS_NUMBER );

    if ( mPrevBlk != null ) {
      mETfrom.setHint( mPrevBlk.mTo );
      mETto.setHint( DistoXStationName.increment( mPrevBlk.mTo ) );
    }

    mBtnOk    = (Button) findViewById(R.id.button_ok_shot_name );
    mBtnSave  = (Button) findViewById(R.id.button_save_shot_name );
    mBtnBack  = (Button) findViewById(R.id.button_back_shot_name );

    mRadioLeft  = (RadioButton) findViewById(R.id.radio_left );
    mRadioVert  = (RadioButton) findViewById(R.id.radio_vert );
    mRadioRight = (RadioButton) findViewById(R.id.radio_right );

    mBtnOk.setOnClickListener( this );
    mBtnSave.setOnClickListener( this );
    mBtnBack.setOnClickListener( this );

    setTitle( R.string.shot_info );
  }

  private void resetData( String from )
  {
    String to = DistoXStationName.increment( from );
    mETfrom.setText( from );
    mETto.setText(to);
    mETdistance.setText("");
    mETbearing.setText("");
    mETclino.setText("");
    mETleft.setText("");
    mETright.setText("");
    mETup.setText("");
    mETdown.setText("");
  }

  // FIXME synchronized ?
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "ShotNewDialog onClick button " + b.getText().toString() );

    if ( b == mBtnOk || b == mBtnSave ) {
      if ( notDone && mETfrom.getText() != null ) {
        notDone = false;
        String shot_from = mETfrom.getText().toString();
        if ( shot_from == null || shot_from.length() == 0 ) {
          shot_from = mETfrom.getHint().toString();
        }
        String shot_to   = mETto.getText().toString();
        if ( shot_to == null || shot_to.length() == 0 ) {
          shot_to = mETto.getHint().toString();
        }

        if ( shot_to == null ) {
          shot_to = "";
        } else {
          shot_to = TopoDroidApp.noSpaces( shot_to );
        }
        if ( shot_from != null ) {
          shot_from = TopoDroidApp.noSpaces( shot_from );
          if ( shot_from.length() > 0 ) { 
            String distance = mETdistance.getText().toString();
            String bearing  = mETbearing.getText().toString();
            String clino    = mETclino.getText().toString();
            if ( distance != null && bearing != null && clino != null ) {
              distance = distance.trim();
              bearing  = bearing.trim();
              clino    = clino.trim();
              if ( distance.length() > 0  && bearing.length() > 0 && clino.length() > 0 ) {
                // Log.v( TopoDroidApp.TAG, "data " + distance + " " + bearing + " " + clino );
                long shot_extend = 1;
                if ( mRadioLeft.isChecked() ) { shot_extend = -1; }
                else if ( mRadioVert.isChecked() ) { shot_extend = 0; }
                if ( shot_to.length() > 0 ) {
                  mApp.makeNewShot( mAt, shot_from, shot_to,
                                     Float.parseFloat(distance),
                                     Float.parseFloat(bearing),
                                     Float.parseFloat(clino),
                                     shot_extend,
                                     mETleft.getText().toString(),
                                     mETright.getText().toString(),
                                     mETup.getText().toString(),
                                     mETdown.getText().toString() );
                } else {
                  mApp.makeNewShot( mAt, shot_from, shot_to,
                                     Float.parseFloat(distance),
                                     Float.parseFloat(bearing),
                                     Float.parseFloat(clino),
                                     shot_extend,
                                     null, null, null, null );
                }
                resetData( shot_to );
                mLister.refreshDisplay( 1, false );
                notDone = true;
              }
            }
          }
        }
      }
      if ( b == mBtnOk ) {
        dismiss();
      }
    } else if ( b == mBtnBack ) {
      dismiss();
    }
  }
}


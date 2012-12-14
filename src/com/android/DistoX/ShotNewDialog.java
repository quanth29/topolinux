/* @file ShotNewDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid dialog for a new shot
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
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


public class ShotNewDialog extends Dialog
                           implements View.OnClickListener
{
  private ShotActivity mParent;
  private boolean  notDone;

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
  private Button   mButtonOK;
  private Button   mButtonCancel;

  public ShotNewDialog( Context context, ShotActivity parent )
  {
    super( context );
    mParent = parent;
    notDone = true;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "ShotNewDialog onCreate" );
    setContentView(R.layout.distox_shot_new_dialog);
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

    mButtonOK     = (Button) findViewById(R.id.button_ok_shot_name );
    mButtonCancel = (Button) findViewById(R.id.button_cancel_shot_name );

    mRadioLeft  = (RadioButton) findViewById(R.id.radio_left );
    mRadioVert  = (RadioButton) findViewById(R.id.radio_vert );
    mRadioRight = (RadioButton) findViewById(R.id.radio_right );

    mButtonOK.setOnClickListener( this );
    mButtonCancel.setOnClickListener( this );
  }

  // FIXME synchronized ?
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "ShotNewDialog onClick button " + b.getText().toString() );

    if ( notDone && b == mButtonOK ) {
      notDone = false;
      String shot_from = mETfrom.getText().toString();
      String shot_to   = mETto.getText().toString();
      if ( shot_from != null && shot_to != null ) {
        shot_from = TopoDroidApp.noSpaces( shot_from );
        shot_to   = TopoDroidApp.noSpaces( shot_to );
        if ( shot_from.length() > 0 && shot_to.length() > 0 ) { 
          String distance = mETdistance.getText().toString();
          String bearing  = mETbearing.getText().toString();
          String clino    = mETclino.getText().toString();
          if ( distance != null && bearing != null && clino != null ) {
            distance = distance.trim();
            bearing  = bearing.trim();
            clino    = clino.trim();
            if ( distance.length() > 0  && bearing.length() > 0 && clino.length() > 0 ) {
              long shot_extend = 1;
              if ( mRadioLeft.isChecked() ) { shot_extend = -1; }
              else if ( mRadioVert.isChecked() ) { shot_extend = 0; }
              mParent.makeNewShot( shot_from, shot_to,
                                   Float.parseFloat(distance),
                                   Float.parseFloat(bearing),
                                   Float.parseFloat(clino),
                                   shot_extend,
                                   mETleft.getText().toString(),
                                   mETright.getText().toString(),
                                   mETup.getText().toString(),
                                   mETdown.getText().toString() );
            }
          }
        }
      }
    }
    dismiss();
  }
}


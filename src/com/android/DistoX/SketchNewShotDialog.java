/** @file SketchNewShotDialog.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid sketch save dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130326 created
 */
package com.android.DistoX;

import java.util.ArrayList;

import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;

import android.content.Context;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;


public class SketchNewShotDialog extends Dialog
                                 implements View.OnClickListener
{
  private Button   mBtnOk;
  private Button   mBtnCancel;
  private CheckBox mCBsplay;
  private EditText mETlength;
  private EditText mETazimuth;
  private EditText mETclino;

  private SketchActivity mParent;
  private DataHelper     mData;
  private ShotActivity   mShots;
  private TopoDroidApp   mApp;
  String mFrom;
  boolean manual_shot;
  DistoXDBlock mBlk;

  SketchNewShotDialog( Context context, SketchActivity parent, TopoDroidApp app, String name )
  {
    super( context );
    mParent = parent;
    mApp    = app;
    mData   = app.mData;
    mShots  = app.mShotActivity;
    mFrom   = name;
    mBlk    = null;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.sketch_new_shot_dialog);
    mBtnOk     = (Button) findViewById(R.id.btn_ok );
    mBtnCancel = (Button) findViewById(R.id.btn_cancel );
    mCBsplay   = (CheckBox) findViewById(R.id.cb_splay );
    mETlength  = (EditText) findViewById(R.id.et_length );
    mETazimuth = (EditText) findViewById(R.id.et_azimuth );
    mETclino   = (EditText) findViewById(R.id.et_clino );

    TextView station = (TextView) findViewById(R.id.tv_station );
    station.setText("at station " + mFrom );

    mBlk = mShots.getNextBlankLegShot( null );
    if ( mBlk != null ) {
      mETlength.setText(  Float.toString( mBlk.mLength ) );
      mETazimuth.setText( Float.toString( mBlk.mBearing ) );
      mETclino.setText(   Float.toString( mBlk.mClino ) );
      mETlength.setEnabled( false );
      mETazimuth.setEnabled( false );
      mETclino.setEnabled( false );
    }
    mBtnOk.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );

    setTitle( R.string.title_sketch_shot );
  }

  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    boolean splay = mCBsplay.isChecked();
    if ( b == mBtnOk ) {
      ArrayList<DistoXDBlock> updateList = null;
      String mTo = "";
      if ( ! splay ) {
        mTo = mData.getNextStationName( mApp.mSID );
      }
      if ( mBlk == null ) {
        float len = Float.parseFloat( mETlength.getText().toString() );
        float ber = Float.parseFloat( mETazimuth.getText().toString() );
        float cln = Float.parseFloat( mETclino.getText().toString() );
        // append a new shot
        DistoXDBlock blk = mShots.makeNewShot( mFrom, mTo, len, ber, cln, 1, null, null, null, null );
        updateList = new ArrayList<DistoXDBlock>();
        updateList.add( blk );
      } else {
        // set stations to mBlk
        // mBlk.setName( mFrom, mTo ); // FIXME
        mShots.updateShot( mFrom, mTo, 1, 0, false, null, mBlk ); // null comment ?
        mBlk.setName( mFrom, mTo ); // reset block name/type
        if ( ! splay ) {
          updateList = mShots.numberSplays();
        } else {
          updateList = new ArrayList<DistoXDBlock>();
        }
        updateList.add( mBlk );
      }
      // TODO mParent update Num
      // mParent.recreateNum( mData.selectAllShots( mApp.mSID, 0 ) );
      mParent.updateNum( updateList );
    }
    dismiss();
  }

}



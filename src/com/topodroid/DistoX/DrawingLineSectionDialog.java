/** @file DrawingLineSectionDialog.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid sketch line section dialog 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20140328 dropped reversed
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.File;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;

import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class DrawingLineSectionDialog extends Dialog
                               implements View.OnClickListener
{
  private DrawingLinePath mLine;
  private DrawingActivity mParent;
  private TopoDroidApp    mApp;

  // private TextView mTVtype;
  private TextView mTVoptions;
  // private CheckBox mReversed;
  String mId;
  PlotInfo mPlotInfo;
  String  mFrom;
  String  mTo;
  boolean mVert;
  float   mAzimuth;

  private Button   mBtnFoto;
  private Button   mBtnDraw;
  private Button   mBtnErase;
  private ImageView mIVimage;   // photo image

  public DrawingLineSectionDialog( DrawingActivity context, TopoDroidApp app,
                                   DrawingLinePath line, String from, String to, boolean vert, float azimuth )
  {
    super( context );
    mParent = context;
    mApp  = app;
    mLine = line;
    mId   = null;
    mFrom = from;
    mTo   = to;
    mVert = vert;
    mAzimuth = azimuth;
    if ( mLine.mOptions != null ) {
      String[] vals = mLine.mOptions.split(" ");
      for (int k = 0; k<vals.length - 1; ++k ) {
        if ( vals[k].equals( "-id" ) ) {
          mId = vals[k+1];
          break;
        }
      }
    } 
    if ( mId == null ) {
      mId = TopoDroidApp.mData.getNextSectionId( mParent.getSID() );
      mLine.mOptions = "-id " + mId;
      mPlotInfo = null;
    } else {
      mPlotInfo = TopoDroidApp.mData.getPlotInfo( mParent.getSID(), mId );
      mFrom = mPlotInfo.start;
      mTo   = mPlotInfo.view;
      mVert = mPlotInfo.type == PlotInfo.PLOT_SECTION;
      mAzimuth = mPlotInfo.azimuth;
    }
    // Log.v( TopoDroidApp.TAG, "line id " + mId );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_line_section_dialog);

    setTitle( String.format( mParent.getResources().getString( R.string.title_draw_line ),
              DrawingBrushPaths.getLineThName( mLine.mLineType ) ) );

    mTVoptions = (TextView) findViewById( R.id.line_options );
    mTVoptions.setText( "ID " + mId );

    mIVimage      = (ImageView) findViewById( R.id.line_image );

    // mReversed = (CheckBox) findViewById( R.id.line_reversed );
    // mReversed.setChecked( mLine.mReversed );

    mBtnFoto = (Button) findViewById( R.id.button_foto );
    mBtnDraw = (Button) findViewById( R.id.button_draw );

    mBtnDraw.setOnClickListener( this );
    mBtnFoto.setOnClickListener( this );
    if ( mPlotInfo != null ) { // check the photo
      String filename = mApp.getSurveyJpgFile( mPlotInfo.name );
      File imagefile = new File( filename );
      if ( imagefile.exists() ) {
        Bitmap image = BitmapFactory.decodeFile( filename );
        if ( image != null ) {
          int w2 = image.getWidth() / 8;
          int h2 = image.getHeight() / 8;
          Bitmap image2 = Bitmap.createScaledBitmap( image, w2, h2, true );
          mIVimage.setImageBitmap( image2 );
          // mIVimage.setHeight( h2 );
          // mIVimage.setWidth( w2 );
        }
        // mBtnFoto.setBackgroundResource( R.drawable.ic_camera_no );
      }
    }

    mBtnErase = (Button) findViewById( R.id.button_erase );
    mBtnErase.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DrawingLineSectionDialog onClick() " + b.getText().toString() );

    // mLine.setReversed( mReversed.isChecked() );

    if ( b == mBtnFoto ) {
      mParent.makeSectionPhoto( mLine, mId );
    } else if ( b == mBtnDraw ) {
      mParent.makeSectionDraw( mLine, mId, mFrom, mTo, mVert, mAzimuth );
    } else if ( b == mBtnErase ) {
      mParent.deleteLine( mLine );
    }
    dismiss();
  }

}


/* @file PhotoEditDialog.java
 *
 * @author marco corvi
 * @date july 2012
 *
 * @brief TopoDroid photo edit dialog 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

// import java.Thread;

import android.app.Dialog;
import android.os.Bundle;

import android.util.Log;

import android.content.Intent;
import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PhotoEditDialog extends Dialog
                             implements View.OnClickListener
{
  private static final String TAG = "DistoX";
  private PhotoActivity mParent;
  private PhotoInfo mPhoto;
  private String mFilename;

  private EditText mETtitle;    // photo title / photo name
  private EditText mETcomment;  // photo comment
  private ImageView mIVimage;   // photo image
  private Button   mButtonOK;
  private Button   mButtonCancel;

  /**
   * @param context   context
   */
  PhotoEditDialog( Context context, PhotoActivity parent, PhotoInfo photo, String filename )
  {
    super( context );
    mParent = parent;
    mPhoto  = photo;
    mFilename = filename;
    Log.v(TAG, "PhotoEditDialog " + mFilename);
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // Log.v( TAG, "onCreate" );
    setContentView(R.layout.photo_dialog);

    mIVimage      = (ImageView) findViewById( R.id.photo_image );
    mETtitle      = (EditText) findViewById( R.id.photo_title );
    mETcomment    = (EditText) findViewById( R.id.photo_comment );
    mButtonOK     = (Button) findViewById( R.id.photo_ok );
    mButtonCancel = (Button) findViewById( R.id.photo_cancel );

    setTitle( R.string.title_photo );
    mETtitle.setText( mPhoto.mTitle );
    // public String mPhoto.mDate;
    if ( mPhoto.mComment != null ) {
      mETcomment.setText( mPhoto.mComment );
    }
    
    // public String getSurveyJpgFile( String name )
    // public String name;    // photo filename without extension ".jpg" and survey prefix dir
    // String filename = TopoDroidApp.APP_FOTO_PATH + mPhoto.name + ".jpg";
    Bitmap image = BitmapFactory.decodeFile( mFilename );
    if ( image != null ) {
      int w2 = image.getWidth() / 8;
      int h2 = image.getHeight() / 8;
      Bitmap image2 = Bitmap.createScaledBitmap( image, w2, h2, true );
      mIVimage.setImageBitmap( image2 );
      // mIVimage.setHeight( h2 );
      // mIVimage.setWidth( w2 );
    }

    mButtonOK.setOnClickListener( this );
    mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // Log.v(TAG, "text " + mETtitle.getText().toString() );
    if ( b == mButtonOK && mETtitle.getText() != null ) {
      if ( mETcomment.getText() == null ) {
        mParent.updatePhoto( mPhoto, mETtitle.getText().toString(), "" );
      } else {
        mParent.updatePhoto( mPhoto, mETtitle.getText().toString(), mETcomment.getText().toString() );
      }
    }
    dismiss();
  }

}


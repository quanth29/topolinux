/* @file PhotoActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey shots management
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120520 created from DistoX.java
 * 20120531 implemented photo and 3D
 * 20120531 shot-numbering bugfix
 * 20120606 3D: implied therion export before 3D
 */
package com.android.DistoX;

import java.io.File;
import java.io.IOException;
// import java.io.EOFException;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.app.Application;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
// import android.view.MenuInflater;
// import android.content.res.ColorStateList;

// import android.util.Log;

// import android.location.LocationManager;

import android.content.Context;
import android.content.Intent;

// import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Dialog;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.preference.PreferenceManager;

import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;

public class PhotoActivity extends Activity
                          implements OnItemClickListener, ILister
{
  // private static final String TAG = "DistoX";
  private TopoDroidApp app;
  // FIXME PHOTO
  // private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

  private ListView mList;
  // private int mListPos = -1;
  // private int mListTop = 0;
  private PhotoAdapter   mDataAdapter;
  private long mShotId = -1;   // id of the shot

  private String mSaveData = "";
  private TextView mSaveTextView = null;
  private PhotoInfo mSavePhoto = null;

  String mPhotoStation;
  String mPhotoComment;
  long   mPhotoId;

  // -------------------------------------------------------------------
  @Override
  public void refreshDisplay( int nr )
  {
    updateDisplay();
  }

  public void updateDisplay( )
  {
    // Log.v( TAG, "updateDisplay() status: " + StatusName() + " forcing: " + force_update );
    DataHelper data = app.mData;
    if ( data != null && app.mSID >= 0 ) {
      List< PhotoInfo > list = data.selectAllPhotos( app.mSID, TopoDroidApp.STATUS_NORMAL );
      // Log.v( TAG, "update shot list size " + list.size() );
      updatePhotoList( list );
      setTitle( app.getSurvey() );
    } else {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
    }
  }

  private void updatePhotoList( List< PhotoInfo > list )
  {
    // Log.v(TAG, "updatePhotoList size " + list.size() );
    mDataAdapter.clear();
    mList.setAdapter( mDataAdapter );
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_photos, Toast.LENGTH_LONG ).show();
      return;
    }
    for ( PhotoInfo item : list ) {
      mDataAdapter.add( item );
    }
  }

  // ---------------------------------------------------------------
  // list items click

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    // CharSequence item = ((TextView) view).getText();
    // String value = item.toString();
    // if ( value.equals( getResources().getString( R.string.back_to_survey ) ) ) {
    //   updateDisplay( );
    //   return;
    // }
    // // setListPos( position  );
    startPhotoDialog( (TextView)view, position );
  }

  public void startPhotoDialog( TextView tv, int pos )
  {
     mSavePhoto = mDataAdapter.get(pos);
     String filename = app.getSurveyJpgFile( mSavePhoto.id );
     (new PhotoEditDialog( this, this, mSavePhoto, filename )).show();
  }


  // ---------------------------------------------------------------
/*
  void takePhoto( long shotid, String name, String comment )
  {
    if ( name != null && name.length() > 0 ) {
      mPhotoId      = app.mData.nextPhotoId( );
      mShotId       = shoitid;
      mPhotoTitle   = name;
      mPhotoComment = comment;
      File imagefile = new File( app.getSurveyJpgFile( mPhotoId ) );
      // Log.v("DistoX", "photo " + imagefile.toString() );

      Uri outfileuri = Uri.fromFile( imagefile );
      Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
      intent.putExtra( MediaStore.EXTRA_OUTPUT, outfileuri );
      intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
      startActivityForResult( intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE );
    } else {
      Toast.makeText( this, R.string.picture_no_station, Toast.LENGTH_SHORT ).show();
    }
  }

  @Override
  protected void onActivityResult( int reqCode, int resCode, Intent data )
  {
    switch ( reqCode ) {
      case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
        if ( resCode == Activity.RESULT_OK ) {
          // Log.v("DistoX", "insert photo in db " + mPhotoId );
          app.mData.insertPhoto( app.mSID, -1L, mShotId, mPhotoTitle, mPhotoComment );
        }
    }
  }
*/

  // ---------------------------------------------------------------

  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.main);
    app = (TopoDroidApp) getApplication();
    mDataAdapter = new PhotoAdapter( this, R.layout.row, new ArrayList< PhotoInfo >() );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    updateDisplay( );
  }

  // ------------------------------------------------------------------


  public void dropPhoto( PhotoInfo photo )
  {
    app.mData.deletePhoto( photo.sid, photo.id );
    updateDisplay( ); // FIXME
  }

  public void updatePhoto( PhotoInfo photo, String title, String comment )
  {
    // Log.v( TAG, "updatePhoto From >" + from + "< To >" + to + "< comment " + comment );
    if ( app.mData.updatePhoto( photo.sid, photo.id, title, comment ) ) {
      if ( app.mListRefresh ) {
        // This works but it refreshes the whole list
        mDataAdapter.notifyDataSetChanged();
      } else {
        mSavePhoto.mTitle = title;
      }
    } else {
      Toast.makeText( this, R.string.no_db, Toast.LENGTH_LONG ).show();
    }
  }
}
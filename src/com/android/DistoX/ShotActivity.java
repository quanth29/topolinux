/* @file ShotActivity.java
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
 * 20120715 per-category preferences
 * 20121001 auto-extend splay shots
 * 20121113 mLastExtend to limit auto-extend of splays to new ones only
 * 20121129 included extend guess for splays in number assignment (only blank splay are "extend"-ed)
 * 20121129 commented MenuItem mMIextend
 * 20121215 merged update splays name+extend in a single db call (updateShotNameAndExtend)
 * 20130109 bug-fix missing block LEG in numberSplays
 * 20130110 menus: Survey -> Display; Distox under More; Number in its place
 * 20130111 photo date
 * 20130204 sleep menu to turn off screen immediately (1 sec)
 * 20130307 made Annotations into a dialog
 * 20130324 added mMIreset to reset DistoX connection
 * 20130910 "delete" button (removed "cancel"), "add shot" and "split survey" buttons
 * 20131022 top buttons and blank-leg violet color
 * 20131117 compute accel.+magn.+dip means
 * 20131201 button bar new interface. reorganized actions
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

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import android.os.Parcelable;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
// import android.os.SystemClock;
// import android.os.PowerManager;
import android.content.res.Resources;

import android.app.Application;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;
// import android.content.res.ColorStateList;

import android.provider.Settings.System;

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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.preference.PreferenceManager;

import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;

import android.util.Log;

public class ShotActivity extends Activity
                          implements OnItemClickListener
                        , OnItemLongClickListener
                        , OnClickListener
                        , ILister
                        , INewPlot
{
  private static int icons[] = { R.drawable.ic_download,
                        R.drawable.ic_splay,
                        R.drawable.ic_mode,
                        R.drawable.ic_plot,
                        R.drawable.ic_note,
                        R.drawable.ic_bt,
                        R.drawable.ic_more,
                        R.drawable.ic_less,
                        R.drawable.ic_disto,
                        R.drawable.ic_add,
                        R.drawable.ic_info,
                        R.drawable.ic_undelete,
                        R.drawable.ic_camera,
                        R.drawable.ic_sensor,
                        R.drawable.ic_3d,
                        R.drawable.ic_symbol,
                        R.drawable.ic_pref,
                        R.drawable.ic_help };
  private static int help_texts[] = { R.string.help_download,
                          R.string.help_splay,
                          R.string.help_display,
                          R.string.help_plot,
                          R.string.help_note,
                          R.string.help_bluetooth,
                          R.string.help_more,
                          R.string.help_less,
                          R.string.help_device,
                          R.string.help_add_shot,
                          R.string.help_info_shot,
                          R.string.help_undelete,
                          R.string.help_photo,
                          R.string.help_sensor,
                          R.string.help_3d,
                          R.string.help_symbol,
                          R.string.help_prefs,
                          R.string.help_help };

  private TopoDroidApp app;
  private static final int SENSOR_ACTIVITY_REQUEST_CODE = 1;
  private static final int EXTERNAL_ACTIVITY_REQUEST_CODE = 2;
  private static final int INFO_ACTIVITY_REQUEST_CODE = 3;
  static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

  // private static final int REQUEST_DEVICE    = 1;
  private static final int REQUEST_ENABLE_BT = 2;

  boolean mSplay = true;  //!< whether to show splay shots
  boolean mLeg   = true;  //!< whether to hide leg extra shots
  boolean mBlank = false; //!< whether to hide blank shots
  // private Bundle mSavedState = null;

  private ListView mList;
  // private int mListPos = -1;
  // private int mListTop = 0;
  private DistoXDBlockAdapter   mDataAdapter;

  // private long mLastExtend; // id of the last-extend-ed splay 

  // private String mSaveData = "";
  // private TextView mSaveTextView = null;
  // private int mFirstPos = -1;
  private static final String LIST_STATE = "listState";
  private int mFirstPos = -1;  
  private int mShotPos  = -1;  // shot entry position
  private int mPrevPos  = 0;   // prev shot entry position
  private int mNextPos  = 0;   // next shot entry position
  // private DistoXDBlock mSaveBlock = null;

  private Button[] mButton1;
  private Button[] mButton2;

  // String mPhotoTitle;
  static long   mSensorId;
  static long   mPhotoId;
  static String mComment;
  static long   mShotId;   // photo/sensor shot id

  ConnHandler mHandler;

  TopoDroidApp getApp() { return app; }

  // -------------------------------------------------------------------
  String getNextStationName()
  {
    return app.mData.getNextStationName( app.mSID );
  }

  private void tryExtendSplay( DistoXDBlock item, float bearing, long extend, boolean flip )
  {
    if ( extend == 0 ) return;
    // double db = Math.cos( (bearing - item.mBearing)*Math.PI/180 );
    // long ext = ( db > TopoDroidApp.mExtendThr )? extend : ( db < -TopoDroidApp.mExtendThr )? -extend : 0;
    double db = bearing - item.mBearing;
    while ( db < -180 ) db += 360;
    while ( db > 180 ) db -= 360;
    db = Math.abs( db );
    long ext = ( db < 90-TopoDroidApp.mExtendThr )? extend : ( db > 90+TopoDroidApp.mExtendThr )? -extend : 0;
    if ( flip ) ext = -ext;
  }

  // private boolean extendSplays()
  // { 
  //   long sid = app.mSID;
  //   if ( sid < 0 ) {
  //     Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
  //     return false;
  //   } else {
  //     List<DistoXDBlock> list = app.mData.selectShotsAfterId( sid, mLastExtend, TopoDroidApp.STATUS_NORMAL );
  //     int size = list.size();
  //     String from = ""; // shot "from" station
  //     String to   = ""; // shot "to" station
  //     float bearing = 0.0f;    // shot bearing
  //     long extend   = 0L;
  //     int k;
  //     DistoXDBlock prev = null;
  //     for ( k=size - 1; k>=0; --k ) {
  //       DistoXDBlock item = list.get( k );
  //       int t = item.type();
  //       // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "shot " + k + " type " + t + " <" + item.mFrom + "> <" + item.mTo + ">" );
  //       if ( t == DistoXDBlock.BLOCK_MAIN_LEG ) {
  //         from    = item.mFrom;
  //         to      = item.mTo;  
  //         bearing = item.mBearing;
  //         extend  = item.mExtend;
  //       } else if ( t == DistoXDBlock.BLOCK_SPLAY ) {
  //         if ( from.equals( item.mFrom ) || to.equals( item.mFrom ) ) {
  //           tryExtendSplay( item, bearing, extend, to.equals( item.mFrom ) );
  //           app.mData.updateShotExtend( item.mId, app.mSID, ext );
  //         }
  //       }
  //     }
  //     mLastExtend = app.mData.getLastShotId( sid );
  //   }
  //   return true;
  // }

  private void computeMeans( List<DistoXDBlock> list )
  {
    TopoDroidApp.mAccelerationMean = 0.0f;
    TopoDroidApp.mMagneticMean     = 0.0f;
    TopoDroidApp.mDipMean          = 0.0f;
    int size = list.size();
    if ( size > 0 ) {
      int cnt = 0;
      for ( DistoXDBlock blk : list ) {
        if ( blk.mAcceleration > 10.0 ) {
          TopoDroidApp.mAccelerationMean += blk.mAcceleration;
          TopoDroidApp.mMagneticMean     += blk.mMagnetic;
          TopoDroidApp.mDipMean          += blk.mDip;
          ++ cnt;
        }
      }
      if ( cnt > 0 ) {
        TopoDroidApp.mAccelerationMean /= cnt;
        TopoDroidApp.mMagneticMean     /= cnt;
        TopoDroidApp.mDipMean          /= cnt;
      }
      // Log.v( TopoDroidApp.TAG, "Acc " + TopoDroidApp.mAccelerationMean + " Mag " + TopoDroidApp.mMagneticMean 
      //                          + " Dip " + TopoDroidApp.mDipMean );
    }
  }

  ArrayList<DistoXDBlock> numberSplays()
  { 
    ArrayList<DistoXDBlock> updatelist = null;
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "numberSplays() ");
    long sid = app.mSID;
    if ( sid < 0 ) {
      // Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
      return null;
    } else {
      updatelist = new ArrayList<DistoXDBlock>();
      List<DistoXDBlock> list = app.mData.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
      computeMeans( list );

      int size = list.size();
      int from = 0;    // index to start with to assign the from-station
      int k;
      // DistoXDBlock current_leg = null;
      for ( k=0; k<size; ++k ) {
        DistoXDBlock item = list.get( k );
        int t = item.type();
        // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "shot " + k + " type " + t + " <" + item.mFrom + "> <" + item.mTo + ">" );
        if ( t == DistoXDBlock.BLOCK_MAIN_LEG ) {
          // current_leg = item;
          if ( from == k ) { // on a main-leg: move "from" to the next shot
            from = k+1;
          } else if ( from < k ) { // on a main-leg and "from" is behind: set splays
            String name = item.mFrom;
            if ( name != null ) {
              // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "update splays from " + from + " to " + k + " with name: <" + name + ">" );
              // set the index of the last splay to extend at the smallest from 
              for ( ; from < k; ++from ) {
                DistoXDBlock splay = list.get( from );
                splay.setName( name, "" );
                tryExtendSplay( splay, item.mBearing, item.mExtend, false );
                updatelist.add( splay ); 
                // mLastExtend = item.mId;
              }
            }
          }
        } else if ( t == DistoXDBlock.BLOCK_SPLAY || t == DistoXDBlock.BLOCK_SEC_LEG ) {
          // on a splay / sec-leg: jump "from" to the nxt shot
          from = k+1;
        } else if ( DistoXDBlock.isTypeBlank( t ) && k > 1 ) {
          DistoXDBlock prev = list.get( k-1 );
          if ( item.relativeDistance( prev ) < app.mCloseDistance ) {
            item.mType = DistoXDBlock.BLOCK_SEC_LEG;
            // current_leg.setTypeBlankLeg();
            updatelist.add( item ); 
            from = k+1;
          }
        }
      }

      // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "numberSplays() updatelist size " + updatelist.size() );
      if ( updatelist.size() > 0 ) {
        app.mData.updateShotNameAndExtend( sid, updatelist );
      }
    }
    return updatelist;
  }

  @Override
  public void refreshDisplay( int nr, boolean toast ) 
  {
    setTitleColor( TopoDroidApp.COLOR_NORMAL );
    if ( nr >= 0 ) {
      if ( nr > 0 ) updateDisplay( );
      if ( toast ) {
        Toast.makeText( this, getString(R.string.read_) + nr + getString(R.string.data), Toast.LENGTH_SHORT ).show();
      }
    } else if ( nr < 0 ) {
      if ( toast ) {
        // Toast.makeText( this, getString(R.string.read_fail_with_code) + nr, Toast.LENGTH_SHORT ).show();
        Toast.makeText( this, app.DistoXConnectionError[ -nr ], Toast.LENGTH_SHORT ).show();
      }
    }
  }
    
  public void updateDisplay( )
  {
    // Log.v( TopoDroidApp.TAG, "updateDisplay() " );

    DataHelper data = app.mData;
    if ( data != null && app.mSID >= 0 ) {
      List<DistoXDBlock> list = data.selectAllShots( app.mSID, TopoDroidApp.STATUS_NORMAL );
      // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "updateDisplay() shot list size " + list.size() );
      // Log.v( TopoDroidApp.TAG, "updateDisplay() shot list size " + list.size() );
      updateShotList( list );
      setTitle( app.mySurvey );
    } else {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
    }
  }

  void setShowIds( boolean show ) { mDataAdapter.show_ids = show; }

  boolean getShowIds() { return mDataAdapter.show_ids; }

  private void updateShotList( List<DistoXDBlock> list )
  {
    TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "updateShotList size " + list.size() );
    mDataAdapter.clear();
    mList.setAdapter( mDataAdapter );
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_shots, Toast.LENGTH_SHORT ).show();
      return;
    }
    DistoXDBlock prev = null;
    boolean prev_is_leg = false;
    for ( DistoXDBlock item : list ) {
      DistoXDBlock cur = item;
      int t = cur.type();
      // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "item " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );

      if ( cur.mType == DistoXDBlock.BLOCK_SEC_LEG
           || cur.relativeDistance( prev ) < app.mCloseDistance ) {
        // if ( prev != null && prev.mType == DistoXDBlock.BLOCK_BLANK ) prev.mType = DistoXDBlock.BLOCK_BLANK_LEG;
        if ( prev != null ) prev.setTypeBlankLeg();

        if ( mLeg ) { // flag: hide leg extra shots
          // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "close distance");
          if ( mBlank && prev != null && prev.isTypeBlank() ) {
            // prev was skipped: draw it now
            if ( ! prev_is_leg ) {
              cur = prev;
              prev_is_leg = true;
            } else {
              continue;
            }
          } else {
            continue;
          }
        } else {
          /* nothing */
        }
      } else {
        // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "not close distance");
        prev_is_leg = false;
        if ( DistoXDBlock.isTypeBlank(t) ) {
          prev = cur;
          if ( mBlank ) continue;
        } else if ( t == DistoXDBlock.BLOCK_SPLAY ) {
          prev = null;
          if ( mSplay ) continue;
        } else { // t == DistoXDBlock.BLOCK_MAIN_LEG
          prev = cur;
        }
      }
      // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "adapter add " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      mDataAdapter.add( cur );
    }
  }

  // ---------------------------------------------------------------
  // list items click

  @Override 
  public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "ShotActivity onItemLongClick id " + id);
    DistoXDBlock blk = mDataAdapter.get(pos);
    mShotId = blk.mId;
    (new PhotoSensorsDialog(this, this) ).show();
    return true;
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "ShotActivity onItemClick id " + id);
    DistoXDBlock blk = mDataAdapter.get(pos);

 
    mShotPos  = pos;
    mFirstPos = mList.getFirstVisiblePosition();

    // TextView tv = (TextView)view;
    // mSaveTextView = tv;
    // String msg = tv.getText().toString();
    // String[] st = msg.split( " ", 6 );
    // String data = st[2] + " " + st[3] + " " + st[4];
      
    DistoXDBlock prevBlock = null;
    DistoXDBlock nextBlock = null;
    // if ( blk.type() == DistoXDBlock.BLOCK_BLANK ) {
      // prevBlock = app.mData.selectPreviousLegShot( blk.mId, app.mSID );
      prevBlock = getPreviousLegShot( blk, false );
      nextBlock = getNextLegShot( blk, false );
      // if ( prevBlock != null ) {
      //   TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "prev leg " + prevBlock.mFrom + " " + prevBlock.mTo );
      // }
    // }
    (new ShotDialog( this, this, pos, blk, prevBlock, nextBlock )).show();
  }


  private void tryDownloadData()
  {
    if ( app.mBTAdapter.isEnabled() ) {
      if ( app.mDevice != null ) {
        setTitleColor( TopoDroidApp.COLOR_CONNECTED );
        // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "shot menu DOWNLOAD" );
        new DistoXRefresh( app, this ).execute();
        // updateDisplay( );
      } else {
        Toast.makeText( this, R.string.device_none, Toast.LENGTH_SHORT ).show();
      }
    } else {
      if ( app.mSID < 0 ) {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
      } else {
        DistoXDBlock last_blk = app.mData.selectLastLegShot( app.mSID );
        (new ShotNewDialog( this, app, this, last_blk, -1L )).show();
      }
    }
  }

  // ---------------------------------------------------------------


  void askPhotoComment( )
  {
    (new PhotoCommentDialog(this, this) ).show();
  }


  void doTakePhoto( String comment )
  {
    mComment = comment;
    mPhotoId      = app.mData.nextPhotoId( app.mSID );


    // imageFile := PHOTO_DIR / surveyId / photoId .jpg
    File imagefile = new File( app.getSurveyJpgFile( Long.toString(mPhotoId) ) );
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "photo " + imagefile.toString() );
    try {
      Uri outfileuri = Uri.fromFile( imagefile );
      Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
      intent.putExtra( MediaStore.EXTRA_OUTPUT, outfileuri );
      intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
      startActivityForResult( intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE );
    } catch ( ActivityNotFoundException e ) {
      Toast.makeText( this, "No image capture app", Toast.LENGTH_SHORT ).show();
    }
  }

  void askSensor( )
  {
    mSensorId = app.mData.nextSensorId( app.mSID );
    TopoDroidApp.Log( TopoDroidApp.LOG_SENSOR, "sensor " + mSensorId );
    Intent intent = new Intent( this, SensorActivity.class );
    startActivityForResult( intent, SENSOR_ACTIVITY_REQUEST_CODE );
  }

  void askExternal( )
  {
    mSensorId = app.mData.nextSensorId( app.mSID );
    TopoDroidApp.Log( TopoDroidApp.LOG_SENSOR, "sensor " + mSensorId );
    Intent intent = new Intent( this, ExternalActivity.class );
    startActivityForResult( intent, EXTERNAL_ACTIVITY_REQUEST_CODE );
  }

  void askShot( )
  {
    DistoXDBlock last_blk = null; // app.mData.selectLastLegShot( app.mSID );
    (new ShotNewDialog( this, app, this, last_blk, mShotId )).show();
  }

  void askSurvey( )
  {
    long old_sid = app.mSID;
    long old_id  = mShotId;
    // Log.v( TopoDroidApp.TAG, "askSurvey " + old_sid + " " + old_id );
    if ( app.mShotActivity != null ) {
      app.mShotActivity.finish();
      app.mShotActivity = null;
    }
    if ( app.mSurveyActivity != null ) {
      app.mSurveyActivity.finish();
      app.mSurveyActivity = null;
    }
    app.mActivity.startSurvey( null, 0, old_sid, old_id );
  }

  void askDelete( )
  {
    app.mData.deleteShot( mShotId, app.mSID );
    updateDisplay( ); // FIXME
  }

  void insertPhoto( )
  {
    // long shotid = 0;
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
    app.mData.insertPhoto( app.mSID, mPhotoId, mShotId, "", sdf.format( new Date() ), mComment ); // FIXME TITLE has to go
  }

  // void deletePhoto( PhotoInfo photo ) 
  // {
  //   app.mData.deletePhoto( app.mSID, photo.id );
  //   File imagefile = new File( app.getSurveyJpgFile( Long.toString(photo.id) ) );
  //   try {
  //     imagefile.delete();
  //   } catch ( IOException e ) { }
  // }

  @Override
  protected void onActivityResult( int reqCode, int resCode, Intent data )
  {
    switch ( reqCode ) {
      case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
        if ( resCode == Activity.RESULT_OK ) {
          // (new PhotoCommentDialog(this, this) ).show();
          insertPhoto();
        } else {
          // app.mData.deletePhoto( app.mSID, mPhotoId );
        }
        break;
      case SENSOR_ACTIVITY_REQUEST_CODE:
      case EXTERNAL_ACTIVITY_REQUEST_CODE:
        if ( resCode == Activity.RESULT_OK ) {
          Bundle extras = data.getExtras();
          String type  = extras.getString( TopoDroidApp.TOPODROID_SENSOR_TYPE );
          String value = extras.getString( TopoDroidApp.TOPODROID_SENSOR_VALUE );
          String comment = extras.getString( TopoDroidApp.TOPODROID_SENSOR_COMMENT );
          TopoDroidApp.Log( TopoDroidApp.LOG_SENSOR, "insert sensor " + type + " " + value + " " + comment );

          SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
          app.mData.insertSensor( app.mSID, mSensorId, mShotId, "", 
                                  sdf.format( new Date() ),
                                  comment,
                                  type,
                                  value );
        }
        break;
      case INFO_ACTIVITY_REQUEST_CODE:
        if ( resCode == Activity.RESULT_OK ) {
          finish();
        }
        break;
    }
  }

  // ---------------------------------------------------------------
  private Button mButtonHelp;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  HorizontalButtonView mButtonView2;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.shot_activity);
    app = (TopoDroidApp) getApplication();
    app.mShotActivity = this; // FIXME

    mDataAdapter = new DistoXDBlockAdapter( this, R.layout.row, new ArrayList<DistoXDBlock>() );

    mButtonHelp = (Button)findViewById( R.id.help );
    mButtonHelp.setOnClickListener( this );
    if ( TopoDroidApp.mHideHelp ) {
      mButtonHelp.setVisibility( View.GONE );
    } else {
      mButtonHelp.setVisibility( View.VISIBLE );
    }


    int nr_button1 = 7;
    int nr_button2 = 10;
    mButton1 = new Button[ nr_button1 ];
    mButton2 = new Button[ nr_button2 ];
    int k;
    for ( k=0; k<nr_button1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      mButton1[k].setBackgroundResource(  icons[k] );
    }
    for ( k=0; k<nr_button2; ++k ) {
      mButton2[k] = new Button( this );
      mButton2[k].setPadding(0,0,0,0);
      mButton2[k].setOnClickListener( this );
      mButton2[k].setBackgroundResource(  icons[k+nr_button1] );
    }
    // Resources res = getResources();

    setBTMenus( app.mBTAdapter.isEnabled() );

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mButtonView2 = new HorizontalButtonView( mButton2 );
    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setAdapter( mButtonView1.mAdapter );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    mList.setOnItemClickListener( this );
    mList.setLongClickable( true );
    mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );
    // mList.setSmoothScrollbarEnabled( true );

    restoreInstanceFromData();

    // mLastExtend = app.mData.getLastShotId( app.mSID );
    List<DistoXDBlock> list = app.mData.selectAllShots( app.mSID, TopoDroidApp.STATUS_NORMAL );
    computeMeans( list );

    updateDisplay( );
  }

  void scrollTo ( int pos ) 
  {
    // View v = mList.getChildAt(0);
    // int top = (v == null) ? 0 : v.getTop();
    // Log.v(TopoDroidApp.TAG, "scrollTo shot_pos " + mShotPos + " first_pos " + mFirstPos );
    // mList.setSelectionFromTop(mFirstPos, 0);
    // mList.scrollTo( mFirstPos, 0 );
    // mList.setSelection( mFirstPos ); // does not work
    // mList.smoothScrollToPositionFromTop( mShotPos, 0, 10 ); // API level 11
    mList.smoothScrollToPosition( mFirstPos );
  }

  @Override
  public synchronized void onPause() 
  {
    super.onPause();
    // Log.v( TopoDroidApp.TAG, "onPause()" );
    app.unregisterConnListener( mHandler );
    // if ( app.mComm != null ) { app.mComm.suspend(); }
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();

    // Log.v( TopoDroidApp.TAG, "onResume()" );
    System.putInt(getContentResolver(), System.SCREEN_OFF_TIMEOUT, TopoDroidApp.mScreenTimeout );

    // if ( app.mComm != null ) { app.mComm.resume(); }
    updateDisplay( );

    app.registerConnListener( mHandler );
    // setTitleColor( app.isConnected() ? TopoDroidApp.COLOR_CONNECTED : TopoDroidApp.COLOR_NORMAL );
  }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
    saveInstanceToData();
  }

  private void restoreInstanceFromData()
  { 
    String shots = app.mData.getValue( "DISTOX_SHOTS" );
    if ( shots != null ) {
      String[] vals = shots.split( " " );
      mSplay  = vals[0].equals("1");
      mLeg    = vals[1].equals("1");
      mBlank  = vals[2].equals("1");
      setShowIds( vals[3].equals("1") );
    }
  }
    
  private void saveInstanceToData()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("%d %d %d %d", mSplay?1:0, mLeg?1:0, mBlank?1:0, getShowIds()?1:0 );
    app.mData.setValue( "DISTOX_SHOTS", sw.getBuffer().toString() );
  }


  public void onClick(View view)
  {
    Button b = (Button)view;
    Intent intent;

    int k1 = 0;
    int k2 = 0;
    if ( b == mButton1[k1++] ) {        // mBtnDownload
      tryDownloadData();
    } else if ( b == mButton1[k1++] ) { // mBtnSplays 
      ArrayList<DistoXDBlock> list = numberSplays();
      if ( list != null && list.size() > 0 ) {
        updateDisplay( );
      }
    } else if ( b == mButton1[k1++] ) { // mBtnDisplay 
      new ShotDisplayDialog( this, this ).show();
    } else if ( b == mButton1[k1++] ) { // mBtnSketch
      new PlotListDialog( this, this, app ).show();
    } else if ( b == mButton1[k1++] ) { // mBtnNote
      if ( app.mySurvey != null ) {
        (new DistoXAnnotations( this, app.mySurvey )).show();
      }
    } else if ( b == mButton1[k1++] ) { // mBtnReset
      app.resetComm();
      Toast.makeText( this, "Reset BT connection", Toast.LENGTH_SHORT ).show();
    } else if ( b == mButton1[k1++] ) { // mBtnMore
      mListView.setAdapter( mButtonView2.mAdapter );
      mListView.invalidate();

    
    } else if ( b == mButton2[k2++] ) { // mBtnLess
      mListView.setAdapter( mButtonView1.mAdapter );
      mListView.invalidate();
    } else if ( b == mButton2[k2++] ) { // mBtnDevice
      if ( app.mBTAdapter.isEnabled() ) {
        intent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
        startActivity( intent );
      }
    } else if ( b == mButton2[k2++] ) { // mBtnAdd 
      DistoXDBlock last_blk = app.mData.selectLastLegShot( app.mSID );
      (new ShotNewDialog( this, app, this, last_blk, -1L )).show();
    } else if ( b == mButton2[k2++] ) { // mBtnInfo
      intent = new Intent( this, SurveyActivity.class );
      intent.putExtra( TopoDroidApp.TOPODROID_SURVEY,  0 ); // mustOpen 
      intent.putExtra( TopoDroidApp.TOPODROID_OLDSID, -1 ); // old_sid 
      intent.putExtra( TopoDroidApp.TOPODROID_OLDID,  -1 ); // old_id 
      startActivityForResult( intent, INFO_ACTIVITY_REQUEST_CODE );

    } else if ( b == mButton2[k2++] ) { // mBtnUndelete
      (new DistoXUndelete(this, this, app.mData, app.mSID ) ).show();
      updateDisplay( );
    } else if ( b == mButton2[k2++] ) { // mBtnCamera
      intent = new Intent( this, PhotoActivity.class );
      startActivity( intent );
    } else if ( b == mButton2[k2++] ) { // mBtnSensor
      intent = new Intent( this, SensorListActivity.class );
      startActivity( intent );
    } else if ( b == mButton2[k2++] ) { // mBtn3D
      app.exportSurveyAsTh(); // make sure to have survey exported as therion
      try {
        intent = new Intent( "Cave3D.intent.action.Launch" );
        intent.putExtra( "survey", app.getSurveyThFile() );
        startActivity( intent );
      } catch ( ActivityNotFoundException e ) {
        Toast.makeText( this, R.string.no_cave3d, Toast.LENGTH_SHORT ).show();
      }
    } else if ( b == mButton2[k2++] ) { // mBtnSymbol
      DrawingBrushPaths.makePaths( getResources() );
      (new SymbolEnableDialog( this, this )).show();
      // DrawingBrushPaths.mReloadSymbols = true;
      // try {
      //   intent = new Intent( "TdSymbol.intent.action.Launch" );
      //   startActivity( intent );
      // } catch ( ActivityNotFoundException e ) {
      //   Toast.makeText( this, R.string.no_tdsymbol, Toast.LENGTH_SHORT ).show();
      // }
    // } else if ( b == mButton2[] ) { // mBtnSleep
    //   System.putInt(getContentResolver(), System.SCREEN_OFF_TIMEOUT, 1000); // 1 secs
    } else if ( b == mButton2[k2++] ) { // mBtnOptions
      intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
      startActivity( intent );
    } else if ( b == mButtonHelp ) { // mBtnHelp

        (new HelpDialog(this, icons, help_texts ) ).show();
    }
  }

  // ------------------------------------------------------------------

  private void setBTMenus( boolean enabled )
  {
    // if ( mBtnDownload != null ) mBtnDownload.setEnabled( enabled );
    mButton1[0].setEnabled( enabled );
  }

  // public void makeNewPlot( String name, long type, String start, String view )
  public void makeNewPlot( String name, String start )
  {
    // plot-id -1, status 0
    long mPIDp = app.mData.insertPlot( app.mSID, -1L, name+"p", PlotInfo.PLOT_PLAN, 0L, start, "", 0, 0, TopoDroidApp.mScaleFactor );
    long mPIDs = app.mData.insertPlot( app.mSID, -1L, name+"s", PlotInfo.PLOT_EXTENDED, 0L, start, "", 0, 0, TopoDroidApp.mScaleFactor );
    if ( mPIDp >= 0 ) {
      startDrawingActivity( start, name+"p", mPIDp, name+"s", mPIDs );
    }
    // updateDisplay( );
  }

  // FIXME_SKETCH_3D
  // public void makeNewSketch3d( String name, String st1, String st2 )
  // {
  //   // FIXME xoffset yoffset, east south and vert (downwards)
  //   if ( st2 != null ) {
  //     if ( ! app.mData.hasShot( app.mSID, st1, st2 ) ) {
  //       Toast.makeText(getApplicationContext(), R.string.no_shot_between_stations, Toast.LENGTH_SHORT).show();
  //       return;
  //     }
  //   } else {
  //     st2 = app.mData.nextStation( app.mSID, st1 );
  //   }
  //   if ( st2 != null ) {
  //     float e = 0.0f; // NOTE (e,s,v) are the coord of station st1, and st1 is taken as the origin of the ref-frame
  //     float s = 0.0f;
  //     float v = 0.0f;
  //     long mPID = app.mData.insertSketch3d( app.mSID, -1L, name, 0L, st1, st1, st2,
  //                                           0, // app.mDisplayWidth/(2*TopoDroidApp.mScaleFactor),
  //                                           0, // app.mDisplayHeight/(2*TopoDroidApp.mScaleFactor),
  //                                           10 * TopoDroidApp.mScaleFactor,
  //                                           0, 0, 10 * TopoDroidApp.mScaleFactor,
  //                                           0, 0, 10 * TopoDroidApp.mScaleFactor,
  //                                           e, s, v, 180, 0 );
  //     if ( mPID >= 0 ) {
  //       startSketchActivity( name );
  //     }
  //   } else {
  //     Toast.makeText(getApplicationContext(), "no to station", Toast.LENGTH_SHORT).show();
  //   }
  // }

  public void startExistingPlot( String name, String type ) // name = plot/sketch3d name
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "startExistingPlot \"" + name + "\" type " + type + " sid " + app.mSID );

    // FIXME_SKETCH_3D
    // if ( type.startsWith( "Sketch" ) ) {
    //   Sketch3dInfo sketch = app.mData.getSketch3dInfo( app.mSID, name );
    //   if ( sketch != null ) {
    //     startSketchActivity( sketch.name );
    //     return;
    //   }
    // } else {
      PlotInfo plot1 =  app.mData.getPlotInfo( app.mSID, name+"p" );
      if ( plot1 != null ) {
        PlotInfo plot2 =  app.mData.getPlotInfo( app.mSID, name+"s" );
        startDrawingActivity( plot1.start, plot1.name, plot1.id, plot2.name, plot2.id );
        return;
      }
    // }
    Toast.makeText(getApplicationContext(), R.string.plot_not_found, Toast.LENGTH_SHORT).show();
  }
 
  // FIXME_SKETCH_3D
  // private void startSketchActivity( String name )
  // {
  //   if ( app.mSID < 0 ) {
  //     Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
  //     return;
  //   }
  //   // TODO
  //   Intent sketchIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, SketchActivity.class );
  //   sketchIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY_ID, app.mSID );
  //   sketchIntent.putExtra( TopoDroidApp.TOPODROID_SKETCH_NAME, name );
  //   startActivity( sketchIntent );
  // }

  private void startDrawingActivity( String start, String plot1_name, long plot1_id, String plot2_name, long plot2_id )
  {
    if ( app.mSID < 0 || plot1_id < 0 || plot2_id < 0 ) {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
      return;
    }
    
    // Toast.makeText( this, R.string.loading_wait, Toast.LENGTH_SHORT ).show();
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "startDrawingActivity start " + start + " viewed " + viewed );
    // FIXME what if plot_name already exists ? 
    // long mPID = app.mData.getPlotId( app.mSID, plot_name );

    // FIXME this extra check is not necessary
    // PlotInfo plot = app.mData.getPlotInfo( app.mSID, plot_name );
    // if ( plot != null ) {
      // plot.dump();
      Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingActivity.class );
      drawIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY_ID, app.mSID );
      drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_NAME, plot1_name );
      drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_NAME2, plot2_name );
      drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_TYPE, PlotInfo.PLOT_PLAN );
      // drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_ID, plot1_id ); // not necessary
      // drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_ID2, plot2_id ); // not necessary

      startActivity( drawIntent );
    // }
  }

  /**
   * @param at   id of the shot before which to insert the new shot (and LRUD)
   *
  public DistoXDBlock makeNewShot( long at, String from, String to,
                           float distance, float bearing, float clino, long extend,
                           String left, String right, String up, String down )
  {
    DistoXDBlock ret = null;
    long id;
    long sid = app.mSID;
    DataHelper data = app.mData;
    if ( from != null && to != null && from.length() > 0 ) {
      // if ( data.makesCycle( -1L, sid, from, to ) ) {
      //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_SHORT ).show();
      // } else
      {
        // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "makeNewShot Data " + distance + " " + bearing + " " + clino );
        boolean horizontal = ( Math.abs( clino ) > app.mVThreshold );
        // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "makeNewShot SID " + sid + " LRUD " + left + " " + right + " " + up + " " + down);
        if ( left != null && left.length() > 0 ) {
          float l = Float.parseFloat( left );
          if ( horizontal ) {
            if ( at >= 0L ) {
              id = data.insertShotAt( sid, at, l, 270.0f, 0.0f, 0.0f );
            } else {
              id = data.insertShot( sid, -1L, l, 270.0f, 0.0f, 0.0f );
            }
          } else {
            float b = bearing - 90.0f;
            if ( b < 0.0f ) b += 360.0f;
            // b = in360( b );
            if ( at >= 0L ) {
              id = data.insertShotAt( sid, at, l, b, 0.0f, 0.0f );
            } else {
              id = data.insertShot( sid, -1L, l, b, 0.0f, 0.0f );
            }
          }
          data.updateShotName( id, sid, from, "" );
          if ( at >= 0L ) ++at;
        }
        if ( right != null && right.length() > 0 ) {
          float r = Float.parseFloat( right );
          if ( horizontal ) {
            if ( at >= 0L ) {
              id = data.insertShotAt( sid, at, r, 90.0f, 0.0f, 0.0f );
            } else {
              id = data.insertShot( sid, -1L, r, 90.0f, 0.0f, 0.0f );
            }
          } else {
            float b = bearing + 90.0f;
            if ( b >= 360.0f ) b -= 360.0f;
            if ( at >= 0L ) {
              id = data.insertShotAt( sid, at, r, b, 0.0f, 0.0f );
            } else {
              id = data.insertShot( sid, -1L, r, b, 0.0f, 0.0f );
            }
          }
          data.updateShotName( id, sid, from, "" );
          if ( at >= 0L ) ++at;
        }
        if ( up != null && up.length() > 0 ) {
          float u = Float.parseFloat( up );
          if ( horizontal ) {
            if ( at >= 0L ) {
              id = data.insertShotAt( sid, at, u, 0.0f, 0.0f, 0.0f );
            } else {
              id = data.insertShot( sid, -1L, u, 0.0f, 0.0f, 0.0f );
            }
          } else {
            if ( at >= 0L ) {
              id = data.insertShotAt( sid, at, u, 0.0f, 90.0f, 0.0f );
            } else {
              id = data.insertShot( sid, -1L, u, 0.0f, 90.0f, 0.0f );
            }
          }
          data.updateShotName( id, sid, from, "" );
          if ( at >= 0L ) ++at;
        }
        if ( down != null && down.length() > 0 ) {
          float d = Float.parseFloat( down );
          if ( horizontal ) {
            if ( at >= 0L ) {
              id = data.insertShotAt( sid, at, d, 180.0f, 0.0f, 0.0f );
            } else {
              id = data.insertShot( sid, -1L, d, 180.0f, 0.0f, 0.0f );
            }
          } else {
            if ( at >= 0L ) {
              id = data.insertShotAt( sid, at, d, 0.0f, -90.0f, 0.0f );
            } else {
              id = data.insertShot( sid, -1L, d, 0.0f, -90.0f, 0.0f );
            }
          }
          data.updateShotName( id, sid, from, "" );
          if ( at >= 0L ) ++at;
        }
        if ( at >= 0L ) {
          id = data.insertShotAt( sid, at, distance, bearing, clino, 0.0f );
        } else {
          id = data.insertShot( sid, -1L, distance, bearing, clino, 0.0f );
        }
        // String name = from + "-" + to;
        data.updateShotName( id, sid, from, to );
        // data.updateShotExtend( id, sid, extend );
        updateDisplay( );

        ret = data.selectShot( id, sid );
      }
    } else {
      Toast.makeText( this, R.string.missing_station, Toast.LENGTH_SHORT ).show();
    }
    return ret;
  }
*/

  // public void dropShot( DistoXDBlock blk )
  // {
  //   app.mData.deleteShot( blk.mId, app.mSID );
  //   updateDisplay( ); // FIXME
  // }

  public DistoXDBlock getNextBlankLegShot( DistoXDBlock blk )
  {
    DistoXDBlock ret = null;
    long id = 0;
    for ( int k=0; k<mDataAdapter.size(); ++k ) {
      DistoXDBlock b = mDataAdapter.get(k);
      if ( b.isTypeBlank() ) {
        id = b.mId - 1;
        break;
      }
    }
    List<DistoXDBlock> list = app.mData.selectShotsAfterId( app.mSID, id , 0 );
    for ( DistoXDBlock b : list ) {
      if ( b.isTypeBlank() ) {
        // Log.v( TopoDroidApp.TAG, "BLANK " + b.mLength + " " + b.mBearing + " " + b.mClino );
        if ( ret != null ) {
          if ( ret.relativeDistance( b ) < app.mCloseDistance ) return ret;
        }
        ret = b;
      } else if ( b.mType == DistoXDBlock.BLOCK_SEC_LEG ) {
        // Log.v( TopoDroidApp.TAG, "LEG " + b.mLength + " " + b.mBearing + " " + b.mClino );
        if ( ret != null ) {
          if ( ret.relativeDistance( b ) < app.mCloseDistance ) return ret;
        }
      } else {
        // Log.v( TopoDroidApp.TAG, "OTHER " + b.mLength + " " + b.mBearing + " " + b.mClino );
        ret = null;
      }
    }
    return null;
  }

  // get the next centerline shot and set mNextPos index
  public DistoXDBlock getNextLegShot( DistoXDBlock blk, boolean move_down )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "getNextLegShot: pos " + mShotPos );
    if ( blk == null ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "   block is null");
      return null;
    }
    if ( move_down ) {
      mPrevPos = mShotPos;
      mShotPos = mNextPos;
      mNextPos = mPrevPos; // the old mShotPos;
    } else {
      mNextPos = mShotPos;
    }
    while ( mNextPos < mDataAdapter.size() && blk != mDataAdapter.get(mNextPos) ) ++ mNextPos;
    ++ mNextPos; // one position after blk
    while ( mNextPos < mDataAdapter.size() ) {
      DistoXDBlock b = mDataAdapter.get(mNextPos);
      int t = b.type();
      if ( t == DistoXDBlock.BLOCK_MAIN_LEG ) {
        return b;
      } else if (    DistoXDBlock.isTypeBlank( t )
                  && mNextPos+1 < mDataAdapter.size()
                  && b.relativeDistance( mDataAdapter.get(mNextPos+1) ) < app.mCloseDistance ) {
        return b;
      }
      ++ mNextPos;
    }
    return null;
    // DistoXDBlock nextBlock = app.mData.selectNextLegShot( blk.mId, app.mSID );
    // return nextBlock;
  }

  // get the previous centerline shot and set the mPrevPos index
  public DistoXDBlock getPreviousLegShot( DistoXDBlock blk, boolean move_up )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "getPreviousLegShot: pos " + mShotPos );
    if ( blk == null ) return null;
    if ( move_up ) {
      mNextPos = mShotPos;
      mShotPos = mPrevPos;
      mPrevPos = mNextPos; // the old mShotPos;
    } else {
      mPrevPos = mShotPos;
    }
    while ( mPrevPos >= 0 && blk != mDataAdapter.get(mPrevPos) ) -- mPrevPos;
    while ( mPrevPos > 0 ) {
      -- mPrevPos;
      DistoXDBlock b = mDataAdapter.get(mPrevPos);
      if ( b.type() == DistoXDBlock.BLOCK_MAIN_LEG ) {
        return b;
      }
    }
    return null;
    // DistoXDBlock prevBlock = app.mData.selectNextLegShot( blk.mId, app.mSID );
    // return prevBlock;
  }

  public void updateShot( String from, String to, long extend, long flag, boolean leg, String comment, DistoXDBlock blk )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "updateShot From >" + from + "< To >" + to + "< comment " + comment );
    // Log.v( TopoDroidApp.TAG, "updateShot pos " + mShotPos + " From >" + from + "< To >" + to + "< comment " + comment );

    int ret = app.mData.updateShot( blk.mId, app.mSID, from, to, extend, flag, leg?1:0, comment );
    if ( ret == -1 ) {
      Toast.makeText( this, R.string.no_db, Toast.LENGTH_SHORT ).show();
    // } else if ( ret == -2 ) {
    //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_SHORT ).show();
    } else {
      // update same shots of the given block
      List< DistoXDBlock > blk_list = app.mData.selectShotsAfterId( blk.mId, app.mSID, 0L );
      for ( DistoXDBlock blk1 : blk_list ) {
        if ( blk1.relativeDistance( blk ) > app.mCloseDistance ) break;
        app.mData.updateShotLeg( blk1.mId, app.mSID, 1L );
      }
      // if ( app.mListRefresh ) {
      //   // This works but it refreshes the whole list
      //   mDataAdapter.notifyDataSetChanged();
      //   // mList.smoothScrollToPosition( mShotPos );
      //   // mSaveTextView.requestLayout();
      //   // mSaveTextView.requestFocus();
      // } else {
      //   // mSaveTextView.setText( blk.toString(false) );
      //   // mSaveTextView.setTextColor( blk.color() );
      //   mDataAdapter.notifyDataSetChanged(); // FIXME
      // }
      mDataAdapter.notifyDataSetChanged(); // FIXME
    }

    scrollTo( mShotPos );
  }

  // ------------------------------------------------------------------------

  @Override
  public void onBackPressed () // askClose
  {
    AlertDialog.Builder alert = new AlertDialog.Builder( this );
    // alert.setTitle( R.string.delete );
    alert.setMessage( getResources().getString( R.string.ask_close_survey ) );
    
    alert.setPositiveButton( R.string.button_ok, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          finish();
        }
    } );

    alert.setNegativeButton( R.string.button_cancel, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) { }
    } );
    alert.show();
  }

}

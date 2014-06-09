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
 * 20140414 last and second-last shot id's
 * 20140115 fixed bug display purple shots 
 * 20140416 menus: palette options help
 * 20140508 dropped setBTMenus (download --> add when BT not enabled)
 */
package com.topodroid.DistoX;

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

import android.graphics.Rect;

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

import android.view.Menu;
import android.view.MenuItem;

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
                        R.drawable.ic_meta,
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

  private TopoDroidApp mApp;
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
  // long mSecondLastShotId = 0L;
  // long mLastShotId;

  long secondLastShotId() { return mApp.mSecondLastShotId; }

  private ListView mList;
  // private int mListPos = -1;
  // private int mListTop = 0;
  private DistoXDBlockAdapter   mDataAdapter;

  // private long mLastExtend; // id of the last-extend-ed splay 

  private static final String LIST_STATE = "listState";
  private int mFirstPos = -1;  
  // private int mScroll   = 0;
  private int mSavePos  = -1;  // shot entry position
  private int mShotPos  = -1;  // shot entry position
  private int mPrevPos  = 0;   // prev shot entry position
  private int mNextPos  = 0;   // next shot entry position
  // private TextView mSaveTextView = null;

  private Button[] mButton1;
  private Button[] mButton2;

  private MenuItem mMIsymbol;
  private MenuItem mMIoptions;
  private MenuItem mMIhelp;

  static long   mSensorId;
  static long   mPhotoId;
  static String mComment;
  static long   mShotId;   // photo/sensor shot id

  ConnHandler mHandler;

  TopoDroidApp getApp() { return mApp; }

  // -------------------------------------------------------------------
  String getNextStationName()
  {
    return mApp.mData.getNextStationName( mApp.mSID );
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
  //   long sid = mApp.mSID;
  //   if ( sid < 0 ) {
  //     Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
  //     return false;
  //   } else {
  //     List<DistoXDBlock> list = mApp.mData.selectShotsAfterId( sid, mLastExtend, TopoDroidApp.STATUS_NORMAL );
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
  //           mApp.mData.updateShotExtend( item.mId, mApp.mSID, ext );
  //         }
  //       }
  //     }
  //     mLastExtend = mApp.mData.getLastShotId( sid );
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
    long sid = mApp.mSID;
    if ( sid < 0 ) {
      // Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
      return null;
    } else {
      String prev_from = "";
      updatelist = new ArrayList<DistoXDBlock>();
      List<DistoXDBlock> list = mApp.mData.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
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
            prev_from = item.mFrom;
            from = k+1;
          } else if ( from < k ) { // on a main-leg and "from" is behind: set splays
            String name = (mApp.mShotAfterSplays) ? item.mFrom : prev_from;
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
          // on a splay / sec-leg: jump "from" to the next shot
          from = k+1;
        } else if ( DistoXDBlock.isTypeBlank( t ) && k > 1 ) {
          DistoXDBlock prev = list.get( k-1 );
          if ( item.relativeDistance( prev ) < mApp.mCloseDistance ) {
            item.mType = DistoXDBlock.BLOCK_SEC_LEG;
            // current_leg.setTypeBlankLeg();
            updatelist.add( item ); 
            from = k+1;
          }
        }
      }

      // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "numberSplays() updatelist size " + updatelist.size() );
      if ( updatelist.size() > 0 ) {
        mApp.mData.updateShotNameAndExtend( sid, updatelist );
      }
    }
    return updatelist;
  }

  @Override
  public void refreshDisplay( int nr, boolean toast ) 
  {
    setTitleColor( TopoDroidApp.COLOR_NORMAL );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        // mLastShotId = mApp.mData.getLastShotId( mApp.mSID );
        updateDisplay( );
      }
      if ( toast ) {
        Toast.makeText( this, getString(R.string.read_) + nr + getString(R.string.data), Toast.LENGTH_SHORT ).show();
      }
    } else if ( nr < 0 ) {
      if ( toast ) {
        // Toast.makeText( this, getString(R.string.read_fail_with_code) + nr, Toast.LENGTH_SHORT ).show();
        Toast.makeText( this, mApp.DistoXConnectionError[ -nr ], Toast.LENGTH_SHORT ).show();
      }
    }
  }
    
  public void updateDisplay( )
  {
    // Log.v( TopoDroidApp.TAG, "updateDisplay() " );

    DataHelper data = mApp.mData;
    if ( data != null && mApp.mSID >= 0 ) {
      List<DistoXDBlock> list = data.selectAllShots( mApp.mSID, TopoDroidApp.STATUS_NORMAL );
      // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "updateDisplay() shot list size " + list.size() );
      // Log.v( TopoDroidApp.TAG, "updateDisplay() shot list size " + list.size() );
      updateShotList( list );
      setTitle( mApp.mySurvey );
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
           || cur.relativeDistance( prev ) < mApp.mCloseDistance ) {

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
        } else { // do not hide extra leg-shots
          if ( mBlank && prev != null && prev.isTypeBlank() ) {
            if ( ! prev_is_leg ) {
              mDataAdapter.add( prev );
              prev_is_leg = true;
            } else {
              /* nothing */
            }
          } else {
            /* nothing */
          }
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

    mSavePos = pos;
    mShotPos = pos;
    mFirstPos = mList.getFirstVisiblePosition();
    // mScroll   = mList.getScrollY();
    // mSaveTextView = (TextView)view;

    // TextView tv = (TextView)view;
    // String msg = tv.getText().toString();
    // String[] st = msg.split( " ", 6 );
    // String data = st[2] + " " + st[3] + " " + st[4];
      
    DistoXDBlock prevBlock = null;
    DistoXDBlock nextBlock = null;
    // if ( blk.type() == DistoXDBlock.BLOCK_BLANK ) {
      // prevBlock = mApp.mData.selectPreviousLegShot( blk.mId, mApp.mSID );
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
    // mSecondLastShotId = mApp.lastShotId( );
    if ( mApp.mDevice != null && mApp.mBTAdapter.isEnabled() ) {
      setTitleColor( TopoDroidApp.COLOR_CONNECTED );
      // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "shot menu DOWNLOAD" );
      new DistoXRefresh( mApp, this ).execute();
      // updateDisplay( );
    } else {
      if ( mApp.mSID < 0 ) {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
      } else {
        DistoXDBlock last_blk = mApp.mData.selectLastLegShot( mApp.mSID );
        (new ShotNewDialog( this, mApp, this, last_blk, -1L )).show();
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
    mPhotoId      = mApp.mData.nextPhotoId( mApp.mSID );


    // imageFile := PHOTO_DIR / surveyId / photoId .jpg
    File imagefile = new File( mApp.getSurveyJpgFile( Long.toString(mPhotoId) ) );
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "photo " + imagefile.toString() );
    try {
      Uri outfileuri = Uri.fromFile( imagefile );
      Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
      intent.putExtra( MediaStore.EXTRA_OUTPUT, outfileuri );
      intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
      startActivityForResult( intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE );
    } catch ( ActivityNotFoundException e ) {
      Toast.makeText( this, "No image capture mApp", Toast.LENGTH_SHORT ).show();
    }
  }

  void askSensor( )
  {
    mSensorId = mApp.mData.nextSensorId( mApp.mSID );
    TopoDroidApp.Log( TopoDroidApp.LOG_SENSOR, "sensor " + mSensorId );
    Intent intent = new Intent( this, SensorActivity.class );
    startActivityForResult( intent, SENSOR_ACTIVITY_REQUEST_CODE );
  }

  void askExternal( )
  {
    mSensorId = mApp.mData.nextSensorId( mApp.mSID );
    TopoDroidApp.Log( TopoDroidApp.LOG_SENSOR, "sensor " + mSensorId );
    Intent intent = new Intent( this, ExternalActivity.class );
    startActivityForResult( intent, EXTERNAL_ACTIVITY_REQUEST_CODE );
  }

  void askShot( )
  {
    // mSecondLastShotId = mApp.lastShotId( );
    DistoXDBlock last_blk = null; // mApp.mData.selectLastLegShot( mApp.mSID );
    (new ShotNewDialog( this, mApp, this, last_blk, mShotId )).show();
  }

  // called by PhotoSensorDialog to split the survey
  //
  void askSurvey( )
  {
    long old_sid = mApp.mSID;
    long old_id  = mShotId;
    // Log.v( TopoDroidApp.TAG, "askSurvey " + old_sid + " " + old_id );
    if ( mApp.mShotActivity != null ) {
      mApp.mShotActivity.finish();
      mApp.mShotActivity = null;
    }
    if ( mApp.mSurveyActivity != null ) {
      mApp.mSurveyActivity.finish();
      mApp.mSurveyActivity = null;
    }
    mApp.mActivity.startSplitSurvey( old_sid, old_id ); // SPLIT SURVEY
  }

  void askDelete( )
  {
    mApp.mData.deleteShot( mShotId, mApp.mSID );
    updateDisplay( ); // FIXME
  }

  void insertPhoto( )
  {
    // long shotid = 0;
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
    mApp.mData.insertPhoto( mApp.mSID, mPhotoId, mShotId, "", sdf.format( new Date() ), mComment ); // FIXME TITLE has to go
  }

  // void deletePhoto( PhotoInfo photo ) 
  // {
  //   mApp.mData.deletePhoto( mApp.mSID, photo.id );
  //   File imagefile = new File( mApp.getSurveyJpgFile( Long.toString(photo.id) ) );
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
          // mApp.mData.deletePhoto( mApp.mSID, mPhotoId );
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
          mApp.mData.insertSensor( mApp.mSID, mSensorId, mShotId, "", 
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
  // private Button mButtonHelp;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  HorizontalButtonView mButtonView2;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.shot_activity);
    mApp = (TopoDroidApp) getApplication();
    mApp.mShotActivity = this; // FIXME

    mDataAdapter = new DistoXDBlockAdapter( this, this, R.layout.row,
            new ArrayList<DistoXDBlock>(), new ArrayList< TextView >(), new ArrayList< View >() );

    // mButtonHelp = (Button)findViewById( R.id.help );
    // mButtonHelp.setOnClickListener( this );
    // if ( TopoDroidApp.mHideHelp ) {
    //   mButtonHelp.setVisibility( View.GONE );
    // } else {
    //   mButtonHelp.setVisibility( View.VISIBLE );
    // }


    int nr_button1 = 7;
    int nr_button2 = 8;
    mButton1 = new Button[ nr_button1 ];
    mButton2 = new Button[ nr_button2 ];
    int k;
    for ( k=0; k<nr_button1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      mButton1[k].setBackgroundResource(  icons[k] );
    }
    if ( mApp.mDevice == null ) {
      mButton1[0].setBackgroundResource( icons[9] );
    }

    for ( k=0; k<nr_button2; ++k ) {
      mButton2[k] = new Button( this );
      mButton2[k].setPadding(0,0,0,0);
      mButton2[k].setOnClickListener( this );
      mButton2[k].setBackgroundResource(  icons[k+nr_button1] );
    }
    // Resources res = getResources();

    // setBTMenus( mApp.mBTAdapter.isEnabled() );

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

    // mLastExtend = mApp.mData.getLastShotId( mApp.mSID );
    List<DistoXDBlock> list = mApp.mData.selectAllShots( mApp.mSID, TopoDroidApp.STATUS_NORMAL );
    // mSecondLastShotId = mApp.lastShotId( );

    if ( list.size() > 4 ) computeMeans( list );
    
    updateDisplay( );
  }

  void enableSketchButton( boolean enabled )
  {
    mApp.mEnableZip = enabled;
    mButton1[3].setEnabled( enabled ); // FIXME SKETCH BUTTON 
    mButton1[3].setBackgroundResource( enabled ? R.drawable.ic_plot : R.drawable.ic_plot_no );
  }

  // void scrollTo ( int pos, DistoXDBlock blk ) 
  // {
    // View v = mList.getChildAt(0);
    // int top = (v == null) ? 0 : v.getTop();
    // Log.v(TopoDroidApp.TAG, "scrollTo " + pos + " " + mFirstPos + " scrollY " + mScroll );
    // mList.setSelectionFromTop(mFirstPos, 0);
    // mList.scrollTo( mFirstPos, 0 );  // not good: this moves the mList inside the container view
    // mList.setSelection( mFirstPos ); // does not work
    // mList.smoothScrollToPositionFromTop( mShotPos, 0, 10 ); // API level 11

    // mList.smoothScrollToPosition( mFirstPos );
    
    // mList.setSelectionFromTop( pos, 10 );
    // mList.setScrollY( mScroll ); // Api level 14

  //   mDataAdapter.updateBlockView( blk );
    // mSaveTextView.requestFocus();

  // }

  @Override
  public synchronized void onPause() 
  {
    super.onPause();
    // Log.v( TopoDroidApp.TAG, "onPause()" );
    mApp.unregisterConnListener( mHandler );
    // if ( mApp.mComm != null ) { mApp.mComm.suspend(); }
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();

    // if ( mApp.mComm != null ) { mApp.mComm.resume(); }
    updateDisplay( );

    mApp.registerConnListener( mHandler );
    // setTitleColor( mApp.isConnected() ? TopoDroidApp.COLOR_CONNECTED : TopoDroidApp.COLOR_NORMAL );
  }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
    saveInstanceToData();
  }

  private void restoreInstanceFromData()
  { 
    String shots = mApp.mData.getValue( "DISTOX_SHOTS" );
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
    mApp.mData.setValue( "DISTOX_SHOTS", sw.getBuffer().toString() );
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
      new PlotListDialog( this, this, mApp ).show();
    } else if ( b == mButton1[k1++] ) { // mBtnNote
      if ( mApp.mySurvey != null ) {
        (new DistoXAnnotations( this, mApp.mySurvey )).show();
      }
    } else if ( b == mButton1[k1++] ) { // mBtnReset
      mApp.resetComm();
      Toast.makeText(this, R.string.bt_reset, Toast.LENGTH_SHORT).show();
    } else if ( b == mButton1[k1++] ) { // mBtnMore
      mListView.setAdapter( mButtonView2.mAdapter );
      mListView.invalidate();

    
    } else if ( b == mButton2[k2++] ) { // mBtnLess
      mListView.setAdapter( mButtonView1.mAdapter );
      mListView.invalidate();
    } else if ( b == mButton2[k2++] ) { // mBtnDevice
      if ( mApp.mBTAdapter.isEnabled() ) {
        intent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
        startActivity( intent );
      }
    } else if ( b == mButton2[k2++] ) { // mBtnAdd 
      // mSecondLastShotId = mApp.lastShotId( );
      DistoXDBlock last_blk = mApp.mData.selectLastLegShot( mApp.mSID );
      (new ShotNewDialog( this, mApp, this, last_blk, -1L )).show();
    } else if ( b == mButton2[k2++] ) { // mBtnInfo
      intent = new Intent( this, SurveyActivity.class );
      intent.putExtra( TopoDroidApp.TOPODROID_SURVEY,  0 ); // mustOpen 
      intent.putExtra( TopoDroidApp.TOPODROID_OLDSID, -1 ); // old_sid 
      intent.putExtra( TopoDroidApp.TOPODROID_OLDID,  -1 ); // old_id 
      startActivityForResult( intent, INFO_ACTIVITY_REQUEST_CODE );

    } else if ( b == mButton2[k2++] ) { // mBtnUndelete
      (new DistoXUndelete(this, this, mApp.mData, mApp.mSID ) ).show();
      updateDisplay( );
    } else if ( b == mButton2[k2++] ) { // mBtnCamera
      intent = new Intent( this, PhotoActivity.class );
      startActivity( intent );
    } else if ( b == mButton2[k2++] ) { // mBtnSensor
      intent = new Intent( this, SensorListActivity.class );
      startActivity( intent );
    } else if ( b == mButton2[k2++] ) { // mBtn3D
      mApp.exportSurveyAsTh(); // make sure to have survey exported as therion
      try {
        intent = new Intent( "Cave3D.intent.action.Launch" );
        intent.putExtra( "survey", mApp.getSurveyThFile() );
        startActivity( intent );
      } catch ( ActivityNotFoundException e ) {
        Toast.makeText( this, R.string.no_cave3d, Toast.LENGTH_SHORT ).show();
      }
    }
  }

  // ------------------------------------------------------------------

  // private void setBTMenus( boolean enabled )
  // {
  //   // if ( mBtnDownload != null ) mBtnDownload.setEnabled( enabled );
  //   mButton1[0].setEnabled( enabled );
  // }

  // public void makeNewPlot( String name, long type, String start, String view )
  public void makeNewPlot( String name, String start )
  {
    // plot-id -1, status 0, azimuth 0.0f
    long mPIDp = mApp.mData.insertPlot( mApp.mSID, -1L, name+"p",
                 PlotInfo.PLOT_PLAN, 0L, start, "", 0, 0, TopoDroidApp.mScaleFactor, 0.0f );
    long mPIDs = mApp.mData.insertPlot( mApp.mSID, -1L, name+"s",
                 PlotInfo.PLOT_EXTENDED, 0L, start, "", 0, 0, TopoDroidApp.mScaleFactor, 0.0f );
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
  //     if ( ! mApp.mData.hasShot( mApp.mSID, st1, st2 ) ) {
  //       Toast.makeText(getApplicationContext(), R.string.no_shot_between_stations, Toast.LENGTH_SHORT).show();
  //       return;
  //     }
  //   } else {
  //     st2 = mApp.mData.nextStation( mApp.mSID, st1 );
  //   }
  //   if ( st2 != null ) {
  //     float e = 0.0f; // NOTE (e,s,v) are the coord of station st1, and st1 is taken as the origin of the ref-frame
  //     float s = 0.0f;
  //     float v = 0.0f;
  //     long mPID = mApp.mData.insertSketch3d( mApp.mSID, -1L, name, 0L, st1, st1, st2,
  //                                           0, // mApp.mDisplayWidth/(2*TopoDroidApp.mScaleFactor),
  //                                           0, // mApp.mDisplayHeight/(2*TopoDroidApp.mScaleFactor),
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
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "startExistingPlot \"" + name + "\" type " + type + " sid " + mApp.mSID );

    // FIXME_SKETCH_3D
    // if ( type.startsWith( "Sketch" ) ) {
    //   Sketch3dInfo sketch = mApp.mData.getSketch3dInfo( mApp.mSID, name );
    //   if ( sketch != null ) {
    //     startSketchActivity( sketch.name );
    //     return;
    //   }
    // } else {
      PlotInfo plot1 =  mApp.mData.getPlotInfo( mApp.mSID, name+"p" );
      if ( plot1 != null ) {
        PlotInfo plot2 =  mApp.mData.getPlotInfo( mApp.mSID, name+"s" );
        startDrawingActivity( plot1.start, plot1.name, plot1.id, plot2.name, plot2.id );
        return;
      }
    // }
    Toast.makeText(getApplicationContext(), R.string.plot_not_found, Toast.LENGTH_SHORT).show();
  }
 
  // FIXME_SKETCH_3D
  // private void startSketchActivity( String name )
  // {
  //   if ( mApp.mSID < 0 ) {
  //     Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
  //     return;
  //   }
  //   // TODO
  //   Intent sketchIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, SketchActivity.class );
  //   sketchIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY_ID, mApp.mSID );
  //   sketchIntent.putExtra( TopoDroidApp.TOPODROID_SKETCH_NAME, name );
  //   startActivity( sketchIntent );
  // }

  private void startDrawingActivity( String start, String plot1_name, long plot1_id, String plot2_name, long plot2_id )
  {
    if ( mApp.mSID < 0 || plot1_id < 0 || plot2_id < 0 ) {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
      return;
    }
    
    Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingActivity.class );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY_ID, mApp.mSID );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_NAME, plot1_name );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_NAME2, plot2_name );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_TYPE, PlotInfo.PLOT_PLAN );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_FROM, start );
    // drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_ID, plot1_id ); // not necessary
    // drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_ID2, plot2_id ); // not necessary

    startActivity( drawIntent );
  }

  /**
   * @param at   id of the shot before which to insert the new shot (and LRUD)
   *
  public DistoXDBlock makeNewShot( long at, String from, String to,
                           float distance, float bearing, float clino, long extend,
                           String left, String right, String up, String down )
  {
    DistoXDBlock ret = null;
    distance /= TopoDroidApp.mUnitLength;
    bearing  /= TopoDroidApp.mUnitAngle;
    clino    /= TopoDroidApp.mUnitAngle;
    if ( ( distance < 0.0f ) ||
         ( clino < -90.0f || clino > 90.0f ) ||
         ( bearing < 0.0f || bearing >= 360.0f ) ) {
      Toast.makeText( this, R.string.illegal_data_value, Toast.LENGTH_SHORT ).show();
      return NULL;
    }

    long id;
    long sid = mApp.mSID;
    DataHelper data = mApp.mData;
    if ( from != null && to != null && from.length() > 0 ) {
      // if ( data.makesCycle( -1L, sid, from, to ) ) {
      //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_SHORT ).show();
      // } else
      {
        // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "makeNewShot Data " + distance + " " + bearing + " " + clino );
        boolean horizontal = ( Math.abs( clino ) > mApp.mVThreshold );
        // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "makeNewShot SID " + sid + " LRUD " + left + " " + right + " " + up + " " + down);
        if ( left != null && left.length() > 0 ) {
          float l = Float.parseFloat( left ) / TopoDroidApp.mUnitLength;
          if ( l >= 0.0f ) {
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
        }
        if ( right != null && right.length() > 0 ) {
          float r = Float.parseFloat( right ) / TopoDroidApp.mUnitLength;
          if ( r >= 0.0f ) {
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
        }
        if ( up != null && up.length() > 0 ) {
          float u = Float.parseFloat( up ) / TopoDroidApp.mUnitLength;
          if ( u >= 0.0f ) {
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
        }
        if ( down != null && down.length() > 0 ) {
          float d = Float.parseFloat( down ) / TopoDroidApp.mUnitLength;
          if ( d >= 0.0f ) {
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
  //   mApp.mData.deleteShot( blk.mId, mApp.mSID );
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
    List<DistoXDBlock> list = mApp.mData.selectShotsAfterId( mApp.mSID, id , 0 );
    for ( DistoXDBlock b : list ) {
      if ( b.isTypeBlank() ) {
        // Log.v( TopoDroidApp.TAG, "BLANK " + b.mLength + " " + b.mBearing + " " + b.mClino );
        if ( ret != null ) {
          if ( ret.relativeDistance( b ) < mApp.mCloseDistance ) return ret;
        }
        ret = b;
      } else if ( b.mType == DistoXDBlock.BLOCK_SEC_LEG ) {
        // Log.v( TopoDroidApp.TAG, "LEG " + b.mLength + " " + b.mBearing + " " + b.mClino );
        if ( ret != null ) {
          if ( ret.relativeDistance( b ) < mApp.mCloseDistance ) return ret;
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
                  && b.relativeDistance( mDataAdapter.get(mNextPos+1) ) < mApp.mCloseDistance ) {
        return b;
      }
      ++ mNextPos;
    }
    return null;
    // DistoXDBlock nextBlock = mApp.mData.selectNextLegShot( blk.mId, mApp.mSID );
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
    // DistoXDBlock prevBlock = mApp.mData.selectNextLegShot( blk.mId, mApp.mSID );
    // return prevBlock;
  }

  public void updateShot( String from, String to, long extend, long flag, boolean leg, String comment, DistoXDBlock blk )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "updateShot From >" + from + "< To >" + to + "< comment " + comment );
    // Log.v( TopoDroidApp.TAG, "updateShot pos " + mShotPos + " From >" + from + "< To >" + to + "< comment " + comment );

    int ret = mApp.mData.updateShot( blk.mId, mApp.mSID, from, to, extend, flag, leg?1:0, comment );
    if ( ret == -1 ) {
      Toast.makeText( this, R.string.no_db, Toast.LENGTH_SHORT ).show();
    // } else if ( ret == -2 ) {
    //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_SHORT ).show();
    } else {
      // update same shots of the given block
      List< DistoXDBlock > blk_list = mApp.mData.selectShotsAfterId( blk.mId, mApp.mSID, 0L );
      for ( DistoXDBlock blk1 : blk_list ) {
        if ( blk1.relativeDistance( blk ) > mApp.mCloseDistance ) break;
        mApp.mData.updateShotLeg( blk1.mId, mApp.mSID, 1L );
      }
      // if ( mApp.mListRefresh ) {
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

      // mDataAdapter.notifyDataSetChanged(); // FIXME FIXME FIXME
    }

    // scrollTo( mShotPos, blk );
    mDataAdapter.updateBlockView( blk );
  }

  void updateBlockViews()
  {
    // mDataAdapter.updateBlockViews( mSavePos-10, mSavePos+10 );
    // mList.invalidateViews();
    // mList.smoothScrollToPosition( mFirstPos );
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

  // ---------------------------------------------------------
  // MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIsymbol  = menu.add( R.string.menu_palette );
    mMIoptions = menu.add( R.string.menu_options );
    mMIhelp    = menu.add( R.string.menu_help  );

    mMIsymbol.setIcon( R.drawable.ic_symbol );
    mMIoptions.setIcon( R.drawable.ic_pref );
    mMIhelp.setIcon( R.drawable.ic_help );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "TopoDroidActivity onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    if ( item == mMIsymbol ) { 
      DrawingBrushPaths.makePaths( getResources() );
      (new SymbolEnableDialog( this, this )).show();
    } else if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
      startActivity( intent );
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      (new HelpDialog(this, icons, help_texts ) ).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

}

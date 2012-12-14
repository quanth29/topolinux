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

public class ShotActivity extends Activity
                          implements OnItemClickListener
                        , OnItemLongClickListener
                        , ILister
                        , INewPlot
{
  private static final String TAG = "DistoX";
  private TopoDroidApp app;
  private static final int SENSOR_ACTIVITY_REQUEST_CODE = 1;
  private static final int EXTERNAL_ACTIVITY_REQUEST_CODE = 2;
  private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

  // private static final int REQUEST_DEVICE    = 1;
  private static final int REQUEST_ENABLE_BT = 2;

  private boolean mSplay = true; //!< whether to show splay shots
  private boolean mLeg   = true; //!< whether to hide leg extra shots
  private boolean mBlank = false; //!< whether to hide blank shots
  // private Bundle mSavedState = null;

  private ListView mList;
  // private int mListPos = -1;
  // private int mListTop = 0;
  private DistoXDBlockAdapter   mDataAdapter;

  private long mLastExtend; // id of the last-extend-ed splay 

  private long mSIDid = -1;    // id of the "current" shot (for the photo)
  // private String mSaveData = "";
  // private TextView mSaveTextView = null;
  private int mShotPos  = 0;   // shot entry position
  private int mPrevPos  = 0;   // prev shot entry position
  private int mNextPos  = 0;   // next shot entry position
  // private DistoXDBlock mSaveBlock = null;

  // String mPhotoTitle;
  long   mSensorId;
  long   mPhotoId;

  private MenuItem mMIdevice = null;
  private SubMenu  mSMsurvey;
  private MenuItem mMIsplay;
  private MenuItem mMIleg;
  private MenuItem mMIblank;
  private MenuItem mMInumber;
  // private MenuItem mMIextend;
  private MenuItem mMIplotnew;
  private MenuItem mMIshotnew;
  private MenuItem mMIundelete;
  // private MenuItem mMIlocation;
  private MenuItem mMIphoto;
  private MenuItem mMIsensor;
  private MenuItem mMI3d;
  private MenuItem mMIplot;
  private MenuItem mMInotes;
  private SubMenu  mSMmore;
  private MenuItem mMIrefresh;
  private MenuItem mMIdownload = null;
  private MenuItem mMIoptions;
  // private MenuItem mMIhelp;

  ConnHandler mHandler;

  // -------------------------------------------------------------------

  private void tryExtendSplay( DistoXDBlock item, float bearing, long extend, boolean flip )
  {
    if ( extend == 0 ) return;
    double db = Math.cos( (bearing - item.mBearing)*Math.PI/180 );
    long ext = ( db > TopoDroidApp.mExtendThr )? extend : ( db < -TopoDroidApp.mExtendThr )? -extend : 0;
    if ( flip ) ext = -ext;
    // app.mData.updateShotExtend( item.mId, app.mSID, ext ); // NOTE done by the caller
  }

  // private boolean extendSplays()
  // { 
  //   long sid = app.mSID;
  //   if ( sid < 0 ) {
  //     Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
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
  //       if ( t == DistoXDBlock.BLOCK_CENTERLINE ) {
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
       

  private boolean numberSplays()
  { 
    long sid = app.mSID;
    if ( sid < 0 ) {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      return false;
    } else {
      ArrayList<DistoXDBlock> updatelist = new ArrayList<DistoXDBlock>();
      List<DistoXDBlock> list = app.mData.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
      int size = list.size();
      int from = 0;
      int k;
      DistoXDBlock prev = null;
      for ( k=0; k<size; ++k ) {
        DistoXDBlock item = list.get( k );
        int t = item.type();
        // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "shot " + k + " type " + t + " <" + item.mFrom + "> <" + item.mTo + ">" );
        if ( from == k && t == DistoXDBlock.BLOCK_CENTERLINE ) {
          from = k + 1;
        } else if ( t == DistoXDBlock.BLOCK_SPLAY ) {
          from = k+1;
        } else if ( t == DistoXDBlock.BLOCK_CENTERLINE ) {
          prev = item;
          if ( from < k ) { // assign splay name
            String name = item.mFrom;
            if ( name != null ) {
              // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "update splays from " + from + " to " + k + " with name: <" + name + ">" );
              // set the index of the last splay to extend at the smallest from 
              for ( ; from < k; ++from ) {
                DistoXDBlock splay = list.get( from );
                splay.setName( name, "" );
                tryExtendSplay( splay, item.mBearing, item.mExtend, false );
                // app.mData.updateShotName( splay.mId, sid, name, "" );
                // app.mData.updateShotExtend( item.mId, app.mSID, ext ); // NOTE done by the caller
                updatelist.add( splay ); 
                mLastExtend = item.mId;
              }
            }
          }
        } else if ( t == DistoXDBlock.BLOCK_BLANK ) {
          if ( item.relativeDistance( prev ) < app.mCloseDistance ) {
            from = k+1;
          }
        }
      }
      if ( updatelist.size() > 0 ) {
        app.mData.updateShotNameAndExtend( sid, updatelist );
      }
    }
    return true;
  }

  @Override
  public void refreshDisplay( int nr ) 
  {
    setTitleColor( TopoDroidApp.COLOR_NORMAL );
    if ( nr >= 0 ) {
      if ( nr > 0 ) updateDisplay( );
      Toast.makeText( this, getString(R.string.read_) + nr + getString(R.string.data), Toast.LENGTH_LONG ).show();
    } else if ( nr < 0 ) {
      // Toast.makeText( this, getString(R.string.read_fail_with_code) + nr, Toast.LENGTH_LONG ).show();
      Toast.makeText( this, app.DistoXConnectionError[ -nr ], Toast.LENGTH_LONG ).show();
    }
  }
    
  public void updateDisplay( )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "updateDisplay() status: " + StatusName() + " forcing: " + force_update );
    DataHelper data = app.mData;
    if ( data != null && app.mSID >= 0 ) {
      List<DistoXDBlock> list = data.selectAllShots( app.mSID, TopoDroidApp.STATUS_NORMAL );
      // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "updateDisplay() shot list size " + list.size() );
      updateShotList( list );
      setTitle( app.getSurvey() );
    } else {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
    }
  }

  private void updateShotList( List<DistoXDBlock> list )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "updateShotList size " + list.size() );
    mDataAdapter.clear();
    mList.setAdapter( mDataAdapter );
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_shots, Toast.LENGTH_LONG ).show();
      return;
    }
    DistoXDBlock prev = null;
    boolean prev_is_leg = false;
    for ( DistoXDBlock item : list ) {
      DistoXDBlock cur = item;
      // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "item " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      int t = cur.type();
      if ( cur.relativeDistance( prev ) < app.mCloseDistance ) {
        if ( mLeg ) { // hide leg extra shots
          // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "close distance");
          if ( mBlank && prev.type() == DistoXDBlock.BLOCK_BLANK ) {
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
        if ( t == DistoXDBlock.BLOCK_BLANK ) {
          prev = cur;
          if ( mBlank ) continue;
        } else if ( t == DistoXDBlock.BLOCK_SPLAY ) {
          prev = null;
          if ( mSplay ) continue;
        } else { // t == DistoXDBlock.BLOCK_CENTERLINE
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
    mSIDid = blk.mId;
    (new PhotoSensorsDialog(this, this) ).show();
    return true;
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "ShotActivity onItemClick id " + id);
    DistoXDBlock blk = mDataAdapter.get(pos);

    mShotPos = pos;
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
       if ( prevBlock != null ) {
         // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "prev leg " + prevBlock.mFrom + " " + prevBlock.mTo );
       }
     // }
     (new ShotDialog( this, this, blk, prevBlock, nextBlock )).show();
  }


  // ---------------------------------------------------------------
  // OPTIONS MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    // MenuInflater inflater = getMenuInflater();
    // inflater.inflate(R.menu.option_menu_none, menu);

    mMIdevice   = menu.add( R.string.menu_device );
    mSMsurvey = menu.addSubMenu( R.string.menu_survey );
      mMIsplay  = mSMsurvey.add( R.string.menu_splay );
        mMIsplay.setCheckable( true );
        mMIsplay.setChecked( mSplay );
      mMIleg = mSMsurvey.add( R.string.menu_leg );
        mMIleg.setCheckable( true );
        mMIleg.setChecked( mLeg );
      mMIblank = mSMsurvey.add( R.string.menu_blank );
        mMIblank.setCheckable( true );
        mMIblank.setChecked( mBlank );
      mMInumber  = mSMsurvey.add( R.string.menu_number );
      // mMIextend  = mSMsurvey.add( R.string.menu_extend );
      mMIrefresh = mSMsurvey.add( R.string.menu_refresh );

    mMIplot     = menu.add( R.string.menu_plot );
    mMIdownload = menu.add( R.string.menu_download );
    mMInotes    = menu.add( R.string.menu_notes );

    mSMmore = menu.addSubMenu( R.string.menu_more );
      mMIplotnew  = mSMmore.add( R.string.menu_plot_new );
      mMIshotnew  = mSMmore.add( R.string.menu_shot_new );
      mMIundelete = mSMmore.add( R.string.menu_undelete );
      // mMIlocation = mSMmore.add( R.string.menu_location );
      mMIphoto    = mSMmore.add( R.string.menu_photo );
      mMIsensor   = mSMmore.add( R.string.menu_sensor );
      mMI3d       = mSMmore.add( R.string.menu_3d );
      mMIoptions  = mSMmore.add( R.string.menu_options );
      // mMIhelp     = mSMmore.add( R.string.menu_help  );

    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "menu size " + menu.size() );
    // menu has size 7

    mMIdevice.setIcon( R.drawable.distox ); 
    mMIdownload.setIcon( R.drawable.download );
    mSMsurvey.setIcon( R.drawable.survey );
    mMInotes.setIcon( R.drawable.compose );

    mMIplot.setIcon( R.drawable.display );
    // mMIplotnew.setIcon( R.drawable.scrapnew );
    mSMmore.setIcon( R.drawable.more );

    setBTMenus( app.mBTAdapter.isEnabled() );

    mHandler = new ConnHandler( app, this );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    if ( item == mMIrefresh ) {
      updateDisplay( );
    } else if ( item == mMIdownload ) {
      if ( app.mDevice != null && app.mDevice.length() > 0 ) {
        setTitleColor( TopoDroidApp.COLOR_CONNECTED );
        // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "shot menu DOWNLOAD" );
        new DistoXRefresh( app, this ).execute();
        // updateDisplay( );
      } else {
        Toast.makeText( this, R.string.device_none, Toast.LENGTH_LONG ).show();
      }
    } else if ( item == mMInotes ) { // ANNOTATIONS DIALOG
      if ( app.getSurvey() != null ) {
        Intent notesIntent = new Intent( this, DistoXAnnotations.class );
        notesIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY, app.getSurvey() );
        startActivity( notesIntent );
      } else {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      }
    } else if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
      optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
      startActivity( optionsIntent );
    // } else if ( item == mMIhelp ) { // HELP DIALOG
    //   TopoDroidHelp.show( this, R.string.help_shot );
    } else if ( item == mMIundelete ) { // UNDELETE SURVEY ITEM
      if ( app.mData != null && app.mSID >= 0 ) {
        (new DistoXUndelete(this, this, app.mData, app.mSID ) ).show();
        updateDisplay( );
      } else {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      }
    } else if ( item == mMIphoto ) { // photo listing
      Intent photoIntent = new Intent( this, PhotoActivity.class );
      startActivity( photoIntent );
    } else if ( item == mMIsensor ) {  // sensor listing
      Intent sensorIntent = new Intent( this, SensorListActivity.class );
      startActivity( sensorIntent );
    } else if ( item == mMI3d ) { // 3D
      app.exportSurveyAsTh(); // make sure to have survey exported as therion
      Intent intent = new Intent( "Cave3D.intent.action.Launch" );
      intent.putExtra( "survey", app.getSurveyThFile() );
      try {
        startActivity( intent );
      } catch ( ActivityNotFoundException e ) {
        Toast.makeText( this, R.string.no_cave3d, Toast.LENGTH_LONG ).show();
      }
    // } else if ( item == mMIlocation ) {
    //   LocationManager lm = (LocationManager) getSystemService( LOCATION_SERVICE );
    //   DistoXLocation loc = new DistoXLocation( this, this, lm );
    //   loc.show();
    } else if ( item == mMIsplay ) {     // toggle splay shots
      mSplay = ! mMIsplay.isChecked();
      mMIsplay.setChecked( mSplay );
      updateDisplay( );
    } else if ( item == mMIleg ) { // toggle leg extra shots
      mLeg =  ! mMIleg.isChecked();
      mMIleg.setChecked( mLeg );
      updateDisplay( );
    } else if ( item == mMIblank ) {     // toggle blank shots
      mBlank = ! mMIblank.isChecked();
      mMIblank.setChecked( mBlank );
      updateDisplay( );
    } else if ( item == mMInumber ) {    // autonumber splays
      // TODO number splay shots
      if ( numberSplays() ) {
        updateDisplay( );
      }
    // } else if ( item == mMIextend ) { // extend splays according to next shot
    //   if ( extendSplays() ) {
    //     updateDisplay( );
    //   }
    // // ---------------------- DEVICES
    } else if ( item == mMIdevice ) {
      if ( app.mBTAdapter.isEnabled() ) {
        Intent deviceIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
        startActivity( deviceIntent );
      }
    // ---------------------- SHOTS
    } else if ( item == mMIshotnew ) {
      if ( app.mSID < 0 ) {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      } else {
        (new ShotNewDialog( this, this )).show();
        updateDisplay( );
      }
    // ---------------------- PLOTS
    } else if ( item == mMIplotnew ) {
      if ( app.mSID < 0 ) {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      } else {
        (new DistoXPlotDialog( this, this )).show();
        // FIXME start Drawing activity
        // updateDisplay( );
      }
    } else if ( item == mMIplot ) {
      if ( app.mSID < 0 ) {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      } else {
        new PlotDialog( this, this, app ).show();
      }
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

  void askPhoto( )
  {
    // using mSIDid as shotid
    mPhotoId      = app.mData.nextPhotoId( app.mSID );

    // imageFile := PHOTO_DIR / surveyId / photoId .jpg
    File imagefile = new File( app.getSurveyJpgFile( mPhotoId ) );
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "photo " + imagefile.toString() );

    Uri outfileuri = Uri.fromFile( imagefile );
    Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
    intent.putExtra( MediaStore.EXTRA_OUTPUT, outfileuri );
    intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
    startActivityForResult( intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE );
  }

  void askSensor( )
  {
    // using mSIDid as shotid
    mSensorId = app.mData.nextSensorId( app.mSID );
    TopoDroidApp.Log( TopoDroidApp.LOG_SENSOR, "sensor " + mSensorId );
    Intent intent = new Intent( this, SensorActivity.class );
    startActivityForResult( intent, SENSOR_ACTIVITY_REQUEST_CODE );
  }

  void askExternal( )
  {
    // using mSIDid as shotid
    mSensorId = app.mData.nextSensorId( app.mSID );
    TopoDroidApp.Log( TopoDroidApp.LOG_SENSOR, "sensor " + mSensorId );
    Intent intent = new Intent( this, ExternalActivity.class );
    startActivityForResult( intent, EXTERNAL_ACTIVITY_REQUEST_CODE );
  }

  void insertPhoto( String comment )
  {
    // long shotid = 0;
    app.mData.insertPhoto( app.mSID, mPhotoId, mSIDid, "", comment ); // FIXME TITLE 
  }

  @Override
  protected void onActivityResult( int reqCode, int resCode, Intent data )
  {
    switch ( reqCode ) {
      case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
        if ( resCode == Activity.RESULT_OK ) {
          // ask the photo comment
          (new PhotoCommentDialog(this, this) ).show();
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
          app.mData.insertSensor( app.mSID, mSensorId, mSIDid, "", 
                                  sdf.format( new Date() ),
                                  comment,
                                  type,
                                  value );
        }
        break;
    }
  }

  // ---------------------------------------------------------------

  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.main);
    app = (TopoDroidApp) getApplication();
    mDataAdapter = new DistoXDBlockAdapter( this, R.layout.row, new ArrayList<DistoXDBlock>() );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    mList.setOnItemClickListener( this );
    mList.setLongClickable( true );
    mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );

    restoreInstanceFromData();

    mLastExtend = app.mData.getLastShotId( app.mSID );
    updateDisplay( );
  }

  @Override
  public synchronized void onPause() 
  {
    super.onPause();
    app.unregisterConnListener( mHandler );
    // if ( app.mComm != null ) { app.mComm.suspend(); }
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
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
    }
  }
    
  private void saveInstanceToData()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("%d %d %d", mSplay?1:0, mLeg?1:0, mBlank?1:0 );
    app.mData.setValue( "DISTOX_SHOTS", sw.getBuffer().toString() );
  }


  // ------------------------------------------------------------------

  private void setBTMenus( boolean enabled )
  {
    if ( mMIdevice != null )   mMIdevice.setEnabled( enabled );
    if ( mMIdownload != null ) mMIdownload.setEnabled( enabled );
  }

  public void makeNewPlot( String name, long type, String start, String view )
  {
    // plot-id -1, status 0
    long mPID = app.mData.insertPlot( app.mSID, -1L, name, type, 0L, start, view );
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "insertPlot " + mPID + " " + name + " start " + start + " view " + view );
    if ( mPID >= 0 ) {
      startDrawingActivity( start, name, type, view );
    }
    updateDisplay( );
  }

  public void startPlot( String plot_name, String type )
  {
    DataHelper data = app.mData;
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "startPlot \"" + plot_name + "\" sid " + app.getSurveyId() );
    long mPID = data.getPlotId( app.getSurveyId(), plot_name );
    if ( mPID >= 0 ) {
      long id = app.mSID;
      // long plot_type = data.getPlotFieldAsLongType( id, mPID, "type" );
      String plot_start = data.getPlotFieldAsString( id, mPID, "start" );
      String plot_view  = null;
      long plot_type = TopoDroidApp.PLOT_PLAN;
      if ( type.equals("V-SECTION") ) { 
        plot_type = TopoDroidApp.PLOT_V_SECTION;
        plot_view = data.getPlotFieldAsString( id, mPID, "view" );
      } else if ( type.equals("PLAN") ) {
        plot_type = TopoDroidApp.PLOT_PLAN;
      } else if ( type.equals("EXTENDED") ) {
        plot_type = TopoDroidApp.PLOT_EXTENDED;
      } else if ( type.equals("H-SECTION") ) {
        plot_type = TopoDroidApp.PLOT_H_SECTION;
        plot_view = data.getPlotFieldAsString( id, mPID, "view" );
      }
      startDrawingActivity( plot_start, plot_name, plot_type, plot_view );
      updateDisplay( );
    } else {
      Toast.makeText(getApplicationContext(), R.string.plot_not_found, Toast.LENGTH_LONG).show();
    }
  }

  private void startDrawingActivity( String start, String plot_name, long plot_type, String viewed )
  {
    if ( app.mSID < 0 ) {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      return;
    }
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "startDrawingActivity start " + start + " viewed " + viewed );
    // FIXME what if plot_name already exists ? 
    long mPID = app.mData.getPlotId( app.mSID, plot_name );
    String filename   = app.getSurvey() + "-" + plot_name;
    Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingActivity.class );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY_ID, app.getSurveyId() );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_ID,   mPID );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_STRT, start );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_VIEW, viewed );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_TYPE, plot_type );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_FILE, filename );
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "startDrawing plot " + plot_name + " SID " + app.getSurveyId() + " PID " + mPID + " start at " + start );
    startActivity( drawIntent );
  }

  public void makeNewShot( String from, String to, float distance, float bearing, float clino, long extend,
                           String left, String right, String up, String down )
  {
    long id;
    long sid = app.mSID;
    DataHelper data = app.mData;
    if ( from != null && to != null && from.length() > 0 && to.length() > 0 ) {
      // if ( data.makesCycle( -1L, sid, from, to ) ) {
      //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_LONG ).show();
      // } else
      {
        // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "makeNewShot Data " + distance + " " + bearing + " " + clino );
        boolean horizontal = ( Math.abs( clino ) > app.mVThreshold );
        // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "makeNewShot SID " + sid + " LRUD " + left + " " + right + " " + up + " " + down);
        if ( left != null && left.length() > 0 ) {
          float l = Float.parseFloat( left );
          if ( horizontal ) {
            id = data.insertShot( sid, -1L, l, 270.0f, 0.0f, 0.0f );
          } else {
            float b = bearing - 90.0f;
            if ( b < 0.0f ) b += 360.0f;
            // b = in360( b );
            id = data.insertShot( sid, -1L, l, b, 0.0f, 0.0f );
          }
          data.updateShotName( id, sid, from, "" );
        }
        if ( right != null && right.length() > 0 ) {
          float r = Float.parseFloat( right );
          if ( horizontal ) {
            id = data.insertShot( sid, -1L, r, 90.0f, 0.0f, 0.0f );
          } else {
            float b = bearing + 90.0f;
            if ( b >= 360.0f ) b -= 360.0f;
            id = data.insertShot( sid, -1L, r, b, 0.0f, 0.0f );
          }
          data.updateShotName( id, sid, from, "" );
        }
        if ( up != null && up.length() > 0 ) {
          float u = Float.parseFloat( up );
          if ( horizontal ) {
            id = data.insertShot( sid, -1L, u, 0.0f, 0.0f, 0.0f );
          } else {
            id = data.insertShot( sid, -1L, u, 0.0f, 90.0f, 0.0f );
          }
          data.updateShotName( id, sid, from, "" );
        }
        if ( down != null && down.length() > 0 ) {
          float d = Float.parseFloat( down );
          if ( horizontal ) {
            id = data.insertShot( sid, -1L, d, 180.0f, 0.0f, 0.0f );
          } else {
            id = data.insertShot( sid, -1L, d, 0.0f, -90.0f, 0.0f );
          }
          data.updateShotName( id, sid, from, "" );
        }
        id = data.insertShot( sid, -1L, distance, bearing, clino, 0.0f );
        // String name = from + "-" + to;
        data.updateShotName( id, sid, from, to );
        // data.updateShotExtend( id, sid, extend );
        updateDisplay( );
      }
    } else {
      Toast.makeText( this, R.string.missing_station, Toast.LENGTH_LONG ).show();
    }
  }

  public void dropShot( DistoXDBlock blk )
  {
    app.mData.deleteShot( blk.mId, app.mSID );
    updateDisplay( ); // FIXME
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
      if ( t == DistoXDBlock.BLOCK_CENTERLINE ) {
        return b;
      } else if (    t == DistoXDBlock.BLOCK_BLANK 
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
      if ( b.type() == DistoXDBlock.BLOCK_CENTERLINE ) {
        return b;
      }
    }
    return null;
    // DistoXDBlock prevBlock = app.mData.selectNextLegShot( blk.mId, app.mSID );
    // return prevBlock;
  }

  public void updateShot( String from, String to, long extend, long flag, String comment, DistoXDBlock blk )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_SHOT, "updateShot From >" + from + "< To >" + to + "< comment " + comment );
    int ret = app.mData.updateShot( blk.mId, app.mSID, from, to, extend, flag, comment );
    if ( ret == -1 ) {
      Toast.makeText( this, R.string.no_db, Toast.LENGTH_LONG ).show();
    // } else if ( ret == -2 ) {
    //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_LONG ).show();
    } else {
      if ( app.mListRefresh ) {
        // This works but it refreshes the whole list
        mDataAdapter.notifyDataSetChanged();
        // mList.smoothScrollToPosition( mSaveTextPos );
        // mSaveTextView.requestLayout();
        // mSaveTextView.requestFocus();
      } else {
        // mSaveTextView.setText( blk.toString() );
        // mSaveTextView.setTextColor( blk.color() );
        mDataAdapter.notifyDataSetChanged(); // FIXME
      }
    }
  }
}

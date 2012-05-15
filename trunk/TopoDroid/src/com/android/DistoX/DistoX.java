/* @file DistoX.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid main class: survey management
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
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
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

// import java.lang.Long;
// import java.lang.reflect.Method;
// import java.lang.reflect.InvocationTargetException;

import android.app.Application;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
// import android.view.MenuInflater;
// import android.content.res.ColorStateList;
import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;
// import android.os.Parcelable;

import android.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.location.LocationManager;

import android.content.Context;
import android.content.Intent;

import android.widget.ArrayAdapter;
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

/*
  Method m = device.getClass().getMethod( "createRfcommSocket", new Class[] (int.class) );
  socket = (BluetoothSocket) m.invoke( device, 2 );
  socket.connect();
*/

public class DistoX extends Activity
                    implements OnItemClickListener, ILister
{
  private static final String TAG = "DistoX";
  private TopoDroidApp app;

  // private static final int REQUEST_DEVICE    = 1;
  private static final int REQUEST_ENABLE_BT = 2;

  // statuses
  private static final int STATUS_NONE   = 0;
  private static final int STATUS_SURVEY = 1;
  private static final int STATUS_SHOT   = 2;
  private static final int STATUS_PLOT   = 3;

  private int mStatus    = STATUS_NONE;
  private int mOldStatus = STATUS_NONE;
  private boolean mSplay = true; //!< whether to show splay shots
  private boolean mLeg   = true; //!< whether to hide leg extra shots
  private boolean mBlank = true; //!< whether to hide blank shots
  // private Bundle mSavedState = null;

  private TextView mSplash;
  private ListView mList;
  // private Button   mButton;
  // private int mListPos = -1;
  // private int mListTop = 0;
  private boolean mNeedUpdate = true;

  private ArrayAdapter<String> mArrayAdapter;
  private DistoXDBlockAdapter   mDataAdapter;

  private long mSIDid = -1;    // id of the shot
  private long mPID = -1;      // id of the plot

  private String mSaveData = "";
  private TextView mSaveTextView = null;
  // private int      mSaveTextPos  = 0;
  private DistoXDBlock mSaveBlock = null;

  private long mCursor = 0;

  // private SubMenu  mSMdevice;
  // private MenuItem mMIdevice;
  // private MenuItem mMIpaired;
  // private MenuItem mMIscan;
  private MenuItem mMIdevice = null;

  private SubMenu  mSMsurvey;
  private MenuItem mMIsplay;
  private MenuItem mMIleg;
  private MenuItem mMIblank;
  private MenuItem mMInumber;
  private MenuItem mMIplotnew;
  private MenuItem mMIexport;
  // privtae MenuItem mMIstats;
  private MenuItem mMIsurveynew;
  private MenuItem mMIshotnew;
  private MenuItem mMIsurveys;
  private MenuItem mMIundelete;
  private MenuItem mMIlocation;

  // private SubMenu  mSMplot;
  private MenuItem mMIplot;
  // private MenuItem mMIplotnew;
  // private MenuItem mMIplan;
  // private MenuItem mMIext;
  // private MenuItem mMIcross;

  private MenuItem  mMInotes;

  private SubMenu  mSMmore;

  private MenuItem mMIcalib;
  private MenuItem mMIrefresh;
  private MenuItem mMIdownload = null;

  private MenuItem  mMIoptions;

  // -------------------------------------------------------------------
  // forward survey name to DataHelper

  // public int getStatus() { return mStatus; }
  public void resetOldStatus() { mStatus = mOldStatus; }
  public void setAllStatus( int s ) { mOldStatus = mStatus = s; }

  public void setStatus( int s ) 
  {
    if ( mStatus == STATUS_SHOT ) {
      mOldStatus = mStatus;
    }
    mStatus = s;
  }

  public void resetStatus() 
  {
    if ( mStatus != STATUS_SHOT ) {
      mStatus = mOldStatus;
    }
  }

  // -------------------------------------------------------------
  public long getSurveyID() { return app.getSurveyId(); } // FIXME getSurveyId()
  public long getPlotID()   { return mPID; }

  public String getSurvey() { return app.getSurvey(); }

  public void setSurveyFromName( String survey ) 
  {
    setAllStatus( STATUS_SHOT );
    app.setSurveyFromName( survey );
  }

  // -------------------------------------------------------------------

  private boolean numberSplays()
  { 
    long sid = app.getSurveyId();
    if ( sid < 0 ) {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      return false;
    } else {
      List<DistoXDBlock> list = app.mData.selectAllShots( sid, 0 ); // status = 0
      int size = list.size();
      int from = 0;
      int k;
      DistoXDBlock prev = null;
      for ( k=0; k<size; ++k ) {
        DistoXDBlock item = list.get( k );
        int t = item.type();
        if ( t == DistoXDBlock.BLOCK_SPLAY ) {
          from = k+1;
        } else if ( t == DistoXDBlock.BLOCK_CENTERLINE ) {
          prev = item;
          if ( from < k ) { // assign splay name
            String name = item.mFrom;
            if ( name != null ) {
              // Log.v( TAG, "update splays from " + from + " to " + k + " with name: <" + name + ">" );
              for ( ; from < k; ++from ) {
                DistoXDBlock cur = list.get( from );
                cur.setName( name, "" );
                app.mData.updateShotName( cur.mId, sid, name, "" );
              }
            }
          }
        } else if ( t == DistoXDBlock.BLOCK_BLANK ) {
          if ( item.relativeDistance( prev ) < app.mCloseDistance ) {
            from = k+1;
          }
        }
      }
    }
    return true;
  }

  private void exportSurvey()
  {
    if ( app.getSurveyId() < 0 ) {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
    } else {
      String filename = null;
      switch ( app.mExportType ) {
        case TopoDroidApp.DISTOX_EXPORT_TLX:
          filename = app.exportSurveyAsTlx();
          break;
        case TopoDroidApp.DISTOX_EXPORT_TH:
          filename = app.exportSurveyAsTh();
          break;
        case TopoDroidApp.DISTOX_EXPORT_DAT:
          filename = app.exportSurveyAsDat();
          break;
        case TopoDroidApp.DISTOX_EXPORT_SVX:
          filename = app.exportSurveyAsSvx();
          break;
        case TopoDroidApp.DISTOX_EXPORT_TRO:
          filename = app.exportSurveyAsTro();
          break;
      }
      if ( filename != null ) {
        Toast.makeText( this, getString(R.string.saving_) + filename, Toast.LENGTH_LONG ).show();
      } else {
        Toast.makeText( this, R.string.saving_file_failed, Toast.LENGTH_LONG ).show();
      }
    }
  }

  @Override
  public void refreshDisplay( int nr ) 
  {
    if ( nr >= 0 ) {
      if ( nr > 0 ) updateDisplay( true );
      Toast.makeText( this, getString(R.string.read_) + nr + getString(R.string.data), Toast.LENGTH_LONG ).show();
    } else if ( nr < 0 ) {
      // Toast.makeText( this, getString(R.string.read_fail_with_code) + nr, Toast.LENGTH_LONG ).show();
      Toast.makeText( this, app.DistoXConnectionError[ -nr ], Toast.LENGTH_LONG ).show();
    }
  }
    
  public void updateDisplay( boolean force_update )
  {
    // Log.v( TAG, "updateDisplay() status: " + StatusName() + " forcing: " + force_update );
    if ( force_update ) {
      // mArrayAdapter.clear();
      DistoXDataHelper data = app.mData;
      switch ( mStatus ) {
        case STATUS_NONE:
          break;
        case STATUS_SURVEY:
          if ( data != null ) {
            List<String> list = data.selectAllSurveys();
            // list.add( "new_survey" );
            setTitle( R.string.title_survey );
            updateList( list );
          }
          break;
        case STATUS_SHOT:
          if ( data != null && app.getSurveyId() >= 0 ) {
            List<DistoXDBlock> list = data.selectAllShots( app.getSurveyId(), 0 );
            // Log.v( TAG, "update shot list size " + list.size() );
            updateShotList( list );
            setTitle( app.getSurvey() );
          } else {
            Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
          }
          break;
        case STATUS_PLOT:
          if ( data != null && app.getSurveyId() >= 0 ) {
            List< DistoXPlot > list = data.selectAllPlots( app.getSurveyId(), 0 ); // status = 0
            setTitle( app.getSurvey() + " scraps" );
            updatePlotList( list );
          } else {
            Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
          }
          break;
          
      }
    }
  }

  private void updateList( List<String> list )
  {
    // Log.v(TAG, "updateList" );
    mList.setAdapter( mArrayAdapter );
    mArrayAdapter.clear();
    for ( String item : list ) {
      mArrayAdapter.add( item );
    }
  }

  private void updatePlotList( List<DistoXPlot> list )
  {
    // Log.v(TAG, "updatePlotList size " + list.size() );
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_plots, Toast.LENGTH_SHORT ).show();
      setStatus( STATUS_SHOT );
      updateDisplay( true );
      return;
    }
    mList.setAdapter( mArrayAdapter ); // FIXME
    mArrayAdapter.clear();
    mArrayAdapter.add( getResources().getString(R.string.back_to_survey) );
    for ( DistoXPlot item : list ) {
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      pw.format("%d <%s> %s", item.id, item.name, item.getTypeString() );
      String result = sw.getBuffer().toString();
      mArrayAdapter.add( result );
      // Log.v( TAG, "Data " + result );
    }
    // mArrayAdapter.add("0 <new_plot> NONE");
  }

  private void updateShotList( List<DistoXDBlock> list )
  {
    // Log.v(TAG, "updateShotList size " + list.size() );
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_shots, Toast.LENGTH_SHORT ).show();
      return;
    }
    mDataAdapter.clear();
    mList.setAdapter( mDataAdapter );
    DistoXDBlock prev = null;
    boolean prev_is_leg = false;
    for ( DistoXDBlock item : list ) {
      DistoXDBlock cur = item;
      // Log.v(TAG, "item " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      int t = cur.type();
      if ( cur.relativeDistance( prev ) < app.mCloseDistance ) {
        if ( mLeg ) { // hide leg extra shots
          // Log.v(TAG, "close distance");
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
        // Log.v(TAG, "not close distance");
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
      // mArrayAdapter.add( cur.toString() );
      // Log.v(TAG, "adapter add " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      mDataAdapter.add( cur );
    }
  }

  private String myUUIDaction = "android.bleutooth.device.action.UUID";

  // ---------------------------------------------------------------
  // list items click


  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    if ( value.equals( getResources().getString( R.string.back_to_survey ) ) ) {
      setStatus( STATUS_SHOT );
      updateDisplay( true );
      return;
    }
    switch ( mStatus ) {
      case STATUS_SURVEY:
        setSurveyFromName( value );
        setStatus( STATUS_SHOT );
        // setListPos( 0 );
        updateDisplay( true );
        // setTitle( value );
        break;
      case STATUS_SHOT:
        // setListPos( position  );
        startShotDialog( (TextView)view, position );
        break;
      case STATUS_PLOT:
        String[] st = value.split( " ", 3 );
        int end = st[1].length() - 1;
        String plot_name = st[1].substring( 1, end );
        DistoXDataHelper data = app.mData;
        mPID = data.getPlotId( app.getSurveyId(), plot_name );
        if ( mPID >= 0 ) {
          // long plot_type = data.getPlotFieldAsLongType( app.getSurveyId(), mPID, "type" );
          String plot_start = data.getPlotFieldAsString( app.getSurveyId(), mPID, "start" );
          // String plot_view = data.getPlotFieldAsString( app.getSurveyId(), mPID, "view" );
          long plot_type = TopoDroidApp.PLOT_PLAN;
          if ( st[2].equals("V-SECTION") ) { plot_type = TopoDroidApp.PLOT_V_SECTION; }
          else if ( st[2].equals("PLAN") ) { plot_type = TopoDroidApp.PLOT_PLAN; }
          else if ( st[2].equals("EXTENDED") ) { plot_type = TopoDroidApp.PLOT_EXTENDED; }
          else if ( st[2].equals("H-SECTION") ) { plot_type = TopoDroidApp.PLOT_H_SECTION; }
          startDrawingActivity( plot_start, plot_name, plot_type, null );
          updateDisplay( true );
        } else {
          Toast.makeText(getApplicationContext(), R.string.plot_not_found, Toast.LENGTH_LONG).show();
        }
        break;
    }
  }
 

  private void startDrawingActivity( String start, String plot_name, long plot_type, String view )
  {
    mNeedUpdate = true;
    if ( app.getSurveyId() < 0 ) {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      return;
    }
    String filename   = app.getSurvey() + "-" + plot_name;
    Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingActivity.class );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY_ID, app.getSurveyId() );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_ID,   mPID );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_STRT, start );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_VIEW, view );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_TYPE, plot_type );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_FILE, filename );
    // Log.v( TAG, "startDrawing plot " + plot_name + " SID " + app.getSurveyId() + " PID " + mPID + " start at " + start );
    startActivity( drawIntent );

    resetStatus(); 
  }

  public void makeNewSurvey( String name, String date, String comment )
  {
    long id = app.setSurveyFromName( name );
    // Log.v( TAG, "INSERT survey id " + id + " date " + date + " name " + name + " comment " + comment );
    app.mData.updateSurveyDayAndComment( id, date, comment );
    setAllStatus( STATUS_SHOT );
    updateDisplay( true );
  }

  public void addLocation( String station, double latitude, double longitude, double altitude )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("\nfix %s %f %f %f m\n", station, latitude, longitude, altitude );
    DistoXAnnotations.append( app.getSurvey(), sw.getBuffer().toString() );
    app.addFixed( station, latitude, longitude, altitude );
  }

  public void startShotDialog( TextView tv, int pos )
  {
     mSaveBlock = mDataAdapter.get(pos);
     mSIDid = mSaveBlock.mId;

     String msg = tv.getText().toString();
     String[] st = msg.split( " ", 6 );
     // mSIDid     = Long.parseLong(st[0]); // shot id
     // int end = st[1].length() - 1;
     // String name = st[1].substring(1,end);
     // String[] stations = name.split("-", 2);
     // String from = ( stations.length < 1 || stations[0] == null)? "" : stations[0];
     // String to   = ( stations.length < 2 || stations[1] == null)? "" : stations[1];
     mSaveTextView = tv;
     // mSaveTextPos  = pos;
     mSaveData = st[2] + " " + st[3] + " " + st[4];
     // end = st[5].length() - 1;
     // char ch = st[5].charAt(1);
     // long extend = 0;
     // if ( ch == DistoXDBlock.mExtendTag[0] ) { extend = -1; }
     // else if ( ch == DistoXDBlock.mExtendTag[1] ) { extend =  0; }
     // else if ( ch == DistoXDBlock.mExtendTag[2] ) { extend =  1; }
     // else { extend = 2; }
     // DistoXDBlock block = app.mData.selectShot( mSIDid, app.getSurveyId() );
     
     DistoXDBlock prevBlock = null;
     if ( mSaveBlock.type() == DistoXDBlock.BLOCK_BLANK ) {
       prevBlock = app.mData.selectPreviousLegShot( mSIDid, app.getSurveyId() );
       // Log.v(TAG, "prev leg " + prevBlock.mFrom + " " + prevBlock.mTo );
     }

     DistoXShotDialog shot_dialog = new DistoXShotDialog( this, this, mSaveBlock, mSaveData, prevBlock );
     shot_dialog.show();
  }


  // ---------------------------------------------------------------
  // OPTIONS MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );
    mNeedUpdate = true;

    // MenuInflater inflater = getMenuInflater();
    // inflater.inflate(R.menu.option_menu_none, menu);

    // mSMdevice = menu.addSubMenu( R.string.menu_device );
    // mMIdevice = mSMdevice.add( app.mDevice );
    // mMIpaired = mSMdevice.add( R.string.menu_paired );
    // mMIscan   = mSMdevice.add( R.string.menu_scan );
    mMIdevice = menu.add( R.string.menu_device );

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
      mMIrefresh = mSMsurvey.add( R.string.menu_refresh );
    mMIshotnew = mSMsurvey.add( R.string.menu_shot_new );
    mMIundelete  = mSMsurvey.add( R.string.menu_undelete );
    mMIlocation  = mSMsurvey.add( R.string.menu_location );

    mMIplot     = menu.add( R.string.menu_plot );
    mMIdownload = menu.add( R.string.menu_download );
    mMInotes    = menu.add( R.string.menu_notes );

    mSMmore    = menu.addSubMenu( R.string.menu_more );
      mMIplotnew = mSMmore.add( R.string.menu_plot_new );
      mMIexport  = mSMmore.add( R.string.menu_save_th );
      mMIsurveys   = mSMmore.add( R.string.menu_survey_list );
      mMIsurveynew = mSMmore.add( R.string.menu_survey_new );
      mMIoptions = mSMmore.add( R.string.menu_options );
      mMIcalib   = mSMmore.add( R.string.menu_calib );

    // Log.v( TAG, "menu size " + menu.size() );
    // menu has size 7

    // mSMdevice.setIcon( R.drawable.distox ); 
    mMIdevice.setIcon( R.drawable.distox ); 
    mSMsurvey.setIcon( R.drawable.survey );
    mMIplot.setIcon( R.drawable.scrap );
    mMIdownload.setIcon( R.drawable.download );
    mMInotes.setIcon( R.drawable.compose );
    mSMmore.setIcon( R.drawable.more );

    // mMIdevice.setIcon( android.R.drawable.ic_menu_mylocation ); 
    // mSMsurvey.setIcon( android.R.drawable.ic_menu_agenda );
    // mMIplot.setIcon( android.R.drawable.ic_menu_gallery );
    // mMIdownload.setIcon( android.R.drawable.ic_menu_upload );
    // mMInotes.setIcon( android.R.drawable.ic_menu_edit );
    // mSMmore.setIcon( android.R.drawable.ic_menu_more );
    
    // mMIrefresh.setIcon( R.drawable.refresh );
    // mMIoptions.setIcon( R.drawable.prefs );

    setBTMenus( app.mBTAdapter.isEnabled() );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // Log.v( TAG, "onOptionsItemSelected() " + StatusName() );
    // Handle item selection
    if ( item == mMIrefresh ) {
      updateDisplay( true );
    } else if ( item == mMIdownload ) {
      new DistoXRefresh( app, this ).execute();
      // updateDisplay( true );
    } else if ( item == mMInotes ) { // ANNOTATIONS DIALOG
      if ( app.getSurvey() != null ) {
        Intent notesIntent = new Intent( this, DistoXAnnotations.class );
        notesIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY, app.getSurvey() );
        startActivity( notesIntent );
      } else {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      }
    } else if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent optionsIntent = new Intent( this, DistoXPreferences.class );
      startActivity( optionsIntent );
    } else if ( item == mMIundelete ) { // UNDELETE SURVEY ITEM
      if ( app.mData != null && app.getSurveyId() >= 0 ) {
        // Intent undeleteIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DistoXUndelete.class );
        // undeleteIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY_UNDL, app.getSurveyId() );
        // startActivityForResult( undeleteIntent, REQUEST_UNDELETE );
        (new DistoXUndelete(this, this, app.getSurveyId() ) ).show();
      } else {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      }
    } else if ( item == mMIlocation ) {
      LocationManager lm = (LocationManager) getSystemService( LOCATION_SERVICE );
      DistoXLocation loc = new DistoXLocation( this, this, lm );
      loc.show();
    } else if ( item == mMIsurveys ) { // SURVEYS LIST
      setAllStatus( STATUS_SHOT );
      setStatus( STATUS_SURVEY );
      updateDisplay( true );
    } else if ( item == mMIsurveynew ) { // NEW SURVEY
      setStatus( STATUS_SURVEY );
      (new DistoXSurveyDialog( this, this )).show();
    } else if ( item == mMIsplay ) {     // toggle splay shots
      mSplay = ! mMIsplay.isChecked();
      mMIsplay.setChecked( mSplay );
      setStatus( STATUS_SHOT );
      updateDisplay( true );
    } else if ( item == mMIleg ) { // toggle leg extra shots
      mLeg =  ! mMIleg.isChecked();
      mMIleg.setChecked( mLeg );
      setStatus( STATUS_SHOT );
      updateDisplay( true );
    } else if ( item == mMIblank ) {     // toggle blank shots
      mBlank = ! mMIblank.isChecked();
      mMIblank.setChecked( mBlank );
      setStatus( STATUS_SHOT );
      updateDisplay( true );
    } else if ( item == mMIexport ) {    // EXPORT
      exportSurvey();
    } else if ( item == mMInumber ) {    // autonumber splays
      // TODO number splay shots
      if ( numberSplays() ) {
        updateDisplay( true );
      }
    // } else if ( item == mMIstats ) {
      // TODO ? maybe not
    // -------------------- CALIBRATIONS
    } else if ( item == mMIcalib ) {  // CALIBRATION
      mNeedUpdate = true;
      Intent calibIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, CalibActivity.class );
      startActivity( calibIntent );
    // // ---------------------- DEVICES
    } else if ( item == mMIdevice ) {
      if ( app.mBTAdapter.isEnabled() ) {
        mNeedUpdate = true;
        Intent deviceIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
        startActivity( deviceIntent );
      }
    // ---------------------- SHOTS
    } else if ( item == mMIshotnew ) {
      if ( app.getSurveyId() < 0 ) {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      } else {
        mNeedUpdate = true;
        (new DistoXShotNewDialog( this, this )).show();
        // updateDisplay( true );
      }
    // ---------------------- PLOTS
    } else if ( item == mMIplotnew ) {
      if ( app.getSurveyId() < 0 ) {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      } else {
        // setStatus( STATUS_PLOT );
        (new DistoXPlotDialog( this, this )).show();
        // updateDisplay( true );
      }
    } else if ( item == mMIplot ) {
      if ( app.getSurveyId() < 0 ) {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      } else {
        setStatus( STATUS_PLOT );
        updateDisplay( true );
      }
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }
  // ---------------------------------------------------------------

  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.main);
    app = (TopoDroidApp) getApplication();
    mArrayAdapter = new ArrayAdapter<String>( this, R.layout.message );
    mDataAdapter = new DistoXDBlockAdapter( this, R.layout.row, new ArrayList<DistoXDBlock>() );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // if ( savedInstanceState == null) {
    //   Log.v(TAG, "onCreate null savedInstanceState" );
    // } else {
    //   Bundle map = savedInstanceState.getBundle(DISTOX_KEY);
    //   restoreInstanceState( map );
    // }
    // restoreInstanceFromFile();
    restoreInstanceFromData();
    if ( app.getSurveyId() < 0 ) {
      final Dialog dial = new Dialog(this);
      dial.setContentView(R.layout.welcome);
      dial.setTitle(R.string.welcome_title);
      Button _btOK = (Button)dial.findViewById(R.id.OK);
      _btOK.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          // dial.hide();
          dial.dismiss();
        } } );
      dial.show();
    }
    // setTitleColor( 0x006d6df6 );
  }

  // private void restoreInstanceState(Bundle map )
  // {
  //   if ( map != null ) {
  //     Log.v( TAG, "onRestoreInstanceState non-null bundle");
  //     mStatus        = map.getInt( DISTOX_KEY_STATUS );
  //     mOldStatus     = map.getInt( DISTOX_KEY_OLD_STATUS );
  //     mSplay         = map.getBoolean( DISTOX_KEY_SPLAY );
  //     mLeg           = map.getBoolean( DISTOX_KEY_CENTERLINE );
  //     mBlank         = map.getBoolean( DISTOX_KEY_BLANK );
  //     String survey  = map.getString( DISTOX_KEY_SURVEY );
  //     String calib   = map.getString( DISTOX_KEY_CALIB );
  //     if ( survey != null ) setSurveyFromName( survey );
  //     if ( calib  != null ) setCalibFromName( calib );
  //   } else {
  //     Log.v( TAG, "onRestoreInstanceState null bundle");
  //     // mStatus ??
  //   }
  // }

  private void restoreInstanceFromData()
  { 
    // Log.v( TAG, "restoreInstanceFromData ");
    DistoXDataHelper data = app.mData;
    String status = data.getValue( "DISTOX_STATUS" );
    // Log.v( TAG, "DISTOX_STATUS " + status );
    if ( status != null ) {
      String[] vals = status.split( " " );
      int st1 = Integer.parseInt( vals[0] );
      int st2 = Integer.parseInt( vals[1] );
      mSplay  = vals[2].equals("1");
      mLeg    = vals[3].equals("1");
      mBlank  = vals[4].equals("1");
      setAllStatus( st2 );
      setStatus( st1 );
    }
     
    String survey = data.getValue( "DISTOX_SURVEY" );
    if ( survey != null && survey.length() > 0 ) {
      // Log.v( TAG, "DISTOX_SURVEY >" + survey + "<" );
      setSurveyFromName( survey );
    } 
  }
    
  private void saveInstanceToData()
  {
    // Log.v(TAG, "saveInstanceToData");
    DistoXDataHelper data = app.mData;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("%d %d %d %d %d",
      mStatus, mOldStatus, mSplay?1:0, mLeg?1:0, mBlank?1:0 );
    data.setValue( "DISTOX_STATUS", sw.getBuffer().toString() );
    // Log.v( TAG, "DISTOX_STATUS " + sw.getBuffer().toString() );
    // if ( app.saveOnDestroy() ) {
      // Log.v( TAG, "DISTOX_SURVEY >" + app.getSurvey() + "<" );
      // Log.v( TAG, "DISTOX_CALIB >" + app.getCalib() + "<" );
      data.setValue( "DISTOX_SURVEY", (app.getSurvey() == null)? "" : app.getSurvey() );
    // }
  }


  // @Override
  // public void onSaveInstanceState(Bundle outState) 
  // {
  //   Log.v( TAG, "onSaveInstanceState");
  //   // outState.putBundle(DISTOX_KEY, mList.saveState());
  //   outState.putInt(DISTOX_KEY_STATUS, mStatus );
  //   outState.putInt(DISTOX_KEY_OLD_STATUS, mOldStatus );
  //   outState.putBoolean(DISTOX_KEY_SPLAY, mSplay );
  //   outState.putBoolean(DISTOX_KEY_CENTERLINE, mLeg );
  //   outState.putBoolean(DISTOX_KEY_BLANK, mBlank );
  //   outState.putString(DISTOX_KEY_SURVEY, getSurvey() );
  //   outState.putString(DISTOX_KEY_CALIB, getCalib() );
  // }

  // ------------------------------------------------------------------
  // LIFECYCLE
  //
  // onCreate --> onStart --> onResume
  //          --> onSaveInstanceState --> onPause --> onStop | drawing | --> onStart --> onResume
  //          --> onSaveInstanceState --> onPause [ off/on ] --> onResume
  //          --> onPause --> onStop --> onDestroy

  @Override
  public void onStart()
  {
    super.onStart();
    // restoreInstanceFromFile();
    // Log.v( TAG, "onStart check BT " + app.mCheckBT + " enabled " + app.mBTAdapter.isEnabled() );

    if ( app.mCheckBT && ! app.mBTAdapter.isEnabled() ) {    
      Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
      startActivityForResult( enableIntent, REQUEST_ENABLE_BT );
    } else {
      // nothing to do: scanBTDEvices(); is called by menu CONNECT
    }
    setBTMenus( app.mBTAdapter.isEnabled() );
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // Log.v( TAG, "onResume " );
    if ( app.mComm != null ) { app.mComm.resume(); }

    // restoreInstanceFromFile();

    // This is necessary: switching display off/on there is the call sequence
    //    [off] onSaveInstanceState
    //    [on]  onResume
    if ( mSaveTextView != null ) {
      mSaveTextView.invalidate();
      mSaveTextView = null;
    }
    updateDisplay( mNeedUpdate );
    mNeedUpdate = false;
  }

  @Override
  protected synchronized void onPause() 
  { 
    super.onPause();
    // Log.v( TAG, "onPause " );
    if ( app.mComm != null ) { app.mComm.suspend(); }
  }

  @Override
  public synchronized void onStop()
  { 
    super.onStop();
    // Log.v( TAG, "onStop " );
    // mSavedState = new Bundle();
  }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
    // Log.v( TAG, "onDestroy " );
    // FIXME if ( app.mComm != null ) { app.mComm.interrupt(); }
    saveInstanceToData();
  }

  // ------------------------------------------------------------------

  private void setBTMenus( boolean enabled )
  {
    if ( mMIdevice != null )   mMIdevice.setEnabled( enabled );
    if ( mMIdownload != null ) mMIdownload.setEnabled( enabled );
  }


  public void onActivityResult( int request, int result, Intent intent ) 
  {
    // Log.v( TAG, "onActivityResult() request " + mRequestName[request] + " result: " + result );
    DistoXDataHelper data = app.mData;
    Bundle extras = (intent != null )? intent.getExtras() : null;
    switch ( request ) {
      case REQUEST_ENABLE_BT:
        if ( result == Activity.RESULT_OK ) {
          // nothing to do: scanBTDEvices() is called by menu CONNECT
        } else {
          Toast.makeText(this, R.string.not_enabled, Toast.LENGTH_LONG).show();
          // finish();
        }
        // FIXME app.mBluetooth = ( result == Activity.RESULT_OK );
        setBTMenus( app.mBTAdapter.isEnabled() );
        updateDisplay( false );
        break;

      // case REQUEST_UNDELETE:
      //   if ( result == Activity.RESULT_OK ) {
      //     updateDisplay( true );
      //   } else {
      //     // nothing
      //   }
      //   break;

    }
    // Log.v( TAG, "onActivityResult() done " );
  }

  public void makeNewPlot( String name, long type, String start, String view )
  {
    mPID = app.mData.insertPlot( name, app.getSurveyId(), type, start, view );
    // Log.v( TAG, "insertPLot return " + mPID + " " + name + " start " + start + " view " + view );
    if ( mPID >= 0 ) {
      startDrawingActivity( start, name, type, view );
    }
    updateDisplay( true );
  }

  public void makeNewShot( String from, String to, float distance, float bearing, float clino, long extend,
                           String left, String right, String up, String down )
  {
    long id;
    long sid = app.getSurveyId();
    DistoXDataHelper data = app.mData;
    if ( from != null && to != null && from.length() > 0 && to.length() > 0 ) {
      // if ( data.makesCycle( -1L, sid, from, to ) ) {
      //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_LONG ).show();
      // } else
      {
        // Log.v( TAG, "Data " + distance + " " + bearing + " " + clino );
        boolean horizontal = ( Math.abs( clino ) > app.mVThreshold );
        // Log.v( TAG, "SID " + sid + " LRUD " + left + " " + right + " " + up + " " + down);
        if ( left != null && left.length() > 0 ) {
          float l = Float.parseFloat( left );
          if ( horizontal ) {
            id = data.insertShot( sid, l, 270.0f, 0.0f, 0.0f );
          } else {
            float b = bearing - 90.0f;
            if ( b < 0.0f ) b += 360.0f;
            // b = in360( b );
            id = data.insertShot( sid, l, b, 0.0f, 0.0f );
          }
          data.updateShotName( id, sid, from, "" );
        }
        if ( right != null && right.length() > 0 ) {
          float r = Float.parseFloat( right );
          if ( horizontal ) {
            id = data.insertShot( sid, r, 90.0f, 0.0f, 0.0f );
          } else {
            float b = bearing + 90.0f;
            if ( b >= 360.0f ) b -= 360.0f;
            id = data.insertShot( sid, r, b, 0.0f, 0.0f );
          }
          data.updateShotName( id, sid, from, "" );
        }
        if ( up != null && up.length() > 0 ) {
          float u = Float.parseFloat( up );
          if ( horizontal ) {
            id = data.insertShot( sid, u, 0.0f, 0.0f, 0.0f );
          } else {
            id = data.insertShot( sid, u, 0.0f, 90.0f, 0.0f );
          }
          data.updateShotName( id, sid, from, "" );
        }
        if ( down != null && down.length() > 0 ) {
          float d = Float.parseFloat( down );
          if ( horizontal ) {
            id = data.insertShot( sid, d, 180.0f, 0.0f, 0.0f );
          } else {
            id = data.insertShot( sid, d, 0.0f, -90.0f, 0.0f );
          }
          data.updateShotName( id, sid, from, "" );
        }
        id = data.insertShot( sid, distance, bearing, clino, 0.0f );
        // String name = from + "-" + to;
        data.updateShotName( id, sid, from, to );
        // data.updateShotExtend( id, sid, extend );
        updateDisplay( true );
      }
    } else {
      Toast.makeText( this, R.string.missing_station, Toast.LENGTH_LONG ).show();
    }
  }

  public void dropShot()
  {
    app.mData.deleteShot( mSIDid, app.getSurveyId() );
    if ( mCursor > 0 ) mCursor --;
    updateDisplay( true ); // FIXME
  }

  public void updateShot( String from, String to, long extend, long flag, String comment )
  {
    // Log.v( TAG, "updateShot From >" + from + "< To >" + to + "< comment " + comment );
    int ret = app.mData.updateShot( mSIDid, app.getSurveyId(), from, to, extend, flag, comment );
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
        mSaveTextView.setText( mSaveBlock.toString() );
        mSaveTextView.setTextColor( mSaveBlock.color() );
      }
    }
  }

}

/* @file CalibActivity.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid calibration activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120517 angle units
 */
package com.android.DistoX;

// import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
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

import android.content.Context;
import android.content.Intent;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;


public class CalibActivity extends Activity
                           implements OnItemClickListener, ILister
{
  private static final String TAG = "DistoX Calib";
  private TopoDroidApp app;

  // statuses
  // private static final int STATUS_NONE   = 0;
  private static final int STATUS_CALIB  = 3;    // list calibrations
  private static final int STATUS_GM     = 4;    // list GM data

  private int mStatus;                     // status
  private String mSaveData;                // saved GM text representation
  private TextView mSaveTextView;          // view of the saved GM
  private CalibCBlock mSaveCBlock = null;  // data of the saved GM

  private ListView mList;                  // display list
  private boolean mNeedUpdate = true;      // whethen need to refresh the list from the dB

  private ArrayAdapter<String> mArrayAdapter; // adapter for the list of calibrations
  private CalibCBlockAdapter   mDataAdapter;  // adapter for the list of GM's

  private long mCIDid = -1;    // id of the GM

  // --------- menus ----------
  private MenuItem mMIdevice = null;
  private MenuItem mMIsurvey;
  private MenuItem mMIgroup;

  // private MenuItem mMIundelete;
  // private MenuItem mMInotes;

  private MenuItem mMIdownload = null;
  private MenuItem mMIcompute;

  private SubMenu  mSMmore;
  private MenuItem mMIwrite = null;
  private MenuItem mMIread = null;
  private MenuItem mMItoggle = null;
  private MenuItem mMIrefresh;
  private MenuItem mMIcalib;
  private MenuItem mMIcalibnew;
  private MenuItem mMIcover;
  private MenuItem mMIoptions;

  // -------------------------------------------------------------------
  // forward survey name to DataHelper

  public void setStatus( int s ) { mStatus = s; }

  // -------------------------------------------------------------
  public long getCalibID()  { return app.getCalibId(); }
  public String getCalib( ) { return app.getCalib(); }

  public long setCalibFromName( String calib ) 
  {
    setStatus( STATUS_CALIB );
    return app.setCalibFromName( calib );
  }

  // public void setCalibFromId( int id ) // FIXME
  // {
  //   setStatus( STATUS_CALIB );
  //   app.setCalibFromId( id );
  // }

  // -------------------------------------------------------------------

  private boolean computeCalib()
  {
    long cid = app.getCalibId();
    if ( cid < 0 ) return false;
    List<CalibCBlock> list = app.mData.selectAllGMs( cid );
    if ( list.size() < 16 ) {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_LONG ).show();
      return false;
    }
    Calibration calibration = app.mCalibration;

    calibration.Reset( list.size() );
    for ( CalibCBlock item : list ) {
      calibration.AddValues( item );
    }
    int iter = calibration.Calibrate();
    if ( iter > 0 ) {
      float[] errors = calibration.Errors();
      int k = 0;
      float max_error = 0.0f;
      for ( CalibCBlock cb : list ) {
        app.mData.updateGMError( cb.mId, cid, errors[k] );
        // cb.setError( errors[k] );
        if ( errors[k] > max_error ) max_error = errors[k];
        ++k;
      }
      Vector bg = calibration.GetBG();
      Matrix ag = calibration.GetAG();
      Vector bm = calibration.GetBM();
      Matrix am = calibration.GetAM();

      float error = max_error * TopoDroidApp.RAD2GRAD_FACTOR;
      (new CalibCoeffDialog( this, bg, ag, bm, am, calibration.Delta(), error, iter ) ).show();
    } else {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_LONG ).show();
      return false;
    }
    return true;
  }

  private void computeGroups( )
  {
    long cid = app.getCalibId();
    if ( cid < 0 ) return;
    float thr = (float)Math.cos( app.mGroupDistance * TopoDroidApp.GRAD2RAD_FACTOR );
    List<CalibCBlock> list = app.mData.selectAllGMs( cid );
    if ( list.size() < 4 ) {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_LONG ).show();
      return;
    }
    long group = 0;
    int cnt = 0;
    float b = 0.0f;
    float c = 0.0f;
    switch ( app.mGroupBy ) {
      case TopoDroidApp.GROUP_BY_DISTANCE:
        for ( CalibCBlock item : list ) {
          if ( group == 0 || item.isFarFrom( b, c, thr ) ) {
            ++ group;
            b = item.mBearing;
            c = item.mClino;
          }
          item.setGroup( group );
          app.mData.updateGMName( item.mId, item.mCalibId, Long.toString(group) );
          // N.B. item.calibId == cid
        }
        break;
      case TopoDroidApp.GROUP_BY_FOUR:
        // Log.v(TAG, "group by four");
        group = 1;
        for ( CalibCBlock item : list ) {
          item.setGroup( group );
          app.mData.updateGMName( item.mId, item.mCalibId, Long.toString(group) );
          ++ cnt;
          if ( (cnt%4) == 0 ) {
            ++group;
            // Log.v(TAG, "cnt " + cnt + " new group " + group );
          }
        }
        break;
      case TopoDroidApp.GROUP_BY_ONLY_16:
        group = 1;
        for ( CalibCBlock item : list ) {
          item.setGroup( group );
          app.mData.updateGMName( item.mId, item.mCalibId, Long.toString(group) );
          ++ cnt;
          if ( (cnt%4) == 0 || cnt >= 16 ) ++group;
        }
        break;
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
        // case STATUS_NONE:
        //   break;
        case STATUS_CALIB:
          if ( data != null ) {
            List<String> list = data.selectAllCalibs( );
            // list.add( "new_calib" );
            setTitle( R.string.title_calib );
            updateList( list );
          }
          break;
        case STATUS_GM:
          if ( data != null && app.getCalibId() >= 0 ) {
            List<CalibCBlock> list = data.selectAllGMs( app.getCalibId() );
            updateGMList( list );
            setTitle( app.getCalib() );
          }
          break;
      }
    }
  }

  private void updateList( List<String> list )
  {
    mArrayAdapter.clear();
    // mArrayAdapter.add( getResources().getString( R.string.back_to_calib ) );
    for ( String item : list ) {
      mArrayAdapter.add( item );
    }
    mList.setAdapter( mArrayAdapter );
  }

  private void updateGMList( List<CalibCBlock> list )
  {
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_gms, Toast.LENGTH_LONG ).show();
      return;
    }
    mDataAdapter.clear();
    for ( CalibCBlock item : list ) {
      // StringWriter sw = new StringWriter();
      // PrintWriter pw  = new PrintWriter(sw);
      // item.computeBearingAndClino();
      // pw.format("%d <%d> %5.1f %5.1f %5.1f %6.4f",
      //   item.mId, item.mGroup, item.mBearing, item.mClino, item.mRoll, item.mError );
      // String result = sw.getBuffer().toString();
      // // Log.v( TAG, "Data " + result );
      // mArrayAdapter.add( result );
      // mArrayAdapter.add( item.toString() );
      mDataAdapter.add( item );
    }
    mList.setAdapter( mDataAdapter );
  }

  private String myUUIDaction = "android.bleutooth.device.action.UUID";

  // ---------------------------------------------------------------
  // list items click


  @Override
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    // if ( value.equals( getResources().getString( R.string.back_to_calib ) ) ) {
    //   setStatus( STATUS_CALIB );
    //   updateDisplay( true );
    //   return;
    // }
    switch ( mStatus ) {
      case STATUS_CALIB:
        setCalibFromName( value );
        setStatus( STATUS_GM );
        updateDisplay( true );
        break;
      case STATUS_GM:
        mSaveCBlock   = mDataAdapter.get( pos );
        mSaveTextView = (TextView) view;
        startGMDialog( );
        break;
    }
  }
 
  public void startGMDialog( )
  {
     String msg = mSaveTextView.getText().toString();
     String[] st = msg.split( " ", 3 );
     // Log.v( TAG, "TextItem: (" + st[0] + ") (" + st[1] + ") (" + st[2] + ")" );
     mCIDid    = Long.parseLong(st[0]);
     String name = st[1];
     mSaveData = st[2];
     int end = name.length() - 1;
     name = name.substring(1,end);
     (new CalibGMDialog( this, this, name, mSaveData )).show();
  }

  // ---------------------------------------------------------------
  // OPTIONS MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );
    // if ( mStatus == STATUS_SURVEY || mStatus == STATUS_CALIB )
    //   return false;
    mNeedUpdate = true;

    // MenuInflater inflater = getMenuInflater();
    // inflater.inflate(R.menu.option_menu_none, menu);

    mMIdevice = menu.add( R.string.menu_device );
    mMItoggle   = menu.add( R.string.menu_toggle );
    mMIgroup  = menu.add( R.string.menu_group );

    // mMIundelete  = mSMsurvey.add( R.string.menu_undelete );
    // mMInotes    = menu.add( R.string.menu_notes );

    mMIdownload = menu.add( R.string.menu_download );
    mMIcompute  = menu.add( R.string.menu_compute );

    mSMmore    = menu.addSubMenu( R.string.menu_more ); // FIXME
    mMIcalib    = mSMmore.add( R.string.menu_calib_list );
    mMIcalibnew = mSMmore.add( R.string.menu_calib_new );
    mMIcover    = mSMmore.add( R.string.menu_coverage );
    mMIread     = mSMmore.add( R.string.menu_read );
    mMIwrite    = mSMmore.add( R.string.menu_write );
    mMIsurvey   = mSMmore.add( R.string.menu_survey );
    mMIrefresh  = mSMmore.add( R.string.menu_refresh );
    mMIoptions  = mSMmore.add( R.string.menu_options );

    mMIdevice.setIcon( R.drawable.distox ); 
    mMIgroup.setIcon( R.drawable.group );
    mMItoggle.setIcon( R.drawable.toggle );
    mMIcompute.setIcon( R.drawable.calib );
    mMIdownload.setIcon( R.drawable.download );
    mSMmore.setIcon( R.drawable.more );
    // mMInotes.setIcon( R.drawable.compose );
    // mMIdevice.setIcon( android.R.drawable.ic_menu_mylocation ); 
    // mMIgroup.setIcon( android.R.drawable.ic_menu_sort_by_size );
    // mMItoggle.setIcon( android.R.drawable.ic_menu_manage );
    // mMIcompute.setIcon( android.R.drawable.ic_menu_compass );
    // mMIdownload.setIcon( android.R.drawable.ic_menu_upload );
    // mSMmore.setIcon( android.R.drawable.ic_menu_more );
    
    // mMIwrite.setIcon( R.drawable.write );
    // mMIread.setIcon( R.drawable.read );
    // mMIsurvey.setIcon( R.drawable.survey );
    // mMIrefresh.setIcon( R.drawable.refresh );
    // mMIcalib.setIcon( R.drawable.calib ); // not shown
    // mMIcalibnew.setIcon( R.drawable.calibnew );
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
    } else if ( item == mMIsurvey ) { // SURVEYS LIST
      finish();
    } else if ( item == mMIcalibnew ) {  // NEW CALIB
      setStatus( STATUS_CALIB );
      mNeedUpdate = true;
      setStatus( STATUS_CALIB );
      (new CalibDialog( this, this, "" ) ).show();
      updateDisplay( true );
    } else if ( item == mMIcalib ) {  // CALIBS LIST
      setStatus( STATUS_CALIB );
      updateDisplay( true );
    } else if ( item == mMIgroup ) {  // CALIB GROUPS
      setStatus( STATUS_GM );
      if ( app.getCalibId() >= 0 ) {
        computeGroups();
        updateDisplay( true );
      } else {
        Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_LONG ).show();
      }
    } else if ( item == mMIcompute ) {   // COMPUTE CALIB
      setStatus( STATUS_GM );
      if ( app.getCalibId() >= 0 ) {
        if ( computeCalib() ) {
          updateDisplay( true );
        } else {
          Toast.makeText( this, R.string.compute_failed, Toast.LENGTH_LONG ).show();
        }
      } else {
        Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_LONG ).show();
      }
    } else if ( item == mMIread ) {
      byte[] coeff = new byte[48];
      if ( app.mComm == null || ! app.mComm.readCoeff( app.mDevice, coeff ) ) {
        Toast.makeText(getApplicationContext(), R.string.read_failed, Toast.LENGTH_LONG).show();
      } else {
        String[] items = new String[8];
        Vector bg = new Vector();
        Matrix ag = new Matrix();
        Vector bm = new Vector();
        Matrix am = new Matrix();
        Calibration.coeffToG( coeff, bg, ag );
        Calibration.coeffToM( coeff, bm, am );
        (new CalibCoeffDialog( this, bg, ag, bm, am, 0.0f, 0.0f, 0 ) ).show();
      } 
    } else if ( item == mMIwrite ) {
      if ( app.mCalibration == null ) {
        Toast.makeText(getApplicationContext(), R.string.no_calibration, Toast.LENGTH_LONG).show();
      } else {
        byte[] coeff = app.mCalibration.GetCoeff();
        if ( coeff == null ) {
          Toast.makeText(getApplicationContext(), R.string.no_calibration, Toast.LENGTH_LONG).show();
        } else {
          if ( app.mComm == null || ! app.mComm.writeCoeff( app.mDevice, coeff ) ) {
            Toast.makeText(getApplicationContext(), R.string.write_failed, Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(getApplicationContext(), R.string.write_ok, Toast.LENGTH_SHORT).show();
          }
        }
      }
    } else if ( item == mMItoggle ) {
      if ( app.mComm == null || ! app.mComm.toggleCalibMode( app.mDevice ) ) {
        Toast.makeText(getApplicationContext(), R.string.toggle_failed, Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(getApplicationContext(), R.string.toggle_ok, Toast.LENGTH_SHORT).show();
      }
    // ---------------------- DEVICES
    } else if ( item == mMIdevice ) {
      mNeedUpdate = true;
      Intent deviceIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
      startActivity( deviceIntent );
    } else if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent optionsIntent = new Intent( this, DistoXPreferences.class );
      startActivity( optionsIntent );
    } else if ( item == mMIcover ) {
      Calibration calib = app.mCalibration;
      if ( calib != null ) {
        List< CalibCBlock > list = app.mData.selectAllGMs( app.getCalibId() );
        ( new CalibCoverage( this, list, calib ) ).show();
      } else {
        Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_LONG ).show();
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
    mDataAdapter  = new CalibCBlockAdapter( this, R.layout.row, new ArrayList<CalibCBlock>() );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // setTitleColor( 0x006d6df6 );
  }

  private void restoreInstanceFromData()
  { 
    // Log.v( TAG, "restoreInstanceFromData ");
    DistoXDataHelper data = app.mData;
    String calib = data.getValue( "DISTOX_CALIB" );
    if ( calib != null && calib.length() > 0 ) {
      // Log.v( TAG, "DISTOX_CALIB >" + calib + "<" );
      setCalibFromName( calib );
    } 
  }
    
  private void saveInstanceToData()
  {
    DistoXDataHelper data = app.mData;
    data.setValue( "DISTOX_CALIB", (app.getCalib() == null)? "" : app.getCalib() );
  }

  private void setBTMenus( boolean enabled )
  {
    if ( mMIdevice != null )   mMIdevice.setEnabled( enabled );
    if ( mMItoggle != null )   mMItoggle.setEnabled( enabled );
    if ( mMIdownload != null ) mMIdownload.setEnabled( enabled );
    if ( mMIwrite != null )    mMIwrite.setEnabled( enabled );
    if ( mMIread != null )     mMIread.setEnabled( enabled );
  }

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
    restoreInstanceFromData();
    setBTMenus( app.mBTAdapter.isEnabled() );
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    if ( app.mComm != null ) { app.mComm.resume(); }
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
    saveInstanceToData();
  }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
  }

  // ------------------------------------------------------------------

  public int downloadData()
  {
    // Log.v( TAG, "downloadData() device " + app.mDevice );
    if ( app.mComm != null && app.mDevice != null ) {
      return app.mComm.downloadData( app.mDevice );
    }
    return 0;
  }

  public void makeNewCalib( String name, String date, String comment )
  {
    long id = setCalibFromName( name );
    if ( id > 0 ) {
      app.mData.updateCalibDayAndComment( id, date, comment );
      setStatus( STATUS_GM );
      // updateDisplay( true );
    }
  }
 
  public void updateGM( String name )
  {
    app.mData.updateGMName( mCIDid, app.getCalibId(), name );
    String id = (new Long(mCIDid)).toString();
    // CalibCBlock blk = app.mData.selectGM( mCIDid, app.getCalibId() );
    mSaveCBlock.setGroup( Long.parseLong( name ) );

    if ( app.mListRefresh ) {
      mDataAdapter.notifyDataSetChanged();
    } else {
      mSaveTextView.setText( id + " <" + name + "> " + mSaveData );
      mSaveTextView.setTextColor( mSaveCBlock.color() );
      // mSaveTextView.invalidate();
      // updateDisplay( true ); // FIXME
    }
  }


}

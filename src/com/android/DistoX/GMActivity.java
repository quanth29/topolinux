/* @file GMActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid calibration data activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120517 angle units
 * 20120715 per-category preferences
 */
package com.android.DistoX;

// import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
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


public class GMActivity extends Activity
                           implements OnItemClickListener, ILister
{
  private TopoDroidApp app;

  private String mSaveData;                // saved GM text representation
  private TextView mSaveTextView;          // view of the saved GM
  private CalibCBlock mSaveCBlock = null;  // data of the saved GM
  private long mCIDid = -1;    // id of the GM

  private ListView mList;                  // display list

  private CalibCBlockAdapter   mDataAdapter;  // adapter for the list of GM's

  // --------- menus ----------
  private MenuItem mMIdevice = null;
  private MenuItem mMItoggle = null;
  private MenuItem mMIgroup;

  private MenuItem mMIdownload = null;
  private MenuItem mMIcompute;
  private SubMenu  mSMmore;
  private MenuItem mMIcover;
  private MenuItem mMIwrite  = null;
  private MenuItem mMIread   = null;
  private MenuItem mMIrefresh;
  private MenuItem mMIoptions;
  private MenuItem mMIhelp;

  private ConnHandler mHandler;

  // -------------------------------------------------------------------
  // forward survey name to DataHelper

  // -------------------------------------------------------------

  private boolean computeCalib()
  {
    long cid = app.mCID;
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
    long cid = app.mCID;
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
        // TopoDroidApp.Log( TopoDroidApp.LOG_CALIB, "group by four");
        group = 1;
        for ( CalibCBlock item : list ) {
          item.setGroup( group );
          app.mData.updateGMName( item.mId, item.mCalibId, Long.toString(group) );
          ++ cnt;
          if ( (cnt%4) == 0 ) {
            ++group;
            // TopoDroidApp.Log( TopoDroidApp.LOG_CALIB, "cnt " + cnt + " new group " + group );
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
    mDataAdapter.clear();
    DataHelper data = app.mData;
    if ( data != null && app.mCID >= 0 ) {
      List<CalibCBlock> list = data.selectAllGMs( app.mCID );
      updateGMList( list );
      setTitle( app.getCalib() );
    }
  }

  private void updateGMList( List<CalibCBlock> list )
  {
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_gms, Toast.LENGTH_LONG ).show();
      return;
    }
    for ( CalibCBlock item : list ) {
      mDataAdapter.add( item );
    }
    // mList.setAdapter( mDataAdapter );
  }


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
    mSaveCBlock   = mDataAdapter.get( pos );
    mSaveTextView = (TextView) view;
    startGMDialog( );
  }
 
  public void startGMDialog( )
  {
     String msg = mSaveTextView.getText().toString();
     String[] st = msg.split( " ", 3 );
     // TopoDroidApp.Log(  TopoDroidApp.LOG_CALIB, "TextItem: (" + st[0] + ") (" + st[1] + ") (" + st[2] + ")" );
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

    // MenuInflater inflater = getMenuInflater();
    // inflater.inflate(R.menu.option_menu_none, menu);

    mMIdevice = menu.add( R.string.menu_device );
    mMItoggle   = menu.add( R.string.menu_toggle );
    mMIgroup  = menu.add( R.string.menu_group );

    mMIdownload = menu.add( R.string.menu_download );
    mMIcompute  = menu.add( R.string.menu_compute );

    mSMmore    = menu.addSubMenu( R.string.menu_more ); // FIXME
    mMIcover    = mSMmore.add( R.string.menu_coverage );
    mMIread     = mSMmore.add( R.string.menu_read );
    mMIwrite    = mSMmore.add( R.string.menu_write );
    mMIrefresh  = mSMmore.add( R.string.menu_refresh );
    mMIoptions  = mSMmore.add( R.string.menu_options );
    mMIhelp     = mSMmore.add( R.string.menu_help );

    mMIdevice.setIcon( R.drawable.distox ); 
    mMIgroup.setIcon( R.drawable.group );
    mMItoggle.setIcon( R.drawable.toggle );
    mMIcompute.setIcon( R.drawable.calib );
    mMIdownload.setIcon( R.drawable.download );
    mSMmore.setIcon( R.drawable.more );
    // mMIwrite.setIcon( R.drawable.write );
    // mMIread.setIcon( R.drawable.read );
    // mMIrefresh.setIcon( R.drawable.refresh );
    // mMIoptions.setIcon( R.drawable.prefs );

    setBTMenus( app.mBTAdapter.isEnabled() );
    return true;
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidApp.Log(  TopoDroidApp.LOG_CALIB, "onOptionsItemSelected() " + StatusName() );
    // Handle item selection
    if ( item == mMIrefresh ) {
      updateDisplay( );
    } else if ( item == mMIdownload ) {
      setTitleColor( TopoDroidApp.COLOR_CONNECTED );
      new DistoXRefresh( app, this ).execute();
    } else if ( item == mMIgroup ) {  // CALIB GROUPS
      if ( app.mCID >= 0 ) {
        computeGroups();
        updateDisplay( );
      } else {
        Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_LONG ).show();
      }
    } else if ( item == mMIcompute ) {   // COMPUTE CALIB
      if ( app.mCID >= 0 ) {
        if ( computeCalib() ) {
          updateDisplay( );
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
      Intent deviceIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
      startActivity( deviceIntent );
    } else if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
      optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_CALIB );
      startActivity( optionsIntent );
    } else if ( item == mMIhelp ) {
      TopoDroidHelp.show( this, R.string.help_gm );
    } else if ( item == mMIcover ) {
      Calibration calib = app.mCalibration;
      if ( calib != null ) {
        List< CalibCBlock > list = app.mData.selectAllGMs( app.mCID );
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

    mDataAdapter  = new CalibCBlockAdapter( this, R.layout.row, new ArrayList<CalibCBlock>() );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mHandler = new ConnHandler( app, this );
  }

  private void setBTMenus( boolean enabled )
  {
    if ( mMIdevice != null )   mMIdevice.setEnabled( enabled );
    if ( mMIdownload != null ) mMIdownload.setEnabled( enabled );
    if ( mMItoggle != null )   mMItoggle.setEnabled( enabled );
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
    setBTMenus( app.mBTAdapter.isEnabled() );
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // if ( app.mComm != null ) { app.mComm.resume(); }
    updateDisplay( );
    app.registerConnListener( mHandler );
  }

  @Override
  protected synchronized void onPause() 
  { 
    super.onPause();
    app.unregisterConnListener( mHandler );
    // if ( app.mComm != null ) { app.mComm.suspend(); }
  }


  // @Override
  // public synchronized void onStop()
  // { 
  //   super.onStop();
  // }

  // @Override
  // public synchronized void onDestroy() 
  // {
  //   super.onDestroy();
  // }

  // ------------------------------------------------------------------

  public int downloadData()
  {
    // TopoDroidApp.Log(  TopoDroidApp.LOG_CALIB, "downloadData() device " + app.mDevice );
    if ( app.mComm != null && app.mDevice != null ) {
      return app.mComm.downloadData( app.mDevice );
    }
    return 0;
  }

  // public void makeNewCalib( String name, String date, String comment )
  // {
  //   long id = setCalibFromName( name );
  //   if ( id > 0 ) {
  //     app.mData.updateCalibDayAndComment( id, date, comment );
  //     setStatus( STATUS_GM );
  //     // updateDisplay( true );
  //   }
  // }
 
  public void updateGM( String name )
  {
    app.mData.updateGMName( mCIDid, app.mCID, name );
    String id = (new Long(mCIDid)).toString();
    // CalibCBlock blk = app.mData.selectGM( mCIDid, app.mCID );
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

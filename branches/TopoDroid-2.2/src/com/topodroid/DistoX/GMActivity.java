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
 * 20121124 check device-calibration consistency before download and write
 * 20131201 button bar new interface. reorganized actions
 */
package com.topodroid.DistoX;

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
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.view.Menu;
import android.view.MenuItem;

import android.util.Log;

public class GMActivity extends Activity
                        implements OnItemClickListener, ILister
                        , OnClickListener
{
  private TopoDroidApp app;

  private String mSaveData;                // saved GM text representation
  private TextView mSaveTextView;          // view of the saved GM
  private CalibCBlock mSaveCBlock = null;  // data of the saved GM
  private long mCIDid = -1;    // id of the GM

  private ListView mList;                  // display list


  private MenuItem mMIoptions;
  private MenuItem mMIhelp;

  private CalibCBlockAdapter mDataAdapter;  // adapter for the list of GM's

  private String mCalibName;
  private ConnHandler mHandler;

  static int CALIB_COMPUTE_CALIB  = 0;
  static int CALIB_COMPUTE_GROUPS = 1;

  static int icons[] = { R.drawable.ic_download,
                        R.drawable.ic_toggle,
                        R.drawable.ic_group,
                        R.drawable.ic_cover,
                        R.drawable.ic_compute,
                        R.drawable.ic_read,
                        R.drawable.ic_write,
                        R.drawable.ic_disto,
                        R.drawable.ic_pref,
                        R.drawable.ic_help
                     };
  static int help_texts[] = { R.string.help_download,
                        R.string.help_toggle,
                        R.string.help_group,
                        R.string.help_cover,
                        R.string.help_compute,
                        R.string.help_read,
                        R.string.help_write,
                        R.string.help_device,
                        R.string.help_prefs,
                        R.string.help_help
                      };
  // -------------------------------------------------------------------
  // forward survey name to DataHelper

  // -------------------------------------------------------------

  /**
   * @return nr of iterations (neg. error)
   */
  int computeCalib()
  {
    long cid = app.mCID;
    if ( cid < 0 ) return -2;
    List<CalibCBlock> list = app.mData.selectAllGMs( cid );
    if ( list.size() < 16 ) {
      return -1;
    }
    Calibration calibration = app.mCalibration;

    calibration.Reset( list.size() );
    for ( CalibCBlock item : list ) {
      calibration.AddValues( item );
    }
    int iter = calibration.Calibrate();
    if ( iter > 0 ) {
      float[] errors = calibration.Errors();
      float max_error = 0.0f;
      int k = 0;
      for ( CalibCBlock cb : list ) {
        app.mData.updateGMError( cb.mId, cid, errors[k] );
        // cb.setError( errors[k] );
        if ( errors[k] > max_error ) max_error = errors[k];
        ++k;
      }
      calibration.mMaxError = max_error;
    }
    // Log.v( TopoDroidApp.TAG, "iteration " + iter );
    return iter;
  }

  void handleComputeCalibResult( int result )
  {
    setTitleColor( TopoDroidApp.COLOR_NORMAL );
    // ( result == -2 ) not handled
    if ( result == -1 ) {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
      return;
    } else if ( result > 0 ) {
      Calibration calibration = app.mCalibration;
      Vector bg = calibration.GetBG();
      Matrix ag = calibration.GetAG();
      Vector bm = calibration.GetBM();
      Matrix am = calibration.GetAM();

      float error = calibration.mMaxError * TopoDroidUtil.RAD2GRAD;
      (new CalibCoeffDialog( this, bg, ag, bm, am, calibration.Delta(), error, result ) ).show();
    } else {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
      return;
    }
    updateDisplay();
  }

  void computeGroups( )
  {
    long cid = app.mCID;
    if ( cid < 0 ) return;
    float thr = (float)Math.cos( app.mGroupDistance * TopoDroidUtil.GRAD2RAD);
    List<CalibCBlock> list = app.mData.selectAllGMs( cid );
    if ( list.size() < 4 ) {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
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
  public void refreshDisplay( int nr, boolean toast )
  {
    // Log.v( TopoDroidApp.TAG, "refreshDisplay nr " + nr );
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
    // Log.v( TopoDroidApp.TAG, "updateDisplay CID " + app.mCID );
    setTitleColor( TopoDroidApp.COLOR_NORMAL );
    mDataAdapter.clear();
    DataHelper data = app.mData;
    if ( data != null && app.mCID >= 0 ) {
      List<CalibCBlock> list = data.selectAllGMs( app.mCID );
      // Log.v( TopoDroidApp.TAG, "updateDisplay GMs " + list.size() );
      updateGMList( list );
      setTitle( mCalibName );
    }
  }

  private void updateGMList( List<CalibCBlock> list )
  {
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_gms, Toast.LENGTH_SHORT ).show();
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
    // TopoDroidApp.Log(  TopoDroidApp.LOG_INPUT, "GMActivity onItemClick() " + item.toString() );

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

  private Button[] mButton1;
  // private Button[] mButton2;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  // private Button mButtonHelp;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.gm_activity);
    app = (TopoDroidApp) getApplication();

    mDataAdapter  = new CalibCBlockAdapter( this, R.layout.row, new ArrayList<CalibCBlock>() );

    // mButtonHelp = (Button)findViewById( R.id.help );
    // mButtonHelp.setOnClickListener( this );
    // if ( TopoDroidApp.mHideHelp ) {
    //   mButtonHelp.setVisibility( View.GONE );
    // } else {
    //   mButtonHelp.setVisibility( View.VISIBLE );
    // }


    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mHandler = new ConnHandler( app, this );

    int nr_button1 = 8;
    mButton1 = new Button[ nr_button1 ];
    for ( int k=0; k<nr_button1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
    }

    int k1 = 0;
    // mButton1[k1++].setBackgroundResource(  R.drawable.ic_cancel );
    mButton1[k1++].setBackgroundResource(  R.drawable.ic_download );  
    mButton1[k1++].setBackgroundResource(  R.drawable.ic_toggle ); // reset
    mButton1[k1++].setBackgroundResource(  R.drawable.ic_group );
    mButton1[k1++].setBackgroundResource(  R.drawable.ic_cover );
    mButton1[k1++].setBackgroundResource(  R.drawable.ic_compute );
    mButton1[k1++].setBackgroundResource(  R.drawable.ic_read );
    mButton1[k1++].setBackgroundResource(  R.drawable.ic_write );
    mButton1[k1++].setBackgroundResource(  R.drawable.ic_disto );
    // mButton1[k1++].setBackgroundResource(  R.drawable.ic_pref );
    // mButton1[k1++].setBackgroundResource(  R.drawable.ic_help );

    mButtonView1 = new HorizontalButtonView( mButton1 );
    // mButtonView2 = new HorizontalButtonView( mButton2 );
    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setAdapter( mButtonView1.mAdapter );

    mCalibName = app.myCalib;
    // updateDisplay();
  }

    public void onClick(View view)
    {
      Button b = (Button)view;
      if ( b == mButton1[0] ) { // download
        if ( ! app.checkCalibrationDeviceMatch() ) {
          Toast.makeText( this, R.string.calib_device_mismatch, Toast.LENGTH_LONG ).show();
        } else {
          setTitleColor( TopoDroidApp.COLOR_CONNECTED );
          new DistoXRefresh( app, this ).execute();
        }
      } else if ( b == mButton1[1] ) { // toggle
        setTitleColor( TopoDroidApp.COLOR_CONNECTED );
        setTitle( R.string.toggle_device );
        if ( app.mComm == null || ! app.mComm.toggleCalibMode( app.distoAddress(), app.distoType() ) ) {
          Toast.makeText(getApplicationContext(), R.string.toggle_failed, Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getApplicationContext(), R.string.toggle_ok, Toast.LENGTH_SHORT).show();
        }
        setTitle( mCalibName );
        setTitleColor( TopoDroidApp.COLOR_COMPUTE );
      } else if ( b == mButton1[2] ) { // group
        if ( app.mCID >= 0 ) {
          List< CalibCBlock > list = app.mData.selectAllGMs( app.mCID );
          if ( list.size() >= 16 ) {
            setTitle( mCalibName + " computing groups" );
            setTitleColor( TopoDroidApp.COLOR_COMPUTE );
            // computeGroups();
            // updateDisplay( );
            new CalibComputer( this, CALIB_COMPUTE_GROUPS ).execute();
          } else {
            setTitleColor( TopoDroidApp.COLOR_NORMAL );
            Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
          }
        } else {
          setTitle( mCalibName );
          setTitleColor( TopoDroidApp.COLOR_NORMAL );
          Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT ).show();
        }
      } else if ( b == mButton1[3] ) { // cover
        Calibration calib = app.mCalibration;
        if ( calib != null ) {
          List< CalibCBlock > list = app.mData.selectAllGMs( app.mCID );
          if ( list.size() >= 16 ) {
            ( new CalibCoverage( this, list, calib ) ).show();
          } else {
            Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
          }
        } else {
          Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT ).show();
        }
      } else if ( b == mButton1[4] ) { // compute
        if ( app.mCID >= 0 ) {
          setTitle( mCalibName + " computing calibration" );
          setTitleColor( TopoDroidApp.COLOR_COMPUTE );
          new CalibComputer( this, CALIB_COMPUTE_CALIB ).execute();
          // if ( computeCalib() ) {
          //   updateDisplay( );
          // } else {
          //   setTitle( mCalibName );
          //   setTitleColor( TopoDroidApp.COLOR_NORMAL );
          //   Toast.makeText( this, R.string.compute_failed, Toast.LENGTH_SHORT ).show();
          // }
        } else {
          Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT ).show();
        }
      // } else if ( b == mButton1[5] ) {  // more
      //   mListView.setAdapter( mButtonView2.mAdapter );
      //   mListView.invalidate();
      // } else if ( b == mButton2[0] ) {  // less
      //   mListView.setAdapter( mButtonView1.mAdapter );
      //   mListView.invalidate();
      } else if ( b == mButton1[5] ) { // read
        setTitle( "reading calib. coeffiicients ..." );
        setTitleColor( TopoDroidApp.COLOR_CONNECTED );
        byte[] coeff = new byte[48];
        if ( app.mComm == null || ! app.mComm.readCoeff( app.distoAddress(), coeff ) ) {
          Toast.makeText(getApplicationContext(), R.string.read_failed, Toast.LENGTH_SHORT).show();
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
        setTitle( mCalibName );
        setTitleColor( TopoDroidApp.COLOR_NORMAL );
      } else if ( b == mButton1[6] ) { // write
        if ( app.mCalibration == null ) {
          Toast.makeText(getApplicationContext(), R.string.no_calibration, Toast.LENGTH_SHORT).show();
        } else {
          setTitle( "writing calib. coeffiicients ..." );
          setTitleColor( TopoDroidApp.COLOR_CONNECTED );
          byte[] coeff = app.mCalibration.GetCoeff();
          if ( coeff == null ) {
            Toast.makeText(getApplicationContext(), R.string.no_calibration, Toast.LENGTH_SHORT).show();
          } else {
            if ( ! app.checkCalibrationDeviceMatch() ) {
              Toast.makeText( this, R.string.calib_device_mismatch, Toast.LENGTH_LONG ).show();
            } else {
              if ( app.mComm == null || ! app.mComm.writeCoeff( app.distoAddress(), coeff ) ) {
                Toast.makeText(getApplicationContext(), R.string.write_failed, Toast.LENGTH_SHORT).show();
              } else {
                Toast.makeText(getApplicationContext(), R.string.write_ok, Toast.LENGTH_SHORT).show();
              }
            }
          }
          setTitle( mCalibName );
          setTitleColor( TopoDroidApp.COLOR_NORMAL );
        }
      } else if ( b == mButton1[7] ) { // disto
        Intent deviceIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
        startActivity( deviceIntent );
      // } else if ( b == mButton1[8] ) { // prefs
      //   Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
      //   optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_CALIB );
      //   startActivity( optionsIntent );
      // } else if ( b == mButtonHelp ) { // help
      //   (new HelpDialog(this, icons, texts ) ).show();
      }
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
    // setBTMenus( app.mBTAdapter.isEnabled() );
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // if ( app.mComm != null ) { app.mComm.resume(); }
    // Log.v( TopoDroidApp.TAG, "onResume ");
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
    // TopoDroidApp.Log(  TopoDroidApp.LOG_CALIB, "downloadData() device " + app.distoAddress() );
    if ( app.mComm != null && app.mDevice != null ) {
      return app.mComm.downloadData( app.distoAddress() );
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

    // if ( app.mListRefresh ) {
    //   mDataAdapter.notifyDataSetChanged();
    // } else {
      mSaveTextView.setText( id + " <" + name + "> " + mSaveData );
      mSaveTextView.setTextColor( mSaveCBlock.color() );
      // mSaveTextView.invalidate();
      // updateDisplay( true ); // FIXME
    // }
  }

  // ---------------------------------------------------------
  // MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIoptions = menu.add( R.string.menu_options );
    mMIhelp    = menu.add( R.string.menu_help  );

    mMIoptions.setIcon( R.drawable.ic_pref );
    mMIhelp.setIcon( R.drawable.ic_help );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "TopoDroidActivity onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_CALIB );
      startActivity( intent );
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      (new HelpDialog(this, icons, help_texts ) ).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

}

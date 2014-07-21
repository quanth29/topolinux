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
 * 20140609 enableWrite: two state write button
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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Application;
import android.app.Activity;
// import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.AsyncTask;
// import android.os.Handler;
// import android.os.Message;
// import android.os.Parcelable;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;

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

// import android.util.Log;

public class GMActivity extends Activity
                        implements OnItemClickListener, ILister
                        , OnClickListener
{
  private TopoDroidApp mApp;

  private String mSaveData;                // saved GM text representation
  private TextView mSaveTextView;          // view of the saved GM
  private CalibCBlock mSaveCBlock = null;  // data of the saved GM
  private long mCIDid = -1;    // id of the GM
  private int mBlkStatus = 0;   // min display Group (can be either 1 [only active] or 0 [all])

  private ListView mList;                  // display list


  private MenuItem mMIoptions;
  private MenuItem mMIdisplay;
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
    long cid = mApp.mCID;
    if ( cid < 0 ) return -2;
    List<CalibCBlock> list = mApp.mData.selectAllGMs( cid, 0 ); 
    if ( list.size() < 16 ) {
      return -1;
    }
    Calibration calibration = mApp.mCalibration;

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
        mApp.mData.updateGMError( cb.mId, cid, errors[k] );
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
    resetTitle( );
    // ( result == -2 ) not handled
    if ( result == -1 ) {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
      return;
    } else if ( result > 0 ) {
      enableWrite( true );
      Calibration calibration = mApp.mCalibration;
      Vector bg = calibration.GetBG();
      Matrix ag = calibration.GetAG();
      Vector bm = calibration.GetBM();
      Matrix am = calibration.GetAM();

      float error = calibration.mMaxError * TopoDroidUtil.RAD2GRAD;
      // (new CalibCoeffDialog( getApplicationContext(), bg, ag, bm, am, calibration.Delta(), error, result ) ).show();
      (new CalibCoeffDialog( this, bg, ag, bm, am, calibration.Delta(), error, result ) ).show();
    } else {
      // Toast.makeText( getApplicationContext(), R.string.few_data, Toast.LENGTH_SHORT ).show();
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
      return;
    }
    updateDisplay();
  }

  void computeGroups( )
  {
    long cid = mApp.mCID;
    if ( cid < 0 ) return;
    float thr = (float)Math.cos( mApp.mGroupDistance * TopoDroidUtil.GRAD2RAD);
    List<CalibCBlock> list = mApp.mData.selectAllGMs( cid, 0 );
    if ( list.size() < 4 ) {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
      return;
    }
    long group = 0;
    int cnt = 0;
    float b = 0.0f;
    float c = 0.0f;
    switch ( mApp.mGroupBy ) {
      case TopoDroidApp.GROUP_BY_DISTANCE:
        for ( CalibCBlock item : list ) {
          if ( group == 0 || item.isFarFrom( b, c, thr ) ) {
            ++ group;
            b = item.mBearing;
            c = item.mClino;
          }
          item.setGroup( group );
          mApp.mData.updateGMName( item.mId, item.mCalibId, Long.toString(group) );
          // N.B. item.calibId == cid
        }
        break;
      case TopoDroidApp.GROUP_BY_FOUR:
        // TopoDroidApp.Log( TopoDroidApp.LOG_CALIB, "group by four");
        group = 1;
        for ( CalibCBlock item : list ) {
          item.setGroup( group );
          mApp.mData.updateGMName( item.mId, item.mCalibId, Long.toString(group) );
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
          mApp.mData.updateGMName( item.mId, item.mCalibId, Long.toString(group) );
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
    resetTitle( );
    if ( nr >= 0 ) {
      if ( nr > 0 ) updateDisplay( );
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
    // Log.v( TopoDroidApp.TAG, "updateDisplay CID " + mApp.mCID );
    resetTitle( );
    mDataAdapter.clear();
    DataHelper data = mApp.mData;
    if ( data != null && mApp.mCID >= 0 ) {
      List<CalibCBlock> list = data.selectAllGMs( mApp.mCID, mBlkStatus );
      // Log.v( TopoDroidApp.TAG, "updateDisplay GMs " + list.size() );
      updateGMList( list );
      setTitle( mCalibName );
    }
  }

  private void updateGMList( List<CalibCBlock> list )
  {
    int nr_saturated_values = 0;
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_gms, Toast.LENGTH_SHORT ).show();
      return;
    }
    for ( CalibCBlock item : list ) {
      if ( item.isSaturated() ) ++ nr_saturated_values;
      mDataAdapter.add( item );
    }
    // mList.setAdapter( mDataAdapter );
    if ( nr_saturated_values > 0 ) {
      Toast.makeText( this, 
        String.format( getResources().getString( R.string.calib_saturated_values ), nr_saturated_values ),
        Toast.LENGTH_LONG ).show();
    }
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
    String msg = mSaveTextView.getText().toString();
    String[] st = msg.split( " ", 3 );
    mCIDid    = Long.parseLong(st[0]);
    // String name = st[1];
    mSaveData = st[2];
    if ( mSaveCBlock.mStatus == 0 ) {
      startGMDialog( st[1] );
    } else { // FIXME TODO ask whether to undelete
      AlertDialog.Builder alert = new AlertDialog.Builder( this );
      // alert.setTitle( R.string.delete );
      alert.setMessage( getResources().getString( R.string.calib_gm_undelete ) );
    
      alert.setPositiveButton( R.string.button_ok, 
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "calib delite" );
            deleteGM( false );
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
 
  private void startGMDialog( String name  )
  {
     // String msg = mSaveTextView.getText().toString();
     // String[] st = msg.split( " ", 3 );
     // // TopoDroidApp.Log(  TopoDroidApp.LOG_CALIB, "TextItem: (" + st[0] + ") (" + st[1] + ") (" + st[2] + ")" );
     // mCIDid    = Long.parseLong(st[0]);
     // String name = st[1];
     // mSaveData = st[2];
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
  boolean mEnableWrite;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.gm_activity);
    mApp = (TopoDroidApp) getApplication();

    mEnableWrite = false;

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

    mHandler = new ConnHandler( mApp, this );

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
    mButton1[k1++].setBackgroundResource(  R.drawable.ic_write_no );  // number 6
    mButton1[k1++].setBackgroundResource(  R.drawable.ic_disto );
    // mButton1[k1++].setBackgroundResource(  R.drawable.ic_pref );
    // mButton1[k1++].setBackgroundResource(  R.drawable.ic_help );

    mButtonView1 = new HorizontalButtonView( mButton1 );
    // mButtonView2 = new HorizontalButtonView( mButton2 );
    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setAdapter( mButtonView1.mAdapter );

    mCalibName = mApp.myCalib;
    // updateDisplay();
  }

  private void resetTitle()
  {
    setTitle( mCalibName );
    if ( mBlkStatus == 0 ) {
      setTitleColor( TopoDroidApp.COLOR_NORMAL );
    } else {
      setTitleColor( TopoDroidApp.COLOR_NORMAL2 );
    }
  }

  private void enableWrite( boolean enable ) 
  {
    if ( enable ) {
      mButton1[6].setBackgroundResource(  R.drawable.ic_write );
    } else {
      mButton1[6].setBackgroundResource(  R.drawable.ic_write_no );
    }
    mEnableWrite = enable;
  }

    public void onClick(View view)
    {
      Button b = (Button)view;
      if ( b == mButton1[0] ) { // download
        if ( ! mApp.checkCalibrationDeviceMatch() ) {
          Toast.makeText( this, R.string.calib_device_mismatch, Toast.LENGTH_LONG ).show();
        } else {
          enableWrite( false );
          setTitleColor( TopoDroidApp.COLOR_CONNECTED );
          new DistoXRefresh( mApp, this ).execute();
        }
      } else if ( b == mButton1[1] ) { // toggle
        mButton1[1].setEnabled( false );
        mButton1[1].setBackgroundResource( R.drawable.ic_toggle_no );
        setTitleColor( TopoDroidApp.COLOR_CONNECTED );
        new CalibToggleTask( this, mButton1[1], mApp ).execute();
      } else if ( b == mButton1[2] ) { // group
        if ( mApp.mCID >= 0 ) {
          List< CalibCBlock > list = mApp.mData.selectAllGMs( mApp.mCID, 0 );
          if ( list.size() >= 16 ) {
            setTitle( R.string.calib_compute_groups );
            setTitleColor( TopoDroidApp.COLOR_COMPUTE );
            // computeGroups();
            // updateDisplay( );
            new CalibComputer( this, CALIB_COMPUTE_GROUPS ).execute();
          } else {
            resetTitle( );
            Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
          }
        } else {
          resetTitle( );
          Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT ).show();
        }
      } else if ( b == mButton1[3] ) { // cover
        Calibration calib = mApp.mCalibration;
        if ( calib != null ) {
          List< CalibCBlock > list = mApp.mData.selectAllGMs( mApp.mCID, 0 );
          if ( list.size() >= 16 ) {
            ( new CalibCoverage( this, list, calib ) ).show();
          } else {
            Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
          }
        } else {
          Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT ).show();
        }
      } else if ( b == mButton1[4] ) { // compute
        if ( mApp.mCID >= 0 ) {
          setTitle( R.string.calib_compute_coeffs );
          setTitleColor( TopoDroidApp.COLOR_COMPUTE );
          new CalibComputer( this, CALIB_COMPUTE_CALIB ).execute();
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
        enableWrite( false );
        mButton1[5].setEnabled( false );
        mButton1[5].setBackgroundResource( R.drawable.ic_read_no );
        setTitleColor( TopoDroidApp.COLOR_CONNECTED );
        new CalibReadTask( this, mButton1[5], mApp ).execute();

      } else if ( b == mButton1[6] ) { // write
        if ( mEnableWrite ) {
          if ( mApp.mCalibration == null ) {
            Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT).show();
          } else {
            setTitle( R.string.calib_write_coeffs );
            setTitleColor( TopoDroidApp.COLOR_CONNECTED );
            byte[] coeff = mApp.mCalibration.GetCoeff();
            if ( coeff == null ) {
              Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT).show();
            } else {
              if ( ! mApp.checkCalibrationDeviceMatch() ) {
                Toast.makeText( this, R.string.calib_device_mismatch, Toast.LENGTH_LONG ).show();
              } else {
                if ( mApp.mComm == null || ! mApp.mComm.writeCoeff( mApp.distoAddress(), coeff ) ) {
                  Toast.makeText( this, R.string.write_failed, Toast.LENGTH_SHORT).show();
                } else {
                  Toast.makeText( this, R.string.write_ok, Toast.LENGTH_SHORT).show();
                }
              }
            }
            resetTitle( );
          }
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
    // setBTMenus( mApp.mBTAdapter.isEnabled() );
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // if ( mApp.mComm != null ) { mApp.mComm.resume(); }
    // Log.v( TopoDroidApp.TAG, "onResume ");
    updateDisplay( );
    mApp.registerConnListener( mHandler );
  }

  @Override
  protected synchronized void onPause() 
  { 
    super.onPause();
    mApp.unregisterConnListener( mHandler );
    // if ( mApp.mComm != null ) { mApp.mComm.suspend(); }
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
    // TopoDroidApp.Log(  TopoDroidApp.LOG_CALIB, "downloadData() device " + mApp.distoAddress() );
    if ( mApp.mComm != null && mApp.mDevice != null ) {
      return mApp.mComm.downloadData( mApp.distoAddress() );
    }
    return 0;
  }

  // public void makeNewCalib( String name, String date, String comment )
  // {
  //   long id = setCalibFromName( name );
  //   if ( id > 0 ) {
  //     mApp.mData.updateCalibDayAndComment( id, date, comment );
  //     setStatus( STATUS_GM );
  //     // updateDisplay( true );
  //   }
  // }
 
  void updateGM( long value, String name )
  {
    mApp.mData.updateGMName( mCIDid, mApp.mCID, name );
    String id = (new Long(mCIDid)).toString();
    // CalibCBlock blk = mApp.mData.selectGM( mCIDid, mApp.mCID );
    mSaveCBlock.setGroup( value );

    // if ( mApp.mListRefresh ) {
    //   mDataAdapter.notifyDataSetChanged();
    // } else {
      mSaveTextView.setText( id + " <" + name + "> " + mSaveData );
      mSaveTextView.setTextColor( mSaveCBlock.color() );
      // mSaveTextView.invalidate();
      // updateDisplay( true ); // FIXME
    // }
  }

  void deleteGM( boolean delete )
  {
    mApp.mData.deleteGM( mApp.mCID, mCIDid, delete );
    updateDisplay();
  }

  // ---------------------------------------------------------
  // MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIoptions = menu.add( R.string.menu_options );
    mMIdisplay = menu.add( R.string.menu_display );
    mMIhelp    = menu.add( R.string.menu_help  );

    mMIoptions.setIcon( R.drawable.ic_pref );
    mMIdisplay.setIcon( R.drawable.ic_logs );
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
    } else if ( item == mMIdisplay  ) { // DISPLAY
      mBlkStatus = 1 - mBlkStatus;       // 0 --> 1;  1 --> 0
      updateDisplay();
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      (new HelpDialog(this, icons, help_texts ) ).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

}

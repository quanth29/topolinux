/* @file DeviceActivity.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX device activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120523 radio buttons: batch - continuous
 * 20120525 using app.mConnectionMode
 * 20120715 per-category preferences
 * 20120726 TopoDroid log
 */
package com.android.DistoX;

// import java.Thread;
import java.util.Set;


import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.RadioButton;
import android.view.View;
import android.view.Menu;
import android.view.SubMenu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.Toast;

import android.bluetooth.BluetoothDevice;

public class DeviceActivity extends Activity
                            implements View.OnClickListener
                            , OnItemClickListener
                            // , RadioGroup.OnCheckedChangeListener
{
  private static final int REQUEST_DEVICE    = 1;

  private TopoDroidApp app;

  private TextView mTvAddress;
  // private Button   mBtnConnect;
  // private Button   mBtnDisconnect;
  // // private Button   mBtnReconnect;
  private Button   mBtnToggle;
  private Button   mBtnHeadTail;
  private Button   mBtnRead;
  // private Button   mBtnWrite;
  // private Button   mBtnBack;

  // private RadioGroup    mRGconnection;
  // private RadioButton   mBtnBatch;
  // private RadioButton   mBtnContinuous;

  private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;

  private String   mAddress;

  // private MenuItem mMIpaired;
  private MenuItem mMIscan;
  private MenuItem mMIreset;
  private SubMenu  mSMmore;
  private MenuItem mMIoption;
  private MenuItem mMIhelp;

// -------------------------------------------------------------------
  private void setState()
  {
    boolean cntd = app.isConnected();
    if ( mAddress.length() > 0 ) {
      mTvAddress.setTextColor( 0xffffffff );
      mTvAddress.setText( String.format( getResources().getString( R.string.using ), mAddress ) );
      // if ( cntd ) {
      //   String msg = getResources().getString( R.string.title_connected );
      //   mTvAddress.setText( mAddress + msg );
      // } else {
      //   String msg = getResources().getString( R.string.title_not_connected );
      //   mTvAddress.setText( mAddress + msg );
      // }
    } else {
      mTvAddress.setTextColor( 0xffff0000 );
      mTvAddress.setText( R.string.no_device_address );
    }

    if ( app.mConnectionMode == TopoDroidApp.CONN_MODE_BATCH ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_DEVICE, "setState batch checked. connected: " + app.isConnected() );
      mBtnToggle     .setEnabled( ! cntd );
      mBtnHeadTail   .setEnabled( ! cntd );
      mBtnRead       .setEnabled( ! cntd );
      // mBtnWrite      .setEnabled( ! cntd );
      // mBtnConnect    .setEnabled( false );
      // // mBtnReconnect  .setEnabled( false );
      // mBtnDisconnect .setEnabled( false );
    // } else { // app.mConnectionMode == TopoDroidApp.CONN_MODE_CONTINUOUS
    //   // TopoDroidApp.Log( TopoDroidApp.LOG_DEVICE, "setState cont. checked. connected: " + app.isConnected() );
    //   mBtnToggle     .setEnabled( false );
    //   mBtnHeadTail   .setEnabled( false );
    //   mBtnRead       .setEnabled( false );
    //   mBtnWrite      .setEnabled( false );
    //   // mBtnConnect    .setEnabled( ! cntd );
    //   // // mBtnReconnect  .setEnabled( true );
    //   // mBtnDisconnect .setEnabled( cntd );
    }
    // mBtnBack       .setEnabled( );

    updateList();
  }  

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    app = (TopoDroidApp) getApplication();

    mAddress = app.mDevice;

    // mAddress = getIntent().getExtras().getString(   TopoDroidApp.TOPODROID_DEVICE_ADDR );

    setContentView(R.layout.distox_device_dialog);
    mTvAddress = (TextView) findViewById( R.id.device_address );

    // mBtnConnect    = (Button) findViewById(R.id.button_connect_device );
    // // mBtnReconnect  = (Button) findViewById(R.id.button_reconnect_device );
    // mBtnDisconnect = (Button) findViewById(R.id.button_disconnect_device );
    mBtnToggle     = (Button) findViewById(R.id.button_toggle_device );
    mBtnHeadTail   = (Button) findViewById(R.id.button_head_tail_device );
    mBtnRead       = (Button) findViewById(R.id.button_read_device );
    // mBtnWrite      = (Button) findViewById(R.id.button_write_device );
    // mBtnBack       = (Button) findViewById(R.id.button_back_device );

    // mRGconnection  = (RadioGroup) findViewById(R.id.download_connection );
    // mBtnBatch      = (RadioButton) findViewById(R.id.download_batch );
    // mBtnContinuous = (RadioButton) findViewById(R.id.download_continuous );
    // if ( app.mConnectionMode == TopoDroidApp.CONN_MODE_BATCH ) {
    //   mBtnBatch.setChecked( true );
    // } else {
    //   mBtnContinuous.setChecked( true );
    // }

    // mBtnConnect.setOnClickListener( this );
    // // mBtnReconnect.setOnClickListener( this );
    // mBtnDisconnect.setOnClickListener( this );
    mBtnToggle.setOnClickListener( this );
    mBtnHeadTail.setOnClickListener( this );
    mBtnRead.setOnClickListener( this );
    // mBtnWrite.setOnClickListener( this );
    // mBtnBack.setOnClickListener( this );

    // mRGconnection.setOnCheckedChangeListener( this );

    mArrayAdapter = new ArrayAdapter<String>( this, R.layout.message );
    mList = (ListView) findViewById(R.id.dev_list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    // mList.setLongClickable( true );
    // mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );

    setState();
  }

  private void updateList( )
  {
    // TopoDroidApp.Log(TopoDroidApp.LOG_MAIN, "updateList" );
    mList.setAdapter( mArrayAdapter );
    mArrayAdapter.clear();
    if ( app.mBTAdapter != null ) {
      Set<BluetoothDevice> device_set = app.mBTAdapter.getBondedDevices();
      if ( device_set.isEmpty() ) {
        // Toast.makeText(this, R.string.no_paired_device, Toast.LENGTH_SHORT).show();
      } else {
        setTitle( R.string.title_device );
        for ( BluetoothDevice device : device_set ) {
          mArrayAdapter.add( "DistoX " + device.getAddress() );
        }
      }
    }
  }

  // @Override
  // public void onCheckedChanged( RadioGroup rg, int id )
  // {
  //   if ( id == R.id.download_batch ) {
  //     if ( mBtnBatch.isChecked() && app.isConnected() ) {
  //       DistoXComm comm = app.mComm;
  //       if ( comm == null ) comm.disconnectRemoteDevice();
  //     }
  //     app.mConnectionMode = TopoDroidApp.CONN_MODE_BATCH;
  //   } else if ( id == R.id.download_continuous ) {
  //     app.mConnectionMode = TopoDroidApp.CONN_MODE_CONTINUOUS;
  //   }
  //   setState();
  // }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    if ( value.startsWith( "DistoX", 0 ) ) {
      StringBuffer buf = new StringBuffer( item );
      int k = buf.lastIndexOf(" ");
      String address = buf.substring(k+1);
      if ( ! address.equals( mAddress ) ) {
        mAddress = address;
        app.setDevice( mAddress );
        if ( app.mComm != null ) {
          app.mComm.disconnectRemoteDevice();
        }
        setState();
      }
    }
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    DistoXComm comm = app.mComm;
    if ( comm == null ) {
      Toast.makeText(getApplicationContext(), R.string.connect_failed, Toast.LENGTH_LONG).show();
      return;
    }
    if ( mAddress.length() < 1 ) {
      Toast.makeText(getApplicationContext(), R.string.no_device_address, Toast.LENGTH_LONG).show();
      return;
    }

    // if ( b == mBtnConnect ) {
    //   // TopoDroidApp.Log( TopoDroidApp.LOG_DEVICE, "onClick mBtnConnect. Is connected " + app.isConnected() );
    //   if ( ! app.isConnected() ) {
    // //     if ( comm.connectRemoteDevice( mAddress ) ) {
    // //       Toast.makeText(getApplicationContext(), R.string.connected, Toast.LENGTH_SHORT).show();
    // //     } else {
    // //       app.resetComm();
    // //       Toast.makeText(getApplicationContext(), R.string.connect_failed, Toast.LENGTH_SHORT).show();
    // //     }
    //     comm.disconnectRemoteDevice();
    //     if ( comm.connectRemoteDevice( mAddress ) ) {
    //       Toast.makeText(getApplicationContext(), R.string.connected, Toast.LENGTH_SHORT).show();
    //     } else {
    //       app.resetComm();
    //       Toast.makeText(getApplicationContext(), R.string.connect_failed, Toast.LENGTH_SHORT).show();
    //     }
    //   } else {
    //     Toast.makeText(getApplicationContext(), R.string.connected_already, Toast.LENGTH_SHORT).show();
    //   }
    // // } else if ( b == mBtnReconnect ) {
    // //   // TopoDroidApp.Log( TopoDroidApp.LOG_DEVICE, "onClick mBtnReconnect. Is connected " + app.isConnected() );
    // //   comm.disconnectRemoteDevice();
    // //   if ( comm.connectRemoteDevice( mAddress ) ) {
    // //     Toast.makeText(getApplicationContext(), R.string.connected, Toast.LENGTH_SHORT).show();
    // //   } else {
    // //     app.resetComm();
    // //     Toast.makeText(getApplicationContext(), R.string.connect_failed, Toast.LENGTH_SHORT).show();
    // //   }
    // } else if (  b == mBtnDisconnect ) {
    //   // TopoDroidApp.Log( TopoDroidApp.LOG_DEVICE, "onClick mBtnDisconnect. Is connected " + app.isConnected() );
    //   if ( app.isConnected() ) {
    //     comm.disconnectRemoteDevice();
    //   } else {
    //     Toast.makeText(getApplicationContext(), R.string.connected_not, Toast.LENGTH_SHORT).show();
    //   }
    // } else
    if (  b == mBtnToggle ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_DEVICE, "onClick mBtnToggle. Is connected " + app.isConnected() );
      if ( ! comm.toggleCalibMode( mAddress ) ) {
        Toast.makeText(getApplicationContext(), R.string.toggle_failed, Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(getApplicationContext(), R.string.toggle_ok, Toast.LENGTH_SHORT).show();
      }
     } else if (  b == mBtnHeadTail ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_DEVICE, "onClick mBtnHeadTail. Is connected " + app.isConnected() );
       String ht = comm.readHeadTail( mAddress );
       if ( ht != null ) {
         Toast.makeText(getApplicationContext(), getString(R.string.head_tail) + ht, Toast.LENGTH_LONG).show();
       } else {
         Toast.makeText(getApplicationContext(), R.string.head_tail_failed, Toast.LENGTH_SHORT).show();
       }
    } else if ( b == mBtnRead ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_DEVICE, "onClick mBtnRead. Is connected " + app.isConnected() );
      byte[] coeff = new byte[48];
      if ( ! comm.readCoeff( mAddress, coeff ) ) {
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
    // } else if (  b == mBtnWrite ) {
    //   // TopoDroidApp.Log( TopoDroidApp.LOG_DEVICE, "onClick mBtnWrite. Is connected " + app.isConnected() );
    //   if ( app.mCalibration == null ) {
    //     Toast.makeText(getApplicationContext(), R.string.no_calibration, Toast.LENGTH_SHORT).show();
    //   } else {
    //     byte[] coeff = app.mCalibration.GetCoeff();
    //     if ( coeff == null ) {
    //       Toast.makeText(getApplicationContext(), R.string.no_calibration, Toast.LENGTH_LONG).show();
    //     } else {
    //       if ( ! comm.writeCoeff( mAddress, coeff ) ) {
    //         Toast.makeText(getApplicationContext(), R.string.write_failed, Toast.LENGTH_LONG).show();
    //       } else {
    //         Toast.makeText(getApplicationContext(), R.string.write_ok, Toast.LENGTH_SHORT).show();
    //       }
    //     }
    //   }
    // } else if ( b == mBtnBack ) {
    //   finish();
    }
    setState();
  }

  @Override
  public void onStart()
  {
    super.onStart();
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    app.resumeComm();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );
    // mMIdevice = menu.add( app.mDevice );
    // mMIpaired = menu.add( R.string.menu_paired );
    mMIscan   = menu.add( R.string.menu_scan );
    mMIreset  = menu.add( R.string.menu_reset );
    mSMmore   = menu.addSubMenu(  R.string.menu_more );
      mMIoption = mSMmore.add( R.string.menu_options );
      mMIhelp   = mSMmore.add( R.string.menu_help );

    // mMIpaired.setIcon( R.drawable.paired );
    mMIscan.setIcon( R.drawable.scan );
    mMIreset.setIcon( R.drawable.bluetooth );
    mSMmore.setIcon( R.drawable.more );
    // mMIoption.setIcon( R.drawable.prefs );
    // mMIpaired.setIcon( android.R.drawable.ic_menu_directions );
    // mMIscan.setIcon( android.R.drawable.ic_menu_share );
    // mMIreset.setIcon( android.R.drawable.ic_menu_close_clear_cancel );
    // mMIoption.setIcon( android.R.drawable.ic_menu_preferences );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // if ( item == mMIpaired ) {
    //   Intent pairIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceList.class );
    //   pairIntent.putExtra( TopoDroidApp.TOPODROID_DEVICE_ACTION, DeviceList.DEVICE_PAIR );
    //   startActivityForResult( pairIntent, REQUEST_DEVICE );
    // } else 
    if ( item == mMIscan ) {
      Intent scanIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceList.class );
      scanIntent.putExtra( TopoDroidApp.TOPODROID_DEVICE_ACTION, DeviceList.DEVICE_SCAN );
      startActivityForResult( scanIntent, REQUEST_DEVICE );
      Toast.makeText(this, R.string.wait_scan, Toast.LENGTH_LONG).show();
    } else if ( item == mMIreset ) {
      app.resetComm();
      setState();
    } else if ( item == mMIoption ) {
      Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
      optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_DEVICE );
      startActivity( optionsIntent );
    } else if ( item == mMIhelp ) { // HELP
      TopoDroidHelp.show( this, R.string.help_device );
    }
    return true;
  }

  // -----------------------------------------------------------------------------

  public void onActivityResult( int request, int result, Intent intent ) 
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_DEVICE, "onActivityResult request " + mRequestName[request] + " result: " + result );
    Bundle extras = (intent != null)? intent.getExtras() : null;
    switch ( request ) {
      case REQUEST_DEVICE:
        if ( result == RESULT_OK ) {
          String address = extras.getString( TopoDroidApp.TOPODROID_DEVICE_ACTION );
          if ( ! address.equals( mAddress ) ) {
            mAddress = address;
            app.setDevice( mAddress );
            if ( app.mComm != null ) {
              app.mComm.disconnectRemoteDevice();
            }
            setState();
          }
        } else if ( result == RESULT_CANCELED ) {
          // back to survey
          finish();
        }
        break;
      // case REQUEST_ENABLE_BT:
      //   if ( result == Activity.RESULT_OK ) {
      //     // nothing to do: canBTDEvices(); is called by menu CONNECT
      //   } else {
      //     Toast.makeText(this, R.string.not_enabled, Toast.LENGTH_LONG).show();
      //     finish();
      //   }
      //   break;
    }
  }
}


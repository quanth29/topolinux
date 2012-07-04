/* @file DeviceList.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX device list activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import java.util.Set;

import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;

// import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;


public class DeviceList extends Activity
                                implements OnItemClickListener
{ 
  // private static final String TAG = "DistoX Device";
  
  private TopoDroidApp app;
  private BroadcastReceiver mBTReceiver = null;

  public static final int DEVICE_PAIR = 0x1;
  public static final int DEVICE_SCAN = 0x2;
  
  private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.main);
    app = (TopoDroidApp) getApplication();

    mArrayAdapter = new ArrayAdapter<String>( this, R.layout.message );
    // mDataAdapter = new ArrayAdapter<String>( this, R.layout.data );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // setTitleColor( 0x006d6df6 );

    int command = getIntent().getExtras().getInt( TopoDroidApp.TOPODROID_DEVICE_ACTION );
    // Log.v( TAG, "command " + command );
    switch ( command )
    {
      case DEVICE_PAIR:
        showPairedDevices();
        break;
      case DEVICE_SCAN:
        scanBTDEvices();
        break;
      default:  // 0x0 or unknown
         // Log.w(TAG, "Unknown intent command! ("+command+")");
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    // Log.v( "DistoX::BTClick", "onItemClick() " + mDistoX.StatusName() + " value: " + value + " pos " + position );
    if ( value.equals( getResources().getString( R.string.back_to_survey ) ) ) {
      setResult( RESULT_CANCELED );
    } else if ( value.startsWith( "DistoX", 0 ) ) {
      StringBuffer buf = new StringBuffer( item );
      int k = buf.lastIndexOf(" ");
      String address = buf.substring(k+1);
      // Toast.makeText(getApplicationContext(), address, Toast.LENGTH_SHORT).show();
      // Log.v( "DistoX", "Address " + address );
      Intent intent = new Intent();
      intent.putExtra( TopoDroidApp.TOPODROID_DEVICE_ACTION, address );
      setResult( RESULT_OK, intent );
    } else {
      setResult( RESULT_CANCELED );
    }
    finish();
  }

  
  private void showPairedDevices()
  {
    if ( app.mBTAdapter != null ) {
      Set<BluetoothDevice> device_set = app.mBTAdapter.getBondedDevices();
      if ( device_set.isEmpty() ) {
        Toast.makeText(this, R.string.no_paired_device, Toast.LENGTH_SHORT).show();
      } else {
        setTitle( R.string.title_device );
        mArrayAdapter.clear();
        mArrayAdapter.add( getResources().getString(R.string.back_to_survey) );
        for ( BluetoothDevice device : device_set ) {
          mArrayAdapter.add( "DistoX " + device.getAddress() );
       }
      }
      // Log.v( TAG, "showPairedDevices n. " + mArrayAdapter.getCount() );
    } else {
      Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
    }
  }

  private void scanBTDEvices()
  {
    // Log.v( TAG, "scanBTDEvices" );
    resetReceiver();
    mBTReceiver = new BroadcastReceiver() 
    {
      // private static final String RECTAG = "DistoX::BroadcastReceiver";
      
      @Override
      public void onReceive( Context ctx, Intent data )
      {
        String action = data.getAction();
        // Log.v(RECTAG, "onReceive action " + action );
        if ( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals( action ) ) {
          // Log.v( RECTAG, "BT DISCOVERY STARTED" );
          setTitle(  R.string.title_discover );
          mArrayAdapter.clear();
          mArrayAdapter.add( getResources().getString(R.string.back_to_survey) );
        } else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) ) {
          // Log.v( RECTAG, "BT DISCOVERY FINISHED, found " + mArrayAdapter.getCount() );
          if ( mArrayAdapter.getCount() > 1 ) { // "back_to_survey"
            setTitle( R.string.title_device );
          } else {
            Toast.makeText(getApplicationContext(), R.string.no_device_found, Toast.LENGTH_SHORT).show();
          }
        } else if ( BluetoothDevice.ACTION_FOUND.equals( action ) ) {
          BluetoothDevice device = data.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
          // Log.v( RECTAG, "BT DEVICES FOUND, name " + device.getName() );
          if ( device.getBondState() != BluetoothDevice.BOND_BONDED ) {
            String device_name = device.getName();
            if ( device_name != null && device_name.equals("DistoX") ) {
              String device_addr = device.getAddress();
              mArrayAdapter.add( device_name + " " + device_addr );
            }
          }
        }
      }
    };
    IntentFilter foundFilter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
    IntentFilter startFilter = new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_STARTED );
    IntentFilter finishFilter = new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
    // IntentFilter uuidFilter  = new IntentFilter( myUUIDaction );
    // IntentFilter bondFilter  = new IntentFilter( BluetoothDevice.ACTION_BOND_STATE_CHANGED );

    registerReceiver( mBTReceiver, foundFilter );
    registerReceiver( mBTReceiver, startFilter );
    registerReceiver( mBTReceiver, finishFilter );
    // registerReceiver( mBTReceiver, uuidFilter );
    // registerReceiver( mBTReceiver, bondFilter );

    mArrayAdapter.clear();
    app.mBTAdapter.startDiscovery();
  }

  @Override
  public void onStop()
  {
    super.onStop();
    resetReceiver();
  }

  private void resetReceiver()
  {
    if ( mBTReceiver != null ) {
      // Log.v( TAG, "unregister receiver");
      unregisterReceiver( mBTReceiver );
      mBTReceiver = null;
    }
  }

  // private void ensureDiscoverable()
  // {
  //   if ( mBTAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE ) {
  //     Intent discoverIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
  //     discoverIntent.putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300 );
  //     startActivity( discoverIntent );
  //   }
  // }

}


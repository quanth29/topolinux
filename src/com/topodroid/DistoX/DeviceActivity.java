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
 * 20121121 bug-fix check that device is "DistoX" to put it on the list
 * 20131201 button bar new interface. reorganized actions
 */
package com.topodroid.DistoX;

// import java.Thread;
import java.util.Set;
import java.util.ArrayList;


import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;
import android.content.DialogInterface;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.RadioButton;
import android.view.View;
// import android.widget.RadioGroup;
// import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.view.Menu;
import android.view.MenuItem;

import android.widget.Toast;

import android.util.Log;

import android.bluetooth.BluetoothDevice;

public class DeviceActivity extends Activity
                            implements View.OnClickListener
                            , OnItemClickListener
                            // , RadioGroup.OnCheckedChangeListener
{
  private static final int REQUEST_DEVICE    = 1;

  private TopoDroidApp app;

  private TextView mTvAddress;

  private static int icons[] = { R.drawable.ic_toggle,
                        R.drawable.ic_sdcard,
                        R.drawable.ic_read,
                        R.drawable.ic_scan,
                        R.drawable.ic_bt,
                        R.drawable.ic_pref,
                        R.drawable.ic_help
                     };
  private static int help_texts[] = { R.string.help_toggle,
                        R.string.help_sdcard,
                        R.string.help_read,
                        R.string.help_scan,
                        R.string.help_bluetooth,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;

  // private String mAddress;
  private Device mDevice;

  private MenuItem mMIoptions;
  private MenuItem mMIhelp;

// -------------------------------------------------------------------
  private void setState()
  {
    boolean cntd = app.isConnected();
    if ( mDevice != null ) { // mAddress.length() > 0 ) {
      mTvAddress.setTextColor( 0xffffffff );
      mTvAddress.setText( String.format( getResources().getString( R.string.using ), mDevice.mAddress ) );
    } else {
      mTvAddress.setTextColor( 0xffff0000 );
      mTvAddress.setText( R.string.no_device_address );
    }

    updateList();
  }  

  // ---------------------------------------------------------------
  // private Button mButtonHelp;
  private Button[] mButton1;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;


  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    app = (TopoDroidApp) getApplication();

    mDevice  = app.mDevice;
    // mAddress = mDevice.mAddress;

    // mAddress = getIntent().getExtras().getString(   TopoDroidApp.TOPODROID_DEVICE_ADDR );

    setContentView(R.layout.device_activity);
    mTvAddress = (TextView) findViewById( R.id.device_address );

    // mButtonHelp = (Button)findViewById( R.id.help );
    // mButtonHelp.setOnClickListener( this );
    // if ( TopoDroidApp.mHideHelp ) {
    //   mButtonHelp.setVisibility( View.GONE );
    // } else {
    //   mButtonHelp.setVisibility( View.VISIBLE );
    // }

    int nr_button1 = 5;
    mButton1 = new Button[ nr_button1 ];
    for ( int k=0; k<nr_button1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      mButton1[k].setBackgroundResource(  icons[k] );
    }
    mButtonView1 = new HorizontalButtonView( mButton1 );
    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setAdapter( mButtonView1.mAdapter );

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
          String name = device.getName();
          String addr = device.getAddress();
          Device dev = app.mData.getDevice( addr );
          if ( dev == null ) {
            if ( name.startsWith( "DistoX", 0 ) ) {
              app.mData.insertDevice( addr, name );
            }
          } else {
              name = dev.mModel;
          }
          if ( name.startsWith("DistoX-") ) {      // DistoX2 X310
            mArrayAdapter.add( " X310 " + addr );
          } else if ( name.equals("DistoX") ) {    // DistoX A3
            mArrayAdapter.add( " A3 " + addr );
          } else {
            // do not add
          } 
        }
      }
    }
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DeviceActivity onItemClick() " + item.toString() );
    // String value = item.toString();
    // if ( value.startsWith( "DistoX", 0 ) ) 
    {
      StringBuffer buf = new StringBuffer( item );
      int k = buf.lastIndexOf(" ");
      String address = buf.substring(k+1);
      if ( mDevice == null || ! address.equals( mDevice.mAddress ) ) {
        app.setDevice( address );
        mDevice = app.mDevice;
        // mAddress = address;
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
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DeviceActivity onClick() button " + b.getText().toString() ); 
    DistoXComm comm = app.mComm;
    if ( comm == null ) {
      Toast.makeText(getApplicationContext(), R.string.connect_failed, Toast.LENGTH_SHORT).show();
      return;
    }

    int k = 0;
    if (  b == mButton1[k++] ) {  // toggle
      if ( mDevice == null ) { // mAddress.length() < 1 ) {
        Toast.makeText(getApplicationContext(), R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } else if ( ! comm.toggleCalibMode( mDevice.mAddress, mDevice.mType ) ) {
        Toast.makeText(getApplicationContext(), R.string.toggle_failed, Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(getApplicationContext(), R.string.toggle_ok, Toast.LENGTH_SHORT).show();
      }

    } else if (  b == mButton1[k++] ) { // memory
      if ( mDevice == null ) { // mAddress.length() < 1 ) {
        Toast.makeText(getApplicationContext(), R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } else if ( mDevice.mType == Device.DISTO_A3 ) {
        new DeviceA3MemoryDialog( this, this ).show();
      } else if ( mDevice.mType == Device.DISTO_X310 ) {
        new DeviceX310MemoryDialog( this, this ).show();
      } else {
        Toast.makeText(getApplicationContext(), "Unknown DistoX type " + mDevice.mType, Toast.LENGTH_SHORT).show();
      }
    } else if ( b == mButton1[k++] ) { // read
      if ( mDevice == null ) { // mAddress.length() < 1 ) {
        Toast.makeText(getApplicationContext(), R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } else {
        // TopoDroidApp.Log( TopoDroidApp.LOG_DEVICE, "onClick mBtnRead. Is connected " + app.isConnected() );
        byte[] coeff = new byte[48];
        if ( ! comm.readCoeff( mDevice.mAddress, coeff ) ) {
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
      }
    } else if ( b == mButton1[k++] ) { // scan
      Intent scanIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceList.class );
      scanIntent.putExtra( TopoDroidApp.TOPODROID_DEVICE_ACTION, DeviceList.DEVICE_SCAN );
      startActivityForResult( scanIntent, REQUEST_DEVICE );
      Toast.makeText(this, R.string.wait_scan, Toast.LENGTH_LONG).show();

    } else if ( b == mButton1[k++] ) { // reset comm state
      app.resetComm();
      setState();
      Toast.makeText(this, R.string.bt_reset, Toast.LENGTH_SHORT).show();

    // } else if ( b == mButton1[k++] ) { // options
    //   Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
    //   optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_DEVICE );
    //   startActivity( optionsIntent );

    // } else if ( b == mButtonHelp ) { // help
    //   (new HelpDialog(this, icons, help_texts ) ).show();

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

  // -----------------------------------------------------------------------------

  boolean readDeviceHeadTail( int[] head_tail )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_DEVICE, "onClick mBtnHeadTail. Is connected " + app.isConnected() );
    String ht = app.mComm.readHeadTail( mDevice.mAddress, head_tail );
    if ( ht == null ) {
      Toast.makeText(getApplicationContext(), R.string.head_tail_failed, Toast.LENGTH_SHORT).show();
      return false;
    }
    // Log.v( TopoDroidApp.TAG, "Head " + head_tail[0] + " tail " + head_tail[1] );
    // Toast.makeText(getApplicationContext(), getString(R.string.head_tail) + ht, Toast.LENGTH_SHORT).show();
    return true;
  }

  // reset data from stored-tail (inclusive) to current-tail (exclusive)
  private void doResetA3DeviceHeadTail( int[] head_tail )
  {
    int from = head_tail[0];
    int to   = head_tail[1];
    // Log.v(TopoDroidApp.TAG, "do reset from " + from + " to " + to );
    int n = app.mComm.swapHotBit( mDevice.mAddress, from, to );
  }

  void storeDeviceHeadTail( int[] head_tail )
  {
    // Log.v(TopoDroidApp.TAG, "store HeadTail " + mDevice.mAddress + " : " + head_tail[0] + " " + head_tail[1] );
    app.mData.updateDeviceHeadTail( mDevice.mAddress, head_tail );
  }

  void retrieveDeviceHeadTail( int[] head_tail )
  {
    // Log.v(TopoDroidApp.TAG, "store HeadTail " + mDevice.mAddress + " : " + head_tail[0] + " " + head_tail[1] );
    app.mData.getDeviceHeadTail( mDevice.mAddress, head_tail );
  }

  void readX310Memory( final int[] head_tail )
  {
    ArrayList< MemoryOctet > memory = new ArrayList< MemoryOctet >();
    int n = app.mComm.readX310Memory( mDevice.mAddress, head_tail[0], head_tail[1], memory );
    if ( n <= 0 ) return;
    (new MemoryListDialog(this, this, memory)).show();
  }

  void readA3Memory( final int[] head_tail )
  {
    if ( head_tail[0] < 0 || head_tail[0] >= 0x8000 || head_tail[1] < 0 || head_tail[1] >= 0x8000 ) {
      Toast.makeText(this, R.string.device_illegal_addr, Toast.LENGTH_SHORT).show();
      return;
    }
    ArrayList< MemoryOctet > memory = new ArrayList< MemoryOctet >();
    int from = head_tail[0];
    int to   = head_tail[1];
    // Log.v(TopoDroidApp.TAG, "read-memory from " + from + " to " + to );
    int n = app.mComm.readA3Memory( mDevice.mAddress, from, to, memory );
    if ( n == 0 ) {
      Toast.makeText(this, "no data", Toast.LENGTH_SHORT).show();
      return;
    } 
    Toast.makeText(this, "read " + n + " data", Toast.LENGTH_SHORT).show();

    (new MemoryListDialog(this, this, memory)).show();

  }

  // X310 data memory is read-only
  // void resetX310DeviceHeadTail( final int[] head_tail )
  // {
  //   int n = app.mComm.resetX310Memory( mDevice.mAddress, head_tail[0], head_tail[1] );
  //   Toast.makeText(this, "X310 memory reset " + n + " data", Toast.LENGTH_SHORT ).show();
  // }

  // reset device from stored-tail to given tail
  void resetA3DeviceHeadTail( final int[] head_tail )
  {
    // Log.v(TopoDroidApp.TAG, "reset device from " + head_tail[0] + " to " + head_tail[1] );
    if ( head_tail[0] < 0 || head_tail[0] >= 0x8000 || head_tail[1] < 0 || head_tail[1] >= 0x8000 ) {
      Toast.makeText(this, R.string.device_illegal_addr, Toast.LENGTH_SHORT).show();
      return;
    } 
    // TODO ask confirm
    AlertDialog.Builder alert = new AlertDialog.Builder( this );
    alert.setMessage( getResources().getString( R.string.device_reset ) + " ?" );
    alert.setPositiveButton( R.string.button_ok, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doResetA3DeviceHeadTail( head_tail );
        }
    } );

    alert.setNegativeButton( R.string.button_cancel, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) { }
    } );
    alert.show();
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
          if ( ! address.equals( mDevice.mAddress ) ) {
            app.setDevice( address );
            mDevice = app.mDevice;
            // mAddress = address;
            if ( app.mComm != null ) {
              app.mComm.disconnectRemoteDevice();
            }
            setState();
          }
        } else if ( result == RESULT_CANCELED ) {
          // finish(); // back to survey
        }
        updateList();
        break;
      // case REQUEST_ENABLE_BT:
      //   if ( result == Activity.RESULT_OK ) {
      //     // nothing to do: scanBTDevices(); is called by menu CONNECT
      //   } else {
      //     Toast.makeText(this, R.string.not_enabled, Toast.LENGTH_SHORT).show();
      //     finish();
      //   }
      //   break;
    }
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
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_DEVICE );
      startActivity( intent );
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      (new HelpDialog(this, icons, help_texts ) ).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

}


/** @file DataDownloader.java
 *
 * @author marco corvi
 * @date sept 2014
 *
 * @brief TopoDroid survey shots data downloader
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import android.bluetooth.BluetoothDevice;

import android.widget.Button;
import android.widget.Toast;

class DataDownloader
{
  static boolean mConnected = false;

  private Context mContext;
  private TopoDroidApp mApp;
  private ILister  mLister; 
  Button mButtonDownload;

  DataDownloader( Context context, TopoDroidApp app, ILister lister )
  {
    mContext = context;
    mApp = app;
    mLister = lister;
    mButtonDownload = null;
  }

  public void setConnectionStatus( boolean connected )
  {
    if ( connected ) {
      // setTitleColor( TopoDroidApp.COLOR_CONNECTED );
      if ( mButtonDownload != null ) {
        mButtonDownload.setBackgroundResource( R.drawable.ic_download_on );
      }
    } else {
      // setTitleColor( TopoDroidApp.COLOR_NORMAL );
      if ( mButtonDownload != null ) {
        mButtonDownload.setBackgroundResource( R.drawable.ic_download );
      }
    }
  }

  public void downloadData()
  {
    if ( mBTReceiver == null ) registerBTreceiver();
    if ( mApp.mConnectionMode == mApp.CONN_MODE_BATCH ) {
      tryDownloadData();
    } else {
      tryConnect();
    }
    if ( ! mConnected ) resetReceiver();
  }

  private void tryConnect()
  {
    if ( mApp.mDevice != null && mApp.mBTAdapter.isEnabled() ) {
      if ( mConnected == false ) {
        mConnected = mApp.connect( mApp.mDevice.mAddress, mLister );
      } else {
        mApp.disconnect( );
        mConnected = false;
      }
      setConnectionStatus( mConnected );
    }
  }

  private void tryDownloadData()
  {
    // mSecondLastShotId = mApp.lastShotId( );
    if ( mApp.mDevice != null && mApp.mBTAdapter.isEnabled() ) {
      setConnectionStatus( true );
      // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "shot menu DOWNLOAD" );
      new DistoXRefresh( mApp, mLister ).execute();
    } else {
      if ( mApp.mSID < 0 ) {
        // Toast.makeText( mContext, R.string.no_survey, Toast.LENGTH_SHORT ).show();
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "download data: no survey selected" );
      } else {
        DistoXDBlock last_blk = mApp.mData.selectLastLegShot( mApp.mSID );
        (new ShotNewDialog( mContext, mApp, mLister, last_blk, -1L )).show();
      }
    }
  }

  private BroadcastReceiver mBTReceiver = null;

  private void resetReceiver()
  {
    if ( mBTReceiver != null ) {
      mContext.unregisterReceiver( mBTReceiver );
      mBTReceiver = null;
    }
  }

  private void registerBTreceiver()
  {
    resetReceiver();
    mBTReceiver = new BroadcastReceiver() 
    {
      @Override
      public void onReceive( Context ctx, Intent data )
      {
        String action = data.getAction();
        // TopoDroidApp.Log( TopoDroidApp.LOG_BT, "onReceive action " + action );
        if ( BluetoothDevice.ACTION_ACL_CONNECTED.equals( action ) ) {
          // Log.v("DistoX", "ACL_CONNECTED");
          setConnectionStatus( true );
        } else if ( BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals( action ) ) {
          // Log.v("DistoX", "ACL_DISCONNECT_REQUESTED");
          setConnectionStatus( false );
        } else if ( BluetoothDevice.ACTION_ACL_DISCONNECTED.equals( action ) ) {
          // Log.v("DistoX", "ACL_DISCONNECTED");
          setConnectionStatus( false );
        }
      }
    };
    IntentFilter connectedFilter = new IntentFilter( BluetoothDevice.ACTION_ACL_CONNECTED );
    IntentFilter disconnectRequestFilter = new IntentFilter( BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED );
    IntentFilter disconnectedFilter = new IntentFilter( BluetoothDevice.ACTION_ACL_DISCONNECTED );

    mContext.registerReceiver( mBTReceiver, connectedFilter );
    mContext.registerReceiver( mBTReceiver, disconnectRequestFilter );
    mContext.registerReceiver( mBTReceiver, disconnectedFilter );
  }

}

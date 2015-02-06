/* @file DistoXComm.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid-DistoX BlueTooth communication 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import java.io.IOException;
import java.io.EOFException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.List;
// import java.Thread;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;

import android.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import android.database.DataSetObserver;

public class DistoXComm
{
  private static final String TAG = "DistoX Comm";

  private TopoDroidApp app;

  private BluetoothDevice mBTDevice;
  private BluetoothSocket mBTSocket;
  private String mAddress;
  private DistoXProtocol mProtocol;
  public boolean mBTConnected;

  private static final byte CALIB_BIT = (byte)0x08;
  public byte[] mCoeff;

  // private int   mHead;
  // private int   mTail;
  // public int Head()      { return mHead; }
  // public int Tail()      { return mTail; }

// --------------------------------------------------
  DistoXDataHelper getData() { return app.mData; }

  private class RfcommThread extends Thread
  {
    private static final String TAG_RF = "DistoX RfThread";
    private DistoXProtocol mProto;
    private int nData;  // number of received data-packet
    private int toRead; // number of packet to read

    public RfcommThread( DistoXProtocol protocol, int to_read )
    {
      nData  = 0;
      toRead = to_read;
      mProto = protocol;
      // Log.v( TAG_RF, "cstr ToRead " + toRead );
    }

    public int nrReadDataPacket() { return nData; }

    public void run()
    {
      boolean hasG = false;
      boolean doWork = true;

      // Log.v( TAG_RF, "running ..." );
      while ( doWork && nData != toRead ) {
            int res = mProto.readPacket();
            if ( res == DistoXProtocol.DISTOX_PACKET_NONE ) {
              doWork = false;
            } else if ( res == DistoXProtocol.DISTOX_PACKET_DATA ) {
              ++nData;
              double d = mProto.Distance();
              double b = mProto.Compass();
              double c = mProto.Clino();
              double r = mProto.Roll();
              // Log.v( TAG_RF, "DATA PACKET " + d + " " + b + " " + c );
              app.mData.insertShot( app.getSurveyId(), d, b, c, r );
            } else if ( res == DistoXProtocol.DISTOX_PACKET_G ) {
              // Log.v( TAG_RF, "G PACKET");
              ++nData;
              hasG = true;
            } else if ( res == DistoXProtocol.DISTOX_PACKET_M ) {
              // Log.v( TAG_RF, "M PACKET");
              ++nData;
              // get G and M from mProto and save them to store
              app.mData.insertGM( app.getCalibId(), mProto.GX(), mProto.GY(), mProto.GZ(), mProto.MX(), mProto.MY(), mProto.MZ() );
              hasG = false;
            } else if ( res == DistoXProtocol.DISTOX_PACKET_REPLY ) {
              
              byte[] addr = mProto.getAddress();
              byte[] reply = mProto.getReply();
              StringWriter sw = new StringWriter();
              PrintWriter pw  = new PrintWriter(sw);
              pw.format("%02x %02x %02x %02x at %02x%02x", reply[0], reply[1], reply[2], reply[3], addr[1], addr[0] );
              String result = sw.getBuffer().toString();
              // Log.v( TAG_RF, "REPLY PACKET: " + result ); 

              if ( addr[0] == (byte)0x00 && addr[1] == (byte)0x80 ) { // 0x8000
                // Log.v( TAG_RF, "toggle reply" );
                // if ( (reply[0] & CALIB_BIT) == 0 ) {
                //     mProto.sendCommand( (byte)0x31 );  // TOGGLE CALIB ON
                // } else {
                //     mProto.sendCommand( (byte)0x30 );  // TOGGLE CALIB OFF
                // }
              } else if ( ( addr[1] & (byte)0x80) == (byte)0x80 ) { // REPLY TO READ/WRITE-CALIBs
                // Log.v( TAG_RF, "write reply" );
                mProto.setWrittenCalib( true );
              } else if ( addr[0] == 0x20 && addr[1] == (byte)0xC0 ) { // C020 READ HEAD-TAIL
                // Log.v( TAG_RF, "read head-tail reply" );
                // mHead = (int)( reply[0] | ( (int)(reply[1]) << 8 ) );
                // mTail = (int)( reply[2] | ( (int)(reply[3]) << 8 ) );
              }
            }
      }
      // Log.v( TAG_RF, "run() exiting");
    }
  };

  private RfcommThread mRfcommThread;


  DistoXComm( TopoDroidApp the_app )
  {
    app        = the_app;
    mProtocol  = null;
    mAddress   = null;
    mRfcommThread = null;
    mBTConnected  = false;
    mBTSocket = null;
    // Log.v( TAG, "DistoXComm cstr");
  }

  public void resume()
  {
    // if ( mRfcommThread != null ) { mRfcommThread.resume(); }
  }

  public void suspend()
  {
    // if ( mRfcommThread != null ) { mRfcommThread.suspend(); }
  }

  // -------------------------------------------------------- 
  // SOCKET


  /** close the socket (and the RFcomm thread) but don't delete it
   */
  private void closeSocket( )
  {
    // Log.v( TAG, "closeSocket() " );
    if ( mBTSocket != null ) {
      try {
        mBTSocket.close();
        if ( mRfcommThread != null ) {
          mRfcommThread.join();
          mRfcommThread = null;
        }
      } catch ( InterruptedException e ) {
        Log.e(TAG, "closeSocket interrupt " + e.getMessage() );
      } catch ( IOException e ) {
        Log.e(TAG, "closeSocket IO " + e.getMessage() );
      } finally {
        mBTConnected = false;
      }
    }
    mBTConnected = false;
  }

  /** close the socket and delete it
   * the connection is unusable
   */
  private void destroySocket( )
  {
    // Log.v( TAG, "destroySocket() " );
    closeSocket();
    // mBTConnected = false;
    mBTSocket = null;
    mProtocol = null;
  }


  /** create a socket (not connected)
   *  and a connection protocol on it
   */
  private void createSocket( String address )
  {
    // Log.v( TAG, "createSocket() " );
    if ( mProtocol == null || address != mAddress ) {
      if ( mProtocol != null && address != mAddress ) {
        disconnectRemoteDevice();
      }
      mBTDevice     = app.mBTAdapter.getRemoteDevice( address );
      String device_name = mBTDevice.getName();
      try {
        if ( mBTSocket != null ) {
          // Log.v( TAG, "createSocket BTSocket not null ... closing");
          mBTSocket.close();
          mBTSocket = null;
        }
        if ( mBTDevice.getBondState() == BluetoothDevice.BOND_NONE ) {
          // byte[] pin = mBTDevice.convertPinToBytes( "0000" );
          // FIXME tried to replace the two following lines with the next one
          // String spin = "0000";
          // byte[] pin = spin.getBytes( "UTF8" );
          byte[] pin = new byte[] { 0, 0, 0, 0 };

          // mBTDevice.setPin( pin );
          Class[] classes3 = new Class[ 1 ];
          classes3[0] = byte[].class;
          Method m = mBTDevice.getClass().getMethod( "setPin", classes3 );
          m.invoke( mBTDevice, pin );

          // mBTDevice.createBond();
          Class[] classes2 = new Class[ 0 ];
          m = mBTDevice.getClass().getMethod( "createBond", classes2 );
          m.invoke( mBTDevice );
        }

        // mBTSocket = mBTDevice.createInsecureRfcommSocket( 0 );
        Class[] classes1 = new Class[ 1 ];
        classes1[0] = int.class;
        Method m = mBTDevice.getClass().getMethod( "createInsecureRfcommSocket", classes1 );
        mBTSocket = (BluetoothSocket) m.invoke( mBTDevice, 1 );

      } catch ( InvocationTargetException e ) {
        Log.e(TAG, "createSocket invoke target " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( UnsupportedEncodingException e ) {
        Log.e(TAG, "createSocket encoding " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( NoSuchMethodException e ) {
        Log.e(TAG, "createSocket no method " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( IllegalAccessException e ) {
        Log.e(TAG, "createSocket access " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( IOException e ) {
        Log.e(TAG, "createSocket IO " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      }
      if ( mBTSocket != null ) {
        mProtocol = new DistoXProtocol( mBTSocket );
        mAddress = address;
      } else {
        mProtocol = null;
        mAddress = null;
      }
      mBTConnected = false;
    }
  }

  /** connect the socket to the device
   */
  private boolean connectSocket()
  {
    // Log.v( TAG, "connectSocket() connected: " + mBTConnected );
    if ( mBTSocket != null ) {
      if ( ! mBTConnected ) {
        try {
          mBTSocket.connect();
          mBTConnected = true;
        } catch ( IOException e ) {
          Log.e(TAG, "connectSocket IO " + e.getMessage() );
          return false;
        }
      }
    }
    // Log.v(TAG, "connectSocket " + mBTConnected );
    return mBTConnected;
  }

  private boolean startRfcommThread( int to_read )
  {
    // Log.v(TAG, "startRFcommThread to_read " + to_read );
    if ( mBTSocket != null && mRfcommThread == null ) {
      mRfcommThread = new RfcommThread( mProtocol, to_read );
      mRfcommThread.start();
      // downloadLoop();
      // Log.v(TAG, "startRFcommThread true");
      return true;
    } else {
      mRfcommThread = null;
      // Log.v(TAG, "startRFcommThread false");
      return false;
    }
  }

  // -------------------------------------------------------- 

  public boolean connectRemoteDevice( String address )
  {
    // Log.v( TAG, "connectRemoteDevice sddress " + address );
    createSocket( address );
    if ( connectSocket() ) {
      if ( mProtocol != null ) {
        return startRfcommThread( -1 );
      }
      // Log.v(TAG, "connectRemoteDevice null protocol");
      destroySocket();
    }
    return false;
  }

  public void disconnectRemoteDevice( )
  {
    // Log.v( TAG, "disconnectRemoteDevice ");
    if ( mBTSocket != null ) {
      destroySocket();
      if ( mRfcommThread != null ) {
        try {
          mRfcommThread.join();
        } catch ( InterruptedException e ) {
          // TODO
        }
      }
      mRfcommThread = null;
    }
    mProtocol = null;
  }

  public boolean toggleCalibMode( String address )
  {
    // Log.v( TAG, "toggleCalibMode sddress " + address );
    if ( mRfcommThread != null ) {
      return false;
    }
    boolean ret = false;
    createSocket( address );
    if ( connectSocket() ) {
      byte[] result = new byte[4];
      if ( ! mProtocol.read8000( result ) ) {
        destroySocket();
        return false;
      }
      if ( (result[0] & CALIB_BIT) == 0 ) {
        ret = mProtocol.sendCommand( (byte)0x31 );  // TOGGLE CALIB ON
      } else {
        ret = mProtocol.sendCommand( (byte)0x30 );  // TOGGLE CALIB OFF
      }
      destroySocket();
    }
    return ret;
  }

  public boolean writeCoeff( String address, byte[] coeff )
  {
    // Log.v( TAG, "writeCoeff sddress " + address );
    if ( mRfcommThread != null ) {
      // Toast.makeText(getApplicationContext(), R,string.device_busy, Toast.LENGTH_LONG).show();
      return false;
    }
    boolean ret = false;
    if ( coeff != null ) {
      mCoeff = coeff;
      createSocket( address );
      if ( connectSocket() ) {
        ret = mProtocol.writeCalibration( mCoeff );
        destroySocket();
      }
    }
    return ret;
  }

  public boolean readCoeff( String address, byte[] coeff )
  {
    // Log.v( TAG, "readCoeff sddress " + address );
    if ( mRfcommThread != null ) {
      // Toast.makeText(getApplicationContext(), R,string.device_busy, Toast.LENGTH_LONG).show();
      return false;
    }
    boolean ret = false;
    if ( coeff != null ) {
      createSocket( address );
      if ( connectSocket() ) {
        ret = mProtocol.readCalibration( coeff );
        destroySocket();
        // int k;
        // for ( k=0; k<48; k+=8 ) {
        //   StringWriter sw = new StringWriter();
        //   PrintWriter pw = new PrintWriter( sw );
        //   pw.format( "%02x %02x %02x %02x %02x %02x %02x %02x",
        //     coeff[k+0], coeff[k+1], coeff[k+2], coeff[k+3], coeff[k+4], coeff[k+5], coeff[k+6], coeff[k+7] );
        //   Log.v( TAG, sw.getBuffer().toString() );
        // }
      }
    }
    return ret;
  }

  public String readHeadTail( String address )
  {
    // Log.v( TAG, "readHeadTail sddress " + address );
    if ( mRfcommThread != null ) {
      // Log.v( TAG, "readHeadTail() Rfcomm thread not null");
      // Toast.makeText(getApplicationContext(), R.string.device_busy, Toast.LENGTH_LONG).show();
      return null;
    }
    createSocket( address );
    if ( connectSocket() ) {
      String result = mProtocol.readHeadTail();
      destroySocket();
      // Log.v( TAG, "readHeadTail() " + result);
      return result; 
    }
    return null;
  }


  // on-demand data download
  public int downloadData( String address )
  {
    // Log.v( TAG, "downloadData address " + address );
    if ( mRfcommThread != null ) {
      // Log.v( TAG, "RFcomm thread not null");
      return DistoXProtocol.DISTOX_ERR_CONNECTED;
    }
    createSocket( address );
    if ( connectSocket() ) {
      int to_read = mProtocol.readToRead();
      // Log.v( TAG, "data to read " + to_read );
      if ( to_read <= 0 ) {
        destroySocket();
        return to_read;
      }

      // FIXME asyncTask ?
      int nr_read = 0;
      startRfcommThread( to_read );
      while ( mRfcommThread != null && nr_read < to_read ) {
        nr_read = mRfcommThread.nrReadDataPacket();
        // Log.v( TAG, "nr read " + nr_read + " / " + to_read );
      }
      // Log.v( TAG, "nr read " + nr_read );
      destroySocket();
      if ( mRfcommThread != null ) {
        try {
          mRfcommThread.join();
        } catch ( InterruptedException e ) {
          // TODO
        }
        mRfcommThread = null;
      }
      return nr_read;
    }
    return -1; // failure
  }

};

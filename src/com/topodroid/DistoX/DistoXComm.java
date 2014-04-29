/* @file DistoXComm.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid-DistoX BlueTooth communication 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120801 BT RFcomm connect method call with UUID
 * 20120803 taken nr. of read packets out of RFcomm thread
 * 20120803 commented connectRemoteDevice
 * 20131116 VECTOR packet
 * 20140320 X310 memory functions
 * 20140409 bugfix check if address == null on connect
 */
package com.topodroid.DistoX;

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
// import android.app.AlertDialog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import android.database.DataSetObserver;

// import android.widget.Toast;
import android.util.Log;

public class DistoXComm
{
  private TopoDroidApp app;

  private BluetoothDevice mBTDevice;
  private BluetoothSocket mBTSocket;
  private String mAddress;
  private DistoXProtocol mProtocol;
  public boolean mBTConnected;
  private boolean mCalibMode;   //!< whether the device is in calib-mode

  private static final byte CALIB_BIT = (byte)0x08;
  public byte[] mCoeff;

  // private int   mHead;
  // private int   mTail;
  // public int Head()      { return mHead; }
  // public int Tail()      { return mTail; }

// --------------------------------------------------
  // DataHelper getData() { return app.mData; }

  int nReadPackets;   // number of received data-packet
  long mLastShotId;   // last shot id

  private class RfcommThread extends Thread
  {
    private DistoXProtocol mProto;
    private int toRead; // number of packet to read

    /** 
     * @param protocol    communication protocol
     * @param to_read     number of data to read (use -1 to read forever until timeout or an exception)
     */
    public RfcommThread( DistoXProtocol protocol, int to_read )
    {
      nReadPackets = 0; // reset nr of read packets
      toRead = to_read;
      mProto = protocol;
      // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "RFcommThread cstr ToRead " + toRead );
    }

    public void run()
    {
      boolean hasG = false;
      boolean doWork = true;

      // Log.v( TopoDroidApp.TAG, "RFcomm thread running ... to_read " + toRead );
      while ( doWork && nReadPackets != toRead ) {
        
        int res = mProto.readPacket( toRead >= 0 );
        // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "RFcomm readPacket returns " + res );

        if ( res == DistoXProtocol.DISTOX_PACKET_NONE ) {
          doWork = false;
        } else if ( res == DistoXProtocol.DISTOX_ERR_OFF ) {
          // tell the user !
          // Toast.makeText(app.getApplicationContext(), R.string.device_off, Toast.LENGTH_SHORT).show()
          doWork = false;
        } else if ( res == DistoXProtocol.DISTOX_PACKET_DATA ) {
          ++nReadPackets;
          double d = mProto.mDistance;
          double b = mProto.mBearing;
          double c = mProto.mClino;
          double r = mProto.mRoll;
          TopoDroidApp.Log( TopoDroidApp.LOG_DISTOX, "DATA PACKET " + d + " " + b + " " + c );
          mLastShotId = app.mData.insertShot( app.mSID, -1L, d, b, c, r );
        } else if ( res == DistoXProtocol.DISTOX_PACKET_G ) {
          // TopoDroidApp.Log( TopoDroidApp.LOG_DISTOX, "G PACKET" );
          ++nReadPackets;
          hasG = true;
        } else if ( res == DistoXProtocol.DISTOX_PACKET_M ) {
          // TopoDroidApp.Log( TopoDroidApp.LOG_DISTOX, "M PACKET" );
          ++nReadPackets;
          // get G and M from mProto and save them to store
          app.mData.insertGM( app.mCID, mProto.mGX, mProto.mGY, mProto.mGZ, mProto.mMX, mProto.mMY, mProto.mMZ );
          hasG = false;
        } else if ( res == DistoXProtocol.DISTOX_PACKET_REPLY ) {
          
          byte[] addr = mProto.getAddress();
          byte[] reply = mProto.getReply();
          StringWriter sw = new StringWriter();
          PrintWriter pw  = new PrintWriter(sw);
          pw.format("%02x %02x %02x %02x at %02x%02x", reply[0], reply[1], reply[2], reply[3], addr[1], addr[0] );
          String result = sw.getBuffer().toString();
          TopoDroidApp.Log( TopoDroidApp.LOG_DISTOX, "REPLY PACKET: " + result ); 

          if ( addr[0] == (byte)0x00 && addr[1] == (byte)0x80 ) { // 0x8000
            // TopoDroidApp.Log( TopoDroidApp.LOG_DISTOX, "toggle reply" );
            // if ( (reply[0] & CALIB_BIT) == 0 ) {
            //     mProto.sendCommand( (byte)0x31 );  // TOGGLE CALIB ON
            // } else {
            //     mProto.sendCommand( (byte)0x30 );  // TOGGLE CALIB OFF
            // }
          } else if ( ( addr[1] & (byte)0x80) == (byte)0x80 ) { // REPLY TO READ/WRITE-CALIBs
            // TopoDroidApp.Log( TopoDroidApp.LOG_DISTOX, "write reply" );
            // mProto.setWrittenCalib( true );
          } else if ( addr[0] == 0x20 && addr[1] == (byte)0xC0 ) { // C020 READ HEAD-TAIL
            // TopoDroidApp.Log( TopoDroidApp.LOG_DISTOX, "read head-tail reply");
            // mHead = (int)( reply[0] | ( (int)(reply[1]) << 8 ) );
            // mTail = (int)( reply[2] | ( (int)(reply[3]) << 8 ) );
          }
        } else if ( res == DistoXProtocol.DISTOX_PACKET_VECTOR ) {
          ++nReadPackets;
          double acc  = mProto.mAcceleration;
          double mag  = mProto.mMagnetic;
          double dip  = mProto.mDip;
          double roll = mProto.mRoll;
          TopoDroidApp.Log( TopoDroidApp.LOG_DISTOX, "VECTOR PACKET " + acc + " " + mag + " " + dip + " " + roll );
          // TODO X310
          if ( app.distoType() == Device.DISTO_X310 ) {
            app.mData.updateShotAMDR( app.mSID, mLastShotId, acc, mag, dip, roll );
          }
        }
      }
      // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "RFcomm thread run() exiting");
      mRfcommThread = null;
      app.notifyConnState( );
    }
  };

  RfcommThread mRfcommThread;


  DistoXComm( TopoDroidApp the_app )
  {
    app        = the_app;
    mProtocol  = null;
    mAddress   = null;
    mRfcommThread = null;
    mBTConnected  = false;
    mBTSocket     = null;
    mCalibMode    = false;
    // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "DistoXComm cstr");
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
  private void closeSocket( boolean wait_thread )
  {
    TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "closeSocket() wait thread " + wait_thread );
    if ( mBTSocket != null ) {
      try {
        mBTSocket.close();
        if ( wait_thread ) {
          if ( mRfcommThread != null ) {
            {
              mRfcommThread.join();
            }
          }
          mRfcommThread = null;
        }
      } catch ( InterruptedException e ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "closeSocket interrupt " + e.getMessage() );
      } catch ( IOException e ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "closeSocket IOexception " + e.getMessage() );
      } finally {
        mBTConnected = false;
      }
    }
    mBTConnected = false;
  }

  /** close the socket and delete it
   * the connection is unusable
   */
  private void destroySocket( boolean wait_thread )
  {
    TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "destroySocket()" );
    closeSocket( wait_thread );
    mBTConnected = false;
    mBTSocket = null;
    mProtocol = null;
  }


  /** create a socket (not connected)
   *  and a connection protocol on it
   */
  private void createSocket( String address )
  {
    TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "createSocket() addr " + address + " mAddress " + mAddress);
    if ( mProtocol == null || ! address.equals( mAddress ) ) {
      if ( mProtocol != null && ! address.equals( mAddress ) ) {
        disconnectRemoteDevice();
      }
      mBTDevice     = app.mBTAdapter.getRemoteDevice( address );
      String device_name = mBTDevice.getName();
      // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "createSocket() device " + device_name );
      try {
        if ( mBTSocket != null ) {
          TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "createSocket() BTSocket not null ... closing");
          mBTSocket.close();
          mBTSocket = null;
        }
        if ( mBTDevice.getBondState() == BluetoothDevice.BOND_NONE ) {
          TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "createSocket() binding device ..." );
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

        if ( TopoDroidApp.mSockType == TopoDroidApp.TOPODROID_SOCK_INSEC ) {
          TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "createSocket() invoke createInsecureRfcommSocket " );
          Class[] classes1 = new Class[ 1 ];
          classes1[0] = int.class;
          Method m = mBTDevice.getClass().getMethod( "createInsecureRfcommSocket", classes1 );
          mBTSocket = (BluetoothSocket) m.invoke( mBTDevice, 1 );
          // mBTSocket = (BluetoothSocket) m.invoke( mBTDevice, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
        // } else if ( TopoDroidApp.mSockType == TopoDroidApp.TOPODROID_SOCK_INSEC_RECORD ) {
        //   // FIXME FIXME FIXME
        //   TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "createSocket() createInsecureRfcommSocketToServiceRecord " );
        //   mBTSocket = mBTDevice.createInsecureRfcommSocketToServiceRecord( UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
        // } else if ( TopoDroidApp.mSockType == TopoDroidApp.TOPODROID_SOCK_INSEC ) {
        //   TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "createSocket() createInsecureRfcommSocket(0) " );
        //   mBTSocket = mBTDevice.createInsecureRfcommSocket( 0 );
        } else { // TOPODROID_SOCK_DEFAULT
          TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "createSocket() createRfcommSocketToServiceRecord " );
          mBTSocket = mBTDevice.createRfcommSocketToServiceRecord( UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
        }

      } catch ( InvocationTargetException e ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "createSocket invoke target " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( UnsupportedEncodingException e ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "createSocket encoding " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( NoSuchMethodException e ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "createSocket no method " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( IllegalAccessException e ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "createSocket access " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( IOException e ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "createSocket IO " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      }

      if ( mBTSocket != null ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "createSocket OK");
        mProtocol = new DistoXProtocol( mBTSocket, app.mDevice );
        mAddress = address;
      } else {
        TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "createSocket fail");
        mProtocol = null;
        mAddress = null;
      }
      mBTConnected = false;
    }
  }

  /** connect the socket to the device
   */
  private boolean connectSocket( String address )
  {
    if ( address == null ) return false;
    TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "DistoXComm.connectSocket(): " + address );
    // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "DistoXComm.connectSocket(): connected is " + mBTConnected );
    createSocket( address );
    if ( mBTSocket != null ) {
      int k = 0;
      while ( ! mBTConnected && k<TopoDroidApp.mCommRetry ) {
        try {
          mBTSocket.connect();
          mBTConnected = true;
        } catch ( IOException e ) {
          TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "connectSocket() IO " + e.getMessage() );
          if ( ++k < TopoDroidApp.mCommRetry ) {
            destroySocket( true );
            createSocket( address );
          }
        }
      }
    } else {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "connectSocket() null socket");
    }
    // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "connectSocket() result " + mBTConnected );
    return mBTConnected;
  }

  private boolean startRfcommThread( int to_read )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "startRFcommThread to_read " + to_read );
    if ( mBTSocket != null ) {
      if ( mRfcommThread == null ) {
        mRfcommThread = new RfcommThread( mProtocol, to_read );
        mRfcommThread.start();
        // downloadLoop();
        // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "startRFcommThread started");
      // } else {
      //   TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "startRFcommThread already running");
      }
      return true;
    } else {
      mRfcommThread = null;
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "startRFcommThread: null socket");
      return false;
    }
  }

  // -------------------------------------------------------- 
  /*
  public boolean connectRemoteDevice( String address )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "connectRemoteDevice address " + address );
    // createSocket( address );
    if ( connectSocket( address ) ) {
      if ( mProtocol != null ) {
        return startRfcommThread( -1 );
      }
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "connectRemoteDevice null protocol");
      destroySocket( true );
    } else {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "connectRemoteDevice failed on connectSocket()" );
    }
    return false;
  }
   */

  public void disconnectRemoteDevice( )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "disconnectRemoteDevice ");
    if ( mBTSocket != null ) {
      destroySocket( true );
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

  private boolean checkRfcommThreadNull( String msg )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, msg );
    return ( mRfcommThread == null );
  }

  /** send the set/unset calib-mode command
   */
  public boolean setCalibMode( String address, boolean turn_on )
  {
    // if ( ! checkRfcommThreadNull( "setCalibMode address " + address ) ) {
    //   return false;
    // }
    boolean ret = false;
    // createSocket( address );
    if ( connectSocket( address ) ) {
      if ( turn_on ) { 
        for (int k=0; k<3; ++k ) { // try three times
          ret |= mProtocol.sendCommand( (byte)0x31 );  // TOGGLE CALIB ON
        }
      } else {
        for (int k=0; k<3; ++k ) { // try three times
          ret |= mProtocol.sendCommand( (byte)0x30 );  // TOGGLE CALIB OFF
        }
      }
      destroySocket( true );
    }
    return ret;
  }

  /** Toggle device calibration mode
   * @param address    device address
   * @param type       device type
   * @return 
   */
  public boolean toggleCalibMode( String address, int type )
  {
    if ( ! checkRfcommThreadNull( "toggleCalibMode address " + address ) ) {
      return false;
    }
    boolean ret = false;
    // createSocket( address );
    if ( connectSocket( address ) ) {
      switch ( type ) {
        case Device.DISTO_A3:
          byte[] result = new byte[4];
          if ( ! mProtocol.read8000( result ) ) {
            destroySocket( true );
            return false;
          }
          TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "toggleCalibMode result " + result[0] );
          if ( (result[0] & CALIB_BIT) == 0 ) {
            ret = mProtocol.sendCommand( (byte)0x31 );  // TOGGLE CALIB ON
          } else {
            ret = mProtocol.sendCommand( (byte)0x30 );  // TOGGLE CALIB OFF
          }
          break;
        case Device.DISTO_X310:
          mCalibMode = ! mCalibMode;
          ret = setCalibMode( address, mCalibMode );
          break;
      }
      destroySocket( true );
    }
    return ret;
  }

  public boolean writeCoeff( String address, byte[] coeff )
  {
    if ( ! checkRfcommThreadNull( "writeCoeff address " + address ) ) {
      return false;
    }
    boolean ret = false;
    if ( coeff != null ) {
      mCoeff = coeff;
      // createSocket( address );
      if ( connectSocket( address ) ) {
        ret = mProtocol.writeCalibration( mCoeff );
        destroySocket( true );
      }
    }
    return ret;
  }

  public boolean readCoeff( String address, byte[] coeff )
  {
    if ( ! checkRfcommThreadNull( "readCoeff address " + address ) ) {
      return false;
    }
    boolean ret = false;
    if ( coeff != null ) {
      // createSocket( address );
      if ( connectSocket( address ) ) {
        ret = mProtocol.readCalibration( coeff );
        destroySocket( true );
        // int k;
        // for ( k=0; k<48; k+=8 ) {
        //   StringWriter sw = new StringWriter();
        //   PrintWriter pw = new PrintWriter( sw );
        //   pw.format( "%02x %02x %02x %02x %02x %02x %02x %02x",
        //     coeff[k+0], coeff[k+1], coeff[k+2], coeff[k+3], coeff[k+4], coeff[k+5], coeff[k+6], coeff[k+7] );
        //   Log.v( TopoDroidApp.TAG, sw.getBuffer().toString() );
        // }
      }
    }
    return ret;
  }

  public String readHeadTail( String address, int[] head_tail )
  {
    if ( app.distoType() == Device.DISTO_A3 ) {
      if ( ! checkRfcommThreadNull( "readHeadTail address " + address ) ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readHeadTail() Rfcomm thread not null");
        return null;
      }
      {
        // createSocket( address );
        if ( connectSocket( address ) ) {
          String result = mProtocol.readHeadTailA3( head_tail );
          destroySocket( true );
          TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "readHeadTail() result " + result );
          return result; 
        }
      }
    }
    // default
    return null;
  }
  
  // X310 data memory is read-only
  // public int resetX310Memory( String address, int from, int to )
  // {
  //   if ( ! checkRfcommThreadNull( "swapHotBit address " + address ) ) {
  //     return -1;
  //   }
  //   int n = 0;
  //   if ( connectSocket( address ) ) {
  //     n = mProtocol.resetX310Memory( from, to );
  //     destroySocket( true );
  //   }
  //   return n;
  // }

  public int readX310Memory( String address, int from, int to, List< MemoryOctet > memory )
  {
    if ( ! checkRfcommThreadNull( "swapHotBit address " + address ) ) {
      return -1;
    }
    int n = 0;
    if ( connectSocket( address ) ) {
      n = mProtocol.readX310Memory( from, to, memory );
      destroySocket( true );
    }
    return n;
  }

  public int readA3Memory( String address, int from, int to, List< MemoryOctet > memory )
  {
    if ( ! checkRfcommThreadNull( "swapHotBit address " + address ) ) {
      return -1;
    }
    from &= 0x7ff8;
    to   &= 0xfff8;
    if ( from >= 0x8000 ) from = 0;
    if ( to >= 0x8000 ) to &= 0x8000;
    int n = 0;
    if ( from < to && connectSocket( address ) ) {
      n = mProtocol.readMemory( from, to, memory );
      destroySocket( true );
    }
    return n;
  }

  /** swap hot bit in the range [from, to)
   */
  public int swapHotBit( String address, int from, int to )
  {
    if ( ! checkRfcommThreadNull( "swapHotBit address " + address ) ) {
      return -1;
    }

    from &= 0x7ff8;
    to   &= 0xfff8;
    if ( from >= 0x8000 ) from = 0;
    if ( to >= 0x8000 ) to &= 0x8000;

    int n = 0;
    if ( from != to && connectSocket( address ) ) {
      do {
        if ( to == 0 ) {
          to = 0x8000 - 8;
        } else {
          to -= 8;
        }
        // Log.v( TopoDroidApp.TAG, "comm swap hot bit at addr " + to/8 );
        if ( ! mProtocol.swapHotBit( to ) ) break;
        ++ n;
      } while ( to != from );
      destroySocket( true );
      TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "swapHotBit swapped " + n + "data" );
    }
    return n;
  }


  // on-demand data download
  public int downloadData( String address )
  {
    TopoDroidApp.Log( TopoDroidApp.LOG_COMM,
      "DistoXComm.downloadData address " + address + " DistoX type " + app.distoType() );
    if ( mRfcommThread != null ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "DistoXComm.downloadData RFcomm thread not null");
      return DistoXProtocol.DISTOX_ERR_CONNECTED;
    }
    {
      // createSocket( address );
      if ( connectSocket( address ) ) {
        // TopoDroidApp.Log( TopoDroidApp.LOG_ERR,
        //                   "DistoXComm.downloadData socket connected ... DistoX type " + app.distoType() );
        if ( app.distoType() == Device.DISTO_A3 ) {
          int prev_read = -1;
          int to_read = mProtocol.readToReadA3();
          // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "DistoXComm.downloadData A3 to-read " + to_read );
          if ( to_read <= 0 ) {
            destroySocket( true );
            return to_read;
          }

          // FIXME asyncTask ?
          // nReadPackets = 0; // done in RfcommThread cstr
          startRfcommThread( to_read );
          while ( mRfcommThread != null && nReadPackets < to_read ) {
            if ( nReadPackets != prev_read ) {
              // TopoDroidApp.Log( TopoDroidApp.LOG_COMM, "downloadData A3 read " + nReadPackets + " / " + to_read );
              prev_read = nReadPackets;
              // try {
              //   Thread.sleep( 500 );
              // } catch ( InterruptedException e ) { }
            }
          }
          // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "downloadData A3 read " + nReadPackets );
          destroySocket( true );
        } else if ( app.distoType() == Device.DISTO_X310 ) {
          startRfcommThread( -1 );
          while ( mRfcommThread != null ) {
            try {
              Thread.sleep( 500 );
            } catch ( InterruptedException e ) { }
          }
          // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "DistoXComm.downloadData X310 read " + nReadPackets );
          destroySocket( true );
        } else {
          TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "DistoXComm.downloadData unknown DistoType " + app.distoType() );
        }
        if ( mRfcommThread != null ) {
          try {
            mRfcommThread.join();
          } catch ( InterruptedException e ) {
            // TODO
          }
          mRfcommThread = null;
        }
        return nReadPackets;
      } else {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "DistoXComm.downloadData fail to connect socket");
      }
    }
    return -1; // failure
  }

};

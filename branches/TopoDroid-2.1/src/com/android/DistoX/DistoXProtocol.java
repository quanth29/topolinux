/* @file DistoXProtocol.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid TopoDroid-DistoX communication protocol
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120726 TopoDroid log
 * 20130205 swapHotBit
 * 20131116 X310 ready
 * 20140115 X310 memory read
 */
package com.android.DistoX;

import java.io.IOException;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.List;

// import java.Thread;
import java.nio.channels.ClosedByInterruptException;

// import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.util.Log;

import android.widget.Toast;

public class DistoXProtocol
{
  private Device mDevice;
  // private DistoX mDistoX;
  // private BluetoothDevice  mBTDevice;
  private BluetoothSocket  mSocket = null;
  private DataInputStream  mIn;
  private DataOutputStream mOut;
  private byte[] mHeadTailA3;  // head/tail for Protocol A3
  private byte[] mAddr8000;
  private byte[] mAddress;   // request-reply address
  private byte[] mRequestBuffer;   // request buffer
  private byte[] mReplyBuffer;     // reply data
  private byte[] mAcknowledge;
  private byte[] mBuffer;
  private byte   mSeqBit;          // sequence bit: 0x00 or 0x80

  private static final UUID MY_UUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );

  static final int DISTOX_PACKET_NONE   = 0;
  static final int DISTOX_PACKET_DATA   = 1;
  static final int DISTOX_PACKET_G      = 2;
  static final int DISTOX_PACKET_M      = 3;
  static final int DISTOX_PACKET_VECTOR = 4;
  static final int DISTOX_PACKET_REPLY  = 5;

  static final int DISTOX_ERR_HEADTAIL     = -1;
  static final int DISTOX_ERR_HEADTAIL_IO  = -2;
  static final int DISTOX_ERR_HEADTAIL_EOF = -3;
  static final int DISTOX_ERR_CONNECTED    = -4;
  static final int DISTOX_ERR_OFF          = -5; // distox has turned off

  double mDistance;
  double mBearing;
  double mClino;
  double mRoll;
  double mAcceleration;
  double mMagnetic;
  double mDip; // magnetic dip
  private byte mRollHigh; // high byte of roll
  long mGX, mGY, mGZ;
  long mMX, mMY, mMZ;

  byte[] getAddress() { return mAddress; }
  byte[] getReply()   { return mReplyBuffer; }

  // FIXME the record of written calibration is not used
  // boolean writtenCalib = false;
  // public void setWrittenCalib( boolean b ) { writtenCalib = b; } 

  public DistoXProtocol( BluetoothSocket socket, Device device )
  {
 
    mDevice = device;
    mSocket = socket;
    // mDistoX = distox;
    mSeqBit = (byte)0xff;

    mHeadTailA3 = new byte[3];   // to read head/tail for Protocol A3
    mHeadTailA3[0] = 0x38;
    mHeadTailA3[1] = 0x20;       // address 0xC020
    mHeadTailA3[2] = (byte)0xC0;

    mAddr8000 = new byte[3];
    mAddr8000[0] = 0x38;
    mAddr8000[1] = 0x00; // address 0x8000
    mAddr8000[2] = (byte)0x80;

    mAddress = new byte[2];
    mReplyBuffer   = new byte[4];
    mRequestBuffer = new byte[8];

    mAcknowledge = new byte[1];
    // mAcknowledge[0] = ( b & 0x80 ) | 0x55;

    mBuffer = new byte[8];
  
    try {
      if ( mSocket != null ) {
        mIn  = new DataInputStream( mSocket.getInputStream() );
        mOut = new DataOutputStream( mSocket.getOutputStream() );
      }
    } catch ( IOException e ) {
      // NOTE socket is null there is nothing we can do
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "Protocol cstr conn failed " + e.getMessage() );
    }
  }

  public int handlePacket( ) 
  {
    // StringWriter sw = new StringWriter();
    // PrintWriter pw = new PrintWriter( sw );
    // pw.format("%02x %02x %02x %02x %02x %02x %02x %02x", mBuffer[0], mBuffer[1], mBuffer[2],
    //     mBuffer[3], mBuffer[4], mBuffer[5], mBuffer[6], mBuffer[7] );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PROTO, "handlePacket " + sw.getBuffer().toString() );

    byte type = (byte)(mBuffer[0] & 0x3f);

    int high, low;
    switch ( type ) {
      case 0x01: // data
        int dhh = (int)( mBuffer[0] & 0x40 );
        double d =  dhh * 1024.0 + MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
        double b = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
        double c = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
        // X31--ready
        mRollHigh = mBuffer[7];

        int r7 = (int)(mBuffer[7] & 0xff); if ( r7 < 0 ) r7 += 256;
        // double r = (mBuffer[7] & 0xff);
        double r = r7;

        if ( mDevice.mType == Device.DISTO_A3 ) {
          mDistance = d / 1000.0;
        } else if ( mDevice.mType == Device.DISTO_X310 ) {
          if ( d < 99999 ) {
            mDistance = d / 1000.0;
          } else {
            mDistance = 100 + (d-100000) / 100.0;
          }
        }

        mBearing  = b * 180.0 / 32768.0; // 180/0x8000;
        mClino    = c * 90.0  / 16384.0; // 90/0x4000;
        if ( c >= 32768 ) { mClino = (65536 - c) * (-90.0) / 16384.0; }
        mRoll = r * 180.0 / 128.0;
        // pw.format(" %7.2f %6.1f %6.1f", mDistance, mBearing, mClino );
        // TopoDroidApp.Log( TopoDroidApp.LOG_PROTO, "handlePacket " + sw.getBuffer().toString() );
        return DISTOX_PACKET_DATA;
      case 0x02: // g
        mGX = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
        mGY = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
        mGZ = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );

        if ( mGX > TopoDroidApp.ZERO ) mGX = mGX - TopoDroidApp.NEG;
        if ( mGY > TopoDroidApp.ZERO ) mGY = mGY - TopoDroidApp.NEG;
        if ( mGZ > TopoDroidApp.ZERO ) mGZ = mGZ - TopoDroidApp.NEG;
        // pw.format(" %x %x %x", mGX, mGY, mGZ );
        // TopoDroidApp.Log( TopoDroidApp.LOG_PROTO, "handlePacket " + sw.getBuffer().toString() );
        return DISTOX_PACKET_G;
      case 0x03: // m
        mMX = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
        mMY = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
        mMZ = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );

        if ( mMX > TopoDroidApp.ZERO ) mMX = mMX - TopoDroidApp.NEG;
        if ( mMY > TopoDroidApp.ZERO ) mMY = mMY - TopoDroidApp.NEG;
        if ( mMZ > TopoDroidApp.ZERO ) mMZ = mMZ - TopoDroidApp.NEG;
        // pw.format(" %x %x %x", mMX, mMY, mMZ );
        // TopoDroidApp.Log( TopoDroidApp.LOG_PROTO, "handlePacket " + sw.getBuffer().toString() );
        return DISTOX_PACKET_M;
      case 0x04: // vector data packet
        if ( mDevice.mType == Device.DISTO_X310 ) {
          double acc = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
          double mag = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
          double dip = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
          double rh = MemoryOctet.toInt( mRollHigh, mBuffer[7] );
          mAcceleration = acc;
          mMagnetic = mag;
          mDip = dip * 90.0  / 16384.0; // 90/0x4000;
          if ( dip >= 32768 ) { mDip = (65536 - dip) * (-90.0) / 16384.0; }
          mRoll  = rh * 180.0 / 32768.0; // 180/0x8000;
        }
        return DISTOX_PACKET_VECTOR;
      case 0x38: 
        mAddress[0] = mBuffer[1];
        mAddress[1] = mBuffer[2];
        mReplyBuffer[0] = mBuffer[3];
        mReplyBuffer[1] = mBuffer[4];
        mReplyBuffer[2] = mBuffer[5];
        mReplyBuffer[3] = mBuffer[6];
        // TopoDroidApp.Log( TopoDroidApp.LOG_PROTO, "handlePacket mReplyBuffer" );
        return DISTOX_PACKET_REPLY;
      // default:
      //   return DISTOX_PACKET_NONE;
    }
    return DISTOX_PACKET_NONE;
  } 

  public int readPacket( boolean no_timeout ) 
  {
    // Log.v( TopoDroidApp.TAG, "readPacket no-timeout " + no_timeout );
    try {
      int timeout = 0;
      int maxtimeout = 8;
      int available = 0;
      if ( no_timeout ) {
        available = 8;
      } else { // do timeout
        while ( ( available = mIn.available() ) == 0 && timeout < maxtimeout ) {
          ++ timeout;
          Thread.sleep( 250 );
        }
      }
      if ( available > 0 ) {
        mIn.readFully( mBuffer, 0, 8 );
        byte seq  = (byte)(mBuffer[0] & 0x80); // sequence bit
        boolean ok = ( seq != mSeqBit );
        mSeqBit = seq;
        // if ( (mBuffer[0] & 0x0f) != 0 ) // ack every packet
        { 
          mAcknowledge[0] = (byte)(( mBuffer[0] & 0x80 ) | 0x55);
          // TopoDroidApp.Log( TopoDroidApp.LOG_PROTO, "readPacket byte ... writing ack");
          mOut.write( mAcknowledge, 0, 1 );
        }
        if ( ok ) return handlePacket();
      } // else timedout with no packet
    } catch (InterruptedException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readPacket InterruptedException" + e.toString() );
    } catch ( EOFException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_PROTO, "readPacket EOFException" + e.toString() );
    } catch (ClosedByInterruptException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readPacket ClosedByInterruptException" + e.toString() );
    // } catch (InterruptedException e ) {
    //   TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readPacket InterruptedException" + e.toString() );
    } catch (IOException e ) {
      // this is OK: the DistoX has been turned off
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readPacket IOException " + e.toString() + " OK distox turned off" );
      return DISTOX_ERR_OFF;
    }
    return DISTOX_PACKET_NONE;
  }

  public boolean sendCommand( byte cmd )
  {
    // StringWriter sw = new StringWriter();
    // PrintWriter pw = new PrintWriter( sw );
    // pw.format("Send command %02x", cmd );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PROTO, "sendCommand " + sw.getBuffer().toString() );

    try {
      mRequestBuffer[0] = (byte)(cmd);
      mOut.write( mRequestBuffer, 0, 1 );
    } catch (IOException e ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "sendCommand failed" );
      return false;
    }
    return true;
  }

  public int readToReadA3() // number of data-packet to read
  {
    try {
      mOut.write( mHeadTailA3, 0, 3 );
      mIn.readFully( mBuffer, 0, 8 );
      if ( mBuffer[0] != (byte)( 0x38 ) ) { return DISTOX_ERR_HEADTAIL; }
      if ( mBuffer[1] != mHeadTailA3[1] ) { return DISTOX_ERR_HEADTAIL; }
      if ( mBuffer[2] != mHeadTailA3[2] ) { return DISTOX_ERR_HEADTAIL; }
      int head = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
      int tail = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
      int ret = ( head >= tail )? (head-tail)/8 : ((0x8000 - tail) + head)/8; 

      // DEBUG
      // StringWriter sw = new StringWriter();
      // PrintWriter pw = new PrintWriter( sw );
      // pw.format("%02x%02x-%02x%02x", mBuffer[4], mBuffer[3], mBuffer[6], mBuffer[5] );
      // TopoDroidApp.Log( TopoDroidApp.LOG_PROTO, "readToRead Head-Tail " + sw.getBuffer().toString() + " " + head + " - " + tail + " = " + ret);

      return ret;
    } catch ( EOFException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readToRead Head-Tail read() failed" );
      return DISTOX_ERR_HEADTAIL_EOF;
    } catch (IOException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readToRead Head-Tail read() failed" );
      return DISTOX_ERR_HEADTAIL_IO;
    }
  }

  boolean swapHotBit( int addr )
  {
    try {
      mBuffer[0] = (byte) 0x38;
      mBuffer[1] = (byte)( addr & 0xff );
      mBuffer[2] = (byte)( (addr>>8) & 0xff );
      mOut.write( mBuffer, 0, 3 );
      mIn.readFully( mBuffer, 0, 8 );
      if ( mBuffer[0] != (byte)0x38 ) { 
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "swapHotBit-38 wrong reply packet addr " + addr );
        return false;
      }

      int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
      // Log.v( TopoDroidApp.TAG, "proto read ... addr " + addr + " reply addr " + reply_addr );
      if ( reply_addr != addr ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "swapHotBit-38 wrong reply addr " + reply_addr + " addr " + addr );
        return false;
      }
      mBuffer[0] = (byte)0x39;
      // mBuffer[1] = (byte)( addr & 0xff );
      // mBuffer[2] = (byte)( (addr>>8) & 0xff );
      if ( mBuffer[3] == 0x00 ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "swapHotBit refusing to swap addr " + addr );
        return false;
      }  

      mBuffer[3] |= (byte)0x80; // RESET HOT BIT
      mOut.write( mBuffer, 0, 7 );
      mIn.readFully( mBuffer, 0, 8 );
      if ( mBuffer[0] != (byte)0x38 ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "swapHotBit-39 wrong reply packet addr " + addr );
        return false;
      }
      reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
      // Log.v( TopoDroidApp.TAG, "proto reset ... addr " + addr + " reply addr " + reply_addr );
      if ( reply_addr != addr ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "swapHotBit-39 wrong reply addr " + reply_addr + " addr " + addr );
        return false;
      }
    } catch ( EOFException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "swapHotBit EOF failed addr " + addr );
      return false;
    } catch (IOException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "swapHotBit IO failed addr " + addr );
      return false;
    }
    return true;
  }

  // FIXME this is specific to DistoA3 (DistoX v.1)
  public String readHeadTailA3( int[] head_tail )
  {
    try {
      mOut.write( mHeadTailA3, 0, 3 );
      mIn.readFully( mBuffer, 0, 8 );
      if ( mBuffer[0] != (byte)( 0x38 ) ) { return null; }
      if ( mBuffer[1] != mHeadTailA3[1] ) { return null; }
      if ( mBuffer[2] != mHeadTailA3[2] ) { return null; }
      // TODO value of mHeadTailA3 in byte[3-7]
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );
      pw.format("%02x%02x-%02x%02x", mBuffer[4], mBuffer[3], mBuffer[6], mBuffer[5] );
      head_tail[0] = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
      head_tail[1] = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );

      // TopoDroidApp.Log( TopoDroidApp.LOG_PROTO, "readHeadTail " + sw.getBuffer().toString() );
      return sw.getBuffer().toString();
    } catch ( EOFException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readHeadTail read() EOF failed" );
      return null;
    } catch (IOException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readHeadTail read() IO failed" );
      return null;
    }
  }

  // X310    
  private static int DATA_PER_BLOCK = 56;
  private static int BYTE_PER_DATA  = 18;

  private int index2addrX310( int index )
  {
    int addr = 0;
    while ( index > DATA_PER_BLOCK ) {
      index -= DATA_PER_BLOCK;
      addr += 0x400;
    }
    addr += BYTE_PER_DATA * index;
    return addr;
  }

  int addr2indexX310( int addr )
  {
    int index = 0;
    addr = addr - ( addr % 8 );
    while ( addr >= 0x400 ) {
      addr -= 0x400;
      index += DATA_PER_BLOCK;
    }
    index += (int)(addr/BYTE_PER_DATA);
    return index;
  }

  // memory layout
  // byte 0-7  first packet
  // byte 8-15 second packet
  // byte 16   hot-flag for the first packet
  // byte 17   hot-flag for the second packet

  public int readX310Memory( int start, int end, List< MemoryOctet > data )
  {
   int cnt = 0;
    while ( start < end ) {
      int addr = index2addrX310( start );
      int endaddr = addr + BYTE_PER_DATA;
      MemoryOctet result = new MemoryOctet( addr );
      int k = 0;
      for ( ; addr < endaddr && k < 8; addr += 4, k+=4 ) {
        mBuffer[0] = (byte)( 0x38 );
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        // TODO write and read
        try {
          mOut.write( mBuffer, 0, 3 );
          mIn.readFully( mBuffer, 0, 8 );
        } catch ( IOException e ) {
          TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readMemory() IO failed" );
          break;
        }
        if ( mBuffer[0] != (byte)( 0x38 ) ) break;
        int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1]);
        if ( reply_addr != addr ) break;
        for (int i=3; i<7; ++i) result.data[k+i-3] = mBuffer[i];
      }
      if ( k == 8 ) {
        data.add( result );
        Log.v( TopoDroidApp.TAG, "memory " + result.toString() );
        ++ cnt;
      } else {
        break;
      }
      ++start;
    }
    return cnt;
  }

  public int readMemory( int start, int end, List< MemoryOctet > data )
  {
    if ( start < 0 ) start = 0;
    if ( end > 0x8000 ) end = 0x8000;
    start = start - start % 8;
    end   = end - ( end % 8 );
    if ( start >= end ) return -1;
    int cnt = 0; // number of data read
    for ( ; start < end; start += 8 ) {
      MemoryOctet result = new MemoryOctet( start );
      int k = 0;
      for ( ; k<8; k+=4 ) {
        int addr = start+k;
        mBuffer[0] = (byte)( 0x38 );
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        // TODO write and read
        try {
          mOut.write( mBuffer, 0, 3 );
          mIn.readFully( mBuffer, 0, 8 );
        } catch ( IOException e ) {
          TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readMemory() IO failed" );
          break;
        }
        if ( mBuffer[0] != (byte)( 0x38 ) ) break;
        int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1]);
        if ( reply_addr != addr ) break;
        for (int i=3; i<7; ++i) result.data[k+i-3] = mBuffer[i];
      }
      if ( k == 8 ) {
        data.add( result );
        // Log.v( TopoDroidApp.TAG, "memory " + result.toString() );
        ++ cnt;
      } else {
        break;
      }
    }
    return cnt;
  }

  public boolean read8000( byte[] result )
  {
    try {
      mOut.write( mAddr8000, 0, 3 );
      mIn.readFully( mBuffer, 0, 8 );
      if ( mBuffer[0] != (byte)( 0x38 ) ) { return false; }
      if ( mBuffer[1] != mAddr8000[1] ) { return false; }
      if ( mBuffer[2] != mAddr8000[2] ) { return false; }
      result[0] = mBuffer[3];
      result[1] = mBuffer[4];
      result[2] = mBuffer[5];
      result[3] = mBuffer[6];
    } catch ( EOFException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "read8000 read() EOF failed" );
      return false;
    } catch (IOException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "read8000 read() IO failed" );
      return false;
    }
    return true;
  }

  public boolean writeCalibration( byte[] calib )
  { 
    long addr = 0x8010;
    long end  = addr + 48;
    try {
      int k = 0;
      while ( k < 48 ) {
        mBuffer[0] = 0x39;
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        mBuffer[3] = calib[k]; ++k;
        mBuffer[4] = calib[k]; ++k;
        mBuffer[5] = calib[k]; ++k;
        mBuffer[6] = calib[k]; ++k;
        mOut.write( mBuffer, 0, 7 );
        mIn.readFully( mBuffer, 0, 8 );
        // StringWriter sw = new StringWriter();
        // PrintWriter pw = new PrintWriter( sw );
        // pw.format("%02x %02x %02x %02x %02x %02x %02x %02x", mBuffer[0], mBuffer[1], mBuffer[2],
        //    mBuffer[3], mBuffer[4], mBuffer[5], mBuffer[6], mBuffer[7] );
        // TopoDroidApp.Log( TopoDroidApp.LOG_PROTO, "writeCalibration " + sw.getBuffer().toString() );
        if ( mBuffer[0] != 0x38 ) { return false; }
        if ( mBuffer[1] != (byte)( addr & 0xff ) ) { return false; }
        if ( mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) { return false; }
        addr += 4;
      }
    } catch ( EOFException e ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "writeCalibration EOF failed" );
      return false;
    } catch (IOException e ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "writeCalibration IO failed" );
      return false;
    }
    return true;  
  }

  public boolean readCalibration( byte[] calib )
  {
    int addr = 0x8010;
    int end  = addr + 48;
    try {
      int k = 0;
      while ( k < 48 ) {
        mBuffer[0] = 0x38;
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        mOut.write( mBuffer, 0, 3 );
        mIn.readFully( mBuffer, 0, 8 );
        if ( mBuffer[0] != 0x38 ) { return false; }
        if ( mBuffer[1] != (byte)( addr & 0xff ) ) { return false; }
        if ( mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) { return false; }
        calib[k] = mBuffer[3]; ++k;
        calib[k] = mBuffer[4]; ++k;
        calib[k] = mBuffer[5]; ++k;
        calib[k] = mBuffer[6]; ++k;
        addr += 4;
      }
    } catch ( EOFException e ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readCalibration EOF failed" );
      return false;
    } catch (IOException e ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "readCalibration IO failed" );
      return false;
    }
    return true;  
  }

};

/** @file Protocol.cpp
 *
 * @author marco corvi
 * @date jan 2009
 *
 * @brief serial disto A3X protocol
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <stdio.h>
#include <stdlib.h> // system

#ifdef WIN32
  #define printd(...) /* nothing */
#else
  // #define printd printf
  #define printd(fmt...) /* nothing */ 
#endif

#include "Protocol.h"

#ifdef LINUX
  // N.B. execute in background
  const char * agent = "/etc/bluetooth/agent.sh &";
#else
  const char * agent = NULL;
#endif

Protocol::Protocol( const char * dev, bool log )
  : serial( dev, log )
{ 
  printd("Protocol cstr \n");
  // if ( agent ) {
  //   system( agent );
  // }
  // serial.Open();
}
    
bool
Protocol::SendCommandByte( unsigned char byte )
{
  return ( serial.Write( &byte, 1 ) == 1);
}

ProtoError
Protocol::SendCommand( Command cmd ) 
{
  unsigned char byte = 0;
  switch ( cmd ) {
        case CALIB_START:  
          // fprintf(stderr, "Protocol SendCommand() CALIB_START\n");
          byte = 0x31; break;
        case CALIB_STOP:   
          // fprintf(stderr, "Protocol SendCommand() CALIB_STOP\n");
          byte = 0x30; break;
        case SILENT_START: 
          // fprintf(stderr, "Protocol SendCommand() SILENT_START\n");
          byte = 0x33; break;
        case SILENT_STOP:  
          // fprintf(stderr, "Protocol SendCommand() SILENT_STOP\n");
          byte = 0x32; break;
  }
  fprintf(stderr, "Protocol SendCommand() byte 0x%02x\n", byte );
  if ( byte == 0 ) {
    return PROTO_COMMAND;
  }
  // return WriteByte( byte );
  if ( ! SendCommandByte( byte ) ) {
    return PROTO_WRITE;
  }
  fprintf(stderr, "Protocol SendCommand() ok \n");
  return PROTO_OK;
}

bool 
Protocol::Read8000( unsigned char * mode )
{
  unsigned long addr = 0x8000;
  unsigned long reply_addr;
  unsigned char buf[8];
  ssize_t nr;

  buf[0] = 0x38;
  buf[1] = (unsigned char)( addr & 0xff );
  buf[2] = (unsigned char)( (addr>>8) & 0xff );
  nr = serial.Write( buf, 3 ); // read data
  nr = serial.Read( buf, 8 );
  if ( nr > 0 ) {
    if ( buf[0] != 0x38 ) {
      printd("ERROR: read() wrong reply packet at addr %04lx\n", addr);
      return false;
    }
    reply_addr = ((unsigned long)(buf[2]))<<8 | buf[1];
    if ( reply_addr != addr ) {
      printd( "ERROR: read() wrong reply addr %04lx at addr %04lx\n", reply_addr, addr);
      return false;
    }
    // for (i=0; i<4; ++i) old_byte[4*k+i] = buf[3+i];
    *mode = buf[3];
  } else if ( nr < 0 ) {
    printd("ERROR: read() error \n");
    return false;
  } else {
    printd("ERROR: read() returns 0 bytes\n");
    return false;
  }
  return true;
}
    
ProtoError
Protocol::Acknowledge( unsigned char byte )
{
  byte = ( byte & 0x80 ) | 0x55;
  if ( serial.Write( &byte, 1 ) != 1 ) {
    return PROTO_WRITE;
  }
  return PROTO_OK;
}

ProtoError
Protocol::ReadData( )
{
  printd("Protocol ReadData() \n");
  unsigned char b[8];
  ssize_t n; 
  if ( (n = serial.Read( b, 8 )) != 8 ) {
    // printd("ReadData() read failed at addr  0x%04lx \n", addr);
    if ( n == 0 ) {
      return PROTO_TIMEOUT;
    }
    return PROTO_READ;
  }
  Acknowledge( b[0] );
  unsigned char type = b[0] & 0x3f;
  switch ( type ) {
    case 0x01: // data
      data_queue.Put( b );
      break;
    case 0x02: // calib G
    case 0x03: // calib M
      calib_queue.Put( b );
      break;
    default:
      printd("ReadData() wrong packet type 0x%02x\n", type );
      return PROTO_PACKET;
  }
  return PROTO_OK;
}

 

ProtoError
Protocol::WriteCalibration( unsigned char * calib )
{
  unsigned long addr = 0x8010;
  unsigned long end = addr + 48;
  unsigned char b[8];
  while ( addr < end ) {
    b[0] = 0x39;
    b[1] = (unsigned char)(addr & 0xff);
    b[2] = (unsigned char)( (addr >> 8) & 0xff);
    for (int k=0; k<4; ++k) b[3+k] = *calib++;
    if ( serial.Write( b, 7 ) != 7 ) {
      printd("WriteCalibration() write failed at addr 0x%04lx \n", addr);
      return PROTO_WRITE;
    }
    if ( serial.Read( b, 8 ) != 8 ) {
      printd("WriteCalibration() read failed at addr 0x%04lx \n", addr);
      return PROTO_READ;
    } 
    if ( b[0] != 0x38 ) {
      printd("WriteCalibration() not a reply packet\n");
      return PROTO_PACKET;
    }
    if( b[1] != (unsigned char)(addr & 0xff) || 
        b[2] != (unsigned char)( (addr >> 8) & 0xff) ) {
      printd("WriteCalibration() wrong reply addr 0x%02x%02x\n", b[2], b[1]);
      return PROTO_ADDR;
    }
    addr += 4;
  }
  return PROTO_OK;
} 


ProtoError
Protocol::ReadCalibration( unsigned char * byte )
{
  unsigned long addr = 0x8010;
  unsigned long reply_addr;
  unsigned char buf[8];
  int i;
  ssize_t nr;

  for (int k=0; k<48/4; ++k) {
    buf[0] = 0x38;
    buf[1] = (unsigned char)( addr & 0xff );
    buf[2] = (unsigned char)( (addr>>8) & 0xff );
    nr = serial.Write( buf, 3 ); // read data
    nr = serial.Read( buf, 8 );
    if ( nr > 0 ) {
      if ( buf[0] != 0x38 ) {
        printf("ERROR: read() wrong reply packet at addr %04lx\n", addr);
        return PROTO_PACKET;
      }
      reply_addr = ((unsigned long)(buf[2]))<<8 | buf[1];
      if ( reply_addr != addr ) {
        printd("ERROR: read() wrong reply addr %04lx at addr %04lx\n", reply_addr, addr);
        return PROTO_ADDR;
      }
      // for (i=3; i<7; ++i) fprintf(stderr, "%02x ", buf[i] );
      for (i=0; i<4; ++i) byte[4*k+i] = buf[3+i];
    } else if ( nr < 0 ) {
      printd("ERROR: read() error\n");
      return PROTO_READ;
    } else {
      printd("ERROR: read() read returns 0 bytes\n");
      return PROTO_READ;
    }
    addr += 4;
  }
  return PROTO_OK;
}


const char * ProtoErrorStr( ProtoError err )
{
  switch ( err ) {
    case PROTO_OK:      return "OK"; 
    case PROTO_READ:    return "read error";
    case PROTO_WRITE:   return "write error";
    case PROTO_COMMAND: return "command error";
    case PROTO_ADDR:    return "address error";
    case PROTO_PACKET:  return "packet error";
    case PROTO_CONNECT: return "connection error";
    case PROTO_TIMEOUT: return "i/o timeout";
    case PROTO_MAX:     return "illegal error code";
  }
  return "illegal error code";
}


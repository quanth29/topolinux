/** @file Serial.cpp
 *
 * @author marco corvi
 * @date jan 2009
 *
 * @brief communication over a serial channel (SPP)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>    // read write close

#include "Serial.h"



#ifdef WIN32
  #define SERIAL_READ_TIMEOUT 200
  #define SERIAL_READ_TOTAL_TIMEOUT_MULTIPLIER 100
  #define SERIAL_READ_TOTAL_TIMEOUT_CONSTANT 400
  #define SERIAL_WRITE_TOTAL_TIMEOUT_MULTIPLIER 100
  #define SERIAL_WRITE_TOTAL_TIMEOUT_CONSTANT 400
  #define RESET_ERRNO _set_errno(0)
#else
  #define SERIAL_READ_TIMEOUT 5 /* 40 1/10 seconds */
  extern int errno;
  #define RESET_ERRNO errno = 0 
#endif




Serial::Serial( const char * dev, bool log )
  : log_fp( NULL )
  , m_fd( INVALID_HANDLE_VALUE )
{
  if ( log ) {
    RESET_ERRNO;
    log_fp = fopen("distox.log", "a" );
    if ( log_fp == NULL ) {
      fprintf(stderr, 
              "WARNING. Cannot open log file \"distox.log\": %s\n",
              strerror( errno ) );
    }
  }
  if ( log_fp ) {
    fprintf(log_fp, "Serial::cstr connection on device %s\n", m_device );
    fflush( log_fp );
  }
  strcpy( m_device, dev );
}

Serial::~Serial()
{
  if ( log_fp != NULL ) {
    fclose( log_fp );
    log_fp = NULL;
  }
  Close();
}

bool 
Serial::Reconnect()
{
  if ( log_fp ) {
    fprintf(log_fp, "Serial::Reconnect() fd %d \n", m_fd );
    fflush( log_fp );
  }
  
  Close();
  return Open();
}

bool
Serial::Open( )
{
  if ( log_fp ) {
    fprintf(log_fp, "Serial::Open() device \"%s\" \n", m_device );
    fflush( log_fp );
  }
  if ( m_fd != INVALID_HANDLE_VALUE ) {
    if ( log_fp ) {
      fprintf(log_fp, "ERROR: Serial::Open() the device is already open\n");
      fflush( log_fp );
    }
    return false;
  }

  RESET_ERRNO;
  #ifdef WIN32
    m_fd = CreateFileA( m_device,
                       GENERIC_READ | GENERIC_WRITE,
                       0, // fdwShareMode
                       0, // Security attr
                       OPEN_EXISTING,
                       FILE_FLAG_NO_BUFFERING, // not FILE_FLAG_OVERLAPPED, // synchronous
                       0 ); // hTemplateFile
  #else
    m_fd = open( m_device, 
                 O_RDWR ); // | O_NOCTTY ); // need O_NOCTTY ?
  #endif

  if ( m_fd == INVALID_HANDLE_VALUE ) {
    if ( log_fp ) {
      fprintf(log_fp, "ERROR: Serial::Open() failed to open device %s: %s\n", 
              m_device, strerror( errno ) );
      fflush( log_fp );
    }
    return false;
  }

  RESET_ERRNO;
  #ifdef WIN32
    BOOL ret = GetCommState( m_fd, &m_termios_save );
  #else
    bool ret = tcgetattr( m_fd, &m_termios_save ) == 0;
  #endif

  if ( ! ret ) {
    if ( log_fp ) {
      fprintf(log_fp, 
              "ERROR: Serial::Open() failed to get term attrs: %s\n",
              strerror( errno) );
      fflush( log_fp );
    }
    CLOSE( m_fd );
    m_fd = INVALID_HANDLE_VALUE;
    return false;
  }

  m_termios = m_termios_save; // struct copy
  
  RESET_ERRNO;
  #ifdef WIN32
    ret = GetCommTimeouts( m_fd, &m_timeouts_save );
    if ( ! ret ) {
      if ( log_fp ) {
        fprintf(log_fp, "Serial: failed to get term timeouts\n");
        fflush( log_fp );
      }
	  CLOSE( m_fd );
      m_fd = INVALID_HANDLE_VALUE;
      return false;
    }
    m_timeouts = m_timeouts_save;
	
    m_timeouts.ReadTotalTimeoutMultiplier = SERIAL_READ_TOTAL_TIMEOUT_MULTIPLIER;
    m_timeouts.ReadTotalTimeoutConstant   = SERIAL_READ_TOTAL_TIMEOUT_CONSTANT;
    m_timeouts.WriteTotalTimeoutMultiplier = SERIAL_READ_TOTAL_TIMEOUT_CONSTANT;
    m_timeouts.WriteTotalTimeoutConstant   = SERIAL_WRITE_TOTAL_TIMEOUT_CONSTANT;

    ret = SetCommTimeouts( m_fd, &m_timeouts );
    if ( ! ret ) {
      SetCommTimeouts( m_fd, &m_timeouts_save );
	  if ( log_fp ) {
        fprintf(log_fp, "Serial: failed to get term timeouts\n");
        fflush( log_fp );
      }
      CLOSE( m_fd );
      m_fd = INVALID_HANDLE_VALUE;
      return false;
    }

    m_termios.fParity = FALSE; // PARENB
    m_termios.fBinary = TRUE;  // ~ICANON
    m_termios.fInX    = FALSE; // IXON
    m_termios.fNull   = FALSE; 

    ret = SetCommState( m_fd, &m_termios );
  #else
    m_termios.c_cc[ VMIN ]  = 0; 
    m_termios.c_cc[ VTIME ] = 40; /* 4 seconds */;
    // printf("m_termios.c_cc %d %d \n", m_termios.c_cc[ VMIN ], m_termios.c_cc[ VTIME ] );
// (0, 0) non-blocking immediate read
//        may return 0 if no byte is available
// (N, 0) reader returns when at least N bytes have been read
//        may block indefinitely
// (0, T) reader returns
//      [1, nbytes] before timeout expires
//      [0] if timeout expires
// (N, T) readers returns
//      N if the byte are available/arrive
//      [1, N-1] if the timeout expires
//      wait indefinitely if no byte arrives/is available
    m_termios.c_lflag &= ~( ECHO | ICANON | IEXTEN | ISIG );
    m_termios.c_iflag &= ~( BRKINT | ICRNL | INPCK | ISTRIP | IXON );
    m_termios.c_cflag &= ~( CSIZE | PARENB );
    m_termios.c_cflag |= CS8;
    m_termios.c_oflag &= ~( OPOST );
    // ~ECHO disable echoing
    // ~ICANON disable special char EOL, etc.
    // ~IEXTEN disable impl. defined input processing
    // ~ISIG do not generate signal INTR, QUIT, SUSP, or DSUSP
    // ~BRKINT if IGNBRK is set BREAK is ignored, otherwise is null char
    // ~ICRNL do not translate CR to NL
    // ~INPCK do not check parity on input
    // ~ISTRIP do not strip 8-th bit
    // ~IXON disable XON/XOFF flow control on input
    // ~PARENB parity not enabled
    // ~CSIZE clear char mask
    // CS8 set 8-bit char mask
    // ~OPOST disable impl. defined output processing
  
    ret = tcsetattr( m_fd, TCSANOW, &m_termios ) == 0;
  #endif

  if ( ! ret ) {
    if ( log_fp ) {
      fprintf(log_fp, 
              "ERROR: Serial::Open() failed to set term in raw mode: %s\n",
              strerror( errno ) );
      fflush( log_fp );
    }
    Close( );
    return false;
  } else {
    if ( log_fp ) {
      fprintf(log_fp, "Serial::Open() set term in raw mode\n");
      fflush( log_fp );
    }
  } 
  return true;
}

void 
Serial::Close()
{
  if ( log_fp ) {
    fprintf(log_fp, "Serial::Close() fd %d \n", m_fd );
    fflush( log_fp );
  }

  if ( IsOpen() ) {
    #ifdef WIN32
      SetCommState( m_fd, &m_termios_save );
    #else
      tcflush( m_fd, TCIOFLUSH );
      tcsetattr( m_fd, TCSANOW, &m_termios_save );
    #endif
    CLOSE( m_fd );
    m_fd = INVALID_HANDLE_VALUE;
  }
}
  
ssize_t
Serial::Write( const unsigned char * buf, size_t n )
{
  if ( ! IsOpen() ) {
    if ( log_fp ) {
      fprintf(log_fp, "ERROR: Serial::Write() device not open\n");
      fflush( log_fp );
    }
    return -1;
  }
  if ( buf == NULL ) {
    if ( log_fp ) {
      fprintf(log_fp, "WARNING Serial::Write() NULL buffer\n");
      fflush( log_fp );
    }
    return 0;
  }
  if ( n == 0 ) {
    if ( log_fp ) {
      fprintf(log_fp, "WARNING Serial::Write() zero byte write request\n");
      fflush( log_fp );
    }
    return 0;
  }

  size_t nw = 0;
  while ( nw < n) {
    RESET_ERRNO;
    #ifdef WIN32
      // FIXME write timeout
      DWORD nw0 = 0;
      BOOL ret = WriteFile( m_fd, buf+nw, n-nw, &nw0, 0 );
    #else 
      ssize_t nw0 = write( m_fd, buf+nw, n-nw );
      bool ret = nw0 > 0;
    #endif
    if ( nw0 == 0 ) {
      if ( log_fp ) {
        fprintf(log_fp, "WARNING: Serial::Write() write 0 bytes\n");
        fflush( log_fp );
      }
      return 0;
    } else if ( ret ) {
	  if ( log_fp ) {
        fprintf(log_fp, "Serial::Write() written ");
        for ( RW_TYPE k=0; k<nw0; ++k ) fprintf(log_fp, "%02x ", buf[nw+k] );
        fprintf(log_fp, "\n");
        fflush( log_fp );
      }
      nw  += nw0;
    } else {
      if ( log_fp ) {
        fprintf(log_fp,
                "ERROR: Serial::Write() error %d: %s \n", 
                nw0, strerror( errno ) ); 
        fflush( log_fp );
      }
      return nw0;
    }
  }
  return nw;
}

ssize_t
Serial::Read( unsigned char * buf, size_t n )
{
  if ( ! IsOpen() ) {
    if ( log_fp ) {
      fprintf(log_fp, "ERROR: Serial::Read() device not open\n");
      fflush( log_fp );
    }
    return 0;
  }
  if ( buf == NULL ) {
    if ( log_fp ) {
      fprintf(log_fp, "WARNING: Serial::Read() NULL buffer\n");
      fflush( log_fp );
    }
    return 0;
  }
  if ( n == 0 ) {
    if ( log_fp ) {
      fprintf(log_fp, "WARNING: Serial::Read() zero bytes read requested\n");
      fflush( log_fp );
    }
    return 0;
  }

  size_t nr = 0;
  while ( nr < n ) {
    RESET_ERRNO;
    #ifdef WIN32
      // FIXME read timeout
      DWORD nr0 = 0;
      BOOL ret = ReadFile( m_fd, buf+nr, n-nr, &nr0, 0 );
    #else
      ssize_t nr0 = read( m_fd, buf+nr, n-nr );
      bool ret = nr0 > 0;
    #endif
    
    if ( nr0 == 0 ) { // timeout expired
      if ( log_fp ) {
        fprintf(log_fp,
                "WARNING: Serial::Read() read timeout expired: %s\n",
                strerror( errno ) );
        fflush( log_fp );
      }
      return 0;
    } else if ( ret ) {
      if ( log_fp ) {
        fprintf(log_fp, "Serial::Read() read ");
        for ( RW_TYPE k=0; k<nr0; ++k ) fprintf(log_fp, "%02x ", buf[nr+k] );
        fprintf(log_fp, "\n");
        fflush( log_fp );
      }
      nr += nr0;
    } else { // error
      if ( log_fp ) {
        fprintf(log_fp, "ERROR: Serial::Read() read error %d: %s\n",
                nr0, strerror( errno ) );
        fflush( log_fp );
      }
      return nr0;
    }
  }
  return nr;
}


// -----------------------------------------------------
#ifdef TEST

#ifdef WIN32
  #define DEFAULT_DEVICE "COM1"
#else
  #define DEFAULT_DEVICE "/dev/rfcomm0"
#endif

int main( int argc, char ** argv )
{
  char * device = DEFAULT_DEVICE;

  if ( argc > 1 ) {
    device = argv[1];
  }
  Serial serial( device );
  printf("Connecting to Disto via device \"%s\" ...\n", device );
  if ( serial.Open() ) {
    printf("successfully connected!\n");
    printf("Enter a character to continue: ");
    getchar();
    serial.Close();
  } else {
    printf("failed to connect.\n");
  }
  return 0;
}

#endif


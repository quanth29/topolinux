/** @file Device.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX device object
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 201311   added model and type info
 */
package com.topodroid.DistoX;

class Device
{
  String mAddress; // device mac address
  String mModel;   // device model (type string)
  int mType;       // device type
  int mHead;
  int mTail;

  final static int DISTO_NONE = 0;
  final static int DISTO_A3   = 1;
  final static int DISTO_X310 = 2;
  final static String[] typeString = { "Unknown", "A3", "X310" };

  static String typeToString( int type ) { return typeString[ type ]; }

  static int  stringToType( String str ) 
  {
    if ( str.equals( "A3" ) || str.equals( "DistoX" ) ) return DISTO_A3;
    if ( str.equals( "X310" ) || str.startsWith( "DistoX-" ) ) return DISTO_X310;
    return DISTO_NONE;
  }

  Device( String addr, String model, int h, int t )
  {
    mAddress = addr;
    mModel = model;
    mType = stringToType( model );
    mHead = h;
    mTail = t;
  }

  Device( String addr, String model )
  {
    mAddress = addr;
    mModel = model;
    mType = stringToType( model );
    mHead = 0;
    mTail = 0;
  }

  public String toString() { return typeString[ mType ] + " " + mAddress; }
  
  
}

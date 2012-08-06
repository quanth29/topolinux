/** @file DistoXStationName.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid station name increment (static)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120520 created
 * 20120726 TopoDroid log
 */
package com.android.DistoX;

public class DistoXStationName
{
  // private static final String TAG = "DistoX";

  private static char[] lc = {
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' 
  };
  private static char[] uc = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' 
  };

  public static String increment( String name )
  {
    // if name is numeric
    if ( name != null && name.length() > 0 ) {
      int len = name.length();
      if ( len > 0 ) {
        char ch = name.charAt( len - 1 );
        int k = Character.getNumericValue( ch );
        if ( k >= 10 && k < 35 ) {
          k -= 9; // - 10 + 1
          // TopoDroidApp.Log( TopoDroidApp.LOG_NAME, "not numeric " + k );
          return name.substring( 0, len - 1 ) + 
           ( Character.isLowerCase( ch )? lc[k] : uc[k] );
        } else if ( k >= 0 && k < 10 ) {
          int n = 0;
          int s = 1;
          // TopoDroidApp.Log( TopoDroidApp.LOG_NAME, "name >" + name + "< n " + n );
          while ( len > 0 ) {
            -- len;
            k = Character.getNumericValue( name.charAt(len) );
            if ( k < 0 || k >= 10 ) { ++len; break; }
            n += s * k;
            s *= 10;
            // TopoDroidApp.Log( TopoDroidApp.LOG_NAME, "k " + k + " n " + n + " len " + len);
          }
          if ( len > 0 ) {
            return name.substring( 0, len ) + Integer.toString( n+1 );
          } 
          return Integer.toString( n+1 );
        }
      }
    }
    return "";
  }

}

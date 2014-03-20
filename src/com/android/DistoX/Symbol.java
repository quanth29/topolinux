/** @file Symbol.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid drawing symbol: 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.android.DistoX;

import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.LinearLayout;

class Symbol // implements View.OnClickListener
{
  boolean mEnabled;  //!< whether the symbol is enabled in the library
  String  mThName;   // therion name

  /** default cstr
   */
  Symbol()
  {
    mEnabled = true;
    mThName  = null;
  }

  Symbol( String th_name ) 
  { 
    mEnabled = true;
    mThName = th_name;
  }

  /** cstr 
   * @param enabled  whether the symbol is enabled
   */
  Symbol(boolean enabled ) { mEnabled = enabled; }

  boolean isEnabled() { return mEnabled; }

  void setEnabled( boolean enabled ) { mEnabled = enabled; }

}

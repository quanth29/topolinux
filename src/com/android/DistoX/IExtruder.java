/* @file IExtruder.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid 3d sketch: surface extruder interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import android.graphics.Path;

/**
 */
public interface IExtruder 
{
  void doExtrudeLineRegion( );

  void doStretchLineRegion( int view );
}


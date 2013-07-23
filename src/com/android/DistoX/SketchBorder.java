/** @file SketchBorder.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3d sketch: surface border
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130216 created
 */
package com.android.DistoX;

import java.util.ArrayList;

class SketchBorder
{
  ArrayList< SketchSide > sides;

  SketchBorder()
  {
    sides = new ArrayList<SketchSide>();
  }

  void add( SketchSide s ) { sides.add( s ); }

  SketchSide get( int k ) { return sides.get(k); }

}

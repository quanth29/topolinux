/** @file SymbolInterface.java
 *
 */
package com.android.DistoX;

import android.graphics.Paint;
import android.graphics.Path;

public interface SymbolInterface
{
  String getName();
  Paint  getPaint();
  Path   getPath();
  boolean isOrientable();
  void rotate( float angle );
}


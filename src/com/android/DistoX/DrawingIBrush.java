/* @file DrawingIBrush.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: brush interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import android.graphics.Path;

/**
 */
public interface DrawingIBrush {
    public void mouseDown( Path path, float x, float y);
    public void mouseMove( Path path, float x, float y);
    public void mouseUp( Path path, float x, float y);
}

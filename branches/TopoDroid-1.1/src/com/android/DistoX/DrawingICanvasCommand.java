/* @file DrawingICanvasCommand.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: command interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import android.graphics.Canvas;

/* interface for the canvas commands
 */
public interface DrawingICanvasCommand {
    public void draw(Canvas canvas);
    public void undo();
}

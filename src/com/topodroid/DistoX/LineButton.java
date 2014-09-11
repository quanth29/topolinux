/** @file LineButton.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid drawing: button for a line type
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import android.content.Context;

import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Path;

import android.widget.Button;

class LineButton extends Button 
{
  // private String mText;
  private Paint mPaint = new Paint();
  private Paint mPathPaint;

  public LineButton(Context context, String text, Paint paint )
  {
    super(context);
    setBackgroundColor( Color.BLACK );
    setPadding(5, 5, 5, 5 );
    setMinimumWidth( 80 );
    setMinimumHeight( 30 );
    // mText = text; 
    mPathPaint = paint;
  }

  public void onDraw(Canvas canvas) 
  {
    // draw the button background
    // draw the text
    // mPaint.setColor( Color.WHITE );
    // canvas.drawText( mText, 5, 15, mPaint);
    Path path = new Path();
    path.moveTo( 5, 20 );
    path.lineTo( getWidth()-5, 20 );
    canvas.drawPath( path, mPathPaint );
  }
}


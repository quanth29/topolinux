/** @file HelpEntry
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief TopoDroid help dialog 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.android.DistoX;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

class HelpEntry
{
  Button   mButton   = null;
  TextView mTextView = null;
  LinearLayout mView;

  /**
   * @param context     display context
   * @param icon        button resource int
   * @param text        text resource int
   */
  HelpEntry( Context context, int icon, int text )
  {
    mButton   = new Button( context );
    mTextView = new TextView( context );

    mButton.setBackgroundResource( icon );
    mTextView.setText( text );

    LinearLayout ll = new LinearLayout( context );
    // ll.setOrientation( LinearLayout.HORIZONTAL );
    int lw = LinearLayout.LayoutParams.WRAP_CONTENT;
    int lh = LinearLayout.LayoutParams.WRAP_CONTENT;

    ll.addView( mButton,   new LinearLayout.LayoutParams(lh,lw) );
    ll.addView( mTextView, new LinearLayout.LayoutParams(lh,lw) );
    mView = ll;
  }

}


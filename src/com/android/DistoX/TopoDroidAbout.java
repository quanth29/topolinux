/** @file TopoDroidAbout.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid about dialog
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 * CHANGES
 * 20120521 created
 */
package com.android.DistoX;

import android.content.Context;
import android.app.Dialog;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

public class TopoDroidAbout extends Dialog
                            implements OnClickListener
{
  Button mBTok;

  TopoDroidAbout( Context context )
  {
    super( context );
    setContentView(R.layout.welcome);
    setTitle(R.string.welcome_title);

    mBTok = (Button)findViewById(R.id.OK);
    mBTok.setOnClickListener( this );
  }
  
  @Override
  public void onClick(View v) 
  {
    dismiss();
  }
}

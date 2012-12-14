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

public class TopoDroidAbout
{
  static public void show( Context context )
  {
    final Dialog dial = new Dialog( context );
    dial.setContentView(R.layout.welcome);
    dial.setTitle(R.string.welcome_title);

    Button _btOK = (Button)dial.findViewById(R.id.OK);
    _btOK.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          // dial.hide();
          dial.dismiss();
    } } );
    dial.show();
  }
}

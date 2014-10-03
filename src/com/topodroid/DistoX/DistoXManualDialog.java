/* @file DistoXManualDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey shot dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130307 made Annotations into a dialog
 * 201311   icon for the button OK
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;
// import android.content.Context;
// import android.content.Intent;

import android.widget.TextView;
// import android.widget.Button;
// import android.widget.SlidingDrawer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ArrayAdapter;


import android.view.View;
import android.view.View.OnClickListener;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.util.Log;

public class DistoXManualDialog extends Activity
                                implements OnItemClickListener, OnClickListener
{
  // private TextView mTVtitle;
  private TextView mTVtext;
  // private Button   mButtonOK;
  // private Button   mButtonCancel;
  // private String   mTitle;

  private void load( String filename )
  {
    mTVtext.setText(""); 
    try {
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      String line = br.readLine();
      while ( line != null ) {
        mTVtext.append( line + "\n" );
        line = br.readLine();
      }
      fr.close();
    } catch ( IOException e ) {
      TopoDroidApp.Log(  TopoDroidApp.LOG_ERR, "load IOexception " + e.toString() );
    }
  }

// -------------------------------------------------------------------
  // SlidingDrawer mDrawer;
  ImageView     mImage;
  ListView      mList;

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.distox_manual_dialog);
    // mTVtitle  = (TextView) findViewById(R.id.manual_title );
    mTVtext   = (TextView) findViewById(R.id.manual_text );
    // mButtonOK = (Button) findViewById(R.id.button_ok );
    // mButtonCancel = (Button) findViewById(R.id.button_cancel );

    // Bundle extras = getIntent().getExtras();
    // String title  = extras.getString( TopoDroidApp.TOPODROID_SURVEY );
    // mTVtitle.setText( mTitle );

    setTitle( R.string.title_manual );
    load( TopoDroidApp.getManFile( "manual00.txt" ) );

    // mButtonOK.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );


    // mDrawer = (SlidingDrawer) findViewById( R.id.drawer );
    // mDrawer.unlock();

    mImage  = (ImageView) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    mList = (ListView) findViewById( R.id.content );

    ArrayAdapter< String > adapter = new ArrayAdapter<String>(this, R.layout.message );
    adapter.add("Preface");        // manual00
    adapter.add("Introducion");
    adapter.add("Database");
    adapter.add("Auxiliary apps");
    adapter.add("TopoDroid");      // manual04
    adapter.add("Device");
    adapter.add("Calibration");
    adapter.add("Survey data");
    adapter.add("Shot list");
    adapter.add("Survey info");    // manual09
    adapter.add("Sketch");
    adapter.add("Drawing");
    adapter.add("Final map");
 
    mList.setAdapter( adapter );
    mList.setVisibility( View.GONE );
    mList.invalidate();
    mList.setOnItemClickListener( this );
  }


  @Override 
  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    ImageView b = (ImageView) v;
    if ( b == mImage ) {
      // Log.v("DistoX", "clicked image" );
      mList.setVisibility( View.VISIBLE );
      
    }
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    CharSequence item = ((TextView) view).getText();
    // Log.v("DistoX", "click " + item + " pos " + pos);
    mList.setVisibility( View.GONE );
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( "manual%02d.txt", pos );
    load( TopoDroidApp.getManFile( sw.getBuffer().toString() ) );
  }

}



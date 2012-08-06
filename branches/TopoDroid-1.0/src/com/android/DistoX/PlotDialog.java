/** @file PlotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid option list
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120520 created
 */
package com.android.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;

import android.content.Context;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.Toast;

public class PlotDialog extends Dialog
                        implements OnItemClickListener
{
  private Context mContext;
  private ShotActivity mParent;
  private TopoDroidApp app;
  private ArrayAdapter<String> mArrayAdapter;

  private ListView mList;

  public PlotDialog( Context context, ShotActivity parent, TopoDroidApp _app )
  {
    super( context );
    mContext = context;
    mParent = parent;
    app = _app;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mArrayAdapter = new ArrayAdapter<String>( mContext, R.layout.message );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    updateList();
  }

  private void updateList()
  {
    if ( app.mData != null && app.mSID >= 0 ) {
      List< PlotInfo > list = app.mData.selectAllPlots( app.mSID, TopoDroidApp.STATUS_NORMAL ); 
      setTitle( String.format( mContext.getResources().getString( R.string.title_scraps ),
                               app.getSurvey() ) );
      if ( list.size() == 0 ) {
        Toast.makeText( mContext, R.string.no_plots, Toast.LENGTH_LONG ).show();
        dismiss();
      }
      // mList.setAdapter( mArrayAdapter );
      mArrayAdapter.clear();
      // mArrayAdapter.add( getResources().getString(R.string.back_to_survey) );
      for ( PlotInfo item : list ) {
        StringWriter sw = new StringWriter();
        PrintWriter pw  = new PrintWriter(sw);
        pw.format("%d <%s> %s", item.id, item.name, item.getTypeString() );
        String result = sw.getBuffer().toString();
        mArrayAdapter.add( result );
        // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "Data " + result );
      }
      // mArrayAdapter.add("0 <new_plot> NONE");
    } else {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "null data or survey (" + app.mSID + ")" );
    }
  }
 
  // @Override
  public void onClick(View v) 
  {
    dismiss();
  }

  // ---------------------------------------------------------------
  // list items click

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    String[] st = value.split( " ", 3 );
    int from = value.indexOf('<');
    int to = value.lastIndexOf('>');
    String plot_name = value.substring( from+1, to );
    // int end = st[1].length() - 1;
    // String plot_name = st[1].substring( 1, end );
    mParent.startPlot( plot_name, st[2] );
    dismiss();
  }

}

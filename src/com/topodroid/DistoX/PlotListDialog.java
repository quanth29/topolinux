/** @file PlotListDialog.java
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
 * 20130215 3D sketches button
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;

import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;

import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.Toast;

public class PlotListDialog extends Dialog
                        implements OnItemClickListener
                                , View.OnClickListener
{
  private Context mContext;
  private ShotActivity mParent;
  private TopoDroidApp app;
  private ArrayAdapter<String> mArrayAdapter;
  private Button mBtnPlotNew;
  // private Button mBtnPlotNormal;

  // private Button mBtnPlotSection;
  // private Button mBtnPlotBack;
  // FIXME_SKETCH_3D
  // private Button mBtnSketch3dNew;

  int mMode; // 0 normal 1 sections

  private ListView mList;

  public PlotListDialog( Context context, ShotActivity parent, TopoDroidApp _app )
  {
    super( context );
    mContext = context;
    mParent = parent;
    app = _app;
    mMode = 0;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plot_list_dialog );
    mArrayAdapter = new ArrayAdapter<String>( mContext, R.layout.message );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mBtnPlotNew     = (Button) findViewById(R.id.plot_new);
    // mBtnPlotNormal  = (Button) findViewById(R.id.plot_normal);

    // mBtnPlotSection = (Button) findViewById(R.id.plot_section);
    // mBtnPlotBack = (Button) findViewById(R.id.plot_back);

    mBtnPlotNew.setOnClickListener( this );
    // mBtnPlotNormal.setOnClickListener( this );

    // mBtnPlotSection.setOnClickListener( this );
    // mBtnPlotBack.setOnClickListener( this );

    // FIXME_SKETCH_3D
    // mBtnSketch3dNew = (Button) findViewById(R.id.sketch3d_new);
    // if ( app.mSketches ) {
    //   mBtnSketch3dNew.setOnClickListener( this );
    // } else {
    //   mBtnSketch3dNew.setEnabled( false );
    //   // mBtnSketch3dNew.  <-- hide
    // }
    // // mBtnSketch3dNew.setEnabled( false );

    updateList();
  }

  private void updateList()
  {
    if ( app.mData != null && app.mSID >= 0 ) {
      setTitle( String.format( mContext.getResources().getString( R.string.title_scraps ), app.mySurvey ) );

      List< PlotInfo > list = app.mData.selectAllPlots( app.mSID, TopoDroidApp.STATUS_NORMAL ); 
      // FIXME_SKETCH_3D
      // List< Sketch3dInfo > slist = null;
      // if ( app.mSketches ) {
      //   slist = app.mData.selectAllSketches( app.mSID, TopoDroidApp.STATUS_NORMAL );
      // }
      // if ( list.size() == 0 && ( slist == null || slist.size() == 0 ) ) {
      if ( list.size() == 0 && mMode == 0 ) {
        Toast.makeText( mContext, R.string.no_plots, Toast.LENGTH_SHORT ).show();
        dismiss();
      }
      // mList.setAdapter( mArrayAdapter );
      mArrayAdapter.clear();
      // mArrayAdapter.add( getResources().getString(R.string.back_to_survey) );
      if ( mMode == 0 ) {
        for ( PlotInfo item : list ) {
          if ( item.type == PlotInfo.PLOT_PLAN /* || item.type == PlotInfo.PLOT_EXTENDED */ ) {
            StringWriter sw = new StringWriter();
            PrintWriter pw  = new PrintWriter(sw);

            String name = item.name.substring( 0, item.name.length() - 1 );

            pw.format("%d <%s> %s", item.id, name, item.getTypeString() );
            mArrayAdapter.add( sw.getBuffer().toString() );
            // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "Data " + result );
          }
        }
      } else if ( mMode == 1 ) {
        for ( PlotInfo item : list ) {
         if ( item.type == PlotInfo.PLOT_SECTION || item.type == PlotInfo.PLOT_H_SECTION 
              /* || item.type == PlotInfo.PLOT_V_SECTION */
             ) {
            StringWriter sw = new StringWriter();
            PrintWriter pw  = new PrintWriter(sw);
            pw.format("%d <%s> %s", item.id, item.name, item.getTypeString() );
            mArrayAdapter.add( sw.getBuffer().toString() );
            // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "Data " + result );
          }
        }
      }
      // FIXME_SKETCH_3D
      // if ( slist != null ) {
      //   for ( Sketch3dInfo sketch : slist ) {
      //     StringWriter sw = new StringWriter();
      //     PrintWriter pw  = new PrintWriter(sw);
      //     pw.format("%d <%s> Sketch 3D", sketch.id, sketch.name );
      //     mArrayAdapter.add( sw.getBuffer().toString() );
      //   }
      // }
    } else {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "null data or survey (" + app.mSID + ")" );
    }
  }
 
  // @Override
  public void onClick(View v) 
  {
    // TopoDroidApp.Log(  TopoDroidApp.LOG_INPUT, "PlotListDialog onClick() " );
    Button b = (Button) v;
    if ( b == mBtnPlotNew ) {
      hide();
      new PlotNewDialog( mParent, mParent ).show();

    // } else if ( b == mBtnPlotNormal ) {
    //   mMode = ( mMode + 1 ) % 2;
    //   updateList();
    //   return;

    // FIXME_SKETCH_3D
    // } else if ( app.mSketches && b == mBtnSketch3dNew ) {
    //   new Sketch3dNewDialog( mParent, mParent ).show();
    // } else if ( b == mBtnPlotBack ) {
    //   /* nothing */
    }
    dismiss();
  }

  // ---------------------------------------------------------------
  // list items click

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    // TopoDroidApp.Log(  TopoDroidApp.LOG_INPUT, "PlotListDialog onItemClick() " + value );

    // String[] st = value.split( " ", 3 );
    int from = value.indexOf('<');
    int to = value.lastIndexOf('>');
    String plot_name = value.substring( from+1, to );
    String plot_type = value.substring( to+2 );
    // int end = st[1].length() - 1;
    // String plot_name = st[1].substring( 1, end );
    mParent.startExistingPlot( plot_name, plot_type ); // context of current SID
    dismiss();
  }

}

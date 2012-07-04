/* @file DistoXStatDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid stats display dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120530 loop closures
 * 20120702 shot surface flag
 */
package com.android.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;

import android.graphics.*;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;

// import android.util.Log;

public class DistoXStatDialog extends Dialog 
                              // implements View.OnClickListener
{
    // private static final String TAG = "DistoX stats";

    private Context mContext;

    private TextView mTextLength;
    private TextView mTextZminmax;
    private TextView mTextStations;
    private TextView mTextShots;
    private TextView mTextSplays;
    private ListView mList;

    // private Button mBtnOk;
    private DistoXNum mNum;
    public DistoXStatDialog( Context context, DistoXNum num )
    {
      super(context);
      mContext = context;
      mNum = num;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.distox_stat_dialog);

        mTextLength   = (TextView) findViewById(R.id.text_stat_length);
        mTextZminmax  = (TextView) findViewById(R.id.text_stat_zminmax);
        mTextStations = (TextView) findViewById(R.id.text_stat_stations);
        mTextShots    = (TextView) findViewById(R.id.text_stat_shots);
        mTextSplays   = (TextView) findViewById(R.id.text_stat_splays);

        // mList.setOnItemClickListener( this );
        List< DistoXNum.Closure > cls = mNum.getClosures();
        if ( cls.size() == 0 ) {
          ((TextView)findViewById( R.id.text_stat_loops )).setText( R.string.loop_none );
        } else {
          mList = (ListView) findViewById(R.id.list);
          DistoXClosureAdapter adapter = 
            new DistoXClosureAdapter( mContext, R.layout.row, new ArrayList< DistoXNum.Closure >() );
          mList.setAdapter( adapter );
          for ( DistoXNum.Closure cl : cls ) {
            adapter.add( cl );
          }
        }

        // mBtnOk = (Button) findViewById(R.id.button_stat_ok);
        // mBtnOk.setOnClickListener( this );

        StringWriter sw1 = new StringWriter();
        PrintWriter pw1  = new PrintWriter( sw1 );
        pw1.format("Length %.2f ",  mNum.surveyLength() );
        mTextLength.setText( sw1.toString() );

        StringWriter sw2 = new StringWriter();;
        PrintWriter pw2  = new PrintWriter( sw2 );
        double zmin = mNum.surveyBottom();
        double zmax = mNum.surveyTop();
        if ( zmin < 0.0 ) {
          if ( zmax > 0.0 ) {
            pw2.format("Depth %.1f, +%.1f ", zmin, zmax );
          } else {
            pw2.format("Depth %.1f ", zmin );
          }
        } else {
          if ( zmax > 0.0 ) {
            pw2.format("Depth +%.1f ", zmax );
          } else {
            pw2.format("Depth 0.0 " );
          }
        }
        mTextZminmax.setText( sw2.toString() );

        StringWriter sw3 = new StringWriter();;
        PrintWriter pw3  = new PrintWriter( sw3 );
        pw3.format("Stations %3d ", mNum.stationsNr() );
        mTextStations.setText( sw3.toString() );

        StringWriter sw4 = new StringWriter();;
        PrintWriter pw4  = new PrintWriter( sw4 );
        pw4.format("Shots  %3d,   Duplicate %3d,   Surface %3d",
          mNum.shotsNr(), mNum.duplicateNr(), mNum.surfaceNr() );
        mTextShots.setText( sw4.toString() );

        StringWriter sw5 = new StringWriter();;
        PrintWriter pw5  = new PrintWriter( sw5 );
        pw5.format("Splays %3d ", mNum.splaysNr() );
        mTextSplays.setText( sw5.toString() );

        setTitle("Stats");
    }

    // @Override
    // public void onClick(View view)
    // {
    //   // Log.v( TAG, "onClick()" );
    //   dismiss();
    // }
}
        


/* @file CalibCoeffDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration coefficients display dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.android.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.Button;
import android.view.View;

public class CalibCoeffDialog extends Dialog
                              // implements View.OnClickListener
{
  private Context mContext;

  private TextView mTextBG;
  private TextView mTextAGx;
  private TextView mTextAGy;
  private TextView mTextAGz;
  private TextView mTextBM;
  private TextView mTextAMx;
  private TextView mTextAMy;
  private TextView mTextAMz;
  private TextView mTextDelta;
  private TextView mTextMaxError;
  private TextView mTextIter;
  // private Button   mButtonBack;

  private String bg0;
  private String agx;
  private String agy;
  private String agz;
  private String bm0;
  private String amx;
  private String amy;
  private String amz;
  private String delta0;
  private String error0;
  private String iter0;

  public CalibCoeffDialog( Context context,
                           Vector bg, Matrix ag, Vector bm, Matrix am, 
                           float delta, float error, long iter )
  {
    super( context );
    mContext = context;

    StringWriter sw1 = new StringWriter();
    PrintWriter  pw1 = new PrintWriter( sw1 );
    pw1.format("bG   %8.4f %8.4f %8.4f", bg.x, bg.y, bg.z );
    bg0 = sw1.getBuffer().toString();
    StringWriter sw2 = new StringWriter();
    PrintWriter  pw2 = new PrintWriter( sw2 );
    pw2.format("aGx  %8.4f %8.4f %8.4f", ag.x.x, ag.x.y, ag.x.z );
    agx = sw2.getBuffer().toString();
    StringWriter sw3 = new StringWriter();
    PrintWriter  pw3 = new PrintWriter( sw3 );
    pw3.format("aGy  %8.4f %8.4f %8.4f", ag.y.x, ag.y.y, ag.y.z );
    agy = sw3.getBuffer().toString();
    StringWriter sw4 = new StringWriter();
    PrintWriter  pw4 = new PrintWriter( sw4 );
    pw4.format("aGz  %8.4f %8.4f %8.4f", ag.z.x, ag.z.y, ag.z.z );
    agz = sw4.getBuffer().toString();

    StringWriter sw5 = new StringWriter();
    PrintWriter  pw5 = new PrintWriter( sw5 );
    pw5.format("bM   %8.4f %8.4f %8.4f", bm.x, bm.y, bm.z );
    bm0 = sw5.getBuffer().toString();
    StringWriter sw6 = new StringWriter();
    PrintWriter  pw6 = new PrintWriter( sw6 );
    pw6.format("aMx  %8.4f %8.4f %8.4f", am.x.x, am.x.y, am.x.z );
    amx = sw6.getBuffer().toString();
    StringWriter sw7 = new StringWriter();
    PrintWriter  pw7 = new PrintWriter( sw7 );
    pw7.format("aMy  %8.4f %8.4f %8.4f", am.y.x, am.y.y, am.y.z );
    amy = sw7.getBuffer().toString();
    StringWriter sw8 = new StringWriter();
    PrintWriter  pw8 = new PrintWriter( sw8 );
    pw8.format("aMz  %8.4f %8.4f %8.4f", am.z.x, am.z.y, am.z.z );
    amz = sw8.getBuffer().toString();

    StringWriter swD = new StringWriter();
    PrintWriter  pwD = new PrintWriter( swD );
    pwD.format("Error %.4f grad", delta );
    delta0 = swD.getBuffer().toString();
    StringWriter swM = new StringWriter();
    PrintWriter  pwM = new PrintWriter( swM );
    pwM.format("Max error %8.4f", error );
    error0 = swM.getBuffer().toString();
    StringWriter swI = new StringWriter();
    PrintWriter  pwI = new PrintWriter( swI );
    pwI.format("Iterations %d", iter );
    iter0 = swI.getBuffer().toString();
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setTitle( mContext.getResources().getString( R.string.title_coeff ) );

    setContentView(R.layout.calib_coeff_dialog);
    mTextBG  = (TextView) findViewById(R.id.coeff_bg);
    mTextAGx = (TextView) findViewById(R.id.coeff_agx);
    mTextAGy = (TextView) findViewById(R.id.coeff_agy);
    mTextAGz = (TextView) findViewById(R.id.coeff_agz);
    
    mTextBM  = (TextView) findViewById(R.id.coeff_bm);
    mTextAMx = (TextView) findViewById(R.id.coeff_amx);
    mTextAMy = (TextView) findViewById(R.id.coeff_amy);
    mTextAMz = (TextView) findViewById(R.id.coeff_amz);
    mTextDelta    = (TextView) findViewById(R.id.coeff_delta);
    mTextMaxError = (TextView) findViewById(R.id.coeff_max_error);
    mTextIter     = (TextView) findViewById(R.id.coeff_iter);

    mTextBG.setText( bg0 );
    mTextAGx.setText( agx );
    mTextAGy.setText( agy );
    mTextAGz.setText( agz );
    mTextBM.setText( bm0 );
    mTextAMx.setText( amx );
    mTextAMy.setText( amy );
    mTextAMz.setText( amz );
    mTextDelta.setText( delta0 );
    mTextMaxError.setText( error0 );
    mTextIter.setText( iter0 );

    // mButtonBack = (Button) findViewById( R.id.button_coeff_back );
    // mButtonBack.setOnClickListener( this );
  }

  // public void onClick(View v) 
  // {
  //   dismiss();
  // }

}


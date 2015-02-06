/* @file Calibration.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration algorithm
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 */
package com.android.DistoX;

import java.lang.Math;
import java.io.StringWriter;
import java.io.PrintWriter;

// import android.util.Log;

public class Calibration
{
  // private static final String TAG = "DistoX Calibration";
  private TopoDroidApp app;

  private Matrix aG = null;
  private Matrix aM = null;
  private Vector bG = null;
  private Vector bM = null;

  private Vector[] g = null;
  private Vector[] m = null;
  private long[] group = null;
  private float[] err = null;

  private int idx;
  private int num;

  private Vector gxp; // opt vectors
  private Vector mxp;
  private Vector gxt; // turn vectors
  private Vector mxt;
  float b0=0.0f, c0=0.0f; // bearing and clino

  private float mDelta;

  // ==============================================================

  public float getDelta() { return mDelta; }

  public Calibration( int N, TopoDroidApp app0 )
  {
    num = 0;
    if ( N > 0 ) Reset( N );
    this.app = app0;
  }

  public float Delta() { return mDelta; }
  public float Error( int k ) { return err[k]; }
  public float[] Errors() { return err; }

  public Matrix GetAG() { return aG; }
  public Matrix GetAM() { return aM; }
  public Vector GetBG() { return bG; }
  public Vector GetBM() { return bM; }

  public byte[] GetCoeff()
  {
    if ( aG == null ) return null;
    byte[] coeff = new byte[48];
    long v;
    v  = (long)(bG.x * TopoDroidApp.FV); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[ 0] = (byte)(v & 0xff);
    coeff[ 1] = (byte)((v>>8) & 0xff);
    v = (long)(aG.x.x * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[ 2] = (byte)(v & 0xff);
    coeff[ 3] = (byte)((v>>8) & 0xff);
    v = (long)(aG.x.y * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[ 4] = (byte)(v & 0xff);
    coeff[ 5] = (byte)((v>>8) & 0xff);
    v = (long)(aG.x.z * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[ 6] = (byte)(v & 0xff);
    coeff[ 7] = (byte)((v>>8) & 0xff);

    v = (long)(bG.y * TopoDroidApp.FV); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[ 8] = (byte)(v & 0xff);
    coeff[ 9] = (byte)((v>>8) & 0xff);
    v = (long)(aG.y.x * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[10] = (byte)(v & 0xff);
    coeff[11] = (byte)((v>>8) & 0xff);
    v = (long)(aG.y.y * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[12] = (byte)(v & 0xff);
    coeff[13] = (byte)((v>>8) & 0xff);
    v = (long)(aG.y.z * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[14] = (byte)(v & 0xff);
    coeff[15] = (byte)((v>>8) & 0xff);

    v = (long)(bG.z * TopoDroidApp.FV); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[16] = (byte)(v & 0xff);
    coeff[17] = (byte)((v>>8) & 0xff);
    v = (long)(aG.z.x * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[18] = (byte)(v & 0xff);
    coeff[19] = (byte)((v>>8) & 0xff);
    v = (long)(aG.z.y * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[20] = (byte)(v & 0xff);
    coeff[21] = (byte)((v>>8) & 0xff);
    v = (long)(aG.z.z * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[22] = (byte)(v & 0xff);
    coeff[23] = (byte)((v>>8) & 0xff);
    
    v = (long)(bM.x * TopoDroidApp.FV); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[24] = (byte)(v & 0xff);
    coeff[25] = (byte)((v>>8) & 0xff);
    v = (long)(aM.x.x * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[26] = (byte)(v & 0xff);
    coeff[27] = (byte)((v>>8) & 0xff);
    v = (long)(aM.x.y * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[28] = (byte)(v & 0xff);
    coeff[29] = (byte)((v>>8) & 0xff);
    v = (long)(aM.x.z * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[30] = (byte)(v & 0xff);
    coeff[31] = (byte)((v>>8) & 0xff);

    v = (long)(bM.y * TopoDroidApp.FV); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[32] = (byte)(v & 0xff);
    coeff[33] = (byte)((v>>8) & 0xff);
    v = (long)(aM.y.x * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[34] = (byte)(v & 0xff);
    coeff[35] = (byte)((v>>8) & 0xff);
    v = (long)(aM.y.y * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[36] = (byte)(v & 0xff);
    coeff[37] = (byte)((v>>8) & 0xff);
    v = (long)(aM.y.z * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[38] = (byte)(v & 0xff);
    coeff[39] = (byte)((v>>8) & 0xff);

    v = (long)(bM.z * TopoDroidApp.FV); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[40] = (byte)(v & 0xff);
    coeff[41] = (byte)((v>>8) & 0xff);
    v = (long)(aM.z.x * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[42] = (byte)(v & 0xff);
    coeff[43] = (byte)((v>>8) & 0xff);
    v = (long)(aM.z.y * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[44] = (byte)(v & 0xff);
    coeff[45] = (byte)((v>>8) & 0xff);
    v = (long)(aM.z.z * TopoDroidApp.FM); if ( v > TopoDroidApp.ZERO ) v = TopoDroidApp.NEG - v;
    coeff[46] = (byte)(v & 0xff);
    coeff[47] = (byte)((v>>8) & 0xff);

    return coeff;
  }

  private static void coeffToBA( byte[] coeff, Vector b, Matrix a, int off )
  {
    long v;
    long c0 = (int)(coeff[off+ 0]); if ( c0 < 0 ) c0 = 256+c0;
    long c1 = (int)(coeff[off+ 1]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidApp.ZERO ) v = v - TopoDroidApp.NEG;
    b.x = v / TopoDroidApp.FV;
    c0 = (int)(coeff[off+ 2]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+ 3]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidApp.ZERO ) v = v - TopoDroidApp.NEG;
    a.x.x = v / TopoDroidApp.FM;
    c0 = (int)(coeff[off+ 4]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+ 5]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidApp.ZERO ) v = v - TopoDroidApp.NEG;
    a.x.y = v / TopoDroidApp.FM;
    c0 = (int)(coeff[off+ 6]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+ 7]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidApp.ZERO ) v = v - TopoDroidApp.NEG;
    a.x.z = v / TopoDroidApp.FM;

    // BY
    c0 = (int)(coeff[off+ 8]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+ 9]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidApp.ZERO ) v = v - TopoDroidApp.NEG;
    b.y = v / TopoDroidApp.FV;

    c0 = (int)(coeff[off+10]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+11]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidApp.ZERO ) v = v - TopoDroidApp.NEG;
    a.y.x = v / TopoDroidApp.FM;

    c0 = (int)(coeff[off+12]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+13]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidApp.ZERO ) v = v - TopoDroidApp.NEG;
    a.y.y = v / TopoDroidApp.FM;

    c0 = (int)(coeff[off+14]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+15]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidApp.ZERO ) v = v - TopoDroidApp.NEG;
    a.y.z = v / TopoDroidApp.FM;

    // BZ
    c0 = (int)(coeff[off+16]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+17]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidApp.ZERO ) v = v - TopoDroidApp.NEG;
    b.z = v / TopoDroidApp.FV;
    c0 = (int)(coeff[off+18]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+19]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidApp.ZERO ) v = v - TopoDroidApp.NEG;
    a.z.x = v / TopoDroidApp.FM;
    c0 = (int)(coeff[off+20]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+21]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidApp.ZERO ) v = v - TopoDroidApp.NEG;
    a.z.y = v / TopoDroidApp.FM;
    c0 = (int)(coeff[off+22]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+23]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidApp.ZERO ) v = v - TopoDroidApp.NEG;
    a.z.z = v / TopoDroidApp.FM;
  }

  public static void coeffToG( byte[] coeff, Vector b, Matrix a )
  {
    coeffToBA( coeff, b, a, 0 );
  }

  public static void coeffToM( byte[] coeff, Vector b, Matrix a )
  {
    coeffToBA( coeff, b, a, 24 );
  }

  public void AddValues( CalibCBlock b )
  {
    // add also group-0 CBlocks to keep CBlock list and calib vectors aligned
    AddValues( b.gx, b.gy, b.gz, b.mx, b.my, b.mz, b.mGroup );
  }

  public void AddValues( long gx, long gy, long gz, long mx, long my, long mz, long group0 )
  {
    if ( idx >= num ) {
      return;
    }
    g[idx] = new Vector( gx/TopoDroidApp.FV, gy/TopoDroidApp.FV, gz/TopoDroidApp.FV );
    m[idx] = new Vector( mx/TopoDroidApp.FV, my/TopoDroidApp.FV, mz/TopoDroidApp.FV );
    group[idx] = group0;
    // StringWriter sw = new StringWriter();
    // PrintWriter  pw = new PrintWriter( sw );
    // pw.format("Add %d G %d %d %d M %d %d %d Grp %d", idx, gx, gy, gz, mx, my, mz, group0 );
    // Log.v( TAG, sw.getBuffer().toString() );
    idx ++;
  }

  public int Size() { return idx; }

  public int Calibrate()
  {
    mDelta = 0.0f;
    // Log.v( TAG, "Calibrate idx " + idx);
    if ( idx < 16 ) return -1;
    return Optimize( idx, g, m );
  }

  public void Reset( int N )
  {
    if ( N != num ) {
      num = N;
      g = new Vector[N];
      m = new Vector[N];
      group = new long[N];
      err   = new float[N];
    }
    idx = 0;
    aG = null;
    bG = null;
    aM = null;
    bM = null;
  }
    
  // ------------------------------------------------------------
  // private methods

  private void InitializeAB()
  {
    aG = new Matrix( Matrix.one );
    aM = new Matrix( Matrix.one );
    bG = new Vector();
    bM = new Vector();
  }

  private void OptVectors( Vector gr, Vector mr, float s, float c )
  {
    Vector no = gr.cross( mr );
    no.Normalized();
    gxp = ( (mr.mult(c)).plus( (mr.cross(no)).mult(s) ) ).plus(gr);
    gxp.Normalized();
    mxp =   (gxp.mult(c)).plus( (no.cross(gxp)).mult(s) );
  }

  private void TurnVectors( Vector gf, Vector mf, Vector gr, Vector mr,
                            boolean print )
  {
    float s1 = gr.z * gf.y - gr.y * gf.z + mr.z * mf.y - mr.y * mf.z;
    float c1 = gr.y * gf.y + gr.z * gf.z + mr.y * mf.y + mr.z * mf.z;
    float d1 = (float)Math.sqrt( c1*c1 + s1*s1 );
    s1 /= d1;
    c1 /= d1;
    gxt = gf.TurnX( s1, c1 );
    mxt = mf.TurnX( s1, c1 );
    // if ( print ) {
    //   LogSC( "TurnVectors", s1, c1 );
    //   LogVectors( "TurnVectors", -1, gf, gxt );
    //   LogVectors( "TurnVectors", -1, mf, mxt );
    // }
  }

/* ============================================================

  private void LogNumber( String msg, int it )
  {
    Log.v( TAG, msg + " " + it );
  }

  private void LogMatrixVector( String msg, Matrix m1, Vector v1 ) 
  {
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format(" M: %8.4f %8.4f %8.4f V: %8.4f\n    %8.4f %8.4f %8.4f   %8.4f\n    %8.4f %8.4f %8.4f   %8.4f",
       m1.x.x, m1.x.y, m1.x.z, v1.x, 
       m1.y.x, m1.y.y, m1.y.z, v1.y, 
       m1.z.x, m1.z.y, m1.z.z, v1.z );
    Log.v( TAG, msg + sw.getBuffer().toString() );
  }

  private void LogVectors( String msg, long group, Vector v1, Vector v2 )
  {
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format(" %3d V1 %8.4f %8.4f %8.4f\n    V2 %8.4f %8.4f %8.4f", group, v1.x, v1.y, v1.z, v2.x, v2.y, v2.z ); 
    Log.v( TAG, msg + sw.getBuffer().toString() );
  }

  private void LogSC( String msg, float s, float c )
  {
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format(" S %8.4f C %8.4f", s, c ); 
    Log.v( TAG, msg + sw.getBuffer().toString() );
  }

============================================================ */

  private void computeBearingAndClinoRad( Vector g0, Vector m0 )
  {
    Vector g = g0.mult( 1.0f / TopoDroidApp.FV );
    Vector m = m0.mult( 1.0f / TopoDroidApp.FV );
    g.Normalized();
    m.Normalized();
    Vector e = new Vector( 1.0f, 0.0f, 0.0f );
    Vector y = m.cross( g );
    Vector x = g.cross( y );
    y.Normalized();
    x.Normalized();
    float ex = e.dot( x );
    float ey = e.dot( y );
    float ez = e.dot( g );
    b0 = (float)Math.atan2( -ey, ex );
    c0   = - (float)Math.atan2( ez, (float)Math.sqrt(ex*ex+ey*ey) );
    // r0    = (float)Math.atan2( g.y, g.z );
    if ( b0 < 0.0f ) b0 += 2*TopoDroidApp.M_PI;
    // if ( r0 < 0.0f ) r0 += 2*TopoDroidApp.M_PI;
  }

  private int Optimize( int nn, Vector[] g, Vector [] m )
  {
    int max_it = app.mCalibMaxIt;
    float eps  = app.mCalibEps;

    // int num = g.Length();
    Vector[] gr = new Vector[nn];
    Vector[] mr = new Vector[nn];
    Vector[] gx = new Vector[nn];
    Vector[] mx = new Vector[nn];

    Matrix aG0;
    Matrix aM0;

    Vector sumG = new Vector();
    Vector sumM = new Vector();
    Matrix sumG2 = new Matrix();
    Matrix sumM2 = new Matrix();

    float sa = 0.0f;
    float ca = 0.0f;
    float invNum = 0.0f;
    for (int i=0; i<nn; ++i ) {
      if ( group[i] != 0 ) {
        invNum += 1.0f;
        sa += ( g[i].cross( m[i] )).Length();
        ca += g[i].dot( m[i] );
        sumG.add( g[i] );
        sumM.add( m[i] );
        sumG2.add( new Matrix(g[i],g[i]) );
        sumM2.add( new Matrix(m[i],m[i]) );
      }
    }
    invNum = 1.0f / invNum;
    Vector avG = sumG.mult( invNum );
    Vector avM = sumM.mult( invNum );
    Matrix invG = (sumG2.minus( new Matrix(sumG, avG) ) ).Inverse();
    Matrix invM = (sumM2.minus( new Matrix(sumM, avM) ) ).Inverse();

    // LogNumber( "Number", nn );
    // LogMatrixVector( "invG", invG, avG );
    // LogMatrixVector( "invM", invM, avM ); // this is OK

    InitializeAB();
    // LogAB( 0, aG, bG, aM, bM ); // this is OK

    int it = 0;
    float da = (float)Math.sqrt( ca*ca + sa*sa );
    float s = sa / da;
    float c = ca / da;
    // LogSC( "sin/cos", s, c ); // this is OK


    do {
      for ( int i=0; i<nn; ++i ) {
        if ( group[i] > 0 ) {
          gr[i] = bG.plus( aG.times(g[i]) );
          mr[i] = bM.plus( aM.times(m[i]) );
          // if ( i < 4 ) {
          //  LogVectors( "GR", i, gr[i], mr[i] ); // this is OK at the first iteration
          // }
        }
      }
      sa = 0.0f;
      ca = 0.0f;
      long group0 = -1;
      for ( int i=0; i<nn; ) {
        if ( group[i] == 0 ) {
          ++i;
        } else if ( group[i] != group0 ) {
          group0 = group[i];
          Vector grp = new Vector();
          Vector mrp = new Vector();
          int first = i;
          while ( i < nn && (group[i] == 0 || group[i] == group0) ) {
            // group must be positive integer
            // group == 0 means to skip
            //
            if ( group[i] != 0 ) {
              TurnVectors( gr[i], mr[i], gr[first], mr[first], i<4 );
              // if ( i < 4 ) {
              //   LogVectors( "GR ", i, gr[i], mr[i] );
              //   LogVectors( "GXT", i, gxt,   mxt );
              // }
              grp.add( gxt );
              mrp.add( mxt );
            }
            ++ i;
          }
          OptVectors( grp, mrp, s, c );
          // if ( group0 < 2 ) {
          //   LogVectors( "grp", group0, grp, mrp );
          //   LogVectors( "gxp", group0, gxp, mxp );
          // }

          sa += (mrp.cross(gxp)).Length();
          ca += mrp.dot(gxp);
          for (int j = first; j < i; ++j ) {
            if ( group[j] != 0 ) {
              TurnVectors( gxp, mxp, gr[j], mr[j], j<4 );
              gx[j] = new Vector( gxt );
              mx[j] = new Vector( mxt );
              // if ( group0 == 1 ) {
              //   LogVectors( "gx", group0, gx[j], mx[j] );
              // }
            }
          }
        }
      }
      da = (float)Math.sqrt( ca*ca + sa*sa );
      s = sa / da;
      c = ca / da;
      // LogSC( "sin/cos", s, c );
      Vector avGx = new Vector();
      Vector avMx = new Vector();
      Matrix sumGxG = new Matrix();
      Matrix sumMxM = new Matrix();
      for (int i=0; i<nn; ++i ) {
        if ( group[i] != 0 ) {
          avGx.add( gx[i] );
          avMx.add( mx[i] );
          sumGxG.add( new Matrix( gx[i], g[i] ) );
          sumMxM.add( new Matrix( mx[i], m[i] ) );
        } 
      }
      aG0 = new Matrix( aG );
      aM0 = new Matrix( aM );
      avGx = avGx.mult( invNum );
      avMx = avMx.mult( invNum );
      // LogMatrixVector( "avGx", sumGxG, avGx );
      // LogMatrixVector( "avMx", sumMxM, avMx );
      aG = (sumGxG.minus( new Matrix(avGx, sumG) )).times( invG );
      aM = (sumMxM.minus( new Matrix(avMx, sumM) )).times( invM );
      aG.z.y = (aG.y.z + aG.z.y) / 2.0f;
      aG.y.z = aG.z.y;
      bG = avGx.minus( aG.times(avG) );
      bM = avMx.minus( aM.times(avM) );
      // LogMatrixVector( "aG", aG, bG );
      // LogMatrixVector( "aM", aM, bM );
      float gmax = aG.MaxDiff(aG0);
      float mmax = aM.MaxDiff(aM0);
      ++ it;
    } while ( it < max_it && ( aG.MaxDiff(aG0) > eps || aM.MaxDiff(aM0) > eps ) );

    // LogMatrixVector( "aG", aG, bG );
    // LogMatrixVector( "aM", aM, bM );

    mDelta = 0.0f;
    for ( int i=0; i<nn; ++i ) {
      gr[i] = bG.plus( aG.times(g[i]) );
      mr[i] = bM.plus( aM.times(m[i]) );
    }
    long group0 = -1;
    long cnt = 0;
    for ( int i=0; i<nn; ) {
      if ( group[i] == 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        Vector grp = new Vector();
        Vector mrp = new Vector();
        int first = i;
        while ( i < nn && (group[i] == 0 || group[i] == group0) ) {
          if ( group[i] != 0 ) {
            TurnVectors( gr[i], mr[i], gr[first], mr[first], i<4 );
            grp.add( gxt );
            mrp.add( mxt );
          }
          ++ i;
        }
        OptVectors( grp, mrp, s, c );
        computeBearingAndClinoRad( gxp, mxp );
        Vector v0 = new Vector( (float)Math.cos(c0) * (float)Math.cos(b0),
                                (float)Math.cos(c0) * (float)Math.sin(b0),
                                (float)Math.sin(c0) );
        for (int j=first; j<i; ++j ) {
          if ( group[j] == 0 ) {
            err[j] = 0.0f;
          } else {
            computeBearingAndClinoRad( gr[j], mr[j] );
            Vector v = new Vector( (float)Math.cos(c0) * (float)Math.cos(b0),
                                   (float)Math.cos(c0) * (float)Math.sin(b0),
                                   (float)Math.sin(c0) );
            err[j] = v0.minus(v).Length(); // approx angle with sin/tan
            mDelta += err[j];
            ++ cnt;
          }
        }
      }
    }
    mDelta = mDelta * TopoDroidApp.RAD2GRAD_FACTOR / cnt;

    // for (int i=0; i<nn; ++i ) {
    //   if ( group[i] != 0 ) {
    //     Vector dg = gx[i].minus( gr[i] );
    //     Vector dm = mx[i].minus( mr[i] );
    //     err[i] = dg.dot(dg) + dm.dot(dm);
    //     mDelta += err[i];
    //     err[i] = (float)Math.sqrt( err[i] );
    //   } else {
    //     err[i] = 0.0f;
    //   }
    // }
    // mDelta = 100 * (float)Math.sqrt( mDelta/invNum );
    return it;
  }
        
}
  

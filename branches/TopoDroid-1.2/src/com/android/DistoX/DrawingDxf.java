/** @file DrawingDxf.java
 */
package com.android.DistoX;


import java.util.Locale;

import java.util.ArrayList;
import java.util.HashMap;

// import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import android.util.FloatMath;
import android.util.Log;

class DrawingDxf
{
  private static final float grad2rad = TopoDroidApp.GRAD2RAD_FACTOR;

  static void write( BufferedWriter out, DistoXNum num, DrawingCommandManager plot, int type )
  {
    float xmin=10000f, xmax=-10000f, 
          ymin=10000f, ymax=-10000f;
    for ( DrawingPath p : plot.mCurrentStack ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
        DrawingLinePath lp = (DrawingLinePath)p;
        if ( lp.lineType() == DrawingBrushPaths.mLineLib.mLineWallIndex ) {
          ArrayList< LinePoint > pts = lp.points;
          for ( LinePoint pt : pts ) {
            if ( pt.mX < xmin ) xmin = pt.mX;
            if ( pt.mX > xmax ) xmax = pt.mX;
            if ( pt.mY < ymin ) ymin = pt.mY;
            if ( pt.mY > ymax ) ymax = pt.mY;
          }
        }
      } else if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
        DrawingPointPath pp = (DrawingPointPath)p;
        if ( pp.mXpos < xmin ) xmin = pp.mXpos;
        if ( pp.mXpos > xmax ) xmax = pp.mXpos;
        if ( pp.mYpos < ymin ) ymin = pp.mYpos;
        if ( pp.mYpos > ymax ) ymax = pp.mYpos;
      } else if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) {
        DrawingStationPath st = (DrawingStationPath)p;
        if ( st.mXpos < xmin ) xmin = st.mXpos;
        if ( st.mXpos > xmax ) xmax = st.mXpos;
        if ( st.mYpos < ymin ) ymin = st.mYpos;
        if ( st.mYpos > ymax ) ymax = st.mYpos;
      }
    }

    try {
      // header
      out.write("999\nDXF created from TopoDroid\n");
      out.write("0\nSECTION\n2\nHEADER\n");

      xmin -= 2f;
      ymax += 2f;

        out.write("9\n$ACADVER\n1\nAC1006\n9\n$INSBASE\n");
      {
        StringWriter sw1 = new StringWriter();
        PrintWriter pw1  = new PrintWriter(sw1);
        pw1.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", 0.0, 0.0, 0.0 ); // FIXME (0,0,0)
        pw1.printf("9\n$EXTMIN\n");
        pw1.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", xmin, -ymax, 0.0 );
        pw1.printf("9\n$EXTMAX\n");
        pw1.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", xmax, -ymin, 0.0 );
        out.write( sw1.getBuffer().toString() );
      }
      out.write("0\nENDSEC\n");
      
      out.write("0\nSECTION\n2\nTABLES\n");
      {
        out.write("0\nTABLE\n2\nLTYPE\n70\n1\n");

        // int flag = 64;
        out.write("0\nLTYPE\n2\nCONTINUOUS\n70\n64\n3\nSolid line\n72\n65\n73\n0\n40\n0.0\n0\nENDTAB\n");
        
        out.write("0\nTABLE\n2\nLAYER\n70\n6\n");
        {
          StringWriter sw2 = new StringWriter();
          PrintWriter pw2  = new PrintWriter(sw2);

          // 2 layer name, 70 flag (64), 62 color code, 6 line style
          String style = "CONTINUOUS";
          int flag = 64;
          pw2.printf("0\nLAYER\n2\nLEG\n70\n%d\n62\n%d\n6\n%s\n",     flag, 1, style );
          pw2.printf("0\nLAYER\n2\nSPLAY\n70\n%d\n62\n%d\n6\n%s\n",   flag, 2, style );
          pw2.printf("0\nLAYER\n2\nSTATION\n70\n%d\n62\n%d\n6\n%s\n", flag, 3, style );
          pw2.printf("0\nLAYER\n2\nLINE\n70\n%d\n62\n%d\n6\n%s\n",    flag, 4, style );
          pw2.printf("0\nLAYER\n2\nPOINT\n70\n%d\n62\n%d\n6\n%s\n",   flag, 5, style );
          pw2.printf("0\nLAYER\n2\nAREA\n70\n%d\n62\n%d\n6\n%s\n",    flag, 6, style );
          pw2.printf("0\nLAYER\n2\nREF\n70\n%d\n62\n%d\n6\n%s\n",     flag, 7, style );
          out.write( sw2.getBuffer().toString() );
        }
        out.write("0\nENDTAB\n");
        
        out.write("0\nTABLE\n2\nSTYLE\n70\n0\n");
        out.write("0\nENDTAB\n");
      }
      out.write("0\nENDSEC\n");
      out.flush();
      
      out.write("0\nSECTION\n2\nBLOCKS\n");
      {
        // // 8 layer (0), 2 block name,
        for ( int n = 0; n < DrawingBrushPaths.mPointLib.size(); ++ n ) {
          StringWriter sw3 = new StringWriter();
          PrintWriter pw3  = new PrintWriter(sw3);
          int block = 1+n; // block_name = 1 + therion_code
          pw3.printf("0\nBLOCK\n8\nPOINT\n2\n%d\n70\n64\n10\n0.0\n20\n0.0\n30\n0.0\n", block );
          out.write( sw3.getBuffer().toString() );
          out.write( DrawingBrushPaths.mPointLib.getPoint(n).getDxf() );
          out.write("0\nENDBLK\n");
        }
      }
      out.write("0\nENDSEC\n");
      out.flush();
      
      out.write("0\nSECTION\n2\nENTITIES\n");
      {
        float CENTER_X = 100f;
        float CENTER_Y = 120f;
        float SCALE_FIX = DrawingActivity.SCALE_FIX;

        // reference
        StringWriter sw9 = new StringWriter();
        PrintWriter pw9  = new PrintWriter(sw9);
        pw9.printf("0\nLINE\n8\nREF\n");
        pw9.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", xmin, -ymax, 0.0f );
        pw9.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", xmin+10*SCALE_FIX, -ymax, 0.0f );
        pw9.printf("0\nLINE\n8\nREF\n");
        pw9.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", xmin, -ymax, 0.0f );
        pw9.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", xmin, -ymax+10*SCALE_FIX, 0.0f );
        pw9.printf("0\nTEXT\n8\nREF\n");
        pw9.printf("1\n%s\n", "10" );
        pw9.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", xmin+10*SCALE_FIX+1, -ymax, 0.0f );
        pw9.printf("0\nTEXT\n8\nREF\n");
        pw9.printf("1\n%s\n", "10" );
        pw9.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", xmin, -ymax+10*SCALE_FIX+1, 0.0f );
        out.write( sw9.getBuffer().toString() );
        out.flush();

        // centerline data
        for ( DrawingPath sh : plot.mFixedStack ) {
          DistoXDBlock blk = sh.mBlock;
          if ( blk == null ) continue;

          StringWriter sw4 = new StringWriter();
          PrintWriter pw4  = new PrintWriter(sw4);
          if ( sh.mType == DrawingPath.DRAWING_PATH_FIXED ) {
            NumStation f = num.getStation( blk.mFrom );
            NumStation t = num.getStation( blk.mTo );
            pw4.printf("0\nLINE\n8\nLEG\n");
            if ( type == TopoDroidApp.PLOT_PLAN ) {
              float x = CENTER_X + f.e*SCALE_FIX;
              float y = CENTER_Y + f.s*SCALE_FIX;
              float x1 = CENTER_X + t.e*SCALE_FIX;
              float y1 = CENTER_Y + t.s*SCALE_FIX;
              pw4.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", x, -y, 0.0f );
              pw4.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", x1, -y1, 0.0f );
            } else if ( type == TopoDroidApp.PLOT_EXTENDED ) {
              float x = CENTER_X + f.h*SCALE_FIX;
              float y = CENTER_Y + f.v*SCALE_FIX;
              float x1 = CENTER_X + t.h*SCALE_FIX;
              float y1 = CENTER_Y + t.v*SCALE_FIX;
              pw4.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", x, -y, 0.0f );
              pw4.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", x1, -y1, 0.0f );
            }
          } else if ( sh.mType == DrawingPath.DRAWING_PATH_SPLAY ) {
            NumStation f = num.getStation( blk.mFrom );
            pw4.printf("0\nLINE\n8\nSPLAY\n");
            float dh = blk.mLength * FloatMath.cos( blk.mClino * grad2rad )*SCALE_FIX;
            if ( type == TopoDroidApp.PLOT_PLAN ) {
              float x = CENTER_X + f.e*SCALE_FIX;
              float y = CENTER_Y + f.s*SCALE_FIX;
              float de =   dh * FloatMath.sin( blk.mBearing * grad2rad);
              float ds = - dh * FloatMath.cos( blk.mBearing * grad2rad);
              pw4.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", x, -y, 0.0f );
              pw4.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", x + de, (-y+ds), 0.0f );
            } else if ( type == TopoDroidApp.PLOT_EXTENDED ) {
              float x = CENTER_X + f.h*SCALE_FIX;
              float y = CENTER_Y + f.v*SCALE_FIX;
              float dv = blk.mLength * FloatMath.sin( blk.mClino * grad2rad )*SCALE_FIX;
              pw4.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", x, -y, 0.0f );
              pw4.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", x+dh*blk.mExtend, -(y+dv), 0.0f );
            }
          }
          out.write( sw4.getBuffer().toString() );
          out.flush();
        }

        // FIXME station scale is 0.3
        float POINT_SCALE = 10.0f;
        for ( DrawingPath path : plot.mCurrentStack ) {
          StringWriter sw5 = new StringWriter();
          PrintWriter pw5  = new PrintWriter(sw5);
          if ( path.mType == DrawingPath.DRAWING_PATH_STATION ) {
            DrawingStationPath st = (DrawingStationPath)path;
            pw5.printf("0\nTEXT\n8\nSTATION\n");
            pw5.printf("1\n%s\n", st.mName );
            pw5.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n%.1f\n", st.mXpos, -st.mYpos, 0.0, POINT_SCALE );
          } else if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
            String layer = "LINE";
            int flag = 0;
            DrawingLinePath line = (DrawingLinePath) path;
            ArrayList< LinePoint > points = line.points;
            pw5.printf("0\nPOLYLINE\n8\n%s\n70\n%d\n", layer, flag );
            for ( LinePoint p : points ) {
              pw5.printf("0\nVERTEX\n8\n%s\n", layer );
              pw5.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", p.mX, -p.mY, 0.0 );
            }
            pw5.printf("0\nSEQEND\n");
          } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath area = (DrawingAreaPath) path;
            ArrayList< LinePoint > points = area.points;
            pw5.printf("0\nHATCH\n8\nAREA\n91\n1\n" );
            pw5.printf("93\n%d\n", points.size() );
            for ( LinePoint p : points ) {
              pw5.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", p.mX, -p.mY, 0.0 );
            }
          } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
            // FIXME point scale factor is 0.3
            DrawingPointPath point = (DrawingPointPath) path;
            int idx = 1 + point.mPointType;
            pw5.printf("0\nINSERT\n8\nPOINT\n2\n%d\n41\n%.1f\n42\n%.1f\n", idx, POINT_SCALE, POINT_SCALE );
            pw5.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", point.mXpos, -point.mYpos, 0.0 );
          }
          out.write( sw5.getBuffer().toString() );
          out.flush();
        }
      }
      out.write("0\nENDSEC\n");
      out.write("0\nEOF\n");
      out.flush();
    } catch ( IOException e ) {
      // FIXME
      Log.v("DistoX", "DXF io-exception " + e.toString() );
    }
  }

}


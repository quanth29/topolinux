
/** @file PocketTopoParser.java
 *
 * @author marco corvi
 * @date nov 2014
 *
 * @brief TopoDroid PocketTopo parser
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Pattern;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;


import android.util.Log;

public class PocketTopoParser
{
  String mName = null;  // survey name
  String mDate = null;  // survey date
  String mTeam = "";
  String mTitle = "";
  float  mDeclination = 0.0f; // one-survey declination
  String mComment;
  private boolean mApplyDeclination = false;
  String mOutline;
  String mSideview;

  private ArrayList< ParserShot > shots;   // centerline shots

  public int getShotNumber()    { return shots.size(); }

  public ArrayList< ParserShot > getShots() { return shots; }

  
  static final int PT_SCALE = 100;

  public PocketTopoParser( String filename, String surveyname, boolean apply_declination ) throws ParserException
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PTOPO, "PocketTopo parser " + surveyname );
    shots  = new ArrayList< ParserShot >();
    mApplyDeclination = apply_declination;
    mOutline  = null;
    mSideview = null;
    mName     = surveyname.replace(".top", "");
    readFile( filename );
  }

  private void readFile( String filename )
                       throws ParserException
  {
    PTFile ptfile = new PTFile();
    TopoDroidApp.Log( TopoDroidApp.LOG_PTOPO, "PT survey " + mName + " read file " + filename );
    try {
      FileInputStream fs = new FileInputStream( filename );
      ptfile.read( fs );
      fs.close();
    } catch ( FileNotFoundException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "File not found: " + filename );
      // FIXME
      return;
    } catch ( IOException e ) { // on close
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "IO exception: " + e );
      return;
    }
    int nr_trip = ptfile.tripCount();
    TopoDroidApp.Log( TopoDroidApp.LOG_PTOPO, "PT trip count " + nr_trip );
    if ( nr_trip > 0 ) { // use only the first trip
      PTTrip trip = ptfile.getTrip(0);
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );
      pw.format( "%04d-%02d-%02d", trip._year, trip._month, trip._day );
      mDate = sw.getBuffer().toString();
      mComment = "";
      if ( trip.hasComment() ) mComment = trip.comment();
      // trip.declination(); NOT USED
      // TODO create a survey
      mTeam = "";

      int shot_count = ptfile.shotCount();
      int extend = DistoXDBlock.EXTEND_NONE;
      int ext_flag = extend;
      DistoXDBlock b     = null;  // temporary block pointer
      DistoXDBlock start = null;  // first block inserted
      DistoXDBlock last  = null;  // last block on the list

      String from_prev = "";
      String to_prev   = "";
      // Pattern pattern = Pattern.compile( "0+" );
      for ( int s=0; s < shot_count; ++s ) {
        PTShot shot = ptfile.getShot(s);
        String from = shot.from().toString();
        String to   = shot.to().toString();
        from = from.replaceAll( "^0+", "" );
        to   = to.replaceAll( "^0+", "" );
        if ( from.equals("-") ) from = "";
        if ( to.equals("-") )   to = "";
        if ( from.equals( from_prev ) && to.equals( to_prev ) && ! to_prev.equals("") ) {
          from = "";
          to   = "";
        } else {
          from_prev = from;
          to_prev   = to;
        }
        float da = shot.distance();
        float ba = shot.azimuth();
        float ca = shot.inclination();
        float ra = shot.roll();
        if ( shot.isFlipped() ) {
          if ( extend != DistoXDBlock.EXTEND_LEFT ) {
            extend = DistoXDBlock.EXTEND_LEFT;
            ext_flag = extend;
          } else {
            ext_flag = DistoXDBlock.EXTEND_NONE;
          }
        } else {
          if ( extend != DistoXDBlock.EXTEND_RIGHT ) {
            extend = DistoXDBlock.EXTEND_RIGHT;
            ext_flag = extend;
          } else {
            ext_flag = DistoXDBlock.EXTEND_NONE;
          }
        }
        shots.add( new ParserShot( from, to,  da, ba, ca, ra, extend, false, false, 
                                   shot.hasComment()? shot.comment() : "" ) );
      }
      // TopoDroidApp.Log( TopoDroidApp.LOG_PTOPO, "PT parser shot count " + shot_count + " size " + shots.size() );

      // FIXME drawings are not imported yet
      PTDrawing outline = ptfile.getOutline();
      mOutline = readDrawing( outline, PlotInfo.PLOT_PLAN );

      PTDrawing sideview = ptfile.getSideview();
      mSideview = readDrawing( sideview, PlotInfo.PLOT_EXTENDED );
    }
  }

  /** return therion buffer with the sketch
   */
  private String readDrawing( PTDrawing outline, long type )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );

    if ( type == PlotInfo.PLOT_PLAN ) {
      pw.format("scrap 1p -proj plan ");
    } else {
      pw.format("scrap 1s -proj extended ");
    }
    pw.format("[0 0 1 0 0.0 0.0 1.0 0.0 m]\n");

    PTMapping mapping = outline.mapping();
    int scale = mapping.scale();
    int x0 = - (mapping.origin().x());
    int y0 =   (mapping.origin().y()) - 1400; // FIXME why 1400 ???
    int elem_count = outline.elementNumber();
    if ( elem_count > 0 ) {
      for (int h=0; h<elem_count; ++h ) {
        try {
          PTPolygonElement elem = (PTPolygonElement)outline.getElement(h);
          int point_count = elem.pointCount();
          int col = elem.color();
          if ( point_count > 1 ) {
            PTPoint point = elem.point(0);
            // FIXME Therion::LineType type = colors.thLine( col );
            // add a line to the plotCanvas
            pw.format("line user\n");
            int k=0;
            int x1 =   (int)( PT_SCALE*(point.x() - x0)/1000.0 );
            int y1 = - (int)( PT_SCALE*(point.y() - y0)/1000.0 );
            // FIXME drawer->insertLinePoint( x1, y1, type, canvas );
            pw.format("  %d %d \n", x1, y1 );

            for (++k; k<point_count; ++k ) {
              point = elem.point(k);
              int x =   (int)( PT_SCALE*(point.x() - x0)/1000.0 );
              int y = - (int)( PT_SCALE*(point.y() - y0)/1000.0 );
              if ( Math.abs(x - x1) >= 4 || Math.abs(y - y1) >= 4 ) {
                x1 = x;
                y1 = y;
                // FIXME drawer->insertLinePoint( x, y, type, canvas );
                pw.format("  %d %d \n", x, y );
              }
            }
            // FIXME drawer->insertLinePoint( x1, y1, type, canvas ); // close the line
            pw.format("  %d %d \n", x1, y1 );
            pw.format("endline\n");
          } else if ( point_count == 1 ) {
            PTPoint point = elem.point(0);
            // FIXME Therion::PointType type = colors.thPoint( col );
            int x =   (int)( PT_SCALE*(point.x() - x0)/1000.0 );
            int y = - (int)( PT_SCALE*(point.y() - y0)/1000.0 );
            // FIXME drawer->insertPoint(x, y, type, canvas );
            pw.format("point %d %d user \n", x, y );
            
          }
        } catch( ClassCastException e ) {
        }
      }
    }
    pw.format("endscrap\n");
    return sw.getBuffer().toString();
  }


}

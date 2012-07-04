/** @file PhotoInfo.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo info (id, station, comment)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120522 created
 */
package com.android.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

class PhotoInfo
{
  public long sid;       // survey id
  public long id;        // photo id
  public String station;
  public String name;    // photo filename without extension ".jpg" and survey prefix dir
  // public String date;
  public String comment;

  public PhotoInfo( long _sid, long _id, String st, String nm, String cmt )
  {
    sid = _sid;
    id  = _id;
    station = st;
    name = nm;
    // date = dt;
    comment = cmt;
  }

  // String getPhotoName() 
  // {
  //   StringWriter sw = new StringWriter();
  //   PrintWriter pw = new PrintWriter( sw );
  //   pw.format( "%d-%03d", sid, id );
  //   return sw.getBuffer().toString();
  // }

}

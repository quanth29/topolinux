/** @file TherionParser.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid Therion parser
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 * CHANGES
 * 20120606 created (adapted from Cave3D to handle single file, only shots)
 */
package com.android.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
// import java.io.StringWriter;
// import java.io.PrintWriter;
import java.util.ArrayList;

import android.util.Log;

public class TherionParser
{
  private static final String TAG = "DistoX";

  public String mDate = null;  // survey date
  public String mTeam = "";
  public String mTitle = "";

  private int extend = 1;
  private boolean duplicate = false;

  /** fix station:
   * fix stations are supposed to be referred to the same coord system
   */
  class Fix 
  {
    // private CS cs;
    String name;
    float e, n, z; // north east, vertical (upwards)

    public Fix( String nm, float e0, float n0, float z0 )
    {
      name = nm;
      e = e0;
      n = n0;
      z = z0;
    }
  }

  class Shot
  {
    String from;
    String to;
    float len, ber, cln;
    int extend;
    boolean duplicate;

    public Shot( String f, String t, float l, float b, float c, int e, boolean d )
    {
      from = f;
      to   = t;
      len = l;
      ber = b;
      cln = c;
      extend = e;
      duplicate = d;
    }
  }

  private ArrayList< Fix > fixes;
  private ArrayList< Shot > shots;   // centerline shots
  private ArrayList< Shot > splays;  // splay shots

  public int getShotNumber()    { return shots.size(); }
  public int getSplayNumber()   { return splays.size(); }

  public ArrayList< Shot > getShots() { return shots; }
  public ArrayList< Shot > getSplays() { return splays; }


  public TherionParser( String filename ) throws ParserException
  {
    fixes  = new ArrayList< Fix >();
    shots  = new ArrayList< Shot >();
    splays = new ArrayList< Shot >();
    readFile( filename, "", false, 0.0f, 1.0f, 1.0f, 1.0f );
  }

  private String nextLine( BufferedReader br ) throws IOException
  {
    StringBuilder ret = new StringBuilder();
    {
      String line = br.readLine();
      if ( line == null ) return null; // EOF
      while ( line != null && line.endsWith( "\\" ) ) {
        ret.append( line.replace( '\\', ' ' ) ); // FIXME
        line = br.readLine();
      }
      if ( line != null ) ret.append( line );
    }
    return ret.toString();
  }

  /** read input file
   * @param filename name of the file to parse
   * @param basepath survey pathname base
   * @param usd   use survey declination
   * @param sd    survey decliunation
   * @param ul units of length (as multiple of 1 meter)
   * @param ub units of bearing (as multiple of 1 degree)
   * @param uc units of clino
   */
  private void readFile( String filename, String basepath,
                         boolean usd, float sd,
                         float ul, float ub, float uc )
                       throws ParserException
  {
    String path = basepath;   // survey pathname(s)
    int[] survey_pos = new int[50]; // current survey pos in the pathname FIXME max 50 levels
    int ks = 0;                     // survey index
    boolean in_centerline = false;
    boolean in_survey = false;
    boolean in_map = false;
    boolean use_centerline_declination = false;
    boolean use_survey_declination = usd;
    float centerline_declination = 0.0f;
    float survey_declination = sd;
    float units_len = ul;
    float units_ber = ub;
    float units_cln = uc;
    int jFrom    = 0;
    int jTo      = 1;
    int jLength  = 2;
    int jCompass = 3;
    int jClino   = 4;
    String prefix = "";
    String suffix = "";

    try {
      String dirname = "./";
      int i = filename.lastIndexOf('/');
      if ( i > 0 ) dirname = filename.substring(0, i+1);
      // System.out.println("readFile dir " + dirname + " filename " + filename );
      // Log.v( TAG, "reading file " + filename + " dir " + dirname );

      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      String line = nextLine( br );
      while ( line != null ) {
        // Log.v(TAG, "TH " + line );
        line = line.trim();
        int pos = line.indexOf( '#' );
        if ( pos >= 0 ) {
          line = line.substring( 0, pos );
        }
        if ( line.length() > 0 ) {
          String[] vals = line.split( " " );
          int vals_len = 0;
          for ( int k=0; k<vals.length; ++k ) {
            vals[vals_len] = vals[k];
            if ( vals[vals_len].length() > 0 ) {
              ++ vals_len;
            }
          }
          if ( vals_len > 0 ) {
            String cmd = vals[0];
            if ( cmd.equals("survey") ) {
              survey_pos[ks] = path.length(); // set current survey pos in pathname
              path = path + "." + vals[1];    // add survey name to path
              ++ks;
              in_survey = true;
              for ( int j=2; j<vals_len; ++j ) {
                if ( vals[j].equals("-declination") ) {
                  use_survey_declination = true;
                  survey_declination = Float.parseFloat( vals[j+1] );
                } else if ( vals[j].equals("-title") ) {
                }
              }
            } else if ( in_map ) {
              if ( cmd.equals("endmap") ) {
                in_map = false;
              }
            } else if ( in_centerline ) {
              if ( cmd.equals("endcenterline") ) {
                in_centerline = false;
                use_centerline_declination = false;
                centerline_declination = 0.0f;
              } else if ( cmd.equals("date") ) {
                String date = vals[1];
                if ( mDate == null ) mDate = date; // save centerline date
              } else if ( cmd.equals("team") ) { 
                for ( int j = 1; j < vals_len; ++j ) {
                  mTeam +=  " " + vals[j];
                }
              // } else if ( cmd.equals("explo-date") ) {
              // } else if ( cmd.equals("explo-team") ) {
              // } else if ( cmd.equals("instrument") ) {
              // } else if ( cmd.equals("calibrate") ) {
              // } else if ( cmd.equals("units") ) { // TODO parse "units" command
              // } else if ( cmd.equals("sd") ) {
              // } else if ( cmd.equals("grade") ) {
              } else if ( cmd.equals("declination") ) { 
                int j = 1;
                while ( vals[j] != null ) {
                  if ( vals[j].length() > 0 ) {
                    use_centerline_declination = true;
                    centerline_declination = Float.parseFloat( vals[j] );
                    break;
                  }
                }
              // } else if ( cmd.equals("infer") ) {
              // } else if ( cmd.equals("mark") ) {
              } else if ( cmd.equals("flags") ) {
                if ( vals[1].equals("not") && vals[2].substring(0,3).equals("dup") ) {
                  duplicate = false;
                } else if ( vals[1].substring(0,3).equals("dup") ) {
                  duplicate = true;
                }
              } else if ( cmd.equals("station") ) {
                // station <station> <comment>
              } else if ( cmd.equals("cs") ) { 
                // TODO cs
              } else if ( cmd.equals("fix") ) {
                // ***** fix station east north Z units
                if ( vals_len > 4 ) {
                  String name;
                  int idx = vals[1].indexOf('@');
                  if ( idx > 0 ) {
                    name = vals[1].substring(0,idx); // + "@" + path + "." + vals[1].substring(idx+1);
                  } else {
                    name = vals[1]; // + "@" + path;
                  }
	          fixes.add( new Fix( name,
                                      Float.parseFloat( vals[2] ),
                                      Float.parseFloat( vals[3] ),
                                      Float.parseFloat( vals[4] ) ) );
                }
              } else if ( cmd.equals("equate") ) {
                if ( vals_len > 2 ) {
                  String from, to;
                  int idx = vals[1].indexOf('@');
                  if ( idx > 0 ) {
                    from = vals[1].substring(0,idx); // + "@" + path + "." + vals[1].substring(idx+1);
                  } else {
                    from = vals[1]; // + "@" + path;
                  }
                  for ( int j=2; j<vals_len; ++j ) {
                    idx = vals[j].indexOf('@');
                    if ( idx > 0 ) {
                      to = vals[j].substring(0,idx); // + "@" + path + "." + vals[j].substring(idx+1);
                    } else {
                      to = vals[j]; // + "@" + path;
                    }
                    shots.add( new Shot( from, to, 0.0f, 0.0f, 0.0f, extend, duplicate ) );
                  }
                }
              // } else if ( cmd.equals("break") ) {
              // } else if ( cmd.equals("group") ) {
              // } else if ( cmd.equals("endgroup") ) {
              // } else if ( cmd.equals("walls") ) {
              // } else if ( cmd.equals("vthreshold") ) {
              } else if ( cmd.equals("extend") ) { 
                if ( vals[1].equals("left") ) {
                  extend = -1;
                } else if ( vals[1].equals("right") ) {
                  extend = 1;
                } else if ( vals[1].substring(0,4).equals("vert") ) {
                  extend = 0;
                }
              } else if ( cmd.equals("station_names") ) {
                prefix = "";
                suffix = "";
                if ( vals_len > 1 ) {
                  int off = vals[1].indexOf( '"' );
                  if ( off >= 0 ) {
                    int end = vals[1].lastIndexOf( '"' );
                    prefix = vals[1].substring(off+1, end );
                  }
                  if ( vals_len > 2 ) {
                    off = vals[2].indexOf( '"' );
                    if ( off >= 0 ) {
                      int end = vals[2].lastIndexOf( '"' );
                      suffix = vals[2].substring(off+1, end );
                    }
                  }
                }
              } else if ( cmd.equals("data") ) {
                // data normal from to length compass clino ...
                if ( vals[1].equals("normal") ) {
                  int j0 = 0;
                  for ( int j=2; j < vals_len; ++j ) {
                    if ( vals[j].equals("from") ) {
                      jFrom = j0; ++j0;
                    } else if ( vals[j].equals("to") ) {
                      jTo = j0; ++j0;
                    } else if ( vals[j].equals("length") || vals[j].equals("tape") ) {
                      jLength = j0; ++j0;
                    } else if ( vals[j].equals("compass") || vals[j].equals("bearing") ) {
                      jCompass = j0; ++j0;
                    } else if ( vals[j].equals("clino") || vals[j].equals("gradient") ) {
                      jClino = j0; ++j0;
                    } else {
                      ++j0;
                    }
                  }
                // TODO other style syntax
                // } else if ( vals[1].equals("topofil") ) {
                // } else if ( vals[1].equals("diving") ) {
                // } else if ( vals[1].equals("cartesian") ) {
                // } else if ( vals[1].equals("cylpolar") ) {
                // } else if ( vals[1].equals("dimensions") ) {
                // } else if ( vals[1].equals("nosurvey") ) {
                }
              } else if ( vals_len >= 5 ) {
                // FIXME
                String from = vals[jFrom];
                String to   = vals[jTo];
                float len  = Float.parseFloat( vals[jLength] ) * units_len;
                float ber  = Float.parseFloat( vals[jCompass] ) * units_ber;
                float cln  = Float.parseFloat( vals[jClino] ) * units_cln;
                if ( use_centerline_declination ) {
                  ber += centerline_declination;
                } else if ( use_survey_declination ) {
                  ber += survey_declination;
                }
                // TODO add shot
                if ( to.equals("-") || to.equals(".") ) {
                  // TODO splay shot
                  // from = from + "@" + path;
                  to = null;
                  splays.add( new Shot( from, to, len, ber, cln, extend, duplicate ) );
                } else {
                  // from = from + "@" + path;
                  // to   = to + "@" + path;
                  shots.add( new Shot( from, to, len, ber, cln, extend, duplicate ) );
                }
              }            
            } else if ( cmd.equals("input") ) {
              // int j = 1;
              // while ( vals[j] != null ) {
              //   if ( vals[j].length() > 0 ) {
              //     filename = vals[j];
              //     if ( filename.endsWith( ".th" ) ) {
              //       readFile( dirname + '/' + filename, 
              //           path,
              //           use_survey_declination, survey_declination,
              //           units_len, units_ber, units_cln );
              //     }
              //     break;
              //   }
              // }
            } else if ( cmd.equals("centerline") ) {
              in_centerline = true;
            } else if ( cmd.equals("map") ) {
              in_map = true;
            } else if ( cmd.equals("endsurvey") ) {
              --ks;
              path = path.substring(survey_pos[ks]); // return to previous survey_pos in path
              in_survey = ( ks > 0 );
            }
          }
        }
        line = nextLine( br );
      }
    } catch ( IOException e ) {
      // TODO
      throw new ParserException();
    }
  }

}

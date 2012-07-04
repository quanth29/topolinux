/* @file DataHelper.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid SQLite database manager
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120516 survey team and survey info unpdate
 * 20120518 db path from TopoDroid app
 * 20120521 methods for CalibInfo
 * 20120522 photo support
 * 20120603 fixed update and delete support
 * 20120610 archive (zip)
 */
package com.android.DistoX;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteException;

// import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataHelper extends DataSetObservable
{
   // private static final String TAG = "DistoX_DH";

   private static String DATABASE_NAME = TopoDroidApp.getDirFile( "distox6.db" );
   private static final int DATABASE_VERSION = 6;

   private static final String CONFIG_TABLE = "configs";
   private static final String SURVEY_TABLE = "surveys";
   private static final String FIXED_TABLE  = "fixeds";
   private static final String CALIB_TABLE  = "calibs";
   private static final String SHOT_TABLE   = "shots";
   private static final String GM_TABLE     = "gms";
   private static final String PLOT_TABLE   = "plots";
   private static final String PHOTO_TABLE  = "photos";

   private SQLiteDatabase myDB = null;
   private long           myNextId;   // id of next shot
   private long           myNextCId;  // id of next calib-data

   private SQLiteStatement updateConfig;
   private SQLiteStatement updateGMGroupStmt;
   private SQLiteStatement updateGMErrorStmt;
   private SQLiteStatement updateShotStmt;
   private SQLiteStatement updateShotStmtFull;
   private SQLiteStatement updateShotNameStmt;
   // private SQLiteStatement updateShotExtendStmt;
   // private SQLiteStatement updateShotFlagStmt;
   // private SQLiteStatement updateShotCommentStmt;
   private SQLiteStatement updateSurveyStmt;
   private SQLiteStatement updateSurveyTeamStmt;
   // private SQLiteStatement updateSurveyNameStmt;
   private SQLiteStatement updateCalibStmt;
   private SQLiteStatement deleteShotStmt;
   private SQLiteStatement undeleteShotStmt;
   private SQLiteStatement deletePlotStmt;
   private SQLiteStatement undeletePlotStmt;

   private SQLiteStatement updateFixedStationStmt;
   private SQLiteStatement updateFixedStatusStmt;

   private SQLiteStatement doDeleteGMStmt;
   private SQLiteStatement doDeleteCalibStmt;
   private SQLiteStatement doDeletePhotoStmt;
   private SQLiteStatement doDeletePlotStmt;
   private SQLiteStatement doDeleteFixedStmt;
   private SQLiteStatement doDeleteShotStmt;
   private SQLiteStatement doDeleteSurveyStmt;


   // ----------------------------------------------------------------------
   // DATABASE

   public SQLiteDatabase getDb() { return myDB; }

   public DataHelper( Context context /* , String survey, String calib */ )
   {

      DistoXOpenHelper openHelper = new DistoXOpenHelper( context );

      try {
        myDB = openHelper.getWritableDatabase();

        updateConfig       = myDB.compileStatement( "UPDATE configs SET value=? WHERE key=?" );
        updateGMGroupStmt  = myDB.compileStatement( "UPDATE gms SET grp=? WHERE calibId=? AND id=?" );
        updateGMErrorStmt  = myDB.compileStatement( "UPDATE gms SET error=? WHERE calibId=? AND id=?" );
        updateShotNameStmt = myDB.compileStatement(
                             "UPDATE shots SET fStation=?, tStation=? WHERE surveyId=? AND id=?" );
        updateShotStmt     = myDB.compileStatement( 
                             "UPDATE shots SET fStation=?, tStation=?, extend=?, flag=? WHERE surveyId=? AND id=?" );
        updateShotStmtFull = myDB.compileStatement(
                             "UPDATE shots SET fStation=?, tStation=?, extend=?, flag=?, comment=? WHERE surveyId=? AND id=?" );

        // updateShotExtendStmt  = myDB.compileStatement( "UPDATE shots SET extend=? WHERE surveyId=? AND id=?" );
        // updateShotFlagStmt    = myDB.compileStatement( "UPDATE shots SET flag=? WHERE surveyId=? AND id=?" );
        // updateShotCommentStmt = myDB.compileStatement( "UPDATE shots SET comment=? WHERE surveyId=? AND id=?" );
        updateSurveyStmt = myDB.compileStatement( "UPDATE surveys SET day=?, comment=? WHERE id=?" );
        updateSurveyTeamStmt = myDB.compileStatement( "UPDATE surveys SET team=? WHERE id=?" );
        // updateSurveyNameStmt = myDB.compileStatement( "UPDATE surveys SET name=? WHERE id=?" );
        updateCalibStmt = myDB.compileStatement( "UPDATE calibs SET day=?, device=?, comment=? WHERE id=?" );

        deleteShotStmt   = myDB.compileStatement( "UPDATE shots set status=1 WHERE surveyId=? AND id=?" );
        undeleteShotStmt = myDB.compileStatement( "UPDATE shots set status=0 WHERE surveyId=? AND id=?" );
        deletePlotStmt   = myDB.compileStatement( "UPDATE plots set status=1 WHERE surveyId=? AND id=?" );
        undeletePlotStmt = myDB.compileStatement( "UPDATE plots set status=0 WHERE surveyId=? AND id=?" );

        updateFixedStationStmt = myDB.compileStatement( "UPDATE fixeds set station=? WHERE surveyId=? AND id=?" );
        updateFixedStatusStmt = myDB.compileStatement( "UPDATE fixeds set status=? WHERE surveyId=? AND id=?" );

        doDeleteGMStmt    = myDB.compileStatement( "DELETE FROM gms where calibId=?" );
        doDeleteCalibStmt = myDB.compileStatement( "DELETE FROM calibs where id=?" );

        doDeletePhotoStmt  = myDB.compileStatement( "DELETE FROM photos where surveyId=?" );
        doDeletePlotStmt   = myDB.compileStatement( "DELETE FROM plots where surveyId=?" );
        doDeleteFixedStmt  = myDB.compileStatement( "DELETE FROM fixeds where surveyId=?" );
        doDeleteShotStmt   = myDB.compileStatement( "DELETE FROM shots where surveyId=?" );
        doDeleteSurveyStmt = myDB.compileStatement( "DELETE FROM surveys where id=?" );

      } catch ( SQLiteException e ) {
        myDB = null;
        // Log.e( TAG, "Failed to get DB " + e.getMessage() );
      }
   }
   
   // ----------------------------------------------------------------------
   // SURVEY DATA

   public int updateShot( long id, long sid, String fStation, String tStation, long extend, long flag, String comment )
   {
     // Log.v( TAG, "updateShot " + fStation + "-" + tStation + " " + extend + " " + flag + " <" + comment + ">");
     if ( myDB == null ) return -1;
     // if ( makesCycle( id, sid, fStation, tStation ) ) return -2;

     if ( comment != null ) {
       updateShotStmtFull.bindString( 1, fStation );
       updateShotStmtFull.bindString( 2, tStation );
       updateShotStmtFull.bindLong(   3, extend );
       updateShotStmtFull.bindLong(   4, flag );
       updateShotStmtFull.bindString( 5, comment );
       updateShotStmtFull.bindLong(   6, sid );     // WHERE
       updateShotStmtFull.bindLong(   7, id );
       updateShotStmtFull.execute();
     } else {
       updateShotStmt.bindString( 1, fStation );
       updateShotStmt.bindString( 2, tStation );
       updateShotStmt.bindLong(   3, extend );
       updateShotStmt.bindLong(   4, flag );
       updateShotStmt.bindLong(   5, sid );
       updateShotStmt.bindLong(   6, id );
       updateShotStmt.execute();
     }
     return 0;
   }

   // public boolean makesCycle( long id, long sid, String f, String t )
   // {
   //   if ( t == null || t.length() == 0 ) return false;
   //   if ( f == null || f.length() == 0 ) return false;
   //   int cnt = 0;
   //   if ( hasShotAtStation( id, sid, f ) ) ++cnt;
   //   if ( hasShotAtStation( id, sid, t ) ) ++cnt;
   //   Log.v( TAG, "makesCycle cnt " + cnt );
   //   return cnt >= 2;
   // }
     

   public void updateShotName( long id, long sid, String fStation, String tStation )
   {
     if ( myDB == null ) return;
     updateShotNameStmt.bindString( 1, fStation );
     updateShotNameStmt.bindString( 2, tStation );
     updateShotNameStmt.bindLong(   3, sid );
     updateShotNameStmt.bindLong(   4, id );
     updateShotNameStmt.execute();
   }

   // public void updateShotExtend( long id, long sid, long extend )
   // {
   //   if ( myDB == null ) return;
   //   updateShotExtendStmt.bindLong( 1, extend );
   //   updateShotExtendStmt.bindLong( 2, sid );
   //   updateShotExtendStmt.bindLong( 3, id );
   //   updateShotExtendStmt.execute();
   // }

   // public void updateShotFlag( long id, long sid, long flag )
   // {
   //   if ( myDB == null ) return;
   //   updateShotFlagStmt.bindLong( 1, flag );
   //   updateShotFlagStmt.bindLong( 2, sid );
   //   updateShotFlagStmt.bindLong( 3, id );
   //   updateShotFlagStmt.execute();
   // }

   // public void updateShotComment( long id, long sid, String comment )
   // {
   //   if ( myDB == null ) return;
   //   updateShotCommentStmt.bindString( 1, comment );
   //   updateShotCommentStmt.bindLong( 2, sid );
   //   updateShotCommentStmt.bindLong( 3, id );
   //   updateShotCommentStmt.execute();
   // }


   public void deleteShot( long shot_id, long survey_id ) 
   {
     if ( myDB == null ) return;
     // Log.v( TAG, "delete shot: " + shot_id + "/" + survey_id );
     deleteShotStmt.bindLong( 1, survey_id ); 
     deleteShotStmt.bindLong( 2, shot_id );
     deleteShotStmt.execute();
   }

   public void undeleteShot( long shot_id, long survey_id ) 
   {
     if ( myDB == null ) return;
     // Log.v( TAG, "undelete shot: " + shot_id + "/" + survey_id );
     undeleteShotStmt.bindLong( 1, survey_id ); 
     undeleteShotStmt.bindLong( 2, shot_id );
     undeleteShotStmt.execute();
   }
   
   public void deletePlot( long plot_id, long survey_id )
   {
     if ( myDB == null ) return;
     deletePlotStmt.bindLong( 1, survey_id );
     deletePlotStmt.bindLong( 2, plot_id );
     deletePlotStmt.execute();
   }
   
   public void undeletePlot( long plot_id, long survey_id )
   {
     if ( myDB == null ) return;
     // Log.v( TAG, "undelete plot: " + plot_id + "/" + survey_id );
     undeletePlotStmt.bindLong( 1, survey_id );
     undeletePlotStmt.bindLong( 2, plot_id );
     undeletePlotStmt.execute();
   }

   public long insertShot( long sid, long id, double d, double b, double c, double r )
   {
     return insertShot( sid, id, "", "",  d, b, c, r, ( b < 180.0 )? 1L : -1L, DistoXDBlock.BLOCK_SURVEY, 0L, "" );
   }

   public long insertShot( long sid, long id, String from, String to, 
                           double d, double b, double c, double r, 
                           long extend, long flag, long status, String comment )
   {
     if ( myDB == null ) return -1;
     if ( id == -1L ) {
       ++ myNextId;
       id = myNextId;
     } else {
       myNextId = id;
     }
     ContentValues cv = new ContentValues();
     cv.put( "surveyId", sid );
     cv.put( "id",       id );
     cv.put( "fStation", from );
     cv.put( "tStation", to );
     cv.put( "distance", d );
     cv.put( "bearing",  b );
     cv.put( "clino",    c );
     cv.put( "roll",     r );
     cv.put( "extend",   extend );
     cv.put( "flag",     flag );
     cv.put( "status",   status );
     cv.put( "comment",  comment );
     myDB.insert( SHOT_TABLE, null, cv );
     return id;
   }

   public void doDeleteCalib( long cid ) 
   {
     if ( myDB == null ) return;
     doDeleteGMStmt.bindLong( 1, cid );
     doDeleteGMStmt.execute();
     doDeleteCalibStmt.bindLong( 1, cid );
     doDeleteCalibStmt.execute();
   }

   public void doDeleteSurvey( long sid ) 
   {
     if ( myDB == null ) return;
     doDeletePhotoStmt.bindLong( 1, sid );
     doDeletePhotoStmt.execute();
     doDeletePlotStmt.bindLong( 1, sid );
     doDeletePlotStmt.execute();
     doDeleteFixedStmt.bindLong( 1, sid );
     doDeleteFixedStmt.execute();
     doDeleteShotStmt.bindLong( 1, sid );
     doDeleteShotStmt.execute();
     doDeleteSurveyStmt.bindLong( 1, sid );
     doDeleteSurveyStmt.execute();
   }
   
   
   // ----------------------------------------------------------------------
   // CALIBRATION DATA

   public long updateGMName( long id, long cid, String grp )
   {
     if ( myDB == null ) return -1;
     updateGMGroupStmt.bindString( 1, grp );
     updateGMGroupStmt.bindLong( 2, cid );
     updateGMGroupStmt.bindLong( 3, id );
     updateGMGroupStmt.execute();
     return 0;
   }

   public long updateGMError( long id, long cid, double error )
   {
     if ( myDB == null ) return -1;
     updateGMErrorStmt.bindDouble( 1, error );
     updateGMErrorStmt.bindLong( 2, cid );
     updateGMErrorStmt.bindLong( 3, id );
     updateGMErrorStmt.execute();
     return 0;
   }

   public long insertGM( long cid, long gx, long gy, long gz, long mx, long my, long mz )
   {
     if ( myDB == null ) return -1;
     ++ myNextCId;
     ContentValues cv = new ContentValues();
     cv.put( "calibId", cid );
     cv.put( "id",      myNextCId );
     cv.put( "gx", gx );
     cv.put( "gy", gy );
     cv.put( "gz", gz );
     cv.put( "mx", mx );
     cv.put( "my", my );
     cv.put( "mz", mz );
     cv.put( "grp", 0 );
     cv.put( "error", 0.0 );
     long ret= myDB.insert( GM_TABLE, null, cv );
     return ret;
   }
   
   // ----------------------------------------------------------------------
   // SELECT STATEMENTS

   public List< PhotoInfo > selectAllPhoto( long sid )
   {
     List< PhotoInfo > list = new ArrayList< PhotoInfo >();
     Cursor cursor = myDB.query( PHOTO_TABLE,
			         new String[] { "id", "station", "name", "comment" }, // columns
                                 "surveyId=?", 
                                new String[] { Long.toString(sid) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       do {
         list.add( new PhotoInfo( sid, 
                                  cursor.getLong(0),
                                  cursor.getString(1),
                                  cursor.getString(2),
                                  cursor.getString(3) ) );
       } while (cursor.moveToNext());
     }
     // Log.v( TAG, "list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public List< PhotoInfo > selectPhotoAtStation( long sid, String station )
   {
     // Log.v( TAG, "selectPhoto(AtStation) survey " + sid + " station " + st );
     List< PhotoInfo > list = new ArrayList< PhotoInfo >();
     Cursor cursor = myDB.query( PHOTO_TABLE,
			         new String[] { "id", "station", "name", "comment" }, // columns
                                 "surveyId=? and station=?",  // selection = WHERE clause (without "WHERE")
                                new String[] { Long.toString(sid), station },     // selectionArgs
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       do {
         list.add( new PhotoInfo( sid,
                                  cursor.getLong(0),
                                  cursor.getString(1),
                                  cursor.getString(2),
                                  cursor.getString(3) ) );
       } while (cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public List< FixedInfo > selectAllFixed( long sid, int status )
   {
     List<  FixedInfo  > list = new ArrayList<  FixedInfo  >();
     Cursor cursor = myDB.query( FIXED_TABLE,
			         new String[] { "id", "station", "longitude", "latitude", "altitude", "comment" }, // columns
                                 "surveyId=? and status=?",  // selection = WHERE clause (without "WHERE")
                                new String[] { Long.toString(sid), Long.toString(status) },     // selectionArgs
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       do {
         list.add( new FixedInfo( cursor.getLong(0),
                                  cursor.getString(1),
                                  cursor.getDouble(2),
                                  cursor.getDouble(3),
                                  cursor.getDouble(4),
                                  cursor.getString(5) ) );
       } while (cursor.moveToNext());
     }
     // Log.v( TAG, "list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public List< PlotInfo > selectAllPlots( long sid, long status )
   {
     List<  PlotInfo  > list = new ArrayList<  PlotInfo  >();
     Cursor cursor = myDB.query(PLOT_TABLE,
			        new String[] { "id", "name", "type" }, // columns
                                "surveyId=? and status=?", 
                                new String[] { Long.toString(sid), Long.toString(status) }, 
                                null,  // groupBy
                                null,  // having
                                "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         PlotInfo plot = new  PlotInfo ();
         plot.setId( cursor.getLong(0), sid );
         plot.name = cursor.getString(1);
         plot.type = cursor.getInt(2);
         list.add( plot );
       } while (cursor.moveToNext());
     }
     // Log.v( TAG, "list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public DistoXDBlock selectShot( long shot_id, long survey_id )
   {
     Cursor cursor = myDB.query(SHOT_TABLE,
       new String[] { "fStation", "tStation", "distance", "bearing", "clino", "extend", "flag", "comment" }, // columns
       "surveyId=? and id=?", 
       new String[] { Long.toString(survey_id), Long.toString(shot_id) },
       null,  // groupBy
       null,  // having
       null ); // order by
     DistoXDBlock block = null;
     if (cursor.moveToFirst()) {
       block = new DistoXDBlock();
       block.setId( shot_id, survey_id );
       block.setName(    cursor.getString(0), cursor.getString(1) );
       block.mLength  = (float)( cursor.getDouble(2) );
       block.setBearing( (float)( cursor.getDouble(3) ) );
       block.mClino   = (float)( cursor.getDouble(4) );
       block.mExtend  = cursor.getLong(5);
       block.mFlag    = cursor.getLong(6);
       block.mComment = cursor.getString(7);
     }
     // Log.v( TAG, "list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return block;
   }


   public DistoXDBlock selectPreviousLegShot( long shot_id, long survey_id )
   {
     Cursor cursor = myDB.query(SHOT_TABLE,
       new String[] { "fStation", "tStation", "distance", "bearing", "clino", "extend", "flag", "comment" }, // columns
       "surveyId=? and id<?",
       new String[] { Long.toString(survey_id), Long.toString(shot_id) },
       null,  // groupBy
       null,  // having
       "id DESC" ); // order by
       // "1" ); // no limit
     DistoXDBlock block = null;
     if (cursor.moveToFirst()) {
       do {
         if ( cursor.getString(0).length() > 0 && cursor.getString(1).length() > 0 ) {
           block = new DistoXDBlock();
           block.setId( shot_id, survey_id );
           block.setName( cursor.getString(0), cursor.getString(1) );
           block.mLength  = (float)( cursor.getDouble(2) );
           block.setBearing( (float)( cursor.getDouble(3) ) );
           block.mClino   = (float)( cursor.getDouble(4) );
           block.mExtend  = cursor.getLong(5);
           block.mFlag    = cursor.getLong(6);
           block.mComment = cursor.getString(7);
         }  
       } while (block == null && cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return block;
   }

   // private boolean hasShotAtStation( long id, long sid, String station )
   // {
   //   Cursor cursor = myDB.query(SHOT_TABLE,
   //     new String[] { "id", "fStation", "tStation" }, // columns
   //     "surveyId=? and status=0 and ( fStation=? or tStation=? ) and id!=?",
   //     new String[] { Long.toString(sid), station, station, Long.toString(id) },
   //     null,  // groupBy
   //     null,  // having
   //     "id" ); // order by
   //   boolean ret = false;
   //   if (cursor.moveToFirst()) {
   //     do {
   //       long idc = (long)cursor.getLong(0);
   //       Log.v(TAG, "hasShotAtStation " + id + " " + idc ); 
   //       if ( id != idc ) {
   //         ret = true;
   //       }
   //     } while (ret == false && cursor.moveToNext());
   //   }
   //   if (cursor != null && !cursor.isClosed()) {
   //     cursor.close();
   //   }
   //   Log.v(TAG, "hasShotAtStation returns " + ret );
   //   return ret;
   // }

   public List<DistoXDBlock> selectAllShotsAtStation( long sid, String station )
   {
     List< DistoXDBlock > list = new ArrayList< DistoXDBlock >();
     Cursor cursor = myDB.query(SHOT_TABLE,
       new String[] { "id", "fStation", "tStation", "distance", "bearing", "clino", "extend", "flag" }, // columns
       "surveyId=? and status=? and fStation=?", 
       new String[] { Long.toString(sid), Long.toString(TopoDroidApp.STATUS_NORMAL), station },
       null,  // groupBy
       null,  // having
       "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         String fStation = cursor.getString(1);
         String tStation = cursor.getString(2);
         DistoXDBlock block = new DistoXDBlock();
         block.setId( cursor.getLong(0), sid );
         block.setName( fStation, tStation );
         block.mLength = (float)( cursor.getDouble(3) );
         block.setBearing(  (float)( cursor.getDouble(4) ) );
         block.mClino  =  (float)( cursor.getDouble(5) );
         block.mExtend =  cursor.getLong(6);
         block.mFlag   =  cursor.getLong(7);
         // block.setComment = cursor.getLong(8) );
         list.add( block );
       } while (cursor.moveToNext());
     }
     // Log.v( TAG, "list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

/*
   public class ShotAdapter extends SimpleCursorAdapter
   {
     public static int BLANK = 0;
     public static int CENTERLINE = 1;
     public static int SPLAY = 2;
     private static int type = BLANK;

     public void reset() { type = BLANK; }

     public ShotAdapter( Context context, Cursor c )
     {
       // super( context, R.layout.message, c, FROM_STRINGS, TO_INTS );
     }

     @Override
     public void bindView( View row, Context context, Cursor cursor )
     {
       TextView tv = (TextView)row;

       long id = cursor.getLong(0);
       String fStation = cursor.getString(1);
       String tStation = cursor.getString(2);
       // block.setName(    fStation, tStation );
       float length  = (float)( cursor.getDouble(3) );
       float bearing = (float)( cursor.getDouble(4) );
       float clino   = (float)( cursor.getDouble(5) );
       long extend   = cursor.getLong(6);
       long flag     = cursor.getLong(7);
       // block.setComment( cursor.getLong(7) );

       // DistoXDBlock cur = item;
       // int t = cur.type();
       // if ( cur.relativeDistance( prev ) < app.closeDistance() ) {
       //   if ( mLeg ) { // hide leg extra shots
       //     if ( mBlank && prev.type() == DistoXDBlock.BLOCK_BLANK ) {
       //       // prev was skipped: draw it now
       //       cur = prev;
       //     } else {
       //       continue;
       //     }
       //   } else {
       //     // nothing 
       //   }
       // } else {
       //   if ( tString == null || tString.length() < 1 ) {
       //     if ( fString == null || fString.length() < 1 ) {
       //       type = BLANK;
       //     } else {
       //       type = SPLAY;
       //     }
       //   } else { 
       //     type = CENTERLINE;
       //   }
       // }
       StringWriter sw = new StringWriter();
       PrintWriter pw  = new PrintWriter(sw);
       pw.format("%d <%s-%s> %.2f %.1f %.1f [%c]",
         id, fStation, tStation, length, bearing, clino, mExtendTag[ extend + 1 ] );
       if ( flag == 1 ) pw.format( " *" );
       tv.setText( sw.getBuffer().toString() );
       //     if ( mBlank ) continue;
       //     if ( mSplay ) continue;
     }
   }
*/

   public List<DistoXDBlock> selectAllShots( long sid, long status )
   {
     List< DistoXDBlock > list = new ArrayList< DistoXDBlock >();
     Cursor cursor = myDB.query(SHOT_TABLE,
       new String[] { "id", "fStation", "tStation", "distance", "bearing", "clino", "extend", "flag", "comment" }, // columns
       "surveyId=? and status=?",
       new String[] { Long.toString(sid), Long.toString(status) },
       null,  // groupBy
       null,  // having
       "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         DistoXDBlock block = new DistoXDBlock();
         block.setId( cursor.getLong(0), sid );
         String fStation = cursor.getString(1);
         String tStation = cursor.getString(2);
         // block.setName( cursor.getString(1) );
         block.setName(    fStation, tStation );
         block.mLength = (float)( cursor.getDouble(3) );
         block.setBearing( (float)( cursor.getDouble(4) ) );
         block.mClino  = (float)( cursor.getDouble(5) );
         block.mExtend = cursor.getLong(6);
         block.mFlag   = cursor.getLong(7);
         block.mComment = cursor.getString(8);
         list.add( block );
       } while (cursor.moveToNext());
     }
     // Log.v( TAG, "list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public List<CalibCBlock> selectAllGMs( long cid )
   {
     List< CalibCBlock > list = new ArrayList< CalibCBlock >();
     Cursor cursor = myDB.query(GM_TABLE,
                                new String[] { "id", "gx", "gy", "gz", "mx", "my", "mz", "grp", "error" }, // columns
                                "calibId=?",
                                new String[] { Long.toString(cid) },
                                null,  // groupBy
                                null,  // having
                                "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         CalibCBlock block = new CalibCBlock();
         block.setId( cursor.getLong(0), cid );
         block.setData( 
           cursor.getLong(1),
           cursor.getLong(2),
           cursor.getLong(3),
           cursor.getLong(4),
           cursor.getLong(5),
           cursor.getLong(6) );
         block.setGroup( cursor.getLong(7) );
         block.setError( (float)( cursor.getDouble(8) ) );
         list.add( block );
       } while (cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public CalibCBlock selectGM( long id, long cid )
   {
     CalibCBlock block = null;
     Cursor cursor = myDB.query(GM_TABLE,
                                new String[] { "id", "gx", "gy", "gz", "mx", "my", "mz", "grp", "error" }, // columns
                                "calibId=? and id=?", 
                                new String[] { Long.toString(cid), Long.toString(id) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       block = new CalibCBlock();
       block.setId( cursor.getLong(0), cid );
       block.setData( 
         cursor.getLong(1),
         cursor.getLong(2),
         cursor.getLong(3),
         cursor.getLong(4),
         cursor.getLong(5),
         cursor.getLong(6) );
       block.setGroup( cursor.getLong(7) );
       block.setError( (float)( cursor.getDouble(8) ) );
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return block;
   }

   public SurveyInfo selectSurveyInfo( long sid )
   {
     SurveyInfo info = null;
     Cursor cursor = myDB.query( SURVEY_TABLE,
                                new String[] { "name", "day", "team", "comment" }, // columns
                                "id=?",
                                new String[] { Long.toString(sid) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       info = new SurveyInfo();
       info.id      = sid;
       info.name    = cursor.getString( 0 );
       info.date    = cursor.getString( 1 );
       info.team    = cursor.getString( 2 );
       info.comment = cursor.getString( 3 );
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return info;
   }
 
   public CalibInfo selectCalibInfo( long cid )
   {
     CalibInfo info = null;
     Cursor cursor = myDB.query( CALIB_TABLE,
                                new String[] { "name", "day", "device", "comment" }, // columns
                                "id=?",
                                new String[] { Long.toString(cid) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       info = new CalibInfo();
       info.id      = cid;
       info.name    = cursor.getString( 0 );
       info.date    = cursor.getString( 1 );
       info.device  = cursor.getString( 2 );
       info.comment = cursor.getString( 3 );
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return info;
   }
    
   // ----------------------------------------------------------------------
   // SELECT: LIST SURVEY / CABIL NAMES

   private List<String> selectAllNames( String table )
   {
     List< String > list = new ArrayList< String >();
     Cursor cursor = myDB.query( table,
                                new String[] { "name" }, // columns
                                null, null, null, null, "name" );
     if (cursor.moveToFirst()) {
       do {
         list.add( cursor.getString(0) );
       } while (cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public List<String> selectAllSurveys() { return selectAllNames( SURVEY_TABLE ); }
       
   public List<String> selectAllCalibs() { return selectAllNames( CALIB_TABLE ); }

   // ----------------------------------------------------------------------
   // CONFIG DATA

   public String getValue( String key )
   {
     String value = null;
     if ( myDB == null ) return null;
     Cursor cursor = myDB.query( CONFIG_TABLE,
                                new String[] { "value" }, // columns
                                "key = ?", new String[] { key },
                                null, null, null );
     if (cursor.moveToFirst()) {
       value = cursor.getString( 0 );
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     return value;
   }

   public void setValue( String key, String value )
   {
     Cursor cursor = myDB.query( CONFIG_TABLE,
                                new String[] { "value" }, // columns
                                "key = ?", new String[] { key },
                                null, null, null );
     if (cursor.moveToFirst()) {
       updateConfig.bindString( 1, value );
       updateConfig.bindString( 2, key );
       updateConfig.execute();
     } else {
       ContentValues cv = new ContentValues();
       cv.put( "key",     key );
       cv.put( "value",   value );
       myDB.insert( CONFIG_TABLE, null, cv );
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
   }

     
   // ----------------------------------------------------------------------
   /* Set the current survey/calib name.
    * If the survey/calib name does not exists a new record is inserted in the table
    */

   private String getNameFromId( String table, long id )
   {
     String ret = null;
     Cursor cursor = myDB.query( table, new String[] { "name" },
                          "id=?", new String[] { Long.toString(id) },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       ret = cursor.getString(0);
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     return ret;
   }

   private long getIdFromName( String table, String name ) 
   {
     long id = -1;
     if ( myDB == null ) { return -2; }
     Cursor cursor = myDB.query( table, new String[] { "id" },
                                 "name = ?", new String[] { name },
                                 null, null, null );
     if (cursor.moveToFirst() ) {
       id = cursor.getLong(0);
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     return id;
   }

   private long setName( String table, String name ) 
   {
     long id = -1;
     if ( myDB == null ) { return 0; }
     // Log.v( TAG, "setName >" + name + "< table " + table );
     Cursor cursor = myDB.query( table, new String[] { "id" },
                                 "name = ?", new String[] { name },
                                 null, null, null );
     if (cursor.moveToFirst() ) {
       id = cursor.getLong(0);
       if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     } else {
       if (cursor != null && !cursor.isClosed()) { cursor.close(); }
       // SELECT max(id) FROM table
       cursor = myDB.query( table, new String[] { "max(id)" },
                            null, null, null, null, null );
       if (cursor.moveToFirst() ) {
         id = 1 + cursor.getLong(0);
       } else {
         id = 1;
       }
       if (cursor != null && !cursor.isClosed()) { cursor.close(); }
       // INSERT INTO table VALUES( id, name, "", "" )
       ContentValues cv = new ContentValues();
       cv.put( "id",      id );
       cv.put( "name",    name );
       cv.put( "day",     "" );
       cv.put( "comment", "" );
       myDB.insert( table, null, cv );
     }
     return id;
   }

   public long getFixedId( long sid, String station )
   {
     long ret = -1;
     if ( station != null ) {
       Cursor cursor = myDB.query( FIXED_TABLE, new String[] { "id" },
                            "surveyId=? and station=?", 
                            new String[] { Long.toString(sid), station },
                            null, null, null );
       if (cursor.moveToFirst() ) {
         ret = cursor.getLong(0);
       }
       if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     }
     return ret;
   }

   public long getPlotId( long sid, String name )
   {
     long ret = -1;
     if ( name != null ) {
       Cursor cursor = myDB.query( PLOT_TABLE, new String[] { "id" },
                            "surveyId=? and name=?", 
                            new String[] { Long.toString(sid), name },
                            null, null, null );
       if (cursor.moveToFirst() ) {
         ret = cursor.getLong(0);
       }
       if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     }
     return ret;
   }

   public String getPlotFieldAsString( long sid, long pid, String field )
   {
     String ret = null;
     Cursor cursor = myDB.query( PLOT_TABLE, new String[] { field },
                          "surveyId=? and id=?", 
                          new String[] { Long.toString(sid), Long.toString(pid) },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       if ( field.equals("type") ) {
         ret = Long.toString( cursor.getLong(0) );
       } else {
         ret = cursor.getString(0);
       }
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     return ret;
   }

   /**
    * @param station   photo station
    * @param sid       survey id
    * @param name      photo file name
    * @param comment
    */
   public long insertPhoto( long sid, long id, String station, String name, String comment )
   {
     if ( id == -1L ) id = maxId( PHOTO_TABLE, sid );
     ContentValues cv = new ContentValues();
     cv.put( "surveyId",  sid );
     cv.put( "id",        id );
     cv.put( "station",   station );
     cv.put( "name",      name );
     cv.put( "comment",   (comment == null)? "" : comment );
     myDB.insert( PHOTO_TABLE, null, cv );
     return id;
   }

   

   public long insertFixed( long sid, long id, String station, double lng, double lat, double alt, String comment, long status )
   {
     long ret = getFixedId( sid, station ); 
     if ( ret >= 0 ) return -1; // fixed already present in the db
     if ( id == -1L ) id = maxId( FIXED_TABLE, sid );
     ContentValues cv = new ContentValues();
     cv.put( "surveyId",  sid );
     cv.put( "id",        id );
     cv.put( "station",   station );
     cv.put( "longitude", lng );
     cv.put( "latitude",  lat );
     cv.put( "altitude",  alt );
     cv.put( "comment",   (comment == null)? "" : comment );
     cv.put( "status",    status );
     myDB.insert( FIXED_TABLE, null, cv );
     return id;
   }

   public long insertPlot( long sid, long id, String name, long type, long status, String start, String view )
   {
     long ret = getPlotId( sid, name );
     if ( ret >= 0 ) return -1;
     if ( id == -1L ) id = maxId( PLOT_TABLE, sid );
     ContentValues cv = new ContentValues();
     cv.put( "surveyId", sid );
     cv.put( "id",       id );
     cv.put( "name",     name );
     cv.put( "type",     type );
     cv.put( "status",   status );
     cv.put( "start",    start );
     cv.put( "view",     (view == null)? "" : view );
     myDB.insert( PLOT_TABLE, null, cv );
     return id;
   }

   private long maxId( String table, long sid )
   {
     long id = 1;
     Cursor cursor = myDB.query( table, new String[] { "max(id)" },
                          "surveyId=?", 
                          new String[] { Long.toString(sid) },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       id = 1 + cursor.getLong(0);
     }
     return id;
   }

   public boolean updateFixedStation( long id, long sid, String station )
   {
     updateFixedStationStmt.bindString( 1, station );
     updateFixedStationStmt.bindLong( 2, sid );
     updateFixedStationStmt.bindLong( 3, id );
     updateFixedStationStmt.execute();
     return true;
   }

   public boolean updateFixedStatus( long id, long sid, long status )
   {
     updateFixedStatusStmt.bindLong( 1, status );
     updateFixedStatusStmt.bindLong( 2, sid );
     updateFixedStatusStmt.bindLong( 3, id );
     updateFixedStatusStmt.execute();
     return true;
   }

   public boolean updateSurveyDayAndComment( String name, String date, String comment )
   {
     long id = getIdFromName( SURVEY_TABLE, name );
     if ( id >= 0 ) { // survey name exists
       return updateSurveyDayAndComment( id, date, comment );
     }
     return false;
   }


   public boolean updateSurveyDayAndComment( long id, String date, String comment )
   {
     // Log.v( TAG, "update survey: id " + id + " day " + date + " comment \"" + comment + "\"" );
     if ( date == null ) return false;
     updateSurveyStmt.bindString( 1, date );
     updateSurveyStmt.bindString( 2, (comment != null)? comment : "" );
     updateSurveyStmt.bindLong( 3, id );
     updateSurveyStmt.execute();
     return true;
   }

   public boolean updateSurveyTeam( long id, String team )
   {
     // Log.v( TAG, "update survey: id " + id + " team \"" + team + "\"" );
     updateSurveyTeamStmt.bindString( 1, (team != null)? team : "" );
     updateSurveyTeamStmt.bindLong( 2, id );
     updateSurveyTeamStmt.execute();
     return true;
   }

   public boolean hasSurveyName( String name )  { return hasName( name, SURVEY_TABLE ); }
   public boolean hasCalibName( String name )  { return hasName( name, CALIB_TABLE ); }

   private boolean hasName( String name, String table )
   {
     boolean ret = false;
     Cursor cursor = myDB.query( table, new String[] { "id" },
                          "name=?", 
                          new String[] { name },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       ret = true;
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     return ret;
   }

   // public boolean updateSurveyName( long id, String name )
   // {
   //   // Log.v( TAG, "update survey: id " + id + " name \"" + name + "\"" );
   //   updateSurveyNameStmt.bindString( 1, (name != null)? name : "" );
   //   updateSurveyNameStmt.bindLong( 2, id );
   //   updateSurveyNameStmt.execute();
   //   return true;
   // }

   public boolean updateCalibInfo( long id, String date, String device, String comment )
   {
     // Log.v( TAG, "data update calibs: id " + id + " day " + date + " comm. " + comment );
     if ( date == null ) return false;
     updateCalibStmt.bindString( 1, date );
     updateCalibStmt.bindString( 2, (device != null)? device : "" );
     updateCalibStmt.bindString( 3, (comment != null)? comment : "" );
     updateCalibStmt.bindLong( 4, id );
     updateCalibStmt.execute();
     return true;
   }

   public long setSurvey( String survey )
   {
     myNextId = 0;
     long sid = setName( SURVEY_TABLE, survey );
     Cursor cursor = myDB.query( SHOT_TABLE, new String[] { "max(id)" },
                          "surveyId=?", new String[] { Long.toString(sid) },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       myNextId = cursor.getLong(0);
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     return sid;
   }

   public long setCalib( String calib ) 
   {
     myNextCId = 0;
     long cid = setName( CALIB_TABLE, calib );
     Cursor cursor = myDB.query( GM_TABLE, new String[] { "max(id)" },
                          "calibId=?", new String[] { Long.toString(cid) },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       myNextCId = cursor.getLong(0);
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     return cid;
   }

   public String getCalibFromId( long cid ) { return getNameFromId( CALIB_TABLE, cid ); }

   public String getSurveyFromId( long sid ) { return getNameFromId( SURVEY_TABLE, sid ); }



   public String getSurveyDate( long sid ) { return getSurveyFieldAsString( sid, "day" ); }

   public String getSurveyComment( long sid ) { return getSurveyFieldAsString( sid, "comment" ); }

   public String getSurveyTeam( long sid ) { return getSurveyFieldAsString( sid, "team" ); }

   private String getSurveyFieldAsString( long sid, String attr )
   {
     String ret = null;
     Cursor cursor = myDB.query( SURVEY_TABLE, new String[] { attr },
                          "id=?", new String[] { Long.toString(sid) },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       ret = cursor.getString(0);
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     return ret;
   }
      
      
   public void dumpToFile( String filename, long sid )
   {
     // Log.v(TAG, "dumpToFile " + filename );
     // String where = "surveyId=" + Long.toString(sid);
     try {
       FileWriter fw = new FileWriter( filename );
       PrintWriter pw = new PrintWriter( fw );
       Cursor cursor = myDB.query( SURVEY_TABLE, 
                            new String[] { "name", "day", "team", "comment" },
                            "id=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, \"%s\", \"%s\", \"%s\", \"%s\" );\n",
                     SURVEY_TABLE,
                     sid,
                     cursor.getString(0),
                     cursor.getString(1),
                     cursor.getString(2),
                     cursor.getString(3) );
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }

       cursor = myDB.query( PHOTO_TABLE, // SELECT ALL PHOTO RECORD
  			           new String[] { "id", "station", "name", "comment" },
                                   "surveyId=?", new String[] { Long.toString(sid) },
                                   null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, %d, \"%s\", \"%s\", \"%s\" );\n",
                     PHOTO_TABLE,
                     sid,
                     cursor.getLong(0),
                     cursor.getString(1),
                     cursor.getString(2),
                     cursor.getString(3) );
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }
       cursor = myDB.query( PLOT_TABLE, 
                            new String[] { "id", "name", "type", "status", "start", "view" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, %d, \"%s\", %d, %d, \"%s\", \"%s\" );\n",
                     PLOT_TABLE,
                     sid,
                     cursor.getLong(0),
                     cursor.getString(1),
                     cursor.getLong(2),
                     cursor.getLong(3),
                     cursor.getString(4),
                     cursor.getString(5) );
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }
       cursor = myDB.query( SHOT_TABLE, 
                            new String[] { "id", "fStation", "tStation", "distance", "bearing", "clino", "roll",
                                           "extend", "flag", "status", "comment" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, %d, \"%s\", \"%s\", %.2f, %.2f, %.2f, %.2f, %d, %d, %d, \"%s\" );\n",
                     SHOT_TABLE,
                     sid,
                     cursor.getLong(0),
                     cursor.getString(1),
                     cursor.getString(2),
                     cursor.getDouble(3),
                     cursor.getDouble(4),
                     cursor.getDouble(5),
                     cursor.getDouble(6),
                     cursor.getLong(7),
                     cursor.getLong(8),
                     cursor.getLong(9),
                     cursor.getString(10) );
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }
       cursor = myDB.query( FIXED_TABLE, 
                            new String[] { "id", "station", "longitude", "latitude", "altitude", "comment", "status" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, %d, \"%s\", %.6f, %.6f, %.6f, \"%s\", %d );\n",
                     FIXED_TABLE,
                     sid,
                     cursor.getLong(0),
                     cursor.getString(1),
                     cursor.getDouble(2),
                     cursor.getDouble(3),
                     cursor.getDouble(4),
                     cursor.getString(5),
                     cursor.getLong(6) );
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }

       fw.flush();
       fw.close();
     } catch ( FileNotFoundException e ) {
       // FIXME
     } catch ( IOException e ) {
       // FIXME
     }
   }

   private int pos, len;

   private void skipSpaces( String val )
   {
     while ( pos < len && val.charAt(pos) == ' ' ) ++ pos;
   }

   private void skipCommaAndSpaces( String val )
   {
     if ( pos < len && val.charAt(pos) == ',' ) ++pos;
     while ( pos < len && val.charAt(pos) == ' ' ) ++ pos;
   }
   
   private int nextQuote( String val )
   {
     int next = pos;
     while ( next < len && val.charAt(next) != '"' ) ++next; 
     return next;
   }

   private int nextCommaOrSpace( String val )
   {
     int next = pos;
     while ( next < len && val.charAt(next) != ',' && val.charAt(next) != ' ' ) ++next; 
     return next;
   }

   private String stringValue( String val ) 
   {
     ++pos; // skip '"'
     int next_pos = nextQuote( val );
     String ret = (pos == next_pos )? "" : val.substring(pos, next_pos );
     // Log.v(TAG, "STRING <" + ret + ">" );
     pos = next_pos + 1;
     skipCommaAndSpaces( val );
     return ret;
   }

   private long longValue( String val )
   {
     int next_pos = nextCommaOrSpace( val );
     // Log.v(TAG, "LONG " + pos + " " + next_pos + " " + len + " <" + val.substring(pos,next_pos) + ">" );
     long ret = Long.parseLong( val.substring( pos, next_pos ) );
     pos = next_pos;
     skipCommaAndSpaces( val );
     return ret;
   }

   private double doubleValue( String val )
   {
     int next_pos = nextCommaOrSpace( val );
     double ret = Double.parseDouble( val.substring(pos, next_pos ) );
     // Log.v(TAG, "DOUBLE " + ret );
     pos = next_pos;
     skipCommaAndSpaces( val );
     return ret;
   }

   /** load survey data from a sql file
    * @param filename  name of the sql file
    */
   long loadFromFile( String filename )
   {
     long sid = -1;
     long id, status;
     String station, name, comment;
     String line;
     try {
       FileReader fr = new FileReader( filename );
       BufferedReader br = new BufferedReader( fr );
       // first line is survey
       line = br.readLine();
       // Log.v(TAG, line );
       String[] vals = line.split(" ", 4);
       String table = vals[2];
       String v = vals[3];
       pos = v.indexOf( '(' ) + 1;
       len = v.lastIndexOf( ')' );
       skipSpaces( v );
       // Log.v(TAG, v + " " + pos + " " + len );
       if ( table.equals(SURVEY_TABLE) ) {
         long skip_sid = longValue( v );
         name        = stringValue( v );
         String day  = stringValue( v );
         String team = stringValue( v );
         comment     = stringValue( v );
         sid = setSurvey( name );
         updateSurveyDayAndComment( sid, day, comment );
         updateSurveyTeam( sid, team );
         while ( (line = br.readLine()) != null ) {
           // Log.v(TAG, line );
           vals = line.split(" ", 4);
           table = vals[2];
           v = vals[3];
           pos = v.indexOf( '(' ) + 1;
           len = v.lastIndexOf( ')' );
           skipSpaces( v );
           // Log.v(TAG, table + " " + v );
           skip_sid = longValue( v );
           id = longValue( v );
           if ( table.equals(PHOTO_TABLE) ) {
             station = stringValue( v );
             name    = stringValue( v );
             comment = stringValue( v );
             insertPhoto( sid, id, station, name, comment );
             // Log.v(TAG, "insertPhoto " + sid + " " + id + " " + station + " " + name );
           } else if ( table.equals(PLOT_TABLE) ) {
             name         = stringValue( v );
             long type    = longValue( v );
             status       = longValue( v );
             String start = stringValue( v );
             String view  = stringValue( v );
             insertPlot( sid, id, name, type, status, start, view );
             // Log.v(TAG, "insertPlot " + sid + " " + id + " " + start + " " + name );
           } else if ( table.equals(SHOT_TABLE) ) {
             String from = stringValue( v );
             String to   = stringValue( v );
             double d    = doubleValue( v );
             double b    = doubleValue( v );
             double c    = doubleValue( v );
             double r    = doubleValue( v );
             long extend = longValue( v );
             long flag   = longValue( v );
             status      = longValue( v );
             comment     = stringValue( v );
             insertShot( sid, id, from, to, d, b, c, r, extend, flag, status, comment );
             // Log.v(TAG, "insertShot " + sid + " " + id + " " + from + " " + to );
           } else if ( table.equals(FIXED_TABLE) ) {
             station    = stringValue( v );
             double lng = doubleValue( v );
             double lat = doubleValue( v );
             double alt = doubleValue( v );
             comment    = stringValue( v );
             status     = longValue( v );
             insertFixed( sid, id, station, lng, lat, alt, comment, status );
             // Log.v(TAG, "insertFixed " + sid + " " + id + " " + station  );
           }
         }
       }
       fr.close();
     } catch ( FileNotFoundException e ) {
     } catch ( IOException e ) {
     }

     return sid;
   }

   // ----------------------------------------------------------------------
   // DATABASE TABLES

   private static class DistoXOpenHelper extends SQLiteOpenHelper
   {
      private static final String create_table = "CREATE TABLE IF NOT EXISTS ";

      DistoXOpenHelper(Context context ) {
         // null SQLiteDatabase.CursorFactory 
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db) 
      {

         // Log.v( "DistoX DH", "onCreate ..." );
         db.execSQL( 
             create_table + CONFIG_TABLE
           + " ( key TEXT NOT NULL,"
           +   " value TEXT )"
         );

         // db.execSQL("DROP TABLE IF EXISTS " + SHOT_TABLE);
         // db.execSQL("DROP TABLE IF EXISTS " + SURVEY_TABLE);
         db.execSQL(
             create_table + SURVEY_TABLE 
           + " ( id INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
           +   " name TEXT, "
           +   " day TEXT, "
           +   " team TEXT, "
           +   " comment TEXT "
           +   ")"
         );
         db.execSQL(
             create_table + SHOT_TABLE 
           + " ( surveyId INTEGER, "
           +   " id INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
           +   " fStation TEXT, "
           +   " tStation TEXT, "
           +   " distance DOUBLE, "
           +   " bearing DOUBLE, "
           +   " clino DOUBLE, "
           +   " roll DOUBLE, "
           +   " extend INTEGER, "
           +   " flag INTEGER, "
           +   " status INTEGER, "
           +   " comment TEXT "
           // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
           // +   " ON DELETE CASCADE "
           +   ")"
         );
         db.execSQL(
             create_table + FIXED_TABLE
           + " ( surveyId INTEGER, "
           +   " id INTEGER, "   //  PRIMARY KEY AUTOINCREMENT, "
           +   " station TEXT, "
           +   " longitude DOUBLE, "
           +   " latitude DOUBLE, "
           +   " altitude DOUBLE, "
           +   " comment TEXT, "
           +   " status INTEGER"
           // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
           // +   " ON DELETE CASCADE "
           +   ")"
         );
          
         db.execSQL(
             create_table + CALIB_TABLE
           + " ( id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
           +   " name TEXT, "
           +   " day TEXT, "
           +   " device TEXT, "
           +   " comment TEXT )"
         );
         db.execSQL(
             create_table + GM_TABLE 
           + " ( calibId INTEGER, "
           +   " id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
           +   " gx INTEGER, "
           +   " gy INTEGER, "
           +   " gz INTEGER, "
           +   " mx INTEGER, "
           +   " my INTEGER, "
           +   " mz INTEGER, "
           +   " grp INTEGER, "
           +   " error REAL "
           // +   " calibId REFERENCES " + CALIB_TABLE + "(id)"
           // +   " ON DELETE CASCADE "
           +   ")"
         );
         db.execSQL(
             create_table + PLOT_TABLE 
           + " ( surveyId INTEGER, "
           +   " id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
           +   " name TEXT, "
           +   " type INTEGER, "
           +   " status INTEGER, "
           +   " start TEXT, "
           +   " view TEXT "
           // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
           // +   " ON DELETE CASCADE "
           +   ")"
         );

         db.execSQL(
             create_table + PHOTO_TABLE
           + " ( surveyId INTEGER, "
           +   " id INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
           +   " station TEXT, "
           +   " comment TEXT, "
           +   " name TEXT "
           // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
           // +   " ON DELETE CASCADE "
           +   ")"
         );

         // db.execSQL(
         //     " CREATE TRIGGER fk_insert_shot BEFORE "
         //   + " INSERT on " + SHOT_TABLE 
         //   + " FOR EACH ROW BEGIN "
         //   +   " SELECT RAISE "
         //   +   " (ROLLBACK, 'insert on \"" + SHOT_TABLE + "\" violates foreing key constraint')"
         //   +   " WHERE ( SELECT id FROM " + SURVEY_TABLE + " WHERE id = NEW.surveyId ) IS NULL; "
         //   + " END;"
         // );
         // db.execSQL(
         //     "CREATE TRIGGER fk_delete_survey BEFORE DELETE ON " + SURVEY_TABLE
         //   + " FOR EACH ROW BEGIN "
         //   +   " SELECT RAISE "
         //   +   " (ROLLBACK, 'delete from \"" + SURVEY_TABLE + "\" violates constraint')"
         //   +   " WHERE ( id IS IN ( SELECT DISTINCT surveyId FROM " + SHOT_TABLE + " ) );"
         //   + " END;"
         // );
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
      {
         // Log.w("DistoXOpenHelper", "Upgrading database: drop tables and recreate.");
         // db.execSQL("DROP TABLE IF EXISTS " + SHOT_TABLE);
         // onCreate(db);
      }
   }
}

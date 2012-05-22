/* @file DistoXDataHelper.java
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
 */
package com.android.DistoX;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteException;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DistoXDataHelper extends DataSetObservable
{
   private static final String TAG = "DistoX_DH";

   private static final String DATABASE_NAME = TopoDroidApp.APP_BASE_PATH + "distox4.db";
   private static final int DATABASE_VERSION = 1;

   private static final String CONFIG_TABLE = "configs";
   private static final String SURVEY_TABLE = "surveys";
   private static final String FIXED_TABLE  = "fixeds";
   private static final String CALIB_TABLE  = "calibs";
   private static final String SHOT_TABLE   = "shots";
   private static final String GM_TABLE     = "gms";
   private static final String PLOT_TABLE   = "plots";

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

   private SQLiteStatement doDeleteGMStmt;
   private SQLiteStatement doDeleteCalibStmt;
   private SQLiteStatement doDeletePlotStmt;
   private SQLiteStatement doDeleteShotStmt;
   private SQLiteStatement doDeleteSurveyStmt;


   // ----------------------------------------------------------------------
   // DATABASE

   public SQLiteDatabase getDb() { return myDB; }

   public DistoXDataHelper( Context context /* , String survey, String calib */ )
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
        updateCalibStmt = myDB.compileStatement( "UPDATE calibs SET day=?, comment=? WHERE id=?" );

        deleteShotStmt   = myDB.compileStatement( "UPDATE shots set status=1 WHERE surveyId=? AND id=?" );
        undeleteShotStmt = myDB.compileStatement( "UPDATE shots set status=0 WHERE surveyId=? AND id=?" );
        deletePlotStmt   = myDB.compileStatement( "UPDATE plots set status=1 WHERE surveyId=? AND id=?" );
        undeletePlotStmt = myDB.compileStatement( "UPDATE plots set status=0 WHERE surveyId=? AND id=?" );

        doDeleteGMStmt   = myDB.compileStatement( "DELETE FROM gms where calibId=?" );
        doDeleteCalibStmt = myDB.compileStatement( "DELETE FROM calibs where id=?" );

        doDeletePlotStmt  = myDB.compileStatement( "DELETE FROM plots where surveyId=?" );
        doDeleteShotStmt  = myDB.compileStatement( "DELETE FROM plots where surveyId=?" );
        doDeleteSurveyStmt = myDB.compileStatement( "DELETE FROM surveys where id=?" );

      } catch ( SQLiteException e ) {
        myDB = null;
        Log.e( TAG, "Failed to get DB " + e.getMessage() );
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

   public long insertShot( long sid, double d, double b, double c, double r )
   {
     if ( myDB == null ) return -1;
     ++ myNextId;
     long extend = -1;
     if ( b < 180.0 ) extend = 1;
     ContentValues cv = new ContentValues();
     cv.put( "surveyId", sid );
     cv.put( "id",       myNextId );
     cv.put( "fStation", "" );
     cv.put( "tStation", "" );
     cv.put( "distance", d );
     cv.put( "bearing",  b );
     cv.put( "clino",    c );
     cv.put( "roll",     r );
     cv.put( "extend",   extend );
     cv.put( "flag",     DistoXDBlock.BLOCK_SURVEY );
     cv.put( "status",   0 );
     cv.put( "comment",  "" );
     myDB.insert( SHOT_TABLE, null, cv );
     return myNextId;
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
     doDeletePlotStmt.bindLong( 1, sid );
     doDeletePlotStmt.execute();
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

   public List< DistoXFix > selectAllFixed( long sid )
   {
     // Log.v( TAG, "selectAllFixeds() survey " + sid );
     List<  DistoXFix  > list = new ArrayList<  DistoXFix  >();
     Cursor cursor = myDB.query( FIXED_TABLE,
			         new String[] { "station", "longitude", "latitude", "altitude", "comment" }, // columns
                                 "surveyId=?",  // selection = WHERE clause (without "WHERE")
                                new String[] { Long.toString(sid) },     // selectionArgs
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       do {
         DistoXFix fix = new DistoXFix( cursor.getString(0),
                                        cursor.getDouble(1),
                                        cursor.getDouble(2),
                                        cursor.getDouble(3),
                                        cursor.getString(4) );
         list.add( fix );
       } while (cursor.moveToNext());
     }
     // Log.v( TAG, "list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public List< DistoXPlot > selectAllPlots( long sid, long status )
   {
     // Log.v( TAG, "selectAllPlots() survey " + sid );
     List<  DistoXPlot  > list = new ArrayList<  DistoXPlot  >();
     Cursor cursor = myDB.query(PLOT_TABLE,
			        new String[] { "id", "name", "type" }, // columns
                                "surveyId=? and status=?",  // selection = WHERE clause (without "WHERE")
                                new String[] { Long.toString(sid), Long.toString(status) },     // selectionArgs
                                null,  // groupBy
                                null,  // having
                                "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         DistoXPlot plot = new  DistoXPlot ();
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
     // Log.v( TAG, "selectShot() " + shot_id + " survey " + survey_id );
     Cursor cursor = myDB.query(SHOT_TABLE,
       new String[] { "fStation", "tStation", "distance", "bearing", "clino", "extend", "flag", "comment" }, // columns
       "surveyId=? and id=?",  // selection = WHERE clause (without "WHERE")
       new String[] { Long.toString(survey_id), Long.toString(shot_id) },  // selectionArgs
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
     // Log.v( TAG, "selectPreviousLegShot() " + shot_id + " survey " + survey_id );
     Cursor cursor = myDB.query(SHOT_TABLE,
       new String[] { "fStation", "tStation", "distance", "bearing", "clino", "extend", "flag", "comment" }, // columns
       "surveyId=? and id<?",  // selection = WHERE clause (without "WHERE")
       new String[] { Long.toString(survey_id), Long.toString(shot_id) },  // selectionArgs
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
   //     "surveyId=? and status=0 and ( fStation=? or tStation=? ) and id!=?",  // selection = WHERE clause (without "WHERE")
   //     new String[] { Long.toString(sid), station, station, Long.toString(id) },  // selectionArgs
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
       "surveyId=? and status=0 and fStation=?",  // selection = WHERE clause (without "WHERE")
       new String[] { Long.toString(sid), station },  // selectionArgs
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
     // Log.v( TAG, "selectAllShots() survey " + sid );
     List< DistoXDBlock > list = new ArrayList< DistoXDBlock >();
     Cursor cursor = myDB.query(SHOT_TABLE,
       new String[] { "id", "fStation", "tStation", "distance", "bearing", "clino", "extend", "flag", "comment" }, // columns
       "surveyId=? and status=?",  // selection = WHERE clause (without "WHERE")
       new String[] { Long.toString(sid), Long.toString(status) },  // selectionArgs
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
     // Log.v( TAG, "selectAllGMs() calib " + cid );
     List< CalibCBlock > list = new ArrayList< CalibCBlock >();
     Cursor cursor = myDB.query(GM_TABLE,
                                new String[] { "id", "gx", "gy", "gz", "mx", "my", "mz", "grp", "error" }, // columns
                                "calibId=?",  // selection = WHERE clause (without "WHERE")
                                new String[] { Long.toString(cid) },  // selectionArgs
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
     // Log.v( TAG, "selectAllGMs() calib " + cid );
     CalibCBlock block = null;
     Cursor cursor = myDB.query(GM_TABLE,
                                new String[] { "id", "gx", "gy", "gz", "mx", "my", "mz", "grp", "error" }, // columns
                                "calibId=? and id=?",  // selection = WHERE clause (without "WHERE")
                                new String[] { Long.toString(cid), Long.toString(id) },  // selectionArgs
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
     // Log.v(TAG, "selectSurveyInfo sid " + sid );
     Cursor cursor = myDB.query( SURVEY_TABLE,
                                new String[] { "name", "day", "team", "comment" }, // columns
                                "id=?",  // selection = WHERE clause (without "WHERE")
                                new String[] { Long.toString(sid) },  // selectionArgs
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
     // Log.v(TAG, "selectCalibInfo cid " + cid );
     Cursor cursor = myDB.query( CALIB_TABLE,
                                new String[] { "name", "day", "comment" }, // columns
                                "id=?",  // selection = WHERE clause (without "WHERE")
                                new String[] { Long.toString(cid) },  // selectionArgs
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       info = new CalibInfo();
       info.id      = cid;
       info.name    = cursor.getString( 0 );
       info.date    = cursor.getString( 1 );
       info.comment = cursor.getString( 2 );
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
     if ( station == null ) {
       return -1;
     }
     Cursor cursor = myDB.query( PLOT_TABLE, new String[] { "id" },
                          "surveyId=? and station=?", 
                          new String[] { Long.toString(sid), station },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       ret = cursor.getLong(0);
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     return ret;
   }

   public long getPlotId( long sid, String name )
   {
     long ret = -1;
     if ( name == null ) {
       return -1;
     }
     Cursor cursor = myDB.query( PLOT_TABLE, new String[] { "id" },
                          "surveyId=? and name=?", 
                          new String[] { Long.toString(sid), name },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       ret = cursor.getLong(0);
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
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

   public long insertFixed( String station, long sid, double lng, double lat, double alt, String comment )
   {
     long ret = getFixedId( sid, station ); 
     if ( ret >= 0 ) return -1; // fixed already present in the db
     long id = 1;
     Cursor cursor = myDB.query( FIXED_TABLE, new String[] { "max(id)" },
                          "surveyId=?", 
                          new String[] { Long.toString(sid) },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       id = 1 + cursor.getLong(0);
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     // INSERT INTO table VALUES( sid, id, name, "", "" )
     ContentValues cv = new ContentValues();
     cv.put( "surveyId",  sid );
     cv.put( "id",        id );
     cv.put( "station",   station );
     cv.put( "longitude", lng );
     cv.put( "latitude",  lat );
     cv.put( "altitude",  alt );
     cv.put( "comment",   (comment == null)? "" : comment );
     myDB.insert( FIXED_TABLE, null, cv );
     return id;
   }

   public long insertPlot( String name, long sid, long type, String start, String view )
   {
     long ret = getPlotId( sid, name );
     if ( ret >= 0 ) return -1;
     long id = 1;
     Cursor cursor = myDB.query( PLOT_TABLE, new String[] { "max(id)" },
                          "surveyId=?", 
                          new String[] { Long.toString(sid) },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       id = 1 + cursor.getLong(0);
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     // INSERT INTO table VALUES( sid, id, name, "", "" )
     ContentValues cv = new ContentValues();
     cv.put( "surveyId", sid );
     cv.put( "id",       id );
     cv.put( "name",     name );
     cv.put( "type",     type );
     cv.put( "status",   0 );
     cv.put( "start",    start );
     cv.put( "view",     (view == null)? "" : view );
     myDB.insert( PLOT_TABLE, null, cv );
     return id;
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

   public boolean updateCalibDayAndComment( long id, String date, String comment )
   {
     // Log.v( TAG, "data update calibs: id " + id + " day " + date + " comm. " + comment );
     if ( date == null ) return false;
     updateCalibStmt.bindString( 1, date );
     updateCalibStmt.bindString( 2, (comment != null)? comment : "" );
     updateCalibStmt.bindLong( 3, id );
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
           +   " id INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
           +   " station TEXT, "
           +   " longitude DOUBLE, "
           +   " latitude DOUBLE, "
           +   " altitude DOUBLE, "
           +   " comment TEXT "
           // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
           // +   " ON DELETE CASCADE "
           +   ")"
         );
          
         db.execSQL(
             create_table + CALIB_TABLE
           + " ( id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
           +   " name TEXT, "
           +   " day TEXT, "
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

/** @file Archiver.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid survey archiver
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120611 created
 * 20120619 added therion export to the zip
 */
package com.android.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
// import java.util.zip.ZipFile;

import java.util.List;
// import java.util.Locale;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class Archiver
{
  private static final String TAG = "DistoX";

  private TopoDroidApp app;
  private static final int BUF_SIZE = 2048;
  private byte[] data = new byte[ BUF_SIZE ];

  public String zipname;

  public Archiver( TopoDroidApp _app )
  {
    app = _app;
    data = new byte[ BUF_SIZE ];
  }

  private boolean addEntry( ZipOutputStream zos, File name )
  {
    try {
      FileInputStream fis = new FileInputStream( name );
      BufferedInputStream bis = new BufferedInputStream( fis, BUF_SIZE );
      ZipEntry entry = new ZipEntry( name.getName() );
      int cnt;
      zos.putNextEntry( entry );
      while ( (cnt = bis.read( data, 0, BUF_SIZE )) != -1 ) {
      zos.write( data, 0, cnt );
      }
      bis.close();
      zos.closeEntry( );
    } catch (FileNotFoundException e ) {
      // FIXME
      return false;
    } catch ( IOException e ) {
      // FIXME
    }
    return true;
  }

  public boolean archive( )
  {
    if ( app.mSID < 0 ) return false;
    
    File temp = null;

    zipname = app.getSurveyZipFile();

    try {
      String pathname;
      FileOutputStream fos = new FileOutputStream( zipname );
      ZipOutputStream zos = new ZipOutputStream( new BufferedOutputStream( fos ) );

      List< PlotInfo > plots  = app.mData.selectAllPlots( app.mSID, TopoDroidApp.STATUS_NORMAL );
      for ( PlotInfo plt : plots ) {
        pathname = app.getSurveyPlotFile( plt.name );
        addEntry( zos, new File(pathname) );
      }
      plots  = app.mData.selectAllPlots( app.mSID, TopoDroidApp.STATUS_DELETED );
      for ( PlotInfo plt : plots ) {
        pathname = app.getSurveyPlotFile( plt.name );
        addEntry( zos, new File(pathname) );
      }

      List< PhotoInfo > photos = app.mData.selectAllPhoto( app.mSID );
      for ( PhotoInfo pht : photos ) {
        pathname = app.getSurveyJpgFile( pht.name );
        addEntry( zos, new File(pathname) );
      }

      File therion = new File( app.getSurveyThFile( ) );
      if ( therion != null && therion.exists() ) {
        addEntry( zos, therion );
      }

      File note = new File( TopoDroidApp.getSurveyNoteFile( app.mySurvey ) );
      if ( note != null && note.exists() ) {
        addEntry( zos, note );
      }
 
      pathname = TopoDroidApp.getSqlFile();
      app.mData.dumpToFile( pathname, app.mSID );
      addEntry( zos, new File(pathname) );

      zos.close();
    } catch ( FileNotFoundException e ) {
      // FIXME
      return false;
    } catch ( IOException e ) {
      // FIXME
      return false;
    } finally {
      File fp = new File( TopoDroidApp.getSqlFile() );
      if ( fp.exists() ) {
        // fp.delete();
      }
    }
    return true;
  }

  public void unArchive( String filename, String surveyname )
  {
    try {
      // byte buffer[] = new byte[36768];
      byte buffer[] = new byte[4096];

      FileInputStream fis = new FileInputStream( filename );
      ZipInputStream zin = new ZipInputStream( fis );
      ZipEntry ze = null;
      while ( ( ze = zin.getNextEntry() ) != null ) {
        if ( ze.isDirectory() ) {
          File dir = new File( TopoDroidApp.getDirFile( ze.getName() ) );
          if ( ! dir.isDirectory() ) {
            dir.mkdirs();
          }
        } else {
          Log.v(TAG, "Zip entry \"" + ze.getName() + "\"" );
          String pathname;
          boolean sql = false;
          if ( ze.getName().equals( "survey.sql" ) ) {
            pathname = TopoDroidApp.getSqlFile();
            sql = true;
          } else if ( ze.getName().endsWith( ".th2" ) ) {
            pathname = TopoDroidApp.getTh2File( ze.getName() );
          } else if ( ze.getName().endsWith( ".jpg" ) ) {
            // FIXME need survey dir
            pathname = TopoDroidApp.getJpgDir( surveyname );
            File file = new File( pathname );
            file.mkdirs();
            pathname = TopoDroidApp.getJpgFile( surveyname, ze.getName() );
          } else {
            pathname = TopoDroidApp.getNoteFile( ze.getName() );
          }
          Log.v(TAG, "filename \"" + pathname + "\"");

          FileOutputStream fout = new FileOutputStream( pathname );
          int c;
          while ( ( c = zin.read( buffer ) ) != -1 ) {
            fout.write(buffer, 0, c);
          }
          zin.closeEntry();
          fout.close();
          if ( sql ) {
            Log.v(TAG, "loadFromFile \"" + pathname + "\"");
            app.mData.loadFromFile( pathname );
            File f = new File( pathname );
            f.delete();
          }
        }
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
  }
}


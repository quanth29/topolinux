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
 * 20120720 added manifest
 * 20120725 TopoDroidApp log
 */
package com.android.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.util.List;
// import java.util.Locale;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;


public class Archiver
{
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

      List< PhotoInfo > photos = app.mData.selectAllPhotos( app.mSID, TopoDroidApp.STATUS_NORMAL );
      for ( PhotoInfo pht : photos ) {
        pathname = app.getSurveyJpgFile( pht.id );
        addEntry( zos, new File(pathname) );
      }

      photos = app.mData.selectAllPhotos( app.mSID, TopoDroidApp.STATUS_DELETED );
      for ( PhotoInfo pht : photos ) {
        pathname = app.getSurveyJpgFile( pht.id );
        addEntry( zos, new File(pathname) );
      }


      File therion = new File( app.getSurveyThFile( ) );
      if ( therion != null && therion.exists() ) {
        addEntry( zos, therion );
      }

      File vtopo = new File( app.getSurveyTroFile( ) );
      if ( vtopo != null && vtopo.exists() ) {
        addEntry( zos, vtopo );
      }

      File survex = new File( app.getSurveySvxFile( ) );
      if ( survex != null && survex.exists() ) {
        addEntry( zos, survex );
      }

      File compass = new File( app.getSurveyDatFile( ) );
      if ( compass != null && compass.exists() ) {
        addEntry( zos, compass );
      }

      File note = new File( TopoDroidApp.getSurveyNoteFile( app.mySurvey ) );
      if ( note != null && note.exists() ) {
        addEntry( zos, note );
      }
 
      pathname = TopoDroidApp.getSqlFile();
      app.mData.dumpToFile( pathname, app.mSID );
      addEntry( zos, new File(pathname) );

      pathname = TopoDroidApp.getManifestFile();
      app.writeManifestFile();
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

  public int unArchive( String filename, String surveyname )
  {
    int ok_manifest = -2;
    String pathname;
    try {
      // byte buffer[] = new byte[36768];
      byte buffer[] = new byte[4096];

      ZipFile zip = new ZipFile( filename );
      ZipEntry ze = zip.getEntry( "manifest" );
      if ( ze != null ) {
        pathname = TopoDroidApp.getManifestFile();
        FileOutputStream fout = new FileOutputStream( pathname );
        InputStream is =  zip.getInputStream( ze );
        int c;
        while ( ( c = is.read( buffer ) ) != -1 ) {
          fout.write(buffer, 0, c);
        }
        fout.close();
        ok_manifest = app.checkManifestFile( pathname, surveyname  );
        File f = new File( pathname );
        f.delete();
      }
      zip.close();
      if ( ok_manifest == 0 ) {
        FileInputStream fis = new FileInputStream( filename );
        ZipInputStream zin = new ZipInputStream( fis );
        while ( ( ze = zin.getNextEntry() ) != null ) {
          if ( ze.isDirectory() ) {
            File dir = new File( TopoDroidApp.getDirFile( ze.getName() ) );
            if ( ! dir.isDirectory() ) {
              dir.mkdirs();
            }
          } else {
            TopoDroidApp.Log( TopoDroidApp.LOG_ZIP, "Zip entry \"" + ze.getName() + "\"" );
            boolean sql = false;
            pathname = null;
            if ( ze.getName().equals( "manifest" ) ) {
              // skip
            } else if ( ze.getName().equals( "survey.sql" ) ) {
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
            // TopoDroidApp.Log( TopoDroidApp.LOG_ZIP, "Zip filename \"" + pathname + "\"" );
            if ( pathname != null ) {
              FileOutputStream fout = new FileOutputStream( pathname );
              int c;
              while ( ( c = zin.read( buffer ) ) != -1 ) {
                fout.write(buffer, 0, c);
              }
              fout.close();
              if ( sql ) {
                // TopoDroidApp.Log( TopoDroidApp.LOG_ZIP, "Zip sqlfile \"" + pathname + "\"" );
                app.mData.loadFromFile( pathname );
                File f = new File( pathname );
                f.delete();
              }
            }
            zin.closeEntry();
          }
        }
        zin.close();
      }
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
    return ok_manifest;
  }
}


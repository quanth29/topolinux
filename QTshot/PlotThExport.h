/** @file PlotThExport.h
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief plot therion export
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef PLOT_TH_EXPORT_H
#define PLOT_TH_EXPORT_H

#include <string>

// #include <qstring.h>

class Datalist; // forward
struct PlotStatus;


namespace PlotThExport
{
  // private:
    // static int scrap_count;  //!< global scrap counter

    // std::string th2FileName;   //!< th2 file name (for saving)
    // int scrap_nr;              //!< scrap count nr. for this plot
    // std::string scrap_name;    //!< name of the scrap (not save in the status)


  // public:
    // PlotThExport()
      // : scrap_nr ( ++scrap_count )
      // : scrap_name( "scrap" )
    // { }

    // void setFileName( const char * filename ) { th2FileName = filename; }

    // const char * getFileName() const { return th2FileName.c_str(); }

    // const char * getScrapName() const { return scrap_name.c_str(); }

    // void setScrapName( const char * name ) 
    // { 
    //   scrap_name = name; 
    //   th2FileName = name;
    //   th2FileName += ".th2";
    //   // FIXME scrap_name.replace( " ", "" );
    // }

    // int getScrapNr() const { return scrap_nr; }

    /** export the plot data as therion th2 file
     */
    void exportTherion( const char * proj, PlotStatus * status, DataList * list );

    void exportImage( PlotStatus * status, DataList * list );

};

#endif

/** @file CenterlineInfo.h
 *
 * @author marco corvi
 * @date dec 2009
 *
 * @brief centerline date and description
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef CENTERLINE_INFO_H
#define CENTERLINE_INFO_H

#include <string>

#include <qstring.h>

#include "GetDate.h"

/**
 * additional info of the survey
 * used only in the export of the data
 */
struct SurveyInfo
{
  QString name;                  //!< survey name
  QString title;                 //!< survey title
  QString team;                  //!< team(s) string
  QString prefix;                //!< prefix for the names of the stations (compass export)
  double declination;            //!< magnetic declination [degrees]
  bool single_survey;            //!< export compass as a single survey
  std::string centerlineCommand; //!< commands to insert in the centerline block (therion export)
  std::string surveyCommand;     //!< commands to insert in the survey block (therion export)

  SurveyInfo()
    : declination( 0.0 )
    , single_survey( true )
  { }
};

struct CenterlineInfo
{
  int year, month, day;    //!< centerline date (yyyy-mm-dd)
  std::string description; //!< centerline description
  QString fileName;        //!< centerline TLX filename
  QString surveyComment;   //!< comment for the TLX file
  QString exportName;      //!< export filename
  SurveyInfo surveyInfo;   //!< export survey info

  CenterlineInfo()
  {
    GetDate( &day, &month, &year );
  }

};

#endif


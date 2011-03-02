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

#define DECLINATION_UNDEF 360.0

/**
 * additional info of the survey
 * used only in the export of the data
 */
struct SurveyInfo
{
  QString exportName;            //!< export filename
  QString name;                  //!< survey name
  QString title;                 //!< survey title
  QString team;                  //!< team(s) string
  double declination;            //!< magnetic declination [degrees]
  QString compassPrefix;         //!< prefix for the names of the stations (compass export)
  bool compassSingleSurvey;      //!< export compass as a single survey
  std::string therionCenterlineCommand; //!< commands to insert in the centerline block (therion export)
  std::string therionSurveyCommand;     //!< commands to insert in the survey block (therion export)
  bool therionThconfig;          //!< whether to write the thconfig file as well

  SurveyInfo()
    : declination( DECLINATION_UNDEF  ) 
    , compassSingleSurvey( true )
    , therionThconfig( true )
  { }
};

struct CenterlineInfo
{
  int year, month, day;    //!< centerline date (yyyy-mm-dd)
  std::string description; //!< centerline description
  QString fileName;        //!< centerline TLX filename
  QString surveyComment;   //!< comment for the TLX file
  SurveyInfo surveyInfo;   //!< export survey info

  CenterlineInfo()
  {
    GetDate( &day, &month, &year );
  }

};

#endif


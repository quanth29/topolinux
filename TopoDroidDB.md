## TopoDroid 1.0 DB ##

TopoDroid stores the data in a SQLite database. The default location of this file
is in the TopoDroid directory in the "external storage". The user can override this
by specifying a different place for the directory "TopoDroid".

This page describes TopoDroid db v 1.0 (name "distox5.db").


The database contains the following tables
  * surveys: list of surveys
  * shots: the survey shots
  * plots: the metadata of the survey plots
  * fixeds: fixed stations (GPS location)
  * calibs: list of calibrations
  * gms: the calibration data

The table **surveys** contains the following fields
  * id: unique identifier of the survey (integer)
  * name: survey nickname (text, one word)
  * day: survey date (yyyy-mm-dd)
  * team: team members
  * comment: survey description (text)

The table **shots** contains the following fields
  * surveyId: id of the survey to which the shot belongs (integer)
  * id: unique shot identifier (integer)
  * fStation: "from" station
  * tStation: "to" station (blank for splay shots)
  * distance: as tranferred from the DistoX (meters) (real)
  * bearing: as tranferred from the DistoX (decimal degrees) (real)
  * clino: as tranferred from the DistoX (decimal degrees) (real)
  * roll: as tranferred from the DistoX (decimal degrees) (real)
  * status: either 0 (normal) or 1 (deleted)
  * extend: -1 (left), +1 (right), 0 (vertical)
  * flag: 0 (normal shot), 1 (surface shot), 2 (duplicate shot)
  * comment (text)

The table **plots** has the following fields
  * surveyId: id of the survey to which the plot belongs (integer)
  * id: unique plot identifier (integer)
  * name: plot/scrap name (text)
  * type: scrap type, 0 (none), 1 (plan), 2 (extended), 3 (x-section), 4 (horizontal x-section)
  * status: either 0 (normal) or 1 (deleted)
  * start: reference station. The plot data are relative to this station which is the origin of the plot coordinate system.
  * view: viewed station for cross sections, ie, the station you are looking at in the cross-section.

The table **fixeds** contains the following fields
  * surveyId
  * id
  * station: name of the fix station
  * longitude (decimal degrees)
  * latitude (decimal degrees)
  * altitude (meters)
  * comment
  * status: either 0 (normal) or 1 (deleted)

The table **photos** contains
  * surveyId
  * id
  * station: station at which the photo has been taken
  * comment

The table **calibs** has the following fields
  * id: unique identifier (integer)
  * name: calibration name (one word text)
  * day: calibration date (yyyy-mm-dd)
  * device: the DistoX device
  * comment: additional description

The table **gms** has the following fields
  * calibId: id of the calibration to which this data belongs
  * id: unique identifier
  * gx, gy, gz: gravity field components
  * mx, my, mz: magnetic field components
  * grp: group to which the data belongs (integer). Data with group "0" or negative are not used to compute the calibration
  * error: error of this data in the computation of the calibration.
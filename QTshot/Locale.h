/** @file Locale.h
 * 
 * @author marco corvi
 * @date jan 2010
 * 
 * @brief localization
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef LOCALE_H
#define LOCALE_H
#include <stdio.h>

#include <QLocale>

class Locale
{
  private:
    static QLocale the_locale;

  public:
    static QLocale Get() { return the_locale; }

    static void SetLocale( const char * locale )
    {
      the_locale = QLocale( locale );
    }

    static QString ToString( double d, int prec = 4 )
    {
      return the_locale.toString(d, 'f', prec ); 
    }

    static QString ToString( int i )
    {
      return the_locale.toString( i );
    }

    static double ToDouble( const char * str )
    {
      return the_locale.toDouble( str );
    }

    static int ToInt( const char * str )
    {
      return the_locale.toInt( str );
    }

    /** 
     * @param s   separator [default '.']
     */
    static void ToDate( char * date, int y, int m, int d, char s = '.' )
    {
      sprintf(date, "%04d%c%02d%c%02d", y, s, m, s, d);
    }

    static void FromDate( const char * date, int & y, int & m, int & d )
    {
      y = (date[0] - '0') * 1000 + (date[1] - '0') * 100
        + (date[2] - '0') * 10   + (date[3] - '0');
      m = (date[5] - '0') * 10   + (date[6] - '0');
      d = (date[8] - '0') * 10   + (date[9] - '0');
    }

};



#endif

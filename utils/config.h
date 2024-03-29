/** @file config.h
 *
 * @author marco corvi
 * @date june 2009
 *
 * @brief stripped down configuration strings manager
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef CONFIG_H
#define CONFIG_H

#include <stdio.h>
#include <stdlib.h>

#include <string>
#include <map>


class Config
{
  private:
    static Config * the_config;
    std::map< std::string, std::string > m_param; //!< map of params

    /** create a config with default configuration
     */
    Config();

  public:
    static void Release()
    {
      if ( the_config ) delete the_config;
    }

    static Config & Get()
    {
      if ( the_config == NULL ) {
        the_config = new Config();
        atexit( Config::Release );
      }
      return *the_config;
    }

    /** load a configuration file
     * @param filename configuration filename
     * @return true if loaded ok
     */
    bool Load( const char * filename );

    const char * operator() ( const char * key ) 
    {
      std::map< std::string, std::string >::const_iterator it = m_param.find( key );
      if ( it == m_param.end() ) return ""; // default empty string
      return (it->second.c_str());
    }

    const char * Value( const char * key ) 
    {
      return operator() (key);
    }

    /** check if the value of a key is true or false
     * @param key   key
     * @return true if the key starts with one of: 1, y, Y, t, T
     */
    bool isTrue( const char * key )
    {
      std::map< std::string, std::string >::const_iterator it = m_param.find( key );
      if ( it == m_param.end() ) return false;
      if ( it->second.empty() ) return false;
      return ( it->second[0] == '1' || it->second[0] == 'y' || it->second[0] == 'Y' ||
               it->second[0] == 't' || it->second[0] == 'T' );
    }

  private:
    void LoadDefaults();
};


#endif

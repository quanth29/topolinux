/** @file Language.h
 *
 * @author marco corvi
 * @date dec 2009
 *
 * @brief has table of strings
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef LANGUAGE_H
#define LANGUAGE_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <string>
#include <map>

struct Idiom
{
  const char * key; // N.B. keys are stored on the stack in the .cpp
  std::string value;
  Idiom * next;

  Idiom( const char * k, const char * v ) 
    : key( k )
    , value( v )
    , next( NULL )
  { }

  bool equal( const char * k ) { return strcmp( key, k ) == 0; }

  bool before( const char * k ) { return strcmp( key, k ) < 0; }

  bool after( const char * k ) { return strcmp( key, k ) > 0; }

};

class Language
{
  private:
    Idiom * words;
    static Language * the_lexicon;

  private:
    void Insert( const char * k, const char * v );
    void Replace( const char * k, const char * v );
   
    /** cstr (default [en] lexicon)
     */
    Language();

  public:
    static void  Release()
    {
      if ( the_lexicon ) delete the_lexicon;
    }

    /** get the singleton
     * @return pointer to the singleton lexicon
     */
    static Language & Get() 
    {
      if ( the_lexicon == NULL ) {
        the_lexicon = new Language();
        atexit( Language::Release );
      }
      return *the_lexicon;
    }

    /** initialize lexicon
     * @param filename file with the list of kay-value strings
     */
    void init( const char * filename );

    const char * operator() ( const char * k )
    {
      Idiom * w = words;
      while ( w != NULL && ! w->equal( k ) ) w = w->next;
      if ( w == NULL ) {
        fprintf(stderr, "***** LANGUAGE: get key not found %s\n", k );
        return NULL;
      } 
      return w->value.c_str();
    }

    const char * get( const char * k ) { return operator()(k); }
};

#endif

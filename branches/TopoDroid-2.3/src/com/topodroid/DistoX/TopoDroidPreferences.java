/* @file TopoDroidPreferences.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid options dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120516 length and angle units
 * 20120521 converted from DistoXPreferences.java
 * 20120715 loading only per-category preferences
 */
package com.topodroid.DistoX;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.CheckBoxPreference;
// import android.preference.EditTextPreference;
// import android.preference.ListPreference;
// import android.view.Menu;
// import android.view.MenuItem;

/**
 */
public class TopoDroidPreferences extends PreferenceActivity 
{
  static final String PREF_CATEGORY = "PrefCategory";
  static final int PREF_CATEGORY_ALL    = 0;
  static final int PREF_CATEGORY_SURVEY = 1;
  static final int PREF_CATEGORY_PLOT   = 2;
  static final int PREF_CATEGORY_CALIB  = 3;
  static final int PREF_CATEGORY_DEVICE = 4;
  static final int PREF_CATEGORY_SKETCH = 5;
  static final int PREF_CATEGORY_LOG    = 6; // this must be the last
  private int mPrefCategory = PREF_CATEGORY_ALL; // preference category

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    Bundle extras = getIntent().getExtras();
    if ( extras != null ) {
      mPrefCategory = extras.getInt( TopoDroidPreferences.PREF_CATEGORY );
      if ( mPrefCategory < PREF_CATEGORY_ALL || mPrefCategory > PREF_CATEGORY_LOG ) {
        mPrefCategory = PREF_CATEGORY_ALL;
      }
    }

    if (mPrefCategory == PREF_CATEGORY_SURVEY ) {
      addPreferencesFromResource(R.xml.preferences_survey);
    } else if (mPrefCategory == PREF_CATEGORY_PLOT ) {
      addPreferencesFromResource(R.xml.preferences_plot);
    } else if (mPrefCategory == PREF_CATEGORY_CALIB ) {
      addPreferencesFromResource(R.xml.preferences_calib);
    } else if (mPrefCategory == PREF_CATEGORY_DEVICE ) {
      addPreferencesFromResource(R.xml.preferences_device);
    } else if (mPrefCategory == PREF_CATEGORY_LOG ) {
      addPreferencesFromResource(R.xml.preferences_log);
    } else if (mPrefCategory == PREF_CATEGORY_SKETCH ) {
      addPreferencesFromResource(R.xml.preferences_sketch);
    } else {
      addPreferencesFromResource(R.xml.preferences);
    }

  }

}
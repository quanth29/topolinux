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
package com.android.DistoX;

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
  // private MyEditPreference mCloseDistance;
  // private MyEditPreference mVThreshold;
  // private MyEditPreference mLineSegment;
  // private MyEditPreference mLineAccuracy;
  // private MyListPreference mExportType;
  // private MyListPreference mLineStyle;
  // private MyEditPreference mGroupDistance;
  // private MyEditPreference mCalibEps;
  // private MyEditPreference mCalibMaxIt;
  // private MyEditPreference mDeviceName;
  // // private CheckBoxPreference mSaveOnDestroy;
  // private CheckBoxPreference mCheckBT;
  // private CheckBoxPreference mListRefresh;
  // private MyListPreference mUnitLength;
  // private MyListPreference mUnitAngle;
  // private MyListPreference mSockType;

  static final String PREF_CATEGORY = "PrefCategory";
  static final int PREF_CATEGORY_ALL    = 0;
  static final int PREF_CATEGORY_SURVEY = 1;
  static final int PREF_CATEGORY_PLOT   = 2;
  static final int PREF_CATEGORY_CALIB  = 3;
  static final int PREF_CATEGORY_DEVICE = 4;
  static final int PREF_CATEGORY_LOG    = 5;
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
    } else {
      addPreferencesFromResource(R.xml.preferences);
    }

    // if ( mPrefCategory == PREF_CATEGORY_ALL || mPrefCategory == PREF_CATEGORY_SURVEY ) {
    //   mCloseDistance = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[0] );
    //   mVThreshold    = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[8] );
    //   mExportType    = (MyListPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[1] );
    //   mListRefresh   = (CheckBoxPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[15] );
    //   mUnitLength    = (MyListPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[16] );
    //   mUnitAngle     = (MyListPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[17] );
    // }
    // if ( mPrefCategory == PREF_CATEGORY_ALL || mPrefCategory == PREF_CATEGORY_PLOT ) {
    //   mLineSegment   = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[7] );
    //   mLineAccuracy  = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[9] );
    //   mLineStyle     = (MyListPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[11] );
    // }
    // if ( mPrefCategory == PREF_CATEGORY_ALL || mPrefCategory == PREF_CATEGORY_CALIB ) {
    //   mGroupDistance = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[2] );
    //   mCalibEps      = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[3] );
    //   mCalibMaxIt    = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[4] );
    // }
    // if ( mPrefCategory == PREF_CATEGORY_ALL || mPrefCategory == PREF_CATEGORY_DEVICE ) {
    //   mDeviceName    = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[5] );
    //   // mSaveOnDestroy = (CheckBoxPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[6] );
    //   mCheckBT       = (CheckBoxPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[12] );
    //   mSockType      = (MyListPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[21] );
    // }

  }

}

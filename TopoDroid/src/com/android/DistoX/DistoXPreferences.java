/* @file DistoXPreferences.java
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
 */
package com.android.DistoX;

// import android.util.Log;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.CheckBoxPreference;
// import android.preference.EditTextPreference;
// import android.preference.ListPreference;
import android.view.Menu;
import android.view.MenuItem;

/**
 */
public class DistoXPreferences extends PreferenceActivity 
{
  // private static final String TAG = "DistoX";

  private MyEditPreference mCloseDistance;
  private MyEditPreference mVThreshold;
  private MyEditPreference mLineSegment;
  private MyEditPreference mLineAccuracy;
  private MyListPreference mExportType;
  private MyListPreference mLineStyle;
  private MyEditPreference mGroupDistance;
  private MyEditPreference mCalibEps;
  private MyEditPreference mCalibMaxIt;
  private MyEditPreference mDeviceName;
  // private CheckBoxPreference mSaveOnDestroy;
  private CheckBoxPreference mCheckBT;
  private CheckBoxPreference mListRefresh;
  private MyListPreference mUnitLength;
  private MyListPreference mUnitAngle;


  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    addPreferencesFromResource(R.xml.preferences);

    mCloseDistance = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[0] );
    mVThreshold    = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[8] );
    mLineSegment   = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[7] );
    mLineAccuracy  = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[9] );
    mLineStyle     = (MyListPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[11] );
    mExportType    = (MyListPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[1] );
    mGroupDistance = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[2] );
    mCalibEps      = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[3] );
    mCalibMaxIt    = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[4] );
    mDeviceName    = (MyEditPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[5] );
    // mSaveOnDestroy = (CheckBoxPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[6] );
    mCheckBT       = (CheckBoxPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[12] );
    mListRefresh   = (CheckBoxPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[15] );
    mUnitLength    = (MyListPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[16] );
    mUnitAngle     = (MyListPreference)getPreferenceScreen().findPreference( TopoDroidApp.key[17] );
  }

}

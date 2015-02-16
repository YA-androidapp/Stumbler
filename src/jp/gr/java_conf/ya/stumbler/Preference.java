package jp.gr.java_conf.ya.stumbler; // Copyright (c) 2012-2015 YA <ya.androidapp@gmail.com> All rights reserved.

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preference extends PreferenceActivity {
	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
	}
}

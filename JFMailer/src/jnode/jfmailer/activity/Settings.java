package jnode.jfmailer.activity;

import jnode.jfmailer.R;
import jnode.jfmailer.conf.Configuration;
import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.widget.EditText;

public class Settings extends Activity {
	private SharedPreferences prefs;
	private static final String[] field = new String[] { Configuration.SYSOP,
			Configuration.LOCATION, Configuration.LOCAL, Configuration.REMOTE,
			Configuration.REMOTE_HOST, Configuration.REMOTE_PORT,
			Configuration.REMOTE_PASW };
	private static final int[] viewid = new int[] { R.id.sysop_text,
			R.id.location_text, R.id.local_text, R.id.remote_text,
			R.id.remoteho_text, R.id.remotepo_text, R.id.remotepw_text };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		prefs = getSharedPreferences(Configuration.PREFS_NAME, 0);
	}

	@Override
	protected void onStart() {
		loadSettings();
		super.onStart();
	}

	private void saveSettings() {
		Editor edit = prefs.edit();
		for (int i = 0; i < viewid.length; i++) {
			Editable text = ((EditText) findViewById(viewid[i])).getText();
			if (text != null && text.length() > 0) {
				edit.putString(field[i], text.toString());
			}
		}
		edit.commit();
	}

	private void loadSettings() {
		for (int i = 0; i < field.length; i++) {
			String value = prefs.getString(field[i], null);
			if (value != null) {
				((EditText) findViewById(viewid[i])).setText(value);
			}
		}
	}

	@Override
	public void onBackPressed() {
		saveSettings();
		super.onBackPressed();
	}
}

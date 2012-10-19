package jnode.jfmailer.activity;

import jnode.ftn.types.FtnAddress;
import jnode.jfmailer.R;
import jnode.jfmailer.conf.Configuration;
import jnode.jfmailer.log.Logger;
import jnode.jfmailer.thread.Poll;

import jnode.protocol.io.exception.ProtocolException;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

public class Main extends Activity {
	private SharedPreferences prefs;
	private Button poll;
	private Button settings;
	private TextView log;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		log = (TextView) findViewById(R.id.log);
		poll = (Button) findViewById(R.id.poll);
		settings = (Button) findViewById(R.id.settings);
		poll.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				try {
					initPerms();
					new Poll().start();
					synchronized (Poll.class) {
						Poll.class.wait();
					}
				} catch (ProtocolException e) {
					Logger.log("Config error: " + e.getMessage());
				} catch (InterruptedException e) {
				}
				log.setText(Logger.getLog());
			}
		});
		settings.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				settings();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {

		case R.id.about:
			about();
			break;
		case R.id.exit:
			System.exit(0);

		default:
			break;
		}
		return true;
	}

	/**
	 * Проверяем значения
	 * 
	 * @throws ProtocolException
	 */
	private void initPerms() throws ProtocolException {
		prefs = getSharedPreferences(Configuration.PREFS_NAME, 0);
		String value;
		value = prefs.getString(Configuration.SYSOP, null);
		if (value != null) {
			Configuration.INSTANSE.setSysop(value);
		}
		value = prefs.getString(Configuration.LOCATION, null);
		if (value != null) {
			Configuration.INSTANSE.setLocation(value);
		}
		value = prefs.getString(Configuration.LOCAL, null);
		if (value != null) {
			try {
				FtnAddress ftn = new FtnAddress(value);
				Configuration.INSTANSE.setLocal(ftn);
			} catch (NumberFormatException e) {
				throw new ProtocolException("Bad Our AKA: " + value);
			}
		}
		value = prefs.getString(Configuration.REMOTE, null);
		if (value != null) {
			try {
				FtnAddress ftn = new FtnAddress(value);
				Configuration.INSTANSE.setRemote(ftn);
			} catch (NumberFormatException e) {
				throw new ProtocolException("Bad Remote AKA: " + value);
			}
		}
		value = prefs.getString(Configuration.REMOTE_HOST, null);
		if (value != null) {
			Configuration.INSTANSE.setRemoteHost(value);
		}
		value = prefs.getString(Configuration.REMOTE_PORT, null);
		if (value != null) {
			try {
				Integer port = Integer.valueOf(value);
				if (port <= 0 || port >= 65535) {
					throw new ProtocolException(
							"Port is a number between 0 and 65535, not "
									+ value);
				}
			} catch (NumberFormatException e) {
				throw new ProtocolException("Bad port " + value);
			}
		}
		value = prefs.getString(Configuration.REMOTE_PASW, null);
		if (value != null) {
			Configuration.INSTANSE.setPassword(value);
		} else {
			Configuration.INSTANSE.setPassword("-");
		}
	}

	private void about() {
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final PopupWindow pw = new PopupWindow(inflater.inflate(
				R.layout.activity_about, null, false), 1000, 1000, true);
		pw.showAtLocation(this.findViewById(R.id.main_layout), Gravity.CENTER,
				0, 0);
		pw.getContentView().setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				pw.dismiss();

			}
		});
	}

	private void settings() {
		startActivity(new Intent(this, Settings.class));
		log.setText(Logger.getLog());
	}

}

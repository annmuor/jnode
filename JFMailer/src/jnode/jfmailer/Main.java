package jnode.jfmailer;

import jnode.ftn.types.FtnAddress;
import jnode.protocol.binkp.BinkpConnector;
import jnode.protocol.io.Connector;
import jnode.protocol.io.exception.ProtocolException;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class Main extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final Button button = (Button) findViewById(R.id.refrbutton);
		button.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				try {
					Connector conn = new Connector(new BinkpConnector());
					conn.connect(new FtnAddress("2:5020/848"), "fidonode.in", 24554);
				} catch (ProtocolException e) {
					e.printStackTrace();
				}
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}

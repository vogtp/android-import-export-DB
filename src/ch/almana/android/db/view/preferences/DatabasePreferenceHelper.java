package ch.almana.android.db.view.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import ch.almana.android.db.R;
import ch.almana.android.logdebug.Debug;

public class DatabasePreferenceHelper {

	public static void addDatabasePreference(final PreferenceActivity prefAct, final String dbName) {

		if (Debug.isUnsinedPackage(prefAct)) {
			prefAct.addPreferencesFromResource(R.xml.debug_db_preferences);

			prefAct.findPreference("prefKeyShowDB").setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					openDbBrowser(prefAct, dbName);
					return false;
				}
			});
		}


	}

	private static void openDbBrowser(PreferenceActivity prefAct, String dbName) {
		try {
			installAppIfNotInstalled(prefAct, "oliver.ehrenmueller.dbadmin");

			File f = prefAct.getApplication().getDatabasePath(dbName);
			File d = new File(Environment.getExternalStorageDirectory() + "/" + dbName);
			try {
				copyFile(f, d);
				Intent i = new Intent(Intent.ACTION_EDIT);
				i.setData(Uri.parse("sqlite:" + d.getAbsolutePath()));
				prefAct.startActivity(i);
			} catch (IOException e) {
				Toast.makeText(prefAct, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		} catch (Exception e1) {
		}
	}

	static void copyFile(File src, File dst) throws IOException {
		FileInputStream in = new FileInputStream(src);
		FileOutputStream out = new FileOutputStream(dst);
		FileChannel inChannel = in.getChannel();
		FileChannel outChannel = out.getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
	}

	private static void installAppIfNotInstalled(PreferenceActivity prefAct, String packageName) throws Exception {
		try {
			ApplicationInfo info = prefAct.getPackageManager().
					getApplicationInfo(packageName, 0);
			return;
		} catch (PackageManager.NameNotFoundException e) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			Uri fromParts = Uri.parse("market://search?q=pname:" + packageName);
			i.setData(fromParts);
			prefAct.startActivity(i);
			throw new Exception("installing");
		}
	}
}

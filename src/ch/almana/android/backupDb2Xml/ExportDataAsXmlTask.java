package ch.almana.android.backupDb2Xml;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class ExportDataAsXmlTask extends AsyncTask<String, Void, String> {

	private final Activity activity;
	private final ProgressDialog dialog;
	private final String directory;
	private SQLiteDatabase db;

	// hide
	private ExportDataAsXmlTask() {
		super();
		this.activity = null;
		this.directory = null;
		this.dialog = null;
	}

	public ExportDataAsXmlTask(Activity activity, SQLiteDatabase db, String saveDirectory) {
		super();
		this.activity = activity;
		this.db = db;
		this.directory = saveDirectory;
		this.dialog = new ProgressDialog(activity);
	}

	// can use UI thread here
	@Override
	protected void onPreExecute() {
		this.getDialog().setMessage("Exporting database as XML...");
		this.getDialog().show();
	}

	// automatically done on worker thread (separate from UI thread)
	@Override
	protected String doInBackground(final String... args) {
		DataXmlExporter dm = new DataXmlExporter(db, directory);
		for (int i = 0; i < args.length; i++) {

			try {
				String dbName = args[i];
				dm.export(dbName);
			} catch (IOException e) {
				Log.e(DataXmlExporter.LOG_TAG, e.getMessage(), e);
				return e.getMessage();
			}
		}
		return null;
	}

	// can use UI thread here
	@Override
	protected void onPostExecute(final String errMsg) {
		if (this.getDialog().isShowing()) {
			this.getDialog().dismiss();
		}
		if (errMsg == null) {
			Toast.makeText(activity, "Export successful!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(activity, "Export failed - " + errMsg, Toast.LENGTH_SHORT).show();
		}
	}

	public ProgressDialog getDialog() {
		return dialog;
	}
}

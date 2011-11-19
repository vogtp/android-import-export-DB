package ch.almana.android.importexportdb;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import ch.almana.android.importexportdb.exporter.DataExporter;
import ch.almana.android.importexportdb.exporter.DataJsonExporter;
import ch.almana.android.importexportdb.exporter.DataXmlExporter;

public class ExportDataTask extends AsyncTask<String, Void, Boolean> {

	private static final String LOG_TAG = "ExportDataTask";
	private final Context ctx;
	private final ProgressDialog dialog;
	private final File directory;
	private SQLiteDatabase db;
	private ExportType exportType;
	private Object errMsg;
	private BackupRestoreCallback cb;

	public enum ExportType {
		JSON, XML;
	}

	// hide
	@SuppressWarnings("unused")
	private ExportDataTask() {
		super();
		this.ctx = null;
		this.directory = null;
		this.dialog = null;
	}

	public ExportDataTask(BackupRestoreCallback cb, SQLiteDatabase db, File saveDirectory, ExportType exportType) {
		super();
		this.cb = cb;
		this.ctx = cb.getContext();
		this.db = db;
		this.directory = saveDirectory;
		if (ctx == ctx.getApplicationContext()) {
			this.dialog = null;
		} else {
			this.dialog = new ProgressDialog(ctx);
		}
		this.exportType = exportType;
	}

	// can use UI thread here
	@Override
	protected void onPreExecute() {
		if (dialog != null) {
			try {
			this.dialog.setMessage("Exporting database...");
			this.dialog.show();
			} catch (Throwable e) {
				Log.i(DataExporter.LOG_TAG, "Where did our window go?", e);
			}
		}
	}

	// automatically done on worker thread (separate from UI thread)
	@Override
	protected Boolean doInBackground(final String... args) {
		DataExporter dm;
		switch (exportType) {
		case JSON:
			dm = new DataJsonExporter(db, directory);
			break;
		case XML:
			dm = new DataXmlExporter(db, directory);
			break;

		default:
			dm = new DataJsonExporter(db, directory);
			break;
		}
		for (int i = 0; i < args.length; i++) {

			try {
				String dbName = args[i];
				dm.export(dbName);
			} catch (Exception e) {
				Log.e(DataXmlExporter.LOG_TAG, e.getMessage(), e);
				errMsg = e.getMessage();
				return false;
			} finally {
				if (db.isOpen()) {
					db.close();
				}
			}
		}
		return true;
	}

	// can use UI thread here
	@Override
	protected void onPostExecute(final Boolean success) {
		if (dialog != null) {
			try {
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				cb.hasFinished(success);
				if (errMsg == null) {
					Toast.makeText(ctx, "Export successful!", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(ctx, "Export failed - " + errMsg, Toast.LENGTH_SHORT).show();
				}
			} catch (Throwable e) {
				Log.w(LOG_TAG, "Export callback not attachted anymore...", e);
			}
		}
	}

	public ProgressDialog getDialog() {
		return dialog;
	}
}

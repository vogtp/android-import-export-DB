package ch.almana.android.importexportdb;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import ch.almana.android.importexportdb.exporter.DataExporter;
import ch.almana.android.importexportdb.exporter.DataJsonExporter;
import ch.almana.android.importexportdb.exporter.DataXmlExporter;

public class ExportDataTask extends AsyncTask<ExportConfig, Void, Boolean> {

	private static final String LOG_TAG = "ExportDataTask";
	private final Context ctx;
	private final ProgressDialog dialog;
	private Object errMsg;
	private BackupRestoreCallback cb;

	// hide
	@SuppressWarnings("unused")
	private ExportDataTask() {
		super();
		this.ctx = null;
		this.dialog = null;
	}

	public ExportDataTask(BackupRestoreCallback cb) {
		super();
		this.cb = cb;
		this.ctx = cb.getContext();
		if (ctx == ctx.getApplicationContext()) {
			this.dialog = null;
		} else {
			this.dialog = new ProgressDialog(ctx);
		}
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
	protected Boolean doInBackground(final ExportConfig... args) {
		for (int i = 0; i < args.length; i++) {
			ExportConfig config = args[i];
			DataExporter dm = null;
			try {
				switch (config.exportType) {
				case JSON:
					dm = new DataJsonExporter(config.db, config.directory);
					break;
				case XML:
					dm = new DataXmlExporter(config.db, config.directory);
					break;

				default:
					dm = new DataJsonExporter(config.db, config.directory);
					break;
				}
				dm.export(config);
			} catch (Exception e) {
				Log.e(DataXmlExporter.LOG_TAG, e.getMessage(), e);
				errMsg = e.getMessage();
				return false;
			} finally {
				if (dm != null) {
					dm.closeDb();
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

package ch.almana.android.importexportdb.importer;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import ch.almana.android.importexportdb.BackupRestoreCallback;
import ch.almana.android.importexportdb.R;
import ch.almana.android.importexportdb.exporter.DataExporter;
import ch.almana.android.importexportdb.exporter.DataXmlExporter;
import ch.almana.android.importexportdb.helper.ProgressCallback;

public class ImportDataTask extends AsyncTask<ImportConfig, String, Boolean> implements ProgressCallback {

	private static final String LOG_TAG = "ExportDataTask";
	private final Context ctx;
	private final ProgressDialog dialog;
	private Object errMsg;
	private BackupRestoreCallback cb;

	// hide
	@SuppressWarnings("unused")
	private ImportDataTask() {
		super();
		this.ctx = null;
		this.dialog = null;
	}

	public ImportDataTask(BackupRestoreCallback cb) {
		super();
		this.cb = cb;
		this.ctx = cb.getContext();
		if (ctx == ctx.getApplicationContext()) {
			this.dialog = null;
		} else {
			this.dialog = new ProgressDialog(ctx);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
	}

	// can use UI thread here
	@Override
	protected void onPreExecute() {
		if (dialog != null) {
			try {
				this.dialog.setMessage(ctx.getString(R.string.msg_import));
			this.dialog.show();
			} catch (Throwable e) {
				Log.i(DataExporter.LOG_TAG, "Where did our window go?", e);
			}
		}
	}

	// automatically done on worker thread (separate from UI thread)
	@Override
	protected Boolean doInBackground(final ImportConfig... args) {
		ContentResolver resolver = cb.getContext().getContentResolver();
		for (int i = 0; i < args.length; i++) {
			ImportConfig config = args[i];
			DataJsonImporter dm = null;
			try {
				dm = new DataJsonImporter(config.getDatabaseName(), config.directory);
				int count = 1;
				int max = config.tables.size();
				for (String tableName : config.tables.keySet()) {
					publishProgress(new String[] { count++ + "/" + max });
					ProgressCallback callback = dialog != null ? this : null;
					dm.restoreTable(resolver, config.tables.get(tableName), tableName, callback);
				}
			} catch (Exception e) {
				Log.e(DataXmlExporter.LOG_TAG, e.getMessage(), e);
				errMsg = e.getMessage();
				return false;
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
					Toast.makeText(ctx, R.string.msg_import_successful, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(ctx, ctx.getString(R.string.msg_import_failed) + errMsg, Toast.LENGTH_SHORT).show();
				}
			} catch (Throwable e) {
				Log.w(LOG_TAG, "Import callback not attachted anymore...", e);
			}
		}
	}

	public ProgressDialog getDialog() {
		return dialog;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		if (dialog != null && values.length > 0 && values[0] != null) {
			dialog.setMessage(ctx.getString(R.string.msg_import) + ": " + values[0]);
		} else {
			dialog.setMessage(ctx.getString(R.string.msg_import));
		}
	}

	@Override
	public void setMaxProgress(int max) {
		if (dialog != null) {
			dialog.setMax(max);
		}
	}

	@Override
	public void setProgress(int p) {
		if (dialog != null) {
			dialog.setProgress(p);
		}
	}
}

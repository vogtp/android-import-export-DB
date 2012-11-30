package ch.almana.android.db.importexport.exporter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import ch.almana.android.db.R;
import ch.almana.android.db.importexport.BackupRestoreCallback;
import ch.almana.android.db.importexport.helper.ProgressCallback;

public class ExportDataTask extends AsyncTask<ExportConfig, String, Boolean> implements ProgressCallback {

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
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
	}

	// can use UI thread here
	@Override
	protected void onPreExecute() {
		if (dialog != null) {
			try {
				this.dialog.setMessage(ctx.getString(R.string.msg_exporting_table));
			this.dialog.show();
			} catch (Throwable e) {
				Log.i(DataExporter.LOG_TAG, "Where did our window go?", e);
			}
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		if (dialog != null && values.length > 0 && values[0] != null) {
			dialog.setMessage(ctx.getString(R.string.msg_exporting_table) + ": " + values[0]);
		} else {
			dialog.setMessage(ctx.getString(R.string.msg_exporting_table));
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
				ProgressCallback callback = dialog != null ? this : null;
				dm.export(config, callback);
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
					Toast.makeText(ctx, R.string.msg_export_successful, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(ctx, ctx.getString(R.string.msg_export_failed) + errMsg, Toast.LENGTH_SHORT).show();
				}
			} catch (Throwable e) {
				Log.w(LOG_TAG, "Export callback not attachted anymore...", e);
			}
		}
	}


	public ProgressDialog getDialog() {
		return dialog;
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

	@Override
	public void SetProgressMessage(String msg) {
		publishProgress(new String[] { msg });
	}
}

package ch.almana.android.backupDb.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

public abstract class DataExporter {

	public static final String LOG_TAG = "DataExporter";

	protected final SQLiteDatabase db;
	protected final String directory;

	public DataExporter(SQLiteDatabase db, String directory) {
		this.db = db;
		this.directory = directory;
	}

	public void export(String dbName) throws Exception {
		Log.i(LOG_TAG, "exporting database - " + dbName);

		prepairExport(dbName);

		// get the tables
		String sql = "select * from sqlite_master";
		Cursor c = this.db.rawQuery(sql, new String[0]);
		Log.d(LOG_TAG, "select * from sqlite_master, cur size " + c.getCount());
		while (c.moveToNext()) {
			String tableName = c.getString(c.getColumnIndex("name"));
			Log.d(LOG_TAG, "table name " + tableName);

			// skip metadata, sequence, and uidx (unique indexes)
			if (!tableName.equals("android_metadata") && !tableName.equals("sqlite_sequence") && !tableName.startsWith("uidx") && !tableName.startsWith("idx_")) {
				try {
					this.exportTable(tableName);
				} catch (SQLiteException e) {
					Log.w(LOG_TAG, "Error exporting table " + tableName, e);
				}
			}
		}
		c.close();
		this.writeToFile(getExportAsString(), dbName + ".json");
		Log.i(LOG_TAG, "exporting database complete");
	}

	private void exportTable(final String tableName) throws Exception {
		Log.d(LOG_TAG, "exporting table - " + tableName);

		String sql = "select * from " + tableName;
		Cursor c = this.db.rawQuery(sql, new String[0]);
		startTable(tableName);
		while (c.moveToNext()) {
			startRow();
			String id = c.getString(1);
			if (id == null || TextUtils.isEmpty(id)) {
				id = c.getString(0);
			}
			for (int i = 0; i < c.getColumnCount(); i++) {
				populateRowWithField(c.getColumnName(i), c.getString(i));
			}
			endRow();
		}
		c.close();
		endTable();
	}

	private void writeToFile(String payload, String exportFileName) throws IOException {
		File dir = new File(directory);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, exportFileName);
		file.createNewFile();

		ByteBuffer buff = ByteBuffer.wrap(payload.getBytes());
		FileChannel channel = new FileOutputStream(file).getChannel();
		try {
			channel.write(buff);
		} finally {
			if (channel != null)
				channel.close();
		}
	}

	abstract protected String getExportAsString() throws Exception;

	abstract protected void prepairExport(String dbName) throws Exception;

	abstract protected void endRow() throws Exception;

	abstract protected void populateRowWithField(String columnName, String string) throws Exception;

	abstract protected void startRow() throws Exception;

	abstract protected void endTable() throws Exception;

	abstract protected void startTable(String tableName) throws Exception;

}

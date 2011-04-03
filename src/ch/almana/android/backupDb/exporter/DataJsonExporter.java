package ch.almana.android.backupDb.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.json.JSONArray;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

public class DataJsonExporter extends DataExporter {

	public static final String LOG_TAG = "Db2XmlExporter";

	private final SQLiteDatabase db;
	private JSONObject jsonRoot;
	private JSONObject jsonDB;

	private final String directory;

	public DataJsonExporter(SQLiteDatabase db, String directory) {
		this.db = db;
		this.directory = directory;
		jsonRoot = new JSONObject();
	}

	public void export(String dbName) throws Exception {
		Log.i(LOG_TAG, "exporting database - " + dbName);
		jsonDB = new JSONObject();
		jsonRoot.put(dbName, jsonDB);

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
		this.writeToFile(jsonRoot.toString(1), dbName + ".json");
		Log.i(LOG_TAG, "exporting database complete");
	}

	private void exportTable(final String tableName) throws Exception {
		Log.d(LOG_TAG, "exporting table - " + tableName);

		String sql = "select * from " + tableName;
		Cursor c = this.db.rawQuery(sql, new String[0]);
		JSONArray table = new JSONArray();
		while (c.moveToNext()) {
			JSONObject row = new JSONObject();
			String id = c.getString(1);
			if (id == null || TextUtils.isEmpty(id)) {
				id = c.getString(0);
			}
			for (int i = 0; i < c.getColumnCount(); i++) {
				row.put(c.getColumnName(i), c.getString(i));
			}
			table.put(row);
		}
		c.close();
		jsonDB.put(tableName, table);
	}

	private void writeToFile(String xmlString, String exportFileName) throws IOException {
		File dir = new File(directory);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, exportFileName);
		file.createNewFile();

		ByteBuffer buff = ByteBuffer.wrap(xmlString.getBytes());
		FileChannel channel = new FileOutputStream(file).getChannel();
		try {
			channel.write(buff);
		} finally {
			if (channel != null)
				channel.close();
		}
	}
}

package ch.almana.android.importexportdb.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;
import ch.almana.android.importexportdb.constants.JsonConstants;
import ch.almana.android.importexportdb.helper.ProgressCallback;

public class DataJsonImporter {

	public static final String LOG_TAG = "DataJsonImporter";

	private JSONObject jsonDb;

	public DataJsonImporter(String dbName, File directory) {
		try {
			File file = new File(directory, dbName + JsonConstants.FILE_NAME);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			parseData(dbName, readFile(dbName, reader));
		} catch (IOException e) {
			Log.e(LOG_TAG, "Cannot read json DB file", e);
			jsonDb = new JSONObject();
		}
	}

	public DataJsonImporter(String DbName, InputStream is) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			parseData(DbName, readFile(DbName, reader));
		} catch (IOException e) {
			Log.e(LOG_TAG, "Cannot read json DB from InputStream", e);
			jsonDb = new JSONObject();
		}
	}

	private void parseData(String DbName, String payload) {
		try {
			jsonDb = (JSONObject) (new JSONObject(payload)).get(DbName);
		} catch (JSONException e) {
			Log.e(LOG_TAG, "Cannot parse as JSON", e);
			jsonDb = new JSONObject();
		}
	}

	private String readFile(String dbName, BufferedReader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	public JSONArray getTables(String tableName) throws JSONException {
		return jsonDb.getJSONArray(tableName);
	}

	public void restoreTable(ContentResolver contentResolver, Uri contentUri, String tableName) throws JSONException {
		restoreTable(contentResolver, contentUri, tableName, null);
	}

	public void restoreTable(ContentResolver contentResolver, Uri contentUri, String tableName, ProgressCallback pcb) throws JSONException {
		JSONArray table = getTables(tableName);
		Log.i(LOG_TAG, "Restoring table " + tableName);
		if (pcb != null) {
			pcb.setMaxProgress(table.length());
		}
		for (int i = 0; i < table.length(); i++) {
			Log.v(LOG_TAG, "Restoring table " + tableName + " row " + i);
			if (pcb != null) {
				pcb.setProgress(i);
			}
			ContentValues values = new ContentValues();
			JSONObject rowJson = table.getJSONObject(i);
			Iterator<String> keys = rowJson.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				values.put(key, rowJson.getString(key));
			}
			try {
				contentResolver.insert(contentUri, values);
			} catch (Throwable e) {
				Log.w(LOG_TAG, "Error restoring row from table " + tableName, e);
			}
		}
		Log.i(LOG_TAG, "Successfully restored table " + tableName);
	}

}

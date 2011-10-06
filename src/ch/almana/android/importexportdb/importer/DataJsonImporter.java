package ch.almana.android.importexportdb.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import ch.almana.android.importexportdb.constants.JsonConstants;

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

}

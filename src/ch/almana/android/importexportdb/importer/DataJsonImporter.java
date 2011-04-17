package ch.almana.android.importexportdb.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import ch.almana.android.importexportdb.constants.JsonConstants;

public class DataJsonImporter {

	public static final String LOG_TAG = "DataJsonImporter";

	private JSONObject jsonDb;

	public DataJsonImporter(String DbName, File directory) {
		try {
			jsonDb = (JSONObject) (new JSONObject(readFile(DbName, directory))).get(DbName);
		} catch (JSONException e) {
			Log.e(LOG_TAG, "Cannot parse as JSON", e);
			jsonDb = new JSONObject();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Cannot read json DB file", e);
			jsonDb = new JSONObject();
		}
	}

	private String readFile(String dbName, File directory) throws IOException {
		File file = new File(directory, dbName + JsonConstants.FILE_NAME);
		BufferedReader reader = new BufferedReader(new FileReader(file));
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

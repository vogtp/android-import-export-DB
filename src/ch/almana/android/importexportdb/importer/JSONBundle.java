package ch.almana.android.importexportdb.importer;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONBundle {

	private static final String LOG_TAG = "JSONBundle";
	private JSONObject jsonObject;

	public JSONBundle(JSONObject jsonObject) {
		super();
		this.jsonObject = jsonObject;
	}

	public long getLong(String name) {
		try {
			return jsonObject.getLong(name);
		} catch (JSONException e) {
			Log.i(LOG_TAG, "Cannot read " + name + " as long", e);
			return 0;
		}
	}

	public String getString(String name) {
		try {
			return jsonObject.getString(name);
		} catch (JSONException e) {
			Log.i(LOG_TAG, "Cannot read " + name + " as string", e);
			return null;
		}
	}

	public int getInt(String name) {
		try {
			return jsonObject.getInt(name);
		} catch (JSONException e) {
			Log.i(LOG_TAG, "Cannot read " + name + " as int", e);
			return 0;
		}

	}

}

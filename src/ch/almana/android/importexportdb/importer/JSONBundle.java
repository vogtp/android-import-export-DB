package ch.almana.android.importexportdb.importer;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONBundle {

	private static final String LOG_TAG = "JSONBundle";
	private final JSONObject jsonObject;

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

	public void putLong(String name, long value) {
		try {
			jsonObject.put(name, value);
		} catch (JSONException e) {
			Log.i(LOG_TAG, "Cannot write " + name + " with long " + value, e);
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

	public void putString(String name, String value) {
		try {
			jsonObject.put(name, value);
		} catch (JSONException e) {
			Log.i(LOG_TAG, "Cannot write " + name + " with string " + value, e);
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

	public void putInt(String name, int value) {
		try {
			jsonObject.put(name, value);
		} catch (JSONException e) {
			Log.i(LOG_TAG, "Cannot write " + name + " with int " + value, e);
		}
	}

}

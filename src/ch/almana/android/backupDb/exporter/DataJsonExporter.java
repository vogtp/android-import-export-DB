package ch.almana.android.backupDb.exporter;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import android.database.sqlite.SQLiteDatabase;

public class DataJsonExporter extends DataExporter {

	private JSONObject jsonRoot;
	private JSONObject jsonDB;
	private JSONObject row;
	private JSONArray table;


	public DataJsonExporter(SQLiteDatabase db, File directory) {
		super(db, directory);
		jsonRoot = new JSONObject();
	}

	@Override
	protected void prepairExport(String dbName) throws Exception {
		jsonDB = new JSONObject();
		jsonRoot.put(dbName, jsonDB);
	}

	@Override
	protected String getExportAsString() throws Exception {
		return jsonRoot.toString(1);
	}

	protected void startTable(String tableName) throws Exception {
		table = new JSONArray();
		jsonDB.put(tableName, table);
	}

	protected void endTable() throws Exception {

	}

	protected void endRow() throws Exception {
		table.put(row);
	}


	protected void populateRowWithField(String columnName, String string) throws Exception {
		row.put(columnName, string);
	}

	protected void startRow() throws Exception {
		row = new JSONObject();
	}




}

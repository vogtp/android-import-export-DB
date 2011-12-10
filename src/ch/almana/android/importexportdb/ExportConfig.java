package ch.almana.android.importexportdb;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import android.database.sqlite.SQLiteDatabase;

public class ExportConfig {

	public enum ExportType {
		JSON, XML;
	}

	SQLiteDatabase db;
	String databaseName;
	File directory;
	ExportType exportType = ExportType.JSON;
	Set<String> excludedTables = null;

	private ExportConfig() {
		super();
	}

	public ExportConfig(SQLiteDatabase db, String databaseName, File directory, ExportType exportType) {
		this();
		this.db = db;
		this.directory = directory;
		this.exportType = exportType;
		this.databaseName = databaseName;
	}

	public void setExcludeTable(String tableName) {
		if (excludedTables == null) {
			excludedTables = new HashSet<String>();
		}
		excludedTables.add(tableName);
	}

	public boolean isExcludeTable(String tableName) {
		if (excludedTables == null) {
			return false;
		}
		return excludedTables.contains(tableName);
	}

	public String getDatabaseName() {
		return databaseName;
	}

}
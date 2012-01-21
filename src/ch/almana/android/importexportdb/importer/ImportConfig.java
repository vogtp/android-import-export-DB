package ch.almana.android.importexportdb.importer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.net.Uri;

public class ImportConfig {

	public enum ImportType {
		JSON, XML_NOT_SUPPORTED;
	}

	String databaseName;
	File directory;
	ImportType type = ImportType.JSON;
	Map<String, Uri> tables = new HashMap<String, Uri>();;

	private ImportConfig() {
		super();
	}

	public ImportConfig(String databaseName, File directory) {
		this();
		this.directory = directory;
		this.databaseName = databaseName;
	}

	public void addTable(String tableName, Uri uri) {
		tables.put(tableName, uri);
	}

	public String getDatabaseName() {
		return databaseName;
	}

}
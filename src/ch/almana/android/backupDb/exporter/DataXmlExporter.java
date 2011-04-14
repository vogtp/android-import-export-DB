package ch.almana.android.backupDb.exporter;

import java.io.File;
import java.io.IOException;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataXmlExporter extends DataExporter {

	private XmlBuilder xmlBuilder;

	public DataXmlExporter(SQLiteDatabase db, File directory) {
		super(db, directory);
	}

	private void exportTable(final String tableName) throws IOException {
		Log.d(LOG_TAG, "exporting table - " + tableName);

		String sql = "select * from " + tableName;
		Cursor c = this.db.rawQuery(sql, new String[0]);
		if (c.moveToFirst()) {
			int cols = c.getColumnCount();
			do {
				for (int i = 0; i < cols; i++) {
				}
			} while (c.moveToNext());
		}
		c.close();

	}

	class XmlBuilder {
		private static final String OPEN_XML_STANZA = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
		private static final String CLOSE_WITH_TICK = "'>";
		private static final String DB_OPEN = "<database name='";
		private static final String DB_CLOSE = "</database>";
		private static final String TABLE_OPEN = "\n<table name='";
		private static final String TABLE_CLOSE = "</table>\n";
		private static final String ROW_OPEN = "<row>\n";
		private static final String ROW_CLOSE = "</row>\n";
		private static final String COL_OPEN = "<col name='";
		private static final String COL_CLOSE = "</col>\n";

		private final StringBuilder sb;

		public XmlBuilder() throws IOException {
			this.sb = new StringBuilder();
		}

		void start(String dbName) {
			this.sb.append(OPEN_XML_STANZA);
			this.sb.append(DB_OPEN + dbName + CLOSE_WITH_TICK);
		}

		String end() throws IOException {
			this.sb.append(DB_CLOSE);
			return this.sb.toString();
		}

		void openTable(String tableName) {
			this.sb.append(TABLE_OPEN + tableName + CLOSE_WITH_TICK);
		}

		void closeTable() {
			this.sb.append(TABLE_CLOSE);
		}

		void openRow() {
			this.sb.append(ROW_OPEN);
		}

		void closeRow() {
			this.sb.append(ROW_CLOSE);
		}

		void addColumn(final String name, final String val) throws IOException {
			this.sb.append(COL_OPEN + name + CLOSE_WITH_TICK + val + COL_CLOSE);
		}
	}

	@Override
	protected void prepairExport(String dbName) throws Exception {
		this.xmlBuilder = new XmlBuilder();
		this.xmlBuilder.start(dbName);

	}

	@Override
	protected String getExportAsString() throws Exception {
		return this.xmlBuilder.end();
	}

	protected void startTable(String tableName) throws Exception {
		this.xmlBuilder.openTable(tableName);
	}

	protected void endTable() throws Exception {
		this.xmlBuilder.closeTable();
	}

	protected void startRow() throws Exception {
		this.xmlBuilder.openRow();
	}

	protected void populateRowWithField(String columnName, String string) throws Exception {
		this.xmlBuilder.addColumn(columnName, string);
	}

	protected void endRow() throws Exception {
		this.xmlBuilder.closeRow();
	}

}

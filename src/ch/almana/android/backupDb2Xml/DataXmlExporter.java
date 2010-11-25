package ch.almana.android.backupDb2Xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class DataXmlExporter {

	public static final String LOG_TAG = "Db2XmlExporter";

	private final SQLiteDatabase db;
	private XmlBuilder xmlBuilder;

	private final String directory;

	public DataXmlExporter(SQLiteDatabase db, String directory) {
		this.db = db;
		this.directory = directory;
	}

	public void export(String dbName) throws IOException {
		Log.i(LOG_TAG, "exporting database - " + dbName);

		this.xmlBuilder = new XmlBuilder();
		this.xmlBuilder.start(dbName);

		// get the tables
		String sql = "select * from sqlite_master";
		Cursor c = this.db.rawQuery(sql, new String[0]);
		Log.d(LOG_TAG, "select * from sqlite_master, cur size " + c.getCount());
		if (c.moveToFirst()) {
			do {
				String tableName = c.getString(c.getColumnIndex("name"));
				Log.d(LOG_TAG, "table name " + tableName);

				// skip metadata, sequence, and uidx (unique indexes)
				if (!tableName.equals("android_metadata") && !tableName.equals("sqlite_sequence")
						&& !tableName.startsWith("uidx") && !tableName.startsWith("idx_")) {
					try {
						this.exportTable(tableName);
					} catch (SQLiteException e) {
						Log.w(LOG_TAG, "Error exporting table " + tableName, e);
					}
				}
			} while (c.moveToNext());
		}
		c.close();
		String xmlString = this.xmlBuilder.end();
		this.writeToFile(xmlString, dbName + ".xml");
		Log.i(LOG_TAG, "exporting database complete");
	}

	private void exportTable(final String tableName) throws IOException {
		Log.d(LOG_TAG, "exporting table - " + tableName);
		this.xmlBuilder.openTable(tableName);
		String sql = "select * from " + tableName;
		Cursor c = this.db.rawQuery(sql, new String[0]);
		if (c.moveToFirst()) {
			int cols = c.getColumnCount();
			do {
				this.xmlBuilder.openRow();
				for (int i = 0; i < cols; i++) {
					this.xmlBuilder.addColumn(c.getColumnName(i), c.getString(i));
				}
				this.xmlBuilder.closeRow();
			} while (c.moveToNext());
		}
		c.close();
		this.xmlBuilder.closeTable();
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

	class XmlBuilder {
		private static final String OPEN_XML_STANZA = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		private static final String CLOSE_WITH_TICK = "'>";
		private static final String DB_OPEN = "<database name='";
		private static final String DB_CLOSE = "</database>";
		private static final String TABLE_OPEN = "<table name='";
		private static final String TABLE_CLOSE = "</table>";
		private static final String ROW_OPEN = "<row>";
		private static final String ROW_CLOSE = "</row>";
		private static final String COL_OPEN = "<col name='";
		private static final String COL_CLOSE = "</col>";

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
}

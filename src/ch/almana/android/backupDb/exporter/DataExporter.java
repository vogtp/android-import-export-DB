package ch.almana.android.backupDb.exporter;

public abstract class DataExporter {

	abstract public void export(String dbName) throws Exception;

}

package ch.almana.android.db.importexport.helper;

public interface ProgressCallback {

	public void SetProgressMessage(String msg);

	public void setMaxProgress(int max);

	public void setProgress(int p);

}

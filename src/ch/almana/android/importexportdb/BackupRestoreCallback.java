package ch.almana.android.importexportdb;

import android.content.Context;

public interface BackupRestoreCallback {
	public Context getContext();

	public void hasFinished(boolean success);

}
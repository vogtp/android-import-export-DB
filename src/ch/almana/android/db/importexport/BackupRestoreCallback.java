package ch.almana.android.db.importexport;

import android.content.Context;

public interface BackupRestoreCallback {
	public Context getContext();

	public void hasFinished(boolean success);

}
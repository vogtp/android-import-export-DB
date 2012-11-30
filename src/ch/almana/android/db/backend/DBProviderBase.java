package ch.almana.android.db.backend;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public abstract class DBProviderBase extends ContentProvider {

	private static final String LOG_TAG = "DBProviderBase";

	private static final int CONTENT = 1;
	private static final int CONTENT_ITEM = 2;

	private UriMatcher uriTableMatcher;

	private UriMatcher uriContentTypeMatcher;
	private static boolean notifyChanges = true;

	private SQLiteOpenHelper openHelper;

	protected abstract SQLiteOpenHelper getOpenHelper();

	protected abstract UriTableMapping[] getUriTableMapping();

	protected abstract String getAuthority();

	@Override
	public boolean onCreate() {
		buildUriMatchers();
		openHelper = getOpenHelper();
		return true;
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		UriTableMapping utm = getUriTableMap(uri);
		SQLiteDatabase db = openHelper.getWritableDatabase();
		switch (uriContentTypeMatcher.match(uri)) {
		case CONTENT:
			count = db.delete(utm.tableName, selection, selectionArgs);
			break;

		case CONTENT_ITEM:
			String id = uri.getPathSegments().get(1);
			count = db.delete(utm.tableName, DBBase.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		notifyChange(uri);
		return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		Uri ret;
		// Validate the requested uri
		if (uriContentTypeMatcher.match(uri) != CONTENT) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		UriTableMapping utm = getUriTableMap(uri);
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		SQLiteDatabase db = openHelper.getWritableDatabase();
		long rowId = db.insert(utm.tableName, DBBase.NAME_ID, values);
		if (rowId > 0) {
			ret = ContentUris.withAppendedId(uri, rowId);
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}

		notifyChange(uri);
		return ret;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		UriTableMapping utm = getUriTableMap(uri);
		qb.setTables(utm.tableName);

		if (utm.specialWhere != null) {
			qb.appendWhere(utm.specialWhere);
		}

		int match = uriContentTypeMatcher.match(uri);
		switch (match) {
		case CONTENT:
			break;

		case CONTENT_ITEM:
			qb.appendWhere(utm.idTableName + DBBase.NAME_ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		qb.setDistinct(utm.distinct);

		// Get the database and run the query
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, utm.groupBy, null, sortOrder);
		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		UriTableMapping utm = getUriTableMap(uri);
		SQLiteDatabase db = openHelper.getWritableDatabase();
		switch (uriContentTypeMatcher.match(uri)) {
		case CONTENT:
			count = db.update(utm.tableName, values, selection, selectionArgs);
			break;

		case CONTENT_ITEM:
			String id = uri.getPathSegments().get(1);
			count = db.update(utm.tableName, values, DBBase.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		notifyChange(uri);
		return count;
	}

	protected void notifyChange(Uri uri) {
		if (notifyChanges) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
	}

	public static boolean isNotifyChanges() {
		return notifyChanges;
	}

	public static void setNotifyChanges(boolean b) {
		notifyChanges = b;
	}

	@Override
	public String getType(Uri uri) {
		UriTableMapping uriTableMapping = getUriTableMap(uri);
		if (uriTableMapping == null) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		switch (uriContentTypeMatcher.match(uri)) {
		case CONTENT:
			return uriTableMapping.contentType;
		case CONTENT_ITEM:
			return uriTableMapping.contentItemType;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	protected UriTableMapping getUriTableMap(Uri uri) {
		int match = uriTableMatcher.match(uri);
		UriTableMapping[] map = getUriTableMapping(); //content://ch.amana.android.cputuner/TimeInStateIndex_DISTINCT
		if (match < 0 || match > map.length - 1) {
			ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException(match);
			Log.e(LOG_TAG, "No uri table machting: ", e);
			throw e;
		}
		return getUriTableMapping()[match];
	}



	private void buildUriMatchers() {
		uriTableMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriContentTypeMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		for (int type = 0; type < getUriTableMapping().length; type++) {
			String contentItemName = getUriTableMapping()[type].contentItemName;
			uriTableMatcher.addURI(getAuthority(), contentItemName, type);
			uriTableMatcher.addURI(getAuthority(), contentItemName + "/#", type);
			uriContentTypeMatcher.addURI(getAuthority(), contentItemName, CONTENT);
			uriContentTypeMatcher.addURI(getAuthority(), contentItemName + "/#", CONTENT_ITEM);
		}

	}

}

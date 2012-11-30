package ch.almana.android.db.backend;


public interface DBBase {

	public static final String SQL_WILDCARD = "%";

	public static final String NAME_ID = "_id";
	public static final int INDEX_ID = 0;

	public static final String[] PROJECTION_ID = new String[] { NAME_ID };
	public static final String SELECTION_BY_ID = NAME_ID + "=?";


}
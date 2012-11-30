package ch.almana.android.db.backend;

import android.net.Uri;

public class UriTableMapping {

		public final Uri contenUri;
		public final String tableName;
		public final String contentItemName;
		public final String contentType;
		public final String contentItemType;
		public boolean notifyOnChange;
		public final String specialWhere;
		public String groupBy;
		public final String idTableName;
		public boolean distinct;

		public UriTableMapping(Uri contenUri, String tableName, String contentItemName, String contentType, String contentItemType, boolean notifyOnChange) {
			this(contenUri, tableName, contentItemName, contentType, contentItemType, notifyOnChange, null, null, "", false);
		}

		public UriTableMapping(Uri contenUri, String tableName, String contentItemName, String contentType, String contentItemType, boolean notifyOnChange, boolean distinct) {
			this(contenUri, tableName, contentItemName, contentType, contentItemType, notifyOnChange, null, null, "", distinct);
		}

		public UriTableMapping(Uri contenUri, String tableName, String contentItemName, String contentType, String contentItemType, boolean notifyOnChange, String specialWhere,
				String groupBy, String idTableName, boolean distinct) {
			super();
			this.contenUri = contenUri;
			this.tableName = tableName;
			this.contentItemName = contentItemName;
			this.contentType = contentType;
			this.contentItemType = contentItemType;
			this.notifyOnChange = notifyOnChange;
			this.specialWhere = specialWhere;
			this.groupBy = groupBy;
			this.idTableName = idTableName;
			this.distinct = distinct;
		}
	}

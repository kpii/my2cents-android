package mobi.my2cents.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class History implements BaseColumns {
	
	public static final String AUTHORITY = "mobi.my2cents";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/history");
	
	public static final String TIME				= "time";
	public static final String GTIN				= "gtin";
	public static final String NAME				= "name";
	public static final String AFFILIATE_NAME	= "affiliate_name";
	public static final String AFFILIATE_URL	= "affiliate_url";
	public static final String IMAGE			= "image";
	
}

package mobi.my2cents.data;

import android.provider.BaseColumns;


public interface TransitionalStateColumns {
	
	
	public static final String TRANSITION_ACTIVE = "transition_active";
	public static final String GET_TRANSITIONAL_STATE =" get_transitional_state ";
	public static final String POST_TRANSITIONAL_STATE =" post_transitional_state ";
	public static final String DEL_TRANSITIONAL_STATE =" del_transitional_state ";
	public static final String PUT_TRANSITIONAL_STATE =" put_transitional_state ";
	
	public static final String SELECTION = TRANSITION_ACTIVE+"=0 AND ( "+
											POST_TRANSITIONAL_STATE+"=1 OR "+
										   	GET_TRANSITIONAL_STATE+"=1 OR "+
										   	PUT_TRANSITIONAL_STATE+"=1 OR "+
										   	DEL_TRANSITIONAL_STATE+"=1 " +
										   ")";
}

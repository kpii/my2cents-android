/* 
 * Copyright (C) 2008 Torgny Bjers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mobi.my2cents.utils;

import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.Date;

/**
 * 
 * @author torgny.bjers
 */
public class RelativeTime {

	private static final int SECOND = 1;
	private static final int MINUTE = 60 * SECOND;
	private static final int HOUR = 60 * MINUTE;
	private static final int DAY = 24 * HOUR;
	private static final int MONTH = 30 * DAY;

	/**
	 * 
	 * @param from
	 * @return String
	 */
	public static String getDifference(long from) {
		String value = new String();
		long to = new Date().getTime();
		long delta = (to - from) / 1000;
		if (delta < 0) {
			value = "just now";
		} else if (delta < 1 * MINUTE) {
			value = delta + " seconds ago";
		} else if (delta < 2 * MINUTE) {
			value = "a minute ago";
		} else if (delta < 59 * MINUTE) {
			value = (delta / MINUTE) + " minutes ago";
		} else if (delta < 90 * MINUTE) {
			value = "an hour ago";
		} else if (delta < 150 * MINUTE) {
			value = "two hours ago";
		} else if (delta < 24 * HOUR) {
			value = (delta / HOUR) + " hours ago";
		} else if (delta < 48 * HOUR) {
			value = "yesterday";
		} else if (delta < 30 * DAY) {
			value = (delta / DAY) + " days ago";
		} else if (delta < 12 * MONTH) {
			MessageFormat form = new MessageFormat("{0}");
			Object[] formArgs = new Object[] { delta / MONTH };
			double[] tweetLimits = { 1, 2 };
			String[] tweetPart = { "one month ago", "{0} months ago" };
			ChoiceFormat tweetForm = new ChoiceFormat(tweetLimits, tweetPart);
			form.setFormatByArgumentIndex(0, tweetForm);
			value = form.format(formArgs);
		}
		return value;
	}
}

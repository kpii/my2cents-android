/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package twitter4j.api;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Trends;
import twitter4j.TwitterException;

import java.util.Date;
import java.util.List;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface SearchMethods
{
	/**
	 * Returns tweets that match a specified query.
	 * <br>This method calls http://search.twitter.com/search
	 * @param query - the search condition
	 * @return the result
	 * @throws TwitterException when Twitter service or network is unavailable
	 * @since Twitter4J 1.1.7
	 * @see <a href="http://apiwiki.twitter.com/Twitter-Search-API-Method%3A-search">Twitter API Wiki / Twitter Search API Method: search</a>
	 */
	QueryResult search(Query query)
			throws TwitterException;

	/**
	 * Returns the top ten topics that are currently trending on Twitter.  The response includes the time of the request, the name of each trend, and the url to the <a href="http://search.twitter.com/">Twitter Search</a> results page for that topic.
	 * <br>This method calls http://search.twitter.com/trends
	 * @return the result
	 * @throws TwitterException when Twitter service or network is unavailable
	 * @since Twitter4J 2.0.2
	 * @see <a href="http://apiwiki.twitter.com/Twitter-Search-API-Method%3A-trends">Twitter Search API Method: trends</a>
	 */
	Trends getTrends()
			throws TwitterException;

	/**
	 * Returns the current top 10 trending topics on Twitter.  The response includes the time of the request, the name of each trending topic, and query used on <a href="http://search.twitter.com/">Twitter Search</a> results page for that topic.
	 * <br>This method calls http://search.twitter.com/trends/current
	 * @return the result
	 * @throws TwitterException when Twitter service or network is unavailable
	 * @since Twitter4J 2.0.2
	 * @see <a href="http://apiwiki.twitter.com/Twitter-Search-API-Method%3A-trends">Twitter Search API Method: trends</a>
	 */
	Trends getCurrentTrends()
			throws TwitterException;

	/**
	 * Returns the current top 10 trending topics on Twitter.  The response includes the time of the request, the name of each trending topic, and query used on <a href="http://search.twitter.com/">Twitter Search</a> results page for that topic.
	 * <br>This method calls http://search.twitter.com/trends/current
	 * @param excludeHashTags Setting this to true will remove all hashtags from the trends list.
	 * @return the result
	 * @throws TwitterException when Twitter service or network is unavailable
	 * @since Twitter4J 2.0.2
	 * @see <a href="http://apiwiki.twitter.com/Twitter-Search-API-Method%3A-trends">Twitter Search API Method: trends</a>
	 */
	Trends getCurrentTrends(boolean excludeHashTags)
			throws TwitterException;


	/**
	 * Returns the top 20 trending topics for each hour in a given day.
	 * <br>This method calls http://search.twitter.com/trends/daily
	 * @return the result
	 * @throws TwitterException when Twitter service or network is unavailable
	 * @since Twitter4J 2.0.2
	 * @see <a href="http://apiwiki.twitter.com/Twitter-Search-API-Method%3A-trends-daily">Twitter Search API Method: trends daily</a>
	 */
	List<Trends> getDailyTrends()
			throws TwitterException;

	/**
	 * Returns the top 20 trending topics for each hour in a given day.
	 * <br>This method calls http://search.twitter.com/trends/daily
	 * @param date Permits specifying a start date for the report.
	 * @param excludeHashTags Setting this to true will remove all hashtags from the trends list.
	 * @return the result
	 * @throws TwitterException when Twitter service or network is unavailable
	 * @since Twitter4J 2.0.2
	 * @see <a href="http://apiwiki.twitter.com/Twitter-Search-API-Method%3A-trends-daily">Twitter Search API Method: trends daily</a>
	 */
	List<Trends> getDailyTrends(Date date, boolean excludeHashTags)
			throws TwitterException;

	/**
	 * Returns the top 30 trending topics for each day in a given week.
	 * <br>This method calls http://search.twitter.com/trends/weekly
	 * @return the result
	 * @throws TwitterException when Twitter service or network is unavailable
	 * @since Twitter4J 2.0.2
	 * @see <a href="http://apiwiki.twitter.com/Twitter-Search-API-Method%3A-trends-weekly">Twitter Search API Method: trends weekly</a>
	 */
	List<Trends> getWeeklyTrends()
			throws TwitterException;

	/**
	 * Returns the top 30 trending topics for each day in a given week.
	 * <br>This method calls http://search.twitter.com/trends/weekly
	 * @param date Permits specifying a start date for the report.
	 * @param excludeHashTags Setting this to true will remove all hashtags from the trends list.
	 * @return the result
	 * @throws TwitterException when Twitter service or network is unavailable
	 * @since Twitter4J 2.0.2
	 * @see <a href="http://apiwiki.twitter.com/Twitter-Search-API-Method%3A-trends-weekly">Twitter Search API Method: trends weekly</a>
	 */
	List<Trends> getWeeklyTrends(Date date, boolean excludeHashTags)
			throws TwitterException;
}

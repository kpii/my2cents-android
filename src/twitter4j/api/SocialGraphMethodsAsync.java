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

import twitter4j.TwitterListener;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface SocialGraphMethodsAsync
{
	/**
	 * Returns an array of numeric IDs for every user the authenticating user is following.
	 * <br>This method calls http://api.twitter.com/1/friends/ids%C2%A0%C2%A0
	 *
	 * @param listener a listener object that receives the response
	 * @since Twitter4J 2.0.0
	 * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method:-friends%C2%A0ids">Twitter API Wiki / Twitter REST API Method: friends%C2%A0ids</a>
	 */
	void getFriendsIDsAsync(TwitterListener listener);

	/**
	 * Returns an array of numeric IDs for every user the authenticating user is following.
	 * <br>This method calls http://api.twitter.com/1/friends/ids%C2%A0%C2%A0
	 *
	 * @param listener a listener object that receives the response
	 * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
	 * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method:-friends%C2%A0ids">Twitter API Wiki / Twitter REST API Method: friends%C2%A0ids</a>
	 * @since Twitter4J 2.0.10
	 */
	void getFriendsIDsAsync(long cursor, TwitterListener listener);

	/**
	 * Returns an array of numeric IDs for every user the specified user is following.
	 * <br>This method calls http://api.twitter.com/1/friends/ids%C2%A0%C2%A0
	 *
	 * @param userId   Specfies the ID of the user for whom to return the friends list.
	 * @param listener a listener object that receives the response
	 * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method:-friends%C2%A0ids">Twitter API Wiki / Twitter REST API Method: friends%C2%A0ids</a>
	 * @since Twitter4J 2.0.0
	 */
	void getFriendsIDsAsync(int userId, TwitterListener listener);

	/**
	 * Returns an array of numeric IDs for every user the specified user is following.
	 * <br>This method calls http://api.twitter.com/1/friends/ids%C2%A0%C2%A0
	 *
	 * @param userId   Specifies the ID of the user for whom to return the friends list.
	 * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
	 * @param listener a listener object that receives the response
	 * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method:-friends%C2%A0ids">Twitter API Wiki / Twitter REST API Method: friends%C2%A0ids</a>
	 * @since Twitter4J 2.0.10
	 */
	void getFriendsIDsAsync(int userId, long cursor, TwitterListener listener);

	/**
	 * Returns an array of numeric IDs for every user the specified user is following.
	 * <br>This method calls http://api.twitter.com/1/friends/ids%C2%A0%C2%A0
	 *
	 * @param screenName Specifies the screen name of the user for whom to return the friends list.
	 * @param listener   a listener object that receives the response
	 * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method:-friends%C2%A0ids">Twitter API Wiki / Twitter REST API Method: friends%C2%A0ids</a>
	 * @since Twitter4J 2.0.0
	 */
	void getFriendsIDsAsync(String screenName, TwitterListener listener);

	/**
	 * Returns an array of numeric IDs for every user the specified user is following.
	 * <br>This method calls http://api.twitter.com/1/friends/ids%C2%A0%C2%A0
	 *
	 * @param screenName Specfies the screen name of the user for whom to return the friends list.
	 * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
	 * @param listener   a listener object that receives the response
	 * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method:-friends%C2%A0ids">Twitter API Wiki / Twitter REST API Method: friends%C2%A0ids</a>
	 * @since Twitter4J 2.0.10
	 */
	void getFriendsIDsAsync(String screenName, long cursor, TwitterListener listener);

	/**
	 * Returns an array of numeric IDs for every user the specified user is followed by.
	 * <br>This method calls http://api.twitter.com/1/followers/ids
	 *
	 * @param listener a listener object that receives the response
	 * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method:-followers%C2%A0ids">Twitter API Wiki / Twitter REST API Method: followers%C2%A0ids</a>
	 * @since Twitter4J 2.0.0
	 */
	void getFollowersIDsAsync(TwitterListener listener);

	/**
	 * Returns an array of numeric IDs for every user the specified user is followed by.
	 * <br>This method calls http://api.twitter.com/1/followers/ids
	 *
	 * @param cursor Breaks the results into pages. A single page contains 100 users. This is recommended for users who are followed by many other users. Provide a value of  -1 to begin paging. Provide values as returned to in the response body's next_cursor and previous_cursor attributes to page back and forth in the list.
	 * @param listener a listener object that receives the response
	 * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method:-followers%C2%A0ids">Twitter API Wiki / Twitter REST API Method: followers%C2%A0ids</a>
	 * @since Twitter4J 2.0.10
	 */
	void getFollowersIDsAsync(long cursor, TwitterListener listener);

	/**
	 * Returns an array of numeric IDs for every user the specified user is followed by.
	 * <br>This method calls http://api.twitter.com/1/followers/ids
	 *
	 * @param userId   Specfies the ID of the user for whom to return the followers list.
	 * @param listener a listener object that receives the response
	 * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method:-followers%C2%A0ids">Twitter API Wiki / Twitter REST API Method: followers%C2%A0ids</a>
	 * @since Twitter4J 2.0.0
	 */
	void getFollowersIDsAsync(int userId, TwitterListener listener);

	/**
	 * Returns an array of numeric IDs for every user the specified user is followed by.
	 * <br>This method calls http://api.twitter.com/1/followers/ids
	 *
	 * @param userId   Specfies the ID of the user for whom to return the followers list.
	 * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
	 * @param listener a listener object that receives the response
	 * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method:-followers%C2%A0ids">Twitter API Wiki / Twitter REST API Method: followers%C2%A0ids</a>
	 * @since Twitter4J 2.0.10
	 */
	void getFollowersIDsAsync(int userId, long cursor, TwitterListener listener);

	/**
	 * Returns an array of numeric IDs for every user the specified user is followed by.
	 * <br>This method calls http://api.twitter.com/1/followers/ids
	 *
	 * @param screenName Specfies the screen name of the user for whom to return the followers list.
	 * @param listener   a listener object that receives the response
	 * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method:-followers%C2%A0ids">Twitter API Wiki / Twitter REST API Method: followers%C2%A0ids</a>
	 * @since Twitter4J 2.0.0
	 */
	void getFollowersIDsAsync(String screenName, TwitterListener listener);

	/**
	 * Returns an array of numeric IDs for every user the specified user is followed by.
	 * <br>This method calls http://api.twitter.com/1/followers/ids
	 *
	 * @param screenName Specfies the screen name of the user for whom to return the followers list.
	 * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
	 * @param listener   a listener object that receives the response
	 * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method:-followers%C2%A0ids">Twitter API Wiki / Twitter REST API Method: followers%C2%A0ids</a>
	 * @since Twitter4J 2.0.10
	 */
	void getFollowersIDsAsync(String screenName, long cursor, TwitterListener listener);
}

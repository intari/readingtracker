package com.viorsan.readingtracker;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 21.12.14.
 * Wrapper around ParseUser class for testing
 */
public class MyParseUser extends ParseUser {
    private static MyParseUser self=null;

    private ParseUser parseUser=null;


    private static MyParseUser getParseUser() {
        if (self==null) {
            self=new MyParseUser(new ParseUser());
        }
        return self;
    }
    private MyParseUser(ParseUser newParseUser) {
        parseUser=newParseUser;
    }
    public static MyParseUser getCurrentUser() {
        ParseUser parseUser=getParseUser().parseUser;
        return new MyParseUser(parseUser.getCurrentUser());
    }
    public MyParseUser fetchIfNeeded() throws ParseException {
        ParseUser parseUser=getParseUser().parseUser;
        return new MyParseUser(parseUser.fetchIfNeeded());
    }
    public boolean isAuthenticated() {
        ParseUser parseUser=getParseUser().parseUser;
        return parseUser.isAuthenticated();
    }
    public boolean isNew() {
        ParseUser parseUser=getParseUser().parseUser;
        return parseUser.isNew();
    }
    public void setACL(com.parse.ParseACL acl) {
        ParseUser parseUser=getParseUser().parseUser;
        parseUser.setACL(acl);
    }
}

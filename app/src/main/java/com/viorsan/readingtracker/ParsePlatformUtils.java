package com.viorsan.readingtracker;

import com.parse.ParseUser;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 22.12.14.
 * Wrapper for some Parse Platform functions. mainly to make it easy to test my programs
 * */
public class ParsePlatformUtils {
    public enum ParsePlatformMode {NORMAL, TEST_LOGGED_IN, TEST_NOT_LOGGED_IN}
    //by default use real Parse Platform objects
    private static ParsePlatformMode parsePlatformMode=ParsePlatformMode.NORMAL;
    //emulation object
    private static ParseUser_TestLoggedIn cachedParseUserTestLoggedIn =null;

    //configure how 'real' we work
    public static void setParsePlatformMode(ParsePlatformMode newMode) {
        parsePlatformMode=newMode;
    }
    public static ParsePlatformMode getParsePlatformMode() {
        return parsePlatformMode;
    }
    static ParseUser_TestLoggedIn getLoggedInEmulationObject() {
        return cachedParseUserTestLoggedIn;
    }

    static public ParseUser getCurrentParseUser() {

        switch (parsePlatformMode) {
            case NORMAL:
                //no emulation
                return ParseUser.getCurrentUser();
             case TEST_NOT_LOGGED_IN:
                //assume not logged in
                return null;
            case TEST_LOGGED_IN:
                cachedParseUserTestLoggedIn =new ParseUser_TestLoggedIn();
                return cachedParseUserTestLoggedIn;

        }
        return null;//we should never reach here if platform mode was correctly setup
    }

    /**
     * Makes appropriate 'channel name'
     * @param name - source string
     * @return string which can be used as channel name without errors
     */
    static public String makeChannelName(String name){
        return name.replaceAll("\\s+","_");
    }
}

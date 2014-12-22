package com.viorsan.readingtracker;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 22.12.14.
 * Simulate case where current user is logged in
 */

@ParseClassName("ParseUser_TestLoggedIn")
public class ParseUser_TestLoggedIn extends ParseUser {

    public static final String TAG = "ParseUser_TestLoggedIn";

    //minimal emulated data
    private static String emulatedUsername;
    private static String emulatedEmail;
    private static String emulatedObjectId;
    private HashMap<String,Object> emulatedData;

    //setup values for emulation. of course we don't want to use 'real' ParseUser data (if it was interface I would thought about implementing it)
    public ParseUser_TestLoggedIn() {
        System.out.println(TAG +":constructor called");
        emulatedData=new HashMap<String, Object>();
        emulatedObjectId="ZATRIX";
        emulatedEmail="brideOfTheSunGod@example.net";
        emulatedUsername="BrideOfTheSun";
        put("name","Bride of the Sun");
        System.out.println(TAG +":constructor finished");
    }
    public static ParseUser getCurrentUser() {
        System.out.println(TAG +":getCurrentUser() called, asked platformUtils for it");
        return ParsePlatformUtils.getCurrentParseUser();
    }
    public static void logout() {
        System.out.println(TAG +":logout() called");
    }
    public boolean isAuthenticated() {
        System.out.println(TAG +":isAuthenticated() called");
        return true;
    }
    public boolean isNew() {
        System.out.println(TAG +":isNew() called");
        return false;
    }
    public ParseUser fetchIfNeeded() throws ParseException {
        System.out.println(TAG +":fetchIfNeeded() called");
        return this;
    }
    public String getEmail() {
        return emulatedEmail;
    }
    public String getUsername() {
        return emulatedUsername;
    }
    public void setEmail(String newEmail) {
        System.out.println(TAG +":setEmail("+newEmail+") called");
        emulatedEmail=newEmail;
    }
    public void setUsername(String newUsername) {
        System.out.println(TAG +":setUsername("+newUsername+") called");
        emulatedUsername=newUsername;
    }
    public void setObjectId(String newObjectId) {
        System.out.println(TAG +":setObjectId("+newObjectId+") called");
        emulatedObjectId=newObjectId;
    }
    public String getObjectId() {
        return emulatedObjectId;
    }
    public void put(String key,Object value) {
        if (value!=null) {
            System.out.println(TAG +":put("+key+","+value.toString()+") called");
        }
        else
        {
            System.out.println(TAG +":put("+key+",null) called");
        }
        emulatedData.put(key,value);
    }
    public Object get(String key) {
        return emulatedData.get(key);
    }
    public String getString(String key) {
        return (String) emulatedData.get(key);
    }
    public void setACL(com.parse.ParseACL acl) {
        System.out.println(TAG +":setACL() called");
    }

}


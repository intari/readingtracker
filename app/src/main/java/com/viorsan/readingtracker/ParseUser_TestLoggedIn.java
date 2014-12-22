package com.viorsan.readingtracker;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        put("email","brideOfTheSunGod@example.net");
        put("username","BrideOfTheSun");
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
    public void setUsername(String username) {
        System.out.println(TAG +":setUsername("+username+") called");
        this.put("username", username);
    }

    public String getUsername() {
        return this.getString("username");
    }

    public void setEmail(String email) {
        System.out.println(TAG +":setEmail("+email+") called");
        put("email", email);
    }

    public String getEmail() {
        return getString("email");
    }

    public void setObjectId(String newObjectId) {
        System.out.println(TAG +":setObjectId("+newObjectId+") called");
        emulatedObjectId=newObjectId;
    }
    public String getObjectId() {
        return emulatedObjectId;
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


    public void remove(String key) {
        if("username".equals(key)) {
            throw new IllegalArgumentException("Can\'t remove the username key.");
        } else {
            emulatedData.remove(key);
        }
    }
    static boolean isValidType(Object value) {
        return value instanceof JSONObject || value instanceof JSONArray || value instanceof String || value instanceof Number || value instanceof Boolean || value == JSONObject.NULL || value instanceof ParseObject || value instanceof ParseACL || value instanceof ParseFile || value instanceof ParseGeoPoint || value instanceof Date || value instanceof byte[] || value instanceof List || value instanceof Map || value instanceof ParseRelation;
    }

    public void put(String key, Object value) {
        if(key == null) {
            throw new IllegalArgumentException("key may not be null.");
        } else if(value == null) {
            throw new IllegalArgumentException("value may not be null.");
        } else if(!ParseUser_TestLoggedIn.isValidType(value)) {
            throw new IllegalArgumentException("invalid type for value: " + value.getClass().toString());
        } else {
            System.out.println(TAG +":put("+key+","+value.toString()+") called");
            emulatedData.put(key,value);

        }
    }

    public boolean has(String key) {
        return emulatedData.containsKey(key);
    }


}


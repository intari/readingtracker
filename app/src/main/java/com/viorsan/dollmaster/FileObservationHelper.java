package com.viorsan.dollmaster;

import android.os.FileObserver;

/**
 * Created by dkzm on 09.07.14.
 */
public class FileObservationHelper extends FileObserver {
    private String monitoredPath;
    public  FileObservationHelper(String newPath) {
        super(newPath);
        monitoredPath=newPath;
    }
    @Override
    public void onEvent(int event, String path) {
        switch(event) {
            case FileObserver.ACCESS:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"FS ACCESS at path:"+ monitoredPath+"/"+path);
                break;
            case FileObserver.CREATE:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"FS CREATE at path:" +monitoredPath+"/"+path);
                break;
            case FileObserver.ATTRIB:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"FS ATTRIB at path:" +monitoredPath+"/"+path);
                break;
            case FileObserver.CLOSE_NOWRITE:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"FS CLOSE_NOWRITE at path:" +monitoredPath+"/"+path);
                break;
            case FileObserver.CLOSE_WRITE:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"FS CLOSE_WRITE at path:" +monitoredPath+"/"+path);
                break;
            case FileObserver.DELETE:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"FS DELETE at path:" +monitoredPath+"/"+path);
                break;
            case FileObserver.DELETE_SELF:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"FS DELETE_SELF at path:" +monitoredPath+"/"+path);
                break;
            case FileObserver.MODIFY:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"FS MODIFY at path:" +monitoredPath+"/"+path);
                break;
            case FileObserver.MOVED_FROM:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"FS MOVED_FROM at path:" +monitoredPath+"/"+path);
                break;
            case FileObserver.MOVED_TO:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"FS MOVED_TO at path:" +monitoredPath+"/"+path);
                break;
            case FileObserver.MOVE_SELF:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"FS MOVE_SELF at path:" +monitoredPath+"/"+path);
                break;
            case FileObserver.OPEN:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"FS OPEN at path:" +monitoredPath+"/"+path);
                break;


        };

    }
}

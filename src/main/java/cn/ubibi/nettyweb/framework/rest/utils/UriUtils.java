package cn.ubibi.nettyweb.framework.rest.utils;

public class UriUtils {
    public static String getPathInfo(String uri) {
        int indexOfQ = uri.indexOf("?");
        String reqPath = uri;
        if (indexOfQ >= 0) {
            reqPath = uri.substring(0, indexOfQ);
        }
        return reqPath;
    }
}

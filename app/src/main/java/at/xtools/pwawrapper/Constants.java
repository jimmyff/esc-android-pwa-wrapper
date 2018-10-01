package at.xtools.pwawrapper;

public class Constants {
    public Constants(){}
    // Root page
    public static String WEBAPP_URL = "https://en.eurovision.app/";
    public static String WEBAPP_HOST = "en.eurovision.app"; // used for checking Intent-URLs

    // User Agent tweaks
    public static boolean POSTFIX_USER_AGENT = true; // set to true to append USER_AGENT_POSTFIX to user agent
    public static boolean OVERRIDE_USER_AGENT = false; // set to true to use USER_AGENT instead of default one
    public static String USER_AGENT_POSTFIX = "AndroidPwaWrapper"; // useful for identifying traffic, e.g. in Google Analytics
    public static String USER_AGENT = "WebView";

    // Constants
    // window transition duration in ms
    public static int SLIDE_EFFECT = 2200;
    // show your app when the page is loaded XX %.
    // lower it, if you've got server-side rendering (e.g. to 35),
    // bump it up to ~98 if you don't have SSR or a loading screen in your web app
    public static int PROGRESS_THRESHOLD = 95;
    // turn on/off mixed content (both https+http within one page) for API >= 21
    public static boolean ENABLE_MIXED_CONTENT = true;
}

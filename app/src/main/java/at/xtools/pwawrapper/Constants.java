package at.xtools.pwawrapper;

public class Constants {
    public Constants(){}
    // Root page
    public static String WEBAPP_URL = "https://en.eurovision.app/";

    public static String FIREBASE_DOMAIN = "eurovisiongp.firebaseapp.com";
    public static String GOOGLE_AUTH_DOMAIN = "accounts.google.com";
    public static String TWITTER_AUTH_DOMAIN = "api.twitter.com";
    public static String FACEBOOK_AUTH_DOMAIN = "facebook.com";

    public static String WEBAPP_HOST = "en.eurovision.app"; // used for checking Intent-URLs

    // User Agent tweaks
    public static boolean POSTFIX_USER_AGENT = true; // set to true to append USER_AGENT_POSTFIX to user agent
    public static boolean OVERRIDE_USER_AGENT = false; // set to true to use USER_AGENT instead of default one
    public static String USER_AGENT_POSTFIX = "Eurovision.app Android"; // useful for identifying traffic, e.g. in Google Analytics
    public static String USER_AGENT = "WebView";

    // Constants
    // window transition duration in ms
    public static int SLIDE_EFFECT = 800;
    // show your app when the page is loaded XX %.
    // lower it, if you've got server-side rendering (e.g. to 35),
    // bump it up to ~98 if you don't have SSR or a loading screen in your web app
    public static int PROGRESS_THRESHOLD = 100;
    // turn on/off mixed content (both https+http within one page) for API >= 21
    public static boolean ENABLE_MIXED_CONTENT = true;
}

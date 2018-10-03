package at.xtools.pwawrapper.webview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
//import android.support.customtabs.browseractions.BrowserActionItem;
//import android.support.customtabs.browseractions.BrowserActionsIntent;
//import android.support.customtabs.browseractions.BrowserServiceFileProvider;
//import android.support.customtabs.trusted.TrustedWebActivityService;

import android.widget.Toast;

import at.xtools.pwawrapper.Constants;
import at.xtools.pwawrapper.R;
import at.xtools.pwawrapper.ui.UIManager;

public class WebViewHelper {
    // Instance variables
    private Activity activity;
    private UIManager uiManager;
    private WebView webView;
    private WebSettings webSettings;

    public WebViewHelper(Activity activity, UIManager uiManager) {
        this.activity = activity;
        this.uiManager = uiManager;
        this.webView = (WebView) activity.findViewById(R.id.webView);
        this.webSettings = webView.getSettings();
    }

    /**
     * Simple helper method checking if connected to Network.
     * Doesn't check for actual Internet connection!
     * @return {boolean} True if connected to Network.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Wifi or Mobile Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    // manipulate cache settings to make sure our PWA gets updated
    private void useCache(Boolean use) {
        if (use) {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }
    }

    // public method changing cache settings according to network availability.
    // retrieve content from cache primarily if not connected,
    // allow fetching from web too otherwise to get updates.
    public void forceCacheIfOffline() {
        useCache(!isNetworkAvailable());
    }

    // handles initial setup of webview
    public void setupWebView() {
        // accept cookies
        CookieManager.getInstance().setAcceptCookie(true);
        // enable JS
        webSettings.setJavaScriptEnabled(true);

        // must be set for our js-popup-blocker:
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // PWA settings
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webSettings.setDatabasePath(activity.getApplicationContext().getFilesDir().getAbsolutePath());
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            webSettings.setAppCacheMaxSize(Long.MAX_VALUE);
        }
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCachePath(activity.getApplicationContext().getCacheDir().getAbsolutePath());
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);

        // enable mixed content mode conditionally
        if (Constants.ENABLE_MIXED_CONTENT
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        // retrieve content from cache primarily if not connected
        forceCacheIfOffline();

        // set User Agent
        if (Constants.OVERRIDE_USER_AGENT || Constants.POSTFIX_USER_AGENT) {
            String userAgent = "";
            if (Constants.OVERRIDE_USER_AGENT) {
                userAgent = Constants.USER_AGENT;
            }
            if (Constants.POSTFIX_USER_AGENT) {
                userAgent = userAgent + " " + Constants.USER_AGENT_POSTFIX;
            }
            webSettings.setUserAgentString(userAgent);
        }

        // enable HTML5-support
        webView.setWebChromeClient(new WebChromeClient() {


            // Jimmys
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView tempWebView = new WebView(activity.getApplicationContext());
                tempWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        try {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                            builder.setToolbarColor(activity.getResources().getColor(R.color.colorPrimary));
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.launchUrl(view.getContext(), Uri.parse(url));
                            return false;
                        } catch (Exception e) {
                            Toast.makeText(activity.getApplicationContext(), "Custom Tab Error", Toast.LENGTH_LONG).show();
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                            activity.startActivity(browserIntent);
                            return true;
                        }
                    }
                });
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(tempWebView);
                resultMsg.sendToTarget();
                return true;
            }
//
//            // This opens a new window
//            @Override
//            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
//                WebView tempWebView = new WebView(activity.getApplicationContext());
//                tempWebView.setWebViewClient(new WebViewClient() {
//                    @Override
//                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                        browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
//                        activity.startActivity(browserIntent);
//                        return true;
//                    }
//                });
//                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
//                transport.setWebView(tempWebView);
//                resultMsg.sendToTarget();
//                return true;
//            }


//            @Override
//            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
//                Message href = view.getHandler().obtainMessage();
//                view.requestFocusNodeHref(href);
//                String url = href.getData().getString("url");
//
//                if (url == null) {
//                    WebView.HitTestResult result = view.getHitTestResult();
//                    int type = result.getType();
//                    url = result.getExtra();
//                }
//
////                Toast.makeText(activity.getApplicationContext(), "OnCreateWindow", Toast.LENGTH_LONG).show();
//
//                if (url != null && !url.startsWith(Constants.WEBAPP_URL)) {
//                    view.stopLoading();
//                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//                    CustomTabsIntent customTabsIntent = builder.build();
//                    customTabsIntent.launchUrl(view.getContext(), Uri.parse(url));
//                    return false;
//                } else if (url != null) {
//                    //it's null for most rouge browser hijack ads
//                    webView.loadUrl(url);
//                    return true;
//                }
//                return false;
//            }


            // update ProgressBar
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                uiManager.setLoadingProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }
        });

        // Set up Webview client
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                handleUrlLoad(view, url);
            }

            // handle loading error by showing the offline screen
            @Deprecated
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    handleLoadError(errorCode);
                }
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // new API method calls this on every error for each resource.
                    // we only want to interfere if the page itself got problems.
                    String url = request.getUrl().toString();
                    if (view.getUrl().equals(url)) {
                        handleLoadError(error.getErrorCode());
                    }
                }
            }
        });
    }

    // Lifecycle callbacks
    public void onPause() {
        webView.onPause();
    }

    public void onResume() {
        webView.onResume();
    }

    // show "no app found" dialog
    private void showNoAppDialog(Activity thisActivity) {
        new AlertDialog.Builder(thisActivity)
            .setTitle(R.string.noapp_heading)
            .setMessage(R.string.noapp_description)
            .show();
    }
    // handle load errors
    private void handleLoadError(int errorCode) {
        if (errorCode != WebViewClient.ERROR_UNSUPPORTED_SCHEME) {
            uiManager.setOffline(true);
        } else {
            // Unsupported Scheme, recover
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    goBack();
                }
            }, 100);
        }
    }

    // handle external urls
    private boolean handleUrlLoad(WebView view, String url) {

//
//        if (!url.startsWith(Constants.WEBAPP_URL)) {
//            view.stopLoading();
//            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//            CustomTabsIntent customTabsIntent = builder.build();
//            customTabsIntent.launchUrl(activity.getApplicationContext(), Uri.parse(url));
//            return true;
//        }

        // prevent loading content that isn't ours
        if (!url.startsWith(Constants.WEBAPP_URL)
                && !url.contains(Constants.FIREBASE_DOMAIN) &&
            !url.contains(Constants.GOOGLE_AUTH_DOMAIN) && !url.contains(Constants.FACEBOOK_AUTH_DOMAIN)
            && !url.contains(Constants.TWITTER_AUTH_DOMAIN)
                ) {
            // stop loading
            view.stopLoading();

            // open external URL in Browser/3rd party apps instead
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivity(intent);
                } else {
                    showNoAppDialog(activity);
                }
            } catch (Exception e) {
                showNoAppDialog(activity);
            }
            // return value for shouldOverrideUrlLoading
            return true;
        } else {
            // let WebView load the page!
            // activate loading animation screen
            uiManager.setLoading(true);
            // return value for shouldOverrideUrlLoading
            return false;
        }
    }

    // handle back button press
    public boolean goBack() {
        if (webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }

    // load app startpage
    public void loadHome() {
        webView.loadUrl(Constants.WEBAPP_URL);
    }

    // load URL from intent
    public void loadIntentUrl(String url) {
        if (!url.equals("") && url.contains(Constants.WEBAPP_HOST)) {
            webView.loadUrl(url);
        } else {
            // Fallback
            loadHome();
        }
    }
}

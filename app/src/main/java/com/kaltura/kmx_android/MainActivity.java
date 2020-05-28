package com.kaltura.kmx_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebView mWebRTCWebView;

    // Populate the KAF endpoint and Kaltura Session
    private String mKAFEndpoint = POPULATE_WITH_VALID_KAF_ENDPOINT; // i.e. "https://xxxxxxx.kaf.kaltura.com/virtualEvent/launch";
    private String mKalturaSession = POPULATE_WITH_VALID_KS;

    private String mURL = mKAFEndpoint + "?ks=" + mKalturaSession;

    // - This arbitrary, app-internal constant represents a group of requested permissions.
    // - For simplicity, this app treats all desired permissions as part of a single group.
    private final int PERMISSIONS_REQUEST_ALL = 1988;

    // Helps check whether app has permission to access what is declared in its manifest.
    // - Permissions from app's manifest that have a "protection level" of "dangerous".
    private static final String[] PERMISSIONS_FOR_REQUEST = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
            //Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebRTCWebView = findViewById(R.id.webView);

        // Beginning in Android 6.0 (API level 23), users grant permissions to an app while
        // the app is running, not when they install the app. Check whether app has permission
        // to access what is declared in its manifest.
        if (Build.VERSION.SDK_INT > 22) {
            List<String> permissionsNeeded = new ArrayList<>();
            for (String permission : PERMISSIONS_FOR_REQUEST) {
                // Check if the permission has already been granted.
                if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                    permissionsNeeded.add(permission);
            }
            if (permissionsNeeded.size() > 0) {
                // Request any permissions which have not been granted. The result will be called back in onRequestPermissionsResult.
                this.requestPermissions(permissionsNeeded.toArray(new String[0]), PERMISSIONS_REQUEST_ALL);
            } else {
                // TODO: activate join buttons
            }
        } else {
            // TODO: activate join buttons
        }

        setUpWebViewDefaults(mWebRTCWebView);

        mWebRTCWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(request.getOrigin().toString().equals("https://smart.newrow.com/")) {
                            request.grant(request.getResources());
                        } else {
                            request.deny();
                        }
                    }
                });
            }
        });
    }

    // Callback containing the result of the permissions request. If permissions were not previously obtained,
    // wait until this is received until calling startVideoViewSizeListener where Connector is initially rendered.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // If the expected request code is received, begin rendering video.
        if (requestCode == PERMISSIONS_REQUEST_ALL) {
            //for (int i = 0; i < permissions.length; ++i)
            //    Log("permission: " + permissions[i] + " " + grantResults[i]);

            // TODO: show join button
        } else {
            //mLogger.Log("ERROR! Unexpected permission requested. Video will not be rendered.");
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // When the application falls into the background we want to stop the media stream
        // such that the camera is free to use by other apps.
        //mWebRTCWebView.evaluateJavascript("if(window.localStream){window.localStream.stop();}", null);
    }

    public void JoinInChrome(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mURL));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null);
            startActivity(intent);
        }
    }

    public void JoinInApp(View v) {
        mWebRTCWebView.loadUrl(mURL);
    }

    // Convenience method to set some generic defaults for a given WebView
    private void setUpWebViewDefaults(WebView webView) {
        WebSettings settings = webView.getSettings();

        // Enable Javascript
        settings.setJavaScriptEnabled(true);

        // Use WideViewport and Zoom out if there is no viewport defined
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // Enable pinch to zoom without the zoom buttons
        settings.setBuiltInZoomControls(true);

        // Allow use of Local Storage
        settings.setDomStorageEnabled(true);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            // Hide the zoom controls for HONEYCOMB+
            settings.setDisplayZoomControls(false);
        }

        // Enable remote debugging via chrome://inspect
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.setWebViewClient(new WebViewClient());

        // WebRTC requires third party cookies to work
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptThirdPartyCookies(webView, true);
    }
}

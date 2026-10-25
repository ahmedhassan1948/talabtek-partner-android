package com.talabtek.partner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Talabtek Partner — غلاف WebView احترافي لموقع طلبتك.
 * المسار الافتراضي: /partner
 */
public class MainActivity extends Activity {
    private static final String DEFAULT_ORIGIN = "https://talabtek.com";
    private static final String APP_PATH = "/partner";
    private static final String PREFS = "talabtek_native";
    private static final String BRAND = "#1B1410";
    private static final String BG = "#FFF9F4";

    private WebView web;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor(BG));

        web = new WebView(this);
        root.addView(web, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setUserAgentString(s.getUserAgentString() + " TalabtekAndroid/1.0");
        s.setGeolocationEnabled(true);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) showOffline();
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        web.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimeType, long contentLength) {
                openExternal(url);
            }
        });

        web.addJavascriptInterface(new Bridge(), "TalabtekAndroid");
        web.loadUrl(startUrl());
    }

    private String origin() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        return prefs.getString("origin", DEFAULT_ORIGIN);
    }

    private String startUrl() {
        return origin() + APP_PATH;
    }

    private boolean handleUrl(String url) {
        if (url == null) return false;
        if (url.startsWith("http://") || url.startsWith("https://")) {
            if (url.startsWith(origin())) return false;
            openExternal(url);
            return true;
        }
        openExternal(url);
        return true;
    }

    private void openExternal(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "لا يوجد تطبيق يفتح هذا الرابط", Toast.LENGTH_SHORT).show();
        }
    }

    private void showOffline() {
        String html = "<!doctype html><html lang='ar' dir='rtl'><head><meta charset='utf-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
                + "<style>"
                + "html,body{margin:0;height:100%;font-family:sans-serif;background:#FFF9F4;color:#1B1410}"
                + "body{display:grid;place-items:center;padding:24px;text-align:center}"
                + ".badge{width:84px;height:84px;border-radius:26px;background:#1B1410;color:#fff;"
                + "display:grid;place-items:center;font-size:38px;font-weight:800;margin:0 auto 18px;box-shadow:0 14px 30px rgba(0,0,0,.16)}"
                + "h1{font-size:22px;margin:0 0 8px}p{font-size:14px;color:#7a6f66;margin:0 0 22px;line-height:1.8}"
                + "button{display:block;width:100%;max-width:300px;margin:8px auto;padding:14px 18px;border:0;border-radius:14px;"
                + "font-size:15px;font-weight:700;font-family:inherit;cursor:pointer}"
                + ".primary{background:#1B1410;color:#fff}.ghost{background:#fff;color:#1B1410;border:1px solid #e8ddd5}"
                + ".url{font-size:11px;color:#a99c92;margin-top:16px;direction:ltr;word-break:break-all}"
                + "</style></head><body><div>"
                + "<div class='badge'>T</div>"
                + "<h1>تعذّر الاتصال</h1>"
                + "<p>تأكد من اتصالك بالإنترنت ثم أعد المحاولة.</p>"
                + "<button class='primary' onclick='TalabtekAndroid.retry()'>إعادة المحاولة</button>"
                + "<button class='ghost' onclick='TalabtekAndroid.openSettings()'>تغيير رابط الموقع</button>"
                + "<div class='url'>" + origin() + APP_PATH + "</div>"
                + "</div></body></html>";
        web.loadDataWithBaseURL(origin(), html, "text/html", "utf-8", null);
    }

    public class Bridge {
        @JavascriptInterface
        public void retry() {
            runOnUiThread(() -> web.loadUrl(startUrl()));
        }

        @JavascriptInterface
        public void openSettings() {
            runOnUiThread(MainActivity.this::showSettings);
        }
    }

    private void showSettings() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(origin());
        input.setGravity(Gravity.CENTER);
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle("رابط الموقع")
                .setMessage("أدخل رابط خادم طلبتك (بدون /app).")
                .setView(input)
                .setPositiveButton("حفظ", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
                    if (!value.startsWith("http://") && !value.startsWith("https://")) {
                        Toast.makeText(this, "الرابط يجب أن يبدأ بـ http:// أو https://", Toast.LENGTH_LONG).show();
                        return;
                    }
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("origin", value).apply();
                    web.loadUrl(startUrl());
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && web != null && web.canGoBack()) {
            web.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}

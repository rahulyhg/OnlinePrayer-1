package christian.online.prayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBarWeb;
    private Toolbar mytoolbar;
    String webURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webURL = getIntent().getStringExtra(HomeActivity.WEB_URL);
        Log.e("Getting",webURL+"");

        initUI();

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.loadUrl(webURL);
        webView.setWebViewClient(new Callback());

        initActionBar();
    }

    private void initUI(){
        webView = (WebView) findViewById(R.id.webView);
        progressBarWeb = (ProgressBar) findViewById(R.id.progressBarWeb);
    }

    private void initActionBar() {
        mytoolbar = (Toolbar) findViewById(R.id.toolbarWeb);
        setSupportActionBar(mytoolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private class Callback extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
            //return (false);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBarWeb.setVisibility(View.GONE);
        }
    }
}

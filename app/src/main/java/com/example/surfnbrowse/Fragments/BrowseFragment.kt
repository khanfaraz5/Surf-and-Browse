package com.example.surfnbrowse.Fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.surfnbrowse.Activity.MainActivity
import com.example.surfnbrowse.R
import com.example.surfnbrowse.databinding.FragmentBrowseBinding

class BrowseFragment(private var urlNew:String) : Fragment() {

    lateinit var binding: FragmentBrowseBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_browse, container, false)
        binding = FragmentBrowseBinding.bind(view)
        // since after searching anything on google in our app, chrome was being opened by default , these two clients will fix that

        return view

    }

    override fun onResume() {
        super.onResume()


        binding.webView.setDownloadListener { url, _, _, _, _ ->
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)))  } // for download using download manager


        val mainRef = requireActivity() as MainActivity

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false // so that + and - zoom buttons are not shown
            webViewClient = object: WebViewClient(){
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    // progress bar code
                    super.onPageStarted(view, url, favicon)
                    mainRef.binding.progressBar.progress =0 // default progress value
                    mainRef.binding.progressBar.visibility = View.VISIBLE
                    if(url!!.contains("")){}
                }


                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    mainRef.binding.progressBar.visibility = View.GONE
                }

            }

            webChromeClient = object: WebChromeClient(){
                // for playing u tube video on fullscreen
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    super.onShowCustomView(view, callback)
                    binding.webView.visibility = View.GONE
                    binding.customView.visibility = View.VISIBLE
                    binding.customView.addView(view)
                }

                override fun onHideCustomView() {
                    // if small screen button is pressed then it should go back to deafult
                    super.onHideCustomView()
                    binding.webView.visibility = View.VISIBLE
                    binding.customView.visibility = View.GONE
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    mainRef.binding.progressBar.progress = newProgress // to update progress
                }
            }

            when{
                // for our default search engine
                URLUtil.isValidUrl(urlNew) -> loadUrl(urlNew)
                urlNew.contains(".com", ignoreCase = true)-> loadUrl(urlNew) // if user has typed .com , then load url
                else -> loadUrl("https://www.google.com/search?q=$urlNew")
            }

        }
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as MainActivity).saveBookmarks()
        // for clearing data after closing application
        binding.webView.apply {
            clearMatches()
            clearHistory()
            clearFormData()
            clearSslPreferences()
            clearCache(true) // remove any data from internal storage too

            CookieManager.getInstance().removeAllCookies(null)
            WebStorage.getInstance().deleteAllData()

        }
    }
}
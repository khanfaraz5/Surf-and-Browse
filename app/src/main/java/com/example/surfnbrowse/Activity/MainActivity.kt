package com.example.surfnbrowse.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.view.Gravity
import android.view.WindowManager
import android.webkit.WebView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.surfnbrowse.Fragments.BrowseFragment
import com.example.surfnbrowse.Fragments.HomeFragment
import com.example.surfnbrowse.Model.Bookmark_model
import com.example.surfnbrowse.R
import com.example.surfnbrowse.databinding.ActivityMainBinding
import com.example.surfnbrowse.databinding.BookmarkDialogBinding
import com.example.surfnbrowse.databinding.MoreFeaturesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var printJob: PrintJob? = null
    // for our total number of tabs of fragments
    companion object{
        var tabsList: ArrayList<Fragment> = ArrayList()
        private var isFullScreen: Boolean = true
        var bookmarkList: ArrayList<Bookmark_model> = ArrayList()
        var bookmarkIndex: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        getAllBookMarks()

        tabsList.add(HomeFragment()) // since we want to add our homeFragment when our app starts

        binding.myPager.adapter = TabsAdapter(supportFragmentManager, lifecycle)
        binding.myPager.isUserInputEnabled = false

        initializeViews()
        changeFullscreen(enable = true)

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {
        var frag: BrowseFragment? = null
        try {
            frag = tabsList[binding.myPager.currentItem] as BrowseFragment
        }catch (e:Exception){}

        when{
            frag?.binding?.webView?.canGoBack() == true-> frag.binding.webView.goBack()
            binding.myPager.currentItem != 0 ->{ // if we have opened a fragment other than HomeFragment
                tabsList.removeAt(binding.myPager.currentItem)  // then that will be removed from tabslist after back button is pressed
                binding.myPager.adapter?.notifyDataSetChanged() // fo notifying adapter that we have changed tabs
                binding.myPager.currentItem = tabsList.size-1 // updating currentItem index

            }
            else -> super.onBackPressed()
        }

    }

// adapter code copied from viewPager2 documentation
    private inner class TabsAdapter(fa: FragmentManager, lc:Lifecycle) : FragmentStateAdapter(fa,lc) {
        override fun getItemCount(): Int = tabsList.size

        override fun createFragment(position: Int): Fragment = tabsList[position] // will return a fragment based on position
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeTab(url:String, fragment: Fragment){
        tabsList.add(fragment)  // adding fragment dynamically
        binding.myPager.adapter?.notifyDataSetChanged() // fo notifying adapter that we have changed tabs
        binding.myPager.currentItem = tabsList.size-1
    }

    fun checkForInternet(context: Context): Boolean {

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    private fun initializeViews(){
        binding.settingsBtn.setOnClickListener {

            var frag: BrowseFragment? = null
            try {
                frag = tabsList[binding.myPager.currentItem] as BrowseFragment
            }catch (e:Exception){}

            val view = layoutInflater.inflate(R.layout.more_features, binding.root,false)
            val dialogueBinding = MoreFeaturesBinding.bind(view)

            val dialog = MaterialAlertDialogBuilder(this).setView(view).create()
            dialog.window?.apply {
                attributes.gravity = Gravity.BOTTOM
                attributes.y = 50
                setBackgroundDrawable(ColorDrawable(0xffffffff.toInt()))
            }
            dialog.show()

            dialogueBinding.backBtn.setOnClickListener {
                onBackPressed()
            }

            dialogueBinding.forwardBtn.setOnClickListener {
                frag?.apply {
                    if(binding.webView.canGoForward()){
                        binding.webView.goForward()
                    }
                }
            }

            dialogueBinding.saveBtn.setOnClickListener {
                dialog.dismiss() // for dismissing the save dialog after save button is clicked
                if(frag!=null) {
                    saveAsPdf(web = frag.binding.webView)
                }
                else{
                    Snackbar.make(binding.root, "Open a WebPage First", 3000).show()
                }
            }

            dialogueBinding.fullscreenBtn.setOnClickListener {
                if (isFullScreen) changeFullscreen(enable = false)
                else changeFullscreen(enable = true)
            }

           frag?.let{
               bookmarkIndex = isBookmarked(it.binding.webView.url!!)
               if(bookmarkIndex!=-1){

                   dialogueBinding.bookmarkBtn.apply {
                       setIconTintResource(R.color.green)
                       setTextColor(ContextCompat.getColor(this@MainActivity, R.color.cool_blue))
                   }
               }
           }

            // for alert dialog to appear after clicking on bookmark button
            dialogueBinding.bookmarkBtn.setOnClickListener {


               frag?.let {

                   if(bookmarkIndex==-1){
                       val viewB = layoutInflater.inflate(R.layout.bookmark_dialog, binding.root,false)
                       val bBinding = BookmarkDialogBinding.bind(viewB)

                       val dialogb = MaterialAlertDialogBuilder(this)
                           .setTitle("Add Bookmark")
                           .setMessage("Url:${it.binding.webView.url}")
                           .setPositiveButton("Add"){self,_ ->
                               bookmarkList.add(Bookmark_model(name = bBinding.bookmarkTitle.text.toString(), url = it.binding.webView.url!!))
                               self.dismiss()}
                           .setNegativeButton("Cancel"){self,_ -> self.dismiss()}
                           .setView(viewB).create()

                       dialogb.show()
                       bBinding.bookmarkTitle.setText(it.binding.webView.title) // for dynamically showing the url of website on bookmark dialog
                   }

                   //if a website is already bookmarked
                   else{
                       val dialogb = MaterialAlertDialogBuilder(this)
                           .setTitle("Remove Bookmark")
                           .setMessage("Url:${it.binding.webView.url}")
                           .setPositiveButton("Remove"){self,_ ->
                               bookmarkList.removeAt((bookmarkIndex))
                               self.dismiss()}
                           .setNegativeButton("Cancel"){self,_ -> self.dismiss()}
                           .create()

                       dialogb.show()
                   }
               }

                dialog.dismiss()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        printJob?.let {
            when{
                it.isCompleted -> Snackbar.make(binding.root, "Successful -> ${it.info.label} !!", 4000).show()
                it.isFailed -> Snackbar.make(binding.root, "Failed -> ${it.info.label} !!", 4000).show()
            }
        }

    }

    // for saving as pdf
    private fun saveAsPdf(web: WebView){
        val pm = getSystemService(Context.PRINT_SERVICE)as PrintManager
        val jobName = "${URL(web.url).host}_${SimpleDateFormat("HH:mm d_MMM_YY", Locale.ENGLISH)
            .format(Calendar.getInstance().time)}"
        val printAdapter = web.createPrintDocumentAdapter(jobName)
        val printAttributes = PrintAttributes.Builder()
        printJob = pm.print(jobName, printAdapter, printAttributes.build())
    }

    // for fullscreen button
    private fun changeFullscreen(enable: Boolean){
        if(enable){
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, binding.root).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }else{
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
        }
    }

    //to check if a website is bookmarked or not .
    fun isBookmarked(url: String):Int{
        bookmarkList.forEachIndexed{index, bookmarkModel ->
            if(bookmarkModel.url==url){
                return index
            }

        }
        return -1
    }

    // for saving bookmarks using shared preferences
    fun saveBookmarks(){
        val editor = getSharedPreferences("BOOKMARKS", MODE_PRIVATE).edit()

        val data = GsonBuilder().create().toJson(bookmarkList)

        editor.putString("bookmarkList", data)

        editor.apply()

    }
    // for getting bookmarks data using shared preferences
    fun getAllBookMarks(){
        bookmarkList = ArrayList()
        val editor = getSharedPreferences("BOOKMARKS", MODE_PRIVATE)
        val data = editor.getString("bookmarkList", null)

        if(data!=null){
            val list:ArrayList<Bookmark_model> = GsonBuilder().create().fromJson(data, object :TypeToken<ArrayList<Bookmark_model>>(){}.type)
            bookmarkList.addAll(list)

        }
    }
}
package com.example.surfnbrowse.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.surfnbrowse.Activity.MainActivity
import com.example.surfnbrowse.Fragments.BrowseFragment
import com.example.surfnbrowse.R
import com.example.surfnbrowse.databinding.BookmarkViewBinding
import com.google.android.material.snackbar.Snackbar

class BookmarkAdapter(private val context: Context): RecyclerView.Adapter<BookmarkAdapter.MyHolder>() {

    private val colors = context.resources.getIntArray(R.array.myColors)

    class MyHolder(binding: BookmarkViewBinding):RecyclerView.ViewHolder(binding.root) {
        val image = binding.bookmarkIcon
        val name = binding.bookmarkName
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(BookmarkViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.image.setBackgroundColor(colors[(colors.indices).random()]) // choosing random bg for bookmark icon whose icon is not available
        holder.image.text =
            MainActivity.bookmarkList[position].name[0].toString()  // passing first letter of bookmark name if icon not available
        holder.name.text = MainActivity.bookmarkList[position].name

        // if user clicks on any bookmark icon, its page should open
        holder.root.setOnClickListener {
            context as MainActivity
            when{
                context.checkForInternet(context) -> context.changeTab(MainActivity.bookmarkList[position].name,
                BrowseFragment(urlNew = MainActivity.bookmarkList[position].url))

                else -> Snackbar.make(holder.root, "Check Your Internet Connection", 3000).show()
            }

        }
    }

    override fun getItemCount(): Int {
        return MainActivity.bookmarkList.size;
    }
}
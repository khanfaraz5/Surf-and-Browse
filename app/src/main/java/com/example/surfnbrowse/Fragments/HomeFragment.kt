package com.example.surfnbrowse.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.surfnbrowse.Adapter.BookmarkAdapter
import com.example.surfnbrowse.Model.Bookmark_model
import com.example.surfnbrowse.Activity.MainActivity
import com.example.surfnbrowse.R

import com.example.surfnbrowse.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        val mainActivityRef = requireActivity() as MainActivity // getting reference of MainActivity

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(result: String?): Boolean {

                if(mainActivityRef.checkForInternet(requireContext())){

                    mainActivityRef.changeTab(result!!, BrowseFragment(result)) // calling the MainActivity's function
                    //BrowseFragment(result) we wrote result inside this because we will be passing this to our BrowseFragment code, which in turn will load url
                    // of the result , result is our keywords typed in searchbar
                }
                else{
                    Snackbar.make(binding.root, "Please Check Your Internet Connection", 3000).show()
                }

                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })



        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.setItemViewCacheSize(5)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.recyclerView.adapter = BookmarkAdapter(requireContext())


    }
}


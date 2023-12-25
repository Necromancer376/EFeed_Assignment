package com.example.e_feedassignment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.e_feedassignment.R
import com.example.e_feedassignment.databinding.ItemIssueBinding
import com.example.e_feedassignment.model.Issue
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IssuesAdapter() : RecyclerView.Adapter<IssuesAdapter.MyViewHolder>() {

    private var issueList = mutableListOf<Issue>()
    private lateinit var context: Context

    inner class MyViewHolder(val binding: ItemIssueBinding) :
        RecyclerView.ViewHolder(binding.root)


    //    // get the list of issues and update the recyclerview
    fun setIssuesList(list: MutableList<Issue>) {
        issueList = list
        notifyDataSetChanged()
    }


    // get the list of issues from different page and join the new list with the existing one.
    fun extendList(list: MutableList<Issue>) {
        issueList.addAll(list)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var binding = ItemIssueBinding.inflate( LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return MyViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return issueList.size
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = issueList[position]

        // assign user information and title
        holder.binding.tvTitle.text = item.title
        holder.binding.tvUsername.text = item.user.login
        loadUserImage(holder.binding.ivUserImage, item.user.avatar_url)

        // assign the dates
        holder.binding.tvDateCreated.text = "Created at: " + getCreatedClosedDateFormat(item.created_at)

        if (item.closed_at == null) {
            holder.binding.tvDateClosed.text = ""
        } else {
            holder.binding.tvDateClosed.text = "Closed at: " + getCreatedClosedDateFormat(item.closed_at)
        }
    }


    // format the date from the api into the desired format
    private fun getCreatedClosedDateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        try {
            val created: Date = inputFormat.parse(date) ?: return ""
            return outputFormat.format(created)
        }
        catch (e: ParseException) {
            e.printStackTrace()
        }

        return ""
    }


    // using glide library load the user image from the url gotten from the api
    fun loadUserImage(view: ImageView, url: String) {
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.ic_image_not_supported)
            .error(R.drawable.ic_image_not_supported)
            .override(200, 200)
            .centerCrop()
            .skipMemoryCache(true)
            .into(view)
    }
}
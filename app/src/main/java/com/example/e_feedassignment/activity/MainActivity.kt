package com.example.e_feedassignment.activity

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.e_feedassignment.BuildConfig
import com.example.e_feedassignment.R
import com.example.e_feedassignment.adapter.IssuesAdapter
import com.example.e_feedassignment.api.ServiceGenerator
import com.example.e_feedassignment.databinding.ActivityMainBinding
import com.example.e_feedassignment.model.Issue
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties


class MainActivity : AppCompatActivity() {

    private val API_KEY: String = BuildConfig.GITHUB_API_KEY
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAdapter: IssuesAdapter
    private lateinit var mProgressDialog: Dialog

    private var pageNo: Int = 1
    private var filterToggle: Boolean = false

    private var filteredURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAdapter = IssuesAdapter()

        filteredURL = buildURL()
        setIssuesList()

        binding.btnLoadMore.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                loadMore()
            }
        }

        binding.btnFilter.setOnClickListener {
            it.hideKeyboard()
            openFilters()
        }

        binding.btnApply.setOnClickListener {
            it.hideKeyboard()
            filteredURL = buildURL()
            binding.tvNoIssues.visibility = View.GONE
            setIssuesList()
        }
    }


    // Assign the list of issues to the recycler view
    fun setIssuesList() {
        CoroutineScope(Dispatchers.Main).launch {
            var result: MutableList<Issue>? = getIssueList()

            if (result != null) {
                withContext(Dispatchers.Main) {
                    configureLoadMoreButton(result)
                    mAdapter.setIssuesList(result)
                    binding.rvIssues.adapter = mAdapter
                }
            }
            else {
                showErrorSnackBar("The user or repository does not exist", true)
            }
        }
    }


    // get the list of issues from api and change the UI based on the response
    suspend fun getIssueList(): MutableList<Issue>? {
        showProgressDialog()
        var result: MutableList<Issue>? = null

        val query = filteredURL
        val rep = ServiceGenerator.api.getRepoIssues(API_KEY, query)
        if(rep != null) {
            if (rep.isSuccessful) {
                result = rep.body()!!.items
                binding.tvNoIssues.visibility = if(result.isEmpty()) View.VISIBLE else View.GONE
            }
            else {
                binding.tvNoIssues.visibility = View.VISIBLE
                showErrorSnackBar("The user or repository does not exist", true)
            }
        }
        hideProgressDialog()
        return result
    }


    // call the api for next page and join the list to the existing list
    suspend fun loadMore() {
        showProgressDialog()
        pageNo++
        var result: MutableList<Issue>? = null

        val query = "${filteredURL} page:${pageNo}"
        val rep = ServiceGenerator.api.getRepoIssues(API_KEY, query)

        if(rep.isSuccessful) {
            result = rep.body()!!.items
            binding.tvNoIssues.visibility = if(result.isEmpty()) View.VISIBLE else View.GONE
            if (result != null) {
                mAdapter.extendList(result)
            }
            else {
                hideProgressDialog()
            }
        }
        else {
            showErrorSnackBar("The user or repository does not exist", true)
            hideProgressDialog()
        }
        hideProgressDialog()
    }


    // based on the issues list size show or hide the load more button
    private fun configureLoadMoreButton(list: MutableList<Issue>?) {
        if(list == null || list.isEmpty()) {
            binding.btnLoadMore.visibility = View.GONE
        }
        else {
            binding.btnLoadMore.visibility = View.VISIBLE
        }
    }


    // show and hide the filters menu when the button is clicked
    private fun openFilters() {
        filterToggle = !filterToggle
        if(filterToggle) {
            binding.btnFilter.setImageDrawable(getDrawable(R.drawable.ic_filter_off))
            binding.llFilters.visibility = View.VISIBLE
        }
        else {
            binding.btnFilter.setImageDrawable(getDrawable(R.drawable.ic_filter))
            binding.llFilters.visibility = View.GONE
        }
    }


    // structure the query in the proper format based on the filters
    fun buildURL(): String {
        var filters = ""

        if((binding.etUsername.text != null && binding.etRepositoryName.text != null) && (binding.etUsername.text!!.isNotEmpty() && binding.etRepositoryName.text!!.isNotEmpty())) {
            filters += "${binding.etUsername.text.toString().trim()}/${binding.etRepositoryName.text.toString().trim()}"
        }
        else {
            showErrorSnackBar("Fill Username and Repository Name", true)
        }

        if(binding.etKeyword.text != null && binding.etKeyword.text!!.isNotEmpty()) {
            filters = binding.etKeyword.text.toString().trim() + " "+ filters
        }

        filters += if(binding.checkboxOpen.isChecked && binding.checkboxOpen.isChecked) {
            ""
        } else if(binding.checkboxOpen.isChecked) {
            " state:open"
        } else if(binding.checkboxClosed.isChecked) {
            " state:closed"
        } else {
            ""
        }

        return filters
    }


    // reset the filters to the default state
    fun clearAll(view: View) {
        if(binding.etKeyword.text != null)
            binding.etKeyword.text!!.clear()

        binding.checkboxOpen.isChecked = false
        binding.checkboxClosed.isChecked = true

        view.hideKeyboard()
    }


    // show error in form of snackbar whenever wrong parameters are given
    private fun showErrorSnackBar(msg: String, errorMsg: Boolean) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        if (errorMsg) {
            snackbarView.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorError
                )
            )
        } else {
            snackbarView.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorAccent
                )
            )
        }
        snackbar.show()
    }


    // hide virtual keyboard using this when button is clicked after edittext
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    // progress dialog functions
    fun showProgressDialog() {

        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        mProgressDialog.setCancelable(false)
        mProgressDialog.setCanceledOnTouchOutside(false)

        mProgressDialog.show()
    }

    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }
}
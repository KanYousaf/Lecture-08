package com.example.lecture06

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private var mList: ArrayList<String> = ArrayList()
    private var mList_adapter: ArrayAdapter<String>? = null

    //user search_array_list to duplicate season names for displaying data on search result
    private var search_array_list: MutableList<String> = ArrayList()
    private var search_adapter: ArrayAdapter<String>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        load_list_data()
        display_list_item()
        //click list item by using Click Listener
        list_clicked_function()
        //remove list item by using Long Click Listener
        remove_list_item()
    }

    // press save button to store list data in shared preference
    fun onSaveButtonPressed(view: View) {
        saveListArrayData()
        Toast.makeText(this, "Data Saved", Toast.LENGTH_SHORT).show()
    }

    // press insert button to put data in list
    fun onInsertButtonPressed(view: View) {
        val taskToAdd = ToDoListEditText.text.toString()
        mList.add(taskToAdd)
        ToDoListEditText.text.clear()
        display_list_item()
    }

    fun display_list_item() {
        //set up array adapter for the list
        mList_adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            mList
        )
        //attach adapter to the list
        displaySeasonList.adapter = mList_adapter
    }

    fun list_clicked_function() {
        //use anonymous class of list to select the item from list and move to another activity
        displaySeasonList.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item_name = parent?.getItemAtPosition(position).toString()
//                val rate = view!!.findViewById<RatingBar>(R.id.rate_season)
                Toast.makeText(this@MainActivity, "Task : ${item_name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun remove_list_item() {
        displaySeasonList.onItemLongClickListener = object : AdapterView.OnItemLongClickListener {
            override fun onItemLongClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ): Boolean {
                val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
                alertDialogBuilder.setTitle("ALERT")
                alertDialogBuilder.setMessage("Do you want to delete it ?")
                // Set a positive button and its click listener on alert dialog
                alertDialogBuilder.setPositiveButton("YES") { dialog, which ->
                    mList.removeAt(position)
//                    myFavSeasonImage.removeAt(position)
                    mList_adapter!!.notifyDataSetChanged()
                }

                alertDialogBuilder.setNegativeButton("NO") { dialog, which ->
                    dialog.dismiss()
                }

                alertDialogBuilder.setNeutralButton("MAY BE LATER") { dialog, which ->
                    Toast.makeText(this@MainActivity, "Changed my mind", Toast.LENGTH_SHORT).show()
                }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
                return true
            }
        }
    }

    override fun onPause() {
        saveListArrayData()
        Log.i("Test", "On Pause Called")
        super.onPause()
    }

    override fun onStop() {
        saveListArrayData()
        Log.i("Test", "On Stop Called")
        super.onStop()
    }

    fun saveListArrayData(): Boolean {
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            "mListDataFile",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.clear()
        val gson = Gson()
        val json_season_names = gson.toJson(mList)
        editor.putString("task_list", json_season_names)
        editor.apply()
        return true
    }

    fun load_list_data() {
        val sharedPreferences = getSharedPreferences(
            "mListDataFile",
            Context.MODE_PRIVATE
        )
        val gson = Gson()
        val received_json_list = sharedPreferences.getString("task_list", "")
        val type_task_list = object : TypeToken<ArrayList<String>>() {
        }.type
        if (received_json_list != null) {
            mList = gson.fromJson(received_json_list, type_task_list)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity2, menu)
        //convert search menu item as search view
        val searchItem = menu?.findItem(R.id.search)
        val searchView = searchItem?.actionView as SearchView

        searchView.queryHint = getString(R.string.search)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                search_array_list.clear()
                if (query!!.isNotEmpty()) {
                    val search = query.toLowerCase()
                    mList.forEach {
                        if (it.toLowerCase().contains(search)) {
                            search_array_list.add(it)
                        }
                    }
                    searchAdapter()
                    search_adapter!!.notifyDataSetChanged()
                } else {
                    searchAdapter()
                    search_array_list.clear()
                    search_array_list.addAll(mList)
                    search_adapter!!.notifyDataSetChanged()
                }
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    fun searchAdapter() {
        //set up array adapter for the search
        search_adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            search_array_list
        )
        //attach custom adapter to the list
        displaySeasonList.adapter = search_adapter
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var selectedItem = ""
        when (item?.itemId) {
            R.id.home -> selectedItem = "Home"
            R.id.setting -> {
                selectedItem = "Settings"
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.help -> selectedItem = "Help"
            R.id.exit -> exitProcess(0)
        }
        Toast.makeText(
            this, "You selected $selectedItem",
            Toast.LENGTH_SHORT
        ).show()
        return super.onOptionsItemSelected(item)
    }


    fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus
        view?.let { v ->
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}

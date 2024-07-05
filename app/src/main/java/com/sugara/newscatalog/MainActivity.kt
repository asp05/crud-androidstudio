package com.sugara.newscatalog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var dbNews: FirebaseFirestore
    private lateinit var myAdapter: AdapterList
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemList: MutableList<ItemList>
    private lateinit var alertDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize firebase
        FirebaseApp.initializeApp(this)
        dbNews = FirebaseFirestore.getInstance()

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val floatingActionButton = findViewById<FloatingActionButton>(R.id.fab)

        floatingActionButton.setOnClickListener {
            val intent = Intent(this, NewsAdd::class.java)
            startActivity(intent)
        }

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_progress_layout, null)

        alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false) // Optional: prevent user from dismissing the dialog
            .create()

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        itemList = mutableListOf()
        myAdapter = AdapterList(itemList)
        recyclerView.adapter = myAdapter

        myAdapter.setOnItemClickListener(object : AdapterList.OnItemClickListener {
            override fun onItemClick(item: ItemList) {
                val intent = Intent(this@MainActivity, NewsDetail::class.java).apply {
                    putExtra("id", item.id)
                    putExtra("title", item.title)
                    putExtra("desc", item.desc)
                    putExtra("imageUrl", item.imageUrl)
                }
                startActivity(intent)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        getData()
    }

    private fun getData() {
        alertDialog.show()
        dbNews.collection("news")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show()
                } else {
                    itemList.clear()
                    for (document in result) {
                        val imageUrl = document.data["imageUrl"].toString()
                        val noImage = R.drawable.empty_image

                        val imageToUse = if (imageUrl.isEmpty()) {
                            noImage
                        } else {
                            imageUrl
                        }

                        val item = ItemList(
                            document.id,
                            document.data["title"].toString(),
                            document.data["desc"].toString(),
                            imageUrl,
                            imageToUse
                        )

                        itemList.add(item)
                    }
                    myAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error getting documents.", exception)
                Toast.makeText(this, "Error getting documents", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                alertDialog.dismiss()
            }
    }
}
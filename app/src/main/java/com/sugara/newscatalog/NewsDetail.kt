package com.sugara.newscatalog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class NewsDetail : AppCompatActivity() {

    lateinit var newsTitle: TextView
    lateinit var newsDesc: TextView
    lateinit var newsImage: ImageView

    lateinit var edit : Button
    lateinit var delete : Button
    private lateinit var dbNews: FirebaseFirestore
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_progress_layout, null)

        alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false) // Optional: prevent user from dismissing the dialog
            .create()

        newsTitle = findViewById(R.id.newsTitle)
        newsDesc = findViewById(R.id.newsSubtitle)
        newsImage = findViewById(R.id.newsImage)

        edit = findViewById(R.id.editButton)
        delete = findViewById(R.id.deleteButton)

        dbNews = FirebaseFirestore.getInstance()

        val intent = intent
        val id = intent.getStringExtra("id")
        val title = intent.getStringExtra("title")
        val desc = intent.getStringExtra("desc")
        var imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val noImage = R.drawable.empty_image

        val imageToUse = if (imageUrl.isEmpty()) {
            noImage
        } else {
            imageUrl
        }

        newsTitle.text = title
        newsDesc.text = desc
        Glide.with(this)
            .load(imageToUse)
            .into(newsImage)


        edit.setOnClickListener {
            val intent = Intent(this, NewsAdd::class.java)
            intent.putExtra("id", id)
            intent.putExtra("title", title)
            intent.putExtra("desc", desc)
            intent.putExtra("imageUrl", imageUrl)
            startActivity(intent)
        }

        delete.setOnClickListener {
            //confirmation dialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete News")
            builder.setMessage("Are you sure you want to delete this news?")
            builder.setPositiveButton("Yes") { _, _ ->
                alertDialog.show()
                dbNews.collection("news").document(id!!)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Data successfully deleted!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        alertDialog.dismiss()
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error deleting data", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    }
            }
            builder.setNegativeButton("No") { _, _ ->
                builder.create().dismiss()
            }
            builder.show()

        }

    }
}
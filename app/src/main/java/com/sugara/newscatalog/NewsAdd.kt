package com.sugara.newscatalog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class NewsAdd : AppCompatActivity() {
    private lateinit var id: String
    private lateinit var title: EditText
    private lateinit var desc: EditText
    private lateinit var imageView: ImageView
    private lateinit var saveNews: Button
    private lateinit var chooseImage: Button

    private lateinit var alertDialog: AlertDialog

    private var imageUri: Uri? = null // nullable Uri to handle potential absence

    // Firebase references
    private val dbNews: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val PICK_IMAGE_REQUEST = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_add)

        //initialize ui
        title = findViewById(R.id.edit_text_title)
        desc = findViewById(R.id.edit_text_description)
        imageView = findViewById(R.id.image_view)
        saveNews = findViewById(R.id.button_save)
        chooseImage = findViewById(R.id.button_choose_image)

        val updateOption = intent
        if(updateOption != null){
            id = updateOption.getStringExtra("id") ?: ""
            title.setText(updateOption.getStringExtra("title"))
            desc.setText(updateOption.getStringExtra("desc"))
            val imageUrl = updateOption.getStringExtra("imageUrl")
            if (imageUrl != null) {
                Glide.with(this)
                    .load(imageUrl)
                    .into(imageView)
            }
        }

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_progress_layout, null)

        alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false) // Optional: prevent user from dismissing the dialog
            .create()

        chooseImage.setOnClickListener() {
            //logic to choose image
            openFileChooser()
        }

        saveNews.setOnClickListener() {
            //logic to save news
            val newsTitle = title.text.toString().trim()
            val newsDesc = desc.text.toString().trim()
            if (newsTitle.isEmpty() || newsDesc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.show()
                if(imageUri != null) {
                    uploadImageToStorage(newsTitle, newsDesc)
                } else {
                    saveData(newsTitle, newsDesc, "")
                }
            }
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.data != null
        ) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun uploadImageToStorage(newsTitle: String, newsDesc: String) {
        if (imageUri != null) {
            val storageRef = storage.reference.child("news_images/${System.currentTimeMillis()}.jpg")
            storageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener {
                        val imageUrl = it.toString()
                        saveData(newsTitle,newsDesc,imageUrl)
                    }
                }
                .addOnFailureListener {
                    Log.e("NewsAdd", "Failed to upload image to storage: ${it.message}")
                    alertDialog.dismiss()
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveData(newsTitle: String, newsDesc: String, imageUrl: String) {
        val news = hashMapOf(
            "title" to newsTitle,
            "desc" to newsDesc,
            "imageUrl" to imageUrl
        )
        Log.d("NewsAdd", "Saving news")
        Log.d("NewsId", id)
        if(id.isNotEmpty()){
            dbNews.collection("news")
                .document(id)
                .set(news)
                .addOnSuccessListener {
                    alertDialog.dismiss()
                    Toast.makeText(this, "News updated successfully", Toast.LENGTH_SHORT).show()
                    title.setText("")
                    desc.setText("")
                    imageView.setImageResource(0)

                    //open main activity
                    val intentMainActivity = Intent(this, MainActivity::class.java)
                    startActivity(intentMainActivity)

                    finish()
                }
                .addOnFailureListener {
                    Log.e("NewsAdd", "Failed to update news: ${it.message}")
                    alertDialog.dismiss()
                    Toast.makeText(this, "Failed to update news", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.d("NewsAdd", "Adding news")
            dbNews.collection("news")
                .add(news)
                .addOnSuccessListener {
                    alertDialog.dismiss()
                    Toast.makeText(this, "News added successfully", Toast.LENGTH_SHORT).show()
                    title.setText("")
                    desc.setText("")
                    imageView.setImageResource(0)
                }
                .addOnFailureListener {
                    Log.e("NewsAdd", "Failed to add news: ${it.message}")
                    alertDialog.dismiss()
                    Toast.makeText(this, "Failed to add news", Toast.LENGTH_SHORT).show()
                }
        }

    }



}
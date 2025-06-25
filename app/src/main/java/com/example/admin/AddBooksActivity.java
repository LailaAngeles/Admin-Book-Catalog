package com.example.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AddBooksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdaptor bookAdaptor;
    private List<BookDetails> bookList = new ArrayList<>();
    private CollectionReference booksRef;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#1C1C1C"));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.write_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigation buttons
        findViewById(R.id.Home).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        findViewById(R.id.createNew).setOnClickListener(v ->
                startActivity(new Intent(this, uploadBooksActivity.class)));

        findViewById(R.id.Editstory).setOnClickListener(v ->
                startActivity(new Intent(this, editBooksActivity.class)));

        // Setup RecyclerView
        recyclerView = findViewById(R.id.contentrecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bookAdaptor = new BookAdaptor(bookList);
        recyclerView.setAdapter(bookAdaptor);

        // Fetch book data
        fetchBooks();
    }

    private void fetchBooks() {
        ProgressBar progressBar = findViewById(R.id.load);
        progressBar.setVisibility(View.VISIBLE); // Show loader

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        booksRef = db.collection("Documents");

        booksRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        BookDetails book = document.toObject(BookDetails.class);
                        if (book != null) {
                            bookList.add(book);
                        }
                    }
                    bookAdaptor.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE); // Hide loader when done
                })
                .addOnFailureListener(e -> {
                    // Optionally show error toast
                    progressBar.setVisibility(View.GONE); // Hide loader even if failed
                });
    }

}

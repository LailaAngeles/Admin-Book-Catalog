package com.example.admin;

import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class editBooksActivity extends AppCompatActivity {
    // Firebase reference
    private CollectionReference booksRef;

    // Ito yung listahan ng lahat ng books at filtered na books
    private List<Book> bookList = new ArrayList<>();
    private List<Book> filteredList = new ArrayList<>();

    // Yung adapter na ginagamit para i-display yung list sa RecyclerView
    private BookAdapter bookAdapter;
    private EditText searchBar;
    private ProgressBar loadingIndicator;
    private String documentId;

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_main); // This links to edit_main.xml
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#1C1C1C")); // Dark gray
        }
        // return to AddBooksActivity
        ImageButton backbtn = findViewById(R.id.backButton);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(editBooksActivity.this, AddBooksActivity.class);
                startActivity(intent);
            }
        });
        // Initialize yung Firestore at kuha yung collection ng books
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        booksRef = db.collection("Documents");
        // Setup RecyclerView para sa list ng books
        RecyclerView recyclerView = findViewById(R.id.contentrecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookAdapter = new BookAdapter(filteredList);
        recyclerView.setAdapter(bookAdapter);
        loadingIndicator = findViewById(R.id.load);

        // Setup ng Search Bar para hanapin yung mga books
        searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBooks(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fetchBooks();

    }
    // Kinukuha lahat ng books from Firestore
    private void fetchBooks() {
        loadingIndicator.setVisibility(View.VISIBLE);
        booksRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Book book = document.toObject(Book.class);
                        if (book != null) {
                            book.setDocumentId(document.getId()); // Save ID para magamit sa update
                            bookList.add(book);
                        }

                    }
                    filteredList.clear();
                    filteredList.addAll(bookList);
                    bookAdapter.notifyDataSetChanged();
                    loadingIndicator.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(editBooksActivity.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    // Para ma-filter yung list based sa search input
    private void filterBooks(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(bookList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Book book : bookList) {
                if ((book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerCaseQuery)) ||
                        (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(lowerCaseQuery)) ||
                        (book.getGenre() != null && book.getGenre().toLowerCase().contains(lowerCaseQuery)) ||
                        (book.getTags() != null && book.getTags().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(book);
                }
            }
        }// I-refresh yung list view
        bookAdapter.notifyDataSetChanged();
    }

    /*public String getFilePathFromUri(Uri uri) {
        String filePath = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(projection[0]);
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return filePath;
    }*/

    // Ito yung Book model class (parang blueprint ng bawat book object)
    public static class Book {
        private String title, author, year, publisher, category, description, tags, imageUrl, imageBase64;

        public Book() {}
        // Ito yung Book model class (parang blueprint ng bawat book object)
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getYear() { return year; }
        public void setYear(String year) { this.year = year; }

        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }

        public String getGenre() { return category; }
        public void setGenre(String category) { this.category = category; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public String getImageBase64() { return imageBase64; }
        public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64;
        }
        private String documentId;

        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

    }

    // Book Adapter
    public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

        private List<Book> bookList;

        public BookAdapter(List<Book> bookList) {
            this.bookList = bookList;
        }

        @NonNull
        @Override
        public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.update_list_adaptor, parent, false);
            return new BookViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
            Book book = bookList.get(position);
            holder.bookTitle.setText(book.getTitle());
            holder.bookAuthor.setText("by: " + book.getAuthor());
            holder.bookDescription.setText("Description: " + book.getDescription());
            holder.bookTags.setText(book.getTags());

            // Kung may image (base64 format), i-convert to Bitmap para ipakita
            if (book.getImageBase64() != null && !book.getImageBase64().isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(book.getImageBase64(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    holder.bookImage.setImageBitmap(bitmap != null ? bitmap : getPlaceholderImage());
                } catch (IllegalArgumentException e) {
                    holder.bookImage.setImageResource(R.drawable.placeholder_image);
                }
            } else {
                holder.bookImage.setImageResource(R.drawable.placeholder_image);
            }

            // Kapag pinindot mo yung book, pupunta sa update screen with the book data
            holder.itemView.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
            // Store yung image sa SharedPreferences para madala sa next activity
                SharedPreferences prefs = context.getSharedPreferences("Images", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(book.getTitle(), book.getImageBase64());
                editor.apply();
                // I-send lahat ng book data papunta sa updateBooks.java
                Intent intent = new Intent(context, updateBooks.class);
                intent.putExtra("documentId", book.getDocumentId());
                intent.putExtra("title", book.getTitle());
                intent.putExtra("author", book.getAuthor());
                intent.putExtra("year", book.getYear());
                intent.putExtra("publisher", book.getPublisher());
                intent.putExtra("genre", book.getGenre());
                intent.putExtra("description", book.getDescription());
                intent.putExtra("tags", book.getTags());
                context.startActivity(intent);
            });
        }

        // Placeholder kapag walang image yung book
        private Bitmap getPlaceholderImage() {
            return BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image);
        }

        @Override
        public int getItemCount() {
            return bookList.size(); // total number of items sa RecyclerView
        }
        // ViewHolder — dito naka-bind lahat ng mga view para sa bawat book
        class BookViewHolder extends RecyclerView.ViewHolder {
            TextView bookTitle, bookAuthor, bookYear, bookPublisher, bookGenre, bookDescription, bookTags;
            ImageView bookImage;

            public BookViewHolder(@NonNull View itemView) {
                super(itemView);
                bookTitle = itemView.findViewById(R.id.book_title);
                bookAuthor = itemView.findViewById(R.id.book_author);
                bookDescription = itemView.findViewById(R.id.book_Description);
                bookTags = itemView.findViewById(R.id.book_tags);
                bookImage = itemView.findViewById(R.id.book_image);
            }
        }
    }
}

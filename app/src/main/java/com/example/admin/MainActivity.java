package com.example.admin;
import com.google.firebase.firestore.PropertyName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
// Main activity ng app, ito yung unang lalabas pag binuksan yung app
public class MainActivity extends AppCompatActivity {

    // Ito yung mga variables na gagamitin sa buong activity
    private List<Book> bookList = new ArrayList<>(); // Lahat ng books galing sa Firestore
    private List<Book> filteredList = new ArrayList<>(); // Ito naman yung filtered na list based sa search
    private BookAdapter bookAdapter; // Adapter para i-connect yung list sa RecyclerView
    private EditText searchBar; // Yung search bar kung saan magta-type si user
    private CollectionReference booksRef; // Reference ng collection sa Firestore
    private ProgressBar Load; // Yung loading spinner habang nagfe-fetch ng data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set layout file

        // Kinukuha natin yung ProgressBar sa layout
        Load = findViewById(R.id.load);

        // Kung Android version is Lollipop or higher, binabago natin kulay ng status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#1C1C1C"));
        }

        // Ini-initialize natin si Firebase
        FirebaseApp.initializeApp(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        booksRef = db.collection("Documents"); // Ito yung Firestore collection

        // Setup ng RecyclerView para sa listahan ng books
        RecyclerView recyclerView = findViewById(R.id.contentrecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Linear layout, pang-scroll pababa
        bookAdapter = new BookAdapter(filteredList);
        recyclerView.setAdapter(bookAdapter);

        // Hanapin si search bar sa layout
        searchBar = findViewById(R.id.search_bar);

        // Listener para ma-detect kung may tina-type si user sa search bar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {} // Di kailangan dito

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBooks(s.toString()); // Tawagin function na magfa-filter ng books
            }

            @Override
            public void afterTextChanged(Editable s) {} // Optional din
        });

        // Kapag pinindot yung "Add Books" button, pupunta sa AddBooksActivity
        ImageButton addBooksButton = findViewById(R.id.AddBooks);
        addBooksButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddBooksActivity.class);
            startActivity(intent);
        });

        fetchBooks(); // Tawagin function para kunin lahat ng books sa Firestore
    }

    // Function para i-fetch lahat ng books galing Firestore
    private void fetchBooks() {
        Load.setVisibility(View.VISIBLE); // Ipakita yung loading spinner
        booksRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookList.clear(); // Clear muna para fresh
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Book book = document.toObject(Book.class); // Convert Firestore document to Book object
                        bookList.add(book); // Add sa list

                        System.out.println("Fetched book: " + book.getTitle()); // Pang-debug lang
                    }
                    filteredList.clear();
                    filteredList.addAll(bookList); // Ipakita lahat muna pag walang search
                    bookAdapter.notifyDataSetChanged(); // I-refresh yung RecyclerView
                    Load.setVisibility(View.GONE); // Tagalin na spinner
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    System.out.println("Firestore Fetch Error: " + e.getMessage());
                });
    }

    // Function para i-filter yung bookList base sa search query
    private void filterBooks(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(bookList); // Kung walang input, ipakita lahat
        } else {
            String lowerCaseQuery = query.toLowerCase(); // Para case insensitive
            for (Book book : bookList) {
                // Check kung pasok yung query sa title, author, genre, or tags
                if (book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(book);
                }
                else if (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(book);
                }
                else if (book.getGenre() != null && book.getGenre().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(book);
                }
                else if (book.getTags() != null && book.getTags().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(book);
                }
            }
        }
        bookAdapter.notifyDataSetChanged(); // Refresh display
    }

    // Function para kunin ang file path from URI (e.g., image picker)
    public String getFilePathFromUri(Uri uri) {
        String filePath = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(projection[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;
    }

    // Model class ng Book — ito yung structure ng data na sinasave sa Firestore
    public static class Book {
        private String title, author, year, publisher, category, description, tags, imageUrl, imageBase64;

        public Book() {} // Kailangan ni Firestore ng empty constructor

        // Getters and Setters — para ma-access at ma-edit yung values ng Book object
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
        public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    }

    // Adapter class para i-bind ang Book objects sa layout ng list item (RecyclerView)
    public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

        private List<Book> bookList;

        public BookAdapter(List<Book> bookList) {
            this.bookList = bookList;
        }

        @NonNull
        @Override
        public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_list_adaptor, parent, false);
            return new BookViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
            Book book = bookList.get(position);

            // Set lahat ng data sa TextViews
            holder.bookTitle.setText(book.getTitle());
            holder.bookAuthor.setText("by: " + book.getAuthor());
            holder.bookYear.setText(book.getYear());
            holder.bookPublisher.setText("Publisher: " + book.getPublisher());
            holder.bookGenre.setText("Genre: " + book.getGenre());
            holder.bookDescription.setText("Description: " + book.getDescription());
            holder.bookTags.setText(book.getTags());

            // Decode and set image kung merong base64 image
            if (book.getImageBase64() != null && !book.getImageBase64().isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(book.getImageBase64(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    holder.bookImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.bookImage.setImageResource(R.drawable.placeholder_image); // fallback image
                }
            } else {
                holder.bookImage.setImageResource(R.drawable.placeholder_image); // fallback image
            }
        }

        @Override
        public int getItemCount() {
            return bookList.size(); // Bilang ng items sa list
        }

        // ViewHolder para hawakan yung views sa bawat item
        class BookViewHolder extends RecyclerView.ViewHolder {
            TextView bookTitle, bookAuthor, bookYear, bookPublisher, bookGenre, bookDescription, bookTags;
            ImageView bookImage;

            public BookViewHolder(@NonNull View itemView) {
                super(itemView);
                bookTitle = itemView.findViewById(R.id.book_title);
                bookAuthor = itemView.findViewById(R.id.book_author);
                bookYear = itemView.findViewById(R.id.book_year);
                bookPublisher =


                        itemView.findViewById(R.id.book_publisher);
                bookGenre = itemView.findViewById(R.id.book_genre);
                bookDescription = itemView.findViewById(R.id.book_Description);
                bookTags = itemView.findViewById(R.id.book_tags);
                bookImage = itemView.findViewById(R.id.book_image);
            }
        }
    }
}

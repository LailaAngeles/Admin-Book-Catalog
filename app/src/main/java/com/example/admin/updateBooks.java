package com.example.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.widget.ImageView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class updateBooks extends AppCompatActivity {


    EditText editTitle, editAuthor, editDescription, editYear, editPublisher, editTags;
    Spinner editCategory;
    MaterialButton updateBtn, editCoverBtn;
    ImageButton backbtn;
    ImageView editcoverIcon;
    ImageButton deleteBtn;
    TextView tagPreview;

    FirebaseFirestore db;
    String documentId;
    private static final int PICK_IMAGE_REQUEST = 1;

    String[] categories = {
            "Unknown Genre",
            "Mythology/Folklore",
            "Educational/Informational",
            "Realistic Fiction",
            "Fantasy/Adventure"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editstory);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#1C1C1C"));
        }

        db = FirebaseFirestore.getInstance();

        // ginagamit para ma-link yung Java variables sa mga UI elements mo sa XML layout.
        editcoverIcon = findViewById(R.id.editcoverIcon);
        editTitle = findViewById(R.id.editTitle);
        editAuthor = findViewById(R.id.editAuthor);
        editDescription = findViewById(R.id.editDescription);
        editYear = findViewById(R.id.editYear);
        editPublisher = findViewById(R.id.editPublisher);
        editTags = findViewById(R.id.editTags);
        editCategory = findViewById(R.id.editCategory);
        updateBtn = findViewById(R.id.update);
        backbtn = findViewById(R.id.editBackBtn);
        tagPreview = findViewById(R.id.tagPreview);
        editCoverBtn = findViewById(R.id.editCoverBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        //delete btn
        deleteBtn.setOnClickListener(v -> {
            //PAG PININDOT UNG DELETE BTN LALABAS YUNG ALERT DIALOG PARA MA MAKE SURE IF TRIP BA TALANG BURAHIN NI ADMIN UNG BOOK OR HINDI
            new androidx.appcompat.app.AlertDialog.Builder(updateBooks.this)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this book?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (documentId != null) {
                            //iF YES PINILI NI USER GAMIT UNG DOCUMENTS FROM FIRESTORE IS YUNG MAG MAMATCH NA ID IS BUBURAHIN
                            db.collection("Documents").document(documentId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(updateBooks.this, "Book deleted successfully.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(updateBooks.this, editBooksActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(updateBooks.this, "Deletion failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        } else {
                            Toast.makeText(updateBooks.this, "Document ID is missing.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });


        // Spinner adapter to show categories ng books like Fantasy, Realistic Fiction, etc. Pinapasa yung array na categories.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editCategory.setAdapter(adapter);

        //  Dito kinukuha yung data na pinasa galing sa ibang activity editBooksActivity).
        //  So kung gusto mo i-edit yung book, yung mga details nito ay pinapasa as extras sa Intent.
        Intent intent = getIntent();
        if (intent != null) {
            documentId = intent.getStringExtra("documentId");

            editTitle.setText(intent.getStringExtra("title"));
            editAuthor.setText(intent.getStringExtra("author"));
            editDescription.setText(intent.getStringExtra("description"));
            editYear.setText(intent.getStringExtra("year"));
            editPublisher.setText(intent.getStringExtra("publisher"));
            editTags.setText(intent.getStringExtra("tags"));

            String genre = intent.getStringExtra("genre");
            int index = Arrays.asList(categories).indexOf(genre);
            editCategory.setSelection(index >= 0 ? index : 0);

            String coverImageUrl = intent.getStringExtra("coverImageUrl");
            if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(coverImageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .into(editcoverIcon);
            }

            // Fallback to Firestore image if needed
            db.collection("Documents")
                    .document(documentId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String imageUrl = snapshot.getString("imageUrl");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.placeholder_image)
                                        .into(editcoverIcon);
                            } else {
                                String base64 = snapshot.getString("imageBase64");
                                if (base64 != null && !base64.isEmpty()) {
                                    try {
                                        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        editcoverIcon.setImageBitmap(bmp);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        editcoverIcon.setImageResource(R.drawable.placeholder_image);
                                    }
                                }
                            }

                            // Ensure genre is updated if incorrect or missing
                            String firestoreGenre = snapshot.getString("genre");
                            if (firestoreGenre != null) {
                                int genreIndex = Arrays.asList(categories).indexOf(firestoreGenre);
                                editCategory.setSelection(genreIndex >= 0 ? genreIndex : 0);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("UpdateBooks", "Error fetching image data", e));
        }

        //Habang tina-type mo yung tags, ito automatic na nilalagyan ng # prefix para maging proper hashtags. Live preview sa tagPreview.
        editTags.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                String[] tagArray = s.toString().split(" ");
                Set<String> uniqueTags = new LinkedHashSet<>();
                StringBuilder previewBuilder = new StringBuilder();
                boolean duplicateFound = false;

                for (String tag : tagArray) {
                    String cleanTag = tag.trim().replaceAll("^#+", "").toLowerCase();
                    if (!cleanTag.isEmpty()) {
                        if (!uniqueTags.add(cleanTag)) {
                            duplicateFound = true;// I-flag if may duplicate
                        }
                    }
                }

                for (String tag : uniqueTags) {
                    previewBuilder.append("#").append(tag).append(" ");
                }

                tagPreview.setText(previewBuilder.toString().trim());

                if (duplicateFound) {
                    editTags.setError("Duplicate tags are not allowed (case-insensitive)");
                } else {
                    editTags.setError(null);  // Clear error if all is fine
                }
            }
        });

        // Pag pinindot mo yung "edit cover" button, bubuksan nito yung gallery para mamili ka ng bagong image.
        editCoverBtn.setOnClickListener(v -> openImagePicker());

        // Back button
        backbtn.setOnClickListener(v -> {
            startActivity(new Intent(updateBooks.this, editBooksActivity.class));
            finish();
        });

        //Pag pinindot yung Update button, kinukuha lahat ng values from input fields,
        // then sinesend sa Firestore after optionally converting the image to Base64 format.
        updateBtn.setOnClickListener(v -> {
            if (!validateInput()) {
                return;
            }
            String title = editTitle.getText().toString().trim();
            String author = editAuthor.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            String year = editYear.getText().toString().trim();
            String publisher = editPublisher.getText().toString().trim();
            String rawTags = editTags.getText().toString().trim();
            String[] tagArray = rawTags.split("\\s+");
            StringBuilder formattedTags = new StringBuilder();
            for (String tag : tagArray) {
                tag = tag.replace("#", "").trim(); // Remove all # and trim extra spaces
                if (!tag.isEmpty()) {
                    formattedTags.append("#").append(tag).append(" ");
                }
            }
            String tags = formattedTags.toString().trim();
            String category = editCategory.getSelectedItem().toString();
            uploadImageAndUpdate(title, author, description, year, publisher, tags, category);
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Pagkatapos mong pumili ng image, ito yung callback na magse-set ng image sa ImageView gamit Glide.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Glide.with(this).load(data.getData()).into(editcoverIcon);
        }
    }

    private void uploadImageAndUpdate(String title, String author, String description, String year, String publisher, String tags, String category) {
        Drawable drawable = editcoverIcon.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
            updateFirestoreData(title, author, description, year, publisher, tags, category, base64Image);
        } else {
            updateFirestoreData(title, author, description, year, publisher, tags, category, null);
        }
    }
   // Dito na talaga ina-update yung Firestore record gamit yung documentId ng book.
   // Pag success, babalik siya sa main edit list (editBooksActivity).
    private void updateFirestoreData(String title, String author, String description, String year, String publisher, String tags, String category, @Nullable String base64Image) {
        if (documentId == null) {
            Toast.makeText(this, "Document ID not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("title", title);
        updatedData.put("author", author);
        updatedData.put("description", description);
        updatedData.put("year", year);
        updatedData.put("publisher", publisher);
        updatedData.put("tags", tags);
        updatedData.put("genre", category);

        if (base64Image != null) {
            updatedData.put("imageBase64", base64Image);
        }

        db.collection("Documents").document(documentId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(updateBooks.this, "Book updated successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(updateBooks.this, editBooksActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(updateBooks.this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
    // Ito yung method para i-validate kung may laman ba yung mga required fields.
    // Kapag may kulang, magse-set ito ng error message at hindi magpro-proceed sa update.
    private boolean validateInput() {
        String title = editTitle.getText().toString().trim();
        String author = editAuthor.getText().toString().trim();
        String desc = editDescription.getText().toString().trim();
        String year = editYear.getText().toString().trim();
        String publisher = editPublisher.getText().toString().trim();

        if(title.length() < 2){
            editTitle.setError("Must be a valid Title");
            return false;
        } if (author.length() < 2) {
            editAuthor.setError("Must be a valid author name");
            return false;
        }
        if (desc.length() < 2) {
            editDescription.setError("Must be a valid Description");
            return false;
        }
        if (publisher.length() < 2) {
            editPublisher.setError("Must be a valid Publisher");
            return false;
        }

        if (title.isEmpty()) {
            editTitle.setError("Kailangan ilagay ang pamagat");
            return false;
        }

        if (author.isEmpty()) {
            editAuthor.setError("Kailangan ilagay ang author");
            return false;
        }
        if (desc.isEmpty()) {
            editDescription.setError("Kailangan ilagay ang description");
            return false;
        }
        if (year.isEmpty()) {
            editYear.setError("Kailangan ilagay ang taon ng pagkalathala");
            return false;
        }
        try {
            int yearInt = Integer.parseInt(year);
            if (yearInt < 1000 || yearInt > 2025) {
                editYear.setError("Ilagay ang valid na 4-digit na taon");
                return false;
            }
        } catch (NumberFormatException e) {
            editYear.setError("Invalid na format ng taon");
            return false;
        }
        if (publisher.isEmpty()) {
            editPublisher.setError("Kailangan ilagay ang publisher");
            return false;
        }
        return true; // Kung pasado lahat, tuloy sa pag-update
    }

}

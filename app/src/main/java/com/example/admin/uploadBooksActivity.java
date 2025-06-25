package com.example.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.admin.databinding.InputstoryBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;

public class uploadBooksActivity extends AppCompatActivity {

    StorageReference storageReference;
    LinearProgressIndicator progress;
    Uri image;
    MaterialButton selectImage, uploadimage;
    ImageView imageView;
    InputstoryBinding binding;
    private FirebaseFirestore db;
    private ProgressDialog pd;
    private String selectedCategory = "Unknown Genre";

    // Ginagamit ito para ma-handle ang pagselect ng image gamit ang Intent
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    uploadimage.setEnabled(true);// I-e-enable lang kapag may napiling image
                    image = result.getData().getData();// I-store natin yung image URI
                    Glide.with(getApplicationContext()).load(image).into(imageView);// Preview ng image
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = InputstoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(uploadBooksActivity.this);

        imageView = findViewById(R.id.coverIcon);
        selectImage = findViewById(R.id.selectImagebtn);
        uploadimage = findViewById(R.id.save);
        uploadimage.setEnabled(true);

        pd = new ProgressDialog(this);
        db = FirebaseFirestore.getInstance();

        selectImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });
        // Preview ng image// Preview ng image
        uploadimage.setOnClickListener(view -> {
            if (validateInput()) {  // ih check if complete na ung inputs and if tama
                uploadimage(image);//Proceed sa upload
            }
        });

        //Dito mine make sure na tama yung tags and autamically na mag lalagayu ng # sa db
        binding.tags.addTextChangedListener(new TextWatcher() {
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

                binding.tagPreview.setText(previewBuilder.toString().trim());

                if (duplicateFound) {
                    binding.tags.setError("Duplicate tags are not allowed (case-insensitive)");
                } else {
                    binding.tags.setError(null);  // Clear error if all is fine
                }
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#1C1C1C"));
        }

        setupCategorySpinner();
        setupBackButton();
    }

    private void setupCategorySpinner() {
        String[] categories = {
                "Unknown Genre",
                "Mythology/Folklore",
                "Educational/Informational",
                "Realistic Fiction",
                "Fantasy/Adventure",
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.category.setAdapter(adapter);

        binding.category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCategory = categories[position];
            }
            @Override public void onNothingSelected(AdapterView<?> parentView) {
                selectedCategory = "Unknown Genre";
            }
        });
    }

    private void setupBackButton() {
        binding.Backbtm.setOnClickListener(v -> {
            startActivity(new Intent(uploadBooksActivity.this, AddBooksActivity.class));
        });
    }

    private boolean validateInput() {
        String title = binding.addtile.getText().toString().trim();
        String author = binding.addAuthor.getText().toString().trim();
        String desc = binding.addDescription.getText().toString().trim();
        String year = binding.yearCreated.getText().toString().trim();
        String publisher = binding.publisher.getText().toString().trim();
        if (title.length() < 2) {
            binding.addtile.setError("Must be a valid Title");
            return false;
        }
        if (author.length() < 2) {
            binding.addAuthor.setError("Must be a valid author name");
            return false;
        }

        if (desc.length() < 2) {
            binding.addDescription.setError("Must be a valid Description");
            return false;
        }
        if (publisher.length() < 2) {
            binding.publisher.setError("Must be a valid Publisher");
            return false;
        }
        if (title.isEmpty()) {
            binding.addtile.setError("Title is required");
            return false;
        }
        if (author.isEmpty()) {
            binding.addAuthor.setError("Author is required");
            return false;
        }
        if (desc.isEmpty()) {
            binding.addDescription.setError("Description is required");
            return false;
        }
        if (year.isEmpty()) {
            binding.yearCreated.setError("Year Released is required");
            return false;
        }
        try {
            int yearInt = Integer.parseInt(year);
            if (yearInt < 1000 || yearInt > 2025) {
                binding.yearCreated.setError("Enter a valid 4-digit year");
                return false;
            }
        } catch (NumberFormatException e) {
            binding.yearCreated.setError("Invalid year format");
            return false;
        }
        if (publisher.isEmpty()) {
            binding.publisher.setError("Publisher is required");
            return false;
        }
        return true;
    }

    //convert muna sa base64 bago ih save
    private void uploadimage(Uri file) {
        pd.setTitle("Processing Image...");
        pd.show();

        String base64Image = convertImageToBase64(file);    //Ito ung taga convert
        saveDataWithImage(base64Image);//taga save ng details
    }
    //Convertion ng img URI to base 64 string pag rekta na string kasi is hindi gagana do ma s-save pero hindi mag a-apppear
    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            int maxDimension = 1024;
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            float scale = Math.min((float) maxDimension / width, (float) maxDimension / height);

            int scaledWidth = Math.round(width * scale);
            int scaledHeight = Math.round(height * scale);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            byte[] compressedBytes = outputStream.toByteArray();
            outputStream.close();

            return android.util.Base64.encodeToString(compressedBytes, android.util.Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveDataWithImage(String imageBase64) {
        String title = binding.addtile.getText().toString().trim();
        String author = binding.addAuthor.getText().toString().trim();
        String desc = binding.addDescription.getText().toString().trim();
        String tags = binding.tags.getText().toString().trim();
        String year = binding.yearCreated.getText().toString().trim();
        String publisher = binding.publisher.getText().toString().trim();

        StringBuilder tagBuilder = new StringBuilder();
        Set<String> uniqueTags = new LinkedHashSet<>();

        for (String tag : tags.split("\\s+")) {
            if (!tag.trim().isEmpty()) {
                uniqueTags.add(tag.trim().toLowerCase());
            }
        }

        for (String tag : uniqueTags) {
            String cleanTag = tag.replaceAll("^#+", "").toLowerCase(); // Remove leading #
            if (!cleanTag.isEmpty()) {
                tagBuilder.append("#").append(cleanTag).append(" ");
            }
        }


        String id = UUID.randomUUID().toString();

        Map<String, Object> doc = new HashMap<>();
        doc.put("id", id);
        doc.put("title", title);
        doc.put("author", author);
        doc.put("description", desc);
        doc.put("genre", selectedCategory);
        doc.put("imageBase64", imageBase64);
        doc.put("tags", tagBuilder.toString().trim());
        doc.put("year", year);
        doc.put("publisher", publisher);

        pd.setTitle("Saving Book Info...");
        pd.show();

        checkIfBookExists(id, doc);
    }

    private void checkIfBookExists(String id, Map<String, Object> bookData) {
        db.collection("Documents")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean duplicateFound = false;
                    for (var document : queryDocumentSnapshots) {
                        String existingTitle = document.getString("title");
                        String existingAuthor = document.getString("author");

                        if (existingTitle != null && existingAuthor != null) {
                            if (existingTitle.equalsIgnoreCase((String) bookData.get("title"))
                                    && existingAuthor.equalsIgnoreCase((String) bookData.get("author"))) {
                                duplicateFound = true;
                                break;
                            }
                        }
                    }

                    if (duplicateFound) {
                        pd.dismiss();
                        Toast.makeText(uploadBooksActivity.this, "This book already exists!", Toast.LENGTH_SHORT).show();
                    } else {
                        uploadToFirestore(id, bookData);
                    }
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(uploadBooksActivity.this, "Failed to check duplicates: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    // I-flag kung may duplikado
    private void uploadToFirestore(String id, Map<String, Object> data) {
        db.collection("Documents")
                .document(id)
                .set(data)
                .addOnSuccessListener(unused -> {
                    pd.dismiss();
                    Toast.makeText(uploadBooksActivity.this, "Book uploaded successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(uploadBooksActivity.this, "Failed to upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

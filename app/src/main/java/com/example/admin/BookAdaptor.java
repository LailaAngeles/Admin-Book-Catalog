package com.example.admin;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookAdaptor extends RecyclerView.Adapter<BookAdaptor.BookViewHolder> {

    private List<BookDetails> bookList;

    public BookAdaptor(List<BookDetails> bookList) {
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.book_image_adaptorr, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BookDetails book = bookList.get(position);

        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText(book.getAuthor());

        // Set Image Safely
        if (book.getImageBase64() != null && !book.getImageBase64().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(book.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    holder.imageView.setImageBitmap(bitmap);
                } else {
                    holder.imageView.setImageResource(R.drawable.placeholder_image);
                }
            } catch (Exception e) {
                holder.imageView.setImageResource(R.drawable.placeholder_image);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_image);
        }
/*
        // Handle Item Click
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();

            SharedPreferences prefs = context.getSharedPreferences("Images", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(book.getTitle(), book.getImageBase64());
            editor.apply();

            Intent intent = new Intent(context, BookDetails.class);
            intent.putExtra("title", book.getTitle());
            intent.putExtra("author", book.getAuthor());
            intent.putExtra("year", book.getYear());
            intent.putExtra("publisher", book.getPublisher());
            intent.putExtra("genre", book.getGenre());
            intent.putExtra("description", book.getDescription());
            intent.putExtra("tags", book.getTags());
            context.startActivity(intent);
        });*/
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView, authorTextView;

        BookViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.book_image);
            titleTextView = itemView.findViewById(R.id.book_title);
            authorTextView = itemView.findViewById(R.id.book_author);
        }
    }
}

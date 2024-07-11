package com.example.myapplication22;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class ListingActivity extends AppCompatActivity {

    private DatabaseHelper sqliteDatabase;
    private List<Track> tracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.verticalLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sqliteDatabase = new DatabaseHelper(this);
        sqliteDatabase.getWritableDatabase(); // !!!

        tracks = sqliteDatabase.getAllTracks(); // Get your list of tracks from the database or elsewhere

        TableLayout tableLayout = findViewById(R.id.tableLayout);

        for (Track t : tracks) {
            TableRow row = new TableRow(this);
            //row.setPadding(5,5,5,5);

            String whiteHexColor = "#00FFFFFF"; // Example hexadecimal color code
            int color = Color.parseColor(whiteHexColor);
            ColorDrawable colorDrawable = new ColorDrawable(color);
            int padding = 20;

            Button textView = new Button(this);
            textView.setText(t.getName());
            textView.setTextSize(15);
            textView.setHeight(100);
            textView.setBackground(colorDrawable);
            textView.setTextColor(Color.BLACK);
            textView.setPadding(0, padding, padding, padding);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ListingActivity.this, InspectActivity.class);
                    intent.putExtra("trackid",t.getId() ); // passing track id
                    startActivity(intent);
                }
            });
            row.addView(textView);

            Button editButton = new Button(this);
            editButton.setText("RENAME");
            editButton.setTextSize(15);
            editButton.setHeight(100);
            editButton.setBackground(colorDrawable);
            editButton.setTextColor(Color.BLACK);
            editButton.setPadding(padding, padding, padding, padding);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // fetch new name with dialog here
                    showRenameDialog(t.getId(), t.getName());

                    //String newName = "";
                    //sqliteDatabase.updateSingleTrackById(t.getId(), newName,null,null,null);
                    //recreate();
                }
            });
            row.addView(editButton);

            Button deleteButton = new Button(this);
            deleteButton.setText("DELETE");
            deleteButton.setHeight(100);
            deleteButton.setTextSize(15);
            deleteButton.setBackground(colorDrawable);
            deleteButton.setTextColor(Color.BLACK);
            deleteButton.setPadding(padding,padding,padding,padding);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sqliteDatabase.updateSingleTrackById(t.getId(), null,null,null, null, null,"1");
                    tableLayout.removeView(row);
                }
            });
            row.addView(deleteButton);

            // Add the row to the table layout
            tableLayout.addView(row);
        }

        ImageView listingToMainButton = findViewById(R.id.listingToMainButton);
        listingToMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showRenameDialog(long trackId, String currentName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Track");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentName);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    // Update track name in database
                    sqliteDatabase.updateSingleTrackById(trackId, newName, null, null, null,null, null);
                    // Refresh the activity to reflect changes
                    recreate();
                } else {
                    Toast.makeText(ListingActivity.this, "Please enter new name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
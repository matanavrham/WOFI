package com.example.wofi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * מסך פרופיל בעל מקצוע
 * מציג את פרטי בעל המקצוע, מאפשר צפייה בדירוגים, עדכון תמונת פרופיל (לבעל המקצוע עצמו)
 * ומאפשר ללקוחות לדרג את בעל המקצוע
 */
public class ProfessionalProfileFragment extends Fragment {

    // רכיבי ממשק המשתמש
    private ShapeableImageView profileImage;
    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView emailTextView;
    private TextView addressTextView;
    private TextView descriptionTextView;
    private RatingBar ratingBar;
    private TextView ratingText;
    private MaterialButton callButton;

    // מופעי Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    // קבועים לבקשות הרשאות
    private static final int CALL_PHONE_PERMISSION_REQUEST = 1;
    private static final int CAMERA_PERMISSION_REQUEST = 2;
    private static final int STORAGE_PERMISSION_REQUEST = 3;

    // משתנים גלובליים
    private String currentPhotoPath;
    private String currentUserId;

    /**
     * מאתחל את ה-launcher למצלמה
     * מטפל בתוצאה מהמצלמה ומעלה את התמונה לשרת
     */
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                uploadImage(Uri.fromFile(new File(currentPhotoPath)));
            }
        }
    );

    /**
     * מאתחל את ה-launcher לגלריה
     * מטפל בתוצאה מהגלריה ומעלה את התמונה לשרת
     */
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                uploadImage(result.getData().getData());
            }
        }
    );

    public ProfessionalProfileFragment() {
        super(R.layout.fragment_professional_profile);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_professional_profile, container, false);

        // אתחול רכיבי הממשק
        initializeViews(view);

        // הגדרת סרגל הכלים
        setupToolbar(view);

        // אתחול מופעי Firebase
        initializeFirebase();

        // טעינת נתוני בעל המקצוע
        loadProfessionalDataFromArguments();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // הגדרת מאזיני לחיצה
        setupClickListeners();
    }

    /**
     * מאתחל את כל רכיבי הממשק
     */
    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        nameTextView = view.findViewById(R.id.name_text);
        phoneTextView = view.findViewById(R.id.phone_text);
        emailTextView = view.findViewById(R.id.email_text);
        addressTextView = view.findViewById(R.id.address_text);
        descriptionTextView = view.findViewById(R.id.description_text);
        ratingBar = view.findViewById(R.id.rating_bar);
        ratingText = view.findViewById(R.id.rating_text);
        callButton = view.findViewById(R.id.call_button);
    }

    /**
     * מגדיר את סרגל הכלים ואת כפתור החזרה
     */
    private void setupToolbar(View view) {
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> 
            Navigation.findNavController(view).navigateUp()
        );
    }

    /**
     * מאתחל את מופעי Firebase
     */
    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    /**
     * טוען את נתוני בעל המקצוע מהארגומנטים
     */
    private void loadProfessionalDataFromArguments() {
        if (getArguments() != null) {
            currentUserId = getArguments().getString("userId");
            if (currentUserId != null) {
                loadProfessionalData(currentUserId);
            } else {
                Toast.makeText(getContext(), "שגיאה: לא נמצא מזהה משתמש", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "שגיאה: לא נמצאו נתוני בעל מקצוע", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * מגדיר את מאזיני הלחיצה לרכיבים השונים
     */
    private void setupClickListeners() {
        // הגדרת מאזין לכפתור התקשרות
        callButton.setOnClickListener(v -> {
            String phoneNumber = phoneTextView.getText().toString();
            if (!phoneNumber.equals("לא צוין")) {
                makePhoneCall(phoneNumber);
            }
        });

        // הגדרת מאזין לתמונת הפרופיל (רק לבעל המקצוע עצמו)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(currentUserId)) {
            profileImage.setOnClickListener(v -> showImageSourceDialog());
        }

        // הגדרת מאזין לדירוג (רק ללקוחות)
        if (currentUser != null && !currentUser.getUid().equals(currentUserId)) {
            View ratingContainer = getView().findViewById(R.id.rating_container);
            ratingContainer.setOnClickListener(v -> {
                Toast.makeText(getContext(), "לחץ לדירוג בעל המקצוע", Toast.LENGTH_SHORT).show();
                showRatingDialog(currentUserId);
            });
        }
    }

    /**
     * מציג דיאלוג לבחירת מקור התמונה (מצלמה או גלריה)
     */
    private void showImageSourceDialog() {
        String[] options = {"מצלמה", "גלריה", "ביטול"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("בחר מקור תמונה")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // מצלמה
                    if (checkCameraPermission()) {
                        dispatchTakePictureIntent();
                    }
                } else if (which == 1) {
                    // גלריה
                    if (checkStoragePermission()) {
                        openGallery();
                    }
                }
            })
            .show();
    }

    /**
     * בודק הרשאות מצלמה
     * @return true אם יש הרשאה, false אחרת
     */
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    /**
     * בודק הרשאות אחסון
     * @return true אם יש הרשאה, false אחרת
     */
    private boolean checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    /**
     * מפעיל את המצלמה לצילום תמונה
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getContext(), "שגיאה ביצירת קובץ תמונה", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        "com.example.wofi.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    /**
     * יוצר קובץ תמונה חדש
     * @return קובץ התמונה שנוצר
     * @throws IOException אם יש שגיאה ביצירת הקובץ
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * פותח את הגלריה לבחירת תמונה
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    /**
     * מעלה תמונה לשרת האחסון של Firebase
     * @param imageUri ה-URI של התמונה להעלאה
     */
    private void uploadImage(Uri imageUri) {
        if (currentUserId == null) return;

        StorageReference storageRef = storage.getReference()
                .child("profile_images")
                .child(currentUserId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                db.collection("users").document(currentUserId)
                                        .update("profileImageUrl", uri.toString())
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "תמונת הפרופיל עודכנה בהצלחה", 
                                                Toast.LENGTH_SHORT).show();
                                            loadProfessionalData(currentUserId);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "שגיאה בעדכון תמונת הפרופיל", 
                                                Toast.LENGTH_SHORT).show();
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בהעלאת התמונה", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * מבצע שיחה טלפונית
     * @param phoneNumber מספר הטלפון להתקשר אליו
     */
    private void makePhoneCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PHONE_PERMISSION_REQUEST);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        }
    }

    /**
     * מטפל בתוצאות בקשות ההרשאות
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case CALL_PHONE_PERMISSION_REQUEST:
                    String phoneNumber = phoneTextView.getText().toString();
                    if (!phoneNumber.equals("לא צוין")) {
                        makePhoneCall(phoneNumber);
                    }
                    break;
                case CAMERA_PERMISSION_REQUEST:
                    dispatchTakePictureIntent();
                    break;
                case STORAGE_PERMISSION_REQUEST:
                    openGallery();
                    break;
            }
        } else {
            Toast.makeText(getContext(), "ההרשאה נדרשת לביצוע הפעולה", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * טוען את נתוני בעל המקצוע מהשרת
     * @param userId מזהה בעל המקצוע
     */
    private void loadProfessionalData(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // טעינת פרטים בסיסיים
                        nameTextView.setText(documentSnapshot.getString("name"));
                        phoneTextView.setText(documentSnapshot.getString("phone"));
                        emailTextView.setText(documentSnapshot.getString("email"));
                        addressTextView.setText(documentSnapshot.getString("address"));
                        descriptionTextView.setText(documentSnapshot.getString("description"));

                        // טעינת תמונת פרופיל
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.default_profile)
                                    .error(R.drawable.default_profile)
                                    .into(profileImage);
                        }

                        // טעינת דירוגים
                        Double rating = documentSnapshot.getDouble("rating");
                        Long ratingCount = documentSnapshot.getLong("ratingCount");
                        if (rating != null && ratingCount != null) {
                            ratingBar.setRating(rating.floatValue());
                            ratingText.setText(String.format(Locale.getDefault(), 
                                "%.1f (%d דירוגים)", rating, ratingCount));
                        } else {
                            ratingBar.setRating(0);
                            ratingText.setText("אין דירוגים עדיין");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בטעינת נתוני בעל המקצוע", 
                        Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * מציג דיאלוג לדירוג בעל המקצוע
     * @param professionalId מזהה בעל המקצוע
     */
    private void showRatingDialog(String professionalId) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rate_professional, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        MaterialButton submitButton = dialogView.findViewById(R.id.submit_button);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        submitButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (rating > 0) {
                submitRating(professionalId, rating);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "אנא בחר דירוג", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    /**
     * שומר דירוג חדש לבעל המקצוע
     * @param professionalId מזהה בעל המקצוע
     * @param rating הדירוג החדש
     */
    private void submitRating(String professionalId, float rating) {
        db.collection("users").document(professionalId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Double currentRating = documentSnapshot.getDouble("rating");
                    Long currentCount = documentSnapshot.getLong("ratingCount");

                    double newRating;
                    long newCount;

                    if (currentRating != null && currentCount != null) {
                        newCount = currentCount + 1;
                        newRating = ((currentRating * currentCount) + rating) / newCount;
                    } else {
                        newRating = rating;
                        newCount = 1;
                    }

                    db.collection("users").document(professionalId)
                            .update("rating", newRating, "ratingCount", newCount)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "הדירוג נשמר בהצלחה", 
                                    Toast.LENGTH_SHORT).show();
                                loadProfessionalData(professionalId);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "שגיאה בשמירת הדירוג", 
                                    Toast.LENGTH_SHORT).show();
                            });
                });
    }
}

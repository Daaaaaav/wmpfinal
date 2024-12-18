package com.example.wmpfinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EnrollmentActivity extends AppCompatActivity {

    private Button addButton, summaryButton;
    private RecyclerView recyclerViewSubjects;
    private SubjectAdapter subjectAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int totalCredits = 0;
    private static final int MAX_CREDITS = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        recyclerViewSubjects = findViewById(R.id.recyclerViewSubjects);
        recyclerViewSubjects.setLayoutManager(new LinearLayoutManager(this));

        addButton = findViewById(R.id.addButton);
        summaryButton = findViewById(R.id.summaryButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadSubjects();

        addButton.setOnClickListener(v -> addSubjects());

        summaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(EnrollmentActivity.this, EnrollmentSummaryActivity.class);
            startActivity(intent);
        });
    }

    private void loadSubjects() {
        db.collection("subjects").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<SubjectModel> subjects = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String name = document.getString("name");
                    Long credits = document.getLong("credits");
                    if (name != null && credits != null) {
                        subjects.add(new SubjectModel(name, credits.intValue()));
                    }
                }
                subjectAdapter = new SubjectAdapter(subjects);
                recyclerViewSubjects.setAdapter(subjectAdapter);
            } else {
                Toast.makeText(this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSubjects() {
        String userId = mAuth.getCurrentUser().getUid();
        List<SubjectModel> selectedSubjects = subjectAdapter.getSelectedSubjects();

        if (selectedSubjects.isEmpty()) {
            Toast.makeText(this, "No subjects selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalSelectedCredits = selectedSubjects.stream().mapToInt(SubjectModel::getCredits).sum();

        db.collection("students").document(userId).get().addOnSuccessListener(studentDoc -> {
            if (studentDoc.exists()) {
                Long totalCreditsObj = studentDoc.getLong("totalCredits");
                AtomicInteger currentTotalCredits = new AtomicInteger(totalCreditsObj != null ? totalCreditsObj.intValue() : 0);
                if (currentTotalCredits.get() + totalSelectedCredits > MAX_CREDITS) {
                    Toast.makeText(this, "Credit limit exceeded! Maximum allowed credits: " + MAX_CREDITS, Toast.LENGTH_SHORT).show();
                    return;
                }

                for (SubjectModel subject : selectedSubjects) {
                    db.collection("students").document(userId)
                            .collection("enrolledSubjects")
                            .whereEqualTo("name", subject.getName())
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null && task.getResult().isEmpty()) {
                                    currentTotalCredits.addAndGet(subject.getCredits());
                                    db.collection("students").document(userId)
                                            .update("totalCredits", currentTotalCredits.get());
                                    db.collection("students").document(userId)
                                            .collection("enrolledSubjects")
                                            .add(subject)
                                            .addOnSuccessListener(docRef ->
                                                    Toast.makeText(this, subject.getName() + " added successfully!", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(this, "Failed to add " + subject.getName(), Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(this, subject.getName() + " already enrolled!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else {
                Toast.makeText(this, "Student record not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
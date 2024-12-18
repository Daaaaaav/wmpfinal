package com.example.wmpfinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private final List<SubjectModel> subjects;
    private final List<SubjectModel> selectedSubjects = new ArrayList<>();

    public SubjectAdapter(List<SubjectModel> subjects) {
        this.subjects = subjects;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        SubjectModel subject = subjects.get(position);
        holder.nameTextView.setText(subject.getName());
        holder.creditsTextView.setText(subject.getCredits() + " credits");

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedSubjects.contains(subject));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedSubjects.add(subject);
            } else {
                selectedSubjects.remove(subject);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public List<SubjectModel> getSelectedSubjects() {
        return selectedSubjects;
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, creditsTextView;
        CheckBox checkBox;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.subjectName);
            creditsTextView = itemView.findViewById(R.id.subjectCredits);
            checkBox = itemView.findViewById(R.id.subjectCheckBox);
        }
    }
}

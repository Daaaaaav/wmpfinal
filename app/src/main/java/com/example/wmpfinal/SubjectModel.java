package com.example.wmpfinal;

public class SubjectModel {
    private String name;
    private int credits;

    public SubjectModel(String name, int credits) {
        this.name = name;
        this.credits = credits;
    }

    public String getName() {
        return name;
    }

    public int getCredits() {
        return credits;
    }
}

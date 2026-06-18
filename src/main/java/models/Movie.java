package com.videolibrary.models;

import java.io.Serializable;

/**
 * Represents a Movie entity in the Video Library System.
 * Implements Serializable to allow network transmission via RMI.
 */
public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String title;
    private String genre;

    public Movie(int id, String title, String genre) {
        this.id = id;
        this.title = title;
        this.genre = genre;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }

    @Override
    public String toString() {
        return title + " (" + genre + ")";
    }
}
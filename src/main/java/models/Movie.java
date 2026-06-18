package com.videolibrary.models;

import java.io.Serializable;

/**
 * Represents a Movie entity within the Video Library System.
 * This class implements Serializable to allow movie objects to be transmitted
 * across the network between the RMI Server and the JavaFX Clients.
 * * @author Gift
 * @version 1.0
 */
public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String title;
    private String genre;
    /**
     * Constructs a new Movie instance.
     * * @param id    The unique database identifier for the movie.
     * @param title The official title of the movie.
     * @param genre The category or genre the movie belongs to.
     */
    public Movie(int id, String title, String genre) {
        this.id = id;
        this.title = title;
        this.genre = genre;
    }
    /**
     * Retrieves the database ID of the movie.
     * * @return The integer ID of the movie.
     */

    public int getId() { return id; }
    /**
     * Retrieves the title of the movie.
     * * @return The string representation of the movie's title.
     */
    public String getTitle() { return title; }
    /**
     * Retrieves the genre of the movie.
     * * @return The string representation of the movie's genre.
     */
    public String getGenre() { return genre; }
    /**
     * Returns a formatted string representation of the movie for UI display.
     * * @return A string containing the title and genre (e.g., "The Matrix (Action)").
     */
    @Override
    public String toString() {
        return title + " (" + genre + ")";
    }
}
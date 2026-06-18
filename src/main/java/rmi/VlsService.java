package com.videolibrary.rmi;

import com.videolibrary.models.Movie;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * The remote interface for the Video Library System.
 * This interface defines all the callable database operations that the
 * Admin and Customer clients can request from the central RMI Server.
 * * @author Trip
 * @version 1.0
 */
public interface VlsService extends Remote {

    /**
     * Registers a new genre into the database.
     * * @param genreName The name of the genre to be added.
     * @return true if the genre was successfully saved, false otherwise.
     * @throws RemoteException if a network communication error occurs.
     */
    boolean registerGenre(String genreName) throws RemoteException;

    /**
     * Registers a new movie into the database under a specific genre.
     * * @param title The title of the movie.
     * @param genre The genre category for the movie.
     * @return true if the movie was successfully saved, false otherwise.
     * @throws RemoteException if a network communication error occurs.
     */
    boolean registerMovie(String title, String genre) throws RemoteException;

    /**
     * Registers a new customer into the database.
     * * @param name  The full name of the customer.
     * @param phone The contact phone number of the customer.
     * @param email The contact email address of the customer.
     * @return true if the customer was successfully saved, false otherwise.
     * @throws RemoteException if a network communication error occurs.
     */
    boolean registerCustomer(String name, String phone, String email) throws RemoteException;

    /**
     * Retrieves a list of all active genres from the database.
     * * @return A List of strings representing genre names.
     * @throws RemoteException if a network communication error occurs.
     */
    List<String> getGenres() throws RemoteException;

    /**
     * Retrieves a list of all active movies belonging to a specific genre.
     * * @param genre The name of the genre to filter by.
     * @return A List of Movie objects matching the specified genre.
     * @throws RemoteException if a network communication error occurs.
     */
    List<Movie> getMoviesByGenre(String genre) throws RemoteException;

    /**
     * Retrieves a list of all active registered customers from the database.
     * * @return A List of strings representing customer names.
     * @throws RemoteException if a network communication error occurs.
     */
    List<String> getCustomers() throws RemoteException;

    /**
     * Assigns a movie rental to a specific customer.
     * * @param customerName The name of the customer renting the movie.
     * @param movieId      The unique ID of the movie being rented.
     * @return true if the rental was successfully recorded, false otherwise.
     * @throws RemoteException if a network communication error occurs.
     */
    boolean rentMovie(String customerName, int movieId) throws RemoteException;

    /**
     * Processes the return of a previously rented movie.
     * * @param customerName The name of the customer returning the movie.
     * @param movieId      The unique ID of the movie being returned.
     * @return true if the return was successfully updated, false otherwise.
     * @throws RemoteException if a network communication error occurs.
     */
    boolean returnMovie(String customerName, int movieId) throws RemoteException;

    /**
     * Retrieves a list of movies currently borrowed (and not yet returned) by a customer.
     * * @param customerName The name of the customer to look up.
     * @return A List of Movie objects currently in the customer's possession.
     * @throws RemoteException if a network communication error occurs.
     */
    List<Movie> getBorrowedMovies(String customerName) throws RemoteException;

    /**
     * Retrieves a list of movies that a customer has previously borrowed and successfully returned.
     * * @param customerName The name of the customer to look up.
     * @return A List of Movie objects returned by the customer.
     * @throws RemoteException if a network communication error occurs.
     */
    List<Movie> getReturnedMovies(String customerName) throws RemoteException;

    /**
     * Performs a soft delete to remove a genre from active listings.
     * * @param genreName The name of the genre to deactivate.
     * @return true if the genre was successfully deactivated, false otherwise.
     * @throws RemoteException if a network communication error occurs.
     */
    boolean removeGenre(String genreName) throws RemoteException;

    /**
     * Performs a soft delete to remove a movie from active listings.
     * * @param movieId The unique ID of the movie to deactivate.
     * @return true if the movie was successfully deactivated, false otherwise.
     * @throws RemoteException if a network communication error occurs.
     */
    boolean removeMovie(int movieId) throws RemoteException;
}
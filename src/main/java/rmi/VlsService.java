package com.videolibrary.rmi;

import com.videolibrary.models.Movie;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote Interface defining the database capabilities provided by the VLS Server.
 */
public interface VlsService extends Remote {
    // Admin Operations
    boolean registerMovie(String title, String genre) throws RemoteException;
    boolean registerCustomer(String name, String phone, String email) throws RemoteException;

    // Customer/Rental Operations
    List<String> getGenres() throws RemoteException;
    List<Movie> getMoviesByGenre(String genre) throws RemoteException;
    List<String> getCustomers() throws RemoteException;
    boolean rentMovie(String customerName, int movieId) throws RemoteException;
    boolean returnMovie(String customerName, int movieId) throws RemoteException;
    boolean registerGenre(String genreName) throws RemoteException;
    boolean removeGenre(String genreName) throws RemoteException;
    boolean removeMovie(int movieId) throws RemoteException;
    List<Movie> getBorrowedMovies(String customerName) throws RemoteException;
    List<Movie> getReturnedMovies(String customerName) throws RemoteException;
}

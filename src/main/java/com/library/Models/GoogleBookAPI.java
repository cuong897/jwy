package com.library.Models;

import com.google.api.services.books.v1.Books;
import com.google.api.services.books.v1.BooksRequestInitializer;
import com.google.api.services.books.v1.model.Volume;
import com.google.api.services.books.v1.model.Volumes;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class GoogleBookAPI {
    private static final Long MAX_RESULTS_PER_QUERY = 30L;
    private static final String API_KEY = "";
    private static final String APPLICATION_NAME = "";
    public static List<Volume> searchBooks(String query) throws IOException {
        Books books = new Books.Builder(new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(), null)
                .setApplicationName(APPLICATION_NAME)
                .setGoogleClientRequestInitializer(new BooksRequestInitializer(API_KEY))
                .build();

        Books.Volumes.List volumesList = books.volumes().list(query);
        volumesList.setMaxResults(30L);
        Volumes volumes = volumesList.execute();

        List<Volume> validBooks = new ArrayList<>();
        for (Volume volume : volumes.getItems()) {
            Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();

            String isbn = getISBN(volumeInfo);
            if (isbn != null) {
                validBooks.add(volume);
            }
        }

        return validBooks;
    }
public static String getISBN(Volume.VolumeInfo volumeInfo) {
        if (volumeInfo.getIndustryIdentifiers() != null) {
            for (Volume.VolumeInfo.IndustryIdentifiers identifier : volumeInfo.getIndustryIdentifiers()) {
                if ("ISBN_13".equals(identifier.getType()) || "ISBN_10".equals(identifier.getType())) {
                    return identifier.getIdentifier();
                }
            }
        }
        return null;
    }
}

package com.library.Models; // Hoặc package đúng của bạn

import com.google.api.client.json.gson.GsonFactory; // Import đầy đủ
import com.google.api.services.books.v1.Books;
import com.google.api.services.books.v1.BooksRequestInitializer;
import com.google.api.services.books.v1.model.Volume;
import com.google.api.services.books.v1.model.Volumes;
import com.google.api.client.http.javanet.NetHttpTransport; // Import đầy đủ

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections; // Import Collections
import java.util.List;

public class GoogleBookAPI {
    // API Key nên được bảo vệ, không hardcode nếu chia sẻ code
    private static final String API_KEY = "AIzaSyCn2_cox32slijmKI5HB9CTytpK9_z7Bdo"; // CHÚ Ý BẢO MẬT
    private static final String APPLICATION_NAME = "MyLibraryApplication"; // Đặt tên ứng dụng cụ thể

    public static List<Volume> searchBooks(String query) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            System.out.println("GoogleBookAPI: Query rỗng, trả về danh sách rỗng.");
            return Collections.emptyList();
        }

        // Khởi tạo GsonFactory một lần để tái sử dụng nếu có thể, hoặc để như này cũng được
        com.google.api.client.json.JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        com.google.api.client.http.HttpTransport httpTransport = new NetHttpTransport();


        Books books = new Books.Builder(httpTransport, jsonFactory, null)
                .setApplicationName(APPLICATION_NAME)
                .setGoogleClientRequestInitializer(new BooksRequestInitializer(API_KEY))
                .build();

        Books.Volumes.List volumesListRequest = books.volumes().list(query);
        volumesListRequest.setMaxResults(30L); // Giữ nguyên giới hạn của bạn
        // volumesListRequest.setPrintType("books"); // Có thể thêm để chỉ lấy sách

        Volumes volumesResult;
        try {
            System.out.println("GoogleBookAPI: Đang thực hiện truy vấn cho: '" + query + "'");
            volumesResult = volumesListRequest.execute();
        } catch (IOException e) {
            System.err.println("Lỗi IOException khi thực thi truy vấn Google Books API cho query '" + query + "': " + e.getMessage());
            // e.printStackTrace(); // Có thể in ra để debug sâu hơn
            throw e; // Ném lại lỗi để Task biết và xử lý trong setOnFailed
        }

        if (volumesResult == null) {
            System.out.println("GoogleBookAPI: volumesResult là null sau khi execute cho query: " + query);
            return Collections.emptyList();
        }

        if (volumesResult.getTotalItems() == null || volumesResult.getTotalItems() == 0) {
            System.out.println("GoogleBookAPI: Không tìm thấy sách (TotalItems = 0 hoặc null) cho query: " + query);
            return Collections.emptyList();
        }

        List<Volume> items = volumesResult.getItems();

        // === ĐÂY LÀ PHẦN SỬA LỖI QUAN TRỌNG ===
        if (items == null) {
            System.out.println("GoogleBookAPI: volumesResult.getItems() trả về NULL cho query: " + query + ". Trả về danh sách rỗng.");
            return Collections.emptyList(); // Trả về danh sách rỗng thay vì null
        }
        // =======================================

        List<Volume> validBooks = new ArrayList<>();
        System.out.println("GoogleBookAPI: Tìm thấy " + items.size() + " items cho query: " + query);
        for (Volume volume : items) { // Bây giờ 'items' chắc chắn không null
            if (volume != null && volume.getVolumeInfo() != null) {
                String isbn = getISBN(volume.getVolumeInfo());
                if (isbn != null) {
                    validBooks.add(volume);
                }
            }
        }
        System.out.println("GoogleBookAPI: Đã lọc được " + validBooks.size() + " sách hợp lệ (có ISBN).");
        return validBooks;
    }

    public static String getISBN(Volume.VolumeInfo volumeInfo) {
        if (volumeInfo != null && volumeInfo.getIndustryIdentifiers() != null) {
            // Ưu tiên ISBN_13
            for (Volume.VolumeInfo.IndustryIdentifiers identifier : volumeInfo.getIndustryIdentifiers()) {
                if (identifier != null && "ISBN_13".equals(identifier.getType())) {
                    return identifier.getIdentifier();
                }
            }
            // Nếu không có ISBN_13, tìm ISBN_10
            for (Volume.VolumeInfo.IndustryIdentifiers identifier : volumeInfo.getIndustryIdentifiers()) {
                if (identifier != null && "ISBN_10".equals(identifier.getType())) {
                    return identifier.getIdentifier();
                }
            }
        }
        return null;
    }
}
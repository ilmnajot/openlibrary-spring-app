package uz.ilmnajot.openlibraryspringapp.service;

import uz.ilmnajot.openlibraryspringapp.model.response.OpenLibrarySearchResponse;

public interface AuthorService {
    OpenLibrarySearchResponse searchAuthor(String name);
}

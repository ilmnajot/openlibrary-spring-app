package uz.ilmnajot.openlibraryspringapp.service;

import uz.ilmnajot.openlibraryspringapp.model.AuthorResponse;

import java.util.List;

public interface AuthorService {
    List<AuthorResponse> searchAuthor(String name);
}

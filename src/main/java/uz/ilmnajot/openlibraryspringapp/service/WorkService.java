package uz.ilmnajot.openlibraryspringapp.service;

import uz.ilmnajot.openlibraryspringapp.model.WorkResponse;

import java.util.List;

public interface WorkService {
    List<WorkResponse> getWorksByAuthor(String authorId);
}

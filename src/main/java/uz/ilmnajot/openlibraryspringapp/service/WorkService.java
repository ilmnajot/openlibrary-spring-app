package uz.ilmnajot.openlibraryspringapp.service;

import uz.ilmnajot.openlibraryspringapp.model.WorkResponseDto;

import java.util.List;

public interface WorkService {
    List<WorkResponseDto> getWorksByAuthor(String authorId);
}

package uz.ilmnajot.openlibraryspringapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.ilmnajot.openlibraryspringapp.entity.Author;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, String> {
    List<Author> findByAuthorNameContainingIgnoreCase(String name);

    Optional<Author> findByAuthorId(String authorId);
}

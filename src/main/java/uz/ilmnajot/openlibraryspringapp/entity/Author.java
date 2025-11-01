package uz.ilmnajot.openlibraryspringapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String authorId;

    private String authorName;

    // Many-to-Many relationship with Works
    @ManyToMany(mappedBy = "authors")
    private List<Work> works = new ArrayList<>();

    public Author(String authorId, String authorName) {
        this.authorId = authorId;
        this.authorName = authorName;
    }
}

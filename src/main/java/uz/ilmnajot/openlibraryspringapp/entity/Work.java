package uz.ilmnajot.openlibraryspringapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Work {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String workId;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "work_subjects", joinColumns = @JoinColumn(name = "work_id"))
    @Column(name = "subject")
    private List<String> subjects = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "work_covers", joinColumns = @JoinColumn(name = "work_id"))
    @Column(name = "cover_id")
    private List<Long> covers = new ArrayList<>();

    // Many-to-Many relationship with Authors
    @ManyToMany
    @JoinTable(
            name = "work_authors",
            joinColumns = @JoinColumn(name = "work_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<Author> authors = new ArrayList<>();
}

package uz.ilmnajot.openlibraryspringapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.ilmnajot.openlibraryspringapp.entity.Work;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkRepository extends JpaRepository<Work, Long>{

    Optional<Work> findByWorkId(String workKey);
}

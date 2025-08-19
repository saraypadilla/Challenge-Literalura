package com.alura.literalura.repository;

import com.alura.literalura.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    // Autores vivos en un año dado
    List<Author> findByBirthYearLessThanEqualAndDeathYearGreaterThanEqual(Integer year1, Integer year2);
}


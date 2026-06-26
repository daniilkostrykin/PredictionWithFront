package org.example.prediction.repositories;

import org.example.prediction.models.entities.Prize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrizeRepository extends JpaRepository<Prize, Long> {
    List<Prize> findAllByOrderByStatusAscDrawDateAsc();
    Page<Prize> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);

}

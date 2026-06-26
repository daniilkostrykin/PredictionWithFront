package org.example.prediction.services;

import org.example.prediction.dto.form.AddPrizeDto;
import org.example.prediction.models.entities.Prize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PrizeService {
    List<Prize> getAllPrizes();

    @Transactional
    void createPrize(AddPrizeDto form);

    @Transactional
    void buyTicket(Long prizeId, String username);

    @Transactional
    void performDraw(Long prizeId);

    Page<Prize> searchPrizes(String search, Pageable pageable);
}

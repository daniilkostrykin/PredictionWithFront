package org.example.prediction.dto;

import java.time.Instant;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowDetailedEventInfoDto implements java.io.Serializable {
    private Long id;
    private String title;
    private String description;
    private String status;
    private Instant closesAt;
    private List<OptionDto> optionsWithStats;
}

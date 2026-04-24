package com.bigsmo.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogBatchRequest {

    @NotEmpty(message = "logs array must not be empty")
    @Size(min = 1, max = 1000, message = "logs array must have between 1 and 1000 items")
    @Valid
    private List<IncomingLogDto> logs;
}
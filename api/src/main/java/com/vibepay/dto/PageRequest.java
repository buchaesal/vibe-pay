package com.vibepay.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 페이징 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private int page = 0;

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    private int size = 10;

    public long getOffset() {
        return (long) page * size;
    }
}
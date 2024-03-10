package com.swig.manda.dto;


import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailDto {
    private Long id;

    @Pattern(regexp = ".{0,15}", message = "상세정보는 15자 이하로 입력해주세요.")
    private String content;
    private Long mainTopicId;

    private String UserId;

}

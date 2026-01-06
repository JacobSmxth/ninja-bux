package dev.jsmitty.bux.system.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record NinjaListResponse(List<NinjaResponse> ninjas, long totalCount) {

    public static NinjaListResponse from(Page<NinjaResponse> page) {
        return new NinjaListResponse(page.getContent(), page.getTotalElements());
    }
}

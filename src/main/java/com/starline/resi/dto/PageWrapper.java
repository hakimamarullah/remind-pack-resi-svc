package com.starline.resi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageWrapper<T> implements Serializable {

    private List<T> content;

    private PageMetadata page;

    public PageWrapper(PagedModel<T> pagedModel) {
        this.content = pagedModel.getContent();
        this.page = new PageMetadata();
        PagedModel.PageMetadata metadata = pagedModel.getMetadata();
        this.page.setNumber(Optional.ofNullable(metadata).map(PagedModel.PageMetadata::number).orElse(0L));
        this.page.setSize(Optional.ofNullable(metadata).map(PagedModel.PageMetadata::size).orElse(0L));
        this.page.setTotalElements(Optional.ofNullable(metadata).map(PagedModel.PageMetadata::totalElements).orElse(0L));
        this.page.setTotalPages(Optional.ofNullable(metadata).map(PagedModel.PageMetadata::totalPages).orElse(0L));
    }

    public PageWrapper(Page<T> pagedModel) {
        this.content = pagedModel.getContent();
        this.page = new PageMetadata();
        this.page.setNumber(pagedModel.getNumber());
        this.page.setSize(pagedModel.getSize());
        this.page.setTotalElements(pagedModel.getTotalElements());
        this.page.setTotalPages(pagedModel.getTotalPages());
    }

    @JsonIgnore
    public static <T> PageWrapper<T> of(Page<T> pagedModel) {
        return new PageWrapper<>(pagedModel);
    }

    @JsonIgnore
    public PageMetadata getMetadata() {
        return page;
    }

    @Setter
    @Getter
    public static class PageMetadata implements Serializable {
        private long number;
        private long size;
        private long totalElements;
        private long totalPages;
    }
}

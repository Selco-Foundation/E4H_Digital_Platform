package org.selco.e4h.web.models.storage;

import lombok.*;

import java.util.List;


@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StorageResponse {
    @With
    private List<File> files;
}

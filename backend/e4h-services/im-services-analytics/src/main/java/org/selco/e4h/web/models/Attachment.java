package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Model for email attachments
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Attachment {
    
    @JsonProperty("fileName")
    private String fileName;
    
    @JsonProperty("contentType")
    private String contentType;
    
    @JsonProperty("base64Content")
    private String base64Content;
    
    @JsonProperty("fileSize")
    private Long fileSize;
}
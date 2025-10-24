package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Model for email notifications
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailNotification {
    
    @JsonProperty("to")
    private String to;
    
    @JsonProperty("subject")
    private String subject;
    
    @JsonProperty("body")
    private String body;
    
    @JsonProperty("isHTML")
    private Boolean isHTML;
    
    @JsonProperty("attachments")
    private List<Attachment> attachments;
    
    @JsonProperty("cc")
    private List<String> cc;
    
    @JsonProperty("bcc")
    private List<String> bcc;
}
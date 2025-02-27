package org.egov.im.web.models;

import lombok.Data;

@Data
public class ProcessingTimes {
    private long transcodeTime;
    private long hlsTime;
    private long uploadTime;

    public long getTotal() {
        return transcodeTime + hlsTime + uploadTime;
    }
}
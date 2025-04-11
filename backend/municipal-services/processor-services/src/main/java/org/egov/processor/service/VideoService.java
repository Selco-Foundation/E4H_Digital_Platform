package org.egov.processor.service;

import org.egov.processor.models.ProcessingContext;

import java.io.File;

public interface VideoService {

    void processVideoAsync(File inputFile, ProcessingContext context);
}

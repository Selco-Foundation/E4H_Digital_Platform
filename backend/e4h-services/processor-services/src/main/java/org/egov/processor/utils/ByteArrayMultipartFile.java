package org.egov.processor.utils;


import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;

@Slf4j
@Builder
public class ByteArrayMultipartFile implements MultipartFile {
    private final byte[] content;
    private final String name;
    private final String originalFilename;
    private final String contentType;

    public ByteArrayMultipartFile(byte[] content, String name, String originalFilename, String contentType) {
        log.trace("Method invoked: ByteArrayMultipartFile constructor, filename: {}, content size: {}", originalFilename, content != null ? content.length : 0);
        this.content = content;
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        log.trace("Method invoked: getName");
        return name;
    }

    @Override
    public String getOriginalFilename() {
        log.trace("Method invoked: getOriginalFilename");
        return originalFilename;
    }

    @Override
    public String getContentType() {
        log.trace("Method invoked: getContentType");
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        log.trace("Method invoked: isEmpty");
        boolean empty = content.length == 0;
        log.debug("File isEmpty check: {}, filename: {}", empty, originalFilename);
        return empty;
    }

    @Override
    public long getSize() {
        log.trace("Method invoked: getSize");
        long size = content.length;
        log.debug("File size: {} bytes, filename: {}", size, originalFilename);
        return size;
    }

    @Override
    public byte[] getBytes() throws IOException {
        log.trace("Method invoked: getBytes, filename: {}", originalFilename);
        log.debug("Returning {} bytes for file: {}", content.length, originalFilename);
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        log.trace("Method invoked: getInputStream, filename: {}", originalFilename);
        log.debug("Creating input stream for file: {}", originalFilename);
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        log.trace("Method invoked: transferTo, destination: {}", dest != null ? dest.getAbsolutePath() : "null");
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            log.debug("Writing {} bytes to file: {}", content.length, dest.getAbsolutePath());
            fos.write(content);
            log.debug("Successfully transferred file: {}", dest.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error transferring file to: {}", dest != null ? dest.getAbsolutePath() : "null", e);
            throw e;
        }
    }
}


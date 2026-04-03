package org.egov.processor.models.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@AllArgsConstructor
public class CommonMultipartFile implements MultipartFile {

    private Resource resource;
    private String filename;

    @Override
    public String getName() {
        return filename;
    }

    @Override
    public String getOriginalFilename() {
        return filename;
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public boolean isEmpty() {
        log.trace("Method invoked: isEmpty, filename: {}", filename);
        try {
            boolean empty = resource.contentLength() == 0;
            log.debug("File isEmpty check: {}, filename: {}", empty, filename);
            return empty;
        } catch (IOException e) {
            log.warn("Error checking if file is empty, filename: {}", filename, e);
            return true;
        }
    }

    @Override
    public long getSize() {
        log.trace("Method invoked: getSize, filename: {}", filename);
        try {
            long size = resource.contentLength();
            log.debug("File size: {} bytes, filename: {}", size, filename);
            return size;
        } catch (IOException e) {
            log.warn("Error getting file size, filename: {}", filename, e);
            return 0;
        }
    }

    @Override
    public byte[] getBytes() throws IOException {
        log.trace("Method invoked: getBytes, filename: {}", filename);
        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            log.debug("Read {} bytes from file: {}", bytes.length, filename);
            return bytes;
        } catch (IOException e) {
            log.error("Error reading bytes from file: {}", filename, e);
            throw e;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        log.trace("Method invoked: getInputStream, filename: {}", filename);
        try {
            InputStream inputStream = resource.getInputStream();
            log.debug("Opened input stream for file: {}", filename);
            return inputStream;
        } catch (IOException e) {
            log.error("Error opening input stream for file: {}", filename, e);
            throw e;
        }
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        log.trace("Method invoked: transferTo, filename: {}, destination: {}", filename, dest != null ? dest.getAbsolutePath() : "null");
        try (InputStream in = resource.getInputStream();
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            log.debug("Successfully transferred {} bytes from file: {} to: {}", totalBytes, filename, dest.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error transferring file: {} to: {}", filename, dest != null ? dest.getAbsolutePath() : "null", e);
            throw e;
        }
    }

}

package org.egov.filestore.domain.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.filestore.domain.model.FileInfo;
import org.egov.filestore.domain.model.FileLocation;
import org.egov.filestore.domain.model.Resource;
import org.egov.filestore.persistence.entity.Artifact;
import org.egov.filestore.persistence.repository.ArtifactRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @InjectMocks
    private StorageService storageService;

    @Mock
    private ArtifactMapper artifactMapper;

    @Mock
    private ArtifactRepository artifactRepository;

    FileLocation fl = FileLocation.builder()
            .fileName("foo.txt")
            .tag("Tag")
            .fileStoreId("42")
            .fileSource("File Source")
            .tenantId("42")
            .module("Module")
            .build();

    @Test
    void testSave() {
        ArrayList<String> stringList = new ArrayList<>();
        when(artifactRepository.save(any(), any()))
                .thenReturn(stringList);

        when(artifactMapper.mapFilesToArtifact(any(), any(), any(), any()))
                .thenReturn(List.of());

        ArrayList<MultipartFile> filesToStore = new ArrayList<>();
        List<String> actualSaveResult =
                storageService.save(filesToStore, "Module", "Tag", "42", new RequestInfo());
        assertSame(stringList, actualSaveResult);
        assertTrue(actualSaveResult.isEmpty());
        verify(artifactRepository).save(any(), any());
    }

    @Test
    void testRetrieve() throws IOException {
        Artifact artifact = new Artifact();
        artifact.setContentType("text/plain");
        artifact.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
        artifact.setCreatedTime(1L);
        artifact.setFileName("foo.txt");
        artifact.setFileSource("File Source");
        artifact.setFileStoreId("42");
        artifact.setId(123L);
        artifact.setLastModifiedBy("Jan 1, 2020 9:00am GMT+0100");
        artifact.setLastModifiedTime(1L);
        artifact.setModule("Module");
        artifact.setTag("Tag");
        artifact.setTenantId("42");
        when(artifactRepository.find(any(), any())).thenReturn(Resource.builder().build());
        Resource resource = storageService.retrieve("foo", "foo");
        assertNotNull(resource);
    }

    @Test
    void testRetrieveNewResource() throws IOException {
        Resource resource = new Resource("text/plain", "foo.txt", new ByteArrayResource("AAAAAAAA".getBytes("UTF-8")), "42",
                "File Size");
        Artifact artifact = mock(Artifact.class);
        when(artifactRepository.find(any(), any())).thenReturn(resource);
        doNothing().when(artifact).setContentType((String) any());
        doNothing().when(artifact).setCreatedBy((String) any());
        doNothing().when(artifact).setCreatedTime((Long) any());
        doNothing().when(artifact).setFileName((String) any());
        doNothing().when(artifact).setFileSource((String) any());
        doNothing().when(artifact).setFileStoreId((String) any());
        doNothing().when(artifact).setId((Long) any());
        doNothing().when(artifact).setLastModifiedBy((String) any());
        doNothing().when(artifact).setLastModifiedTime((Long) any());
        doNothing().when(artifact).setModule((String) any());
        doNothing().when(artifact).setTag((String) any());
        doNothing().when(artifact).setTenantId((String) any());
        artifact.setContentType("text/plain");
        artifact.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
        artifact.setCreatedTime(1L);
        artifact.setFileName("foo.txt");
        artifact.setFileSource("File Source");
        artifact.setFileStoreId("42");
        artifact.setId(123L);
        artifact.setLastModifiedBy("Jan 1, 2020 9:00am GMT+0100");
        artifact.setLastModifiedTime(1L);
        artifact.setModule("Module");
        artifact.setTag("Tag");
        artifact.setTenantId("42");

        when(artifactRepository.find((String) any(), (String) any())).thenReturn(resource);
        assertSame(resource, (storageService.retrieve("foo", "foo")));
        verify(artifact).setContentType((String) any());
        verify(artifact).setCreatedBy((String) any());
        verify(artifact).setCreatedTime((Long) any());
        verify(artifact).setFileName((String) any());
        verify(artifact).setFileSource((String) any());
        verify(artifact).setFileStoreId((String) any());
        verify(artifact).setId((Long) any());
        verify(artifact).setLastModifiedBy((String) any());
        verify(artifact).setLastModifiedTime((Long) any());
        verify(artifact).setModule((String) any());
        verify(artifact).setTag((String) any());
        verify(artifact).setTenantId((String) any());
        verify(artifactRepository).find((String) any(), (String) any());
    }

    @Test
    void testRetrieveByTag() {
        when(artifactRepository.findByTag( any(), any())).thenReturn(new ArrayList<>());
        assertTrue((storageService.retrieveByTag("foo", "foo").isEmpty()));
    }

    @Test
    void testRetrieveByTagDefaultArguments() {

        when(artifactRepository.findByTag(any(), any()))
                .thenReturn(List.of(FileInfo.builder()
                        .contentType("text/plain")
                        .tenantId("42")
                        .fileLocation(fl)
                        .build()));

        List<FileInfo> actualRetrieveByTagResult = storageService.retrieveByTag("foo", "foo");
        assertEquals(1, actualRetrieveByTagResult.size());
        FileInfo getResult = actualRetrieveByTagResult.get(0);
        assertEquals("text/plain", getResult.getContentType());
        assertEquals("42", getResult.getTenantId());
        FileLocation fileLocation = getResult.getFileLocation();
        assertEquals("foo.txt", fileLocation.getFileName());
        assertEquals("42", fileLocation.getTenantId());
        assertEquals("Tag", fileLocation.getTag());
        assertEquals("Module", fileLocation.getModule());
        assertEquals("42", fileLocation.getFileStoreId());
        assertEquals("File Source", fileLocation.getFileSource());
    }


    @Test
    void testRetrieveByTagMultipleArtifacts() {
        when(artifactRepository.findByTag(any(), any())).thenReturn(
                List.of(FileInfo.builder()
                                .contentType("text/plain")
                                .tenantId("42")
                                .fileLocation(fl)
                                .build(),
                        FileInfo.builder()
                                .contentType("text/plain")
                                .tenantId("42")
                                .fileLocation(fl)
                                .build()));

        List<FileInfo> actualRetrieveByTagResult = storageService.retrieveByTag("foo", "foo");
        assertEquals(2, actualRetrieveByTagResult.size());
        FileInfo getResult = actualRetrieveByTagResult.get(0);
        assertEquals("42", getResult.getTenantId());
        FileInfo getResult1 = actualRetrieveByTagResult.get(1);
        assertEquals("42", getResult1.getTenantId());
        assertEquals("text/plain", getResult1.getContentType());
        assertEquals("text/plain", getResult.getContentType());
        FileLocation fileLocation = getResult1.getFileLocation();
        assertEquals("Module", fileLocation.getModule());
        FileLocation fileLocation1 = getResult.getFileLocation();
        assertEquals("42", fileLocation1.getTenantId());
        assertEquals("Tag", fileLocation1.getTag());
        assertEquals("Module", fileLocation1.getModule());
        assertEquals("42", fileLocation1.getFileStoreId());
        assertEquals("File Source", fileLocation1.getFileSource());
        assertEquals("foo.txt", fileLocation1.getFileName());
        assertEquals("42", fileLocation.getFileStoreId());
        assertEquals("File Source", fileLocation.getFileSource());
        assertEquals("foo.txt", fileLocation.getFileName());
        assertEquals("Tag", fileLocation.getTag());
        assertEquals("42", fileLocation.getTenantId());
    }


    @Test
    void testRetrieveByTagMockRepo() {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        when(artifactRepository.findByTag(any(), any())).thenReturn(fileInfoList);
        List<FileInfo> actualRetrieveByTagResult = storageService.retrieveByTag("foo", "foo");
        assertSame(fileInfoList, actualRetrieveByTagResult);
        assertTrue(actualRetrieveByTagResult.isEmpty());
        verify(artifactRepository).findByTag((String) any(), (String) any());
    }
}


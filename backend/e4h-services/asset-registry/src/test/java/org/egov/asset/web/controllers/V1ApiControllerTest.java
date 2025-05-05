package org.egov.asset.web.controllers;

import digit.web.models.AssetCreateUpdateResponse;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.egov.asset.TestConfiguration;

import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
* API tests for V1ApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(V1ApiController.class)
@Import(TestConfiguration.class)
public class V1ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void bulkCreateAssetSuccess() throws Exception {
        mockMvc.perform(post("/v1/asset/bulk/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void bulkCreateAssetFailure() throws Exception {
        mockMvc.perform(post("/v1/asset/bulk/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void createAMCVisitSuccess() throws Exception {
        mockMvc.perform(post("/v1/asset/amc/visit/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void createAMCVisitFailure() throws Exception {
        mockMvc.perform(post("/v1/asset/amc/visit/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void createAssetSuccess() throws Exception {
        mockMvc.perform(post("/v1/asset/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void createAssetFailure() throws Exception {
        mockMvc.perform(post("/v1/asset/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void createAssetAMCSuccess() throws Exception {
        mockMvc.perform(post("/v1/asset/amc/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void createAssetAMCFailure() throws Exception {
        mockMvc.perform(post("/v1/asset/amc/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void searchAMCVisitsSuccess() throws Exception {
        mockMvc.perform(post("/v1/asset/amc/visit/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void searchAMCVisitsFailure() throws Exception {
        mockMvc.perform(post("/v1/asset/amc/visit/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void searchAssetAMCSuccess() throws Exception {
        mockMvc.perform(post("/v1/asset/amc/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void searchAssetAMCFailure() throws Exception {
        mockMvc.perform(post("/v1/asset/amc/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void searchAssetsSuccess() throws Exception {
        mockMvc.perform(post("/v1/asset/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void searchAssetsFailure() throws Exception {
        mockMvc.perform(post("/v1/asset/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void updateAMCVisitSuccess() throws Exception {
        mockMvc.perform(post("/v1/asset/amc/visit/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void updateAMCVisitFailure() throws Exception {
        mockMvc.perform(post("/v1/asset/amc/visit/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void updateAssetSuccess() throws Exception {
        mockMvc.perform(post("/v1/asset/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void updateAssetFailure() throws Exception {
        mockMvc.perform(post("/v1/asset/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void updateAssetAMCSuccess() throws Exception {
        mockMvc.perform(post("/v1/asset/amc/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void updateAssetAMCFailure() throws Exception {
        mockMvc.perform(post("/v1/asset/amc/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void updateAssetWorkflowSuccess() throws Exception {
        mockMvc.perform(post("/v1/asset/workflow/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void updateAssetWorkflowFailure() throws Exception {
        mockMvc.perform(post("/v1/asset/workflow/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}

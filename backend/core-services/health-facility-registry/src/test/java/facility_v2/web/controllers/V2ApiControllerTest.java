package facility_v2.web.controllers;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import facility-v2.yaml.TestConfiguration;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
* API tests for V2ApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(V2ApiController.class)
@Import(TestConfiguration.class)
public class V2ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void createFacilitySuccess() throws Exception {
        mockMvc.perform(post("/v2/facility/create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void createFacilityFailure() throws Exception {
        mockMvc.perform(post("/v2/facility/create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void createHFAssessmentSuccess() throws Exception {
        mockMvc.perform(post("/v2/facility/assessment/create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void createHFAssessmentFailure() throws Exception {
        mockMvc.perform(post("/v2/facility/assessment/create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void getFacilitiesSummarySuccess() throws Exception {
        mockMvc.perform(post("/v2/facility/summary").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void getFacilitiesSummaryFailure() throws Exception {
        mockMvc.perform(post("/v2/facility/summary").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void searchFacilitiesSuccess() throws Exception {
        mockMvc.perform(post("/v2/facility/search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void searchFacilitiesFailure() throws Exception {
        mockMvc.perform(post("/v2/facility/search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void updateFacilitySuccess() throws Exception {
        mockMvc.perform(post("/v2/facility/update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void updateFacilityFailure() throws Exception {
        mockMvc.perform(post("/v2/facility/update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void updateHFAssessmentSuccess() throws Exception {
        mockMvc.perform(post("/v2/facility/assessment/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void updateHFAssessmentFailure() throws Exception {
        mockMvc.perform(post("/v2/facility/assessment/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}

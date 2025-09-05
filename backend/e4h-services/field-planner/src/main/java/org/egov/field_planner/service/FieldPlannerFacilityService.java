package org.egov.field_planner.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.ds.Tuple;
import org.egov.common.models.ErrorDetails;
import org.egov.common.producer.Producer;
import org.egov.common.validator.Validator;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.repository.FieldPlannerRepository;
import org.egov.field_planner.service.enrichment.FieldPlannerEnrichment;
import org.egov.field_planner.util.FieldPlannerServiceUtil;
import org.egov.field_planner.util.MDMSUtils;
import org.egov.field_planner.validator.FieldPlannerValidator;
import org.egov.field_planner.validator.facility.FPFacilityIdValidator;
import org.egov.field_planner.validator.facility.FPFieldPlanIdValidator;
import org.egov.field_planner.validator.facility.FPUniqueCombinationValidator;
import org.egov.field_planner.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.*;
import static org.egov.field_planner.Constants.*;
import static org.egov.field_planner.util.FieldPlannerConstants.*;

@Service
@Slf4j
public class FieldPlannerFacilityService {

    private final FieldPlannerValidator fieldPlannerValidator;
    private final FieldPlannerRepository fieldPlannerRepository;
    private final Producer producer;
    private final FieldPlannerEnrichment fieldPlannerEnrichment;

    private final List<Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility>> validators;
    private final FieldPlannerConfiguration fieldPlannerConfiguration;
    private final MDMSUtils mdmsUtils;

    private final Predicate<Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility>> isApplicableForCreate = validator ->
            validator.getClass().equals(FPFacilityIdValidator.class)
                    || validator.getClass().equals(FPFieldPlanIdValidator.class)
                    || validator.getClass().equals(FPUniqueCombinationValidator.class);

    @Autowired
    public FieldPlannerFacilityService(
            FieldPlannerRepository fieldPlannerRepository, List<Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility>> validators,
            FieldPlannerValidator fieldPlannerValidator, FieldPlannerEnrichment fieldPlannerEnrichment, FieldPlannerConfiguration fieldPlannerConfiguration,
            Producer producer, FieldPlannerServiceUtil projectServiceUtil, MDMSUtils mdmsUtils) {
            this.fieldPlannerValidator = fieldPlannerValidator;
            this.producer = producer;
            this.fieldPlannerConfiguration = fieldPlannerConfiguration;
            this.fieldPlannerRepository = fieldPlannerRepository;
            this.fieldPlannerEnrichment = fieldPlannerEnrichment;
            this.mdmsUtils = mdmsUtils;
            this.validators = validators;
    }

    public FieldPlanFacility create(FieldPlanFacilityRequest request) {
        log.info("received request to create fieldplan facility");
        FieldPlanFacilityBulkRequest bulkRequest = FieldPlanFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .fieldPlanFacilities(Collections.singletonList(request.getFieldPlanFacility())).build();
        log.info("creating bulk request");
        return create(bulkRequest, false).get(0);
    }

    public List<FieldPlanFacility> create(FieldPlanFacilityBulkRequest request, boolean isBulk) {
        log.info("received request to create bulk fieldplan facility");
        Tuple<List<FieldPlanFacility>, Map<FieldPlanFacility, ErrorDetails>> tuple = validate(validators,
                isApplicableForCreate, request,
                isBulk);

        Map<FieldPlanFacility, ErrorDetails> errorDetailsMap = tuple.getY();
        List<FieldPlanFacility> validEntities = tuple.getX();
        try {
            if (!validEntities.isEmpty()) {
                log.info("processing {} valid entities", validEntities.size());
                fieldPlannerEnrichment.enrichFieldPlanFacilityOnCreate(validEntities, request);
                producer.push(fieldPlannerConfiguration.getCreateFieldPlanFacilityTopic(), validEntities);
                log.info("successfully created project facility");
            }
        } catch (Exception exception) {
            log.error("error occurred while creating project facility: {}", ExceptionUtils.getStackTrace(exception));
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_FIELDPLAN_FACILITIES);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);

        return validEntities;
    }

    private Tuple<List<FieldPlanFacility>, Map<FieldPlanFacility, ErrorDetails>> validate(List<Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility>> validators,
                                                                                      Predicate<Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility>> applicableValidators,
                                                                                        FieldPlanFacilityBulkRequest request, boolean isBulk) {
        log.info("validating request");
        Map<FieldPlanFacility, ErrorDetails> errorDetailsMap = new HashMap<>();
        List<FieldPlanFacility> validEntities = request.getFieldPlanFacilities().stream()
                .filter(notHavingErrors()).toList();
        log.info("validation successful, found valid fieldplan facility");
        return new Tuple<>(validEntities, errorDetailsMap);
    }


}

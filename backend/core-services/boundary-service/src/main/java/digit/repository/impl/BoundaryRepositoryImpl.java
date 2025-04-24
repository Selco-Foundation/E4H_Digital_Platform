package digit.repository.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.repository.BoundaryRepository;
import digit.repository.querybuilder.BoundaryEntityQueryBuilder;
import digit.repository.rowmapper.BoundaryEntityRowMapper;
import digit.web.models.Boundary;
import digit.web.models.BoundaryRequest;
import digit.web.models.BoundarySearchCriteria;
import digit.web.models.PaginatedBoundaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class BoundaryRepositoryImpl implements BoundaryRepository {

    private final ObjectMapper mapper;

    private final RestTemplate restTemplate;

    private final JdbcTemplate jdbcTemplate;

    private final BoundaryEntityRowMapper boundaryEntityRowMapper;

    private final BoundaryEntityQueryBuilder boundaryEntityQueryBuilder;

    private final Producer producer;

    private final ApplicationProperties applicationProperties;

    public BoundaryRepositoryImpl(ObjectMapper mapper , RestTemplate restTemplate , JdbcTemplate jdbcTemplate , BoundaryEntityRowMapper boundaryEntityRowMapper
            , BoundaryEntityQueryBuilder boundaryEntityQueryBuilder , Producer producer , ApplicationProperties applicationProperties) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.boundaryEntityRowMapper = boundaryEntityRowMapper;
        this.boundaryEntityQueryBuilder = boundaryEntityQueryBuilder;
        this.producer = producer;
        this.applicationProperties = applicationProperties;
    }

    /**
     * This method implements boundary entity repository interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform insert.
     * @param boundaryRequest
     */
    @Override
    public void create(BoundaryRequest boundaryRequest) {
        producer.push(applicationProperties.getCreateBoundaryTopic() , boundaryRequest);
    }

    /**
     * This method is used to search for boundary entity
     * @param boundarySearchCriteria
     * @return
     */
    @Override
    public List<Boundary> search(BoundarySearchCriteria boundarySearchCriteria) {

        List<Object> preparedStmtList = new ArrayList<>();

        String query = boundaryEntityQueryBuilder.getBoundaryDataSearchQuery(boundarySearchCriteria , preparedStmtList);

        List<Boundary> boundaryList = jdbcTemplate.query(query , preparedStmtList.toArray() , boundaryEntityRowMapper);

        return boundaryList;
    }

    /**
     * This method implements boundary type hierarchy repository interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform update.
     * @param boundaryRequest
     */
    @Override
    public void update(BoundaryRequest boundaryRequest) {
        producer.push(applicationProperties.getUpdateBoundaryTopic() , boundaryRequest);
    }

    /**
     * This method returns the set of codes for a given tenantId
     * @param tenantId
     * @return
     */
    public Set<String> getCodeListByTenantId(String tenantId) {

        // create a boundary search criteria object with the given tenantId
        BoundarySearchCriteria boundarySearchCriteria = new BoundarySearchCriteria();
        boundarySearchCriteria.setTenantId(tenantId);

        // get all the boundary entities for the given tenantId from the database
        List<Boundary> boundaryList = search(boundarySearchCriteria);

        // return the set of codes from the boundary entities
        return boundaryList.stream().map(Boundary::getCode).collect(Collectors.toSet());

    }

    public PaginatedBoundaryResponse getPaginatedBoundaries(BoundarySearchCriteria c) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder()
                .append("SELECT ")
                .append("  b.id, b.tenantid, b.code, b.geometry, b.additionaldetails, ")
                .append("  b.createdtime, b.createdby, b.lastmodifiedtime, b.lastmodifiedby, ")
                .append("  COUNT(*) OVER() AS total_count ")
                .append("FROM boundary b ")
                .append("WHERE b.tenantid = ? ");
        params.add(c.getTenantId());

        if (c.getCodes() != null) {
            sql.append(" AND b.parentBoundaryCode = ? ");
            params.add(c.getCodes().get(0));
        }
        if (c.getBoundaryType() != null) {
            sql.append(" AND b.boundaryType = ? ");
            params.add(c.getBoundaryType());
        }

        sql.append(" LIMIT ? OFFSET ?");
        params.add(c.getLimit());
        params.add(c.getOffset());

        return jdbcTemplate.query(
                sql.toString(),
                params.toArray(),
                rs -> {
                    List<Boundary> list = new ArrayList<>();
                    int total = 0;
                    while (rs.next()) {
                        if (total == 0) {
                            total = rs.getInt("total_count");
                        }
                        list.add(boundaryEntityRowMapper.mapRow(rs, rs.getRow()));
                    }
                    int totalPages = total == 0
                            ? 0
                            : (int) Math.ceil((double) total / c.getLimit());
                    int currentPage = c.getOffset() / c.getLimit();

                    return PaginatedBoundaryResponse.builder()
                            .responseInfo(/* populate your ResponseInfo here */ null)
                            .boundary(list)
                            .page(currentPage)
                            .size(c.getLimit())
                            .totalElements((long) total)
                            .totalPages(totalPages)
                            .build();
                }
        );
    }


}
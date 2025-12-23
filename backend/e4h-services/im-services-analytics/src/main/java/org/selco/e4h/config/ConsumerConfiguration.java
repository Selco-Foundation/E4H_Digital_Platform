package org.selco.e4h.config;

import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TracerConfiguration.class})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConsumerConfiguration {

	//Kafka Config
	@Value("${kafka.config.bootstrap_server_config}")
	private String brokerAddress;

	@Value("${spring.kafka.consumer.group-id}")
	private String consumerGroup;

	@Value("${kafka.topics.consumer}")
	private String consumerTopics;

	//ElasticSearch Config
	@Value("${egov.infra.indexer.host}")
	private String esHostUrl;

	@Value("${egov.update.index.path}")
	private String updateIndexPath;

	@Value("${elasticsearch.poll.interval.seconds}")
	private String pollInterval;

	@Value("${egov.indexer.es.username}")
	private String esUsername;

	@Value("${egov.indexer.es.password}")
	private String esPassword;

	@Value("${egov.statelevel.tenantId}")
	private String stateLevelTenantId;

	@Value("${incident.kafka.update.topic.indexer}")
	private String updateTopicIndexer;

    @Value("${egov.workflow.host}")
    private String wfHost;

    @Value("${egov.workflow.processinstance.search.path}")
    private String wfProcessInstanceSearchPath;

    // Escalation Service Config
    @Value("${egov.kafka.escalation.status.topic}")
    private String escalationStatusTopic;

    @Value("${egov.kafka.notification.email.topic}")
    private String notificationEmailTopic;

    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.search.endpoint}")
    private String userSearchEndpoint;

    @Value("${egov.hrms.host}")
    private String hrmsHost;

    @Value("${egov.hrms.search.url}")
    private String hrmsSearchUrl;

    @Value("${egov.filestore.host}")
    private String fileStoreHost;

    @Value("${egov.filestore.baseUrl}")
    private String fileStoreBaseUrl;

    @Value("${egov.filestore.upload.endpoint}")
    private String fileStoreUploadEndpoint;

    @Value("${egov.filestore.hls.upload.endpoint}")
    private String fileStoreHlsUploadEndpoint;

    @Value("${egov.filestore.download.endpoint}")
    private String fileStoreDownloadEndpoint;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsSearchEndpoint;


    // Elasticsearch Indexer Config
    @Value("${egov.indexer.es.host.name}")
    private String esHostName;

    @Value("${egov.indexer.es.port.no}")
    private int esPortNo;
}

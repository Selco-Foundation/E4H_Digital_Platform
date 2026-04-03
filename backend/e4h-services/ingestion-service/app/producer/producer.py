from kafka import KafkaProducer
import json

from app.core.logging import AppLogger

logger = AppLogger().get_logger()


class Producer:
    def __init__(self):
        self.producer = KafkaProducer(
            bootstrap_servers='localhost:9092',
            value_serializer=lambda v: json.dumps(v).encode('utf-8'),
            key_serializer=str.encode
        )

    def send(self, topic: str, sms_request: dict):
        logger.trace(f"Sending message to Kafka topic: {topic}")
        try:
            self.producer.send(topic, key="notification", value=sms_request)
            self.producer.flush()
            logger.info(f"Successfully sent message to Kafka topic: {topic}")
            logger.debug(f"Message sent to topic '{topic}', message_id: {sms_request.get('id', 'unknown')}")
        except Exception as e:
            logger.error(f"Error sending message to Kafka topic '{topic}': {e}", exc_info=True)
            raise e

    def close(self):
        logger.trace("Closing Kafka producer")
        self.producer.flush()
        self.producer.close()
        logger.debug("Kafka producer closed")
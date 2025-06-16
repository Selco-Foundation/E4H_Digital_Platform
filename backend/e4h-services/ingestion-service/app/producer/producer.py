from kafka import KafkaProducer
import json


def send(self, topic: str, sms_request: dict):
    """
    Send a JSON message to the specified Kafka topic.

    :param topic: Kafka topic name (e.g., "egov.core.notification.sms")
    :param message: Dictionary representing the message (e.g., {"mobileNumber": "1234567890", "message": "Hi"})
    """
    try:
        producer = KafkaProducer(
            bootstrap_servers='localhost:9092',
            value_serializer=lambda v: json.dumps(v).encode('utf-8')
        )
        producer.send(topic, value=sms_request)
        producer.flush()
        print(f"Sent message to topic '{topic}': {sms_request}")
    except Exception as e:
        print(f"Error sending message to Kafka: {e}")
        raise e

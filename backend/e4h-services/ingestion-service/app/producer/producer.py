from kafka import KafkaProducer
import json


class Producer:
    def __init__(self):
        self.producer = KafkaProducer(
            bootstrap_servers='localhost:9092',
            value_serializer=lambda v: json.dumps(v).encode('utf-8'),
            key_serializer=str.encode
        )

    def send(self, topic: str, sms_request: dict):
        try:
            self.producer.send(topic, key="notification", value=sms_request)
            self.producer.flush()
            print(f"Sent message to topic '{topic}': {sms_request}")
        except Exception as e:
            print(f"Error sending message to Kafka: {e}")
            raise e

    def close(self):
        self.producer.flush()
        self.producer.close()
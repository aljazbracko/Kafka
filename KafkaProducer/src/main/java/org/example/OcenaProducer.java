package org.example;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;

public class OcenaProducer {

    private final static String TOPIC = "kafka-ocena";

    private static KafkaProducer<String, GenericRecord> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "AvroProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        return new KafkaProducer<>(props);
    }

    private static ProducerRecord<String, GenericRecord> generateRecord(Schema schema) {
        Random rand = new Random();
        GenericRecord avroRecord = new GenericData.Record(schema);

        // Generating random values for the record
        int idOcena = rand.nextInt(99) + 1;
        avroRecord.put("id", idOcena);
        avroRecord.put("cistost", rand.nextInt(10));
        avroRecord.put("bistrost", rand.nextInt(10));
        avroRecord.put("okus", rand.nextInt(10));
        avroRecord.put("vonj", rand.nextInt(10));
        avroRecord.put("harmonicnost", rand.nextInt(10));

        // Ensuring Vzorec_idVzorec and Ocenjevalec_idOcenjevalec are not null
        avroRecord.put("Vzorec_idVzorec", rand.nextInt(100) + 1); // Random value between 1 and 100
        avroRecord.put("Ocenjevalec_idOcenjevalec", rand.nextInt(50) + 1); // Random value between 1 and 50

        return new ProducerRecord<>(TOPIC, String.valueOf(idOcena), avroRecord);
    }

    public static void main(String[] args) throws Exception {
        // Define Avro schema for ocena
        Schema schema = SchemaBuilder.record("ocena")
                .fields()
                .requiredInt("id")
                .requiredInt("cistost")
                .requiredInt("bistrost")
                .requiredInt("okus")
                .requiredInt("vonj")
                .requiredInt("harmonicnost")
                .name("Vzorec_idVzorec").type().unionOf().nullType().and().intType().endUnion().nullDefault()
                .name("Ocenjevalec_idOcenjevalec").type().unionOf().nullType().and().intType().endUnion().nullDefault()
                .endRecord();


        // Create Kafka producer
        KafkaProducer<String, GenericRecord> producer = createProducer();

        // Send records to Kafka topic
        while (true) {
            ProducerRecord<String, GenericRecord> record = generateRecord(schema);
            producer.send(record);
            System.out.println("[RECORD] Sent: " + record.value());
            Thread.sleep(5000);
        }
    }
}

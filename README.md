# Running the application
- Please enter the correct credentials in twitter4j.properties file.
- Then go to docker-compose folder and run docker-compose up command to run local kafka cluster
- Then run TwitterToKafkaServiceApplication inside IntelliJ, or run with mvn spring-boot:run command
- Check the new TwitterStatusToAvroTransformer and updated TwitterKafkaStatusListener classes, where we implemented the part 
that transforms twitter status object to kafka compatible avro object and send the message to kafka using producer  
- Check the StreamInitializer and KafkaStreamInitializer in twitter-to-kafka-service, where we added initializing logic for kafka cluster
and then used this initializer in the TwitterToKafkaServiceApplication prior to starting streaming data
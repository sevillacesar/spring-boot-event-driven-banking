# Kafka Topics Configuration

#!/bin/bash

BROKER="localhost:9092"

echo "Creating Kafka topics..."

kafka-topics --bootstrap-server $BROKER --create --if-not-exists \
  --topic banking.customer.created \
  --partitions 3 \
  --replication-factor 1

kafka-topics --bootstrap-server $BROKER --create --if-not-exists \
  --topic banking.account.created \
  --partitions 3 \
  --replication-factor 1

kafka-topics --bootstrap-server $BROKER --create --if-not-exists \
  --topic banking.transaction.initiated \
  --partitions 3 \
  --replication-factor 1

echo "Topics created successfully."
kafka-topics --bootstrap-server $BROKER --list

CREATE DATABASE IF NOT EXISTS logsantos;

CREATE TABLE IF NOT EXISTS logsantos.metrics
(
    metricName String,
    value UInt64,
    windowStart DateTime64(3, 'UTC'),
    windowEnd DateTime64(3, 'UTC'),
    createdAt DateTime64(3, 'UTC')
    )
    ENGINE = MergeTree
    ORDER BY (metricName, createdAt);

CREATE TABLE IF NOT EXISTS logsantos.metrics_kafka
(
    metricName String,
    value UInt64,
    windowStart Decimal(20, 9),
    windowEnd Decimal(20, 9),
    createdAt Decimal(20, 9)
    )
    ENGINE = Kafka
    SETTINGS
    kafka_broker_list = 'kafka:9092',
    kafka_topic_list = 'metrics-topic',
    kafka_group_name = 'clickhouse-consumer',
    kafka_format = 'JSONEachRow',
    kafka_num_consumers = 1;

CREATE MATERIALIZED VIEW IF NOT EXISTS logsantos.metrics_mv
TO logsantos.metrics
AS
SELECT
    metricName,
    value,
    toDateTime64(windowStart, 3, 'UTC') AS windowStart,
    toDateTime64(windowEnd, 3, 'UTC') AS windowEnd,
    toDateTime64(createdAt, 3, 'UTC') AS createdAt
FROM logsantos.metrics_kafka;
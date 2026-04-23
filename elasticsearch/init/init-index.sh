echo "Waiting for Elasticsearch to be ready..."
until curl -s http://localhost:9200/_cluster/health | grep -q '"status":"green"\|"status":"yellow"'; do
    echo "Elasticsearch is unavailable, waiting..."
    sleep 5
done

echo "Elasticsearch is ready!"

if curl -s -o /dev/null -w "%{http_code}" http://localhost:9200/log_event | grep -q "200"; then
    echo "Index 'log_event' already exists. Skipping creation."
else
    echo "Creating log_event index using SQL-like statement..."

    curl -X POST "http://localhost:9200/_sql?format=json" \
      -H 'Content-Type: application/json' \
      -d '{
        "query": "CREATE TABLE log_event (\n  event_id KEYWORD,\n  service_id KEYWORD,\n  \"@timestamp\" DATE,\n  level KEYWORD,\n  message TEXT,\n  attrs FLATTENED,\n  trace_id KEYWORD,\n  span_id KEYWORD,\n  ingested_at DATE\n)"
      }'

    echo ""

    if ! curl -s http://localhost:9200/log_event | grep -q "mappings"; then
        echo "SQL method failed, falling back to REST API..."
        curl -X PUT "http://localhost:9200/log_event" \
          -H 'Content-Type: application/json' \
          -d '{
            "settings": {
              "number_of_shards": 1,
              "number_of_replicas": 0
            },
            "mappings": {
              "properties": {
                "event_id": { "type": "keyword" },
                "service_id": { "type": "keyword" },
                "@timestamp": {
                  "type": "date",
                  "format": "strict_date_optional_time||epoch_millis"
                },
                "level": { "type": "keyword" },
                "message": {
                  "type": "text",
                  "fields": {
                    "keyword": {
                      "type": "keyword",
                      "ignore_above": 256
                    }
                  }
                },
                "attrs": { "type": "flattened" },
                "trace_id": { "type": "keyword" },
                "span_id": { "type": "keyword" },
                "ingested_at": {
                  "type": "date",
                  "format": "strict_date_optional_time||epoch_millis"
                }
              }
            }
          }'
        echo ""
    fi

    echo "Index log_event created successfully!"
fi
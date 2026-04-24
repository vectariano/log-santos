echo "Waiting for Elasticsearch to be ready..."
until curl -s http://elasticsearch-ls:9200/_cluster/health | grep -q '"status":"green"\|"status":"yellow"'; do
    echo "Elasticsearch is unavailable - sleeping"
    sleep 5
done

echo "Elasticsearch is up!"

if curl -s -o /dev/null -w "%{http_code}" http://elasticsearch-ls:9200/log_event | grep -q "200"; then
    echo "Index 'log_event' already exists. Skipping creation."
else
    echo "Creating index 'log_event'..."

    curl -X PUT "http://elasticsearch:9200/log_event" -H 'Content-Type: application/json' -d'
    {
      "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0
      },
      "mappings": {
        "properties": {
          "event_id": {
            "type": "keyword"
          },
          "service_id": {
            "type": "keyword"
          },
          "@timestamp": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "level": {
            "type": "keyword"
          },
          "message": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "attrs": {
            "type": "flattened"
          },
          "trace_id": {
            "type": "keyword"
          },
          "span_id": {
            "type": "keyword"
          },
          "ingested_at": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          }
        }
      }
    }'

    echo ""
    echo "Index 'log_event' created successfully."
fi

// ...existing code...
    private List<String> headers;
    private boolean headerRead = false;

    public List<String> getHeaders() {
        if (!headerRead && recordIterator != null && recordIterator.hasNext()) {
            CSVRecord csvRecord = recordIterator.next();
            headers = new ArrayList<>();
            for (String value : csvRecord) {
                headers.add(value);
            }
            headerRead = true;
        }
        return headers;
    }

    public Map<String, String> getNextRecordAsMap() {
        if (headers == null) {
            getHeaders(); // Ensure headers are read first
        }
        if (recordIterator != null && recordIterator.hasNext()) {
            CSVRecord csvRecord = recordIterator.next();
            Map<String, String> recordMap = new java.util.HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                String value = i < csvRecord.size() ? csvRecord.get(i) : null;
                recordMap.put(headers.get(i), value);
            }
            return recordMap;
        }
        return null;
    }

    public List<String> getNextBatch() {
        if (headers == null) {
            getHeaders(); // Skip header row on first batch read
        }
        List<String> batch = new ArrayList<>();
        int count = 0;
        while (recordIterator != null && recordIterator.hasNext() && count < batchSize) {
            CSVRecord record = recordIterator.next();
            if (record.size() > 0) {
                batch.add(record.get(0));
                count++;
            }
        }
        return batch.isEmpty() ? null : batch;
    }
// ...existing code...

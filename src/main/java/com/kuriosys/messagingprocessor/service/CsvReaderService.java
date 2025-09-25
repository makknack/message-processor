package com.kuriosys.messagingprocessor.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CsvReaderService {

    private static final Logger log = LoggerFactory.getLogger(CsvReaderService.class);
    private Iterator<CSVRecord> recordIterator;
    private final int batchSize;
    private long totalRecords = -1;
    private CSVParser csvParser;
    private List<String> headers;
    private boolean headerRead = false;
    private boolean hasHeaders;
    private String filePath;

    public CsvReaderService(String filePath, boolean hasHeaders,int batchSize) throws IOException {
        Reader reader = new FileReader(filePath);
        this.csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        this.recordIterator = csvParser.iterator();
        this.batchSize = batchSize;
        this.hasHeaders = hasHeaders;
        this.filePath = filePath;
        if(hasHeaders){
            readHeaders();
        }
    }

    public List<String> readHeaders() {
        if (!headerRead && recordIterator != null && recordIterator.hasNext()) {
            CSVRecord csvRecord = recordIterator.next();
            headers = new ArrayList<>();
            headers.addAll(csvRecord.toList());
            headerRead = true;
        }
        return headers;
    }

    public void skipRecords(int count) {
        int skipped = 0;
        while (recordIterator != null && recordIterator.hasNext() && skipped < count) {
            recordIterator.next();
            skipped++;
        }
    }

    public List<String> getNextBatch() {
        List<String> batch = new ArrayList<>(batchSize);
        int count = 0;
        while (recordIterator.hasNext() && count < batchSize) {
            CSVRecord record = recordIterator.next();
            if (record.size() > 0) {
                batch.add(record.get(0));
                count++;
            }
        }
        return batch.isEmpty() ? null : batch;
    }

    public boolean hasMoreRecords() {
        return recordIterator != null && recordIterator.hasNext();
    }

    public long getTotalRecords() throws IOException {
        if(totalRecords != -1) {
            return totalRecords;
        }
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {
            long count = 0;
            for (CSVRecord record: csvParser) {
                count++;
            }
           totalRecords = hasHeaders ? count - 1 : count;
            return totalRecords;
        }
    }

    public List<String> getNextRecord() {
        if (recordIterator != null && recordIterator.hasNext()) {
            CSVRecord csvRecord = recordIterator.next();
            List<String> record = new ArrayList<>();
            csvRecord.forEach(record::add);
            return record;
        }
        return null;
    }

    public Map<String, String> getNextRecordAsMap() {
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

    public List<String> getNextBatchOfFirstColumn(int batchSize) {
        List<String> batch = new ArrayList<>();
        int count = 0;
        while (recordIterator != null && recordIterator.hasNext() && count < batchSize) {
            CSVRecord record = recordIterator.next();
            if (record.size() > 0) {
                batch.add(record.get(0));
                count++;
            }
        }
        return batch;
    }

}

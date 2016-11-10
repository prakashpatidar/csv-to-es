package com.ubs.ii;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by praka on 11/6/2016.
 */
public class Record {
    private String indexName;
    private String indexType;
    private String docId;
    private Map<String,String> kv;

    public Record(String indexName, String indexType, String docId, Map<String, String> kv) {
        this.indexName = indexName;
        this.indexType = indexType;
        this.docId = docId;
        this.kv = kv;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    public Map<String, String> getKv() {
        return kv;
    }

    public void setKv(Map<String, String> kv) {
        this.kv = kv;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}

package com.ubs.ii;

import com.opencsv.CSVReader;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;


import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by praka on 11/6/2016.
 */
public class Ingester {
    TransportClient client = null;
    public Ingester()
    {
        connectES();
    }

    private void connectES() {

        try {
    /*        client = TransportClient.builder()
                        .settings(Settings.builder()
                            .put("cluster.name", "ELK")
                                .put("number_of_replicas",1)
                                .put("number_of_shards",1)
                            .build()).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
*/
            Settings settings = Settings.builder()
                    .put("cluster.name", "elasticsearch")
                    .build()
                    ;
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
            /*.settings(Settings.builder()
                            .put("cluster.name", "ELK")
                            .put("number_of_replicas",1)
                            .put("number_of_shards",1)
                            .build()).build()
                                        Settings settings = Settings.builder()
                    .put("cluster.name", "myClusterName").build();
            TransportClient client = new PreBuiltTransportClient(settings);
*/
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public void ingest(List<String> files)
    {
        for(String file:files)
        {
            ingest(file);
        }
    }

    private void ingest(String file) {

        List<Record> records = parseFile(file);

        if(records.size()>0)
            ingestES(records);
    }

    private List<Record> parseFile(String file) {
        String indexName = "ii";
        String indexType = file.split("\\.")[1];
        List<Record> records = new ArrayList<Record>();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file));
            String[] line;

            int count =0;
            List<String> header= new ArrayList<>();
            Map<String,String> mapping= new HashMap<>();
            while ((line = reader.readNext()) != null) {
                Map<String,String> kv = new HashMap<>();
                if(count==0)
                {
                    for(String col:line)
                    {
                        String[] nametype = col.split("=");
                        String name = nametype[0];
                        String dataType= nametype.length==1 ? "string" : nametype[1];
                        header.add(name);
                        mapping.put(name,dataType);
                    }
                    createIndex(indexName,indexType,mapping);
                }
                else
                {
                    for(int i=0;i<line.length;i++)
                    {
                        kv.put(header.get(i),line[i]);
                    }
                    String id=kv.get("id");
                    if( id ==null || id.isEmpty())
                    {
                        id = UUID.randomUUID().toString();
                    }
                    Record record = new Record(indexName,indexType,id,kv);
                    records.add(record);

                }

                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return records;
    }

    private void createIndex(String index,String type,Map<String,String> mapping) {
        try {

            client.admin().indices().prepareDelete(index).get();
            client.admin().indices().prepareCreate(index)
                    .setSettings(Settings.builder()
                            .put("index.number_of_shards", 1)
                            .put("index.number_of_replicas", 1)
                    )
                    .get();
        }
        catch (Exception ex)
        {
            System.out.println(ex.getStackTrace().toString());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"properties\": {\n");
        for(String key:mapping.keySet())
        {
            sb.append("    \"" + key + "\": {\n");
            if(mapping.get(key).equals("date"))
            {
                sb.append("      \"type\": \"" + mapping.get(key) + "\",\n");
                sb.append("      \"format\": \"" + "dd-MM-yy" + "\"\n");
            }
            else {
                sb.append("      \"type\": \"" + mapping.get(key) + "\",\n");
                sb.append("      \"index\": \"" + "not_analyzed" + "\"\n");
            }
            sb.append("    },\n");
        }
        sb.append("    \"" + "samplestring" + "\": {\n");
        sb.append("      \"type\": \"" + "string"+ "\"\n");
        sb.append("    }\n");
        sb.append( "  }\n" );
        sb.append("}");
        System.out.println(sb.toString());
        client.admin().indices().preparePutMapping(index).setType(type).setSource(sb.toString()).get();
    }

    private void ingestES(List<Record> records) {



            BulkRequestBuilder bulkRequest = client.prepareBulk();

            for(Record record:records)
            {
                bulkRequest.add(client.prepareIndex(record.getIndexName(),record.getIndexType(),record.getDocId())
                        .setSource(record.getKv()));
            }

            BulkResponse bulkResponse = bulkRequest.get();
            if(bulkResponse.hasFailures())
            {
                System.out.println(bulkResponse.buildFailureMessage().toString());
                System.out.println("Retry indexing");
            }
            client.close();

        

// on shutdown

        client.close();
    }

}

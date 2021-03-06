package com.google.bigquery;

import com.google.cloud.bigquery.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class QueryVerifierTest {

    final String resourcesPath = "src/test/resources/";

    @Test
    public void testGetTableIdFromJsonSchema() {
        String schemaContents = Main.getContentsOfFile(resourcesPath + "schema1.json");
        QueryVerificationSchema schema = QueryVerificationSchema.create(schemaContents, "");
        List<TableInfo> tableInfos = QueryVerifier.getTableInfoFromJsonSchema(schema);

        assertEquals(tableInfos.size(), 1);
        TableId tableId = tableInfos.get(0).getTableId();

        assertEquals(tableId.getDataset(), "dataset");
        assertEquals(tableId.getTable(), "table");
    }

    @Test
    public void testGetMultipleTableIdFromJsonSchema() {
        String schemaContents = Main.getContentsOfFile(resourcesPath + "schema3.json");
        QueryVerificationSchema schema = QueryVerificationSchema.create(schemaContents, "");
        List<TableInfo> tableInfos = QueryVerifier.getTableInfoFromJsonSchema(schema);

        assertEquals(tableInfos.size(), 2);
        TableId tableId;

        tableId = tableInfos.get(0).getTableId();
        assertEquals(tableId.getDataset(), "dataset");
        assertEquals(tableId.getTable(), "firstTable");

        tableId = tableInfos.get(1).getTableId();
        assertEquals(tableId.getDataset(), "dataset");
        assertEquals(tableId.getTable(), "secondTable");
    }

    @Test
    public void testGetFieldsFromJsonSchema() {
        String schemaContents = Main.getContentsOfFile(resourcesPath + "schema2.json");
        QueryVerificationSchema schema = QueryVerificationSchema.create(schemaContents, "");
        List<TableInfo> tableInfos = QueryVerifier.getTableInfoFromJsonSchema(schema);

        assertEquals(tableInfos.size(), 1);
        FieldList fieldList = tableInfos.get(0).getDefinition().getSchema().getFields();

        assertEquals(fieldList.size(), 3);

        Field.Builder builder;

        builder = Field.newBuilder("stringField", StandardSQLTypeName.STRING);
        builder.setMode(Field.Mode.NULLABLE);
        assertEquals(fieldList.get(0), builder.build());

        builder = Field.newBuilder("integerField", StandardSQLTypeName.INT64);
        builder.setMode(Field.Mode.REQUIRED);
        builder.setDescription("This field stores integers.");
        assertEquals(fieldList.get(1), builder.build());

        Field subField = Field.of("integerField", StandardSQLTypeName.INT64);
        builder = Field.newBuilder("structField", StandardSQLTypeName.STRUCT, FieldList.of(subField));
        builder.setMode(Field.Mode.REPEATED);
        assertEquals(fieldList.get(2), builder.build());
    }

    @Test
    public void testGetTableIdFromDdlSchema() {
        String schemaContents = "CREATE TABLE dataset.table (stringField STRING, integerField INT64);";
        QueryVerificationSchema schema = QueryVerificationSchema.create(schemaContents, "");
        List<TableId> tableIds = QueryVerifier.getTableIdsFromDdlSchema(schema);

        assertEquals(tableIds.size(), 1);
        TableId tableId = tableIds.get(0);

        assertEquals(tableId.getDataset(), "dataset");
        assertEquals(tableId.getTable(), "table");
    }

    @Test
    public void testGetMultipleTableIdFromDdlSchema() {
        String schemaContents = "CREATE TABLE dataset.firstTable (stringField STRING, integerField INT64);\n" +
                "CREATE TABLE dataset.secondTable (stringField STRING, integerField INT64);";
        QueryVerificationSchema schema = QueryVerificationSchema.create(schemaContents, "");
        List<TableId> tableIds = QueryVerifier.getTableIdsFromDdlSchema(schema);

        assertEquals(tableIds.size(), 2);
        TableId tableId;

        tableId = tableIds.get(0);
        assertEquals(tableId.getDataset(), "dataset");
        assertEquals(tableId.getTable(), "firstTable");

        tableId = tableIds.get(1);
        assertEquals(tableId.getDataset(), "dataset");
        assertEquals(tableId.getTable(), "secondTable");
    }

}

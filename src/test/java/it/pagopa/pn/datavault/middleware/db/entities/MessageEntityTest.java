package it.pagopa.pn.datavault.middleware.db.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

class MessageEntityTest {

    private MessageObjEntity buildPrimaryMessage() {
        MessageObjEntity obj = new MessageObjEntity();
        obj.setSubject("Oggetto principale");
        obj.setLongBody("Corpo del messaggio principale");
        obj.setShortBody("Abstract principale");
        obj.setLanguage("IT");
        return obj;
    }

    private MessageObjEntity buildAdditionalMessage() {
        MessageObjEntity obj = new MessageObjEntity();
        obj.setSubject("Zusätzlicher Betreff");
        obj.setLongBody("Zusätzlicher Nachrichtentext");
        obj.setShortBody("Zusätzliche Zusammenfassung");
        obj.setLanguage("DE");
        return obj;
    }

    @Test
    void shouldExposeNewMessageStructure() {
        MessageEntity entity = new MessageEntity();
        entity.setMessageId("message-id");
        entity.setSk("sender-id");
        entity.setPrimaryMessage(buildPrimaryMessage());
        entity.setAdditionalMessage(buildAdditionalMessage());
        entity.setCreatedAt("2026-04-08T10:15:30Z");
        entity.setExpiration(123456789L);

        Assertions.assertEquals(MessageEntity.buildPk("message-id"), entity.getPk());
        Assertions.assertEquals("message-id", entity.getMessageId());
        Assertions.assertEquals("sender-id", entity.getSk());
        Assertions.assertNotNull(entity.getPrimaryMessage());
        Assertions.assertEquals("IT", entity.getPrimaryMessage().getLanguage());
        Assertions.assertEquals("Oggetto principale", entity.getPrimaryMessage().getSubject());
        Assertions.assertEquals("Corpo del messaggio principale", entity.getPrimaryMessage().getLongBody());
        Assertions.assertEquals("Abstract principale", entity.getPrimaryMessage().getShortBody());
        Assertions.assertNotNull(entity.getAdditionalMessage());
        Assertions.assertEquals("DE", entity.getAdditionalMessage().getLanguage());
        Assertions.assertEquals("Zusätzlicher Betreff", entity.getAdditionalMessage().getSubject());
        Assertions.assertEquals("2026-04-08T10:15:30Z", entity.getCreatedAt());
        Assertions.assertEquals(123456789L, entity.getExpiration());
    }

    @Test
    void shouldMapNewAttributesToDynamoColumns() {
        MessageEntity entity = new MessageEntity();
        entity.setMessageId("message-id");
        entity.setSk("sender-id");
        entity.setPrimaryMessage(buildPrimaryMessage());
        entity.setAdditionalMessage(buildAdditionalMessage());
        entity.setCreatedAt("2026-04-08T10:15:30Z");
        entity.setExpiration(987654321L);

        TableSchema<MessageEntity> schema = TableSchema.fromBean(MessageEntity.class);
        Map<String, AttributeValue> item = schema.itemToMap(entity, true);
        MessageEntity mappedEntity = schema.mapToItem(item);

        Assertions.assertEquals("MSG#message-id", item.get(MessageEntity.COL_PK).s());
        Assertions.assertEquals("sender-id", item.get(MessageEntity.COL_SK).s());
        Assertions.assertEquals("2026-04-08T10:15:30Z", item.get(MessageEntity.COL_CREATED_AT).s());
        Assertions.assertEquals("987654321", item.get(MessageEntity.COL_EXPIRATION).n());

        // primaryMessage è mappato come Map in DynamoDB
        Map<String, AttributeValue> primaryMap = item.get(MessageEntity.COL_PRIMARY_MESSAGE).m();
        Assertions.assertEquals("IT", primaryMap.get(MessageObjEntity.COL_LANGUAGE).s());
        Assertions.assertEquals("Oggetto principale", primaryMap.get(MessageObjEntity.COL_SUBJECT).s());
        Assertions.assertEquals("Corpo del messaggio principale", primaryMap.get(MessageObjEntity.COL_LONG_BODY).s());
        Assertions.assertEquals("Abstract principale", primaryMap.get(MessageObjEntity.COL_SHORT_BODY).s());

        // additionalMessage è mappato come Map in DynamoDB
        Map<String, AttributeValue> additionalMap = item.get(MessageEntity.COL_ADDITIONAL_MESSAGE).m();
        Assertions.assertEquals("DE", additionalMap.get(MessageObjEntity.COL_LANGUAGE).s());
        Assertions.assertEquals("Zusätzlicher Betreff", additionalMap.get(MessageObjEntity.COL_SUBJECT).s());

        // verifica round-trip
        Assertions.assertEquals("message-id", mappedEntity.getMessageId());
        Assertions.assertEquals("sender-id", mappedEntity.getSk());
        Assertions.assertEquals("IT", mappedEntity.getPrimaryMessage().getLanguage());
        Assertions.assertEquals("Oggetto principale", mappedEntity.getPrimaryMessage().getSubject());
        Assertions.assertEquals("DE", mappedEntity.getAdditionalMessage().getLanguage());
        Assertions.assertEquals("Zusätzlicher Betreff", mappedEntity.getAdditionalMessage().getSubject());
    }
}

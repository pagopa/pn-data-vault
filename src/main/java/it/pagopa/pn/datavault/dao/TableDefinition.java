package it.pagopa.pn.datavault.dao;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Getter
@Slf4j
public class TableDefinition {

    @Value("${pn.data-vault.dynamodb.table-name}")
    private String tableName;

    private String hashKeyAttributeName = "hashKey";
    private String sortKeyAttributeName = "sortKey";
    private String valueAttributeName = "value";

    @PostConstruct
    private void logTableName() {
      log.info("Used table tableName={}", tableName);
    }

}

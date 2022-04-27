package it.pagopa.pn.datavault.svc.mockvalues;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.svc.PnDataVaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
public class MockValueLoader {

    private String mockValuesFile;

    private final ApplicationContext ctx;
    private final PnDataVaultService svc;

    public MockValueLoader(ApplicationContext ctx, PnDataVaultService svc) {
        this.ctx = ctx;
        this.svc = svc;
    }

    @PostConstruct
    private void loadMockValue() {

        if( StringUtils.hasText(mockValuesFile) ) {
            loadMockValues( mockValuesFile );
        }
    }

    private void loadMockValues(String mockValuesFile) {

        Resource mockFileResource = ctx.getResource( mockValuesFile );

        try( InputStream mockFileInputStream = mockFileResource.getInputStream() ) {

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            MockValue[] values = mapper.readValue( mockFileInputStream, MockValue[].class);

            for(MockValue value: values) {
                RecipientType recipientType = value.getRecipientType();
                String taxId = value.getTaxId();
                String denomination = value.getDenomination();

                log.info("Preload recipientType={} taxId={} denomination=[{}]", recipientType, taxId, denomination);

                svc.ensureRecipientByExternalId( value.getRecipientType(), value.getTaxId() )
                    .flatMap( internalId -> {
                        BaseRecipientDto dto = new BaseRecipientDto();
                        dto.setInternalId( internalId );
                        dto.setDenomination( value.getDenomination() );
                        return svc.setDenominationByInternalId( internalId, dto );
                    })
                    .block();
            }

        } catch (IOException exc) {
            throw new RuntimeException( exc );
        }
    }

}

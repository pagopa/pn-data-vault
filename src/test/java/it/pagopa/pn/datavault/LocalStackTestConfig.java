package it.pagopa.pn.datavault;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*;

/**
 * Classe che permette di creare un container Docker di LocalStack.
 * Il container (e quindi la classe) può essere condivisa tra più classi di test.
 * Per utilizzare questa classe, le classi di test dovranno essere annotate con
 * @Import(LocalStackTestConfig.class)
 */
@TestConfiguration
public class LocalStackTestConfig {

    static LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.0.4.1.nodejs18"))
                    .withServices(DYNAMODB)
                    .withClasspathResourceMapping("testcontainers/init.sh",
                            "/docker-entrypoint-initaws.d/make-storages.sh", BindMode.READ_ONLY)
                    .withClasspathResourceMapping("testcontainers/credentials",
                            "/root/.aws/credentials", BindMode.READ_ONLY)
                    .withNetworkAliases("localstack")
                    .withNetwork(Network.builder().createNetworkCmdModifier(cmd -> cmd.withName("test-net")).build())
                    .waitingFor(Wait.forLogMessage(".*Initialization terminated.*", 1));

    static {
        localStack.start();
        System.setProperty("aws.endpoint-url", localStack.getEndpointOverride(DYNAMODB).toString());
    }
}

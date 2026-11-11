package io.kestra.plugin.debezium.mongodb;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.flows.Flow;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.validations.ModelValidator;
import io.kestra.core.serializers.YamlParser;
import io.kestra.core.utils.IdUtils;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

@KestraTest
class ConnectionStringValidationTest {
    private static final String CONNECTION_STRING = "mongodb://mongo_user:mongo_passwd@127.0.0.1:27017/?replicaSet=rs0";

    @Inject
    private ModelValidator modelValidator;

    @Test
    void flowWithOnlyConnectionStringIsValid() {
        var flow = YamlParser.parse("""
            id: %s
            namespace: company.team

            tasks:
              - id: %s
                type: io.kestra.plugin.debezium.mongodb.Capture
                connectionString: "%s"
                maxRecords: 10

            triggers:
              - id: %s
                type: io.kestra.plugin.debezium.mongodb.RealtimeTrigger
                connectionString: "%s"
              - id: %s
                type: io.kestra.plugin.debezium.mongodb.Trigger
                connectionString: "%s"
            """.formatted(IdUtils.create(), IdUtils.create(), CONNECTION_STRING, IdUtils.create(), CONNECTION_STRING, IdUtils.create(), CONNECTION_STRING), Flow.class);

        assertThat(modelValidator.isValid(flow).map(Throwable::getMessage), is(Optional.empty()));
    }

    @Test
    void captureWithDeprecatedHostnameAndPortIsStillValid() {
        var capture = Capture.builder()
            .id(IdUtils.create())
            .type(Capture.class.getName())
            .connectionString(Property.ofValue(CONNECTION_STRING))
            .hostname(Property.ofValue("127.0.0.1"))
            .port(Property.ofValue("27017"))
            .build();

        assertThat(modelValidator.isValid(capture).map(Throwable::getMessage), is(Optional.empty()));
    }

    @Test
    void captureWithoutConnectionStringIsInvalid() {
        var capture = Capture.builder()
            .id(IdUtils.create())
            .type(Capture.class.getName())
            .build();

        var violations = modelValidator.isValid(capture).orElseThrow().getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath().toString())
            .toList();

        assertThat(violations, contains("connectionString"));
    }
}

package io.kestra.plugin.debezium.mysql;

import org.junit.jupiter.api.Test;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.validations.ModelValidator;
import io.kestra.core.utils.IdUtils;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@KestraTest
class HostnameValidationTest {
    @Inject
    private ModelValidator modelValidator;

    @Test
    void captureWithoutHostnameAndPortIsInvalid() {
        var capture = Capture.builder()
            .id(IdUtils.create())
            .type(Capture.class.getName())
            .username(Property.ofValue("root"))
            .password(Property.ofValue("mysql_passwd"))
            .build();

        var violations = modelValidator.isValid(capture).orElseThrow().getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath().toString())
            .toList();

        assertThat(violations, containsInAnyOrder("hostname", "port"));
    }
}

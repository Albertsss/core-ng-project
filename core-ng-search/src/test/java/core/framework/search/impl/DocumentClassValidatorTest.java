package core.framework.search.impl;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.search.Index;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
class DocumentClassValidatorTest {
    @Test
    void validate() {
        new DocumentClassValidator(TestDocument.class).validate();
    }

    @Index(name = "test")
    public static class TestDocument {
        @Property(name = "date_time_field")
        public LocalDateTime dateTimeField;

        @NotNull
        @Property(name = "string_field")
        public String stringField;

        @Property(name = "list_field")
        public List<String> listField;

        @Property(name = "map_field")
        public Map<String, String> mapField;
    }
}

package org.max2ba.usernotificationservcei.util;

import lombok.experimental.UtilityClass;
import org.apache.avro.generic.GenericRecord;
import org.max2ba.usernotificationservcei.avro.UserEvent;
import org.max2ba.usernotificationservcei.avro.UserOperation;

@UtilityClass
public final class UserEventMapper {

    public static UserEvent toUserEvent(GenericRecord record) {
        return record == null ? null : UserEvent.newBuilder()
                .setEmail(getString(record, "email"))
                .setOperation("d".equals(getString(record, "__op")) ? UserOperation.DELETE : UserOperation.CREATE)
                .build();
    }

    private static String getString(GenericRecord record, String field) {
        var value = record.get(field);
        return value != null ? value.toString() : null;
    }
}

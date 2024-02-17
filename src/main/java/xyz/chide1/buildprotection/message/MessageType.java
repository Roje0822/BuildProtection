package xyz.chide1.buildprotection.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageType {

    NORMAL("messages"),
    ERROR("errorMessages");

    public final String key;
}

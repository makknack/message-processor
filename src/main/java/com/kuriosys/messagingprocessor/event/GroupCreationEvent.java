package com.kuriosys.messagingprocessor.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupCreationEvent extends Event {
    private String groupId;
}

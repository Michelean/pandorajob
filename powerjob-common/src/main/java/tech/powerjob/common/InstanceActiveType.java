package tech.powerjob.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InstanceActiveType {

    USEFUL(1),
    USELESS(2);

    private final int v;

}

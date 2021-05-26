package tech.powerjob.common.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Task Processor Type
 *
 * @author tjq
 * @since 2020/3/23
 */
@Getter
@AllArgsConstructor
public enum ProcessorType {

    BUILT_IN(1, "内建处理器"),
    EXTERNAL(4, "外部处理器（动态加载）"),

    SHELL(2, "SHELL脚本"),
    PYTHON(3, "Python脚本"),

    PYTHON2(5, "PYTHON2"),
    PYTHON3(6, "PYTHON3"),
    JDK7(7, "JDK7"),
    JDK8(8, "JDK8"),
    JAVA_WINDOWS(9,"JAVA_WINDOWS"),
    PYTHON_WINDOWS(10,"JAVA_WINDOWS"),
    EXE(11,"JAVA_WINDOWS"),
    CONTAINER_SCRIPT(12,"脚本容器");

    private final int v;
    private final String des;

    public static ProcessorType of(int v) {
        for (ProcessorType type : values()) {
            if (type.v == v) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown ProcessorType of " + v);
    }

    public static Boolean isKill(Integer type){
        List<ProcessorType> processorTypes = Lists.newArrayList(PYTHON2, PYTHON3, JDK7, JDK8);
        return processorTypes.contains(of(type));
    }

    public static Boolean isDestroy(Integer type){
        List<ProcessorType> processorTypes = Lists.newArrayList(JAVA_WINDOWS, PYTHON_WINDOWS, EXE);
        return processorTypes.contains(of(type));
    }

}

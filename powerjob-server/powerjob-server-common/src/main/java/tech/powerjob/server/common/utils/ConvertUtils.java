package tech.powerjob.server.common.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * User: zmx
 */
public class ConvertUtils {

    public static <T, R> R convert(T from, Supplier<R> supplier) {
        return convert(from, supplier, null);
    }

    public static <T, R> R convert(T from, Supplier<R> supplier, BiConsumer<T, R> consumer) {
        if (from == null) {
            return null;
        }

        BiConsumer<T, R> copyConsumer = BeanUtils::copyProperties;

        if (consumer != null) {
            consumer = copyConsumer.andThen(consumer);
        } else {
            consumer = copyConsumer;
        }

        R r = supplier.get();

        consumer.accept(from, r);

        return r;
    }

    public static <K, T, R> Map<K, R> convertMap(Map<K, T> map, Supplier<R> supplier, BiConsumer<T, R> consumer) {
        Map<K, R> returnMap = new HashMap<>();
        map.forEach((k, t) -> {
            R r = supplier.get();
            consumer.accept(t, r);
            returnMap.put(k, r);
        });
        return returnMap;
    }

    public static <K, T, R> Map<K, List<R>> convertMapList(Map<K, List<T>> map, Supplier<R> supplier, BiConsumer<T, R> consumer) {
        Map<K, List<R>> returnMap = new HashMap<>();
        map.forEach((k, v) -> v.forEach(t -> {
            R r = supplier.get();
            consumer.accept(t, r);
            returnMap.computeIfAbsent(k, key -> new ArrayList<>()).add(r);
        }));
        return returnMap;
    }

    public static <T, R> List<R> convertList(List<T> list, Supplier<R> supplier) {
        return ListUtils.toList(list, t -> convert(t, supplier));
    }

    public static <T, R> List<R> convertList(List<T> list, Supplier<R> supplier, BiConsumer<T, R> consumer) {
        return ListUtils.toList(list, t -> convert(t, supplier, consumer));
    }

}
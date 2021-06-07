package tech.powerjob.server.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

/**
 * User: zmx
 */
public class ListUtils {

    public static <R, T> List<R> toList(List<T> list, Supplier<List<R>> supplier, Function<T, R> function) {
        return stream(list).map(function).collect(Collectors.toCollection(supplier));
    }

    public static <T, R> List<R> toList(List<T> list, Function<T, R> function) {
        return stream(list).map(function).collect(Collectors.toList());
    }

    public static <T, R> List<R> toUniqueList(List<T> list, Function<T, R> function) {
        return stream(list).map(function).distinct().collect(Collectors.toList());
    }

    public static <T> boolean anyMatch(List<T> list, Predicate<T> predicate) {
        return stream(list).anyMatch(predicate);
    }

    public static <T> boolean allMatch(List<T> list, Predicate<T> predicate) {
        return stream(list).allMatch(predicate);
    }

    public static <R> List<R> filter(List<R> list, Predicate<R> predicate) {
        return stream(list).filter(predicate).collect(Collectors.toList());
    }

    public static <T, R> List<R> filter(List<T> list, Predicate<T> predicate, Function<T, R> function) {
        return stream(list).filter(predicate).map(function).collect(Collectors.toList());
    }

    public static <R> Set<R> filterSet(List<R> list, Predicate<R> predicate) {
        return stream(list).filter(predicate).collect(Collectors.toSet());
    }

    public static <T, R> Set<R> filterSet(List<T> list, Predicate<T> predicate, Function<T, R> function) {
        return stream(list).filter(predicate).map(function).collect(Collectors.toSet());
    }

    public static <T, R> List<T> distinctByProperty(List<T> list, Function<T, R> propertyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return stream(list).filter(t -> seen.add(propertyExtractor.apply(t))).collect(Collectors.toList());
    }

    public static <T, R> Set<R> toSet(List<T> list, Function<T, R> function) {
        return stream(list).map(function).collect(Collectors.toSet());
    }

    public static <T, K> Map<K, T> toMap(List<T> list, Function<T, K> keyMapper) {
        return toMap(list, keyMapper, t -> t);
    }

    public static <T, K, V> Map<K, V> toMap(List<T> list, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return toMap(list, keyMapper, valueMapper, (v, v2) -> v);
    }

    public static <T, K> Map<K, T> toMap(List<T> list, Function<T, K> keyMapper, BinaryOperator<T> mergeFunction) {
        return toMap(list, keyMapper, t -> t, mergeFunction);
    }

    public static <T, K, V> Map<K, V> toMap(List<T> list, Function<T, K> keyMapper, Function<T, V> valueMapper, BinaryOperator<V> mergeFunction) {
        return stream(list).collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction));
    }

    public static <K, T> Map<K, List<T>> group(List<T> list, Function<T, K> function) {
        return stream(list).collect(groupingBy(function));
    }

    public static <K, T, R> Map<K, List<R>> group(List<T> list, Function<T, K> function, Function<T, R> convert) {
        return stream(list).collect(groupingBy(function, mapping(convert, Collectors.toList())));
    }

    public static <K, T> Map<K, Set<T>> groupSet(List<T> list, Function<T, K> function) {
        return stream(list).collect(groupingBy(function, Collectors.toSet()));
    }

    public static <K, T, R> Map<K, Set<R>> groupSet(List<T> list, Function<T, K> function, Function<T, R> convert) {
        return stream(list).collect(groupingBy(function, mapping(convert, Collectors.toSet())));
    }

    public static List<String> toList(String str, String separatorChars) {
        if (StringUtils.isBlank(str)) {
            return Collections.emptyList();
        }
        return Arrays.asList(StringUtils.split(str, separatorChars));
    }

    private static <T> Stream<T> stream(List<T> list) {
        return list.parallelStream();
    }
}
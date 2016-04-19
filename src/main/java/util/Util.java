package util;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.*;
import static util.GeneralMath.abs;
import static util.GeneralMath.round;
import static util.NaUtil.isValue;

public class Util
{
    public static final double SCORE_VALUE_EPSILON = 5E-16;

    private static final Logger log = LoggerFactory.getLogger(Util.class);

    //
    // Double comparison
    //

    /**
     * Checks whether value1 is equal to value2, with a tiny error margin to correct for floating-point
     * arithmetic imprecision. Values are in or around range [-1,1].
     **/
    public static boolean scoreValueEquals(double value1, double value2)
    {
        if ( ! isValue(value1) && ! isValue(value2))
            return true;
        return abs(value1 - value2) < SCORE_VALUE_EPSILON;
    }

    /**
     * Checks whether value1 is equal to or less than value2, with a tiny error margin to correct for floating-point
     * arithmetic imprecision. Values are in or around range [-1,1].
     **/
    public static boolean scoreValueEqualToOrLessThan(double value1, double value2)
    {
        if ( ! isValue(value1) || ! isValue(value2))
            return false;
        return value1 <= value2 + SCORE_VALUE_EPSILON;
    }

    /**
     * Checks whether value1 is equal to or greater than value2, with a tiny error margin to correct for floating-point
     * arithmetic imprecision. Values are in or around range [-1,1].
     **/
    public static boolean scoreValueEqualToOrGreaterThan(double value1, double value2)
    {
        if ( ! isValue(value1) || ! isValue(value2))
            return false;
        return value1 >= value2 - SCORE_VALUE_EPSILON;
    }
    //
    // String normalization
    //

    /** Return string lower cased and trimmed. **/
    public static String normalize(String string)
    {
        if (string == null)
            return null;
        return string.trim().toLowerCase();
    }

    /** Return strings lower cased and trimmed. **/
    public static void normalize(@Nullable String[] strings)
    {
        if (strings != null)
            for (int i = 0; i < strings.length; i++)
                strings[i] = normalize(strings[i]);
    }

    /** Return strings lower cased and trimmed. **/
    public static void normalize(@Nullable List<String> strings)
    {
        if (strings != null)
            for (int i = 0; i < strings.size(); i++)
                strings.set(i, normalize(strings.get(i)));
    }

    public static boolean isNormalized(String string)
    {
        return Objects.equals(string, normalize(string));
    }

    public static boolean equalsNormalized(String string1, String string2)
    {
        return (Objects.equals(normalize(string1), normalize(string2)));
    }

    public static boolean containsNormalized(String string1, String query)
    {
        return (normalize(string1).contains(normalize(query)));
    }

    public static String shortenString(String string, int desiredLength)
    {
        int length = string.length();
        if (length <= desiredLength)
            return string;
        if (length < 9)
            return string.substring(0, Math.min(length, desiredLength));
        int partLength = ((desiredLength-2) / 2);
        return string.substring(0, partLength) + ".." + string.substring(length - partLength, length);
    }

    public static String shortenUrlString(String urlString, int desiredLength)
    {
        String urlStringWithoutPrefix =
                  urlString.startsWith("https://www.") ? urlString.substring(12)
                : urlString.startsWith("http://www.") ? urlString.substring(11)
                : urlString.startsWith("https://") ? urlString.substring(8)
                : urlString.startsWith("http://") ? urlString.substring(7)
                : urlString.startsWith("www.") ? urlString.substring(4)
                : urlString;
        return shortenString(urlStringWithoutPrefix, desiredLength);
    }


    //
    // Formatting percentages, values, NA, time
    //

    /** Formats score value with two digits, or " -" for NA. **/
    public static String shortForm(double d)
    {
        return isValue(d) ? scoreValueEquals(d, 0) ? "0" : String.format("%.2f", d)
                : " -";
    }

    /** Returns the int as a string, or " -" for NA. **/
    public static String shortForm(int i)
    {
        return isValue(i) ? String.format("%s", i) : " -";
    }

    /**
     * Formats a positive score value as a percentage. Result is not fixed-width.
     **/
    public static String asPercentage(double d)
    {
        if ( ! isValue(d))
            return "-";
        return String.valueOf(round(d * 100));
    }

    /**
     * Formats a positive score value as a percentage. Two spaces wide, no decimals, right aligned, <0..0.005> becomes "0."
     **/
    public static String asPercentageTwoSpaces(double d)
    {
        if ( ! isValue(d))
            return " -";
        if (scoreValueEquals(d, 1))
            return "HH";
        if (d > 1) {
            log.trace("Score {} is greater than 1", d);
            return "H!";
        }
        if (d > SCORE_VALUE_EPSILON && d < .005)
            return "0.";
        if (d > .995 && d < 1)
            return "99";
        return String.format("%2.0f", d * 100);
    }

    /**
     * Formats a positive or negative score value as a percentage.
     * Three spaces wide, no decimals, right aligned, <0..0.005> becomes " 0.", <-0.005..0> becomes "-0."
     **/
    public static String asPercentageTwoSpacesNeg(double d)
    {
        if ( ! isValue(d))
            return "  -";
        if (scoreValueEquals(d, 1))
            return " HH";
        if (scoreValueEquals(d, -1))
            return "-HH";
        if (d > 1) {
            log.trace("Score {} is greater than 1", d);
            return " H!";
        }
        if (d < -1) {
            log.trace("Score {} is less than -1", d);
            return "-H!";
        }
        if (d > SCORE_VALUE_EPSILON && d < .005)
            return " 0.";
        if (d < -SCORE_VALUE_EPSILON && d > -.005)
            return "-0.";
        else
            return String.format("%3.0f", d * 100);
    }

    /**
     * Produces string representations of numbers like 1, 11, 111, 1.1k, 1m, etc.
     */
    public static String toHumanReadableNumber(int number)
    {
        if (number > -9999 && number < 9999)
            return "" + number;
        return toHumanReadableNumber(number, 0);
    }

    /**
     * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invokation.
     * Taken from http://stackoverflow.com/a/4753866/1901037
     * @param n the number to format
     * @param iteration in fact this is the class from the array c
     * @return a String representing the number n formatted in a cool looking way.
     */
    private static String toHumanReadableNumber(double n, int iteration)
    {
        final char[] c = new char[]{'k', 'm', 'b', 't'};
        double d = ((long) n / 100) / 10.0;
        boolean isRound = (d * 10) % 10 == 0;//true if the decimal part is equal to 0 (then it's trimmed anyway)
        return (d < 1000 ? //this determines the class, i.e. 'k', 'm' etc
                ((d > 99.9 || isRound || d > 9.99 ? //this decides whether to trim the decimals
                        (int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
                ) + "" + c[iteration])
                : toHumanReadableNumber(d, iteration + 1));
    }

    /**
     * Print out a timespan in nanoseconds in a pleasantly readable format, for instance 2.34 µs.
     *
     * <p>Copied from Guava's Stopwatch class.</p>
     * @param nanos Timespan in nanos
     * @return Human-readable string representation of timespan.
     */
    public static String nanosToString(long nanos)
    {
        TimeUnit unit = chooseUnit(nanos);
        double value = (double) nanos / NANOSECONDS.convert(1, unit);
        return String.format("%.4g %s", value, abbreviate(unit));
    }

    /** Needed for nanosToString. Copied from Guava's Stopwatch class. */
    private static TimeUnit chooseUnit(long nanos)
    {
        if (DAYS.convert(nanos, NANOSECONDS) > 0) return DAYS;
        if (HOURS.convert(nanos, NANOSECONDS) > 0) return HOURS;
        if (MINUTES.convert(nanos, NANOSECONDS) > 0) return MINUTES;
        if (SECONDS.convert(nanos, NANOSECONDS) > 0) return SECONDS;
        if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) return MILLISECONDS;
        if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) return MICROSECONDS;
        return NANOSECONDS;
    }

    /** Needed for nanosToString. Copied from Guava's Stopwatch class. */
    private static String abbreviate(TimeUnit unit)
    {
        switch (unit) {
            case NANOSECONDS: return "ns";
            case MICROSECONDS: return "\u03bcs"; // μs
            case MILLISECONDS: return "ms";
            case SECONDS: return "s";
            case MINUTES: return "min";
            case HOURS: return "h";
            case DAYS: return "d";
            default: throw new AssertionError();
        }
    }

    public static <T> String listToStringLineByLine(List<T> list)
    {
        String result = "[\n";
        for (T t : list)
            result += "  " + t.toString() + "\n";
        result += "]";
        return result;
    }

    public static <K,V> String mapToStringLineByLine(Map<K,V> map)
    {
        String result = "[\n";
        for (Map.Entry<K, V> entry : map.entrySet())
            result += "  " + entry.getKey().toString() + " -> " + entry.getValue() + "\n";
        result += "]";
        return result;
    }

    static String toWidth(String string, int width)
    {
        return Strings.padEnd("" + string, width, ' ').substring(0, width);
    }


    //
    // File I/O
    //

    public static List<String> readFileOrLogError(String name)
    {
        try {
            return readFileOrThrowException(name);
        } catch (Exception e) {
            log.error("Error while reading file {}", name, e);
        }
        return Collections.emptyList();
    }

    public static List<String> readFileOrThrowException(String name) throws IOException
    {
        try {
            // This way of getting to a resource seems to work in Docker, Maven and Intellij IDEA.
            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            return reader.lines().collect(Collectors.toList());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static String readFileAsOneStringOrThrowException(String name) throws IOException
    {
        try {
            log.debug("Getting resource {}", name);
            // This way of getting to a resource seems to work in Docker, Maven and Intellij IDEA.
            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static CSVParser readCsvFileWithHeaders(String fileName) throws IOException
    {
        String fileContent = Util.readFileAsOneStringOrThrowException(fileName);
        return CSVParser.parse(fileContent, CSVFormat.EXCEL.withHeader().withAllowMissingColumnNames()
                .withIgnoreEmptyLines().withIgnoreSurroundingSpaces());
    }

    public static CSVParser readCsvFileWithoutHeaders(String fileName) throws IOException
    {
        String fileContent = Util.readFileAsOneStringOrThrowException(fileName);
        return CSVParser.parse(fileContent, CSVFormat.EXCEL.withIgnoreEmptyLines().withIgnoreSurroundingSpaces());
    }

    /** Returns relative filenames, e.g. "restaurantTaggings/Amersfoort.csv". **/
    public static List<String> getFilenamesInDirectory(String directoryName) throws IOException
    {
        // Getting filenames within a directory become non-trivial when files are zipped into a jar. Hence this
        // somewhat unelegant code.
        URL resource = Util.class.getClassLoader().getResource(directoryName);
        if (resource == null)
            throw new IOException("Can't find directory '" + directoryName + "'");

        URI uri;
        try {
            uri = resource.toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Cannot translate URL " + resource + " to URI", e);
        }

        Path myPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            myPath = fileSystem.getPath("/" + directoryName);
        } else {
            myPath = Paths.get(uri);
        }

        Stream<Path> walk = java.nio.file.Files.walk(myPath, 1);
        Iterator<Path> it = walk.iterator();
        List<String> results = new ArrayList<>();
        it.next(); // skip first element, as this is always the root itself
        while (it.hasNext()) {
            Path path = it.next();
            if (uri.getScheme().equals("jar"))
                results.add(path.toString().substring(1)); // peel off first slash
            else
                results.add(directoryName + "/" + path.getFileName());
        }
        return results;
    }

    public static URL getResource(String name)
    {
        log.debug("getResource '{}'", name);
        // This works in Maven and IDEA
        URL resource = Thread.currentThread().getContextClassLoader().getResource(name);
        log.debug("resource = " + resource);
        if (resource != null)
            return resource;

        // This works in JAR
        File file = new File(name);
        System.out.println("file.exists() = " + file.exists());
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Sorts a map by value. Adapted from http://stackoverflow.com/a/2581754/1901037
     *
     * @return A map that, when iterated over, returns keys, values or entries sorted by value
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map, boolean ascending)
    {
        Map<K,V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K,V>> st = map.entrySet().stream();

        Comparator<Map.Entry<K, V>> comparator = Comparator.comparing(Map.Entry::getValue);
        if ( ! ascending)
            comparator = comparator.reversed();

        st.sorted(comparator).forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    /**
     * Sorts a map by a function on its values. Adapted from http://stackoverflow.com/a/2581754/1901037
     *
     * @return A map that, when iterated over, returns keys, values or entries sorted by value
     */
    public static <K, V> Map<K, V> sortMapByValue(Map<K, V> map, Function<V, Comparable> function, boolean ascending)
    {
        Map<K,V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K,V>> entries = map.entrySet().stream();

        Comparator<Map.Entry<K, V>> comparator = Comparator.comparing(
                (Function<Map.Entry<K, V>, Comparable>) (kvEntry) -> function.apply(kvEntry.getValue())
        );
        if ( ! ascending)
            comparator = comparator.reversed();

        entries.sorted(comparator).forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    /**
     * Sorts a Map by a function on its value, ascendingly.
     *
     * <p>Example usage:</p>
     * <pre>
     *  Map&lt;Integer, ContentBasedScore&gt; sortedContentBasedScoreColumn =
     *      Util.sortColumn(recommendation.contentBasedScoreColumn, cbs -> cbs.contentBasedScore);
     * </pre>
     *
     * @return List of keys
     */
    public static <K,V> List<K> sortMapByValueFunctionAscending(Map<K, V> map, Function<V, Double> function)
    {
        Comparator<Map.Entry<K, V>> comparator =
                Comparator.comparingDouble(e -> getValueOr(function.apply(e.getValue()), Double.MIN_VALUE));
        return map.entrySet().stream()
                .sorted()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Sorts a Map by a function on its value, descendingly.
     *
     * <p>Example usage:</p>
     * <pre>
     *  Map&lt;Integer, ContentBasedScore&gt; sortedContentBasedScoreColumn =
     *      Util.sortColumn(recommendation.contentBasedScoreColumn, cbs -> cbs.contentBasedScore);
     * </pre>
     *
     * @return List of keys
     */
    public static <K,V> List<K> sortMapByValueFunctionDescending(Map<K, V> map, Function<V, Double> function)
    {
        Comparator<Map.Entry<K, V>> comparator =
                Comparator.comparingDouble(e -> getValueOr(function.apply(e.getValue()), Double.MIN_VALUE));
        return map.entrySet().stream()
                .sorted(comparator.reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private static Double getValueOr(Double aDouble, Double defaultValue)
    {
        if (aDouble == null) return defaultValue;
        if (Double.isNaN(aDouble)) return defaultValue;
        return aDouble;
    }

    //
    // Other
    //

    public static void simpleSleep(long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static <T, R> List<R> map(List<T> list, Function<T, R> function)
    {
        return list.stream().map(function::apply).collect(Collectors.toList());
    }

    public static <T, R> Set<R> map(Set<T> set, Function<T, R> function)
    {
        return set.stream()
                .filter(o -> o != null)
                .map(function::apply)
                .collect(Collectors.toSet());
    }

    public static <T, R> void mapPrint(List<T> list, Function<T, R> function)
    {
        list.stream().map(function::apply).forEach(System.out::println);
    }

    public static <T> List<T> filter(List<T> list, Function<T, Boolean> function)
    {
        return list.stream().filter(function::apply).collect(Collectors.toList());
    }

    /**
     * Returns the results of pairwise applying {@code function} on the elements of {@code list1} and {@code list2}.
     */
    public static <L1, L2, R> List<R> zipWith(List<L1> list1, List<L2> list2, BiFunction<L1, L2, R> function)
    {
        assert list1.size() == list2.size() : "zipWith: lists must be equal size";

        List<R> result = new ArrayList<>();
        for (int i = 0; i < list1.size(); i++) {
            L1 element1 = list1.get(i);
            L2 element2 = list2.get(i);
            result.add(i, function.apply(element1, element2));
        }
        return result;
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... objects)
    {
        Set<T> set = new HashSet<T>();
        Collections.addAll(set, objects);
        return set;
    }

    @SafeVarargs
    public static <T> List<T> asArrayList(T... objects)
    {
        List<T> list = new ArrayList<>();
        Collections.addAll(list, objects);
        return list;
    }

    public static String getEnvOrDefault(String name, String defaultValue)
    {
        String value = System.getenv(name);
        // If docker is told to copy an env var from host to container, and the var is not set on the host, it will
        // set the var on the container to ''
        if (Strings.isNullOrEmpty(value)) {
            log.debug("Environment variable {} not set, falling back to '{}'", name, defaultValue);
            value = defaultValue;
        } else {
            log.debug("{}={}", name, value);
        }
        return value;
    }

    public static void logMemory()
    {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long allocatedMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        log.info("Max usable memory (mb):                      {}", maxMemory);
        log.info("Allocated memory (mb):                       {}", allocatedMemory);
        log.info("Free memory within allocated (mb):           {}", freeMemory);
        log.info("Max memory that still can be allocated (mb): {}", maxMemory - allocatedMemory);
    }

}

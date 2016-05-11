package gmjonker.citool.util;

import com.google.common.base.Strings;

import java.util.Map;

/**
 * Utility methods that do not fall in any of the other categories.
 */
public class Util
{
    private static final LambdaLogger log = new LambdaLogger(Util.class);

    public static void simpleSleep(long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void logMemory()
    {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory       = runtime.maxMemory()   / 1024 / 1024;
        long allocatedMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory      = runtime.freeMemory()  / 1024 / 1024;
        log.info("Allocated memory (mb):                       {}", allocatedMemory);
        log.info("Free memory within allocated (mb):           {}", freeMemory);
        log.info("Max usable memory (mb):                      {}", maxMemory);
        log.info("Max memory that still can be allocated (mb): {}", maxMemory - allocatedMemory);
    }

    public static String getEnvOrFail(String name)
    {
        String value = System.getenv(name);
        // If docker is told to copy an env var from host to container, and the var is not set on the host, it will
        // set the var on the container to ''
        if (Strings.isNullOrEmpty(value)) {
            log.error("Environment variable {} not set, exiting...", name);
            System.exit(-1);
        }
        log.debug("{}={} (env)", name, value);
        return value;
    }

    public static String getEnvOrDefault(String name, String defaultValue)
    {
        String value = System.getenv(name);
        // If docker is told to copy an env var from host to container, and the var is not set on the host, it will
        // set the var on the container to ''
        if (Strings.isNullOrEmpty(value)) {
            value = defaultValue;
            log.debug("{}={} (default, override by setting env var)", name, value);
        } else {
            log.debug("{}={} (env)", name, value);
        }
        return value;
    }

    public static void printAllEnvironmentVariables()
    {
        Map<String, String> getenv = System.getenv();
        for (Map.Entry<String, String> entry : getenv.entrySet())
            System.out.println(entry.getKey() + "=" + entry.getValue());
    }

    public static void logEnvironmentVariable(String name)
    {
        String value = System.getenv(name);
        if (value != null)
            log.info(name + "=" + value);
        else
            log.info(name + " not set");
    }
}

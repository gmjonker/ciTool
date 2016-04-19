import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
//        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %-15.-15logger{15} - %msg%n"
        pattern = "%1.1level %-15.-15logger{15} - %msg%n"
    }
}

logger("a2t", DEBUG)

logger("a2t.citool.CiCorpusHelper", TRACE)

logger("org.mongodb", WARN)
logger("org.apache.solr.client.solrj", WARN)
logger("org.eclipse.jetty", WARN)
logger("org.glassfish.jersey", TRACE)

root(DEBUG, ["STDOUT"])
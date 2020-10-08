package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;

import org.apache.catalina.util.ServerInfo;

import org.springframework.core.env.Environment;

import java.util.Formatter;

/**
 * This is {@link CasTomcatBanner}.
 *
 * @author Misagh Moayyed
 * @author Longze Chen
 * @since 5.1.0
 */
public class CasTomcatBanner extends AbstractCasBanner {

    @Override
    protected void injectEnvironmentInfoIntoBanner(final Formatter formatter, final Environment environment,
                                                   final Class<?> sourceClass) {
        formatter.format("Apache Tomcat Version: %s%n", ServerInfo.getServerInfo());
        formatter.format("%s%n", LINE_SEPARATOR);
    }

    // Customize the title banner to use "OSF CAS" instead of "APEREO CAS"
    @Override
    protected String getTitle() {
        return "\n"
                + "   ___   ____  _____     ____    _    ____   \n"
                + "  / _ \\ / ___|| ____|   / ___|  / \\  / ___|  \n"
                + " | | | |\\___ \\| |__    | |     / _ \\ \\___ \\  \n"
                + " | |_| | ___) |  __|   | |___ / ___ \\ ___) | \n"
                + "  \\___/ \\____/|_|       \\____/_/   \\_\\____/  \n"
                + "                                             \n";

    }
}

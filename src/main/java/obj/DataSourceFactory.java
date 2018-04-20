package obj;

import com.zaxxer.hikari.HikariDataSource;
import database.mysql.context.ExceptionTranslator;
import database.mysql.context.SpringConnectionProvider;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import java.util.HashMap;
import java.util.Map;

public class DataSourceFactory {

    private Map<String, HikariDataSource> dataSourceMap;

    public DataSourceFactory() {
        dataSourceMap = new HashMap<>();
    }

    public DataSourceFactory addSource(String sourceName, HikariDataSource source) {
        dataSourceMap.put(sourceName, source);
        return this;
    }

    public HikariDataSource getSource(String sourceName) {
        return dataSourceMap.get(sourceName);
    }

    public Configuration getSourceConfig(String sourceName) {
        Settings settings = new Settings();
        settings.setExecuteLogging(false);

        return new DefaultConfiguration()
                .set(new SpringConnectionProvider(dataSourceMap.get(sourceName)))
                .set(SQLDialect.MARIADB)
                .set(new DefaultExecuteListenerProvider(new ExceptionTranslator(dataSourceMap.get(sourceName))))
                .set(settings);
    }

    public DataSourceTransactionManager getDataSourceTransactionManager(String sourceName) {
        return new DataSourceTransactionManager(new TransactionAwareDataSourceProxy(dataSourceMap.get(sourceName)));
    }

}

package obj;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import database.mysql.handler.TransactionalMethodInterceptor;
import database.mysql.module.DbConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

public class SqlConnector extends AbstractModule {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private DataSourceFactory dataSourceFactory;
    private String connectionName;
    private DbConfig config;

    public SqlConnector(DbConfig config,
                        String connectionName,
                        DataSourceFactory dataSourceFactory) {
        this.config = config;
        this.connectionName = connectionName;
        this.dataSourceFactory = dataSourceFactory;
        initPool(this.config, this.connectionName);
    }

    private void initPool(DbConfig config, String connectionName) {
        LOGGER.info("initializing connection");
        //deserialize the specified configJson into JsonObject

        //config hikariCp by above attributes
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSourceClassName(config.getDataSourceClassName());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.addDataSourceProperty("serverName", config.getHost());
        hikariConfig.addDataSourceProperty("port", config.getPort());
        hikariConfig.addDataSourceProperty("databaseName", config.getDatabase());
        hikariConfig.addDataSourceProperty("characterEncoding", StandardCharsets.UTF_8);
        hikariConfig.addDataSourceProperty("useUnicode", "true");
//        hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
//        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
//        hikariConfig.addDataSourceProperty("cachePrepStmts", true);
//        hikariConfig.addDataSourceProperty("useServerPrepStmts", true);
//        hikariConfig.addDataSourceProperty("useLocalSessionState", true);
//        hikariConfig.addDataSourceProperty("useLocalTransactionState", true);
//        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", true);
//        hikariConfig.addDataSourceProperty("cacheServerConfiguration", true);
//        hikariConfig.addDataSourceProperty("autoReconnectForPools", true);

//        hikariConfig.addDataSourceProperty("autoReconnect", true);
//        hikariConfig.addDataSourceProperty("failOverReadOnly", false);
//        hikariConfig.addDataSourceProperty("maxReconnects", 10);
//        hikariConfig.addDataSourceProperty("connectTimeout", 10000);
//        hikariConfig.setConnectionTimeout(100000);
//        hikariConfig.setIdleTimeout(3000);
//        hikariConfig.setMaxLifetime(10000);
//        hikariConfig.setMinimumIdle(1);
//        hikariConfig.setIdleTimeout(15000);
        //hikariConfig.addDataSourceProperty("useTimezone", "true");
        hikariConfig.addDataSourceProperty("serverTimezone", "Asia/Ho_Chi_Minh");
        hikariConfig.addDataSourceProperty("useLegacyDatetimeCode", "false");
        LOGGER.trace("Set character encoding utf8");
        hikariConfig.setAutoCommit(false); //disable this one because we will use our own TransactionalManager
        /*if(!Strings.isNullOrEmpty(poolName)) {
            hikariConfig.setPoolName(poolName);
        }*/

        if (config.getMaxPoolSize() != null) {
            hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
            hikariConfig.setMinimumIdle(10);
        } else {
            //using default pool size of hikariCP
            hikariConfig.setMaximumPoolSize(16);
            hikariConfig.setMinimumIdle(10);
        }
        dataSourceFactory.addSource(connectionName, new HikariDataSource(hikariConfig));
        LOGGER.trace("connection pool initialized");
    }

    @Override
    protected void configure() {
        //bind annotation
        bindInterceptor(Matchers.any(),
                Matchers.annotatedWith(Transactional.class),
                new TransactionalMethodInterceptor(dataSourceFactory.getDataSourceTransactionManager(this.connectionName)));
        LOGGER.info("Bind done");
    }
}

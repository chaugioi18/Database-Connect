package app;

import com.google.gson.Gson;
import com.google.inject.*;
import database.mysql.module.DbConnectionConfig;
import database.mysql.repository.DatabaseCache;
import database.mysql.repository.IDatabaseCache;
import database.mysql.repository.ITestDataRepo;
import database.mysql.repository.TestDataImpl;
import database.redis.IRedisAdapter;
import database.redis.RedisAdapter;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxImpl;
import obj.DataSourceFactory;
import obj.SqlConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;


public class ModuleService extends AbstractModule {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private Context context;
    private DbConnectionConfig config;
    private Vertx vertx;
    private DataSourceFactory dataSourceFactory;
//    private Map<String, CacheData> cacheMap;

    @Inject
    ModuleService(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.context = context;
        dataSourceFactory = new DataSourceFactory();
    }

    @Provides
    @Singleton
    public DbConnectionConfig getConfig() {
        return new Gson().fromJson(context.config().encode(), DbConnectionConfig.class);
    }

    @Provides
    @Singleton
    public Vertx getVertx() {
        return vertx;
    }

    @Provides
    @Singleton
    public ExecutorService getExecutorService() {
        VertxImpl vertxImpl = (VertxImpl) vertx;
        return vertxImpl.getWorkerPool();
    }

    @Provides
    @Singleton
    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    @Provides
    @Singleton
    public Context getContext() {
        return context;
    }

    @Override
    protected void configure() {

        bind(IService.class).to(ServiceImpl.class).in(Scopes.SINGLETON);
        bind(ITestDataRepo.class).to(TestDataImpl.class).in(Scopes.SINGLETON);
        bind(IRedisAdapter.class).to(RedisAdapter.class).in(Scopes.SINGLETON);
        bind(IDatabaseCache.class).to(DatabaseCache.class).in(Scopes.SINGLETON);
        bind(IDataService.class).to(DataService.class).in(Scopes.SINGLETON);

        //install(new SqlConnector(getConfig().getWriteDbConfig(), "write", dataSourceFactory));
        installJooq();

//        bindInterceptor(Matchers.any(),
//                Matchers.annotatedWith(Transactional.class),
//                new TransactionalMethodInterceptor(getDataSourceTransactionManager()));
    }

    private void installJooq() {
        install(new SqlConnector(getConfig().getReadDbConfig(), "read", dataSourceFactory));
        install(new SqlConnector(getConfig().getWriteDbConfig(), "write", dataSourceFactory));
    }

}

package app;

import com.google.gson.Gson;
import com.google.inject.Inject;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class ServiceImpl implements IService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private Vertx vertx;
    private ExecutorService executorService;
    private Context context;
    private IDataService dataService;

    @Inject
    ServiceImpl(Vertx vertx,
                ExecutorService executorService,
                Context context,
                IDataService dataService) {
        this.vertx = vertx;
        this.executorService = executorService;
        this.context = context;
        this.dataService = dataService;
    }

    @Override
    public IService executeData() {
        HttpServer server = vertx.createHttpServer(new HttpServerOptions()
                .setHost("localhost")
                .setPort(9990));
        Router router = Router.router(vertx);
        server.requestHandler(router::accept);
        LOGGER.trace("Is multi thread worker {} is event loop {} is worker {}", context.isMultiThreadedWorkerContext(), context.isEventLoopContext(), context.isWorkerContext());
        LOGGER.info("\n Core pool size {} \n max {} \n largest {} \n pool size {}", ((ThreadPoolExecutor) executorService).getCorePoolSize(),
                ((ThreadPoolExecutor) executorService).getMaximumPoolSize(), ((ThreadPoolExecutor) executorService).getLargestPoolSize(),
                ((ThreadPoolExecutor) executorService).getPoolSize());
        router.post("/test").handler(event -> {
            event.request().bodyHandler(buffer -> {
                vertx.executeBlocking(handle -> {
                    try {
//                        LOGGER.info("Number of worker {} and task count {}", ((ThreadPoolExecutor) executorService).getActiveCount(), ((ThreadPoolExecutor) executorService).getTaskCount());
//                        LOGGER.debug("Start with payload {}", String.valueOf(buffer));
                        Data data = new Gson().fromJson(String.valueOf(buffer), Data.class);
                        dataService.handleData(data);
                        handle.complete();
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                        handle.complete();
                    }
                }, res -> {
                    event.response().end();
                });
            });
        });
        router.get("/test").handler(request -> {
            LOGGER.info("Get from database");
        });
        server.listen();
//        vertx.setPeriodic(1000, handle -> {
//            LOGGER.trace("Count {}", Thread.activeCount());
//            LOGGER.trace("Count {}", ((ThreadPoolExecutor) executorService).getActiveCount());
//        });
        return this;
    }

    @Override
    public IService getData() {
        return this;
    }
}

package app;

import com.google.gson.Gson;
import com.google.inject.Inject;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
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

        LOGGER.trace("Is multi thread worker {} is event loop {} is worker {}", context.isMultiThreadedWorkerContext(), context.isEventLoopContext(), context.isWorkerContext());
        LOGGER.info("\n Core pool size {} \n max {} \n largest {} \n pool size {}", ((ThreadPoolExecutor) executorService).getCorePoolSize(),
                ((ThreadPoolExecutor) executorService).getMaximumPoolSize(), ((ThreadPoolExecutor) executorService).getLargestPoolSize(),
                ((ThreadPoolExecutor) executorService).getPoolSize());
        router.post("/test").handler(BodyHandler.create()).blockingHandler(this::testHandler, false);
//        router.get("/test").blockingHandler(request -> {
//            LOGGER.info("Get from database");
//        });
        server.requestHandler(router::accept).listen();
//        vertx.setPeriodic(1000, handle -> {
//            LOGGER.trace("Count {}", Thread.activeCount());
//            LOGGER.trace("Count {}", ((ThreadPoolExecutor) executorService).getActiveCount());
//        });
        return this;
    }

    private void testHandler(RoutingContext event) {
        LOGGER.info("Number of worker {} and task count {}", ((ThreadPoolExecutor) executorService).getActiveCount(), ((ThreadPoolExecutor) executorService).getTaskCount());
        if (((ThreadPoolExecutor) executorService).getActiveCount() == ((ThreadPoolExecutor) executorService).getMaximumPoolSize()) {
            event
                    .response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(500)
                    .end("Server internal error");
        } else {
            Buffer buffer = event.getBody();
            LOGGER.debug("Event loop thread over here");
            vertx.executeBlocking(handle -> {
                LOGGER.debug("Worker doing...");

                //WorkerExecutor executor = vertx.createSharedWorkerExecutor("chaunn", 40);
                try {
                    Thread.sleep(1000);
                    Data data = new Gson().fromJson(String.valueOf(buffer), Data.class);
                    dataService.handleData(data);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
                handle.complete();
            }, false, res -> {
                event.response()
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(200)
                        .end();
            });
        }

//        });
    }

    @Override
    public IService getData() {
        return this;
    }
}

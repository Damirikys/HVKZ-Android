package org.hvkz.hvkz.router;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public final class RouteChannel
{
    private static final List<Router> routers = new ArrayList<>();

    public static void sendRouteRequest(RouteRequest request) {
        for (Router router : routers) {
            router.onRouteRequest(request);
        }
    }

    static void subscribe(Router router) {
        routers.add(router);
    }

    static void unsubscribe(Router router) {
        routers.remove(router);
    }

    public static void clear() {
        routers.clear();
    }

    public static class RouteRequest
    {
        private Bundle args;

        public RouteRequest(Bundle args) {
            this.args = args;
        }

        public Bundle getArgs() {
            return args;
        }
    }
}

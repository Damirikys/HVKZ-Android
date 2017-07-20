package org.hvkz.hvkz.modules;

import android.os.Bundle;

import org.hvkz.hvkz.models.Router;

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

    public static void subscribe(Router router) {
        routers.add(router);
    }

    public static void unsubscribe(Router router) {
        routers.remove(router);
    }

    public static class RouteRequest {
        private Bundle args;

        public RouteRequest(Bundle args) {
            this.args = args;
        }

        public Bundle getArgs() {
            return args;
        }
    }
}

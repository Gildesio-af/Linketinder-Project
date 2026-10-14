package zg.acelera.controller

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import java.util.regex.Pattern

abstract class BaseWebController extends HttpServlet {
    protected final JsonSlurper jsonSlurper
    protected static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"

    BaseWebController(JsonSlurper jsonSlurper) {
        this.jsonSlurper = jsonSlurper
    }

    private static class Route {
        String httpMethod
        Pattern pattern
        Closure action
    }

    private final List<Route> routes = []

    protected void get(String regexPath, Closure action) {
        routes << new Route(httpMethod: "GET", pattern: ~"^${regexPath}\$", action: action)
    }

    protected void post(String regexPath, Closure action) {
        routes << new Route(httpMethod: "POST", pattern: ~"^${regexPath}\$", action: action)
    }

    protected void put(String regexPath, Closure action) {
        routes << new Route(httpMethod: "PUT", pattern: ~"^${regexPath}\$", action: action)
    }

    protected void delete(String regexPath, Closure action) {
        routes << new Route(httpMethod: "DELETE", pattern: ~"^${regexPath}\$", action: action)
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        configureHeader(resp)

        if (req.method == "OPTIONS") {
            resp.status = HttpServletResponse.SC_OK
            return
        }

        String path = req.pathInfo ?: "/"

        Route matchedRoute = routes.find { r ->
            r.httpMethod == req.method && r.pattern.matcher(path).matches()
        }

        if (!matchedRoute) {
            answerJson(resp, HttpServletResponse.SC_NOT_FOUND, [error: "Endpoint not found: ${req.method} ${path}"])
            return
        }

        try {
            matchedRoute.action.call(req, resp)
        } catch (IllegalArgumentException e) {
            answerJson(resp, HttpServletResponse.SC_BAD_REQUEST, [error: e.message])
        }
//        catch (Exception e) {
//            answerJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, [error: e.message])
//        }
    }

    protected static void answerJson(HttpServletResponse resp, int status, Object data) {
        resp.status = status
        if (data != null) {
            resp.writer.write(JsonOutput.toJson(data))
        }
    }

    protected Map readBody(HttpServletRequest req) {
        return (Map) jsonSlurper.parse(req.reader)
    }

    private static void configureHeader(HttpServletResponse resp) {
        resp.contentType = "application/json"
        resp.characterEncoding = "UTF-8"
        resp.setHeader("Access-Control-Allow-Origin", "*")
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type")
    }
}

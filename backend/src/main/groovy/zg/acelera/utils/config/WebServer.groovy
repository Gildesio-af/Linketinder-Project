package zg.acelera.utils.config

import org.apache.catalina.Context
import org.apache.catalina.Wrapper
import org.apache.catalina.startup.Tomcat
import zg.acelera.controller.CandidateWebController

class WebServer {
    private final int port
    private final Tomcat tomcat

    WebServer(int port = 8080) {
        this.port = port
        this.tomcat = new Tomcat()
    }

    void start(CandidateWebController candidateController) {
        tomcat.setPort(port)

        tomcat.getConnector()

        Context ctx = tomcat.addContext("", new File(".").absolutePath)

        Wrapper candidateWrapper = tomcat.addServlet(ctx, "candidatoServlet", candidateController)
        candidateWrapper.addMapping("/candidates/*")

        println "Servidor Tomcat iniciando na porta ${port}..."
        tomcat.start()
        println "Endpoints disponíveis em http://localhost:${port}/..."

        tomcat.getServer().await()
    }
}

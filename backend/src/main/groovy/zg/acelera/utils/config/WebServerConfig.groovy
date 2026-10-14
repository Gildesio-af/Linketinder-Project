package zg.acelera.utils.config

import org.apache.catalina.Context
import org.apache.catalina.Wrapper
import org.apache.catalina.startup.Tomcat
import zg.acelera.controller.CandidateWebController
import zg.acelera.controller.CompanyWebController

class WebServerConfig {
    private final int port
    private final Tomcat tomcat

    WebServerConfig(int port = 8080) {
        this.port = port
        this.tomcat = new Tomcat()
    }

    void start(CandidateWebController candidateController, CompanyWebController companyController) {
        tomcat.setPort(port)

        tomcat.getConnector()

        Context ctx = tomcat.addContext("", new File(".").absolutePath)

        Wrapper candidateWrapper = tomcat.addServlet(ctx, "candidatoServlet", candidateController)
        candidateWrapper.addMapping("/candidates/*")

        Wrapper companyWrapper = tomcat.addServlet(ctx, "empresaServlet", companyController)
        companyWrapper.addMapping("/companies/*")

        println "Servidor Tomcat iniciando na porta ${port}..."
        tomcat.start()
        println "Endpoints disponíveis em http://localhost:${port}/..."

        tomcat.getServer().await()
    }
}

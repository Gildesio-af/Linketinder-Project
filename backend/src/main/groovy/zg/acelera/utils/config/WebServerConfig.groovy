package zg.acelera.utils.config

import org.apache.catalina.Context
import org.apache.catalina.Wrapper
import org.apache.catalina.startup.Tomcat
import zg.acelera.controller.CandidateWebController
import zg.acelera.controller.CompanyWebController
import zg.acelera.controller.JobWebController
import zg.acelera.controller.AuthController
import zg.acelera.controller.RecruiterWebController
import zg.acelera.controller.RecruiterInvitationWebController
import zg.acelera.controller.RecruiterLikeWebController

class WebServerConfig {
    private final int port
    private final Tomcat tomcat

    WebServerConfig(int port = 8080) {
        this.port = port
        this.tomcat = new Tomcat()
    }

    void start(CandidateWebController candidateController, CompanyWebController companyController, JobWebController jobController) {
        start(candidateController, companyController, jobController, null, null, null, null)
    }

    void start(CandidateWebController candidateController, CompanyWebController companyController, JobWebController jobController,
               AuthController authController, RecruiterWebController recruiterController,
               RecruiterInvitationWebController invitationController, RecruiterLikeWebController likeController) {
        tomcat.setPort(port)

        tomcat.getConnector()

        Context ctx = tomcat.addContext("", new File(".").absolutePath)

        Wrapper candidateWrapper = tomcat.addServlet(ctx, "candidatoServlet", candidateController)
        candidateWrapper.addMapping("/candidates/*")

        Wrapper companyWrapper = tomcat.addServlet(ctx, "empresaServlet", companyController)
        companyWrapper.addMapping("/companies/*")

        Wrapper jobWrapper = tomcat.addServlet(ctx, "jobServlet", jobController)
        jobWrapper.addMapping("/jobs/*")

        if (authController) {
            Wrapper authWrapper = tomcat.addServlet(ctx, "authServlet", authController)
            authWrapper.addMapping("/auth/*")
        }
        if (recruiterController) {
            Wrapper recruiterWrapper = tomcat.addServlet(ctx, "recruiterServlet", recruiterController)
            recruiterWrapper.addMapping("/recruiters/*")
        }
        if (invitationController) {
            Wrapper invitationWrapper = tomcat.addServlet(ctx, "recruiterInvitationServlet", invitationController)
            invitationWrapper.addMapping("/recruiter-invitations/*")
        }
        if (likeController) {
            Wrapper likeWrapper = tomcat.addServlet(ctx, "recruiterLikeServlet", likeController)
            likeWrapper.addMapping("/recruiter-likes/*")
        }

        println "Servidor Tomcat iniciando na porta ${port}..."
        tomcat.start()
        println "Endpoints disponíveis em http://localhost:${port}/..."

        tomcat.getServer().await()
    }
}

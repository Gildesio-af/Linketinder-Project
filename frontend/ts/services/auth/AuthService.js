import { StorageService } from "../storage/StorageService.js";
import { RecruiterService } from "../recruiter/RecruiterService.js";
import { RecruiterSessionService } from "../recruiter/RecruiterSessionService.js";
const formLogin = document.getElementById("login-form");
const recruiterService = new RecruiterService();
if (formLogin) {
    formLogin.addEventListener("submit", async (event) => {
        event.preventDefault();
        const userInput = document.getElementById("login-user").value.trim();
        const password = document.getElementById("login-password").value;
        if (userInput.includes("@")) {
            try {
                const { recruiter } = await recruiterService.login({
                    email: userInput,
                    password
                });
                const session = {
                    userId: recruiter.userId,
                    name: recruiter.name,
                    email: recruiter.email,
                    recruitmentAreaId: recruiter.recruitmentAreaId,
                    recruitmentArea: recruiter.recruitmentArea ?? "",
                    description: recruiter.description,
                    status: recruiter.status
                };
                StorageService.deleteCurrentUser();
                RecruiterSessionService.start(session);
                document.getElementById("login-password").value = "";
                window.location.replace("./recruiter_dashboard.html");
            }
            catch {
                alert("Usuário ou senha inválidos");
            }
            return;
        }
        const user = userInput.replace(/\D/g, "");
        if (user.length == 11) {
            const candidates = StorageService.getCandidates();
            const candidate = candidates.find(c => c.cpf === user);
            if (candidate === undefined || candidate.password !== password) {
                alert("User or password invalid");
                return;
            }
            StorageService.setCurrentUser(candidate);
            RecruiterSessionService.clear();
            window.location.replace("./jobs.html");
        }
        else if (user.length == 14) {
            const companies = StorageService.getCompanies();
            const company = companies.find(c => c.cnpj === user);
            if (company === undefined || company.password !== password) {
                alert("User or password invalid");
                return;
            }
            StorageService.setCurrentUser(company);
            RecruiterSessionService.clear();
            window.location.replace("./company_menu.html");
        }
        else {
            throw new Error("User format invalid.");
        }
    });
}
//# sourceMappingURL=AuthService.js.map
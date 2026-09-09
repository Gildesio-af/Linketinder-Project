import { StorageService } from "../storage/StorageService.js";
const formLogin = document.getElementById("login-form");
if (formLogin) {
    formLogin.addEventListener("submit", (event) => {
        event.preventDefault();
        const user = document.getElementById("login-user").value.replace(/\D/g, '');
        const password = document.getElementById("login-password").value;
        if (user.length == 11) {
            const candidates = StorageService.getCandidates();
            const candidate = candidates.find(c => c.cpf === user);
            if (candidate === undefined || candidate.password !== password) {
                alert("User or password invalid");
                return;
            }
            StorageService.setCurrentUser(candidate);
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
            window.location.replace("./candidates.html");
        }
        else {
            throw new Error("User format invalid.");
        }
    });
}
//# sourceMappingURL=AuthService.js.map
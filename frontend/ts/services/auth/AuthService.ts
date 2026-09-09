import { StorageService } from "../storage/StorageService.js";
import type { User, Company, Candidate } from "../../models/Domain.js";


const formLogin = document.getElementById("login-form") as HTMLFormElement;

if (formLogin) {
    formLogin.addEventListener("submit", (event) => {
        event.preventDefault();

        const user = (document.getElementById("login-user") as HTMLInputElement).value.replace(/\D/g, '');
        const password = (document.getElementById("login-password") as HTMLInputElement).value;

        if(user.length == 11) {
            const candidates: Candidate[] = StorageService.getCandidates();
            const candidate: Candidate | undefined = candidates.find(c => c.cpf === user);

            if (candidate === undefined || candidate.password !== password) {
                alert("User or password invalid");
                return;
            }

            StorageService.setCurrentUser(candidate);
            window.location.replace("./jobs.html")

        } else if(user.length == 14) {
            const companies: Company[] = StorageService.getCompanies();
            const company: Company | undefined = companies.find(c => c.cnpj === user);

            if (company === undefined || company.password !== password) {
                alert("User or password invalid");
                return;
            }

            StorageService.setCurrentUser(company);
            window.location.replace("./candidates.html")
        } else {
            throw new Error("User format invalid.");
        }
    })
}




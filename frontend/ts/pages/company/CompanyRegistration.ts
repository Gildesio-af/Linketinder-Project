import { StorageService } from "../../services/storage/StorageService.js";
import type { Company } from "../../models/Domain.js";
import { skillsArray } from "../../services/tag/TagsService.js";

const formRegistration = document.getElementById('form-company-registration') as HTMLFormElement;

if (formRegistration) {
    formRegistration.addEventListener('submit', (event) => {
        event.preventDefault();

        const corporateName = (document.getElementById('corporate-name') as HTMLInputElement).value;
        const cnpj = (document.getElementById('cnpj') as HTMLInputElement).value;
        const corporateEmail = (document.getElementById('corporate-email') as HTMLInputElement).value;
        const place = (document.getElementById('c-place') as HTMLInputElement).value;
        const password = (document.getElementById('c-password') as HTMLInputElement).value;
        const confirmPassword = (document.getElementById('c-confirm-password') as HTMLInputElement).value;
        const companyDescription = (document.getElementById('company-description') as HTMLInputElement).value;

        if (password !== confirmPassword) {
            alert("As senhas não coincidem!");
            return;
        }

        const company: Company = {
            name: corporateName,
            cnpj: cnpj,
            email: corporateEmail,
            localization: place,
            password: password,
            confirmPassword: confirmPassword,
            description: companyDescription,
            skills: skillsArray,
            likedCandidates: []
        }

        try {
            StorageService.saveCompany(company);
            alert("Empresa cadastrada com sucesso!");
            formRegistration.reset();
            window.location.replace('./login.html');
        } catch (error) {
            if (error instanceof Error) {
                alert(error.message);
            }
        }
    })
}
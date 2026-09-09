import { StorageService } from "../../services/StorageService.js";
const formRegistration = document.getElementById('form-company-registration');
if (formRegistration) {
    formRegistration.addEventListener('submit', (event) => {
        event.preventDefault();
        const corporateName = document.getElementById('corporate-name').value;
        const cnpj = document.getElementById('cnpj').value;
        const corporateEmail = document.getElementById('corporate-email').value;
        const place = document.getElementById('c-place').value;
        const password = document.getElementById('c-password').value;
        const confirmPassword = document.getElementById('c-confirm-password').value;
        const companyDescription = document.getElementById('company-description').value;
        if (password !== confirmPassword) {
            alert("As senhas não coincidem!");
            return;
        }
        const company = {
            name: corporateName,
            cnpj: cnpj,
            email: corporateEmail,
            localization: place,
            password: password,
            confirmPassword: confirmPassword,
            description: companyDescription
        };
        try {
            StorageService.saveCompany(company);
            alert("Empresa cadastrada com sucesso!");
            formRegistration.reset();
            window.location.replace('./login.html');
        }
        catch (error) {
            if (error instanceof Error) {
                alert(error.message);
            }
        }
    });
}
//# sourceMappingURL=CompanyRegistration.js.map
import { RecruiterHttpError, RecruiterService } from "../../services/recruiter/RecruiterService.js";
const form = document.getElementById("recruiter-registration-form");
const feedback = document.getElementById("recruiter-registration-feedback");
const showError = (message) => {
    if (!feedback)
        return;
    feedback.textContent = message;
    feedback.hidden = false;
};
const clearCredentials = () => {
    const password = document.getElementById("recruiter-password");
    const confirmation = document.getElementById("recruiter-password-confirmation");
    if (password)
        password.value = "";
    if (confirmation)
        confirmation.value = "";
};
if (form) {
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const labels = {
            "recruiter-name": "Nome",
            "recruitment-area-id": "Área de recrutamento (UUID)",
            "recruiter-description": "Descrição",
            "recruiter-email": "E-mail",
            "recruiter-cpf": "CPF",
            "recruiter-birth-date": "Data de nascimento",
            "recruiter-password": "Senha (mínimo de 6 caracteres)",
            "recruiter-password-confirmation": "Confirmação de senha (mínimo de 6 caracteres)"
        };
        const invalidField = Array.from(form.elements).find((element) => (element instanceof HTMLInputElement || element instanceof HTMLTextAreaElement)
            && !element.checkValidity());
        if (invalidField) {
            invalidField.focus();
            showError(`Verifique o campo: ${labels[invalidField.id] ?? invalidField.id}.`);
            return;
        }
        if (feedback)
            feedback.hidden = true;
        const password = document.getElementById("recruiter-password").value;
        const confirmation = document.getElementById("recruiter-password-confirmation").value;
        if (password !== confirmation) {
            clearCredentials();
            showError("As senhas não coincidem.");
            return;
        }
        const cpf = document.getElementById("recruiter-cpf").value.replace(/\D/g, "");
        if (cpf.length !== 11) {
            clearCredentials();
            showError("Informe um CPF válido com 11 dígitos.");
            return;
        }
        try {
            await new RecruiterService().register({
                name: document.getElementById("recruiter-name").value.trim(),
                recruitmentAreaId: document.getElementById("recruitment-area-id").value.trim(),
                description: document.getElementById("recruiter-description").value.trim(),
                email: document.getElementById("recruiter-email").value.trim().toLowerCase(),
                cpf,
                birthDate: document.getElementById("recruiter-birth-date").value,
                password
            });
            clearCredentials();
            form.reset();
            window.location.replace("./recruiter_login.html");
        }
        catch (error) {
            clearCredentials();
            if (error instanceof RecruiterHttpError) {
                showError("Não foi possível concluir o cadastro. Verifique os dados informados.");
            }
            else {
                showError("Não foi possível concluir o cadastro. Tente novamente.");
            }
        }
    });
}
//# sourceMappingURL=RecruiterRegistration.js.map
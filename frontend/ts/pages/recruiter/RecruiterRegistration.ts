import { RecruiterHttpError, RecruiterService } from "../../services/recruiter/RecruiterService.js";

const form = document.getElementById("recruiter-registration-form") as HTMLFormElement | null;
const feedback = document.getElementById("recruiter-registration-feedback") as HTMLParagraphElement | null;

const showError = (message: string): void => {
    if (!feedback) return;
    feedback.textContent = message;
    feedback.hidden = false;
};

const clearCredentials = (): void => {
    const password = document.getElementById("recruiter-password") as HTMLInputElement | null;
    const confirmation = document.getElementById("recruiter-password-confirmation") as HTMLInputElement | null;
    if (password) password.value = "";
    if (confirmation) confirmation.value = "";
};

if (form) {
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        const password = (document.getElementById("recruiter-password") as HTMLInputElement).value;
        const confirmation = (document.getElementById("recruiter-password-confirmation") as HTMLInputElement).value;
        if (password !== confirmation) {
            clearCredentials();
            showError("As senhas não coincidem.");
            return;
        }

        const cpf = (document.getElementById("recruiter-cpf") as HTMLInputElement).value.replace(/\D/g, "");
        if (cpf.length !== 11) {
            clearCredentials();
            showError("Informe um CPF válido com 11 dígitos.");
            return;
        }

        try {
            await new RecruiterService("").register({
                name: (document.getElementById("recruiter-name") as HTMLInputElement).value.trim(),
                recruitmentAreaId: (document.getElementById("recruitment-area-id") as HTMLInputElement).value.trim(),
                description: (document.getElementById("recruiter-description") as HTMLTextAreaElement).value.trim(),
                email: (document.getElementById("recruiter-email") as HTMLInputElement).value.trim().toLowerCase(),
                cpf,
                birthDate: (document.getElementById("recruiter-birth-date") as HTMLInputElement).value,
                password
            });
            clearCredentials();
            form.reset();
            window.location.replace("./recruiter_login.html");
        } catch (error) {
            clearCredentials();
            if (error instanceof RecruiterHttpError) {
                showError("Não foi possível concluir o cadastro. Verifique os dados informados.");
            } else {
                showError("Não foi possível concluir o cadastro. Tente novamente.");
            }
        }
    });
}

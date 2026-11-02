import { RecruiterHttpError, RecruiterService } from "../../services/recruiter/RecruiterService.js";
import { RecruiterSessionService } from "../../services/recruiter/RecruiterSessionService.js";
import type { RecruiterSession } from "../../models/Domain.js";

const form = document.getElementById("recruiter-login-form") as HTMLFormElement | null;
const feedback = document.getElementById("recruiter-login-feedback") as HTMLParagraphElement | null;
const logout = document.getElementById("recruiter-logout") as HTMLButtonElement | null;
const service = new RecruiterService();

const showError = (message: string): void => {
    if (!feedback) return;
    feedback.textContent = message;
    feedback.hidden = false;
};

const clearPassword = (): void => {
    const password = document.getElementById("recruiter-login-password") as HTMLInputElement | null;
    if (password) password.value = "";
};

if (RecruiterSessionService.getCurrent()?.status === "ACTIVE") {
    window.location.replace("./recruiter_dashboard.html");
}

if (logout) {
    logout.hidden = !RecruiterSessionService.getCurrent();
    logout.addEventListener("click", () => {
        RecruiterSessionService.clear();
        logout.hidden = true;
    });
}

if (form) {
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const emailField = document.getElementById("recruiter-login-email") as HTMLInputElement;
        const passwordField = document.getElementById("recruiter-login-password") as HTMLInputElement;
        if (!emailField.value.trim() || !emailField.checkValidity()) {
            showError("Informe um e-mail válido.");
            emailField.focus();
            return;
        }
        if (!passwordField.value) {
            showError("Informe a senha.");
            passwordField.focus();
            return;
        }

        const password = (document.getElementById("recruiter-login-password") as HTMLInputElement).value;
        const email = (document.getElementById("recruiter-login-email") as HTMLInputElement).value.trim().toLowerCase();
        try {
            const { recruiter } = await service.login({ email, password });
            const session: RecruiterSession = {
                userId: recruiter.userId,
                name: recruiter.name,
                email: recruiter.email,
                recruitmentAreaId: recruiter.recruitmentAreaId,
                recruitmentArea: recruiter.recruitmentArea ?? "",
                description: recruiter.description,
                status: recruiter.status
            };
            RecruiterSessionService.start(session);
            clearPassword();
            window.location.replace("./recruiter_dashboard.html");
        } catch (error) {
            clearPassword();
            if (error instanceof RecruiterHttpError && error.status === 403) {
                showError("E-mail ou senha inválidos, ou a conta está inativa.");
            } else {
                showError("Não foi possível entrar. Verifique os dados e tente novamente.");
            }
        }
    });
}

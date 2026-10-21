import { RecruiterHttpError } from "../../services/recruiter/RecruiterService.js";
import { RecruiterSessionService } from "../../services/recruiter/RecruiterSessionService.js";
const form = document.getElementById("recruiter-login-form");
const feedback = document.getElementById("recruiter-login-feedback");
const logout = document.getElementById("recruiter-logout");
const apiBaseUrl = "";
const showError = (message) => {
    if (!feedback)
        return;
    feedback.textContent = message;
    feedback.hidden = false;
};
const clearPassword = () => {
    const password = document.getElementById("recruiter-login-password");
    if (password)
        password.value = "";
};
const loadProfile = async (id) => {
    const response = await fetch(`${apiBaseUrl}/recruiters/${encodeURIComponent(id)}`, {
        headers: { "X-User-Id": id, "X-User-Role": "RECRUITER" }
    });
    if (!response.ok)
        throw new RecruiterHttpError(response.status, await response.text());
    return await response.json();
};
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
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }
        const password = document.getElementById("recruiter-login-password").value;
        const email = document.getElementById("recruiter-login-email").value.trim().toLowerCase();
        try {
            const authResponse = await fetch(`${apiBaseUrl}/auth/`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username: email, password })
            });
            if (!authResponse.ok)
                throw new RecruiterHttpError(authResponse.status, await authResponse.text());
            const auth = await authResponse.json();
            if (auth.role !== "RECRUITER")
                throw new Error("This account is not a recruiter.");
            const recruiter = await loadProfile(auth.id);
            if (recruiter.status !== "ACTIVE")
                throw new Error("Inactive recruiter accounts cannot start a session.");
            const session = {
                userId: auth.id,
                name: recruiter.name,
                email: recruiter.email,
                recruitmentAreaId: recruiter.recruitmentAreaId,
                recruitmentArea: recruiter.recruitmentArea ?? "",
                description: recruiter.description,
                status: recruiter.status
            };
            RecruiterSessionService.start(session);
            clearPassword();
            window.location.replace("./recruiter_login.html");
        }
        catch (error) {
            clearPassword();
            if (error instanceof RecruiterHttpError && error.status === 403) {
                showError("E-mail ou senha inválidos, ou a conta está inativa.");
            }
            else {
                showError("Não foi possível entrar. Verifique os dados e tente novamente.");
            }
        }
    });
}
//# sourceMappingURL=RecruiterLogin.js.map
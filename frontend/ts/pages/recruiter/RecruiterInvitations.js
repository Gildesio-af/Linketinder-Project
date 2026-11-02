import { RecruiterHttpError, RecruiterService } from "../../services/recruiter/RecruiterService.js";
import { RecruiterSessionService } from "../../services/recruiter/RecruiterSessionService.js";
import { StorageService } from "../../services/storage/StorageService.js";
const session = RecruiterSessionService.getCurrent();
const service = new RecruiterService();
const feedback = document.getElementById("recruiter-invitations-feedback");
const loading = document.getElementById("recruiter-invitations-loading");
const list = document.getElementById("recruiter-invitations-list");
const refresh = document.getElementById("recruiter-invitations-refresh");
const logout = document.getElementById("recruiter-invitations-logout");
const description = document.getElementById("recruiter-invitations-description");
const statusLabels = {
    PENDING: "Pendente",
    ACCEPTED: "Aceito",
    DECLINED: "Recusado",
    CANCELLED: "Cancelado",
    REVOKED: "Revogado"
};
const showFeedback = (message) => {
    if (!feedback)
        return;
    feedback.textContent = message;
    feedback.hidden = false;
};
const clearFeedback = () => {
    if (feedback)
        feedback.hidden = true;
};
const formatDate = (value) => value ? new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" }).format(new Date(value)) : "—";
const getCompanyId = (invitation) => {
    if ("companyId" in invitation && typeof invitation.companyId === "string") {
        return invitation.companyId;
    }
    return "não informado";
};
const render = (invitations) => {
    if (!list)
        return;
    list.replaceChildren();
    if (invitations.length === 0) {
        const empty = document.createElement("p");
        empty.className = "empty-state";
        empty.textContent = "Você não possui convites no momento.";
        list.appendChild(empty);
        return;
    }
    invitations.forEach((invitation) => {
        const card = document.createElement("article");
        card.className = "invitation-card";
        const title = document.createElement("h3");
        title.textContent = "Convite para uma vaga";
        card.appendChild(title);
        const status = document.createElement("span");
        status.className = "invitation-status";
        status.textContent = statusLabels[invitation.status] ?? invitation.status;
        card.appendChild(status);
        const job = document.createElement("p");
        job.textContent = `Identificador da vaga: ${invitation.jobId}`;
        card.appendChild(job);
        const company = document.createElement("p");
        company.textContent = `Identificador da empresa: ${getCompanyId(invitation)}`;
        card.appendChild(company);
        const created = document.createElement("p");
        created.textContent = `Recebido em: ${formatDate(invitation.createdAt)}`;
        card.appendChild(created);
        if (invitation.status === "PENDING") {
            const actions = document.createElement("div");
            actions.className = "invitation-actions";
            actions.appendChild(createAction(invitation, "accept", "Aceitar"));
            actions.appendChild(createAction(invitation, "decline", "Recusar"));
            card.appendChild(actions);
        }
        list.appendChild(card);
    });
};
const createAction = (invitation, action, label) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `invitation-action${action === "decline" ? " decline" : ""}`;
    button.textContent = label;
    button.addEventListener("click", async () => {
        button.disabled = true;
        clearFeedback();
        StorageService.clearRecruiterInvitationCache();
        try {
            if (action === "accept") {
                await service.acceptInvitation(invitation.id);
            }
            else {
                await service.declineInvitation(invitation.id);
            }
            await loadInvitations();
        }
        catch (error) {
            StorageService.clearRecruiterInvitationCache();
            if (error instanceof RecruiterHttpError && (error.status === 401 || error.status === 403)) {
                showFeedback("Este convite não está disponível para esta sessão. A lista foi atualizada.");
                await loadInvitations();
            }
            else if (error instanceof RecruiterHttpError && error.status === 409) {
                showFeedback("O convite já foi alterado ou não está mais disponível.");
                await loadInvitations();
            }
            else {
                showFeedback("Não foi possível atualizar o convite. Tente novamente.");
                button.disabled = false;
            }
        }
    });
    return button;
};
const loadInvitations = async () => {
    if (!list || !loading)
        return;
    loading.hidden = false;
    if (refresh)
        refresh.disabled = true;
    clearFeedback();
    try {
        const invitations = await service.getInvitations();
        if (session)
            StorageService.setRecruiterInvitationCache(session.userId, invitations);
        render(invitations);
    }
    catch (error) {
        StorageService.clearRecruiterInvitationCache();
        if (error instanceof RecruiterHttpError && (error.status === 401 || error.status === 403)) {
            showFeedback("Sua sessão não está autorizada. Entre novamente para consultar os convites.");
        }
        else {
            showFeedback("Não foi possível carregar os convites. Tente novamente.");
        }
        list.replaceChildren();
    }
    finally {
        loading.hidden = true;
        if (refresh)
            refresh.disabled = false;
    }
};
if (!session || session.status !== "ACTIVE") {
    StorageService.clearRecruiterInvitationCache();
    window.location.replace("./recruiter_login.html");
}
else {
    if (description)
        description.textContent = `Olá, ${session.name}. Consulte apenas os convites destinados à sua conta.`;
    refresh?.addEventListener("click", () => void loadInvitations());
    logout?.addEventListener("click", () => {
        RecruiterSessionService.clear();
        window.location.replace("./recruiter_login.html");
    });
    void loadInvitations();
}
//# sourceMappingURL=RecruiterInvitations.js.map
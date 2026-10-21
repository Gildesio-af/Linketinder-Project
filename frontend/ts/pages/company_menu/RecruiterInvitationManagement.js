import { StorageService } from "../../services/storage/StorageService.js";
import { RecruiterHttpError, RecruiterService } from "../../services/recruiter/RecruiterService.js";
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const STATUS_LABELS = {
    PENDING: "Pendente",
    ACCEPTED: "Aceito",
    DECLINED: "Recusado",
    CANCELLED: "Cancelado",
    REVOKED: "Revogado"
};
export class RecruiterInvitationManagement {
    service;
    modal;
    form;
    emailInput;
    jobInput;
    feedback;
    list;
    constructor(service = new RecruiterService()) {
        this.service = service;
        this.modal = document.getElementById("recruiter-invitation-modal");
        this.form = document.getElementById("form-recruiter-invitation");
        this.emailInput = document.getElementById("recruiter-invitation-email");
        this.jobInput = document.getElementById("recruiter-invitation-job");
        this.feedback = document.getElementById("recruiter-invitation-feedback");
        this.list = document.getElementById("recruiter-invitation-list");
    }
    init(openButton) {
        openButton.addEventListener("click", () => this.open());
        this.modal.querySelector(".close-modal-invitation")
            ?.addEventListener("click", () => this.close());
        this.modal.addEventListener("click", (event) => {
            if (event.target === this.modal)
                this.close();
        });
        this.form.addEventListener("submit", (event) => {
            event.preventDefault();
            void this.submit();
        });
    }
    open() {
        this.modal.style.display = "block";
        this.setFeedback("", "info");
        void this.refresh();
    }
    close() {
        this.modal.style.display = "none";
    }
    /** Lê o identificador da empresa autenticada; nunca usa CNPJ como credencial. */
    companyId() {
        const user = StorageService.getCurrentUser();
        return user && typeof user.id === "string" && UUID_PATTERN.test(user.id) ? user.id : null;
    }
    async submit() {
        const companyId = this.companyId();
        const recruiterEmail = this.emailInput.value.trim().toLowerCase();
        const jobId = this.jobInput.value.trim();
        if (!companyId)
            return this.setFeedback("Sessão da empresa sem identificador válido.", "error");
        if (!EMAIL_PATTERN.test(recruiterEmail))
            return this.setFeedback("Informe um e-mail válido.", "error");
        if (!UUID_PATTERN.test(jobId))
            return this.setFeedback("Informe o identificador de uma vaga sua.", "error");
        try {
            await this.service.inviteRecruiter(companyId, { recruiterEmail, jobId });
            this.form.reset();
            this.setFeedback("Convite enviado.", "success");
            await this.refresh();
        }
        catch (error) {
            this.setFeedback(this.errorMessage(error), "error");
        }
    }
    async refresh() {
        const companyId = this.companyId();
        this.list.replaceChildren();
        if (!companyId) {
            this.list.appendChild(this.emptyItem("Faça login como empresa para gerenciar convites."));
            return;
        }
        try {
            const invitations = await this.service.getCompanyInvitations(companyId);
            if (invitations.length === 0) {
                this.list.appendChild(this.emptyItem("Nenhum convite enviado."));
                return;
            }
            invitations.forEach((invitation) => this.list.appendChild(this.renderItem(companyId, invitation)));
        }
        catch (error) {
            this.list.appendChild(this.emptyItem(this.errorMessage(error)));
        }
    }
    /** Renderiza somente campos administrativos mínimos. Usa textContent para evitar injeção de HTML. */
    renderItem(companyId, invitation) {
        const item = document.createElement("li");
        item.className = "invitation-item";
        const info = document.createElement("div");
        info.className = "invitation-info";
        const title = document.createElement("strong");
        title.className = "label-font-m";
        title.textContent = invitation.jobTitle ?? `Vaga ${invitation.jobId.slice(0, 8)}`;
        const meta = document.createElement("span");
        meta.className = "description-sm";
        meta.textContent = [invitation.recruiterEmail, invitation.recruiterArea]
            .filter((value) => Boolean(value))
            .join(" · ") || "Recrutador convidado";
        const status = document.createElement("span");
        status.className = `invitation-status status-${invitation.status.toLowerCase()}`;
        status.textContent = STATUS_LABELS[invitation.status] ?? invitation.status;
        info.append(title, meta);
        item.append(info, status);
        if (invitation.status === "PENDING") {
            item.appendChild(this.actionButton("Cancelar", () => this.service.cancelInvitation(companyId, invitation.id)));
        }
        else if (invitation.status === "ACCEPTED") {
            item.appendChild(this.actionButton("Revogar", () => this.service.revokeInvitation(companyId, invitation.id)));
        }
        return item;
    }
    actionButton(label, action) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "invitation-action label-font-s";
        button.textContent = label;
        button.addEventListener("click", async () => {
            button.disabled = true;
            try {
                await action();
                this.setFeedback(`Convite ${label === "Cancelar" ? "cancelado" : "revogado"}.`, "success");
                await this.refresh();
            }
            catch (error) {
                button.disabled = false;
                this.setFeedback(this.errorMessage(error), "error");
            }
        });
        return button;
    }
    emptyItem(text) {
        const item = document.createElement("li");
        item.className = "invitation-empty description-sm";
        item.textContent = text;
        return item;
    }
    setFeedback(message, kind) {
        this.feedback.textContent = message;
        this.feedback.className = `invitation-feedback description-sm ${kind}`;
    }
    /** Mensagens genéricas: não confirmam nem negam a existência de cadastros além do necessário. */
    errorMessage(error) {
        if (error instanceof RecruiterHttpError) {
            switch (error.status) {
                case 400: return "Dados do convite inválidos.";
                case 401:
                case 403: return "Operação não autorizada para esta vaga.";
                case 404: return "Recrutador ou vaga não encontrados.";
                case 409: return "Já existe um convite ativo para este recrutador nesta vaga.";
            }
        }
        return "Não foi possível concluir a operação.";
    }
}
//# sourceMappingURL=RecruiterInvitationManagement.js.map
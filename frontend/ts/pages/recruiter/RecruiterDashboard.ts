import { RecruiterHttpError, RecruiterService } from "../../services/recruiter/RecruiterService.js";
import { RecruiterSessionService } from "../../services/recruiter/RecruiterSessionService.js";
import { StorageService } from "../../services/storage/StorageService.js";
import type { AnonymousCandidate, AssignedJob, RecruiterMatch } from "../../models/Domain.js";

const session = RecruiterSessionService.getCurrent();
const service = new RecruiterService();
const feedback = document.getElementById("recruiter-dashboard-feedback") as HTMLParagraphElement | null;
const description = document.getElementById("recruiter-dashboard-description") as HTMLParagraphElement | null;
const jobsLoading = document.getElementById("recruiter-dashboard-jobs-loading") as HTMLParagraphElement | null;
const jobsList = document.getElementById("recruiter-dashboard-jobs") as HTMLDivElement | null;
const candidatesHint = document.getElementById("recruiter-dashboard-candidates-hint") as HTMLParagraphElement | null;
const candidatesLoading = document.getElementById("recruiter-dashboard-candidates-loading") as HTMLParagraphElement | null;
const candidatesList = document.getElementById("recruiter-dashboard-candidates") as HTMLDivElement | null;
const refresh = document.getElementById("recruiter-dashboard-refresh") as HTMLButtonElement | null;
const logout = document.getElementById("recruiter-dashboard-logout") as HTMLButtonElement | null;
const matchesLoading = document.getElementById("recruiter-dashboard-matches-loading") as HTMLParagraphElement | null;
const matchesList = document.getElementById("recruiter-dashboard-matches") as HTMLDivElement | null;

let selectedJobId: string | null = null;
// Estado visual das curtidas desta sessão de tela; nunca persistido.
const likedCandidates = new Set<string>();

const showFeedback = (message: string): void => {
    if (!feedback) return;
    feedback.textContent = message;
    feedback.hidden = false;
};

const clearFeedback = (): void => {
    if (feedback) feedback.hidden = true;
};

const isAuthorizationError = (error: unknown): boolean =>
    error instanceof RecruiterHttpError && (error.status === 401 || error.status === 403);

const resetCandidates = (hint: string): void => {
    selectedJobId = null;
    likedCandidates.clear();
    candidatesList?.replaceChildren();
    if (candidatesHint) {
        candidatesHint.textContent = hint;
        candidatesHint.hidden = false;
    }
};

const buildSkills = (skills: { name: string }[]): HTMLUListElement => {
    const skillList = document.createElement("ul");
    skillList.className = "skill-list";
    skills.forEach((skill) => {
        const item = document.createElement("li");
        item.className = "skill-tag";
        item.textContent = skill.name;
        skillList.appendChild(item);
    });
    return skillList;
};

const formatLocation = (job: AssignedJob): string =>
    [job.city, job.state, job.country].filter((part): part is string => Boolean(part)).join(", ") || "Não informada";

const renderJobs = (jobs: AssignedJob[]): void => {
    if (!jobsList) return;
    jobsList.replaceChildren();
    if (jobs.length === 0) {
        const empty = document.createElement("p");
        empty.className = "empty-state";
        empty.textContent = "Você não possui vagas com convite aceito.";
        jobsList.appendChild(empty);
        return;
    }

    jobs.forEach((job) => {
        const card = document.createElement("article");
        card.className = `job-card${job.id === selectedJobId ? " selected" : ""}`;

        const title = document.createElement("h3");
        title.textContent = job.name;
        card.appendChild(title);

        const jobDescription = document.createElement("p");
        jobDescription.textContent = job.description;
        card.appendChild(jobDescription);

        const location = document.createElement("p");
        location.textContent = `Localização: ${formatLocation(job)}`;
        card.appendChild(location);

        card.appendChild(buildSkills(job.skills ?? []));

        const select = document.createElement("button");
        select.type = "button";
        select.className = "job-select";
        select.textContent = "Ver candidatos";
        select.addEventListener("click", () => void loadCandidates(job.id));
        card.appendChild(select);

        jobsList.appendChild(card);
    });
};

const createLikeButton = (jobId: string, candidateId: string): HTMLButtonElement => {
    const button = document.createElement("button");
    button.type = "button";
    const sync = (): void => {
        const liked = likedCandidates.has(candidateId);
        button.className = `like-button${liked ? " liked" : ""}`;
        button.textContent = liked ? "Descurtir" : "Curtir";
    };
    sync();

    button.addEventListener("click", async () => {
        button.disabled = true; // evita envio duplicado enquanto a ação está em andamento
        clearFeedback();
        try {
            if (likedCandidates.has(candidateId)) {
                await service.unlikeCandidate(jobId, candidateId);
                likedCandidates.delete(candidateId);
            } else {
                await service.likeCandidate(jobId, candidateId);
                likedCandidates.add(candidateId);
            }
            sync();
            button.disabled = false;
        } catch (error) {
            if (isAuthorizationError(error)) {
                showFeedback("Você não está mais autorizado nesta vaga. A tela foi atualizada.");
                resetCandidates("Selecione uma vaga para ver os candidatos.");
                await loadJobs();
            } else {
                showFeedback("Não foi possível atualizar a curtida. Tente novamente.");
                button.disabled = false;
            }
        }
    });
    return button;
};

const renderCandidates = (jobId: string, candidates: AnonymousCandidate[]): void => {
    if (!candidatesList) return;
    candidatesList.replaceChildren();
    if (candidates.length === 0) {
        const empty = document.createElement("p");
        empty.className = "empty-state";
        empty.textContent = "Nenhum candidato interessado nesta vaga no momento.";
        candidatesList.appendChild(empty);
        return;
    }

    candidates.forEach((candidate, index) => {
        const card = document.createElement("article");
        card.className = "candidate-card";

        // Rótulo sequencial local: o identificador real nunca é exibido.
        const title = document.createElement("h3");
        title.textContent = `Candidato anônimo ${index + 1}`;
        card.appendChild(title);

        const summary = document.createElement("p");
        summary.textContent = candidate.description || "Sem descrição informada.";
        card.appendChild(summary);

        card.appendChild(buildSkills(candidate.skills ?? []));

        if (candidate.candidateId) {
            card.appendChild(createLikeButton(jobId, candidate.candidateId));
        } else {
            const unavailable = document.createElement("p");
            unavailable.className = "empty-state";
            unavailable.textContent = "Curtida indisponível para este candidato.";
            card.appendChild(unavailable);
        }
        candidatesList.appendChild(card);
    });
};

const renderMatches = (matches: RecruiterMatch[]): void => {
    if (!matchesList) return;
    matchesList.replaceChildren();
    if (matches.length === 0) {
        const empty = document.createElement("p");
        empty.className = "empty-state";
        empty.textContent = "Nenhum match autorizado encontrado.";
        matchesList.appendChild(empty);
        return;
    }

    matches.forEach((match) => {
        const card = document.createElement("article");
        card.className = "match-card";
        const title = document.createElement("h3");
        title.textContent = "Match confirmado";
        const job = document.createElement("p");
        job.textContent = `Vaga: ${match.jobId}`;
        const candidate = document.createElement("p");
        candidate.textContent = "Candidato identificado somente pelo vínculo autorizado.";
        card.append(title, job, candidate);
        matchesList.appendChild(card);
    });
};

const loadMatches = async (): Promise<void> => {
    if (!matchesList || !matchesLoading) return;
    matchesLoading.hidden = false;
    try {
        renderMatches(await service.getMatches());
    } catch {
        matchesList.replaceChildren();
        showFeedback("Não foi possível carregar os matches autorizados. Tente novamente.");
    } finally {
        matchesLoading.hidden = true;
    }
};

const loadCandidates = async (jobId: string): Promise<void> => {
    if (!candidatesLoading || !candidatesList) return;
    clearFeedback();
    selectedJobId = jobId;
    likedCandidates.clear();
    candidatesList.replaceChildren();
    if (candidatesHint) candidatesHint.hidden = true;
    candidatesLoading.hidden = false;
    try {
        const candidates = await service.getAnonymousCandidates(jobId);
        renderCandidates(jobId, candidates);
        await loadJobs(false);
    } catch (error) {
        if (isAuthorizationError(error)) {
            showFeedback("Esta vaga não está disponível para a sua conta. A lista foi atualizada.");
            resetCandidates("Selecione uma vaga para ver os candidatos.");
            await loadJobs();
        } else {
            showFeedback("Não foi possível carregar os candidatos. Tente novamente.");
            resetCandidates("Selecione uma vaga para ver os candidatos.");
        }
    } finally {
        candidatesLoading.hidden = true;
    }
};

const loadJobs = async (showLoading = true): Promise<void> => {
    if (!session || !jobsList || !jobsLoading) return;
    if (showLoading) jobsLoading.hidden = false;
    if (refresh) refresh.disabled = true;
    try {
        const jobs = await service.getAssignedJobs(session.userId);
        if (selectedJobId && !jobs.some((job) => job.id === selectedJobId)) {
            resetCandidates("A vaga selecionada não está mais disponível.");
        }
        renderJobs(jobs);
    } catch (error) {
        jobsList.replaceChildren();
        resetCandidates("Selecione uma vaga para ver os candidatos.");
        showFeedback(isAuthorizationError(error)
            ? "Sua sessão não está autorizada. Entre novamente para consultar as vagas."
            : "Não foi possível carregar as vagas. Tente novamente.");
    } finally {
        jobsLoading.hidden = true;
        if (refresh) refresh.disabled = false;
    }
};

if (!session || session.status !== "ACTIVE") {
    StorageService.clearRecruiterInvitationCache();
    window.location.replace("./recruiter_login.html");
} else {
    if (description) description.textContent = `Olá, ${session.name}. Candidatos são exibidos sem dados de identificação.`;
    refresh?.addEventListener("click", () => {
        clearFeedback();
        void loadJobs();
        void loadMatches();
        if (selectedJobId) void loadCandidates(selectedJobId);
    });
    logout?.addEventListener("click", () => {
        RecruiterSessionService.clear();
        window.location.replace("./recruiter_login.html");
    });
    void loadJobs();
    void loadMatches();
}

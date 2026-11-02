import type {
    AnonymousCandidate,
    AssignedJob,
    InvitationStatus,
    Recruiter,
    RecruiterAuthResponse,
    RecruiterInvitation,
    RecruiterLike,
    RecruiterLoginRequest,
    RecruiterMatch,
    RecruiterRegistrationRequest
} from "../../models/Domain.js";
import { StorageService } from "../storage/StorageService.js";
import {
    RECRUITMENT_AREAS,
    RecruiterLocalStore,
    type StoredInvitation,
    type StoredRecruiter
} from "./RecruiterLocalStore.js";

/**
 * Erro de regra de negócio/autorização do fluxo de recrutador. Mantém o
 * código de status HTTP equivalente para que as telas tratem 400/401/403/404/409.
 */
export class RecruiterHttpError extends Error {
    readonly status: number;
    readonly responseBody: string;

    constructor(status: number, responseBody: string) {
        super(`Recruiter request failed with status ${status}.`);
        this.name = "RecruiterHttpError";
        this.status = status;
        this.responseBody = responseBody;
    }
}

/**
 * Projeção administrativa mínima de um convite, vista pela empresa.
 * Nunca contém nome, CPF, data de nascimento ou outros dados pessoais do recrutador.
 */
export interface CompanyRecruiterInvitation {
    id: string;
    jobId: string;
    status: InvitationStatus;
    createdAt: string;
    respondedAt?: string | null;
    revokedAt?: string | null;
    recruiterEmail?: string;
    recruiterArea?: string;
    jobTitle?: string;
}

export interface CompanyInvitationRequest {
    recruiterEmail: string;
    jobId: string;
}

export interface AvailableRecruiter {
    email: string;
    recruitmentArea: string;
}

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * Serviço do recrutador com persistência em localStorage.
 * A autorização é aplicada aqui, antes de qualquer leitura de dados.
 */
export class RecruiterService {
    register(request: RecruiterRegistrationRequest): Promise<RecruiterAuthResponse> {
        return this.run(() => {
            const name = request.name.trim();
            const description = request.description.trim();
            const email = request.email.trim().toLowerCase();
            const cpf = request.cpf.replace(/[.\-\s]/g, "");
            const area = RECRUITMENT_AREAS.find((item) => item.id === request.recruitmentAreaId);

            if (!name || !description) this.fail(400, "Nome e descrição são obrigatórios.");
            if (!EMAIL_PATTERN.test(email)) this.fail(400, "E-mail inválido.");
            if (!/^\d{11}$/.test(cpf)) this.fail(400, "CPF inválido.");
            if (!area) this.fail(400, "Área de recrutamento inexistente.");
            if (!this.isAdult(request.birthDate)) this.fail(400, "O recrutador deve ter ao menos 18 anos.");
            if (!request.password || request.password.length < 6) this.fail(400, "Senha muito curta.");

            const recruiters = RecruiterLocalStore.getRecruiters();
            const emailTaken = recruiters.some((item) => item.email === email)
                || StorageService.getCandidates().some((item) => item.email.toLowerCase() === email)
                || StorageService.getCompanies().some((item) => item.email.toLowerCase() === email);
            const cpfTaken = recruiters.some((item) => item.cpf === cpf)
                || StorageService.getCandidates().some((item) => item.cpf.replace(/\D/g, "") === cpf);
            if (emailTaken || cpfTaken) this.fail(409, "E-mail ou CPF já cadastrado.");

            const stored: StoredRecruiter = {
                userId: RecruiterLocalStore.newId(),
                name,
                email,
                recruitmentAreaId: area!.id,
                recruitmentArea: area!.name,
                description,
                status: "ACTIVE",
                cpf,
                birthDate: request.birthDate,
                password: request.password
            };
            RecruiterLocalStore.saveRecruiters([...recruiters, stored]);
            return { recruiter: this.toPublicRecruiter(stored) };
        });
    }

    login(request: RecruiterLoginRequest): Promise<RecruiterAuthResponse> {
        return this.run(() => {
            const email = request.email.trim().toLowerCase();
            const recruiter = RecruiterLocalStore.getRecruiters()
                .find((item) => item.email === email && item.password === request.password);
            if (!recruiter || recruiter.status !== "ACTIVE") this.fail(403, "Credenciais inválidas ou conta inativa.");
            return { recruiter: this.toPublicRecruiter(recruiter!) };
        });
    }

    getInvitations(): Promise<RecruiterInvitation[]> {
        return this.runAsRecruiter((recruiter) => {
            const jobs = StorageService.getJobs();
            return RecruiterLocalStore.getInvitations()
                .filter((item) => item.recruiterId === recruiter.userId)
                .map((item) => {
                    const job = jobs.find((candidate) => candidate.name === item.jobId);
                    return {
                        id: item.id,
                        jobId: item.jobId,
                        jobTitle: job?.name ?? item.jobId,
                        jobDescription: job?.description ?? "",
                        jobLocation: job?.location ?? "",
                        jobSkills: job?.skills ?? [],
                        status: item.status,
                        createdAt: item.createdAt
                    };
                });
        });
    }

    acceptInvitation(invitationId: string): Promise<RecruiterInvitation> {
        return this.respondToInvitation(invitationId, "ACCEPTED");
    }

    declineInvitation(invitationId: string): Promise<RecruiterInvitation> {
        return this.respondToInvitation(invitationId, "DECLINED");
    }

    getAssignedJobs(recruiterId: string): Promise<AssignedJob[]> {
        return this.runAsRecruiter((recruiter) => {
            if (recruiter.userId !== recruiterId) this.fail(403, "Acesso permitido somente às próprias vagas.");
            const jobs = StorageService.getJobs();
            const acceptedIds = new Set(this.acceptedJobIds(recruiter.userId));
            return jobs
                .filter((job) => acceptedIds.has(job.name))
                .map((job) => ({
                    id: job.name,
                    name: job.name,
                    description: job.description,
                    skills: (job.skills ?? []).map((skill) => ({ id: skill, name: skill })),
                    city: job.location
                }));
        });
    }

    getAnonymousCandidates(jobId: string): Promise<AnonymousCandidate[]> {
        return this.runAsRecruiter((recruiter) => {
            this.requireAcceptedJob(recruiter.userId, jobId);
            // Projeção anônima: somente descrição e competências; o id é opaco.
            return StorageService.getCandidates()
                .filter((candidate) => (candidate.likedJobs ?? []).includes(jobId))
                .map((candidate) => ({
                    candidateId: this.opaqueCandidateId(candidate.cpf),
                    description: candidate.description,
                    skills: (candidate.skills ?? []).map((skill) => ({ id: skill, name: skill }))
                }));
        });
    }

    getActiveLikeCandidateIds(jobId: string): Promise<string[]> {
        return this.runAsRecruiter((recruiter) => {
            this.requireAcceptedJob(recruiter.userId, jobId);
            return RecruiterLocalStore.getLikes()
                .filter((like) =>
                    like.recruiterId === recruiter.userId
                    && like.jobId === jobId
                    && like.status === "ACTIVE"
                )
                .map((like) => like.candidateId);
        });
    }

    likeCandidate(jobId: string, candidateId: string): Promise<RecruiterLike> {
        return this.setLike(jobId, candidateId, true);
    }

    unlikeCandidate(jobId: string, candidateId: string): Promise<RecruiterLike> {
        return this.setLike(jobId, candidateId, false);
    }

    getMatches(): Promise<RecruiterMatch[]> {
        return this.runAsRecruiter((recruiter) => {
            const acceptedInvitations = RecruiterLocalStore.getInvitations()
                .filter((invitation) =>
                    invitation.recruiterId === recruiter.userId && invitation.status === "ACCEPTED"
                );
            const activeLikes = RecruiterLocalStore.getLikes()
                .filter((like) =>
                    like.recruiterId === recruiter.userId && like.status === "ACTIVE"
                );
            const candidateLikes = new Map(
                StorageService.getCandidates().map((candidate) => [
                    this.opaqueCandidateId(candidate.cpf),
                    new Set(candidate.likedJobs ?? [])
                ])
            );
            const invitationByJob = new Map(
                acceptedInvitations.map((invitation) => [invitation.jobId, invitation])
            );

            return activeLikes
                .filter((like) => candidateLikes.get(like.candidateId)?.has(like.jobId) === true)
                .filter((like) => invitationByJob.has(like.jobId))
                .flatMap((like) => {
                    const candidate = StorageService.getCandidates().find(
                        (item) => this.opaqueCandidateId(item.cpf) === like.candidateId
                    );
                    if (!candidate) return [];

                    return [{
                        jobId: like.jobId,
                        candidateId: like.candidateId,
                        createdAt: like.createdAt,
                        candidateName: candidate.name,
                        candidateEmail: candidate.email,
                        candidateDescription: candidate.description
                    }];
                });
        });
    }

    getCompanyMatches(companyId: string): Promise<RecruiterMatch[]> {
        return this.run(() => {
            if (!companyId) this.fail(401, "Empresa não autenticada.");

            const acceptedInvitations = RecruiterLocalStore.getInvitations()
                .filter((invitation) =>
                    invitation.companyId === companyId && invitation.status === "ACCEPTED"
                );
            const acceptedJobs = new Set(acceptedInvitations.map((invitation) => invitation.jobId));
            const candidates = StorageService.getCandidates();
            const candidateById = new Map(
                candidates.map((candidate) => [this.opaqueCandidateId(candidate.cpf), candidate])
            );
            const grouped = new Map<string, RecruiterMatch>();

            RecruiterLocalStore.getLikes()
                .filter((like) =>
                    like.status === "ACTIVE"
                    && acceptedJobs.has(like.jobId)
                    && candidateById.get(like.candidateId)?.likedJobs.includes(like.jobId) === true
                )
                .forEach((like) => {
                    const candidate = candidateById.get(like.candidateId);
                    if (!candidate) return;

                    const key = `${like.jobId}:${like.candidateId}`;
                    const existing = grouped.get(key);
                    if (existing) {
                        existing.recruiterIds = [...(existing.recruiterIds ?? []), like.recruiterId];
                        return;
                    }

                    grouped.set(key, {
                        jobId: like.jobId,
                        candidateId: like.candidateId,
                        companyId,
                        recruiterIds: [like.recruiterId],
                        createdAt: like.createdAt,
                        candidateName: candidate.name,
                        candidateEmail: candidate.email,
                        candidateDescription: candidate.description,
                        candidateSkills: candidate.skills ?? []
                    });
                });

            return [...grouped.values()];
        });
    }

    inviteRecruiter(companyId: string, request: CompanyInvitationRequest): Promise<CompanyRecruiterInvitation> {
        return this.run(() => {
            const email = request.recruiterEmail.trim().toLowerCase();
            const jobId = request.jobId.trim();
            if (!companyId) this.fail(401, "Empresa não autenticada.");
            if (!EMAIL_PATTERN.test(email) || !jobId) this.fail(400, "Dados do convite inválidos.");
            if (!StorageService.getJobs().some((job) => job.name === jobId)) this.fail(404, "Vaga não encontrada.");

            const recruiter = RecruiterLocalStore.getRecruiters()
                .find((item) => item.email === email && item.status === "ACTIVE");
            if (!recruiter) this.fail(404, "Recrutador não encontrado.");

            const invitations = RecruiterLocalStore.getInvitations();
            const duplicate = invitations.some((item) => item.recruiterId === recruiter!.userId
                && item.jobId === jobId && (item.status === "PENDING" || item.status === "ACCEPTED"));
            if (duplicate) this.fail(409, "Convite ativo já existe.");

            const invitation: StoredInvitation = {
                id: RecruiterLocalStore.newId(),
                recruiterId: recruiter!.userId,
                companyId,
                jobId,
                status: "PENDING",
                createdAt: new Date().toISOString(),
                respondedAt: null,
                revokedAt: null
            };
            RecruiterLocalStore.saveInvitations([...invitations, invitation]);
            return this.toCompanyInvitation(invitation);
        });
    }

    getCompanyInvitations(companyId: string): Promise<CompanyRecruiterInvitation[]> {
        return this.run(() => {
            if (!companyId) this.fail(401, "Empresa não autenticada.");
            return RecruiterLocalStore.getInvitations()
                .filter((item) => item.companyId === companyId)
                .map((item) => this.toCompanyInvitation(item));
        });
    }

    getAvailableRecruiters(): Promise<AvailableRecruiter[]> {
        return this.run(() => RecruiterLocalStore.getRecruiters()
            .filter((recruiter) => recruiter.status === "ACTIVE")
            .map((recruiter) => ({
                email: recruiter.email,
                recruitmentArea: recruiter.recruitmentArea
            })));
    }

    cancelInvitation(companyId: string, invitationId: string): Promise<CompanyRecruiterInvitation> {
        return this.changeCompanyInvitation(companyId, invitationId, "PENDING", "CANCELLED");
    }

    revokeInvitation(companyId: string, invitationId: string): Promise<CompanyRecruiterInvitation> {
        return this.changeCompanyInvitation(companyId, invitationId, "ACCEPTED", "REVOKED");
    }

    private respondToInvitation(invitationId: string, target: "ACCEPTED" | "DECLINED"): Promise<RecruiterInvitation> {
        return this.runAsRecruiter((recruiter) => {
            const invitations = RecruiterLocalStore.getInvitations();
            const invitation = invitations.find((item) => item.id === invitationId);
            if (!invitation || invitation.recruiterId !== recruiter.userId) this.fail(403, "Convite não disponível.");
            if (invitation!.status !== "PENDING") this.fail(409, "O convite não está mais pendente.");

            invitation!.status = target;
            invitation!.respondedAt = new Date().toISOString();
            RecruiterLocalStore.saveInvitations(invitations);

            const job = StorageService.getJobs().find((item) => item.name === invitation!.jobId);
            return {
                id: invitation!.id,
                jobId: invitation!.jobId,
                jobTitle: job?.name ?? invitation!.jobId,
                jobDescription: job?.description ?? "",
                jobLocation: job?.location ?? "",
                jobSkills: job?.skills ?? [],
                status: invitation!.status,
                createdAt: invitation!.createdAt
            };
        });
    }

    private changeCompanyInvitation(
        companyId: string,
        invitationId: string,
        from: InvitationStatus,
        to: InvitationStatus
    ): Promise<CompanyRecruiterInvitation> {
        return this.run(() => {
            if (!companyId) this.fail(401, "Empresa não autenticada.");
            const invitations = RecruiterLocalStore.getInvitations();
            const invitation = invitations.find((item) => item.id === invitationId);
            if (!invitation || invitation.companyId !== companyId) this.fail(403, "Convite não disponível.");
            if (invitation!.status !== from) this.fail(409, "Estado do convite não permite a operação.");

            invitation!.status = to;
            if (to === "REVOKED") invitation!.revokedAt = new Date().toISOString();
            RecruiterLocalStore.saveInvitations(invitations);
            return this.toCompanyInvitation(invitation!);
        });
    }

    private setLike(jobId: string, candidateId: string, active: boolean): Promise<RecruiterLike> {
        return this.runAsRecruiter((recruiter) => {
            this.requireAcceptedJob(recruiter.userId, jobId);
            const exists = StorageService.getCandidates()
                .some((candidate) => this.opaqueCandidateId(candidate.cpf) === candidateId);
            if (!exists) this.fail(404, "Candidato não encontrado.");

            const likes = RecruiterLocalStore.getLikes();
            let like = likes.find((item) => item.recruiterId === recruiter.userId
                && item.jobId === jobId && item.candidateId === candidateId);
            if (!like) {
                like = {
                    recruiterId: recruiter.userId, jobId, candidateId, status: "REMOVED",
                    createdAt: new Date().toISOString(), removedAt: null
                };
                likes.push(like);
            }
            // Idempotente: repetir a mesma ação não altera o estado.
            if (active && like.status !== "ACTIVE") {
                like.status = "ACTIVE";
                like.removedAt = null;
            } else if (!active && like.status === "ACTIVE") {
                like.status = "REMOVED";
                like.removedAt = new Date().toISOString();
            }
            RecruiterLocalStore.saveLikes(likes);
            return { recruiterId: like.recruiterId, jobId, candidateId, status: like.status, removedAt: like.removedAt };
        });
    }

    private acceptedJobIds(recruiterId: string): string[] {
        return RecruiterLocalStore.getInvitations()
            .filter((item) => item.recruiterId === recruiterId && item.status === "ACCEPTED")
            .map((item) => item.jobId);
    }

    private requireAcceptedJob(recruiterId: string, jobId: string): void {
        if (!this.acceptedJobIds(recruiterId).includes(jobId)) this.fail(403, "Recrutador não autorizado nesta vaga.");
    }

    private toCompanyInvitation(item: StoredInvitation): CompanyRecruiterInvitation {
        const recruiter = RecruiterLocalStore.getRecruiters().find((candidate) => candidate.userId === item.recruiterId);
        return {
            id: item.id,
            jobId: item.jobId,
            status: item.status,
            createdAt: item.createdAt,
            respondedAt: item.respondedAt,
            revokedAt: item.revokedAt,
            recruiterEmail: recruiter?.email ?? "",
            recruiterArea: recruiter?.recruitmentArea ?? "",
            jobTitle: item.jobId
        };
    }

    private toPublicRecruiter(stored: StoredRecruiter): Recruiter {
        return {
            userId: stored.userId,
            name: stored.name,
            email: stored.email,
            recruitmentAreaId: stored.recruitmentAreaId,
            recruitmentArea: stored.recruitmentArea,
            description: stored.description,
            status: stored.status
        };
    }

    /** Identificador opaco e estável (hash) para que o CPF nunca saia do armazenamento. */
    private opaqueCandidateId(cpf: string): string {
        let hash = 2166136261;
        for (const char of cpf) {
            hash ^= char.charCodeAt(0);
            hash = Math.imul(hash, 16777619);
        }
        return `c${(hash >>> 0).toString(16)}`;
    }

    private isAdult(birthDate: string): boolean {
        const birth = new Date(birthDate);
        if (Number.isNaN(birth.getTime())) return false;
        const limit = new Date();
        limit.setFullYear(limit.getFullYear() - 18);
        return birth <= limit;
    }

    private fail(status: number, message: string): never {
        throw new RecruiterHttpError(status, message);
    }

    private async run<T>(action: () => T): Promise<T> {
        return action();
    }

    /** Exige sessão ativa de recrutador; limpa o cache de convites em 401/403. */
    private async runAsRecruiter<T>(action: (recruiter: StoredRecruiter) => T): Promise<T> {
        try {
            const session = StorageService.getRecruiterSession();
            if (!session) this.fail(401, "Sessão de recrutador necessária.");
            const recruiter = RecruiterLocalStore.getRecruiters()
                .find((item) => item.userId === session!.userId && item.status === "ACTIVE");
            if (!recruiter) this.fail(403, "Conta de recrutador indisponível.");
            return action(recruiter!);
        } catch (error) {
            if (error instanceof RecruiterHttpError && (error.status === 401 || error.status === 403)) {
                StorageService.clearRecruiterInvitationCache();
            }
            throw error;
        }
    }
}

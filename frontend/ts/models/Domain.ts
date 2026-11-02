export interface User {
    name: string;
    email: string;
    password?: string;
    confirmPassword?: string;
    description: string;
    localization: string;
    skills?: string[];
}

export interface Company extends User {
    cnpj: string;
    likedCandidates: string[];
}

export interface Candidate extends User {
    cpf: string;
    age: number;
    likedJobs: string[];
}

export interface Job {
    name: string;
    description: string;
    salary: number;
    location: string;
    skills?: string[];
    jobType?: string;
}

export interface Match {
    cnpj: string;
    cpf: string;
    jobId?: string;
}

export type RecruiterStatus = "ACTIVE" | "INACTIVE";
export type InvitationStatus = "PENDING" | "ACCEPTED" | "DECLINED" | "CANCELLED" | "REVOKED";

export interface Recruiter {
    userId: string;
    name: string;
    email: string;
    recruitmentAreaId: string;
    recruitmentArea: string;
    description: string;
    status: RecruiterStatus;
}

export interface RecruiterSession {
    userId: string;
    name: string;
    email: string;
    recruitmentAreaId: string;
    recruitmentArea: string;
    description: string;
    status: RecruiterStatus;
}

export interface RecruiterInvitation {
    id: string;
    jobId: string;
    jobTitle: string;
    jobDescription: string;
    jobLocation: string;
    jobSkills: string[];
    status: InvitationStatus;
    createdAt: string;
}

export interface SkillReference {
    id: string;
    name: string;
}

/** Vaga com vínculo ACCEPTED, sem dados da empresa (projeção anônima). */
export interface AssignedJob {
    id: string;
    name: string;
    description: string;
    skills: SkillReference[];
    city?: string | null;
    state?: string | null;
    country?: string | null;
}

/**
 * Projeção anônima pré-match: somente descrição genérica e competências.
 * `candidateId` é um identificador opaco, usado apenas como alvo da curtida,
 * nunca exibido; quando ausente, a curtida não está disponível.
 */
export interface AnonymousCandidate {
    candidateId?: string;
    description: string;
    skills: SkillReference[];
}

export type RecruiterLikeStatus = "ACTIVE" | "REMOVED";

export interface RecruiterLike {
    recruiterId: string;
    jobId: string;
    candidateId: string;
    status: RecruiterLikeStatus;
    removedAt?: string | null;
}

export interface RecruiterMatch {
    jobId: string;
    candidateId: string;
    companyId?: string;
    createdAt: string;
    candidateName?: string;
    candidateEmail?: string;
    candidateDescription?: string;
}

export interface RecruiterRegistrationRequest {
    name: string;
    recruitmentAreaId: string;
    description: string;
    email: string;
    cpf: string;
    birthDate: string;
    password: string;
}

export interface RecruiterLoginRequest {
    email: string;
    password: string;
}

export interface RecruiterAuthResponse {
    recruiter: Recruiter;
    token?: string;
}

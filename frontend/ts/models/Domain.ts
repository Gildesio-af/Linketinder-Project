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

export interface AssignedJob {
    jobId: string;
    title: string;
    description: string;
    location: string;
    skills: string[];
}

export interface AnonymousCandidate {
    candidateId: string;
    description: string;
    experience?: string;
    skills: string[];
}

export interface RecruiterLike {
    jobId: string;
    candidateId: string;
    active: boolean;
    createdAt: string;
}

export interface RecruiterMatch {
    jobId: string;
    candidateId: string;
    companyId: string;
    candidateName?: string;
    candidateEmail?: string;
    createdAt: string;
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

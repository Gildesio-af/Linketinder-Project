import type { InvitationStatus, RecruiterLikeStatus, RecruiterStatus } from "../../models/Domain.js";

/** Áreas de recrutamento fixas do modo local (equivalente ao seed TI/RH/Marketing). */
export const RECRUITMENT_AREAS: ReadonlyArray<{ id: string; name: string }> = [
    { id: "746f34a8-0602-4b86-8426-eac894be1f03", name: "TI" },
    { id: "a62f518f-e4bb-49c4-a0c2-740ba38c328f", name: "RH" },
    { id: "0e62da83-e188-4e7b-b5ae-be4a9a2d1f93", name: "Marketing" }
];

export interface StoredRecruiter {
    userId: string;
    name: string;
    email: string;
    recruitmentAreaId: string;
    recruitmentArea: string;
    description: string;
    status: RecruiterStatus;
    cpf: string;
    birthDate: string;
    password: string;
}

export interface StoredInvitation {
    id: string;
    recruiterId: string;
    /** Identificador local da empresa (CNPJ); nunca é enviado ao recrutador. */
    companyId: string;
    jobId: string;
    status: InvitationStatus;
    createdAt: string;
    respondedAt: string | null;
    revokedAt: string | null;
}

export interface StoredLike {
    recruiterId: string;
    jobId: string;
    candidateId: string;
    status: RecruiterLikeStatus;
    createdAt: string;
    removedAt: string | null;
}

const KEYS = {
    RECRUITERS: "lt_recruiters",
    INVITATIONS: "lt_recruiter_invitation_records",
    LIKES: "lt_recruiter_likes"
} as const;

/** Persistência local (localStorage) das entidades de recrutador. */
export class RecruiterLocalStore {
    static getRecruiters(): StoredRecruiter[] {
        return this.read<StoredRecruiter>(KEYS.RECRUITERS);
    }

    static saveRecruiters(recruiters: StoredRecruiter[]): void {
        this.write(KEYS.RECRUITERS, recruiters);
    }

    static getInvitations(): StoredInvitation[] {
        return this.read<StoredInvitation>(KEYS.INVITATIONS);
    }

    static saveInvitations(invitations: StoredInvitation[]): void {
        this.write(KEYS.INVITATIONS, invitations);
    }

    static getLikes(): StoredLike[] {
        return this.read<StoredLike>(KEYS.LIKES);
    }

    static saveLikes(likes: StoredLike[]): void {
        this.write(KEYS.LIKES, likes);
    }

    static newId(): string {
        return crypto.randomUUID();
    }

    private static read<T>(key: string): T[] {
        const data = localStorage.getItem(key);
        if (!data) return [];
        try {
            const parsed: unknown = JSON.parse(data);
            return Array.isArray(parsed) ? parsed as T[] : [];
        } catch {
            return [];
        }
    }

    private static write<T>(key: string, values: T[]): void {
        localStorage.setItem(key, JSON.stringify(values));
    }
}

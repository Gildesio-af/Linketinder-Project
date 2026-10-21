import type {
    AnonymousCandidate,
    AssignedJob,
    InvitationStatus,
    RecruiterAuthResponse,
    RecruiterInvitation,
    RecruiterLike,
    RecruiterLoginRequest,
    RecruiterMatch,
    RecruiterRegistrationRequest
} from "../../models/Domain.js";
import { StorageService } from "../storage/StorageService.js";

export class RecruiterHttpError extends Error {
    readonly status: number;
    readonly responseBody: string;

    constructor(status: number, responseBody: string) {
        super(`Recruiter request failed with HTTP ${status}.`);
        this.name = "RecruiterHttpError";
        this.status = status;
        this.responseBody = responseBody;
    }
}

type FetchFunction = (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>;

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

type ActorRole = "RECRUITER" | "COMPANY";

export class RecruiterService {
    private readonly baseUrl: string;
    private readonly fetchFunction: FetchFunction;

    constructor(baseUrl = "/api", fetchFunction: FetchFunction = fetch) {
        this.baseUrl = baseUrl.replace(/\/$/, "");
        this.fetchFunction = fetchFunction;
    }

    register(request: RecruiterRegistrationRequest): Promise<RecruiterAuthResponse> {
        return this.request<RecruiterAuthResponse>("/recruiters", {
            method: "POST",
            body: request
        });
    }

    login(request: RecruiterLoginRequest): Promise<RecruiterAuthResponse> {
        return this.request<RecruiterAuthResponse>("/recruiters/login", {
            method: "POST",
            body: request
        });
    }

    getInvitations(): Promise<RecruiterInvitation[]> {
        return this.request<RecruiterInvitation[]>("/recruiter-invitations/recruiter");
    }

    acceptInvitation(invitationId: string): Promise<RecruiterInvitation> {
        return this.request<RecruiterInvitation>(`/recruiter-invitations/${encodeURIComponent(invitationId)}/accept`, {
            method: "POST"
        });
    }

    declineInvitation(invitationId: string): Promise<RecruiterInvitation> {
        return this.request<RecruiterInvitation>(`/recruiter-invitations/${encodeURIComponent(invitationId)}/decline`, {
            method: "POST"
        });
    }

    getAssignedJobs(): Promise<AssignedJob[]> {
        return this.request<AssignedJob[]>("/recruiters/jobs");
    }

    getAnonymousCandidates(jobId: string): Promise<AnonymousCandidate[]> {
        return this.request<AnonymousCandidate[]>(
            `/recruiters/jobs/${encodeURIComponent(jobId)}/candidates`
        );
    }

    likeCandidate(jobId: string, candidateId: string): Promise<RecruiterLike> {
        return this.request<RecruiterLike>("/recruiters/likes", {
            method: "POST",
            body: { jobId, candidateId }
        });
    }

    unlikeCandidate(jobId: string, candidateId: string): Promise<void> {
        return this.request<void>(
            `/recruiters/likes/${encodeURIComponent(jobId)}/${encodeURIComponent(candidateId)}`,
            { method: "DELETE" }
        );
    }

    getMatches(): Promise<RecruiterMatch[]> {
        return this.request<RecruiterMatch[]>("/recruiters/matches");
    }

    inviteRecruiter(companyId: string, request: CompanyInvitationRequest): Promise<CompanyRecruiterInvitation> {
        const body: CompanyInvitationRequest = {
            recruiterEmail: request.recruiterEmail.trim().toLowerCase(),
            jobId: request.jobId.trim()
        };
        return this.request<CompanyRecruiterInvitation>("/recruiter-invitations", {
            method: "POST",
            body,
            actor: { id: companyId, role: "COMPANY" }
        });
    }

    getCompanyInvitations(companyId: string): Promise<CompanyRecruiterInvitation[]> {
        return this.request<CompanyRecruiterInvitation[]>("/recruiter-invitations/company", {
            actor: { id: companyId, role: "COMPANY" }
        });
    }

    cancelInvitation(companyId: string, invitationId: string): Promise<CompanyRecruiterInvitation> {
        return this.request<CompanyRecruiterInvitation>(
            `/recruiter-invitations/${encodeURIComponent(invitationId)}/cancel`,
            { method: "POST", actor: { id: companyId, role: "COMPANY" } }
        );
    }

    revokeInvitation(companyId: string, invitationId: string): Promise<CompanyRecruiterInvitation> {
        return this.request<CompanyRecruiterInvitation>(
            `/recruiter-invitations/${encodeURIComponent(invitationId)}/revoke`,
            { method: "POST", actor: { id: companyId, role: "COMPANY" } }
        );
    }

    private async request<T>(
        path: string,
        options: { method?: string; body?: object; actor?: { id: string; role: ActorRole } } = {}
    ): Promise<T> {
        const requestInit: RequestInit = {
            method: options.method ?? "GET",
            headers: this.authenticationHeaders(options.body !== undefined, options.actor)
        };

        if (options.body) {
            requestInit.body = JSON.stringify(options.body);
        }

        const response = await this.fetchFunction(`${this.baseUrl}${path}`, requestInit);
        const responseBody = await response.text();

        if (!response.ok) {
            if ((response.status === 401 || response.status === 403) && options.actor?.role !== "COMPANY") {
                StorageService.clearRecruiterInvitationCache();
            }
            throw new RecruiterHttpError(response.status, responseBody);
        }

        if (response.status === 204 || responseBody.trim() === "") {
            return undefined as T;
        }

        try {
            return JSON.parse(responseBody) as T;
        } catch {
            throw new Error("Recruiter request returned an invalid JSON response.");
        }
    }

    private authenticationHeaders(
        hasBody: boolean,
        actor?: { id: string; role: ActorRole }
    ): Record<string, string> {
        const headers: Record<string, string> = {};
        if (hasBody) headers["Content-Type"] = "application/json";
        if (actor) {
            headers["X-User-Id"] = actor.id;
            headers["X-User-Role"] = actor.role;
            return headers;
        }
        const session = StorageService.getRecruiterSession();
        if (session) {
            headers["X-User-Id"] = session.userId;
            headers["X-User-Role"] = "RECRUITER";
        }
        return headers;
    }
}

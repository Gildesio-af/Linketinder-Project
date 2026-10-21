import type {
    AnonymousCandidate,
    AssignedJob,
    RecruiterAuthResponse,
    RecruiterInvitation,
    RecruiterLike,
    RecruiterLoginRequest,
    RecruiterMatch,
    RecruiterRegistrationRequest
} from "../../models/Domain.js";

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
        return this.request<RecruiterInvitation[]>("/recruiters/invitations");
    }

    acceptInvitation(invitationId: string): Promise<RecruiterInvitation> {
        return this.request<RecruiterInvitation>(`/recruiters/invitations/${encodeURIComponent(invitationId)}/accept`, {
            method: "POST"
        });
    }

    declineInvitation(invitationId: string): Promise<RecruiterInvitation> {
        return this.request<RecruiterInvitation>(`/recruiters/invitations/${encodeURIComponent(invitationId)}/decline`, {
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

    private async request<T>(
        path: string,
        options: { method?: string; body?: object } = {}
    ): Promise<T> {
        const requestInit: RequestInit = {
            method: options.method ?? "GET",
        };

        if (options.body) {
            requestInit.headers = { "Content-Type": "application/json" };
            requestInit.body = JSON.stringify(options.body);
        }

        const response = await this.fetchFunction(`${this.baseUrl}${path}`, requestInit);
        const responseBody = await response.text();

        if (!response.ok) {
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
}

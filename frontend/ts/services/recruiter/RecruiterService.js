import { StorageService } from "../storage/StorageService.js";
export class RecruiterHttpError extends Error {
    status;
    responseBody;
    constructor(status, responseBody) {
        super(`Recruiter request failed with HTTP ${status}.`);
        this.name = "RecruiterHttpError";
        this.status = status;
        this.responseBody = responseBody;
    }
}
export class RecruiterService {
    baseUrl;
    fetchFunction;
    constructor(baseUrl = "/api", fetchFunction = fetch) {
        this.baseUrl = baseUrl.replace(/\/$/, "");
        this.fetchFunction = fetchFunction;
    }
    register(request) {
        return this.request("/recruiters", {
            method: "POST",
            body: request
        });
    }
    login(request) {
        return this.request("/recruiters/login", {
            method: "POST",
            body: request
        });
    }
    getInvitations() {
        return this.request("/recruiter-invitations/recruiter");
    }
    acceptInvitation(invitationId) {
        return this.request(`/recruiter-invitations/${encodeURIComponent(invitationId)}/accept`, {
            method: "POST"
        });
    }
    declineInvitation(invitationId) {
        return this.request(`/recruiter-invitations/${encodeURIComponent(invitationId)}/decline`, {
            method: "POST"
        });
    }
    getAssignedJobs() {
        return this.request("/recruiters/jobs");
    }
    getAnonymousCandidates(jobId) {
        return this.request(`/recruiters/jobs/${encodeURIComponent(jobId)}/candidates`);
    }
    likeCandidate(jobId, candidateId) {
        return this.request("/recruiters/likes", {
            method: "POST",
            body: { jobId, candidateId }
        });
    }
    unlikeCandidate(jobId, candidateId) {
        return this.request(`/recruiters/likes/${encodeURIComponent(jobId)}/${encodeURIComponent(candidateId)}`, { method: "DELETE" });
    }
    getMatches() {
        return this.request("/recruiters/matches");
    }
    inviteRecruiter(companyId, request) {
        const body = {
            recruiterEmail: request.recruiterEmail.trim().toLowerCase(),
            jobId: request.jobId.trim()
        };
        return this.request("/recruiter-invitations", {
            method: "POST",
            body,
            actor: { id: companyId, role: "COMPANY" }
        });
    }
    getCompanyInvitations(companyId) {
        return this.request("/recruiter-invitations/company", {
            actor: { id: companyId, role: "COMPANY" }
        });
    }
    cancelInvitation(companyId, invitationId) {
        return this.request(`/recruiter-invitations/${encodeURIComponent(invitationId)}/cancel`, { method: "POST", actor: { id: companyId, role: "COMPANY" } });
    }
    revokeInvitation(companyId, invitationId) {
        return this.request(`/recruiter-invitations/${encodeURIComponent(invitationId)}/revoke`, { method: "POST", actor: { id: companyId, role: "COMPANY" } });
    }
    async request(path, options = {}) {
        const requestInit = {
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
            return undefined;
        }
        try {
            return JSON.parse(responseBody);
        }
        catch {
            throw new Error("Recruiter request returned an invalid JSON response.");
        }
    }
    authenticationHeaders(hasBody, actor) {
        const headers = {};
        if (hasBody)
            headers["Content-Type"] = "application/json";
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
//# sourceMappingURL=RecruiterService.js.map
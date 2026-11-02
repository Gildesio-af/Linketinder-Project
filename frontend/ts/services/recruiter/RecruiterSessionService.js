import { StorageService } from "../storage/StorageService.js";
export class RecruiterSessionService {
    static getCurrent() {
        return StorageService.getRecruiterSession();
    }
    static start(session) {
        if (session.status !== "ACTIVE") {
            throw new Error("Inactive recruiter accounts cannot start a session.");
        }
        StorageService.setRecruiterSession(session);
    }
    static requireCurrent() {
        const session = this.getCurrent();
        if (!session) {
            throw new Error("Recruiter session is required.");
        }
        return session;
    }
    static clear() {
        StorageService.deleteRecruiterSession();
    }
}
//# sourceMappingURL=RecruiterSessionService.js.map
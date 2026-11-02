import { StorageService } from "../storage/StorageService.js";
import type { RecruiterSession } from "../../models/Domain.js";

export class RecruiterSessionService {
    static getCurrent(): RecruiterSession | null {
        return StorageService.getRecruiterSession();
    }

    static start(session: RecruiterSession): void {
        if (session.status !== "ACTIVE") {
            throw new Error("Inactive recruiter accounts cannot start a session.");
        }

        StorageService.setRecruiterSession(session);
    }

    static requireCurrent(): RecruiterSession {
        const session = this.getCurrent();
        if (!session) {
            throw new Error("Recruiter session is required.");
        }

        return session;
    }

    static clear(): void {
        StorageService.deleteRecruiterSession();
    }
}

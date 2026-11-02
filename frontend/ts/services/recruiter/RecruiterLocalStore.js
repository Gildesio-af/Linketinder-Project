/** Áreas de recrutamento fixas do modo local (equivalente ao seed TI/RH/Marketing). */
export const RECRUITMENT_AREAS = [
    { id: "746f34a8-0602-4b86-8426-eac894be1f03", name: "TI" },
    { id: "a62f518f-e4bb-49c4-a0c2-740ba38c328f", name: "RH" },
    { id: "0e62da83-e188-4e7b-b5ae-be4a9a2d1f93", name: "Marketing" }
];
const KEYS = {
    RECRUITERS: "lt_recruiters",
    INVITATIONS: "lt_recruiter_invitation_records",
    LIKES: "lt_recruiter_likes"
};
/** Persistência local (localStorage) das entidades de recrutador. */
export class RecruiterLocalStore {
    static getRecruiters() {
        return this.read(KEYS.RECRUITERS);
    }
    static saveRecruiters(recruiters) {
        this.write(KEYS.RECRUITERS, recruiters);
    }
    static getInvitations() {
        return this.read(KEYS.INVITATIONS);
    }
    static saveInvitations(invitations) {
        this.write(KEYS.INVITATIONS, invitations);
    }
    static getLikes() {
        return this.read(KEYS.LIKES);
    }
    static saveLikes(likes) {
        this.write(KEYS.LIKES, likes);
    }
    static newId() {
        return crypto.randomUUID();
    }
    static read(key) {
        const data = localStorage.getItem(key);
        if (!data)
            return [];
        try {
            const parsed = JSON.parse(data);
            return Array.isArray(parsed) ? parsed : [];
        }
        catch {
            return [];
        }
    }
    static write(key, values) {
        localStorage.setItem(key, JSON.stringify(values));
    }
}
//# sourceMappingURL=RecruiterLocalStore.js.map
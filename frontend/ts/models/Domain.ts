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
    
    


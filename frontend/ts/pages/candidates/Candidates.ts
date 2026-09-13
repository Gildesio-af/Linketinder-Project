import { StorageService } from "../../services/storage/StorageService.js";
import type { Company, Candidate, Job } from "../../models/Domain.js";
import { MatchService } from "../../services/match/MatchService.js";

const profileBtn = document.getElementById("profile-btn") as HTMLButtonElement;
const btnUpdateData = document.getElementById("btn-update-data") as HTMLButtonElement;
const btnLogout = document.getElementById("company-logout") as HTMLButtonElement;
const btnDeleteCompany = document.getElementById("btn-delete-company") as HTMLButtonElement;

const btnCandidates = document.getElementById("candidates") as HTMLButtonElement;
const btnMyMatches = document.getElementById("my-matches-company") as HTMLButtonElement;

// Elements profile dropdown
const profileDropdown = document.getElementById("profile-dropdown") as HTMLDivElement;
const companyDpName = document.getElementById("company-dp-name") as HTMLSpanElement;
const companyDpCnpj = document.getElementById("company-dp-cnpj") as HTMLSpanElement;
const companyDpEmail = document.getElementById("company-dp-email") as HTMLSpanElement;
const companyDpLocation = document.getElementById("company-dp-location") as HTMLSpanElement;
const companyDpDescription = document.getElementById("company-dp-description") as HTMLSpanElement;
const companyDpSkills = document.getElementById("company-dp-skills") as HTMLUListElement;

// Elements update company modal
const updateModal = document.getElementById('company-update-modal') as HTMLDivElement;
const formUpdateCompany = document.getElementById('form-update-company') as HTMLFormElement;
const companyUpdateName = document.getElementById('company-update-name') as HTMLInputElement;
const companyUpdateCnpj = document.getElementById('company-update-cnpj') as HTMLInputElement;
const companyUpdateEmail = document.getElementById('company-update-email') as HTMLInputElement;
const companyUpdateLocation = document.getElementById('company-update-place') as HTMLInputElement;
const companyUpdateDescription = document.getElementById('company-update-description') as HTMLTextAreaElement;

const updateSkillsContainer = document.getElementById('update-skills-container') as HTMLDivElement;
const updateSkillsInput = document.getElementById('update-skills-input') as HTMLInputElement;
const addSkillBtn = document.getElementById('add-skill-btn') as HTMLButtonElement;
const closeModalBtn = document.querySelector('.close-modal') as HTMLSpanElement;

const candidatesPanel = document.getElementById('candidates-panel') as HTMLElement;

// Elements job create modal
const btnCreateJob = document.getElementById('job-form-btn') as HTMLButtonElement;
const jobCreateModal = document.getElementById('job-create-modal') as HTMLDivElement;
const formCreateJob = document.getElementById('form-create-job') as HTMLFormElement;
const jobCreateTitle = document.getElementById('job-create-title') as HTMLInputElement;
const jobCreateType = document.getElementById('job-create-type') as HTMLSelectElement;
const jobCreateLocation = document.getElementById('job-create-location') as HTMLInputElement;
const jobCreateDescription = document.getElementById('job-create-description') as HTMLTextAreaElement;
const jobCreateSkillsContainer = document.getElementById('job-create-skills-container') as HTMLDivElement;
const jobCreateSkillsInput = document.getElementById('job-create-skills-input') as HTMLInputElement;
const jobAddSkillBtn = document.getElementById('job-add-skill-btn') as HTMLButtonElement;
const closeJobModalBtn = document.querySelector('.close-modal-job') as HTMLSpanElement;

let temporarySkills: string[] = [];
let jobTemporarySkills: string[] = [];

function renderSkillsTags() {
    const tags = updateSkillsContainer.querySelectorAll('.skill-tag');
    tags.forEach(tag => tag.remove());

    temporarySkills.forEach(skill => {
        const span = document.createElement('span');
        span.className = 'skill-tag';
        span.innerHTML = `${skill} <button type="button" aria-label="Remover ${skill}">&times;</button>`;
        
        span.querySelector('button')?.addEventListener('click', () => {
            temporarySkills = temporarySkills.filter(s => s !== skill);
            renderSkillsTags();
        });

        updateSkillsContainer.insertBefore(span, updateSkillsInput);
    });
}

function renderJobSkillsTags() {
    const tags = jobCreateSkillsContainer.querySelectorAll('.skill-tag');
    tags.forEach(tag => tag.remove());

    jobTemporarySkills.forEach(skill => {
        const span = document.createElement('span');
        span.className = 'skill-tag';
        span.innerHTML = `${skill} <button type="button" aria-label="Remover ${skill}">&times;</button>`;
        
        span.querySelector('button')?.addEventListener('click', () => {
            jobTemporarySkills = jobTemporarySkills.filter(s => s !== skill);
            renderJobSkillsTags();
        });

        jobCreateSkillsContainer.insertBefore(span, jobCreateSkillsInput);
    });
}

profileBtn.addEventListener("click", () => {
    if (profileDropdown.classList.contains("show")) {
        profileDropdown.classList.remove("show");
        return;
    } else {
        profileDropdown.classList.add("show");
    }

    const currentUser: Company = StorageService.getCurrentUser() as Company;

    companyDpName.textContent = currentUser.name;
    companyDpCnpj.textContent = currentUser.cnpj;
    companyDpEmail.textContent = currentUser.email;
    companyDpLocation.textContent = currentUser.localization;
    companyDpDescription.textContent = currentUser.description;

    companyDpSkills.innerHTML = "";
    if (currentUser.skills && currentUser.skills.length > 0) {
        currentUser.skills.forEach(skill => {
            const li = document.createElement("li");
            li.textContent = skill;
            companyDpSkills.appendChild(li);
        });
    } else {
        const li = document.createElement("li");
        li.textContent = "Nenhuma competência cadastrada";
        companyDpSkills.appendChild(li);
    }
})

window.addEventListener("click", (event: Event) => {
    const target = event.target as HTMLElement;

    if (!target.closest(".profile-container") && !target.closest(".modal-content")) {
        if (profileDropdown.classList.contains("show")) {
            profileDropdown.classList.remove("show");
        }
    }
    
    if (target === updateModal) {
        updateModal.style.display = 'none';
    }
    
    if (target === jobCreateModal) {
        jobCreateModal.style.display = 'none';
    }
});

closeModalBtn.addEventListener('click', () => {
    updateModal.style.display = 'none';
});

btnCreateJob.addEventListener('click', () => {
    jobCreateModal.style.display = 'block';
});

closeJobModalBtn.addEventListener('click', () => {
    jobCreateModal.style.display = 'none';
});

addSkillBtn.addEventListener('click', () => {
    const skill = updateSkillsInput.value.trim();
    if (skill && !temporarySkills.includes(skill)) {
        temporarySkills.push(skill);
        renderSkillsTags();
        updateSkillsInput.value = '';
    }
});

updateSkillsInput.addEventListener('keypress', (event) => {
    if (event.key === 'Enter') {
        event.preventDefault();
        addSkillBtn.click();
    }
});

jobAddSkillBtn.addEventListener('click', () => {
    const skill = jobCreateSkillsInput.value.trim();
    if (skill && !jobTemporarySkills.includes(skill)) {
        jobTemporarySkills.push(skill);
        renderJobSkillsTags();
        jobCreateSkillsInput.value = '';
    }
});

jobCreateSkillsInput.addEventListener('keypress', (event) => {
    if (event.key === 'Enter') {
        event.preventDefault();
        jobAddSkillBtn.click();
    }
});

btnUpdateData.addEventListener('click', () => {
    const currentCmpany: Company = StorageService.getCurrentUser() as Company;
    updateModal.style.display = 'block';

    companyUpdateName.value = currentCmpany.name;
    companyUpdateCnpj.value = currentCmpany.cnpj;
    companyUpdateEmail.value = currentCmpany.email;
    companyUpdateLocation.value = currentCmpany.localization;
    companyUpdateDescription.value = currentCmpany.description;
    
    temporarySkills = [...(currentCmpany.skills || [])];
    renderSkillsTags();
});

formUpdateCompany.addEventListener('submit', (event) => {
    event.preventDefault();

    const currentCompany: Company = StorageService.getCurrentUser() as Company;

    const companyUpdated: Company = {
        name: companyUpdateName.value,
        cnpj: companyUpdateCnpj.value,
        email: companyUpdateEmail.value,
        localization: companyUpdateLocation.value,
        description: companyUpdateDescription.value,
        likedCandidates: currentCompany.likedCandidates,
        skills: temporarySkills
    };

    StorageService.updateCompany(companyUpdated);
    StorageService.setCurrentUser(companyUpdated);
    updateModal.style.display = "none";

    if (profileDropdown.classList.contains("show"))
        profileDropdown.classList.remove("show");

    alert("Dados atualizados com sucesso");
});

formCreateJob.addEventListener('submit', (event) => {
    event.preventDefault();

    const job: Job = {
        name: jobCreateTitle.value,
        description: jobCreateDescription.value,
        salary: 0, 
        location: jobCreateLocation.value,
        skills: jobTemporarySkills,
        jobType: jobCreateType.value
    };

    try {
        StorageService.saveJob(job);
        jobCreateModal.style.display = "none";
        formCreateJob.reset();
        jobTemporarySkills = [];
        renderJobSkillsTags();
        alert("Vaga cadastrada com sucesso!");
    } catch (e: any) {
        alert(e.message);
    }
});

btnLogout.addEventListener('click', () => {
    StorageService.deleteCurrentUser();
    window.location.replace("./login.html")
})

btnDeleteCompany.addEventListener('click', () => {
    const currentCompany: Company = StorageService.getCurrentUser() as Company;
    StorageService.deleteCompany(currentCompany.cnpj);
    StorageService.deleteCurrentUser();
    window.location.replace("./login.html")
});

export function renderCandidates() {
    if (!candidatesPanel) return;

    candidatesPanel.innerHTML = '';
    const currentUser: Company = StorageService.getCurrentUser() as Company;

    

    const candidates = StorageService.getCandidates();
    const filteredCandidates = candidates.filter(candidate => 
        (currentUser.skills || []).some(skill => (candidate.skills || []).includes(skill))
    )


    filteredCandidates.forEach((candidate, index) => {
        const card = document.createElement('div');
        card.className = 'candidate-card';

        const skillsHtml = (candidate.skills || [])
            .map(skill => `<li>${skill}</li>`)
            .join('');

        const anonimousNumber = String(index + 1).padStart(3, '0')

        card.innerHTML = `
            <div class="candidate-header">
                <div class="candidate-avatar">
                    <img src="../assets/user.svg" alt="Avatar do candidato">
                </div>
                <h2 class="candidate-name-font">Candidato #${anonimousNumber}</h2>
            </div>
            
            <div class="candidate-education">
                <h3 class="sub-title">Descrição</h3>
                <p class="description-sm">${candidate.description}</p>
            </div>

            <ul class="candidate-skills label-font-s">
                ${skillsHtml}
            </ul>

            <div class="candidate-actions">
                <button class="btn-interest">
                    <img src="../assets/heart-outline.svg" alt="Coração" width="24" height="24">
                    Demonstrar Interesse
                </button>
            </div>
        `;

        const btnInterest = card.querySelector(".btn-interest") as HTMLButtonElement;

        btnInterest.addEventListener('click', () => {
            MatchService.registerLikeCompany(candidate.cpf);

            btnInterest.innerHTML = `Interesse Enviado!`;
            btnInterest.disabled = true;
            btnInterest.style.opacity = '0.5';
        });

        candidatesPanel.appendChild(card);
    });
}

renderCandidates();

btnCandidates.addEventListener('click', () => {
    btnCandidates.className = 'nav-font-selected';
    btnMyMatches.className = 'nav-font';
    renderCandidates();
});

btnMyMatches.addEventListener('click', () => {
    btnMyMatches.className = 'nav-font-selected';
    btnCandidates.className = 'nav-font';
    renderMatches();
});

export function renderMatches() {
    if (!candidatesPanel) return;

    candidatesPanel.innerHTML = '';
    const currentUser: Company = StorageService.getCurrentUser() as Company;
    
    const matches = StorageService.getMatches().filter(m => m.cnpj === currentUser.cnpj);
    const candidates = StorageService.getCandidates();
    
    const matchedCandidates = candidates.filter(candidate => 
        matches.some(m => m.cpf === candidate.cpf)
    );

    if (matchedCandidates.length === 0) {
        candidatesPanel.innerHTML = '<p class="description-m" style="text-align: center; margin-top: 2rem;">Nenhum match encontrado.</p>';
        return;
    }

    matchedCandidates.forEach((candidate, index) => {
        const card = document.createElement('div');
        card.className = 'candidate-card';

        const skillsHtml = (candidate.skills || [])
            .map(skill => `<li>${skill}</li>`)
            .join('');

        const anonimousNumber = String(index + 1).padStart(3, '0');

        card.innerHTML = `
            <div class="candidate-header">
                <div class="candidate-avatar">
                    <img src="../assets/user.svg" alt="Avatar do candidato">
                </div>
                <h2 class="candidate-name-font">${candidate.name}</h2>
                <p class="description-sm">${candidate.email}</p>
            </div>
            
            <div class="candidate-education">
                <h3 class="sub-title">Descrição</h3>
                <p class="description-sm">${candidate.description}</p>
            </div>

            <ul class="candidate-skills label-font-s">
                ${skillsHtml}
            </ul>

            <div class="candidate-actions">
                <button class="btn-interest" disabled style="opacity: 0.5;">
                    <img src="../assets/heart.svg" alt="Coração" width="24" height="24">
                    Match!
                </button>
            </div>
        `;

        candidatesPanel.appendChild(card);
    });
}




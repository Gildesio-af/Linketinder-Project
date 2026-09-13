import { StorageService } from "../../services/storage/StorageService.js";
import type { Candidate, Company, Job } from "../../models/Domain.js";

const profileBtn = document.getElementById('profileBtn') as HTMLAnchorElement;
const profileDropdown = document.getElementById('profileDropdown') as HTMLDivElement;
const btnUpdateData = document.getElementById('btnUpdateData') as HTMLButtonElement;
const updateModal = document.getElementById('updateModal') as HTMLDivElement;
const closeModal = document.querySelector('.close-modal') as HTMLSpanElement;
const saveUpdateBtn = document.getElementById('saveUpdateBtn') as HTMLButtonElement;

const btnJobsAvailable = document.getElementById('jobs-available') as HTMLButtonElement;
const btnMyMatches = document.getElementById('my-matches-candidate') as HTMLButtonElement;
const btnLogout = document.getElementById('candidate-logout') as HTMLButtonElement;

const jobsContainer = document.getElementById('jobs-container') as HTMLDivElement;

const userDpName = document.getElementById('user-dp-name') as HTMLSpanElement;
const userDpAge = document.getElementById('user-dp-age') as HTMLSpanElement;
const userDpEmail = document.getElementById('user-dp-email') as HTMLSpanElement;
const userDpLocation = document.getElementById('user-dp-location') as HTMLSpanElement;
const userDpDescription = document.getElementById('user-dp-description') as HTMLSpanElement;
const userDpSkills = document.getElementById('user-dp-skills') as HTMLUListElement;

const updateName = document.getElementById('update-name') as HTMLInputElement;
const updateAge = document.getElementById('update-age') as HTMLInputElement;
const updateEmail = document.getElementById('update-email') as HTMLInputElement;
const updatePlace = document.getElementById('update-place') as HTMLInputElement;
const updateDescription = document.getElementById('update-description') as HTMLTextAreaElement;
const updateSkillsContainer = document.getElementById('update-skills-container') as HTMLDivElement;
const updateSkillsInput = document.getElementById('update-skills-input') as HTMLInputElement;

let temporarySkills: string[] = [];

function renderSkillsTags() {
    if (!updateSkillsContainer) return;
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

profileBtn?.addEventListener('click', function(event) {
    event.preventDefault();
    if (profileDropdown?.classList.contains('show')) {
        profileDropdown.classList.remove('show');
        return;
    } else {
        profileDropdown?.classList.add('show');
    }

    const currentUser: Candidate = StorageService.getCurrentUser() as Candidate;
    if (currentUser) {
        if (userDpName) userDpName.textContent = currentUser.name;
        if (userDpAge) userDpAge.textContent = currentUser.age?.toString() || '';
        if (userDpEmail) userDpEmail.textContent = currentUser.email;
        if (userDpLocation) userDpLocation.textContent = currentUser.localization;
        if (userDpDescription) userDpDescription.textContent = currentUser.description;

        if (userDpSkills) {
            userDpSkills.innerHTML = "";
            if (currentUser.skills && currentUser.skills.length > 0) {
                currentUser.skills.forEach(skill => {
                    const li = document.createElement("li");
                    li.textContent = skill;
                    userDpSkills.appendChild(li);
                });
            } else {
                const li = document.createElement("li");
                li.textContent = "Nenhuma competência";
                userDpSkills.appendChild(li);
            }
        }
    }
});

btnUpdateData?.addEventListener('click', function(event) {
    event.preventDefault();
    profileDropdown?.classList.remove('show');
    
    const currentUser: Candidate = StorageService.getCurrentUser() as Candidate;
    if (currentUser) {
        if (updateName) updateName.value = currentUser.name;
        if (updateAge) updateAge.value = currentUser.age?.toString() || '';
        if (updateEmail) updateEmail.value = currentUser.email;
        if (updatePlace) updatePlace.value = currentUser.localization;
        if (updateDescription) updateDescription.value = currentUser.description;
        
        temporarySkills = [...(currentUser.skills || [])];
        renderSkillsTags();
    }

    if (updateModal) updateModal.style.display = 'block';
});

closeModal?.addEventListener('click', function() {
    if (updateModal) updateModal.style.display = 'none';
});

saveUpdateBtn?.addEventListener('click', function(event) {
    event.preventDefault();
    if (updateModal) updateModal.style.display = 'none';
});

window.addEventListener('click', function(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('.profile-container') && !target.closest('.modal-content')) {
        if (profileDropdown?.classList.contains('show')) {
            profileDropdown.classList.remove('show');
        }
    }
    if (target === updateModal) {
        updateModal.style.display = 'none';
    }
});

btnLogout?.addEventListener('click', () => {
    StorageService.deleteCurrentUser();
    window.location.replace("./login.html");
});

let hardcodedJobsHtml = '';
if (jobsContainer) {
    hardcodedJobsHtml = jobsContainer.innerHTML;
}

export function renderJobs() {
    if (!jobsContainer) return;
    
    const jobs = StorageService.getJobs();

    if (jobs.length === 0) {
        jobsContainer.innerHTML = hardcodedJobsHtml;
        return;
    }

    jobsContainer.innerHTML = '';

    jobs.forEach(job => {
        const card = document.createElement('div');
        card.className = 'job-card';

        const skillsHtml = (job.skills || [])
            .map(skill => `<li>${skill}</li>`)
            .join('');

        card.innerHTML = `
            <div class="company-info">
                <div>
                    <div>
                        <img class="img-company" src="../assets/company-green.svg" alt="Logo">
                    </div>
                    <div>
                        <h3 class="company-font">empresa confidencial</h3>
                        <p class="location-font">
                            <img src="../assets/location.svg" alt="Ícone de localização">
                            ${job.jobType}, ${job.location}
                        </p>
                    </div>
                </div>
            </div>
            <div>
                <h2 class="job-title-font">${job.name}</h2>
                <p class="description-sm">${job.description}</p>
                <ul class="label-font-s">
                    ${skillsHtml}
                </ul>
            </div>
            <div>
                <button class="label-font-m btn-like">
                    <img src="../assets/heart.svg" alt="Ícone branco de coração">
                    Dar Like
                </button>
            </div>
        `;
        jobsContainer.appendChild(card);
    });
}

export function renderMatches() {
    if (!jobsContainer) return;
    jobsContainer.innerHTML = '';

    const currentUser: Candidate = StorageService.getCurrentUser() as Candidate;
    const matches = StorageService.getMatches().filter(m => m.cpf === currentUser?.cpf);
    const companies = StorageService.getCompanies();

    const matchedCompanies = companies.filter(company => 
        matches.some(m => m.cnpj === company.cnpj)
    );

    if (matchedCompanies.length === 0) {
        jobsContainer.innerHTML = '<p class="description-m" style="text-align: center; margin-top: 2rem;">Nenhum match encontrado.</p>';
        return;
    }

    matchedCompanies.forEach((company, index) => {
        const card = document.createElement('div');
        card.className = 'job-card';

        const skillsHtml = (company.skills || [])
            .map(skill => `<li>${skill}</li>`)
            .join('');

        card.innerHTML = `
            <div class="company-info">
                <div>
                    <div>
                        <img class="img-company" src="../assets/company-green.svg" alt="Logo">
                    </div>
                    <div>
                        <h3 class="company-font">${company.name}</h3>
                        <p class="location-font">
                            <img src="../assets/location.svg" alt="Ícone de localização">
                            ${company.localization}
                        </p>
                    </div>
                </div>
            </div>
            <div>
                <h2 class="job-title-font">Empresa Parceira</h2>
                <p class="description-sm">${company.description}</p>
                <p class="description-sm" style="margin-top: 10px;"><strong>Contato:</strong> ${company.email}</p>
                <ul class="label-font-s" style="margin-top: 10px;">
                    ${skillsHtml}
                </ul>
            </div>
            <div>
                <button class="label-font-m btn-like" disabled style="opacity: 0.5;">
                    <img src="../assets/heart.svg" alt="Ícone branco de coração">
                    Match!
                </button>
            </div>
        `;
        jobsContainer.appendChild(card);
    });
}

btnJobsAvailable?.addEventListener('click', () => {
    btnJobsAvailable.className = 'nav-font-selected';
    if(btnMyMatches) btnMyMatches.className = 'nav-font';
    renderJobs();
});

btnMyMatches?.addEventListener('click', () => {
    if(btnMyMatches) btnMyMatches.className = 'nav-font-selected';
    if(btnJobsAvailable) btnJobsAvailable.className = 'nav-font';
    renderMatches();
});

renderJobs();

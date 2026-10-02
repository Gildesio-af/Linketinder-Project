import { StorageService } from "../../services/storage/StorageService.js";
import type { Candidate, Company, Job } from "../../models/Domain.js";
import { MatchService } from "../../services/match/MatchService.js";

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
const btnFilterAllJobs = document.getElementById('filter-all-jobs') as HTMLButtonElement;
const btnFilterRecommendedJobs = document.getElementById('filter-recommended-jobs') as HTMLButtonElement;

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

let currentJobFilter: 'all' | 'recommended' = 'all';
let temporarySkills: string[] = [];

const hardcodedJobsHtml = jobsContainer ? jobsContainer.innerHTML : '';

function setupSkillInput(inputEl: HTMLInputElement, containerEl: HTMLDivElement, skillsArray: string[]) {
    const renderTags = () => {
        if (!containerEl) return;
        containerEl.querySelectorAll('.skill-tag').forEach(tag => tag.remove());

        skillsArray.forEach(skill => {
            const span = document.createElement('span');
            span.className = 'skill-tag';
            span.innerHTML = `${skill} <button type="button" aria-label="Remover ${skill}">&times;</button>`;
            
            span.querySelector('button')?.addEventListener('click', () => {
                const index = skillsArray.indexOf(skill);
                if (index > -1) skillsArray.splice(index, 1);
                renderTags();
            });

            containerEl.insertBefore(span, inputEl);
        });
    };

    inputEl?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            const skill = inputEl.value.trim();
            if (skill && !skillsArray.includes(skill)) {
                skillsArray.push(skill);
                renderTags();
                inputEl.value = '';
            }
        }
    });

    return renderTags;
}

const renderSkillsTags = setupSkillInput(updateSkillsInput, updateSkillsContainer, temporarySkills);

type CardOptions = {
    title: string;
    subtitle: string;
    location: string;
    description: string;
    skills: string[];
    contactInfo?: string;
    isMatch: boolean;
    onLike?: (btn: HTMLButtonElement) => void;
};

function createCard(options: CardOptions): HTMLDivElement {
    const card = document.createElement('div');
    card.className = 'job-card';

    const skillsHtml = options.skills.map(skill => `<li>${skill}</li>`).join('');
    
    const contactHtml = options.contactInfo 
        ? `<p class="description-sm" style="margin-top: 10px;"><strong>Contato:</strong> ${options.contactInfo}</p>` 
        : '';

    const actionHtml = options.isMatch
        ? `<button class="label-font-m btn-like" disabled style="opacity: 0.5;">
             <img src="../assets/heart.svg" alt="Coração"> Match!
           </button>`
        : `<button class="label-font-m btn-like">
             <img src="../assets/heart.svg" alt="Coração"> Dar Like
           </button>`;

    card.innerHTML = `
        <div class="company-info">
            <div>
                <div><img class="img-company" src="../assets/company-green.svg" alt="Logo"></div>
                <div>
                    <h3 class="company-font">${options.subtitle}</h3>
                    <p class="location-font">
                        <img src="../assets/location.svg" alt="Localização"> ${options.location}
                    </p>
                </div>
            </div>
        </div>
        <div>
            <h2 class="job-title-font">${options.title}</h2>
            <p class="description-sm">${options.description}</p>
            ${contactHtml}
            <ul class="label-font-s" style="margin-top: 10px;">${skillsHtml}</ul>
        </div>
        <div>${actionHtml}</div>
    `;

    if (!options.isMatch && options.onLike) {
        const btnLike = card.querySelector(".btn-like") as HTMLButtonElement;
        btnLike.addEventListener('click', () => options.onLike!(btnLike));
    }

    return card;
}

function getFilteredJobs(currentUser: Candidate): Job[] {
    const allJobs = StorageService.getJobs();
    
    if (currentJobFilter === 'all') return allJobs;
    if (!currentUser || !currentUser.skills) return [];

    const userSkills = currentUser.skills.map(s => s.toLowerCase());
    return allJobs.filter(job => 
        (job.skills || []).some(skill => userSkills.includes(skill.toLowerCase()))
    );
}

function getMatchedCompanies(currentUser: Candidate): Company[] {
    if (!currentUser) return [];
    
    const matches = StorageService.getMatches().filter(m => m.cpf === currentUser.cpf);
    const companies = StorageService.getCompanies();

    return companies.filter(company => 
        matches.some(m => m.cnpj === company.cnpj)
    );
}

profileBtn?.addEventListener('click', (event) => {
    event.preventDefault();
    profileDropdown?.classList.toggle('show');
    
    if (!profileDropdown?.classList.contains('show')) return;

    const currentUser = StorageService.getCurrentUser() as Candidate;
    if (!currentUser) return;

    if (userDpName) userDpName.textContent = currentUser.name;
    if (userDpAge) userDpAge.textContent = currentUser.age?.toString() || '';
    if (userDpEmail) userDpEmail.textContent = currentUser.email;
    if (userDpLocation) userDpLocation.textContent = currentUser.localization;
    if (userDpDescription) userDpDescription.textContent = currentUser.description;

    if (userDpSkills) {
        userDpSkills.innerHTML = "";
        const skillsToRender = (currentUser.skills?.length) ? currentUser.skills : ["Nenhuma competência"];
        
        skillsToRender.forEach(skill => {
            const li = document.createElement("li");
            li.textContent = skill;
            userDpSkills.appendChild(li);
        });
    }
});

btnUpdateData?.addEventListener('click', (event) => {
    event.preventDefault();
    profileDropdown?.classList.remove('show');
    
    const currentUser = StorageService.getCurrentUser() as Candidate;
    if (currentUser) {
        if (updateName) updateName.value = currentUser.name;
        if (updateAge) updateAge.value = currentUser.age?.toString() || '';
        if (updateEmail) updateEmail.value = currentUser.email;
        if (updatePlace) updatePlace.value = currentUser.localization;
        if (updateDescription) updateDescription.value = currentUser.description;
        
        temporarySkills.length = 0;
        if (currentUser.skills) temporarySkills.push(...currentUser.skills);
        renderSkillsTags();
    }

    if (updateModal) updateModal.style.display = 'block';
});

saveUpdateBtn?.addEventListener('click', (event) => {
    event.preventDefault();
    
    const currentUser = StorageService.getCurrentUser() as Candidate;
    if (!currentUser) return;

    const updatedCandidate: Candidate = {
        ...currentUser, 
        name: updateName?.value || currentUser.name,
        age: parseInt(updateAge?.value) || currentUser.age,
        email: updateEmail?.value || currentUser.email,
        localization: updatePlace?.value || currentUser.localization,
        description: updateDescription?.value || currentUser.description,
        skills: [...temporarySkills]
    };

    StorageService.setCurrentUser(updatedCandidate);

    if (updateModal) updateModal.style.display = 'none';
    alert('Dados atualizados com sucesso!');
});

window.addEventListener('click', (event: Event) => {
    const target = event.target as HTMLElement;
    
    if (!target.closest('.profile-container') && !target.closest('.modal-content')) {
        profileDropdown?.classList.remove('show');
    }
    
    if (target === updateModal || target === closeModal) {
        if (updateModal) updateModal.style.display = 'none';
    }
});

btnLogout?.addEventListener('click', () => {
    StorageService.deleteCurrentUser();
    window.location.replace("./login.html");
});

export function renderJobs() {
    if (!jobsContainer) return;
    
    const currentUser = StorageService.getCurrentUser() as Candidate;
    const jobs = getFilteredJobs(currentUser);

    if (jobs.length === 0) {
        jobsContainer.innerHTML = currentJobFilter === 'recommended' 
            ? '<p class="description-m" style="text-align: center; margin-top: 2rem;">Nenhuma vaga recomendada encontrada.</p>'
            : hardcodedJobsHtml;
        return;
    }

    jobsContainer.innerHTML = '';

    jobs.forEach(job => {
        const card = createCard({
            title: job.name,
            subtitle: "empresa confidencial",
            location: `${job.jobType}, ${job.location}`,
            description: job.description,
            skills: job.skills || [],
            isMatch: false,
            onLike: (btn) => {
                MatchService.registerLikeJob(job.name);
                btn.innerHTML = `<img src="../assets/heart.svg" alt="Coração"> Interesse Enviado!`;
                btn.disabled = true;
                btn.style.opacity = '0.5';
            }
        });
        jobsContainer.appendChild(card);
    });
}

export function renderMatches() {
    if (!jobsContainer) return;
    
    const currentUser = StorageService.getCurrentUser() as Candidate;
    const matchedCompanies = getMatchedCompanies(currentUser);

    if (matchedCompanies.length === 0) {
        jobsContainer.innerHTML = '<p class="description-m" style="text-align: center; margin-top: 2rem;">Nenhum match encontrado.</p>';
        return;
    }

    jobsContainer.innerHTML = '';

    matchedCompanies.forEach(company => {
        const card = createCard({
            title: "Empresa Parceira",
            subtitle: company.name,
            location: company.localization,
            description: company.description,
            skills: company.skills || [],
            contactInfo: company.email,
            isMatch: true
        });
        jobsContainer.appendChild(card);
    });
}

btnJobsAvailable?.addEventListener('click', () => {
    btnJobsAvailable.classList.add('nav-font-selected');
    btnJobsAvailable.classList.remove('nav-font');
    
    if (btnMyMatches) {
        btnMyMatches.classList.add('nav-font');
        btnMyMatches.classList.remove('nav-font-selected');
    }
    renderJobs();
});

btnMyMatches?.addEventListener('click', () => {
    if (btnMyMatches) {
        btnMyMatches.classList.add('nav-font-selected');
        btnMyMatches.classList.remove('nav-font');
    }
    if (btnJobsAvailable) {
        btnJobsAvailable.classList.add('nav-font');
        btnJobsAvailable.classList.remove('nav-font-selected');
    }
    renderMatches();
});

btnFilterAllJobs?.addEventListener('click', () => {
    currentJobFilter = 'all';
    btnFilterAllJobs.classList.add('btn-selected');
    btnFilterRecommendedJobs?.classList.remove('btn-selected');
    renderJobs();
});

btnFilterRecommendedJobs?.addEventListener('click', () => {
    currentJobFilter = 'recommended';
    btnFilterRecommendedJobs.classList.add('btn-selected');
    btnFilterAllJobs?.classList.remove('btn-selected');
    renderJobs();
});

renderJobs();
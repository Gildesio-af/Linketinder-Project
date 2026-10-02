import { StorageService } from "../../services/storage/StorageService.js";
import { MatchService } from "../../services/match/MatchService.js";
const profileBtn = document.getElementById("profile-btn");
const btnUpdateData = document.getElementById("btn-update-data");
const btnLogout = document.getElementById("company-logout");
const btnDeleteCompany = document.getElementById("btn-delete-company");
const btnCandidates = document.getElementById("candidates");
const btnMyMatches = document.getElementById("my-matches-company");
const profileDropdown = document.getElementById("profile-dropdown");
const companyDpName = document.getElementById("company-dp-name");
const companyDpCnpj = document.getElementById("company-dp-cnpj");
const companyDpEmail = document.getElementById("company-dp-email");
const companyDpLocation = document.getElementById("company-dp-location");
const companyDpDescription = document.getElementById("company-dp-description");
const companyDpSkills = document.getElementById("company-dp-skills");
const updateModal = document.getElementById('company-update-modal');
const formUpdateCompany = document.getElementById('form-update-company');
const companyUpdateName = document.getElementById('company-update-name');
const companyUpdateCnpj = document.getElementById('company-update-cnpj');
const companyUpdateEmail = document.getElementById('company-update-email');
const companyUpdateLocation = document.getElementById('company-update-place');
const companyUpdateDescription = document.getElementById('company-update-description');
const updateSkillsContainer = document.getElementById('update-skills-container');
const updateSkillsInput = document.getElementById('update-skills-input');
const addSkillBtn = document.getElementById('add-skill-btn');
const closeModalBtn = document.querySelector('.close-modal');
const candidatesPanel = document.getElementById('candidates-panel');
const btnCreateJob = document.getElementById('job-form-btn');
const jobCreateModal = document.getElementById('job-create-modal');
const formCreateJob = document.getElementById('form-create-job');
const jobCreateTitle = document.getElementById('job-create-title');
const jobCreateType = document.getElementById('job-create-type');
const jobCreateLocation = document.getElementById('job-create-location');
const jobCreateDescription = document.getElementById('job-create-description');
const jobCreateSkillsContainer = document.getElementById('job-create-skills-container');
const jobCreateSkillsInput = document.getElementById('job-create-skills-input');
const jobAddSkillBtn = document.getElementById('job-add-skill-btn');
const closeJobModalBtn = document.querySelector('.close-modal-job');
let temporarySkills = [];
let jobTemporarySkills = [];
function setupSkillInput(inputEl, btnEl, containerEl, skillsArray) {
    const renderTags = () => {
        containerEl.querySelectorAll('.skill-tag').forEach(tag => tag.remove());
        skillsArray.forEach(skill => {
            const span = document.createElement('span');
            span.className = 'skill-tag';
            span.innerHTML = `${skill} <button type="button" aria-label="Remover ${skill}">&times;</button>`;
            span.querySelector('button')?.addEventListener('click', () => {
                const index = skillsArray.indexOf(skill);
                if (index > -1)
                    skillsArray.splice(index, 1);
            });
            containerEl.insertBefore(span, inputEl);
        });
    };
    const addSkill = () => {
        const skill = inputEl.value.trim();
        if (skill && !skillsArray.includes(skill)) {
            skillsArray.push(skill);
            renderTags();
            inputEl.value = '';
        }
    };
    btnEl.addEventListener('click', addSkill);
    inputEl.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            addSkill();
        }
    });
    return renderTags;
}
const renderCompanySkills = setupSkillInput(updateSkillsInput, addSkillBtn, updateSkillsContainer, temporarySkills);
const renderJobSkills = setupSkillInput(jobCreateSkillsInput, jobAddSkillBtn, jobCreateSkillsContainer, jobTemporarySkills);
function setupModalClosing() {
    window.addEventListener("click", (event) => {
        const target = event.target;
        if (!target.closest(".profile-container") && !target.closest(".modal-content")) {
            profileDropdown.classList.remove("show");
        }
        if (target === updateModal)
            updateModal.style.display = 'none';
        if (target === jobCreateModal)
            jobCreateModal.style.display = 'none';
    });
    closeModalBtn.addEventListener('click', () => updateModal.style.display = 'none');
    closeJobModalBtn.addEventListener('click', () => jobCreateModal.style.display = 'none');
}
setupModalClosing();
profileBtn.addEventListener("click", () => {
    profileDropdown.classList.toggle("show");
    if (!profileDropdown.classList.contains("show"))
        return;
    const currentUser = StorageService.getCurrentUser();
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
    }
    else {
        const li = document.createElement("li");
        li.textContent = "Nenhuma competência cadastrada";
        companyDpSkills.appendChild(li);
    }
});
btnUpdateData.addEventListener('click', () => {
    const currentCompany = StorageService.getCurrentUser();
    updateModal.style.display = 'block';
    companyUpdateName.value = currentCompany.name;
    companyUpdateCnpj.value = currentCompany.cnpj;
    companyUpdateEmail.value = currentCompany.email;
    companyUpdateLocation.value = currentCompany.localization;
    companyUpdateDescription.value = currentCompany.description;
    temporarySkills.length = 0;
    if (currentCompany.skills)
        temporarySkills.push(...currentCompany.skills);
    renderCompanySkills();
});
formUpdateCompany.addEventListener('submit', (event) => {
    event.preventDefault();
    const currentCompany = StorageService.getCurrentUser();
    const companyUpdated = {
        name: companyUpdateName.value,
        cnpj: companyUpdateCnpj.value,
        email: companyUpdateEmail.value,
        localization: companyUpdateLocation.value,
        description: companyUpdateDescription.value,
        likedCandidates: currentCompany.likedCandidates,
        skills: [...temporarySkills]
    };
    StorageService.updateCompany(companyUpdated);
    StorageService.setCurrentUser(companyUpdated);
    updateModal.style.display = "none";
    profileDropdown.classList.remove("show");
    alert("Dados atualizados com sucesso");
});
btnCreateJob.addEventListener('click', () => {
    jobCreateModal.style.display = 'block';
});
formCreateJob.addEventListener('submit', (event) => {
    event.preventDefault();
    const job = {
        name: jobCreateTitle.value,
        description: jobCreateDescription.value,
        salary: 0,
        location: jobCreateLocation.value,
        skills: [...jobTemporarySkills],
        jobType: jobCreateType.value
    };
    try {
        StorageService.saveJob(job);
        jobCreateModal.style.display = "none";
        formCreateJob.reset();
        jobTemporarySkills.length = 0;
        renderJobSkills();
        alert("Vaga cadastrada com sucesso!");
    }
    catch (error) {
        if (error instanceof Error) {
            alert(error.message);
        }
        else {
            alert("Ocorreu um erro desconhecido ao cadastrar a vaga.");
        }
    }
});
function createCandidateCard(candidate, index, isMatch) {
    const card = document.createElement('div');
    card.className = 'candidate-card';
    const skillsHtml = (candidate.skills || []).map(skill => `<li>${skill}</li>`).join('');
    const anonimousNumber = String(index + 1).padStart(3, '0');
    const title = isMatch ? candidate.name : `Candidato #${anonimousNumber}`;
    const emailHtml = isMatch ? `<p class="description-sm">${candidate.email}</p>` : '';
    const actionHtml = isMatch
        ? `<button class="btn-interest" disabled style="opacity: 0.5;">
             <img src="../assets/heart.svg" alt="Coração" width="24" height="24"> Match!
           </button>`
        : `<button class="btn-interest">
             <img src="../assets/heart-outline.svg" alt="Coração" width="24" height="24"> Demonstrar Interesse
           </button>`;
    card.innerHTML = `
        <div class="candidate-header">
            <div class="candidate-avatar"><img src="../assets/user.svg" alt="Avatar"></div>
            <h2 class="candidate-name-font">${title}</h2>
            ${emailHtml}
        </div>
        
        <div class="candidate-education">
            <h3 class="sub-title">Descrição</h3>
            <p class="description-sm">${candidate.description}</p>
        </div>

        <ul class="candidate-skills label-font-s">
            ${skillsHtml}
        </ul>

        <div class="candidate-actions">
            ${actionHtml}
        </div>
    `;
    if (!isMatch) {
        const btnInterest = card.querySelector(".btn-interest");
        btnInterest.addEventListener('click', () => {
            MatchService.registerLikeCompany(candidate.cpf);
            btnInterest.innerHTML = `Interesse Enviado!`;
            btnInterest.disabled = true;
            btnInterest.style.opacity = '0.5';
        });
    }
    return card;
}
function getRecommendedCandidates(company, allCandidates) {
    return allCandidates.filter(candidate => (company.skills || []).some(skill => (candidate.skills || []).includes(skill)));
}
export function renderCandidates() {
    if (!candidatesPanel)
        return;
    candidatesPanel.innerHTML = '';
    const currentUser = StorageService.getCurrentUser();
    const candidates = StorageService.getCandidates();
    const filteredCandidates = getRecommendedCandidates(currentUser, candidates);
    filteredCandidates.forEach((candidate, index) => {
        candidatesPanel.appendChild(createCandidateCard(candidate, index, false));
    });
}
export function renderMatches() {
    if (!candidatesPanel)
        return;
    candidatesPanel.innerHTML = '';
    const currentUser = StorageService.getCurrentUser();
    const matches = StorageService.getMatches().filter(m => m.cnpj === currentUser.cnpj);
    const candidates = StorageService.getCandidates();
    const matchedCandidates = candidates.filter(candidate => matches.some(m => m.cpf === candidate.cpf));
    if (matchedCandidates.length === 0) {
        candidatesPanel.innerHTML = '<p class="description-m" style="text-align: center; margin-top: 2rem;">Nenhum match encontrado.</p>';
        return;
    }
    matchedCandidates.forEach((candidate, index) => {
        candidatesPanel.appendChild(createCandidateCard(candidate, index, true));
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
btnLogout.addEventListener('click', () => {
    StorageService.deleteCurrentUser();
    window.location.replace("./login.html");
});
btnDeleteCompany.addEventListener('click', () => {
    const currentCompany = StorageService.getCurrentUser();
    StorageService.deleteCompany(currentCompany.cnpj);
    StorageService.deleteCurrentUser();
    window.location.replace("./login.html");
});
//# sourceMappingURL=CompanyMenu.js.map
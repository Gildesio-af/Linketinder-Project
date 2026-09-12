import { StorageService } from "../../services/storage/StorageService.js";
import type { Company, Candidate, Job } from "../../models/Domain.js";
import { MatchService } from "../../services/match/MatchService.js";

const profileBtn = document.getElementById("profile-btn") as HTMLButtonElement;
const btnUpdateData = document.getElementById("btn-update-data") as HTMLButtonElement;
const btnLogout = document.getElementById("company-logout") as HTMLButtonElement;
const btnDeleteCompany = document.getElementById("btn-delete-company") as HTMLButtonElement;

// Elements profile dropdown
const profileDropdown = document.getElementById("profile-dropdown") as HTMLDivElement;
const companyDpName = document.getElementById("company-dp-name") as HTMLSpanElement;
const companyDpCnpj = document.getElementById("company-dp-cnpj") as HTMLSpanElement;
const companyDpEmail = document.getElementById("company-dp-email") as HTMLSpanElement;
const companyDpLocation = document.getElementById("company-dp-location") as HTMLSpanElement;
const companyDpDescription = document.getElementById("company-dp-description") as HTMLSpanElement;

// Elements update company modal
const updateModal = document.getElementById('company-update-modal') as HTMLDivElement;
const formUpdateCompany = document.getElementById('form-update-company') as HTMLFormElement;
const companyUpdateName = document.getElementById('company-update-name') as HTMLInputElement;
const companyUpdateCnpj = document.getElementById('company-update-cnpj') as HTMLInputElement;
const companyUpdateEmail = document.getElementById('company-update-email') as HTMLInputElement;
const companyUpdateLocation = document.getElementById('company-update-place') as HTMLInputElement;
const companyUpdateDescription = document.getElementById('company-update-description') as HTMLTextAreaElement;

const candidatesPanel = document.getElementById('candidates-panel') as HTMLElement;


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
})

window.addEventListener("click", (event: Event) => {
    const target = event.target as HTMLElement;

    if (!target.closest(".profile-container") && !target.closest(".modal-content")) {
        if (profileDropdown.classList.contains("show")) {
            profileDropdown.classList.remove("show");
        }
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
        likedCandidates: currentCompany.likedCandidates
    };

    StorageService.updateCompany(companyUpdated);
    StorageService.setCurrentUser(companyUpdated);
    updateModal.style.display = "none";

    if (profileDropdown.classList.contains("show"))
        profileDropdown.classList.remove("show");

    alert("Dados atualizados com sucesso");
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


//     updateModal.style.display = "none";
// });

// window.addEventListener("click", (event) => {
//     if (event.target === updateModal) {
//         updateModal.style.display = "none";
//     }
// });
// });



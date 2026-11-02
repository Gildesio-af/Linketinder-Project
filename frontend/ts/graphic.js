import { StorageService } from "./services/storage/StorageService.js";
const topSkillsLinketinder = StorageService.getTopSkills();
const canvas = document.getElementById('competencias-chart');
const qtActiveProfiles = document.getElementById('qt-active-profiles');
const responseRateText = document.getElementById('response-rate-text');
const responseRateFill = document.getElementById('response-rate-fill');
const matchesMadeText = document.getElementById('matches-made-text');
const matchesMadeFill = document.getElementById('matches-made-fill');
const currentUser = StorageService.getCurrentUser();
const ctx = canvas.getContext('2d');
const desiredSkills = currentUser.skills?.map(skill => skill.toLocaleLowerCase()) || [];
const topSkillsDesired = topSkillsLinketinder
    .filter(skill => desiredSkills.includes(skill.name.toLocaleLowerCase()));
const xValues = topSkillsDesired.map(skill => skill.name);
const yValues = topSkillsDesired.map(skill => skill.count);
const colors = ['#006C49', '#4DE59C', '#8BE9B6', '#B4E3C9', '#C5D6CD'];
new Chart(ctx, {
    type: 'bar',
    data: {
        labels: xValues,
        datasets: [{
                data: yValues,
                backgroundColor: colors,
                borderRadius: 6,
                borderSkipped: 'bottom',
                barThickness: 80,
            }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { display: false },
            tooltip: {
                backgroundColor: '#1F2937',
                padding: 12,
                cornerRadius: 8
            }
        },
        scales: {
            x: {
                grid: { display: false },
                border: { display: false },
                ticks: {
                    font: { family: "'Inter', sans-serif", size: 15, weight: 600 },
                    color: '#4B5563'
                }
            },
            y: {
                grid: { color: '#6B7280' },
                border: { display: false },
                ticks: {
                    display: true,
                    font: { family: "'Inter', sans-serif", size: 16, weight: 600 },
                    color: '#6B7280',
                    padding: 8
                },
                beginAtZero: true
            }
        }
    }
});
function setQtActiveProfiles() {
    const qtActiveProfilesNumber = StorageService.getCandidates().length.toString().padStart(2, '0');
    qtActiveProfiles.textContent = qtActiveProfilesNumber;
}
function setEngagementMetrics() {
    if (!currentUser)
        return;
    const matches = StorageService.getMatches().filter(m => m.cnpj === currentUser.cnpj);
    const totalMatches = matches.length;
    if (matchesMadeText && matchesMadeFill) {
        matchesMadeText.textContent = totalMatches.toString();
        const matchPercentage = Math.min(100, totalMatches);
        matchesMadeFill.style.width = `${matchPercentage}%`;
    }
    // Usa o registro atualizado da empresa (a sessão pode estar desatualizada).
    const company = StorageService.getCompanies().find(c => c.cnpj === currentUser.cnpj) ?? currentUser;
    const likedCandidates = new Set(company.likedCandidates ?? []);
    const matchedLikedCandidates = new Set(matches.map(m => m.cpf).filter(cpf => likedCandidates.has(cpf)));
    const totalLikes = likedCandidates.size;
    let responseRate = 0;
    if (totalLikes > 0) {
        responseRate = Math.round((matchedLikedCandidates.size / totalLikes) * 100);
    }
    responseRate = Math.max(0, Math.min(100, responseRate));
    if (responseRateText && responseRateFill) {
        responseRateText.textContent = `${responseRate}%`;
        responseRateFill.style.width = `${responseRate}%`;
    }
}
setQtActiveProfiles();
setEngagementMetrics();
//# sourceMappingURL=graphic.js.map
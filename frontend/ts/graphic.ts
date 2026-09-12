import type { Company } from "./models/Domain.js";
import { StorageService } from "./services/storage/StorageService.js";

declare const Chart: any;

const topSkillsLinketinder: {name: string, count: number}[] = StorageService.getTopSkills();

const canvas = document.getElementById('competencias-chart') as HTMLCanvasElement;
const qtActiveProfiles = document.getElementById('qt-active-profiles') as HTMLParagraphElement;
const responseRateText = document.getElementById('response-rate-text') as HTMLSpanElement;
const responseRateFill = document.getElementById('response-rate-fill') as HTMLDivElement;
const matchesMadeText = document.getElementById('matches-made-text') as HTMLSpanElement;
const matchesMadeFill = document.getElementById('matches-made-fill') as HTMLDivElement;
const currentUser = StorageService.getCurrentUser() as Company;
const ctx = canvas.getContext('2d');


const desiredSkills: string[] = currentUser.skills?.map(skill => skill.toLocaleLowerCase()) as string[] || [];
const topSkillsDesired: {name: string, count: number}[] = topSkillsLinketinder
                        .filter(skill => desiredSkills.includes(skill.name.toLocaleLowerCase()));

const xValues: string[] = topSkillsDesired.map(skill => skill.name);

const yValues: number[] = topSkillsDesired.map(skill => skill.count);
const colors: String[] = ['#006C49', '#4DE59C', '#8BE9B6', '#B4E3C9', '#C5D6CD'];

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
    const qtActiveProfilesNumber: string = StorageService.getCandidates().length.toString().padStart(2, '0');
    qtActiveProfiles.textContent = qtActiveProfilesNumber;
}
function setEngagementMetrics() {
    if (!currentUser) return;
    
    const matches = StorageService.getMatches().filter(m => m.cnpj === currentUser.cnpj);
    const totalMatches = matches.length;
    
    if (matchesMadeText && matchesMadeFill) {
        matchesMadeText.textContent = totalMatches.toString();
        const matchPercentage = Math.min(100, totalMatches); 
        matchesMadeFill.style.width = `${matchPercentage}%`;
    }

    const totalLikes = currentUser.likedCandidates?.length || 0;
    let responseRate = 0;
    if (totalLikes > 0) {
        responseRate = Math.round((totalMatches / totalLikes) * 100);
    }
    
    if (responseRateText && responseRateFill) {
        responseRateText.textContent = `${responseRate}%`;
        responseRateFill.style.width = `${responseRate}%`;
    }
}

setQtActiveProfiles();
setEngagementMetrics();

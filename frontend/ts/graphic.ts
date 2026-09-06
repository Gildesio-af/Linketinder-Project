declare const Chart: any;

const canvas = document.getElementById('competencias-chart') as HTMLCanvasElement;
const ctx = canvas.getContext('2d');

const xValues: string[] = ['Python', 'TypeScript', 'React', 'Java', 'AWS'];
const yValues: number[] = [85, 65, 55, 40, 30];
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


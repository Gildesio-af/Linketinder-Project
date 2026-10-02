const skillsContainer = document.getElementById('skills-container') as HTMLDivElement;
const skillInput = document.getElementById('skill-input') as HTMLInputElement;
const addSkillBtn = document.getElementById('add-skill-btn') as HTMLButtonElement;

export let skillsArray: string[] = [];

if (skillsContainer && skillInput && addSkillBtn) {
    function renderSkills() {
        if (!skillsContainer || !skillInput) return;

        const oldTags = skillsContainer.querySelectorAll('.skill-tag');
        oldTags.forEach(tag => tag.remove());

        skillsArray.forEach((skill, index) => {
            const spanTag = document.createElement('span');
            spanTag.className = 'skill-tag';
            spanTag.textContent = skill + ' ';

            const btnDelete = document.createElement('button');
            btnDelete.type = 'button';
            btnDelete.textContent = '×';

            btnDelete.onclick = () => {
                skillsArray.splice(index, 1);
                renderSkills();
            }

            spanTag.appendChild(btnDelete);
            skillsContainer.insertBefore(spanTag, skillInput);
        });
    }

    addSkillBtn.addEventListener('click', () => {
        const newSkill = skillInput.value.trim();

        if (newSkill !== '' && !skillsArray.includes(newSkill)) {
            skillsArray.push(newSkill);
            skillInput.value = '';
            renderSkills();
        }
    });

    skillInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            event.preventDefault();
            addSkillBtn.click();
        }
    });
}
    

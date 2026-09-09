import { StorageService } from "../../services/StorageService.js";
import { skillsArray } from "../../services/tag/TagsService.js";
const formRegistration = document.getElementById("form-user-registration");
if (formRegistration) {
    formRegistration.addEventListener('submit', (event) => {
        event.preventDefault();
        const name = document.getElementById('name').value;
        const age = Number(document.getElementById('age').value);
        const cpf = document.getElementById('cpf').value;
        const email = document.getElementById('work-email').value;
        const localization = document.getElementById('place').value;
        const password = document.getElementById('password').value;
        const newPassword = document.getElementById('confirm-password').value;
        const description = document.getElementById('learning-description').value;
        if (password !== newPassword) {
            alert("As senhas não coincidem!");
            return;
        }
        const newCandidate = {
            name: name,
            age: age,
            cpf: cpf,
            email: email,
            localization: localization,
            password: password,
            description: description,
            skills: skillsArray
        };
        try {
            StorageService.saveCandidate(newCandidate);
            alert("Candidato cadastrado com sucesso!");
            formRegistration.reset();
            window.location.replace('./login.html');
        }
        catch (error) {
            if (error instanceof Error) {
                alert(error.message);
            }
        }
    });
}
//# sourceMappingURL=UserRegistration.js.map
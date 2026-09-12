import type { Candidate } from "../../models/Domain.js";
import { StorageService } from "../../services/storage/StorageService.js";
import { skillsArray } from "../../services/tag/TagsService.js"

const formRegistration = document.getElementById("form-user-registration") as HTMLFormElement | null;

if (formRegistration) {
    formRegistration.addEventListener('submit', (event) => {
        event.preventDefault();

        const name = (document.getElementById('name') as HTMLInputElement).value;
        const age = Number((document.getElementById('age') as HTMLInputElement).value);
        const cpf = (document.getElementById('cpf') as HTMLInputElement).value;
        const email = (document.getElementById('work-email') as HTMLInputElement).value;
        const localization = (document.getElementById('place') as HTMLInputElement).value;
        const password = (document.getElementById('password') as HTMLInputElement).value;
        const newPassword = (document.getElementById('confirm-password') as HTMLInputElement).value;
        const description = (document.getElementById('learning-description') as HTMLInputElement).value;

        if (password !== newPassword) {
            alert("As senhas não coincidem!");
            return;
        }

        const newCandidate: Candidate = {
            name: name,
            age: age,
            cpf: cpf,
            email: email,
            localization: localization,
            password: password,
            description: description,
            skills: skillsArray,
            likedJobs: []
        };

        try {
            StorageService.saveCandidate(newCandidate);
            alert("Candidato cadastrado com sucesso!");
            formRegistration.reset();
            window.location.replace('./login.html');
        } catch (error) {
            if (error instanceof Error) {
                alert(error.message);
            }
        }
    });
}




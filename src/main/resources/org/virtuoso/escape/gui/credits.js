/**
 * Interfaces for the credits screen.
 * @author aheuer
 */
window.app = window.app || {};

function shuffleNames() {
    const name_div = document.getElementById("name-list");
    const email_div = document.getElementById("email-list");
    const name_list = Array.from(name_div.children);
    const email_list = Array.from(email_div.children);
    var [shuffledNames, shuffledEmails] = shuffleArrays(name_list, email_list)
    name_div.innerHTML = '';
    email_div.innerHTML = '';
    shuffledNames.forEach(name => name_div.appendChild(name));
    shuffledEmails.forEach(email => email_div.appendChild(email));

}

function shuffleArrays(input_names, input_emails) {
    let return_names = [];
    let return_emails = [];
    const num_names = input_names.length;
    for (let i=0; i<num_names; i++) {
        const index_to_place = Math.floor(Math.random() * input_names.length);
        return_names[i] = input_names[index_to_place];
        return_emails[i] = input_emails[index_to_place];
        input_names.splice(index_to_place,1);
        input_emails.splice(index_to_place,1)
    }
    return [return_names, return_emails];
}


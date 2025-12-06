/**
 * Interfaces for the credits screen.
 * @author aheuer
 */
/**
 * Must be defined in java.
 *  @type {App}
 */
window.app = window.app || {};

/** Populate the leaderboard with values from the game state. */
function populateLeaderboard() {
    const leaderboard = document.getElementById("leaderboard");
    const input = app.getLeaderboardElements();
    for (let i = 0; i < input.length; i++) {
        var label = document.createElement("label");
        label.textContent = input[i];
        leaderboard.appendChild(label);
    }
}

/** Populate the run information section with values from the game state. */
function populateRunInfo() {
    const info = app.getRunInfo();
    document.getElementById("time_remaining").textContent = info[0];
    document.getElementById("final_score").textContent = info[1];
    document.getElementById("hints_used").textContent = info[2];
    document.getElementById("difficulty").textContent = info[3];
}

/** Shuffle the names and emails to display the credits in a random order. */
function shuffleNames() {
    const name_div = document.getElementById("name-list");
    const email_div = document.getElementById("email-list");
    const name_list = Array.from(name_div.children);
    const email_list = Array.from(email_div.children);
    var [shuffledNames, shuffledEmails] = shuffleArrays(name_list, email_list);
    name_div.innerHTML = "";
    email_div.innerHTML = "";
    shuffledNames.forEach((name) => name_div.appendChild(name));
    shuffledEmails.forEach((email) => email_div.appendChild(email));
}

/** Shuffle 2 arrays in the same order relative to each other and return both.
 * @param {String[]} input_names The names to shuffle
 * @param {String[]} input_emails The emails to shuffle
 * @returns {String[][]} An array that holds the shuffled names and emails.
 */
function shuffleArrays(input_names, input_emails) {
    let return_names = [];
    let return_emails = [];
    const num_names = input_names.length;
    for (let i = 0; i < num_names; i++) {
        const index_to_place = Math.floor(Math.random() * input_names.length);
        return_names[i] = input_names[index_to_place];
        return_emails[i] = input_emails[index_to_place];
        input_names.splice(index_to_place, 1);
        input_emails.splice(index_to_place, 1);
    }
    return [return_names, return_emails];
}

/**
 * Update the key handler to click logout when the user presses "l" and exit when the user presses "e".
 */
function updateKeyHandlers() {
    document.onkeydown = (event) => {
        let eventKey;
        if (event.keyIdentifier === undefined) eventKey = event.key;
        else
            try {
                eventKey = String.fromCodePoint(
                    "0x" + event.keyIdentifier.substring(2)
                ).toLowerCase();
            } catch (_) {
                return;
            }

        if (eventKey === "l") app.logout();
        else if (eventKey === "e") app.exit();
    };
}

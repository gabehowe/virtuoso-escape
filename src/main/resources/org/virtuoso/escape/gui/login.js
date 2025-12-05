/**
 * Interfaces for the login screen.
 * @author gabri
 */
/**
 * An instance of LoginController to be set from java.
 * @type {{toggleAuthMode: () => [ string, string, string ], tryAuth: (user: string, pass: string) => string}}
 */
window.app = window.app || {};

/**
 * Update the key handler to change authentication or exit if 'c' or 'e' is pressed.
 */
function updateKeyHandler(key) {
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
        if (document.activeElement.tagName === "INPUT") return;
        if (eventKey === 'c') document.getElementById("auth-change").click();
        else if (eventKey === 'e') app.exit();

    };
}

/**
 * Toggle the auth mode and change messaging to reflect the change.
 */
function toggleAuthMode() {
    let j = app.toggleAuthMode();
    let [prompt, change, welcome] = j.map((it) => it.toString());
    document.getElementById("auth-prompt").innerText = prompt;
    document.getElementById("welcome-text").innerText = welcome;
    document.getElementById("auth-change").innerHTML = `[<u><b>${change.charAt(
        0
    )}</b></u>${String(change).substring(1)}]`;
    updateKeyHandler(change.charAt(0).toLowerCase());
}

/**
 * Try to authenticate with the currently typed credentials.
 */
function tryAuth() {
    document.getElementById("auth-error").innerText = app.tryAuth(
        document.getElementById("username").value,
        document.getElementById("password").value
    );
}

/**
 * Initialize.
 */
function init() {
    addEventListener("submit", (ev) => ev.preventDefault());
    document.getElementById("username").focus();
}

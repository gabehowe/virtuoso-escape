/**
 * @type {{toggleAuthMode: () => [ String, string, string ], tryAuth: (user: string, pass: string) => string}}
 */
window.app = window.app || {};

/**
 * Update the key handler to click auth change if {@link key} is pressed
 * @param {string} key - The key to compare the pressed key with.
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

        if (eventKey === key) document.getElementById("auth-change").click();
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

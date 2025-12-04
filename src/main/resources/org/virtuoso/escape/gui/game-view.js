/**
 * Provides methods for interfacing with the game view.
 * @author gabri
 */

/**
 * An instance of GameViewController.
 * @typedef App
 * @property {() => string} debugCheckDifficulty - Returns the current difficulty.
 * @property {() => string} getTime - Returns the current time.
 * @property {(input: string) => void} input - Tries the input action with the current input.
 * @property {() => void} updateButtons - Create keyboard hotkeys.
 * @property {() => void} toggleTTS - Toggle text to speech.
 * @property {() => void} debugEndGame - End the game.
 * @property {() => void} exit - Close the window.
 * @property {(difficultyId: ("VIRTUOSIC"|"TRIVIAL"|"SUBSTANTIAL")) => void} pickDifficulty - change the difficulty to difficultyId.
 * @property {() => string[]} debugGetFloors - Returns an array of floor names.
 * @property {(floorId: string) => void} debugSetFloor - Sets the floor to floorId.
 */

/**
 * Must be defined in java.
 *  @type {App}
 */
window.app = window.app || {};
let debug = {};

/**
 * Initialize the game view screen.
 */
function init() {
    debug = {
        enabled: false,
        selected: () =>
            Array.from(document.querySelectorAll(".selected"))
                .map((it) => it.id)
                .join("; "),
        difficulty: () => app.debugCheckDifficulty(),
    };
    clearSettings();
    document.updateBox = updateBox;
    addEventListener("submit", (ev) => ev.preventDefault());
    addEventListener("keydown", (event) => {
        if (event.keyCode === 27) clearSettings();
        let tagName = document.activeElement.tagName;
        if (tagName === "INPUT") return;
        for (let key of Object.keys(keyMap)) {
            let eventKey = getKeyEventKey(event);
            if (eventKey !== key) continue;
            keyMap[key].click();
            event.preventDefault();
        }
    });
    let timeAnimator = () => {
        setTimeout(timeAnimator, 250);
        document.getElementById("timer").innerText = app.getTime();
        if (debug["enabled"]) {
            let dbgE = document.getElementById("debug");
            dbgE.innerHTML = "";
            for (const debugKey in debug) {
                let debugEntry = document.createElement("span");
                let out;
                if (typeof debug[debugKey] == "function") {
                    try {
                        out = debug[debugKey]();
                    } catch (e) {
                        out = e;
                    }
                } else out = debug[debugKey];
                debugEntry.innerHTML = `<strong>${debugKey}</strong> ${out}`;
                dbgE.append(debugEntry);
            }
            document.getElementById("debug");
        }
    };
    timeAnimator();
}

/**
 * Get the key from a keyboard event. Supports multiple browsers and JavaFX to enable some future conversion.
 * @param {KeyboardEvent} event - The event to find the key from.
 * @returns {String|null}
 */
function getKeyEventKey(event) {
    let eventKey;
    if (event.keyIdentifier === undefined) eventKey = event.key;
    else
        try {
            eventKey = String.fromCodePoint(
                "0x" + event.keyIdentifier.substring(2)
            ).toLowerCase();
        } catch (_) {
            return null;
        }
    return eventKey;
}

/**
 * Update a left bar box with a currently selected value.
 * @param {string} id - The id of the box to change.
 * @param {string} current - The id of the currently selected button.
 * @param {string[]} rooms - The list of all rooms (including current)
 * @param {boolean} button - Whether the elements should be buttons.
 */
function updateBox(id, current, rooms, button) {
    let mapBox = document.getElementById(id);
    mapBox.querySelectorAll(".box-element").forEach((it) => it.remove());
    for (let i of rooms) {
        let elem = makeLogicalButton(i[1], i[0], button);
        elem.classList.add("box-element");
        if (i[1] == current) elem.classList.add("selected");
        mapBox.append(elem);
    }
}

/**
 * Create an input box to take respond via {@link window#app.input}
 */
function createInputBox() {
    if (document.getElementById("speak").style.display === "none") return;
    let box = document.getElementById("input-box");
    box.style.display = "";
    box.disabled = false;
    let input = document.getElementById("input");
    input.disabled = false;
    input.focus();
    input.value = "";
    let inputbox = document.getElementById("input-box");
    inputbox.onsubmit = () => {
        inputbox.style.display = "none";
        inputbox.disabled = true;
        input.disabled = true;
        app.input(document.getElementById("input").value);
    };
}

/**
 * @enum {string}
 * @readonly
 */
const Settings = Object.freeze({
    SETTINGS: "SETTINGS",
    DEBUG: "DEBUG",
    DIFFICULTY: "DIFFICULTY",
    CHANGE_FLOOR: "CHANGE_FLOOR",
});

/**
 * Clear the settings box.
 */
function clearSettings() {
    let box = document.getElementById("settings-box");
    while (box.firstChild) box.firstChild.remove();
    box.style.display = "none";
    createKeys();
}

/**
 * Display a settings menu.
 * @param {Settings} name - The settings menu to display.
 */
function displaySettings(name) {
    clearSettings();
    let box = document.getElementById("settings-box");
    box.style.display = "";
    switch (name) {
        case Settings.SETTINGS: {
            let diff = makeLogicalButton(
                "setting-change-difficulty",
                "Change Difficulty",
                true
            );
            let debug = makeLogicalButton("setting-debug", "Debug", true);
            debug.style.color = "PaleGreen";
            let exit = makeLogicalButton("setting-exit", "Exit", true);
            diff.onclick = () => displaySettings(Settings.DIFFICULTY);
            debug.onclick = () => displaySettings(Settings.DEBUG);
            exit.onclick = () => {
                app.exit();
                clearSettings();
            };
            box.append(diff, debug, exit);
            break;
        }
        case Settings.DEBUG: {
            let changeFloor = makeLogicalButton(
                "debug-change-floor",
                "Change Floor",
                true
            );
            let endGame = makeLogicalButton("debug-end-game", "End game", true);
            let enableDebugMenu = makeLogicalButton(
                "debug-enable-menu",
                "Debug Menu",
                true
            );
            changeFloor.onclick = () => displaySettings(Settings.CHANGE_FLOOR);
            endGame.onclick = () => app.debugEndGame();
            enableDebugMenu.onclick = () => {
                debug["enabled"] = !debug["enabled"];
                document.getElementById("debug").style.display = debug[
                    "enabled"
                ]
                    ? ""
                    : "none";
                clearSettings();
            };
            box.append(
                ...[changeFloor, endGame, enableDebugMenu].map((it) => {
                    it.style.color = "PaleGreen";
                    return it;
                })
            );
            break;
        }
        case Settings.DIFFICULTY: {
            let difficulties = ["VIRTUOSIC", "SUBSTANTIAL", "TRIVIAL"];
            for (let i of difficulties) {
                let difficultyButton = makeLogicalButton(
                    "change-difficulty" + difficulties,
                    i,
                    true
                );
                difficultyButton.onclick = () => {
                    app.pickDifficulty(i);
                    clearSettings();
                };
                box.append(difficultyButton);
            }
            break;
        }
        case Settings.CHANGE_FLOOR: {
            let floors = app.debugGetFloors();
            for (let i of floors) {
                let elem = makeLogicalButton("change-floor-" + i, i, true);
                elem.style.color = "PaleGreen";
                elem.onclick = () => {
                    app.debugSetFloor(i);
                    clearSettings();
                };
                box.append(elem);
            }
        }
    }
    createKeys();
}

/**
 * A global mapping of keyboard buttons to logical button callbacks.
 * @type {Object.<string, Element>}
 */
let keyMap = {};

/**
 * Find all logical buttons and create callback keys for them.
 */
function createKeys() {
    // Find action box buttons to ensure they stay the same between createKeys calls.
    let actions = document.querySelectorAll("#action-box > .logical-button");
    let buttons = document.querySelectorAll(".logical-button");
    let selected = document.querySelectorAll(".selected");
    keyMap = {};
    // let fixed = Array.from(buttons).filter(it => it.hasAttribute('keyboard'))
    // Find a unique letter in elem.innerText.
    let findValidKey = (sourceKey, elem) => {
        if (Object.values(keyMap).includes(elem)) {
            let key = Object.keys(keyMap).filter(
                (it) => keyMap[it].id === elem.id
            )[0];
            return elem.textContent
                .charAt(elem.textContent.toLowerCase().indexOf(key))
                .toString();
        }
        for (let c of Array.from(sourceKey)) {
            if (
                c.match(/\w+$/) != null &&
                !Object.keys(keyMap).includes(c.toLowerCase())
            ) {
                keyMap[c.toLowerCase()] = elem;
                return c;
            }
        }
        return null;
    };
    // Try to find actions and fixed buttons first.
    actions.forEach((it) => findValidKey(it.textContent, it));
    for (let elem of buttons) {
        console.assert(elem.id.length !== 0);
        let sourceKey = elem.textContent;
        findValidKey(sourceKey, elem);
    }
    for (let k in keyMap) {
        let elem = keyMap[k];
        if (elem.matches(".selected")) continue;
        let sourceKey = elem.textContent;
        sourceKey = sourceKey.replace(/\[(.+?)]/, "$1");
        let index = sourceKey.toLowerCase().indexOf(k);
        let visualKey = sourceKey.charAt(index);
        elem.innerHTML = "";
        elem.append(
            "[",
            sourceKey.substring(0, index),
            new DOMParser().parseFromString(
                `<u><b>${visualKey}</b></u>`,
                "text/html"
            ).body.firstElementChild,
            sourceKey.substring(index + 1),
            "]"
        );
        elem.setAttribute("keyboard", "true");
    }
}

/**
 * Replace all text in node with typewriter animation text recursively.
 * @param {Node} node - The node in which to replace text nodes with animated text.
 * @param {number} count - How many characters before this node have already been replaced. This is necessary to offset animation starting times for every node.
 * @returns {number} the new {@link count}.
 */
function recurseTypewriter(node, count) {
    const typewriterDelay = 16;
    if (node.nodeType === 3) {
        let j = document.createElement("div");
        j.style.display = "inline";
        Array.from(node.textContent).forEach((str) => {
            let elem = document.createElement("span");
            count += 1;
            elem.style.animation =
                count * typewriterDelay + "ms step-end fadeIn";
            elem.textContent = str;
            j.appendChild(elem);
        });
        node.replaceWith(j);
    } else if (node.nodeType === 1) {
        for (let i = 0; i < node.childNodes.length; i++) {
            count = recurseTypewriter(node.childNodes.item(i), count);
        }
    }
    return count;
}

/**
 * Set the dialogue box to a {@link text}, which will recursively be animated with {@link recurseTypewriter}.
 * @param {string} text - The text to put in the dialogue box.
 */
function setDialogue(text) {
    let messageBox = document.getElementById("message");
    messageBox.innerHTML = text;
    recurseTypewriter(messageBox, 0);
}

/**
 * Place entities in the center of the screen.
 * @param {string} current - The image name of the currently selected entity in resources/images, not including .png.
 * @param {string[]} entities - The image names of all entities including {@link current} in resources/images, not including .png.
 */
function populateBackground(current, entities) {
    let findCurrent = document.querySelector(
        "#background-entities > img.selected"
    );
    if (findCurrent?.src.includes(current)) return;
    let predefined = document.querySelectorAll("#background-entities > img");
    if (
        predefined.length > 0 &&
        Array.from(predefined)
            .map((it) => it.src)
            .every((it) => Array.from(entities).some((key) => it.includes(key)))
    ) {
        document
            .querySelectorAll("#background-entities > img.selected")
            .forEach((it) => it.classList.remove("selected"));
        document
            .querySelector(`#background-entities > img[src*="${current}"]`)
            ?.classList?.add("selected");
        return;
    }
    let flow = document.getElementById("background-entities");
    flow.innerHTML = "";
    for (const url of entities) {
        let picture = document.createElement("img");
        if (url === current) picture.classList.add("selected");
        picture.onload = () =>
            (picture.width *= url.includes("elephant") ? 0.9 : 0.7);
        picture.src = `../../../../images/${url}.png`;
        picture.id = `img-${url}`;
        picture.style.animationDelay = -2 * Math.random() + "s";
        flow.append(picture);
    }
}

/**
 * Set the background image of the game view.
 * @param {string} url - The id of the current image in resources/images (not including .png)
 */
function setRoomImage(url) {
    document.getElementById(
        "viewport"
    ).style.backgroundImage = `url('../../../../images/${url}.png')`;
}

/**
 * Create a logical button element.
 * @param {string} id - The id of the element.
 * @param {string} text - The inner text of the element.
 * @param {boolean} button - Whether the element should be clickable.
 * @returns {HTMLSpanElement} The new element.
 */
function makeLogicalButton(id, text, button) {
    let elem = document.createElement("span");
    if (button) elem.classList.add("logical-button");
    elem.id = id;
    elem.innerText = text;
    return elem;
}

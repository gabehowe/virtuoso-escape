console.log = i => {
    logger.log_(i)
}
window.onerror = e => {
    console.log(e);
    logger.log_(e)
}

function makeElement(id, text, button) {
    let elem = document.createElement('span')
    if (button) elem.classList.add('logical-button')
    elem.id = id;
    elem.innerText = text;
    return elem
}

function updateBox(name, current, rooms, button) {
    let mapBox = document.getElementById(name)
    mapBox.querySelectorAll('.box-element').forEach(it => it.remove())
    for (let i of rooms) {
        let elem = makeElement(i[1], i[0], button)
        elem.classList.add('box-element')
        if (i[1] == current) elem.classList.add('selected')
        mapBox.append(elem)
    }
}

function tryInput() {
    document.getElementById('input-box').style.display = "none";
    app.input(document.getElementById('input').value)
}

function createInputBox() {
    if (document.getElementById('speak').style.display === "none") return
    let box = document.getElementById('input-box');
    box.style.display = "";
    let input = document.getElementById('input');
    input.focus()
    input.value = ""
}

const Settings = Object.freeze({
    SETTINGS: "SETTINGS",
    DEBUG: "DEBUG",
    DIFFICULTY: "DIFFICULTY",
    CHANGE_FLOOR: "CHANGE_FLOOR"
})

function clearSettings() {
    let box = document.getElementById('settings-box')
    while (box.firstChild) box.firstChild.remove()
    box.style.display = "none"
    app.updateButtons()
}

function displaySettings(name) {
    clearSettings()
    let box = document.getElementById('settings-box')
    box.style.display = ""
    switch (name) {
        case Settings.SETTINGS: {
            let diff = makeElement("setting-change-difficulty", "Change Difficulty", true);
            let toggleTTS = makeElement("setting-toggle-tts", "Toggle TTS", true);
            let debug = makeElement("setting-debug", "Debug", true);
            debug.style.color = 'green'
            let exit = makeElement("setting-exit", "Exit", true)
            diff.onclick = () => displaySettings(Settings.DIFFICULTY)
            toggleTTS.onclick = () => {
                app.toggleTTS();
                clearSettings()
            }
            debug.onclick = () => displaySettings(Settings.DEBUG)
            exit.onclick = () => {
                app.exit()
                clearSettings()
            }
            box.append(diff, toggleTTS, debug, exit)
            break;
        }
        case Settings.DEBUG: {
            let changeFloor = makeElement("debug-change-floor", "Change Floor", true);
            let endGame = makeElement('debug-end-game', "End game", true)
            changeFloor.onclick = () => displaySettings(Settings.CHANGE_FLOOR)
            endGame.onclick = () => app.endGame()
            box.append(...[changeFloor, endGame].map(it => {
                it.style.color = "green";
                return it
            }))
            break;
        }
        case Settings.DIFFICULTY: {

            break;
        }
        case Settings.CHANGE_FLOOR: {
            let floors = JSON.parse(app.getFloors());
            for (let i of floors) {
                let elem = makeElement("change-floor-" + i, i, true);
                elem.style.color = "green";
                elem.onclick = () => {
                    app.setFloor(i);
                    clearSettings()
                }
                box.append(elem)
            }
        }
    }
    app.updateButtons()

}

function setTextOnElement(id, text) {
    document.getElementById(id).innerHTML = text
}

const typewriterDelay = 16;

function setDialogue(text) {
    let messageBox = document.getElementById('message')
    messageBox.innerHTML = text;
    let count = 0;
    for (let i = 0; i < messageBox.childNodes.length; i++) {
        let cnode = messageBox.childNodes.item(i)
        if (cnode.nodeType !== 3) continue;
        messageBox.childNodes.item(i).replaceWith(...Array.from(cnode.textContent).map(str => {
            let elem = document.createElement('span');
            elem.style.animation = count++ * typewriterDelay + 'ms step-end fadeIn';
            elem.textContent = str;
            return elem;
        }))
    }
}

function init() {
    clearSettings()
    document.updateBox = updateBox
    document.addEventListener('keydown', ev => {
        if (ev.keyCode === 27) clearSettings()
    })
    addEventListener('submit', ev => ev.preventDefault())

}
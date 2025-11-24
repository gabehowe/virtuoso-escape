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
    SETTINGS: "SETTINGS", DEBUG: "DEBUG", DIFFICULTY: "DIFFICULTY", CHANGE_FLOOR: "CHANGE_FLOOR"
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

let keyMap = {};

function createKeys() {
    var actions = document.querySelectorAll('#action-box > .logical-button')
    var buttons = document.querySelectorAll('.logical-button')
    var selected = document.querySelectorAll('.selected')
    keyMap = {}
    let fixed = Array.from(buttons).filter(it => it.hasAttribute('keyboard'))

    let findValidKey = (sourceKey, elem) => {
        if (Object.values(keyMap).includes(elem)) {
            let key = Object.keys(keyMap).filter(it => keyMap[it].id === elem.id)[0]
            return elem.textContent.charAt(elem.textContent.toLowerCase().indexOf(key)).toString()
        }
        for (let c of Array.from(sourceKey)) {
            if (c.match(/\w+$/) != null && !Object.keys(keyMap).includes(c.toLowerCase())) {
                keyMap[c.toLowerCase()] = elem;
                return c;
            }
        }
        return null;
    }
    actions.forEach(it => findValidKey(it.textContent, it))
    fixed.forEach(it => findValidKey(it.textContent, it))
    for (let elem of buttons) {
        console.assert(elem.id.length !== 0)
        let sourceKey = elem.textContent;
        let key = findValidKey(sourceKey, elem)
        if (!elem.hasAttribute('keyboard') && key !== null && !Array.from(selected).includes(elem)) {
            let index = sourceKey.toLowerCase().indexOf(key.toLowerCase())
            elem.innerHTML = ""
            elem.append("[", sourceKey.substring(0, index), new DOMParser().parseFromString(`<u><b>${key}</b></u>`, 'text/html').body.firstElementChild, sourceKey.substring(index + 1), "]")
            elem.setAttribute('keyboard', "true")
        }
    }
}

function setDialogue(text) {
    const typewriterDelay = 16;
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

function populateBackground(current, others) {
    // if (document.getElementById('entity').src.includes(current)) return
    let findCurrent = document.querySelector('#background-entities > img.selected')
    if (findCurrent?.src.includes(current)) return
    let predefined = document.querySelectorAll('#background-entities > img');
    if (predefined.length > 0 && Array.from(predefined).map(it => it.src).every(it => Array.from(others).some(key => it.includes(key)))) {
        selectEntity(current)
        return
    }
    let flow = document.getElementById('background-entities')
    flow.innerHTML = ""
    for (const url of others) {
        let picture = document.createElement('img')
        if (url === current) picture.classList.add('selected')
        picture.onload = () => picture.width *= 0.7;
        picture.src = `../../../../images/${url}.png`
        picture.style.animationDelay = -2 * Math.random() + "s"
        flow.append(picture)
    }
}

function selectEntity(id) {
    document.querySelectorAll('#background-entities > img.selected').forEach(it => it.classList.remove('selected'))
    document.querySelector(`#background-entities > img[src*="${id}"]`).classList.add('selected')
}

function setRoomImage(url) {
    document.getElementById('viewport').style.backgroundImage = `url('../../../../images/${url}.png')`
}

function init() {
    console.error = i => logger.error(i)
    console.log = i => logger.log_(i)
    window.onerror = e => console.error(e);
    clearSettings()
    document.updateBox = updateBox
    document.addEventListener('keydown', ev => {
        if (ev.keyCode === 27) clearSettings()
    })
    addEventListener('submit', ev => ev.preventDefault())

    let keyboardHandler = (event) => {
        let tagName = document.activeElement.tagName
        if (tagName === "INPUT") return;
        let eventKey = (event.keyIdentifier === undefined) ? event.key : String.fromCodePoint('0x' + event.keyIdentifier.substring(2)).toLowerCase()
        for (let key of Object.keys(keyMap)) {
            if (eventKey !== key) continue;
            keyMap[key].click()
        }
    }
    addEventListener('keydown', keyboardHandler)
    timeAnimator()
}

function timeAnimator() {
    setTimeout(timeAnimator, 500)
    document.getElementById('timer').innerText = app.getTime()
}
